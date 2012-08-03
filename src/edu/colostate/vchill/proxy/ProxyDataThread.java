package edu.colostate.vchill.proxy;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.ViewUtil;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.chill.ChillDataHeader;
import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.chill.ChillGenRay;
import edu.colostate.vchill.chill.ChillHeader;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Sends data to client.
 *
 * @author Alexander Deyke
 * @author Jochen Deyke
 * @version 2007-09-28
 */
class ProxyDataThread implements Runnable
{
    /** initial type definition scale information is stored here */
    private static final ScaleManager sm = ScaleManager.getInstance();

    /** data connection to client */
    private Socket clientDataSocket;
    private DataInputStream clientDataIn;
    private DataOutputStream clientDataOut;

    /** is the connection up? */
    private boolean connected = false;

    /** status messages */
    public enum Status {
        READY,
        SENDING,
        CONDEMNED,
    }

    /** this thread's status */
    private Status status = Status.READY;

    /** command to process */
    private ControlMessage command;

    /** shared cache */
    private CacheMain cache;

    /** the last data type(s) requested (don't need to notify server if same type is wanted) */
    private long dataType;

    private final Control parent;

    private final Thread thread;

    private boolean realtime;

    public ProxyDataThread (final Socket clientDataSocket, final Control parent, final CacheMain cache) throws IOException
    {
        this(clientDataSocket, parent, cache, false);
    }

    /**
     * @param clientDataSocket socket to get type requests from and send data to
     * @param parent Control to get configuration info from
     * @param cache data storage
     * @param realtime whether to start at the most current ray or at the beginning
     */
    public ProxyDataThread (final Socket clientDataSocket, final Control parent, final CacheMain cache, final boolean realtime) throws IOException
    {
System.out.println("in constructor");
        this.realtime = realtime;
        this.clientDataSocket = clientDataSocket;
        this.clientDataIn = new DataInputStream(new BufferedInputStream(clientDataSocket.getInputStream()));
        this.clientDataOut = new DataOutputStream(new BufferedOutputStream(clientDataSocket.getOutputStream()));
        this.cache = cache;
        this.parent = parent;
System.out.println("sending scaling info");
        do { Proxy.sleep(250); } while (sm.getTypes().size() < 1); //wait for scaling info
        for (String scale : sm.getTypes()) {
System.out.println(scale);
            sendHeader(sm.getScale(scale));
        }
        this.dataType = clientDataIn.readLong();
        this.thread = new Thread(this, "ProxyDataThread");
    }

    /**
     * Causes this thread to begin execution; the Java Virtual Machine calls the <code>run</code> method of this thread.
     */
    public void start () { this.thread.start(); }


    /**
     * Check connection status
     *
     * @return true if the connection is active and ready, false if it is not
     */
    public synchronized boolean connected ()
    {
        if (this.connected == false) return false;
        if (!(this.clientDataSocket.isConnected())) {
            try {
                System.out.println("ProxyDataThread: Socket timed out, disconnecting");
                this.disconnect();
            } catch (IOException ioe) {
                throw new Error("Socket PANIC!! - IOException while trying to disconnect: ", ioe);
            }
        }
        return this.connected;
    }

    /**
     * Idle until receiving a condemnation or a command.
     */
    public /*NOT synchronized*/ void listen () throws IOException
    {
        while (this.connected()) {
            switch (this.status) {
                case READY:     Proxy.sleep(100); break;
                case CONDEMNED: disconnect();     break;
                case SENDING:   doSend();         break;
            }
        }
    }

    /**
     * Closes the connection to the server.
     */
    public synchronized void disconnect () throws IOException
    {
        System.out.println("ProxyDataThread: Disconnecting.");
        this.clientDataIn.close();     this.clientDataIn     = null;
        this.clientDataOut.close();    this.clientDataOut    = null;
        this.clientDataSocket.close(); this.clientDataSocket = null;
        this.connected = false;
    }

    /**
     * Send a ChillMomentHeader to the client.
     *
     * @param header header to send
     */
    private void sendHeader (final ChillHeader header) throws IOException
    {
//System.out.println("Sending ChillHeader " + Integer.toHexString(header.header.recordType));
        header.write(this.clientDataOut);
        this.clientDataOut.flush();
    }

    /**
     * Send a byte array of data to the client.
     *
     * @param data data to send
     */
    private void sendData (final byte[] data) throws IOException
    {
        this.clientDataOut.write(data);
        this.clientDataOut.flush();
    }

    /**
     * Listen, then exit.
     */
    public /*NOT synchronized*/ void run ()
    {
        try {
            this.connected = true;
            System.out.println("ProxyDataThread: Listening on data socket.");
            listen();
            System.out.println("ProxyDataThread: Data socket thread closing.");
        } catch (IOException ioe) {
            System.err.println("ProxyDataThread: Caught exception: " + ioe);
            System.err.println("...disconnecting");
            if (this.parent != null) try { this.parent.killConnection(this); }
            catch (IOException ioe2) { throw new Error(ioe2); }
            ioe.printStackTrace();
        }
    }

