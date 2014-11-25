package edu.colostate.vchill.connection;

import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.cache.CacheMainLRU;
import edu.colostate.vchill.file.FileConnection;
import edu.colostate.vchill.gui.ViewFileBrowser;
import edu.colostate.vchill.radar.RealtimeConnection;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Control module for VCHILL's data acquisistion backend - controls Socket, Cache and File modules.
 * No external synchronization is necessary.
 *
 * @author Jochen Deyke
 * @author Alexander Deyke
 * @author jpont
 * @version 2009-11-10
 */
public class Controller extends Observable {
    public static final String FILESYSTEM = "Local%20Filesystem";
    private final CacheMainLRU cache = new CacheMainLRU(2); //shared cache
    private final HashMap<String, Connection> connections = new HashMap<String, Connection>();
    private final FileConnection filesystem;

    /**
     * Sole constructor
     */
    public Controller() {
        connections.put(FILESYSTEM, this.filesystem = new FileConnection(cache));
        try {
            this.filesystem.connect();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void addFile(final File file) {
        this.filesystem.addFile(file);
        ViewFileBrowser.getInstance().getActions().refreshConnections();
    }

    /**
     * Gets the list of all currently active connections.
     * Each is identified by a string of the format servername:port
     *
     * @return a Collection of String objects containing available servers and ports
     */
    public Collection<String> getConnectionList() {
        synchronized (this.connections) {
            String[] connectionStrings = this.connections.keySet().toArray(new String[0]);
            Arrays.sort(connectionStrings);
            Vector<String> connectionList = new Vector<String>();
            for (String connection : connectionStrings)
                connectionList.add(connection);
            return connectionList;
        }
    }

    /**
     * Gets the list of all available (sub-)directories and files on a given server and port
     * in a given directory
     *
     * @param key ControlMessage containing the directory, server and port (other fields are ignored)
     * @return a Collection of String objects containing all available directories
     */
    public Collection<String> getDirectory(final ControlMessage key) throws IOException {
        return this.selectConnection(key).getDirectory(key);
    }

    /**
     * Gets the list of all available sweeps in a given file in a given directory on a given server and port
     *
     * @param key ControlMessage containing the file, directory, server and port (other fields are ignored)
     * @return a Collection of String objects containing all available sweeps
     */
    public Collection<String> getSweepList(final ControlMessage key) throws IOException {
        return this.selectConnection(key).getSweepList(key);
    }

    /**
     * Alert the backend that we want a certain sweep for display
     *
     * @param key ControlMessage containing all types needed
     */
    public void requestSweep(final ControlMessage key, final Set<String> wantedTypes) {
        this.selectConnection(key).requestSweep(key, wantedTypes);
    }

    /**
     * Indicates whether the sweep has been fully read in yet.
     *
     * @param key ControlMessage to indentify server and sweep.
     */
    public boolean isSweepDone(final ControlMessage key) {
        return this.selectConnection(key).isSweepDone();
    }

    /**
     * Gets a specific ray
     *
     * @param key ControlMessage containing the desired data type, sweep, file, directory, server and port
     * @return the requested ray (or null if it does not exist)
     */
    public Object getRay(final ControlMessage key, final String type, final int ray) {
        return this.selectConnection(key).getRay(key, type, ray);
    }

    /**
     * Gets a specific ray.  If the ray is not yet available, waits
     * for the ray to become available or the sweep to be marked complete.
     *
     * @param key ControlMessage containing the desired data type, sweep, file, directory, server and port
     * @return the requested ray (or null if it does not exist)
     */
    public Object getRayWait(final ControlMessage key, final String type, final int ray) {
        return this.selectConnection(key).getRayWait(key, type, ray);
    }

    /**
     * Gets a specific ray.  If the ray is not yet available, waits
     * for the ray to become available or the sweep to be marked complete.
     *
     * @param key     ControlMessage containing the desired data type, sweep, file, directory, server and port
     * @param timeout the maximum number of milliseconds to wait
     * @return the requested ray (or null if it does not exist)
     */
    public Object getRayWait(final ControlMessage key, final String type, final int ray, final long timeout) {
        return this.selectConnection(key).getRayWait(key, type, ray, timeout);
    }

    /**
     * Clears all enqueued requests
     *
     * @param key ControlMessage containing the server and port of the connection to clear (other fields are ignored)
     */
    public void stop(final ControlMessage key) {
        this.selectConnection(key).stop();
    }

    /**
     * Establishes a connection to a specific server and port
     *
     * @param key ControlMessage containing the server and port to connect to (other fields are ignored)
     */
    public void connect(final ControlMessage key) throws IOException, LoginException {
        synchronized (this.connections) {
            try {
                this.selectConnection(key).connect();
            } catch (IOException ioe) {
                this.connections.remove(key.getURL());
                throw ioe;
            }
            if (!isConnected(key)) {
                this.connections.remove(key.getURL());
                throw new LoginException("connect failed");
            }

            this.setChanged();
            this.notifyObservers();
            ViewFileBrowser.getInstance().getActions().refreshConnections();
        }
    }

    /**
     * Disconnects from a specific server and port
     *
     * @param key ControlMessage containing the server and port to disconnect from (other fields are ignored)
     */
    public void disconnect(final ControlMessage key) throws IOException {
        synchronized (this.connections) {
            if (this.selectConnection(key).disconnect()) {
                this.connections.remove(key.getURL());
            }
            this.setChanged();
            this.notifyObservers();
            ViewFileBrowser.getInstance().getActions().refreshConnections();
        }
    }

    /**
     * Reconnects to a specific server and port
     *
     * @param key ControlMessage containing the server and port to reconnect to (other fields are ignored)
     */
    public void reconnect(final ControlMessage key) throws IOException {
        synchronized (this.connections) {
            this.selectConnection(key).reconnect();
            this.setChanged();
            this.notifyObservers();
            ViewFileBrowser.getInstance().getActions().refreshConnections();
        }
    }

    /**
     * Checks whether a connection is currently active to a specific server and port
     *
     * @param key ControlMessage containing the server and port to disconnect from (other fields are ignored)
     * @return true if the specified connection is active; false otherwise
     */
    public boolean isConnected(final ControlMessage key) throws IOException {
        final String url = key.getURL();
        Connection conn;
        synchronized (this.connections) {
            conn = this.connections.get(url);
            return conn == null ? false : conn.isConnected();
        }
    }

    /**
     * Returns the number of rays currently cached in a particular sweep
     *
     * @param key ControlMessage containing the desired data type, sweep, file, directory, server and port
     */
    public int getNumberOfRays(final ControlMessage key, final String type) {
        return this.cache.getNumberOfRays(key, type);
    }

    /**
     * Selects the connection matching <code>key</code>.
     * Always returns a valid connection - if it did not previously exist, it is created.
     *
     * @param key ControlMessage containing the server and port of the connection to select  (other fields are ignored)
     * @return the selected connection
     */
    private Connection selectConnection(final ControlMessage key) {
        synchronized (this.connections) {
            final String url = key.getURL();
            Connection conn = this.connections.get(url);
            if (conn == null) {
            /*
			 * Username and password don't need to be prompted for if VCHILL
			 * is simply connecting to the other vchill server.
			 */
                boolean promptLogin = true;
                if (url.equals("vchill.chill.colostate.edu:2510")) {
                    if (this.connections.get("vchill.chill.colostate.edu:2513") != null)
                        promptLogin = false;
                } else if (url.equals("vchill.chill.colostate.edu:2513")) {
                    if (this.connections.get("vchill.chill.colostate.edu:2510") != null)
                        promptLogin = false;
                }
                conn = ConnectionFactory.createConnection(key, this.cache, promptLogin);
                this.connections.put(url, conn);
            }
            return conn;
        }
    }

    /**
     * Creates a connection to a realtime server
     *
     * @param url the server:port to connect to
     */
    public void createRealtimeConnection(final String url) throws IOException {
        synchronized (this.connections) {
            Connection conn = new RealtimeConnection(url);
            this.connections.put(url, conn);
            this.setChanged();
            this.notifyObservers(/* Object arg */);
            ViewFileBrowser.getInstance().getActions().refreshConnections();
        }
    }

    /**
     * Empties the shared file/socket connection cache
     */
    public void clearCache() {
        this.cache.clear();
    }
}
