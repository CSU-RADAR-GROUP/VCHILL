package edu.colostate.vchill.proxy;

import edu.colostate.vchill.*;
import edu.colostate.vchill.ChillDefines.Channel;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.chill.*;
import edu.colostate.vchill.socket.SocketArchCtl;
import edu.colostate.vchill.socket.SocketResponse;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Thread that deals with the control socket.
 *
 * @author Alexander Deyke
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-06-01
 */
class ProxyControlThread implements Runnable, Control {
    /**
     * initial type definition scale information is stored here
     */
    private static final ScaleManager sm = ScaleManager.getInstance();

    /**
     * name:port this object is connected to
     */
    private String url;

    /**
     * control connection to client
     */
    private Socket clientControlSocket;
    private DataInputStream clientControlIn;
    private DataOutputStream clientControlOut;

    /**
     * control connection to server
     */
    private Socket serverControlSocket;
    private DataInputStream serverControlIn;
    private DataOutputStream serverControlOut;

    /**
     * session ID; used to match data channels with control channel
     */
    private int sessionID;

    /**
     * reuseable object to represent the server's response to commands
     */
    private SocketResponse response;

    /**
     * reuseable object to represent the client's command
     */
    private SocketArchCtl command;

    /**
     * is the connection up?
     */
    private boolean connected;

    /**
     * timeout for headers on data channel
     */
    private final static int timeout = 5000; // in millis

    /**
     * list of ProxyDataThread associated with this ProxyControlThread
     */
    private ArrayList<ProxyDataThread> clientDataThreads;

    /**
     * number of data sockets to server necessary to transfer all data
     */
    private int serverDataSockets;

    /**
     * arrays of data sockets to server
     */
    private Socket[] serverDataSocket;
    private DataInputStream[] serverDataIn;
    private DataOutputStream[] serverDataOut;
    private long[] dataType;

    /**
     * time last packet was sent or received on this channel
     */
    private long lastPacketTime;

    /**
     * shared cache
     */
    private CacheMain cache;

    /**
     * parent Proxy object, get configuration info from here
     */
    private Proxy parent;

    /**
     * thread object; needed for isAlive
     */
    private Thread thread;

    public ProxyControlThread(final Socket clientControlSocket, final Proxy parent) throws IOException {
        this.parent = parent;
        this.serverDataSockets = (ChillFieldInfo.types.length - (this.parent.getCalcFlag() ? 4 : 0) - 1) / ChillDefines.MAX_PER_CHANNEL + 1;
        this.clientControlSocket = clientControlSocket;
        this.clientDataThreads = new ArrayList<ProxyDataThread>();
        this.serverDataSocket = new Socket[serverDataSockets];
        this.serverDataIn = new DataInputStream[serverDataSockets];
        this.serverDataOut = new DataOutputStream[serverDataSockets];
        this.dataType = new long[serverDataSockets];
        // Assign data types to the three sockets
        for (int i = 0; i < ChillFieldInfo.types.length; ++i) {
            // If we're calculating hybrid types, don't retrieve them
            ChillFieldInfo type = ChillFieldInfo.types[i];
            if (this.parent.getCalcFlag() && type.fieldNumber > ChillFieldInfo.CALC_CUTOFF) continue;
            this.dataType[i / ChillDefines.MAX_PER_CHANNEL] |= 1l << type.fieldNumber;
        }
        this.clientControlIn = new DataInputStream(new BufferedInputStream(clientControlSocket.getInputStream()));
        this.clientControlOut = new DataOutputStream(clientControlSocket.getOutputStream());
        this.cache = this.parent.getCache();
        this.thread = new Thread(this, "ProxyControlThread");
        this.sessionID = -1;
    }

    /**
     * Causes this thread to begin execution; the Java Virtual Machine calls the <code>run</code> method of this thread.
     */
    public void start() {
        this.thread.start();
    }

    /**
     * Tests if this thread is alive. A thread is alive if it has been started and has not yet died.
     *
     * @return <code>true</code> if this thread is alive; <code>false</code> otherwise.
     */
    public boolean isAlive() {
        return this.thread.isAlive();
    }

