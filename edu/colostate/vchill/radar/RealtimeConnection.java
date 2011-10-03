package edu.colostate.vchill.radar;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ChillDefines.Channel;
import edu.colostate.vchill.Config;
import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.LocationManager;
import edu.colostate.vchill.ViewControl;
import edu.colostate.vchill.cache.CacheMainCyclic;
import edu.colostate.vchill.chill.ChillDataHeader;
import edu.colostate.vchill.chill.ChillGenRay;
import edu.colostate.vchill.chill.ChillHeaderReader;
import edu.colostate.vchill.connection.Connection;
import edu.colostate.vchill.gui.WindowManager;
import edu.colostate.vchill.plot.ViewPlotWindow;
import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

/**
 * Caching realtime socket connection for VCHILL's data acquisition backend.
 * This implementation is not synchronized; access mut be synchronized externally.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public final class RealtimeConnection extends Connection
{
    private static final WindowManager wm = WindowManager.getInstance();
    private static final LocationManager lm = LocationManager.getInstance();
    private static final ViewControl vc = ViewControl.getInstance();
    private static final Config config = Config.getInstance();
    private static final int BUFFER_SIZE = 5;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private final ControlMessage metaCommand;
    private final String server;
    private final int port;
	private CacheThread cacheThread;
	private Thread prefetch;

    /**
     * Constructor for the RealtimeConnection object 
     * @param url server:port to connect to
     */
    public RealtimeConnection (final String url) throws IOException
    {
        super(new CacheMainCyclic(BUFFER_SIZE));
        this.df.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.metaCommand = new ControlMessage(url, ChillDefines.REALTIME_DIR, ChillDefines.REALTIME_FILE, ChillDefines.REALTIME_SWEEP);
        String[] parts = url.split(":");
        assert parts.length == 2 : "Malformed url - should be server:port";
        this.server = parts[0];
        this.port = Integer.parseInt(parts[1]);
        this.connect();
        assert vc != null;
    }

    @Override public Collection<String> getDirectory (final ControlMessage key) throws IOException
    {
        Collection<String> tmp = new ArrayList<String>(1);
        tmp.add(ChillDefines.REALTIME_FILE);
        return tmp;
    }

    @Override public Collection<String> getSweepList (final ControlMessage key) throws IOException
    {
        Collection<String> tmp = new ArrayList<String>(1);
        tmp.add(ChillDefines.REALTIME_SWEEP);
        return tmp;
    }

    @Override public Object getRay (final ControlMessage key, final String type, final int index)
    {
        return cache.getData(key, type, index);
    }

    @Override public Object getRayWait (final ControlMessage key, final String type, final int index)
    {
        return cache.getData(key, type, index);
    }

    @Override public void connect () throws IOException
    {
	cacheThread = new CacheThread();
	cacheThread.connect();
        prefetch = new Thread(cacheThread, "RealtimeConnection.CacheThread");
        prefetch.setDaemon(true);
        prefetch.setPriority(Thread.NORM_PRIORITY);
        prefetch.start();
	super.connect();
	config.setRealtimeModeEnabled(true);
	config.setDefaultRealtimeName(this.server + ":" + this.port);
	config.setLastConnectionType(Config.REALTIME_CONN);
    }

    @Override public boolean disconnect () throws IOException
    {
        this.stop();
		while( prefetch.isAlive() ) { //Make sure the prefetch thread stops first
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {}
		}
		this.commands.stopped();
        super.disconnect();
        config.setRealtimeModeEnabled(false);
        vc.setCurrentURL(null);
        return true; //always remove from list
    }

	/**
     * Reconnects to this connection's server and port
     */
    @Override public void reconnect () throws IOException
    {
		this.commands.clear();
		cache.clear();
		cacheThread.reconnect( 1000 );
    }
 
    protected class CacheThread implements Runnable
    {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private ChillHeaderReader hr;
        private long lastTypeRequested;

        public CacheThread ()
        {
            this.lastTypeRequested = -1;
            //this.connect();
        }

        /**
         * Open a connection to the server
         */
        protected void connect () throws IOException
        {
            while (!connected) {
                System.out.println("RealtimeConnection: trying to connect to " + server + ":" + port);
                try {
		    this.socket = new Socket();
		    this.socket.setSoTimeout(2500); //ms
		    this.socket.connect( new InetSocketAddress(server, port), 2500 );
                    this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
                    this.out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
                    this.hr = new ChillHeaderReader(this.in, cache) {
                        @Override public void hskHupdated () {
                            lm.setLatitude(hskH.radarLatitude * 1e-6);
                            lm.setLongitude(hskH.radarLongitude * 1e-6);
                            wm.replotOverlay();
                        }

                        private int curr = 0; //current position in the buffer; used to plot data
                        @Override public boolean readData (final ChillDataHeader dataH, final ControlMessage metaCommand) throws IOException {
                            ArrayList<String> types = dataH.calculateTypes();
                            byte[][] data = new byte[types.size()][dataH.numGates];
                            byte[] interleavedData = new byte[types.size() * dataH.numGates];
                            this.in.readFully(interleavedData);
                            for (int t = 0; t < types.size(); ++t) { //split data types
								if( types.get(t) == null ) continue;
                                for (int g = 0; g < dataH.numGates; ++g) {
                                    data[t][g] = interleavedData[types.size() * g + t];
                                }
                                cache.addRay(metaCommand, types.get(t), new ChillGenRay(hskH, dataH, types.get(t), data[t]));
                            }
                            vc.setMessage(metaCommand);
    //System.out.println("plotting ray ");
                            vc.plotRay(curr = (curr + 1) % BUFFER_SIZE);
                            return true; //data read successful
                        }

                        private boolean saveArmed = false;
                        @Override public void startNotice () {
                            if (config.isSaveAllEnabled() || hskH.tiltNum == hskH.saveTilt) { //always save or webtilt
                                if (config.isImageAutosaveEnabled() || config.isImageAutoExportEnabled()) { //save or export enabled
                                    try { EventQueue.invokeAndWait(new Runnable() { public void run () {
                                        wm.clearScreen();
                                        for (ViewPlotWindow win : wm.getPlotList())
                                        {
                                            //clear any existing aircraft info from previous sweeps
                                            win.clearAircraftInfo();
                                        }
                                        wm.setPlotting(true);
                                    }}); } catch (Exception e) { throw new Error(e); }
                                    saveArmed = true;
                                }
                            }
                        }
                        @Override public void endNotice (final ControlMessage metaCommand) {
                            if (!saveArmed) return; //no matching start of sweep -> don't save
                            if (config.isSaveAllEnabled() || hskH.tiltNum == hskH.saveTilt) { //always save or webtilt
                                final ControlMessage msg = metaCommand.setFile("realtime " + df.format(new Date())).setSweep("Sweep " + hskH.tiltNum);
                                if (config.isImageAutosaveEnabled()) { //save enabled
                                    try { EventQueue.invokeAndWait(new Runnable() { public void run () {
                                        wm.savePlotImages(msg);
                                    }}); } catch (Exception e) { throw new Error(e); }
                                }
                                if (config.isImageAutoExportEnabled()) { //export enabled
                                    try { EventQueue.invokeAndWait(new Runnable() { public void run () {
                                        wm.export(msg);
                                    }}); } catch (Exception e) { throw new Error(e); }
                                }
                                wm.setPlotting(false);
                                saveArmed = false;
                            }
                        }
                    };
                    this.out.writeInt(ChillDefines.HELLO);
                    this.out.writeInt(Channel.GEN_MOM_DAT.ordinal());
                    this.out.flush();
                } catch (SocketException se) {
                    System.out.println("Error connecting to server, will retry");
                    this.sleep(15000);
                    continue;
                }
                connected = true;
                System.out.println("connected");
            }
        }

        /**
         * Close the connection to the server
         */
        private void disconnect ()
        {
            System.out.println("RealtimeConnection: trying to disconnect from " + server + ":" + port);
            try {
                if (this.out != null) {
                    this.out.close();
                    this.out = null;
                }
                if (this.in != null) {
                    this.in.close();
                    this.in = null;
                }
                if (this.socket != null) {
                    this.socket.close();
                    this.socket = null;
                }
            } catch (Exception e) {
                throw new Error(e);
            } finally {
                connected = false;
				this.lastTypeRequested = -1;
                System.out.println("disconnected");
            }
        }

        /**
         * Continually load data into the cache, processing new type and stop requests as they occur.
         */
        public void run ()
        {
            int timesWaited = 0;
            System.out.println("RealtimeConnection.CacheThread is running");
            while (true) {
                try {
                    if (commands.stopping()) { //stop requested?
                        this.disconnect();
                        return;
                    }

                    this.requestType(wm.getOpenWindows()); //necessity checked in method

                    if (this.in.available() > 0) { //get data
                        this.hr.readHeader(metaCommand); //calls back to read data
                        timesWaited = 0;
//System.out.println("done");
                        this.sleep(25); //don't steal processor from gui
                    } else { //wait for data
                        ++timesWaited;
                        if (timesWaited > 100) { //waited for too long to be normal
                            System.out.println("No data from server - connection dead?");
                            timesWaited = 0;
                            this.out.writeLong(this.lastTypeRequested); //test connection
                            this.out.flush();
                        } else {
                            this.sleep(50);
                        }
                    }
                } catch (SocketTimeoutException ste) { //apparently only if partial header block arrived
                    System.err.println("Timeout communicating with server, attempting to reconnect");
                    timesWaited = 0;
                    this.reconnect(5000);
                } catch (SocketException se) { //connection reset or the like
                    System.err.println("Error communicating with server, attempting to reconnect");
                    timesWaited = 0;
                    this.reconnect(5000);
                } catch (NullPointerException npe) {
                    System.err.println("Caught a null pointer exception");
                    timesWaited = 0;
                    this.reconnect(0);
                } catch (Exception e) { throw new Error(e); } //unknown (and therefore fatal) problem
            }
        }

        private void reconnect (long sleepTime)
        {
            this.disconnect();
            this.sleep(sleepTime); //don't overload the network with reconnection attempts
            try { this.connect(); } catch (IOException ioe) { ioe.printStackTrace(); }
        }

        /**
         * Utility method to keep InterruptedExcpetions out of normal code
         */
        private void sleep (final long millis)
        {
            try { Thread.sleep(millis); } catch (InterruptedException ie) {}
        }

        /**
         * Alert the server that we now want this type instead.
         * @param type the bitmask of the desired type(s)
         */
        private void requestType (final long type) throws IOException
        {
            if (type == this.lastTypeRequested) return;
            this.out.writeLong(this.lastTypeRequested = type);
            this.out.flush();
        }
    }
}
