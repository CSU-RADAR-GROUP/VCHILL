#!/usr/bin/env python

"""
A VCHILL archive server for NetCDF files that uses threads to handle multiple clients at a time.
Last modified: 2007-07-13
"""

import array
import fpconst
import gzip
import os
import pycdf
import select
import socket
import struct
import sys
import threading

config = {}
exec open("vchill_netcdf_server.config") in config

rootdir = config.get('rootdir', u'.')
use64bit = config.get('use64bit', False)

def isValidDir (filename):
    if not os.path.isdir(filename): return 0
    return 1
def isNetCDF (filename):
    #if filename.endswith(".gz"): filename = filename[:-3] # strip compressed-ness
    if not filename.endswith((".nc", ".netcdf")): return 0
    if os.path.isdir(filename): return 0
    return 1

class Constants:
    hello = 0xf0f00f0f

    archCtl = 12
    genMomDat = 15

class Session (threading.Thread):
    def __init__ (self, control, sessionID):
        threading.Thread.__init__(self)
        self.angleScale = 0x7fffffff
        self.sessionID = sessionID
        self.control = control # control channel
        self.data = [] # data channels - only one supported for now
        self.dataTypes = 0 # requested data types
    def sendBookmarks (self): #deprecated
        follows = ResponsePacket(statusCode = ResponsePacket.Status.directoryFollows) # extStatus (length of file) = 0 for now
        follows.send(self.control)
        # send actual bookmark data here -- none for now
        sent = ResponsePacket(statusCode = ResponsePacket.Status.directorySent)
        sent.send(self.control)
    def sendStatus (self):
        response = ResponsePacket(statusCode = ResponsePacket.Status.statusUpdate, maxSweeps = 1)
        response.send(self.control)
    def sendDirList (self): #deprecated
        bufferArray = []
        for filename in os.listdir(rootdir):
            if isValidDir(filename): # filter here
                bufferArray.append(filename.replace(" ", "%20").encode("UTF-8") + '\n')
        buffer = "".join(bufferArray)
        follows = ResponsePacket(statusCode = ResponsePacket.Status.directoryFollows, extStatus = len(buffer), vol = 1)
        follows.send(self.control)
        self.control.send(buffer)
        sent = ResponsePacket(statusCode = ResponsePacket.Status.directorySent)
        sent.send(self.control)
    def sendFileList (self, dirname): #deprecated
        bufferArray = []
        dirname = dirname.replace("%20", " ")
        try:
            list = os.listdir(dirname)
        except:
            print "failed to list dir, defaulting to empty list"
            list = []
        for filename in list:
            if isNetCDF(dirname + "/" + filename): # filter here
                bufferArray.append((filename + " NetCDF\n").encode("UTF-8"))
        buffer = "".join(bufferArray)
        #print "   " + buffer
        follows = ResponsePacket(statusCode = ResponsePacket.Status.directoryFollows, extStatus = len(buffer), vol = 1)
        follows.send(self.control)
        self.control.send(buffer)
        sent = ResponsePacket(statusCode = ResponsePacket.Status.directorySent)
        sent.send(self.control)
    def sendDirectory (self, path):
        bufferArray = []
        path = path.replace("%20", " ")
        if len(path) < 1: path = rootdir # top level
        try:
            list = os.listdir(path)
        except:
            print "failed to list dir, defaulting to empty list"
            list = []
        for filename in list:
            if isValidDir(path + "/" + filename): # filter here
                bufferArray.append(path + "/" + filename.replace(" ", "%20").encode("UTF-8") + ' DIR\n')
            elif isNetCDF(path + "/" + filename): # filter here
                bufferArray.append((filename + " NetCDF\n").encode("UTF-8"))
            #else:
                #print "skipping " + filename
        bufferArray.sort()
        buffer = "".join(bufferArray)
        follows = ResponsePacket(statusCode = ResponsePacket.Status.directoryFollows, extStatus = len(buffer), vol = 1)
        follows.send(self.control)
        self.control.send(buffer)
        sent = ResponsePacket(statusCode = ResponsePacket.Status.directorySent)
        sent.send(self.control)
    def sendSweep (self, path):
        # if path.endswith(".gz"): nc = pycdf.CDF(gzip.open(path))
        #else:
        nc = pycdf.CDF(path.encode("US-ASCII"))
        if path.find("ncswp_") < 0: self.sendCASASweep(nc)
        else: self.sendNCARSweep(nc)
    def sendNCARSweep (self, nc):
        attr = nc.attributes(full = 1)
        dims = nc.dimensions(full = 1)
        vars = nc.variables()

        fields = nc.var("field_names")
        scales = []
        missing = {}
        sf = {}
        ao = {}
        numfields = dims["fields"][0]
        for number in range(0, numfields): #gather field names
            name = ''
            for index in range(0, dims["short_string"][0]):
                char = fields.get_1((number, index))
                if char.isalnum(): name = name + char
                #else: print "skipping '" + char + "'"
            #gather metadata
            field = nc.var(name)
            meta = field.attributes()
            currsf = meta["scale_factor"]
            currao = meta["add_offset"]
            scale = config.get('ncarScale', {}).get(name, {})
            max = scale.get('max', currsf * (32767 - currao))
            min = scale.get('min', currsf * (-32767 - currao))
            f = 10000
            b = int((255.0 * min  * f - max * f) / 254.0)
            s = int(min * f - b)
            scale = ChillMomentFieldScale(fieldName = name, fieldDescription = meta["long_name"], units = meta["units"], fieldNumber = number, factor = int(f), scale = int(s), bias = int(b), maxValue = int(max * f), minValue = int(min * f), dataWordCoding = scale.get('dataWordCoding', 0), colorMapType = scale.get('colortable', 0))
            scales.append(scale)
            missing[scale] = meta["missing_value"]
            sf[scale] = currsf
            ao[scale] = currao
        available = bitmaskToList(self.dataTypes, scales)
        for type in available:
            print "type " + type.fieldName + " was requested"
            if not type.fieldName in vars.keys():
                print " ... but not available"
                available.remove(type)

        numrays = dims['Time'][0]
        #print "num rays =", numrays
        numgates = dims['maxCells'][0]
        #print "num gates =", numgates

        ### control: send start
        response = ResponsePacket(statusCode = 256, ray = 1)
        response.send(self.control)
        ### control: send end
        response = ResponsePacket(statusCode = ResponsePacket.Status.endFile, ray = numrays)
        response.send(self.control)
        ### data: send scaling, send HSK, (send dataH, send data)*
        for d in self.data:
            #print "sending scaling info"
            for scale in scales: scale.send(d)
        id = attr['Instrument_Name'][0]
        lat = int(nc.var('Latitude').get() * 1e6)
        lon = int(nc.var('Longitude').get() * 1e6)
        gw = int(nc.var('Cell_Spacing').get() * 1e3)
        mode = attr['Scan_Mode'][0]
        #print "Mode =", mode
        if mode == 'RHI': mode = 1
        else: mode = 0
        hskH = ChillHousekeepingHeader(radarId = id, radarLatitude = lat, radarLongitude = lon, gateWidth = gw, angleScale = self.angleScale, antMode = mode)
        hskH.send(self.data[0])
        for radialI in range(0, numrays):
            #print "sending header for ray#", radialI + 1
            az = int(nc.var('Azimuth')[radialI] / 360 * self.angleScale)
            el = int(nc.var('Elevation')[radialI] / 360 * self.angleScale)
            sr = int(1e3 * nc.var('Range_to_First_Cell').get())
            time = nc.var("base_time").get() + nc.var('time_offset')[radialI]
            dt = int(time)
            ns = int((time - dt) * 1e9)
            data = [nc.var(type.fieldName)[radialI] for type in available] # get actual data into list of arrays
            dataH = ChillDataHeader(requestedData = self.dataTypes, availableData = listToBitmask(bitmaskToList(-1, scales)), startAz = az, startEl = el, endAz = az, endEl = el, numGates = numgates, startRange = sr, dataTime = dt, fractionalSecs = ns, rayNumber = radialI + 1)
            dataH.send(self.data[0])
            values = array.array('B')
            for gateI in range(0, numgates):
                #print "gate #", gateI
                for typeI in range(0, len(available)):
                    if use64bit: value = data[typeI][gateI][0]
                    else: value = data[typeI][gateI]
                    #print "value =", value
                    if value == missing[available[typeI]]:
                        value = 0
                    else:
                        value = sf[available[typeI]] * (value - ao[available[typeI]]) #convert short to floating pt
                        value = int((value * available[typeI].factor - available[typeI].bias) / available[typeI].scale) #convert to byte
                        if value > 255: value = 255
                        elif value < 1: value = 1 # 0 is missing
                    #print "packed =", value
                    values.append(value)
            string = values.tostring()
            #print string
            self.data[0].send(string)
    def sendCASASweep (self, nc):
        attr = nc.attributes(full = 1)
        dims = nc.dimensions(full = 1)
        vars = nc.variables()

        #get field names
        names = []
        for var in vars:
            #print 'considering', var
            if nc.var(var).inq_type() == pycdf.NC.FLOAT:
                #print "found:", var
                names.append(var)
            else:
                #print "not a valid field:", var
                pass
        #print 'done checking vars'
        scales = []
        number = 0
        for name in names:
            field = nc.var(name)
            meta = field.attributes()
            scale = config.get('casaScale', {}).get(name, {})
            max = scale.get('max', 255)
            min = scale.get('min', 1)
            f = 10000
            b = int((255.0 * min  * f - max * f) / 254.0)
            s = int(min * f - b)
            scale = ChillMomentFieldScale(fieldName = scale.get('fieldName', name), fieldDescription = name, units = meta["Units"], fieldNumber = number, factor = int(f), scale = int(s), bias = int(b), maxValue = int(max * f), minValue = int(min * f), dataWordCoding = scale.get('dataWordCoding', 0), colorMapType = scale.get('colortable', 0))
            scales.append(scale)
            number += 1

        available = bitmaskToList(self.dataTypes, scales)
        for type in available:
            #print "type " + type.fieldName + " was requested"
            if not type.fieldDescription in vars.keys():
                #print " ... but not available"
                available.remove(type)

        numrays = dims['Radial'][0]
        #print "num rays =", numrays
        numgates = dims['Gate'][0]
        #print "num gates =", numgates

        ### control: send start
        response = ResponsePacket(statusCode = 256, ray = 1)
        response.send(self.control)
        ### control: send end
        response = ResponsePacket(statusCode = ResponsePacket.Status.endFile, ray = numrays)
        response.send(self.control)
        ### data: send scaling, send HSK, (send dataH, send data)*
        for d in self.data:
            #print "sending scaling info"
            for scale in scales: scale.send(d)
        hskH = ChillHousekeepingHeader(radarId = attr['RadarName'][0], radarLatitude = int(attr['Latitude'][0] * 1e6), radarLongitude = int(attr['Longitude'][0] * 1e6), gateWidth = nc.var('GateWidth')[0], angleScale = self.angleScale)
        hskH.send(self.data[0])
        for radialI in range(0, numrays):
            #print "sending header for ray#", radialI + 1
            az = int(nc.var('Azimuth')[radialI] / 360 * self.angleScale)
            el = int(nc.var('Elevation')[radialI] / 360 * self.angleScale)
            sr = nc.var('StartRange')[radialI]
            dt = nc.var('Time')[radialI]
            try: ns = nc.var('TimenSec')[radialI]
            except: ns = 0 # fractional (nano) seconds not available
            data = [nc.var(type.fieldDescription)[radialI] for type in available] # get actual data into list of arrays
            dataH = ChillDataHeader(requestedData = self.dataTypes, availableData = listToBitmask(bitmaskToList(-1, scales)), startAz = az, startEl = el, endAz = az, endEl = el, numGates = numgates, startRange = sr, dataTime = dt, fractionalSecs = ns, rayNumber = radialI + 1)
            dataH.send(self.data[0])
            values = array.array('B')
            for gateI in range(0, numgates):
                #print "gate #", gateI
                for typeI in range(0, len(available)):
                    if use64bit: value = data[typeI][gateI][0]
                    else: value = data[typeI][gateI]
                    #print "value =", value
                    if fpconst.isFinite(value):
                        value = int((value * available[typeI].factor - available[typeI].bias) / available[typeI].scale)
                        if value > 255: value = 255
                        elif value < 1: value = 1 # 0 is missing
                    else:
                        value = 0 
                    #print "packed =", value
                    values.append(value)
            string = values.tostring()
            #print string
            self.data[0].send(string)
    def run (self):
        running = 1
        def disconnect ():
            self.control.close()
            for data in self.data: data.close()
            print "client disconnected"
            return 0
        while running:
            input = self.data + [self.control] # attempt to read data channel(s?) first
            inputready, outputready, exceptready = select.select(input, [], [])
            for s in inputready:
                if s in self.data: # data channel
                    try:
                        desiredTypes = struct.unpack("!Q", s.recv(8)) # store this with the corresponding data channel
                        self.dataTypes = desiredTypes[0]
                        print "requested data types =", self.dataTypes
                    except: # hit when client quits/dies without proper disconnect
                        #print sys.exc_info()
                        print "dead data channel"
                        running = disconnect()
                elif s == self.control: # control channel
                    try:
                        request = ArchCtlPacket(self.control)
                        if request.archMode == ArchCtlPacket.Command.sweepMode:
                            print "request for data from file '" + request.inFile + "'"
                            self.sendSweep(request.inFile)
                        elif request.archMode == ArchCtlPacket.Command.statusReq:
                            print "request for status of file '" + request.inFile + "'"
                            self.sendStatus()
                        elif request.archMode == ArchCtlPacket.Command.directoryReq:
                            if request.startSweep == ArchCtlPacket.DirType.contents:
                                print "request for contents of '" + request.inFile + "'"
                                self.sendDirectory(request.inFile)
                            elif request.startSweep == ArchCtlPacket.DirType.directories: #deprecated
                                print "request for list of directories"
                                self.sendDirList()
                            elif request.startSweep == ArchCtlPacket.DirType.files: #deprecated
                                print "request for list of files in '" + request.inFile + "'"
                                self.sendFileList(request.inFile)
                            elif request.startSweep == ArchCtlPacket.DirType.bookmarks: #deprecated
                                self.sendBookmarks()
                            else:
                                print "unknown dir type " + request.startSweep
                                response = ResponsePacket(statusCode = ResponsePacket.Status.requestError)
                                response.send(self.control)
                        elif request.archMode == ArchCtlPacket.Command.connectReq:
                            p = request.inFile.find(':')
                            signon = request.inFile[:p] # cut off everything past 1st :
                            password = request.inFile[p + 1:]
                            print "connect request from '" + signon + "'"
                            #print "  password = '" + password + "'"
                            response = ResponsePacket(statusCode = ResponsePacket.Status.serverReady, extStatus = self.sessionID)
                            response.send(self.control)
                            # now send initial scaling info (now optional)
                            #while len(self.data) < 1: pass # wait for a data connection
                            #for d in self.data:
                                #print "sending scaling info"
                                #for scale in scales: scale.send(d)
                        elif request.archMode == ArchCtlPacket.Command.disconnectReq:
                            print "disconnect request"
                            running = disconnect()
                        else:
                            print "unknown request " + request.archMode
                            response = ResponsePacket(statusCode = ResponsePacket.Status.requestError)
                            response.send(self.control)
                    except: # hit when client quits/dies without proper disconnect?
                        #print sys.exc_info()
                        print "dead control channel"
                        running = disconnect()
                else: # not control or data - should not be reachable
                    print "unknown channel"
                    running = disconnect()

