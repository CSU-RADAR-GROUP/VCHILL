package edu.colostate.vchill;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A <code>BufferedOutputStream</code> that allows for easy monitoring of write progress with a <code>JProgressBar</code>.
 *
 * @author Jochen Deyke
 * @version 2007-05-24
 */
public class ProgressMonitorOutputStream extends BufferedOutputStream {
    protected JProgressBar progress;

    public ProgressMonitorOutputStream(final OutputStream out, final JProgressBar progress) {
        super(out);
        this.progress = progress;
    }

    public void write(final byte[] b) throws IOException {
        super.write(b);
        if (this.progress != null) progress.setValue(progress.getValue() + b.length);
    }

    public void write(final byte[] b, final int off, final int len) throws IOException {
        super.write(b, off, len);
        if (this.progress != null) progress.setValue(progress.getValue() + len);
    }

    public void write(final int b) throws IOException {
        super.write(b);
        if (this.progress != null) progress.setValue(progress.getValue() + 1);
    }
}
