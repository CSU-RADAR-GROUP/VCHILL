package edu.colostate.vchill.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds a sweep of data for any/all available types.
 * This implementation is fully synchronized -
 * external synchronization is not necessary.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class CacheType<E>
{
    private final Map<String, CacheSweep<E>> cache;

    /**
     * Creates a new instance of CacheType.
     */
    public CacheType ()
    {
        this.cache = new LinkedHashMap<String, CacheSweep<E>>();
    }

    /**
     * Retrieves data from the cache
     *
     * @param type the field name to deal with
     * @param ray the 0-based index of the desired data within the sweep/type
     * @return the requested data 
     */
    public Object getData (final String type, final int ray)
    {
        return selectSweep(type).getData(ray);
    }

    /**
     * Retrieves data from the cache.  If the data is not yet available, waits
     * for the data to become available or the sweep to be marked complete.
     *
     * @param type the field name to deal with
     * @param ray the 0-based index of the desired data within the sweep/type
     * @return the requested data 
     */
    public Object getDataWait (final String type, final int ray)
    {
        return selectSweep(type).getDataWait(ray);
    }

    /**
     * Retrieves data from the cache.  If the data is not yet available, waits
     * for the data to become available or the sweep to be marked complete.
     *
     * @param type the field name to deal with
     * @param ray the 0-based index of the desired data within the sweep/type
     * @param timeout the maximum number of milliseconds to wait 
     * @return the requested data 
     */
    public Object getDataWait (final String type, final int ray, final long timeout)
    {
        return selectSweep(type).getDataWait(ray, timeout);
    }

    /**
     * Appends data to the cache
     *
     * @param type the field name to deal with
     * @param data the data to store
     */
    public void addRay (final String type, final E data)
    {
        if (!getCompleteFlag(type)) selectSweep(type).addRay(data);
    }

    /**
     * Retrieves the CacheType matching <i>key</i> from the cache.
     *
     * If the desired CacheType does not exist, it is created and added to the cache.
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
     * @return the requested CacheType 
     */
    private CacheSweep<E> selectSweep (final String type)
    { synchronized (this.cache) {
        CacheSweep<E> sweep = this.cache.get(type);
        if (sweep == null) this.cache.put(type, (sweep = new CacheSweep<E>()));
        return sweep;
    }}

    /**
     * Removes a sweep/type from the cache.  This is useful for removing partially loaded / aborted data.
     *
     * @param type the field name to deal with
     */
    public void removeType (final String type)
    { synchronized (this.cache) {
        this.cache.remove(type);
    }}

    /**
     * Gets the number of cached rays
     *
     * @param type the field name to deal with
     * @return the number of available rays of the specified sweep/type
     */
    public int getNumberOfRays (final String type)
    {
        return selectSweep(type).getNumberOfRays();
    }

    /**
     * Marks a sweep/type as complete
     *
     * @param type the field name to deal with
     */
    public void setCompleteFlag (final String type)
    {
        selectSweep(type).setCompleteFlag();
    }

    /**
     * Checks whether a sweep/type is complete
     *
     * @param type the field name to deal with
     * @return is the sweep/type complete?
     */
    public boolean getCompleteFlag (final String type)
    {
        return selectSweep(type).getCompleteFlag();
    }

    /**
     * Checks whether a sweep/type is empty
     *
     * @param type the field name to deal with
     * @return is the sweep/type empty?
     */
    public boolean isEmpty (final String type)
    {
        return selectSweep(type).isEmpty();
    }
}