class ArchCtlPacket:
    class Command:
        sweepMode = 2
        statusReq = 5
        directoryReq = 8
        connectReq = 9
        disconnectReq = 10
    class DirType:
        directories = 1
        files = 2
        bookmarks = 3
        contents = 4
    def __init__ (self, client):
        bytes = client.recv(116)
        #print bytes
        self.archMode, self.startSweep, self.rayStep, self.sweepLow, self.sweepHigh, self.extraDelay, self.inFile = struct.unpack("!i4hi100s", bytes)
        self.inFile = self.inFile.decode("UTF-8")
        p = self.inFile.find('\0')
        if p >= 0: self.inFile = self.inFile[:p] # cut off everything past 1st 0

class ResponsePacket:
    class Status:
        positionError = 2
        endFile = 6
        directorySent = 7
        statusUpdate = 11
        directoryFollows = 14
        serverReady = 16
        requestError = 18
    def __init__ (self, statusCode = 0, extStatus = 0, vol = -1, sweep = -1, ray = -1, scanMode = -1, maxSweeps = 0):
        self.statusCode = statusCode
        self.extStatus = extStatus
        self.vol = vol
        self.sweep = sweep
        self.ray = ray
        self.scanMode = scanMode
        self.maxSweeps = maxSweeps
    def send (self, client):
        string = struct.pack("!7i", self.statusCode, self.extStatus, self.vol, self.sweep, self.ray, self.scanMode, self.maxSweeps)
        client.send(string)