    /**
     * Connects to given server and port number
     *
     * @param server the name of the server to connect to
     * @param port   the port number to connect to
     * @throws IOException if the connection fails
     */
    public synchronized void connect(final String server, final int port) throws IOException {
        this.url = server + ":" + port;
        System.out.println("ProxyControlThread: Connecting to " + this.url);
        this.serverControlSocket = new Socket(server, port);
        this.serverControlSocket.setSoTimeout(25000); // timeout on control while connecting (ms)
        this.serverControlIn = new DataInputStream(new BufferedInputStream(this.serverControlSocket.getInputStream()));
        this.serverControlOut = new DataOutputStream(new BufferedOutputStream(this.serverControlSocket.getOutputStream()));
        boolean accepted = false;
        do {
            System.out.println("ProxyControlThread: Getting and sending login and password.");
            if (!connected) {
                serverControlOut.writeInt(ChillDefines.HELLO);
                serverControlOut.writeInt(ChillDefines.Channel.ARCH_CTL.ordinal());
                serverControlOut.flush();
            }
            this.readCommand(); // Getting login and password from client
            this.passCommand();
            System.out.println("ProxyControlThread: Getting response to login and password.");
            this.readAndPassResponse(); // Getting response
            this.connected = true; // mark as connected so correct packet gets sent on retry
            switch (this.response.getStatus()) {
                case SERVER_READY:
                    System.out.println("ProxyControlThread: Server ready.");
                    accepted = true;
                    break;
                case SIGNON_UNKNOWN:
                    System.err.println("ProxyControlThread: Bad login.");
                    continue;
                case PASSWD_FAIL:
                    System.err.println("ProxyControlThread: Bad password.");
                    continue;
                default: // unknown response
                    throw new Error("ProxyControlThread: Don't know how to handle:\n" + this.response);
            }
        } while (!accepted);
        this.sessionID = this.response.getExtStatus();
        this.serverControlSocket.setSoTimeout(0); // no timeout on control
        for (int i = 0; i < serverDataSockets; ++i) {
            this.serverDataSocket[i] = new Socket(server, port);
            this.serverDataSocket[i].setSoTimeout(0); // no default timeout on data
            this.serverDataIn[i] = new DataInputStream(new BufferedInputStream(this.serverDataSocket[i].getInputStream()));
            this.serverDataOut[i] = new DataOutputStream(new BufferedOutputStream(this.serverDataSocket[i].getOutputStream()));
            this.sendDataHandshake(i);
        }
        System.out.println("ProxyControlThread: Listening to commands.");
        this.listenCommands();
    }

    /**
     * Check connection status
     *
     * @return true if the connection is active and ready, false if it is not
     */
    public synchronized boolean connected() {
        if (this.connected == false) return false;
        if (!(this.clientControlSocket.isConnected() && this.serverControlSocket.isConnected())) {
            try {
                System.out.println("ProxyControlThread: Socket: socket timed out, disconnecting");
                this.disconnect();
            } catch (IOException ioe) {
                throw new Error("Socket PANIC!! - IOException while trying to disconnect: " + ioe);
            }
        }
        return this.connected;
    }

    /**
     * Listen to commands from the client.
     */
    public synchronized void listenCommands() throws IOException {
        while (true) {
            while (this.connected() && clientControlIn.available() < SocketArchCtl.BYTE_SIZE) {
                if (this.parent.getTimeout() != 0 && System.currentTimeMillis() - lastPacketTime >= this.parent.getTimeout()) {
                    System.out.println("ProxyControlThread: Idle too long.  Disconnecting.");
                    disconnect();
                } else {
                    Proxy.sleep(100);
                }
            }
            if (!this.connected()) break;
            this.readCommand();
            switch (this.command.getArchMode()) {
                case DIRECTORY_REQ:
                    getDirectory();
                    break; //dirs, files, bookmarks, contents
                case STATUS_REQ:
                    getStatus();
                    break;
                case DISCONNECT_REQ:
                    disconnect();
                    break;
                case HALT_COMMAND:
                    halt();
                    break;
                case SWEEP_MODE:
                    getSweep();
                    break;
                default:
                    System.err.println("ProxyControlThread: Invalid command packet:\n" + this.command);
            }
        }
    }

