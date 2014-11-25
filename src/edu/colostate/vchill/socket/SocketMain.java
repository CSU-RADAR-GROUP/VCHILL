package edu.colostate.vchill.socket;

import edu.colostate.vchill.*;
import edu.colostate.vchill.ChillDefines.Channel;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.chill.*;
import edu.colostate.vchill.gui.FileTreeNode;
import edu.colostate.vchill.gui.ViewFileBrowser;
import edu.colostate.vchill.socket.SocketArchCtl.Command;
import edu.colostate.vchill.socket.SocketArchCtl.DirType;
import edu.colostate.vchill.socket.SocketResponse.Status;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Main class of VCHILL's Socket Module (it's for controlling and interacting
 * with an archive server). Available functions include connecting, disconnecting,
 * getting lists of available files and bookmarks.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public final class SocketMain {
    private static final NumberFormat nf = new DecimalFormat("00");
    private static final Config config = Config.getInstance();
    private static final ScaleManager sm = ScaleManager.getInstance();

    private String url;

    /**
     * control connection
     */
    private Socket controlSocket;
    private DataInputStream controlIn;
    private DataOutputStream controlOut;

    /**
     * data connection
     */
    private Socket dataSocket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    /**
     * session id from the server
     */
    private int sessionID;

    /**
     * is the connection up?
     */
    private boolean connected;

    /**
     * the connection this socket is associated with.
     */
    private SocketConnection connection;

    /**
     * indicates whether or not to ask for login info.
     */
    private boolean promptLogin;

    /**
     * the last data type(s) requested. Don't need to notify server if same type is wanted
     */
    private long lastRequestedDataType = -1; //ensure first request will be different, even if 0

    /**
     * timeout for headers on data channel
     */
    private final static int TIMEOUT = 500; //in millis

    /**
     * Sole constructor
     *
     * @param promptLogin Indicates whether or not to prompt for login info.
     */
    public SocketMain(final SocketConnection connection, final boolean promptLogin) {
        this.sessionID = -1;
        this.connected = false;
        this.connection = connection;
        this.promptLogin = promptLogin;
    }

    /**
     * Connects to given server and port number
     *
     * @param server the name of the server to connect to
     * @param port   the port number to connect to
     * @throws IOException if the connection fails
     */
    public synchronized void connect(final String server, final int port) throws IOException {
        if (this.connected) this.disconnect(); //can't connect if we're already connected...
        this.url = server + ":" + port;
        boolean loggedIn = true;
        if (this.promptLogin)
            loggedIn = this.getLogin(server); //do this first to ensure main win doesn't block login prompts

        if (loggedIn) {
            doConnect(server, port);
            /*
            new Thread(new Runnable() {public void run () {
                try { doConnect(server, port); }
                catch (IOException ioe) { DialogUtil.showErrorDialog(ioe.toString()); }
            }}, "ConnectionThread").start();
            */
        }
    }

    /**
     * Sets the global login strings (in SocketUtil) if they have not been set.
     *
     * @param server The name of the server to be used in prompts
     * @return true if login OK, false if cancelled
     */
    private boolean getLogin(final String server) throws IOException {
        String signon = config.getSignon();
        String password = config.getPassword();
        String[] login = DialogUtil.showLoginDialog("Connecting to " + this.url + " - Login",
                "Please enter your signon:", signon,
                "Please enter your password:", password);
        if (login == null) {
            System.out.println("Login aborted");
            return false;
        }
        config.setSignon(login[0]);
        config.setPassword(login[1]);
        return true;
    }

    private synchronized void doConnect(final String server, final int port) throws IOException {
        System.out.println("socket: connecting to " + this.url);
        this.controlSocket = new Socket();
        this.controlSocket.setSoTimeout(5000); //timeout on control while connecting (ms)
        this.controlSocket.connect(new InetSocketAddress(server, port), 5000);
        this.controlIn = new DataInputStream(new BufferedInputStream(this.controlSocket.getInputStream()));
        this.controlOut = new DataOutputStream(new BufferedOutputStream(this.controlSocket.getOutputStream()));

        SocketResponse response = null;
        loop:
        do {
            if (!this.connected) { //first packet; preface with channel type
                this.controlOut.writeInt(ChillDefines.HELLO);
                this.controlOut.writeInt(Channel.ARCH_CTL.ordinal());
            }
            this.sendPacket(new SocketArchCtl(
                    Command.CONNECT_REQ,
                    (short) 0, //start_sweep (ignored field)
                    (short) 2, //ray_step (2 = java version (vs old C version))
                    (short) Version.majorRevision, //sweep_low (major version)
                    (short) Version.minorRevision, //sweep_high (minor version)
                    0, //extra_delay (ignored field)
                    config.getSignon() + ":" + config.getPassword()));
            response = this.readResponse();
            if (response == null) return; //failure
            this.connected = true; //mark as connected so correct packet gets sent on retry
            switch (response.getStatus()) {
                case SERVER_READY: //no message of the day
                    break loop;
                case SIGNON_UNKNOWN: //bad login
                    config.setSignon(null);
                    config.setPassword(null);
                    DialogUtil.showErrorDialog("Unknown signon",
                            "Specified email address is not registered.\n" +
                                    "please try again, or if you have not\n" +
                                    "registered, please do so at:\n" +
                                    "http://chill.colostate.edu/vchill/");
                    if (!this.getLogin(server)) { //retried login cancelled
                        this.connected = false;
                        return;
                    }
                    break;
                case PASSWD_FAIL: //bad password
                    config.setPassword(null);
                    DialogUtil.showErrorDialog("Bad password",
                            "Password is incorrect, please check\n" +
                                    "your Caps Lock status and try again.\n" +
                                    "If you cannot, please call 970-491-6248\n" +
                                    "or email dave@chill.colostate.edu");
                    if (!this.getLogin(server)) { //retried login cancelled
                        this.connected = false;
                        return;
                    }
                    break;
                case SERVER_BUSY:
                case SERVER_FAILURE: //server problem
                    DialogUtil.showErrorDialog("Problems with " + this.url,
                            "Due to technical difficulties, the server is\n" +
                                    "currently not available.  Please try connecting\n" +
                                    "again in a few minutes.  If the problem persists,\n" +
                                    "please email dave@chill.colostate.edu and report\n" +
                                    "the problem.");
                    this.connected = false;
                    return;
                default: //unknown response
                    throw new Error("Don't know how to handle:\n" + response);
            }
        } while (true);
        this.sessionID = response.getExtStatus();
        System.out.println("session id = " + this.sessionID);
        this.controlSocket.setSoTimeout(0); //no timeout on control
        this.dataSocket = new Socket(server, port);
        this.dataSocket.setSoTimeout(0); //no default timeout on data
        this.dataIn = new DataInputStream(new BufferedInputStream(this.dataSocket.getInputStream()));
        this.dataOut = new DataOutputStream(new BufferedOutputStream(this.dataSocket.getOutputStream()));
        this.sendDataHandshake();
        if (this.controlSocket == null || this.dataSocket == null) {
            System.err.println("Socket FAILED!! to connect!");
            this.disconnect();
            throw new IOException("Fatal error! Socket connection failure!");
        }
        System.out.println("Connected.");
    }

    /**
     * Sets up the data connection.
     * Once the connection is open,  The client writes two ints: a hello word (int 0xf0f00f0f),
     * and a channel request word (int 15 is used to request the GEN_MOM_DAT_CHANNEL type).
     * The server will respond with a ChillHSKHeader, followed by a ChillMomentFieldScale
     * for each available field.  This provides the display with information on what's available
     * and how to scale and display the data.
     */
    private void sendDataHandshake() throws IOException {
        this.dataOut.writeInt(ChillDefines.HELLO);
        this.dataOut.writeInt((this.sessionID << 16) | Channel.GEN_MOM_DAT.ordinal());
        this.dataOut.flush();

        try {
            Thread.sleep(250);
        } //wait to see if data is coming in
        catch (InterruptedException ie) {
        }
        while (this.dataIn.available() > 0) { //get (now optional) initial scaling info
            ChillHeaderHeader headerH = new ChillHeaderHeader(this.dataIn);
            assert headerH.recordType == ChillDefines.FIELD_SCALE_DATA;
            ChillMomentFieldScale scale = new ChillMomentFieldScale(this.dataIn, headerH);
            sm.putScale(scale);
            if (this.dataIn.available() > 0) continue; //skip delay
            try {
                Thread.sleep(250);
            } //wait to see if data is still coming in
            catch (InterruptedException ie) {
            }
        }
    }

    /**
     * Reads a response from the control channel.
     * If that response was a message, shows that message and reads another response.
     *
     * @return a SocketResponse packet from the control channel,
     * or null if an EOFException is encountered
     */
    protected SocketResponse readResponse() throws IOException {
        SocketResponse response;
        while (true) {
            try {
                response = new SocketResponse(this.controlIn);
            } catch (EOFException eofe) { //got kicked out
                System.out.println("Kicked from server " + this.url + "!");
                DialogUtil.showErrorDialog("Kicked from server " + this.url + "!");
                this.cleanupDeadConnection();
                return null;
            }

            switch (response.getStatus()) {
                case REQUEST_ERROR:
                case OPEN_ERROR:
                case FORMAT_ERROR: //handle server error codes
                    //System.err.println(response.toString());
                    throw new IOException(response.getStatus().toString());
                case MESSAGE_FOLLOWS: //check for message
                    String message = SocketUtil.readString(this.controlIn, response.getExtStatus());
                    DialogUtil.showHelpDialog("Message from " + this.url, message);
                    break;
                default: //unknown response
                    return response;
            }
        }
    }

    /**
     * Sends a disconnect message, then closes the connection to the server
     */
    public synchronized void disconnect() throws IOException {
        System.out.println("socket: disconnecting");
        try {
            this.sendPacket(new SocketArchCtl(Command.DISCONNECT_REQ,
                    (short) 0, (short) 0, (short) 0, (short) 0, 0, "")); //ignored fields
            //disconnect doesn't get a response, so we don't need to read it
            //} catch (Exception e) {}
        } finally {
            cleanupDeadConnection();
        }
    }

    /**
     * Closes the (presumed dead) connection to the server
     */
    private void cleanupDeadConnection() throws IOException {
        this.dataIn.close();
        this.dataIn = null;
        this.dataOut.close();
        this.dataOut = null;
        this.dataSocket.close();
        this.dataSocket = null;
        this.controlIn.close();
        this.controlIn = null;
        this.controlOut.close();
        this.controlOut = null;
        this.controlSocket.close();
        this.controlSocket = null;
        this.connected = false;
        this.lastRequestedDataType = 0;
        this.sessionID = -1;
    }

    /**
     * Retrieves the list of available (sub-)directories and files from the server
     *
     * @return a Collection of String objects containing the directory listing (including any extended data)
     * @throws IOException if an error occurs when communicating with the server
     */
    public synchronized Collection<String> getDirectory(final String dir) throws IOException {
        SocketResponse response;
        try {
            Collection<String> list = new ArrayList<String>();
            this.sendPacket(new SocketArchCtl(
                    Command.DIRECTORY_REQ,
                    (short) DirType.CONTENTS.ordinal(), //dirs + files
                    (short) 0, (short) 0, (short) 0, 0, //ignored fields
                    dir.split(" ")[0])); //strip extra info
            response = this.readResponse(); //dir follows
            if (response == null) return list; //failure - return empty list
            assert response.getStatus() == Status.DIRECTORY_FOLLOWS;
            int dirlen = response.getExtStatus();
            byte[] dirlist = this.readControlBytes(dirlen);
            response = this.readResponse(); //dir sent
            if (response == null) return list; //failure - return empty list
            assert response.getStatus() == Status.DIRECTORY_SENT;
            int start = 0;
            for (int end = 0; end < dirlen; ++end) { //cut the byte[] into Strings
                switch (dirlist[end]) {
                    case 0:
                    case '\n':
                    case '\r': //end of a string
                        String x = new String(dirlist, start, end - start, "UTF-8");
                        start = end + 1;
                        if (!x.equals("")) {
                            String[] parts = x.split(" ");
                            if (parts.length < 2) {
                                if (parts[0].contains(".")) continue; //no scan type => not a data file
                            } else {
                                if (parts[parts.length - 1].equals("TS")) continue; //timeseries => no moment data
                            }
                            list.add(x);
                        }
                }
            }
            return list;
        } catch (SocketTimeoutException stoe) {
            System.err.println("socket: timed out getting directory; retrying");
            return this.getDirectory(dir);
        }
    }

    /**
     * Retrieves the list of available sweeps in a given file from the server
     *
     * @param dir  the directory to list
     * @param file a file in <code>dir</code> to list
     * @return a Collection of String objects containing the sweeps
     * @throws IOException if an error occurs when communicating with the server
     */
    public synchronized Collection<String> getSweepList(final String dir, final String file) throws IOException {
        try {
            SocketResponse response = this.getStatus(dir, file);
            if (response == null) return new ArrayList<String>(); //failure - return empty list
            int numSweeps = response.getMaxSweeps();
            Collection<String> list = new ArrayList<String>(numSweeps);
            for (int i = 0; i < numSweeps; ++i) list.add("Sweep " + nf.format(i + 1));
            if (response.isCalibrationPresent()) {
                FileTreeNode node = ViewFileBrowser.getInstance().getActions().getNode(url, dir, file);
                if (node != null)
                    node.special = true;
            }
            return list;
        } catch (SocketTimeoutException stoe) {
            System.err.println("socket: timed out getting sweep list; retrying");
            return this.getSweepList(dir, file);
        }
    }

    /**
     * Retrieves the status of a given file from the server.
     * this.response is overwritten with the result
     *
     * @param dir  name of a directory (full path, no trailing slash)
     * @param file name of a file in <code>dir</code> to get the status of
     * @return a SocketResponse object (this.response) containing the server's response
     * @throws IOException if an error occurs when communicating with the server
     */
    public synchronized SocketResponse getStatus(final String dir, final String file) throws IOException {
        this.sendPacket(new SocketArchCtl(
                Command.STATUS_REQ,
                (short) 0, (short) 0, (short) 0, (short) 0, 0, //ignored fields
                dir.split(" ")[0] + "/" + file.split(" ")[0])); //chop off extended info from names
        return this.readResponse();
    }

    /**
     * Sends the requested fields list to the server.
     *
     * @param newDataType A bitmask of requested fields.
     * @throws java.io.IOException
     */
    private void sendRequestedFields(final long newDataType) throws IOException {
        if (newDataType != this.lastRequestedDataType) { //only reset data channel if actually different
            System.out.println("requesting " + Long.toBinaryString(newDataType));//Long.toHexString(newDataType));
            this.dataOut.writeLong(this.lastRequestedDataType = newDataType);
            this.dataOut.flush();
        }
    }

    /**
     * Retrieves a sweep from the server.
     * The downloaded sweep is stored in the cache specified.
     *
     * @param commands a ControlSyncQueue containing a ControlMessage containing all necessary info for type of data desired
     * @param cache    a shared cache object to load the data into
     * @throws IOException if an error occurs when communicating with the server
     */
    public synchronized void getSweep(final ControlSyncQueue<TypedControlMessage> commands, final CacheMain cache) throws IOException {
        //gather list of types to get
        TypedControlMessage command = commands.get();
        if (!command.message.isValid()) {
            this.connection.setIsSweepDone(true);
            return;
        }

        ArrayList<String> requestTypes = new ArrayList<String>(ChillDefines.MAX_PER_CHANNEL); //ControlMessages representing data requested
        ControlMessage rayCommand; //data currently being worked on
        ChillHeaderReader hr = new ChillHeaderReader(this.dataIn, cache) {
            ArrayList<String> types = null; //names of available data types

            @Override
            public boolean readData(final ChillDataHeader dataH, final ControlMessage metaCommand) throws IOException {
                if (types == null) { //only initialize once
                    types = dataH.calculateTypes(); //determine which types are available
                }
                dataSocket.setSoTimeout(0); //no timeout on data
                byte[][] data = new byte[types.size()][dataH.numGates];
                byte[] interleavedData = readDataBytes(types.size() * dataH.numGates);
                for (int t = 0; t < types.size(); ++t) { //split data types
                    if (types.get(t) == null) continue;
                    for (int g = 0; g < dataH.numGates; ++g) {
                        data[t][g] = interleavedData[types.size() * g + t];
                    }
                    cache.addRay(metaCommand, types.get(t), new ChillGenRay(hskH, dataH, types.get(t), data[t])); //add data to cache
                }
                return true; //currRayNum should increment
                //++currRayNumber; //increment ray number here so it doesn't go up on "continue"
            }
        };

        System.out.println("socket: getting " + command);
        long newDataType = 0;
        for (String type : command.types) {
            ChillMomentFieldScale scale = sm.getScale(type);
            if (scale == null) continue; //unknown/not available
            requestTypes.add(type); //unknown fields will not be marked complete
            newDataType |= 1l << scale.fieldNumber; //add the new type to the bitmask
            if (requestTypes.size() == ChillDefines.MAX_PER_CHANNEL)
                break; //allow at most MAX_PER_CHANNEL data types (this is the server's limit)
        }

        sendRequestedFields(newDataType);

        //request data
        String filename =
                command.message.getDir().split(" ")[0] + "/" +
                        command.message.getFile().split(" ")[0];
        this.sendPacket(new SocketArchCtl(
                Command.SWEEP_MODE,
                Short.parseShort(command.message.getSweep().split(" ")[1]),
                (short) 0, (short) 0, (short) 0, 0, //ignored fields
                filename));
        SocketResponse response = this.readResponse();
        if (response == null) { //failure
            this.connection.setIsSweepDone(true);
            return;
        }
        if (response.getStatus() == Status.POSITION_ERROR) {
            System.err.println("Warning: Position error - skipping nonexistant sweep");
            markComplete(requestTypes, command.message, cache); //error - don't bother to retry
            this.connection.setIsSweepDone(true);
            return;
        } else if (!response.isRunning()) { //something went wrong and the server isn't sending the sweep
            System.err.println("Failed to get sweep");
            markComplete(requestTypes, command.message, cache); //error - don't bother to retry
            this.connection.setIsSweepDone(true);
            return;
        }

        //read radar data into cache
        sm.clear(); //clear the list of fields because the server should be sending us a new list
        int firstRayNumber = response.getRayNumber() - 1; //convert to 0-based
        int lastRayNumber = Integer.MAX_VALUE; //don't know when to stop yet
        for (int currRayNumber = firstRayNumber; currRayNumber < lastRayNumber; ) { //each ray:
            if (commands.stopping()) { //stop request received
                System.out.println("SocketMain: Attempting to stop...");
                this.stop(filename);
                for (String type : requestTypes) cache.removeType(command.message, type); //remove incomplete sweep
                this.connection.setIsSweepDone(true);
                return;
            }

            if (this.controlIn.available() > 0) { //notification: sweep done
                response = this.readResponse();
                if (response == null) { //failure
                    for (String type : requestTypes) cache.removeType(command.message, type); //remove incomplete sweep
                    this.connection.setIsSweepDone(true);
                    return;
                }
                lastRayNumber = response.getRayNumber(); //we know when to stop now
                if (lastRayNumber == currRayNumber)
                    break;
            }

            try {
                this.dataSocket.setSoTimeout(TIMEOUT); //default timeout on data
                //mark the stream so that if a timeout occurs then we can
                //put back all the data read so that VCHILL doesn't get
                //messed up
                this.dataIn.mark(Integer.MAX_VALUE);
                if (hr.readHeader(command.message)) ++currRayNumber; //successful data read?
            } catch (SocketTimeoutException stoe) {
                System.err.println("SocketMain: Read timed out getting sweep; retrying");
                //put back the data that we tried reading
                this.dataIn.reset();
                continue;
            }

            //try { Thread.sleep(5); } //let plotting code have a go
            //catch (InterruptedException ie) {}
        }
        markComplete(requestTypes, command.message, cache);
        this.connection.setIsSweepDone(true);
    }

    private void markComplete(final ArrayList<String> requestTypes, final ControlMessage command, final CacheMain cache) {
        //mark each type as complete - 
        //use requested rather than available commands so unavailable data is not requested again
        for (String type : requestTypes) {
            System.out.println("marking " + type + " complete; cached " + cache.getNumberOfRays(command, type) + " rays");
            cache.setCompleteFlag(command, type);
        }
        cache.setCompleteFlag(command, ChillDefines.META_TYPE);
    }

    /**
     * Stop server from sending further data and clear all input streams of remaining unprocessed data
     *
     * @param filename the name of the file currently downloading
     * @throws IOException if an error occurs while communicating with the server
     */
    private void stop(final String filename) throws IOException {
        this.sendPacket(new SocketArchCtl(
                Command.HALT_COMMAND,
                (short) 0, (short) 0, (short) 0, (short) 0, 0, //ignored fields
                filename));
        //The server sends a response to the halt command but
        //since we are going to skip everything then it's not
        //necessary to explicitly read the response. In fact,
        //not explicitly reading it prevents the situation where
        //vchill gets stuck because it's supposed to read the
        //data channel first.
        //SocketResponse response = this.readResponse();
        //if (response == null) return; //failure
        //System.out.println(response);
        clearChannels();
    }

    /**
     * Clears all channels of unprocessed incoming data
     *
     * @throws IOException if an error occurs
     */
    private void clearChannels() throws IOException {
        int toSkip;
        int skipped;
        while (this.dataIn.available() + this.controlIn.available() > 0) {
            toSkip = this.controlIn.available();
            System.out.println("skipping " + toSkip + " control bytes");
            skipped = 0;
            while (skipped < toSkip) skipped += this.controlIn.skip(toSkip);

            toSkip = this.dataIn.available();
            System.out.println("skipping " + toSkip + " data bytes");
            skipped = 0;
            while (skipped < toSkip) skipped += this.dataIn.skip(toSkip);

            try {
                Thread.sleep(100);
            } //wait to see if data is still coming in
            catch (InterruptedException ie) {
            }
        }
    }

    /**
     * Send a command packet over the control channel
     *
     * @param command the command to send
     */
    protected void sendPacket(final SocketArchCtl command) throws IOException {
        command.write(this.controlOut);
        this.controlOut.flush();
    }

    /**
     * Check connection status
     *
     * @return true if the connection is active and ready, false if it is not
     */
    public synchronized boolean connected() {
        if (this.controlSocket == null || this.dataSocket == null)
            return false;

        if (!(this.controlSocket.isConnected() && this.dataSocket.isConnected())) {
            try {
                System.out.println("Socket: socket timed out, disconnecting");
                this.disconnect();
            } catch (IOException ie) {
                throw new Error("Socket PANIC!! - IOException while trying to disconnect: ", ie);
            }
        }
        return this.connected;
    }

    /**
     * Read a specified number of bytes from the control channel
     *
     * @param numBytes the number of bytes to read from in (will block until numBytes bytes read)
     * @return byte[numBytes] containing the read bytes
     */
    private byte[] readControlBytes(final int numBytes) throws IOException {
        return this.readBytes(numBytes, this.controlIn);
    }

    /**
     * Read a specified number of bytes from the data channel
     *
     * @param numBytes the number of bytes to read from in (will block until numBytes bytes read)
     * @return byte[numBytes] containing the read bytes
     */
    private byte[] readDataBytes(final int numBytes) throws IOException {
        return this.readBytes(numBytes, this.dataIn);
    }

    /**
     * Read a specified number of bytes from the given InputStream
     *
     * @param numBytes the number of bytes to read from in (will block until numBytes bytes read)
     * @param in       the stream to read from
     * @return byte[numBytes] containing the read bytes
     */
    private byte[] readBytes(final int numBytes, final DataInputStream in) throws IOException {
        byte[] result = new byte[numBytes];
        in.readFully(result);
        return result;
    }
}