class ChillMomentFieldScale:
    def __init__ (self, fieldName = "", fieldDescription = "", keyboardAccelerator = -1, units = "", fieldNumber = -1, factor = 1, scale = 1, bias = 0, maxValue = 0, minValue = 0, dataWordCoding = 0, colorMapType = 0):
        self.recordType = 0x9292 # FIELD_SCALE_DATA
        self.headerLength = 232 # must match total of pack()
        self.fieldName = fieldName
        self.fieldDescription = fieldDescription
        self.keyboardAccelerator = keyboardAccelerator
        self.units = units
        self.fieldNumber = fieldNumber
        self.factor = factor
        self.scale = scale
        self.bias = bias
        self.maxValue = maxValue
        self.minValue = minValue
        self.dataWordCoding = dataWordCoding
        self.colorMapType = colorMapType
    def send (self, client):
        string = struct.pack("!2i32s128si32s6i2h", self.recordType, self.headerLength, self.fieldName, self.fieldDescription, self.keyboardAccelerator, self.units, self.fieldNumber, self.factor, self.scale, self.bias, self.maxValue, self.minValue, self.dataWordCoding, self.colorMapType)
        client.send(string)

def bitmaskToList (mask, types):
    list = []
    for type in types:
        if (mask & (1 << type.fieldNumber)) != 0:
            list.append(type)
    return list