    /**
     * Retrieves a sweep from the server.  All data types are requested
     * or calculated and put into the cache.  Then the requested ones can
     * be passed on by the ProxyDataThread.
     *
     * @throws IOException if an error occurs when communicating with the server
     */
    @SuppressWarnings("unchecked")
    public synchronized void getSweep() throws IOException {
        System.out.println("ProxyControlThread: Got Sweep Mode request");
        ChillHSKHeader[] hskHs = new ChillHSKHeader[serverDataSockets];
        ChillDataHeader[] dataHs = new ChillDataHeader[serverDataSockets];
        ArrayList<String>[] types = new ArrayList[serverDataSockets];
        String filename = command.getInFile();
        String directory = filename.substring(0, filename.lastIndexOf("/"));
        String file = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
        // Set rayCommand to metadata since that's where we're storing headers
        //ControlMessage rayCommand = new ControlMessage(url, directory, file, "Sweep " + command.getStartSweep(), ChillDefines.META_TYPE);
        ControlMessage rayCommand = new ControlMessage(url, directory, file, "Sweep " + command.getStartSweep());
        //ControlMessage controlCommand = rayCommand.setType(ChillDefines.CONTROL_TYPE); //used for storing responses
        //ControlMessage metaCommand = rayCommand; //rayCommand gets reused in for loops
        boolean sentResponses = false;
        if (cache.getCompleteFlag(rayCommand, ChillDefines.CONTROL_TYPE)) {
            System.out.println("meta info complete");
            this.response = (SocketResponse) cache.getData(rayCommand, ChillDefines.CONTROL_TYPE, 0);
            System.out.println("ProxyControlThread: Sending start header.");
            this.passResponse();
            this.response = (SocketResponse) cache.getData(rayCommand, ChillDefines.CONTROL_TYPE, 1);
            System.out.println("ProxyControlThread: Sending end header.");
            this.passResponse();
            sentResponses = true;
        }
        System.out.println("starting data threads");
        for (ProxyDataThread thread : clientDataThreads) thread.send(rayCommand);
        if (!sentResponses) { // Responses were only sent if everything was cached.
            System.out.println("responses not yet sent");
            ArrayList<String> allTypes = new ArrayList<String>();
            for (ChillFieldInfo type : ChillFieldInfo.types) {
                // If we're calculating hybrid data types, don't retrieve them
                if (this.parent.getCalcFlag() && type.fieldNumber > ChillFieldInfo.CALC_CUTOFF) continue;
                allTypes.add(type.fieldName);
            }
            ArrayList<String>[] availableTypes = new ArrayList[serverDataSockets]; // subset of this socket's subset of allTypes representing available data

            System.out.println("ProxyControlThread: Not everything is cached.");
            this.clearChannels();
            this.passCommand();
            this.readAndPassResponse();

            // store initial response
            System.out.println("adding initial response");
            cache.addRay(rayCommand, ChillDefines.CONTROL_TYPE, this.response);

            // do HDR, NCP+, etc. calculations
            if (this.parent.getCalcFlag()) new ProxyCalculationThread(rayCommand, cache).start();

            // read radar data into cache
            int firstRayNumber = this.response.getRayNumber() - 1; // convert to 0-based
            System.out.println("1st ray# = " + firstRayNumber);
            int lastRayNumber = Integer.MAX_VALUE; // don't know when to stop yet
            for (int currRayNumber = firstRayNumber; currRayNumber < lastRayNumber; ) { // each ray:
                //System.out.println("loop executing");
                if (this.clientControlIn.available() > 0) { // stop request received
                    System.out.println("in clientIn");
                    //Make it so we can revert the stream if the command isn't what we are looking for.
                    this.clientControlIn.mark(SocketArchCtl.BYTE_SIZE);

                    this.readCommand();

                    if (this.command.getArchMode() == SocketArchCtl.Command.HALT_COMMAND) {
                        System.out.println("ProxyControlThread: Attempting to stop...");
                        this.halt();
                        for (int i = 0; i < allTypes.size(); ++i) {
                            cache.removeType(rayCommand, allTypes.get(i)); // remove incomplete sweep
                        }
                        cache.removeType(rayCommand, ChillDefines.META_TYPE);
                        return;
                    } else {
                        //Reset the stream so that the command received will be
                        //handled properly later on.
                        this.clientControlIn.reset();
                    }
                }

                if (this.serverControlIn.available() > 0) { // notification: sweep done
                    System.out.println("in serverIn");
                    //System.out.println(this.response);
                    this.readAndPassResponse();
                    //System.out.println(this.response);
                    lastRayNumber = this.response.getRayNumber(); // we know when to stop now
                    System.out.println("last ray# = " + lastRayNumber);
                }
                try {
                    for (int i = 0; i < serverDataSockets; ++i) {
                        this.serverDataSocket[i].setSoTimeout(ProxyControlThread.timeout); // default timeout on data
                        ChillHeaderHeader headerH = new ChillHeaderHeader(serverDataIn[i]);
//System.out.println("header type = " + Integer.toHexString(headerH.recordType));
                        switch (headerH.recordType) {
                            case ChillDefines.GEN_MOM_DATA:
                                dataHs[i] = new ChillDataHeader(serverDataIn[i], headerH); // read header
                                if (i == 0)
                                    cache.addRay(rayCommand, ChillDefines.META_TYPE, dataHs[i]); //only save one copy
                                if (availableTypes[i] == null) { // only initialize once
                                    types[i] = dataHs[i].calculateTypes(); // determine which types are available
                                    availableTypes[i] = new ArrayList<String>(types[i].size());
                                    for (String type : types[i]) { // create a ControlMessage for each available type
                                        availableTypes[i].add(type);
                                    }
                                }
                                this.serverDataSocket[i].setSoTimeout(0); // no timeout on data
                                byte[][] data = new byte[availableTypes[i].size()][dataHs[i].numGates];
                                byte[] interleavedData = this.readBytes(availableTypes[i].size() * dataHs[i].numGates, serverDataIn[i]); // calculate size of and read data
                                for (int t = 0; t < availableTypes[i].size(); ++t) { // split apart different data types
                                    if (availableTypes[i].get(t) == null) continue;
                                    for (int g = 0; g < dataHs[i].numGates; ++g) {
                                        data[t][g] = interleavedData[availableTypes[i].size() * g + t];
                                    }
                                    cache.addRay(rayCommand, availableTypes[i].get(t), new ChillGenRay(hskHs[i], dataHs[i], types[i].get(t), data[t])); //add data to cache
                                }
                                ++currRayNumber; //increment ray number here so it doesn't go up on "continue"
//System.out.println("currRayNumber = " + currRayNumber);
                                break;
                            case ChillDefines.BRIEF_HSK_DATA:
                                hskHs[i] = new ChillHSKHeader(serverDataIn[i], headerH);
                                if (i == 0)
                                    cache.addRay(rayCommand, ChillDefines.META_TYPE, hskHs[i]); //only save one copy
                                break;
                            case ChillDefines.FIELD_SCALE_DATA:
                                ChillMomentFieldScale scale = new ChillMomentFieldScale(serverDataIn[i], headerH);
                                if (i == 0)
                                    cache.addRay(rayCommand, ChillDefines.META_TYPE, scale); //only save one copy
                                sm.putScale(scale);
                                break;
                            case ChillDefines.TRACK_DATA:
                                ChillTrackInfo track = new ChillTrackInfo(serverDataIn[i], headerH);
                                if (i == 0)
                                    cache.addRay(rayCommand, ChillDefines.META_TYPE, track); //only save one copy
                                break;
                            default:
                                System.out.println("Don't know how to handle header of type " + headerH.recordType);
                                ChillHeader generic = new ChillHeader(headerH);
                                if (i == 0)
                                    cache.addRay(rayCommand, ChillDefines.META_TYPE, generic); //only save one copy
                                break;
                        }
                    }
                } catch (SocketTimeoutException stoe) {
                    System.err.println("ProxyControlThread: Read timed out getting sweep; retrying");
                    continue;
                }

                Proxy.sleep(5);
            }

            // Add in the calculated scales
            if (this.parent.getCalcFlag()) {
                cache.addRay(rayCommand, ChillDefines.META_TYPE, KdpUtil.scale);
                cache.addRay(rayCommand, ChillDefines.META_TYPE, NcpPlusUtil.scale);
                cache.addRay(rayCommand, ChillDefines.META_TYPE, HdrUtil.scale);
                cache.addRay(rayCommand, ChillDefines.META_TYPE, RainUtil.scale);
            }

            // mark each type as complete -
            // use requested rather than available commands so unavailable data is not requested again
            for (int t = 0; t < allTypes.size(); ++t) {
                String type = allTypes.get(t);
                System.out.println("ProxyControlThread: marking " + type + " complete; cached " + cache.getNumberOfRays(rayCommand, type) + " rays");
                cache.setCompleteFlag(rayCommand, type);
            }
            cache.addRay(rayCommand, ChillDefines.CONTROL_TYPE, this.response); // add final response
            cache.setCompleteFlag(rayCommand, ChillDefines.CONTROL_TYPE);
            System.out.println("ProxyControlThread: marking " + ChillDefines.META_TYPE + " complete; cached " + cache.getNumberOfRays(rayCommand, ChillDefines.META_TYPE) + " rays");
            cache.setCompleteFlag(rayCommand, ChillDefines.META_TYPE);
        }

        System.out.println("ProxyControlThread: (Now) everything is cached.");
    }

