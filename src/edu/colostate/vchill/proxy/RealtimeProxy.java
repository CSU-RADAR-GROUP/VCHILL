package edu.colostate.vchill.proxy;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ChillDefines.Channel;
import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.HdrUtil;
import edu.colostate.vchill.KdpUtil;
import edu.colostate.vchill.NcpPlusUtil;
import edu.colostate.vchill.RainUtil;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.cache.CacheMainCyclic;
import edu.colostate.vchill.chill.ChillDataHeader;
import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.chill.ChillGenRay;
import edu.colostate.vchill.chill.ChillHeader;
import edu.colostate.vchill.chill.ChillHeaderHeader;
import edu.colostate.vchill.chill.ChillHSKHeader;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.chill.ChillTrackInfo;
import edu.colostate.vchill.gui.GUIUtil;
import edu.colostate.vchill.socket.SocketArchCtl.Command;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Driver class for a realtime proxy server.
 * Listens to socket requests and creates new threads to handle them.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */

public final class RealtimeProxy extends Proxy implements Control
{
    private ControlMessage pseudo;

    /** private constructor prevents instantiation */
    private RealtimeProxy ()
    {
        //change defaults; can be overriden via commandline args
        super.serverName = "force.chill.colostate.edu";
        super.cacheSize = 3;
    }

    public static void main (final String[] args) throws IOException
    {
        new RealtimeProxy().parseCommandLineArguments(args).run();
        System.exit(0);
    }

    public void run () throws IOException
    {
        if (guiflag) GUIUtil.startGUI("Java VCHILL Realtime Proxy Server"); //gui requested

        // instantiate cache
        cache = new CacheMainCyclic(cacheSize);

        //connect to realtime data server
        pseudo = new ControlMessage(serverName + ":" + serverPort, ChillDefines.REALTIME_DIR, ChillDefines.REALTIME_FILE, ChillDefines.REALTIME_SWEEP);
        new Thread(new CacheThread(cache, serverName, serverPort, calcflag), "RealtimeProxy.CacheThread").start();

        //set up listening server socket
        ServerSocket clientsocket = null;
        try {
            clientsocket = new ServerSocket(listenPort);
            System.out.println("RealtimeProxy: Listening on port " + listenPort);
        } catch (IOException ioe) {
            throw new Error("RealtimeProxy: Could not listen on port " + listenPort, ioe);
        }

        while (true) { // repeat forever
            Socket socket = clientsocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            int fromSocket = in.readInt();
            if (fromSocket == Command.HALT_COMMAND.ordinal()) {
                String pwd = in.readUTF();
                if (password.equals(pwd)) {
                    System.out.println("Realtime proxy server shutting down");
                    return;
                } else {
                    System.out.println("Shutdown attempt with bad password: " + pwd);
                    System.out.println("Realtime proxy server NOT shutting down");
                }
            } else if (fromSocket == ChillDefines.HELLO) {
                fromSocket = in.readInt(); //channel
                if (fromSocket == Channel.MOM_DAT.ordinal()) {
                    System.out.println("RealtimeProxy: Data socket request identified from " + socket.getInetAddress().getHostName());
                    //now do something constructive with it
                    ProxyDataThread thread = new ProxyDataThread(socket, this, cache, true);
                    thread.start();
                    thread.send(pseudo);
                } else {
                    System.out.println("Bad request from " + socket.getInetAddress().getHostName() + ": unknown channel type");
                }
            } else {
                System.out.println("Bad request from " + socket.getInetAddress().getHostName() + ": outdated version?");
            }

            Proxy.sleep(100);
        }
    }

    /**
     * Sets the global setting variables according to the arguments
     * passed in.  If an error is encountered, the application is
     * terminated.
     *
     * @param args The command line arguments as passed to main
     * @return this object; useful for chaining this method into creation
     */
    private RealtimeProxy parseCommandLineArguments (final String[] args)
    {
        //parse commandline arguments
        if (args.length % 2 != 0) die();
        for (int i = 0; i < args.length; i += 2) {
            if (args[i].equals("-server") || args[i].equals("-s")) {
                serverName = args[i + 1];
            } else if (args[i].equals("-serverport") || args[i].equals("-sp")) {
                try { serverPort = Integer.parseInt(args[i + 1]); }
                catch (NumberFormatException e) { die(); }
                if (serverPort < 1 || serverPort > 65535) die();
            } else if (args[i].equals("-listenport") || args[i].equals("-lp")) {
                try { listenPort = Integer.parseInt(args[i + 1]); }
                catch (NumberFormatException e) { die(); }
                if (listenPort < 1 || listenPort > 65535) die();
            } else if (args[i].equals("-timeout") || args[i].equals("-t") || args[i].equals("-to")) {
                try { timeout = 60000l * Integer.parseInt(args[i + 1]); }
                catch (NumberFormatException e) { die(); }
                if (timeout < 0) die();
            } else if (args[i].equals("-calculation") || args[i].equals("-c")) {
                if (args[i + 1].equals("on")) {
                    calcflag = true;
                } else if (args[i + 1].equals("off")) {
                    calcflag = false;
                } else {
                    die();
                }
            } else if (args[i].equals("-password") || args[i].equals("-p")) {
                password = args[i + 1];
            } else if (args[i].equals("-gui") || args[i].equals("-g")) {
                if (args[i + 1].equals("on")) {
                    guiflag = true;
                } else if (args[i + 1].equals("off")) {
                    guiflag = false;
                } else {
                    die();
                }
            } else {
                die();
            }
        }
        addCalculatedTypeScales();
        return this;
    }