def listToBitmask (list):
    mask = 0
    for type in list:
        mask = mask | (1 << type.fieldNumber)
    return mask

class ChillHousekeepingHeader:
    def __init__ (self, radarId = "", radarLatitude = 0, radarLongitude = 0, radarAltitude = 0, antMode = 0, nyquistVel = 0, gateWidth = 0, pulses = 0, polarizationMode = 0, tiltNum = 0, saveTilt = 0, angleScale = 0, sweepStartTime = 0):
        self.recordType = 0x9191 # BRIEF_HSK_DATA
        self.headerLength = 88 # must match total of pack()
        self.radarId = radarId
        self.radarLatitude = radarLatitude
        self.radarLongitude = radarLongitude
        self.radarAltitude = radarAltitude
        self.antMode = antMode
        self.nyquistVel = nyquistVel
        self.gateWidth = gateWidth
        self.pulses = pulses
        self.polarizationMode = polarizationMode
        self.tiltNum = tiltNum
        self.saveTilt = saveTilt
        self.angleScale = angleScale
        self.sweepStartTime = sweepStartTime
    def send (self, client):
        string = struct.pack("!2i32s11iI", self.recordType, self.headerLength, self.radarId, self.radarLatitude, self.radarLongitude, self.radarAltitude, self.antMode, self.nyquistVel, self.gateWidth, self.pulses, self.polarizationMode, self.tiltNum, self.saveTilt, self.angleScale, self.sweepStartTime)
        client.send(string)

