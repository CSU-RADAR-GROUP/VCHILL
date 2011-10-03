package edu.colostate.vchill;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JProgressBar;

/**
 * A <code>BufferedInputStream</code> that allows for easy monitoring of read progress with a <code>JProgressBar</code>.
 * @author Jochen Deyke
 * @version 2007-05-24
 */
public class ProgressMonitorInputStream extends BufferedInputStream
{
    protected JProgressBar progress;
    
    public ProgressMonitorInputStream (final InputStream in, final JProgressBar progress)
    {
        super(in);
        this.progress = progress;
    }

    public int read () throws IOException
    {
        int b = super.read();
        if (this.progress != null) progress.setValue(progress.getValue() + 1);
        return b;
    }

    public int read (final byte[] b) throws IOException
    {
        int numRead = super.read(b);
        if (this.progress != null) progress.setValue(progress.getValue() + numRead);
        return numRead;
    }

    public int read (final byte[] b, final int off, final int len) throws IOException
    {
        int numRead = super.read(b, off, len);
        if (this.progress != null) progress.setValue(progress.getValue() + numRead);
        return numRead;
    }

    public void reset () throws IOException
    {
        int before = in.available();
        super.reset();
        int after = in.available();
        if (this.progress != null) progress.setValue(progress.getValue() + before - after);
    }

    public long skip (final long n) throws IOException
    {
        long skipped = super.skip(n);
        if (this.progress != null) progress.setValue(progress.getValue() + (int)skipped);
        return skipped;
    }
}
