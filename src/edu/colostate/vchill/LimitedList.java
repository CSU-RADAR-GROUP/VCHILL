package edu.colostate.vchill;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A size-limited synchronized linked list wrapper
 *
 * @author Jochen Deyke
 * @version 2004-12-09
 */
public final class LimitedList<E> implements Iterable<E> {
    /**
     * the maximum number of elements allowed
     */
    private int limit;

    /**
     * actual list
     */
    private LinkedList<E> objects;

    public LimitedList(final int size) {
        this.limit = size;
        this.objects = new LinkedList<E>();
    }

    /**
     * Empties the list of elements.
     */
    public synchronized void clear() {
        this.objects.clear();
    }

    /**
     * Sets the size limit on the list.  At most <code>size</code>
     * objects can be stored in the list; any excess objects are
     * discarded (oldest first).
     *
     * @param size the maximum number of objects to store
     */
    public synchronized void setLimit(final int size) {
        this.limit = size;
    }

    /**
     * Adds an object to the list.  If the number of objects would
     * exceed the limit, excess objects are discarded (oldest first).
     *
     * @param object the object to add
     */
    public synchronized void add(final E object) {
        while (this.objects.size() >= this.limit) this.objects.removeFirst();
        this.objects.addLast(object);
    }

    /**
     * Returns an iterator over a copy of this list.  This copy
     * references the same objects as this list.
     *
     * @return an Iterator over a list containing the same elements as this list
     */
    public synchronized Iterator<E> iterator() {
        LinkedList<E> copy = new LinkedList<E>();
        for (E obj : objects) copy.add(obj);
        return copy.iterator();
    }
}