    /**
     * Check if there are any data type change requests.  Then return data type.
     *
     * @return bits of requested data types
     */
    public synchronized long getDataType () throws IOException
    {
        while (this.clientDataIn.available() > 0) {
            this.dataType = this.clientDataIn.readLong();
        }
        /*
        for (ChillFieldInfo type : ChillFieldInfo.types) {
            if ((this.dataType & 1l << type.fieldNumber) != 0) {
                System.out.println("ProxyDataThread: Got request for " + type.fieldName);
            }
        }
        */
        return this.dataType;
    }

    /**
     * Sets this thread to sending and specifies command.
     *
     * @param command command to process
     */
    public synchronized void send (final ControlMessage command)
    {
System.out.println("start of send");
        this.command = command;
        this.status = Status.SENDING;
System.out.println("end of send");
    }

    /**
     * Send data to client.
     */
    private void doSend () throws IOException
    {
        System.out.println("ProxyDataThread: Initiating sending.");
        long allAvailableTypes = 0;
        int currRayNumber = realtime ? cache.getNumberOfRays(this.command, ChillFieldInfo.NCP.fieldName) : 0;
        //ControlMessage metaCommand = this.command.setType(ChillDefines.META_TYPE);

        int metaIndex = 0;
        while (true) {
            ChillHeader header = (ChillHeader)cache.getDataWait(this.command, ChillDefines.META_TYPE, metaIndex); // Get header
            if (header == null) {
                this.status = Status.READY;
                return; //done
            } else if (header.header.recordType == ChillDefines.FIELD_SCALE_DATA) {
                sm.putScale((ChillMomentFieldScale)header);
                sendHeader(header);
            } else if (header.header.recordType == ChillDefines.GEN_MOM_DATA) {
                ChillDataHeader oldHeader = (ChillDataHeader)header;
                allAvailableTypes = oldHeader.availableData;

                // If we're calculating hybrid data types, mark them available 
                if (parent.getCalcFlag()) { 
                    // If Z, PhiDP, and RhoHV are available, so is KDP.
                    if (((allAvailableTypes & (1l << ChillFieldInfo.Z.fieldNumber)) != 0) &&
                        ((allAvailableTypes & (1l << ChillFieldInfo.RHOHV.fieldNumber)) != 0) &&
                        ((allAvailableTypes & (1l << ChillFieldInfo.PHIDP.fieldNumber)) != 0)) {
                        allAvailableTypes |= 1l << ChillFieldInfo.KDP.fieldNumber;
                        // If KDP available, so is Rcomp.
                        allAvailableTypes |= 1l << ChillFieldInfo.RCOMP.fieldNumber;
                    }
                    // If Z and ZDR are available, so is HDR.
                    if (((allAvailableTypes & (1l << ChillFieldInfo.Z.fieldNumber)) != 0) &&
                        ((allAvailableTypes & (1l << ChillFieldInfo.ZDR.fieldNumber)) != 0)) {
                        allAvailableTypes |= 1l << ChillFieldInfo.HDR.fieldNumber;
                    }
                    // If NCP and ZDR are available, so is NCP+.
                    if (((allAvailableTypes & (1l << ChillFieldInfo.NCP.fieldNumber)) != 0) &&
                        ((allAvailableTypes & (1l << ChillFieldInfo.ZDR.fieldNumber)) != 0)) {
                        allAvailableTypes |= 1l << ChillFieldInfo.NCP_PLUS.fieldNumber;
                    }
                }

                ArrayList<String> availableRequestedTypes = null;
                long availableRequestedTypesMask = 0;
                if (this.clientDataIn.available() > 0 || availableRequestedTypes == null) {
                    availableRequestedTypesMask = getDataType() & allAvailableTypes;
                    availableRequestedTypes = new ArrayList<String>();
                    for (ChillFieldInfo type : ChillFieldInfo.types) {
                        if ((availableRequestedTypesMask & 1l << type.fieldNumber) != 0) {
                            availableRequestedTypes.add(type.fieldName);
                        }
                    }
                }

                // Create new header with correct requestedData field.
                ChillDataHeader newHeader = new ChillDataHeader(oldHeader);
                newHeader.requestedData = availableRequestedTypesMask;
                newHeader.availableData = allAvailableTypes;
                sendHeader(newHeader);
                byte[][] data = new byte[availableRequestedTypes.size()][];
                byte[] toSocket = new byte[availableRequestedTypes.size() * newHeader.numGates];
                for (int t = 0; t < availableRequestedTypes.size(); ++t) {
                    ChillGenRay ray = (ChillGenRay)cache.getDataWait(this.command, availableRequestedTypes.get(t), currRayNumber);
                    data[t] = ViewUtil.getBytes(ray.getData(), ray.getType());
                    for (int g = 0; g < newHeader.numGates; ++g) {
                        toSocket[availableRequestedTypes.size() * g + t] = data[t][g];
                    }
                }
                sendData(toSocket);
//System.out.println("sent ray #" + currRayNumber);
                ++currRayNumber;
                Thread.yield();
            } else { //other header
                sendHeader(header);
            }
        ++metaIndex;
        }
    }

    public synchronized void condemn () { this.status = Status.CONDEMNED; }

    public synchronized boolean isReady () { return this.status == Status.READY; }
}