    /**
     * Prints usage message and exits.
     */
    private static void die ()
    {
        System.err.println("Usage: java RealtimeProxy [-server name] [-serverport port]");
        System.err.println("    [-listenport port] [-timeout minutes] [-calculation on|off]"); 
        System.err.println("    [-password pwd] [-gui on|off]"); 
        System.err.println("Defaults: force.chill.colostate.edu 2510 2510 60 on secret");
        System.err.println("server: what server the proxy should connect to");
        System.err.println("serverport: what port the proxy should connect to");
        System.err.println("listenport: what port the proxy should listen on");
        System.err.println("timeout: how long the control socket can idle before being terminated");
        System.err.println("    0 means forever");
        System.err.println("calculation: if the proxy should calculate hybrid data types");
        System.err.println("    Turn this off if you're connecting to another proxy");
        System.err.println("password: a password which must be matched to shut down the proxy");
        System.exit(1);
    }

    public synchronized void killConnection (final ProxyDataThread pdt)
    {
    }

    private class CacheThread implements Runnable
    {
        private final CacheMain cache;
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private final String server;
        private final int port;
        private final boolean calcflag;

        /**
         * @param cache a shared cache to load data into
         * @param server the name of the server to connect to
         * @param port the port to connect to
         */ 
        public CacheThread (final CacheMain cache, final String server, final int port, final boolean calcflag)
        {
            this.cache = cache;
            this.server = server;
            this.port = port;
            this.calcflag = calcflag;
            this.connect();
        }

        /**
         * Open a connection to the server
         */
        private void connect ()
        {
            System.out.println("RealtimeProxy: trying to connect to " + this.server + ":" + this.port);
            int allTypes = 0;
            for (ChillFieldInfo type : ChillFieldInfo.types) {
                if ((type.fieldNumber < ChillFieldInfo.CALC_CUTOFF) || !this.calcflag) {
                   allTypes |= 1l << type.fieldNumber;
//System.out.println(Integer.toHexString(allTypes));
                }
            }
            try {
                this.socket = new Socket(this.server, this.port);
                this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
                this.out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
                this.out.writeInt(Channel.MOM_DAT.ordinal());
                this.out.writeInt(allTypes);
                this.out.flush();
            } catch (Exception e) { throw new Error(e); }
            System.out.println("connected");
        }