    /**
     * Passes the disconnect request to the server and closes the connection
     */
    private synchronized void disconnect() throws IOException {
        System.out.println("ProxyControlThread: Got Disconnect request");
        this.clearChannels();
        this.passCommand();
        this.killConnection(null);
    }

    /**
     * Closes the connection to the server
     */
    public synchronized void killConnection(final ProxyDataThread pdt) throws IOException {
        System.out.println("ProxyControlThread: Disconnecting.");
        this.clientControlIn.close();
        this.clientControlIn = null;
        this.clientControlOut.close();
        this.clientControlOut = null;
        this.clientControlSocket.close();
        this.clientControlSocket = null;
        this.serverControlIn.close();
        this.serverControlIn = null;
        this.serverControlOut.close();
        this.serverControlOut = null;
        this.serverControlSocket.close();
        this.serverControlSocket = null;
        // Condemn all associated ProxyDataThreads so they'll disconnect, too.
        for (ProxyDataThread thread : clientDataThreads) thread.condemn();
        this.connected = false;
        this.sessionID = -1;
    }

    /**
     * Stop server from sending further data and clear all input streams of remaining unprocessed data.
     *
     * @throws IOException if an error occurs while communicating with the server
     */
    private synchronized void halt() throws IOException {
        System.out.println("ProxyControlThread: Got Halt request");
        this.passCommand();
        this.readAndPassResponse();
        this.clearChannels();
    }

