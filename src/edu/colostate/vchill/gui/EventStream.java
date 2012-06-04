package edu.colostate.vchill.gui;

import java.io.PrintStream;

/**
 * Stream for copying a stream to a ViewEventWindow.  The original stream
 * is unaffected, but the ViewEventWindow gets everything as well.
 *
 * @author  Alexander Deyke
 * @author  Jochen Deyke
 * @version 2004-10-11
 */
public class EventStream extends PrintStream
{
    private ViewEventWindow window;
    boolean isErr;

    /**
     * Sole EventStream Constructor.
     *
     * @param stream Stream to pipe from
     * @param window ViewEventWindow to copy to
     * @param isErr is this Stream intended for displaying error messages? (only used for formatting purposes in <code>window</code>)
     */
    public EventStream (final PrintStream stream, final ViewEventWindow window, final boolean isErr)
    {
        super(stream);
        this.window = window;
        this.isErr = isErr;
    }

    public synchronized void print (final boolean b)
    {
        super.print(b);
        window.addEvent(b ? "true" : "false", isErr);
    }

    public synchronized void print (final char c)
    {
        super.print(c);
        window.addEvent(String.valueOf(c), isErr);
    }

    public synchronized void print (final int i)
    {
        super.print(i);
        window.addEvent(String.valueOf(i), isErr);
    }

    public synchronized void print (final long l)
    {
        super.print(l);
        window.addEvent(String.valueOf(l), isErr);
    }

    public synchronized void print (final float f)
    {
        super.print(f);
        window.addEvent(String.valueOf(f), isErr);
    }

    public synchronized void print (final double d)
    {
        super.print(d);
        window.addEvent(String.valueOf(d), isErr);
    }

    public synchronized void print (final char s[])
    {
        super.print(s);
        window.addEvent(new String(s), isErr);
    }

    public synchronized void print (final String s)
    {
        super.print(s);
        window.addEvent(s == null ? "null" : s, isErr);
    }

    public synchronized void print (final Object obj)
    {
        super.print(obj);
        window.addEvent(String.valueOf(obj), isErr);
    }

    public synchronized void println ()
    {
        super.println();
        window.addEvent("\n", isErr);
    }

    public synchronized void println (final boolean x)
    {
        super.println(x);
        window.addEvent("\n", isErr);
    }

    public synchronized void println (final char x)
    {
        super.println(x);
        window.addEvent("\n", isErr);
    }

    public synchronized void println (final int x)
    {
        super.println(x);
        window.addEvent("\n", isErr);
    }

    public synchronized void println (final long x)
    {
        super.println(x);
        window.addEvent("\n", isErr);
    }

    public synchronized void println (final float x)
    {
        super.println(x);
        window.addEvent("\n", isErr);
    }

    public synchronized void println (final double x)
    {
        super.println(x);
        window.addEvent("\n", isErr);
    }

    public synchronized void println (final char x[])
    {
        super.println(x);
        window.addEvent("\n", isErr);
    }

    public synchronized void println (final String x)
    {
        super.println(x);
        window.addEvent("\n", isErr);
    }

    public synchronized void println (final Object x)
    {
        super.println(x);
        window.addEvent("\n", isErr);
    }
}
