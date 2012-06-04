package edu.colostate.vchill.cache;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.ScaleManager;
import java.util.HashMap;

/**
 * Implementation of a cyclic buffer.
 * This type of cache should NOT be shared between connections.
 *
 * @author Jochen Deyke
 * @version 2007-09-28
 */
public class CacheMainCyclic extends CacheMain
{
    protected ScaleManager sm = ScaleManager.getInstance();

    /** The number of elements to store */
    protected final int buffSize;

    /** The actual storage */
    protected final HashMap<String, Object>[] buffer;

    /** Insertion point for new elements */
    protected int[] index;

    @SuppressWarnings("unchecked") public CacheMainCyclic (final int size)
    {
        this.buffSize = size;
        this.buffer = new HashMap[size];
        for (int i = 0; i < size; ++i) this.buffer[i] = new HashMap<String, Object>();
        this.index = new int[ChillDefines.MAX_NUM_TYPES + 1]; //plus metadata
    }

    /**
     * Retrieves data from the cache
     *
     * @param key ControlMessage containing type information of the desired data
     * @param index the 0-based index of the desired data within the sweep/type
     * @return the requested data 
     */
    @Override public Object getData (final ControlMessage key, final String type, final int index)
    { synchronized (this.buffer) {
        return this.buffer[index % buffSize].get(type);
    }}

    /**
     * Retrieves data from the cache.  If the data is not yet available, waits
     * for the data to become available or the sweep to be marked complete.
     *
     * @param key ControlMessage containing type information of the desired data
     * @param index the 0-based index of the desired data within the sweep/type
     * @return the requested data 
     */
    @Override public Object getDataWait (final ControlMessage key, final String type, final int index)
    { synchronized (this.buffer) {
        while (index > this.index[sm.getScale(type).fieldNumber])
            try { this.buffer.wait(); } catch (InterruptedException ie) {}
        return this.buffer[index % buffSize].get(type);
    }}

    /**
     * Retrieves data from the cache.  If the data is not yet available, waits
     * for the data to become available or the sweep to be marked complete.
     *
     * @param key ControlMessage containing type information of the desired data
     * @param index the 0-based index of the desired data within the sweep/type
     * @param timeout the maximum number of milliseconds to wait 
     * @return the requested data 
     */
    @Override public Object getDataWait (final ControlMessage key, final String type, final int index, final long timeout)
    { synchronized (this.buffer) {
        if (index > this.index[sm.getScale(type).fieldNumber])
            try { this.buffer.wait(timeout); } catch (InterruptedException ie) {}
        return this.buffer[index % buffSize].get(type);
    }}

    /**
     * Appends data to the cache
     *
     * @param key ControlMessage containing server, port, dir, file, sweep, and type information of the desired data
     * @param data the data store
     */
    @Override public void addRay (final ControlMessage key, final String type, final Object data)
    { synchronized (this.buffer) {
        int fieldNum = type.equals(ChillDefines.META_TYPE) ? ChillDefines.MAX_NUM_TYPES : sm.getScale(type).fieldNumber;
        this.buffer[this.index[fieldNum] % buffSize].put(type, data);
        ++this.index[fieldNum];
        this.buffer.notifyAll();
    }}

    @Override public int getNumberOfRays (final ControlMessage key, final String type)
    { synchronized (this.buffer) {
        return this.index[sm.getScale(type).fieldNumber];
    }}
}