        /**
         * Continually load data into the cache, processing new type and stop requests as they occur.
         */
        public void run ()
        {
            System.out.println("RealtimeProxy.CacheThread is running");

            ChillHSKHeader hskH = null;
            LOOP: { ChillGenRay prevZDR = null; while (true) {
                ChillGenRay rayZ = null;
                ChillGenRay rayZDR = null;
                ChillGenRay rayNCP = null;
                ChillGenRay rayPHIDP = null;
                ChillGenRay rayRHOHV = null;

                try {
                    if (this.in.available() > 0) { //get data
                        //check whether this is scaling info or moment header
                        ChillHeaderHeader headerH = new ChillHeaderHeader(this.in);
                        switch (headerH.recordType) {
                            case ChillDefines.GEN_MOM_DATA:
                                ChillDataHeader dataH = new ChillDataHeader(this.in, headerH);
                                ArrayList<String> types = dataH.calculateTypes();
                                byte[][] data = new byte[types.size()][dataH.numGates];
                                byte[] interleavedData = new byte[types.size() * dataH.numGates];
                                this.in.readFully(interleavedData);
                                for (int t = 0; t < types.size(); ++t) { //split data types
									String type = types.get(t);
									if( type == null ) continue;
                                    for (int g = 0; g < dataH.numGates; ++g) {
                                        data[t][g] = interleavedData[types.size() * g + t];
                                    }
                                    ChillGenRay ray =  new ChillGenRay(hskH, dataH, type, data[t]);
                                    { //save references to some types for calculations
                                             if (type.equals(ChillFieldInfo.Z.fieldName))     rayZ     = ray;
                                        else if (type.equals(ChillFieldInfo.ZDR.fieldName))   rayZDR   = ray;
                                        else if (type.equals(ChillFieldInfo.NCP.fieldName))   rayNCP   = ray;
                                        else if (type.equals(ChillFieldInfo.PHIDP.fieldName)) rayPHIDP = ray;
                                        else if (type.equals(ChillFieldInfo.RHOHV.fieldName)) rayRHOHV = ray;
                                    }
                                    cache.addRay(pseudo, type, ray);
                                }

                                CALC: {
                                    double[] dataZ     = rayZ     == null ? null : rayZ.getData();
                                    double[] dataZDR   = rayZDR   == null ? null : rayZDR.getData();
                                    double[] dataZDRp  = prevZDR  == null ? null : prevZDR.getData();
                                    double[] dataNCP   = rayNCP   == null ? null : rayNCP.getData();
                                    double[] dataPHIDP = rayPHIDP == null ? null : rayPHIDP.getData();
                                    double[] dataRHOHV = rayRHOHV == null ? null : rayRHOHV.getData();
                                    double[] dataHDR = null;
                                    double[] dataKDP = null;

                                    HDR: {
                                        //if (dataZ == null) System.out.println ("Z missing");
                                        //if (dataZDR == null) System.out.println ("ZDR missing");
                                        if (dataZ == null || dataZDR == null) {
                                            //System.out.println ("not calculating HDR");
                                            break HDR;
                                        }
                                        //System.out.println("calculating HDR");

                                        dataHDR = HdrUtil.calculateHDR(dataZ, dataZDR);

                                        //store results
                                        cache.addRay(pseudo, ChillFieldInfo.HDR.fieldName, new ChillGenRay(hskH, dataH, ChillFieldInfo.HDR.fieldName, dataHDR));
                                    }

                                    KDP: {
                                        //if (dataPHIDP == null) System.out.println("PHIDP missing");
                                        //if (dataRHOHV == null) System.out.println("RHOHV missing");
                                        if (dataPHIDP == null || dataZ == null || dataRHOHV == null) {
                                            //System.out.println("not calculating KDP");
                                            break KDP;
                                        }
                                        //System.out.println("calculating KDP");

                                        dataKDP = KdpUtil.calculateKDP(dataPHIDP, dataZ, dataRHOHV, rayPHIDP.getStartRange() * 1e-6, rayPHIDP.getGateWidth());

                                        //store result
                                        cache.addRay(pseudo, ChillFieldInfo.KDP.fieldName, new ChillGenRay(hskH, dataH, ChillFieldInfo.KDP.fieldName, dataKDP));
                                    }

                                    RCOMP: {
                                        //if (dataKDP == null) System.out.println("KDP missing");
                                        if (dataKDP == null || dataZ == null || dataZDR == null) {
                                            //System.out.println("not calculating rain");
                                            break RCOMP;
                                        }
                                        //System.out.println("calculating rain");

                                        double[] rain = RainUtil.calculateCompositeRain(dataKDP, dataZ, dataZDR);

                                        //store result
                                        cache.addRay(pseudo, ChillFieldInfo.RCOMP.fieldName, new ChillGenRay(hskH, dataH, ChillFieldInfo.RCOMP.fieldName, rain));
                                    }

                                    NCP_PLUS: {
                                        //if (dataNCP == null) System.out.println("NCP missing");
                                        if (dataNCP == null || dataZDR == null) {
                                            //System.out.println("not calculating NCP_PLUS");
                                            break NCP_PLUS;
                                        }
                                        //System.out.println("calculating NCP_PLUS");

                                        double[] dataNCP_PLUS = NcpPlusUtil.calculateNCP_PLUS(dataNCP, dataZDRp, dataZDR, null);

                                        //store result
                                        cache.addRay(pseudo, ChillFieldInfo.NCP_PLUS.fieldName, new ChillGenRay(hskH, dataH, ChillFieldInfo.NCP_PLUS.fieldName, dataNCP_PLUS));
                                    }
                                }
//System.out.println("bytes available after data = " + this.in.available());
                                break;
                            case ChillDefines.BRIEF_HSK_DATA:
                                hskH = new ChillHSKHeader(this.in, headerH);
                                break;
                            case ChillDefines.FIELD_SCALE_DATA:
                                ChillMomentFieldScale scale = new ChillMomentFieldScale(this.in, headerH);
                                sm.putScale(scale);
                                break;
                            case ChillDefines.TRACK_DATA:
                                ChillTrackInfo track = new ChillTrackInfo(this.in, headerH);
                                break;
                            default:
                                System.out.println("Don't know how to handle header of type " + headerH.recordType);
                                ChillHeader generic = new ChillHeader(headerH);
                                break;
                        }
                    } else { Proxy.sleep(100); } //wait for data
                } catch (Exception e) { throw new Error(e); }
            prevZDR = rayZDR; }} //end loop
        }
    }
}