class ChillDataHeader:
    def __init__ (self, requestedData = 0, availableData = 0, startAz = 0, startEl = 0, endAz = 0, endEl = 0, numGates = 0, startRange = 0, dataTime = 0, fractionalSecs = 0, rayNumber = 0):
        self.recordType = 0x9090 # GEN_MOM_DATA
        self.headerLength = 60 # must match total of pack()
        self.requestedData = requestedData
        #print "requested = ", requestedData
        self.availableData = availableData
        #print "available = ", availableData
        self.startAz = startAz
        self.startEl = startEl
        self.endAz = endAz
        self.endEl = endEl
        self.numGates = numGates
        self.startRange = startRange
        self.dataTime = dataTime
        self.fractionalSecs = fractionalSecs
        self.rayNumber = rayNumber
    def send (self, client):
        string = struct.pack("!2i2Q6iI2i", self.recordType, self.headerLength, self.requestedData, self.availableData, self.startAz, self.startEl, self.endAz, self.endEl, self.numGates, self.startRange, self.dataTime, self.fractionalSecs, self.rayNumber)
        client.send(string)

class Server:
    def __init__ (self):
        self.host = ''
        self.port = 2510
        self.backlog = 5
        self.server = None
        self.threads = []
        self.sessionID = 0
        self.sessions = {}

    def open_socket (self):
        try:
            self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR, 1)
            self.server.bind((self.host,self.port))
            self.server.listen(5)
        except socket.error, (value,message):
            if self.server:
                self.server.close()
            print "Could not open socket: " + message
            sys.exit(1)

    def run (self):
        self.open_socket()
        input = [self.server]#,sys.stdin]
        running = 1
        while running:
            inputready, outputready, exceptready = select.select(input, [], [])

            for s in inputready:

                if s == self.server: # handle the server socket
                    client, address = self.server.accept()
                    hello, sessionID, channeltype = struct.unpack("!I2h", client.recv(8))

                    if not hello == Constants.hello:
                        print "bad hello: " + str(hello)
                        continue

                    if channeltype == Constants.archCtl: # control channel
                        print "** Control connection **"
                        self.sessions[self.sessionID] = Session(client, self.sessionID)
                        self.sessions[self.sessionID].start()
                        self.sessionID += 1
                    elif channeltype == Constants.genMomDat: # data channel
                        print "** DATA connection **"
                        self.sessions[sessionID].data.append(client)
                    else:
                        print "bad channel = " + str(channeltype)
                        print "session = " + str(sessionID)

                elif s == sys.stdin: # handle standard input
                    junk = sys.stdin.readline()
                    running = 0 

        # close all threads

        self.server.close()
        for c in self.threads:
            c.join()

if __name__ == "__main__":
    if len(sys.argv) >= 2: rootdir = sys.argv[1]
    s = Server()
    s.run()
