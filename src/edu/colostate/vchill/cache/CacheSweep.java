package edu.colostate.vchill.cache;

import java.util.ArrayList;

/**
 * Storage class for VCHILL's Cache module.
 * Instances of this class are what is stored in the CacheMap.
 *
 * @author Jochen Deyke
 * @version 2007-09-28
 */
class CacheSweep<E>
{
    private final ArrayList<E> rays;
    private volatile boolean complete;

    /**
     * Default constructor
     */
    public CacheSweep ()
    {
        this.rays = new ArrayList<E>(500); //initial size
        this.complete = false;
    }

    /**
     * Gets a ray out of the cache
     *
     * @param ray index of the desired ray
     * @return the ray if it is in the cache, null otherwise
     */
    public E getData (final int ray)
    { synchronized (this.rays) {
        return ray < this.rays.size() ? this.rays.get(ray) : null;
    }}

    /**
     * Gets a ray out of the cache.  If the ray is not yet available, waits
     * for the ray to become available or the sweep to be marked complete.
     *
     * @param ray index of the desired ray
     * @return the ray if it is in the cache, null otherwise
     */
    public E getDataWait (final int ray)
    { synchronized (this.rays) {
        while (!this.complete && ray >= this.rays.size())
            try { this.rays.wait(); } catch (InterruptedException ie) {}
        return ray < this.rays.size() ? this.rays.get(ray) : null;
    }}

    /**
     * Gets a ray out of the cache.  If the ray is not yet available, waits
     * for the ray to become available or the sweep to be marked complete.
     *
     * @param ray index of the desired ray
     * @param timeout the maximum number of milliseconds to wait
     * @return the ray if it is in the cache, null otherwise
     */
    public E getDataWait (final int ray, final long timeout)
    { synchronized (this.rays) {
        if (!this.complete && ray >= this.rays.size())
            try { this.rays.wait(timeout); } catch (InterruptedException ie) {}
        return ray < this.rays.size() ? this.rays.get(ray) : null;
    }}

    /**
     * Adds a ray to the cache
     *
     * @param data the ray to add
     */
    public void addRay (final E data)
    { synchronized (this.rays) {
        this.rays.add(data);
        this.rays.notifyAll();
    }}

    /**
     * Checks how many rays are chached
     *
     * @return the number of rays currently stored of this type.
     * This value may change as rays are added.
     */
    public int getNumberOfRays ()
    { synchronized (this.rays) {
        return this.rays.size();
    }}

    /**
     * Marks a Type as complete.
     *
     * This allows extra memory to be freed,
     * and getRay to be sure that no more rays will be added.
     */
    public void setCompleteFlag ()
    { synchronized (this.rays) {
        this.complete = true;
        this.trim(); //free extra memory
        this.rays.notifyAll();
    }}

    /**
     * Checks if a type completely cached
     *
     * @return <code>true</code> if type has been marked complete,
     * <code>false</code> otherwise
     */
    public boolean getCompleteFlag ()
    { synchronized (this.rays) {
        return this.complete;
    }}

    /**
     * Checks if part of the type is already cached
     *
     * @return <code>true</code> if there are no rays in this type and it has been marked complete,
     * <code>false</code> otherwise
     */
    public boolean isEmpty ()
    { synchronized (this.rays) {
        if (this.complete) return false;
        return this.rays.isEmpty();
    }}

    /**
     * Frees excess memory by removing null entries from the cache
     */
    public void trim ()
    { synchronized (this.rays) {
        this.rays.remove(null);
        this.rays.trimToSize();
    }}
}
