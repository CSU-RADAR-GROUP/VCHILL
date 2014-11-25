package edu.colostate.vchill;

import java.util.LinkedList;

/**
 * A thread-safe FIFO queue
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-03-23
 */
public final class ControlSyncQueue<E> {
    private LinkedList<E> objects = new LinkedList<E>();
    private boolean stopping = false;

    /**
     * Enqueues an object.
     * Only one copy of each object (as determined by equals) can be enqueued at once.
     *
     * @param object the object to enqueue
     */
    public void add(final E object) {
        synchronized (this.objects) {
            if (!(this.objects.contains(object))) this.objects.add(object);
            this.stopping = false;
            this.objects.notify();
        }
    }

    /**
     * Non-destructively gets the oldest enqueued object
     *
     * @return the oldest enqueued object
     */
    public E peek() {
        synchronized (this.objects) {
            while (this.isEmpty()) {
                try {
                    this.objects.wait();
                } catch (InterruptedException e) {
                }
            }
            return this.objects.getFirst();
        }
    }

    /**
     * Destructively gets the oldest enqueued object
     *
     * @return the oldest enqueued object
     */
    public E get() {
        synchronized (this.objects) {
            while (this.isEmpty()) {
                try {
                    this.objects.wait();
                } catch (InterruptedException e) {
                }
            }
            return this.objects.remove(0);
        }
    }

    /**
     * Destroys the oldest enqueued object
     */
    public void remove() {
        synchronized (this.objects) {
            this.objects.remove(0);
        }
    }

    /**
     * Removes the specified object from the queue
     *
     * @param object the object to remove
     */
    public void remove(final Object object) {
        synchronized (this.objects) {
            this.objects.remove(object);
        }
    }

    /**
     * Tests if the queue is currenty empty
     *
     * @return true if the queue is empty, false if it contains one or more items
     */
    public boolean isEmpty() {
        synchronized (this.objects) {
            return this.objects.isEmpty();
        }
    }

    /**
     * Clears all enqueued requests
     */
    public void clear() {
        synchronized (this.objects) {
            this.objects.clear();
        }
    }

    /**
     * Clears all enqueud requests and sets the stopping flag to let Socket know to cancel current request
     */
    public void stop() {
        synchronized (this.objects) {
            this.stopping = true;
            this.clear();
        }
    }

    /**
     * Tests whether the socket is to stop or not
     *
     * @return true if the socket should abort, false otherwise
     */
    public boolean stopping() {
        synchronized (this.objects) {
            return this.stopping;
        }
    }

    /**
     * Indicates that a socket has finished stopping
     * and is now in a stopped state.
     */
    public void stopped() {
        synchronized (this.objects) {
            this.stopping = false;
        }
    }
}
