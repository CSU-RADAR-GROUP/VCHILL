package edu.colostate.vchill.cache;

import edu.colostate.vchill.ControlMessage;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of a cache that removes it's
 * eldest element first once it reaches a
 * maximum allowable size. It's supposed to be
 * an LRU (Least Recently Used) cache it looks
 * like but fails to implement this functionality.
 * This implementation is fully synchronized -
 * external synchronization is not necessary.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class CacheMainLRU extends CacheMain
{
    private final Map<ControlMessage, CacheType<Object>> cache;

    /**
     * @param maxTypesToCache the maximum number of sweeps/types to store at once
     */
    public CacheMainLRU (final int maxTypesToCache)
    {
        this.cache = new LinkedHashMap<ControlMessage, CacheType<Object>>(maxTypesToCache)
        {
            /**
           * 
           */
          private static final long serialVersionUID = 7608231869146777712L;

            @Override protected boolean removeEldestEntry (Map.Entry<ControlMessage, CacheType<Object>> eldest)
            {
                return this.size() > maxTypesToCache;
            }
        };
    }

    /**
     * Retrieves data from the cache
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
     * @param ray the 0-based index of the desired data within the sweep/type
     * @return the requested data 
     */
    @Override public Object getData (final ControlMessage key, final String type, final int ray)
    {
        return selectType(key).getData(type, ray);
    }

    /**
     * Retrieves data from the cache.  If the data is not yet available, waits
     * for the data to become available or the sweep to be marked complete.
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
     * @param ray the 0-based index of the desired data within the sweep/type
     * @return the requested data 
     */
    @Override public Object getDataWait (final ControlMessage key, final String type, final int ray)
    {
        return selectType(key).getDataWait(type, ray);
    }

    /**
     * Retrieves data from the cache.  If the data is not yet available, waits
     * for the data to become available or the sweep to be marked complete.
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
     * @param ray the 0-based index of the desired data within the sweep/type
     * @param timeout the maximum number of milliseconds to wait 
     * @return the requested data 
     */
    @Override public Object getDataWait (final ControlMessage key, final String type, final int ray, final long timeout)
    {
        return selectType(key).getDataWait(type, ray, timeout);
    }

    /**
     * Appends data to the cache
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
	 * @param type the field name to deal with
     * @param data the data to store
     */
    @Override public void addRay (final ControlMessage key, final String type, final Object data)
    {
        if (!getCompleteFlag(key, type)) selectType(key).addRay(type, data);
    }

    /**
     * Retrieves the CacheType matching <i>key</i> from the cache.
     *
     * If the desired CacheType does not exist, it is created and added to the cache.
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
     * @return the requested CacheType 
     */
    private CacheType<Object> selectType (final ControlMessage key)
    { synchronized (this.cache) {
        CacheType<Object> type = this.cache.get(key);
        if (type == null) this.cache.put(key, (type = new CacheType<Object>()));
        return type;
    }}

    /**
     * Removes a sweep/type from the cache.  This is useful for removing partially loaded / aborted data.
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
	 * @param type the field name to deal with
     */
    @Override public void removeType (final ControlMessage key, final String type)
    { synchronized (this.cache) {
        this.cache.remove(key);
    }}

    /**
     * Gets the number of cached rays
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
	 * @param type the field name to deal with
     * @return the number of available rays of the specified sweep/type
     */
    @Override public int getNumberOfRays (final ControlMessage key, final String type)
    {
        return selectType(key).getNumberOfRays(type);
    }

    /**
     * Gets the list of all currently active connections.
     * Each is identified by a string of the format servername:port
     *
     * @return a Collection of String objects containing available servers and ports
     */
    @Override public Collection<String> getConnectionList ()
    { synchronized (this.cache) {
        Set<String> urls = new HashSet<String>();
        for (ControlMessage msg : this.cache.keySet()) urls.add(msg.getURL());
        return urls;
    }}

    /**
     * Gets a list of available directories, given a server and port number
     *
     * @param key ControlMessage containing server and port (other fields are ignored)
     * @return a Collection of String objects in the format other Cache methods want
     */
    public Collection<String> getDirectoryList (final ControlMessage key)
    { synchronized (this.cache) {
        Set<String> dirs = new HashSet<String>();
        for (ControlMessage msg : this.cache.keySet()) {
            if (key.getURL().equals(msg.getURL())) dirs.add(msg.getDir());
        }
        dirs.remove(null); //just in case
        return dirs;
    }}

    /**
     * Gets a list of available files in a given directory
     *
     * @param key ControlMessage containing the directory, server, and port to list (other fields are ignored)
     * @return a Collection of String objects in the format other Cache methods want
     */
    public Collection<String> getFileList (final ControlMessage key)
    { synchronized (this.cache) {
        Set<String> files = new HashSet<String>();
        for (ControlMessage msg : this.cache.keySet()) {
            if (key.getURL().equals(msg.getURL()) && key.getDir().equals(msg.getDir())) files.add(msg.getFile());
        }
        return files;
    }}

    /**
     * Gets a list of available sweeps in a given file
     *
     * @param key ControlMessage containing the file, directory, server, and port to list (other fields are ignored)
     * @return a Collection of String objects in the format other Cache methods want
     */
    @Override public Collection<String> getSweepList (final ControlMessage key)
    { synchronized (this.cache) {
        Set<String> sweeps = new HashSet<String>();
        for (ControlMessage msg : this.cache.keySet()) {
            if (key.getURL().equals(msg.getURL()) && key.getDir().equals(msg.getDir()) && key.getFile().equals(msg.getFile())) sweeps.add(msg.getSweep());
        }
        return sweeps;
    }}

    /**
     * Marks a sweep/type as complete
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the sweep/type to mark complete
	 * @param type the field name to deal with
     */
    @Override public void setCompleteFlag (final ControlMessage key, final String type)
    {
        selectType(key).setCompleteFlag(type);
    }

    /**
     * Checks whether a sweep/type is complete
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the sweep/type to check
	 * @param type the field name to deal with
     * @return is the sweep/type complete?
     */
    @Override public boolean getCompleteFlag (final ControlMessage key, final String type)
    {
        return selectType(key).getCompleteFlag(type);
    }

    /**
     * Checks whether a sweep/type is empty
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the sweep/type to check
	 * @param type the field name to deal with
     * @return is the sweep/type empty?
     */
    @Override public boolean isEmpty (final ControlMessage key, final String type)
    {
        return selectType(key).isEmpty(type);
    }

    /**
     * Removes all entries from the cache
     */
    @Override public void clear ()
    { synchronized (this.cache) {
        this.cache.clear();
    }}
}