    /**
     * Retrieves the status of a given file from the server.
     * this.response is overwritten with the result
     *
     * @throws IOException if an error occurs when communicating with the server
     */
    public synchronized void getStatus() throws IOException {
        System.out.println("ProxyControlThread: Got Status request");
        this.clearChannels();
        this.passCommand();
        this.readAndPassResponse();
    }

    /**
     * Retrieves the list of available directories/files or bookmarks from the server
     *
     * @throws IOException if an error occurs when communicating with the server
     */
    public synchronized void getDirectory() throws IOException {
        System.out.println("ProxyControlThread: Got Directory request");
        this.clearChannels();
        this.passCommand();
        this.readAndPassResponse(); // dir follows
        int dirlen = this.response.getExtStatus();
        byte[] dirlist = this.readBytes(dirlen, this.serverControlIn);
        this.sendBytes(dirlist, dirlen, this.clientControlOut);
        this.readAndPassResponse(); // dir sent
    }

    /**
     * Clears all channels of unprocessed incoming data
     *
     * @throws IOException if an error occurs
     */
    private synchronized void clearChannels() throws IOException {
        int toSkip;
        int skipped;
        while (this.serverControlIn.available() > 0) {
            toSkip = this.serverControlIn.available();
            System.out.println("ProxyControlThread: skipping " + toSkip + " control bytes");
            skipped = 0;
            while (skipped < toSkip) skipped += this.serverControlIn.skip(toSkip);
            for (int i = 0; i < serverDataSockets; ++i) {
                toSkip = this.serverDataIn[i].available();
                System.out.println("ProxyControlThread: skipping " + toSkip + " data bytes");
                skipped = 0;
                while (skipped < toSkip) skipped += this.serverDataIn[i].skip(toSkip);
            }

            Proxy.sleep(100); // wait to see if data is still coming in
        }
    }

    /**
     * Reads a SocketArchCtl packet from the client control channel into this.command
     */
    protected synchronized void readCommand() throws IOException {
        this.command = new SocketArchCtl(readBytes(SocketArchCtl.BYTE_SIZE, clientControlIn));
    }

