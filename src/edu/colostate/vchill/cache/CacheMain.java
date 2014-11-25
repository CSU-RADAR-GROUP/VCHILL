package edu.colostate.vchill.cache;

import edu.colostate.vchill.ControlMessage;

import java.util.Collection;

/**
 * Generic cache definition.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public abstract class CacheMain {
    /**
     * Retrieves data from the cache
     *
     * @param key   ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
     * @param index the 0-based index of the desired data within the sweep/type
     * @return the requested data
     */
    public abstract Object getData(final ControlMessage key, final String type, final int index);

    /**
     * Retrieves data from the cache.  If the data is not yet available, waits
     * for the data to become available or the sweep to be marked complete.
     *
     * @param key   ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
     * @param index the 0-based index of the desired data within the sweep/type
     * @return the requested data
     */
    public abstract Object getDataWait(final ControlMessage key, final String type, final int index);

    /**
     * Retrieves data from the cache.  If the data is not yet available, waits
     * for the data to become available or the sweep to be marked complete.
     *
     * @param key     ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
     * @param index   the 0-based index of the desired data within the sweep/type
     * @param timeout the maximum number of milliseconds to wait
     * @return the requested data
     */
    public abstract Object getDataWait(final ControlMessage key, final String type, final int index, final long timeout);

    /**
     * Appends data to the cache
     *
     * @param key  ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
     * @param data the data store
     */
    public abstract void addRay(final ControlMessage key, final String type, final Object data);

    /**
     * Removes a sweep/type from the cache.  This is useful for removing partially loaded / aborted data.
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
     */
    public synchronized void removeType(final ControlMessage key, final String type) {
    }

    /**
     * Gets the number of cached rays
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
     * @return the number of available rays of the specified sweep/type
     */
    public int getNumberOfRays(final ControlMessage key, final String type) {
        return 0;
    }

    /**
     * Gets the list of all currently active connections.
     * Each is identified by a string of the format servername:port
     *
     * @return a Collection of String objects containing available servers and ports
     */
    public Collection<String> getConnectionList() {
        return null;
    }

    /**
     * Gets a list of available subdirectories and files in a given directory
     *
     * @param key ControlMessage containing the directory, server, and port to list (other fields are ignored)
     * @return a Collection of String objects in the format other Cache methods want
     */
    public Collection<String> getDirectory(final ControlMessage key) {
        return null;
    }

    /**
     * Gets a list of available sweeps in a given file
     *
     * @param key ControlMessage containing the file, directory, server, and port to list (other fields are ignored)
     * @return a Collection of String objects in the format other Cache methods want
     */
    public Collection<String> getSweepList(final ControlMessage key) {
        return null;
    }

    /**
     * Marks a sweep/type as complete
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the sweep/type to mark complete
     */
    public void setCompleteFlag(final ControlMessage key, final String type) {
    }

    /**
     * Checks whether a sweep/type is complete
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the sweep/type to check
     * @return is the sweep/type complete?
     */
    public boolean getCompleteFlag(final ControlMessage key, final String type) {
        return false;
    }

    /**
     * Checks whether a sweep/type is empty.
     * Used to determine whether to add a request to the queue.
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the sweep/type to check
     * @return is the sweep/type empty?
     */
    public boolean isEmpty(final ControlMessage key, final String type) {
        return false;
    }

    /**
     * Removes all content from the cache if applicable
     */
    public void clear() {
    }
}