    /**
     * Passes the command packet to the server
     */
    protected synchronized void passCommand() throws IOException {
        this.serverControlOut.write(this.command.getBytes(), 0, SocketArchCtl.BYTE_SIZE);
        this.serverControlOut.flush();
    }

    /**
     * Reads a SocketResponse packet from the server control channel into this.response
     * and passes the response packet to the client
     */
    protected synchronized void readAndPassResponse() throws IOException {
        while (true) {
            System.out.println("reading response");
            this.response = new SocketResponse(readBytes(SocketResponse.BYTE_SIZE, serverControlIn));
//System.out.println(this.response.toString());
            System.out.println("passing response");
            this.passResponse();
            switch (response.getStatus()) {
                case POSITION_ERROR:
                case OPEN_ERROR:
                case FORMAT_ERROR: // handle server error codes
                    System.err.println(this.response.toString());
                    throw new IOException(this.response.getStatus().toString());
                case MESSAGE_FOLLOWS: //check for message
                    this.sendBytes(this.readBytes(this.response.getExtStatus(), serverControlIn), this.response.getExtStatus(), clientControlOut);
                    break;
                default: //unknown/other response
                    return;
            }
        }
    }

    /**
     * Passes the response this.response packet to the client
     */
    protected synchronized void passResponse() throws IOException {
        this.lastPacketTime = System.currentTimeMillis();
        this.clientControlOut.write(this.response.getBytes(), 0, SocketResponse.BYTE_SIZE);
        this.clientControlOut.flush();
    }

    /**
     * Read a specified number of bytes from the given InputStream
     *
     * @param numBytes the number of bytes to read from in (will block until numBytes bytes read)
     * @param in       the stream to read from
     * @return byte[numBytes] containing the read bytes
     */
    private synchronized byte[] readBytes(final int numBytes, final DataInputStream in) throws IOException {
        this.lastPacketTime = System.currentTimeMillis();
        byte[] result = new byte[numBytes];
        in.readFully(result);
        return result;
    }

    /**
     * Write a byte array of given length to the given OutputStream
     *
     * @param array    byte[numBytes] containing the read bytes
     * @param numBytes the number of bytes to read from in (will block until numBytes bytes read)
     * @param out      the stream to write to
     */
    private synchronized void sendBytes(final byte[] array, final int numBytes, final DataOutputStream out) throws IOException {
        this.lastPacketTime = System.currentTimeMillis();
        out.write(array, 0, numBytes);
        out.flush();
    }

    /**
     * Sets up the data connection by sending the source and a default data type.
     *
     * @param socketNum index of socket on which to send the handshake
     */
    private void sendDataHandshake(final int socketNum) throws IOException {
        System.out.println("sending data handshake");
        this.serverDataOut[socketNum].writeInt(ChillDefines.HELLO);
        this.serverDataOut[socketNum].writeInt((this.sessionID << 16) | Channel.GEN_MOM_DAT.ordinal());
        this.serverDataOut[socketNum].flush();

        do { //get initial scaling info; needed to open first window(s)
            ChillHeaderHeader headerH = new ChillHeaderHeader(this.serverDataIn[socketNum]);
            assert headerH.recordType == ChillDefines.FIELD_SCALE_DATA;
            ChillMomentFieldScale scale = new ChillMomentFieldScale(this.serverDataIn[socketNum], headerH);
            System.out.println("got scaling block " + scale.fieldName);
            sm.putScale(scale);
            if (this.serverDataIn[socketNum].available() > 0) continue; //skip delay
            try {
                Thread.sleep(250);
            } //wait to see if data is still coming in
            catch (InterruptedException ie) {
            }
        } while (this.serverDataIn[socketNum].available() > 0);

        this.serverDataOut[socketNum].writeLong(this.dataType[socketNum]);
        this.serverDataOut[socketNum].flush();
    }


    /**
     * Adds a ProxyDataThread to this thread's list.
     *
     * @param thread thread to add
     */
    public void addDataThread(final ProxyDataThread thread) {
        clientDataThreads.add(thread);
    }

    /**
     * Connects to the archive server specified in Proxy.
     */
    public void run() {
        try {
            connect(this.parent.getServerName(), this.parent.getServerPort());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Returns the session ID associated with this thread's sockets.
     */
    public int getSessionID() {
        return this.sessionID;
    }

    public boolean getCalcFlag() {
        return this.parent.getCalcFlag();
    }
}
