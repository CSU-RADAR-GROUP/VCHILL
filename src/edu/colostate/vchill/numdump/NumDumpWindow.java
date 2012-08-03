package edu.colostate.vchill.numdump;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import edu.colostate.vchill.data.Ray;
import edu.colostate.vchill.gui.ViewEventWindow;
import edu.colostate.vchill.gui.ViewWindow;

/**
 * A window that, instead of plotting the data, displays the raw numbers
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2007-10-31
 */
public class NumDumpWindow extends ViewWindow
{
    /**
   * 
   */
  private static final long serialVersionUID = 7103116639318118357L;
    private static final NumberFormat af = new DecimalFormat ("000.00"); //for angles
    private static final NumberFormat rf = new DecimalFormat ("000.000"); //for range
    private static final NumberFormat vf = new DecimalFormat (" 000.0000;-000.0000"); //for values
    private final ViewEventWindow scroll;

    private PrintStream out = null;

    /**
     * Constructor for the NumDumpWindow object
     *
     * @param type The datatype to display (eg Z, V, ...)
     */
    public NumDumpWindow (final String type)
    {
        super();

        this.type = type;
        this.setLayout(new BorderLayout());
        this.add(this.scroll = new ViewEventWindow(new Dimension(320, 512)), BorderLayout.CENTER);
    }

    public void plot (final Ray rayCurr, final Ray rayThresh)
    {
        final double[] dataCurr = rayCurr.getData();
        final double[] dataThresh = rayThresh == null ? null : rayThresh.getData();

        final double width = rayCurr.getGateWidth(); //in km
        final double start = rayCurr.getStartRange() * 1e-6; //mm -> km
        double range = start - width;
        final String threshType = config.getThresholdType();
        StringBuilder buff = new StringBuilder("Azimuth = ").append(af.format(rayCurr.getStartAzimuth()));
        buff.append("\u00b0\tElevation = ").append(af.format(rayCurr.getStartElevation()));
        buff.append("\u00b0\nRange (Km)\n\t").append(this.type).append("\n\t\t");
        buff.append(threshType).append("\n");
        if (this.out == null) { //output to window
            boolean lastgood = false; //was the previous gate below threshold
            for (int gate = 0; gate < dataCurr.length; ++gate) {
                boolean currgood = rayThresh != null || dataThresh[gate] > config.getThresholdFilterCutoff(); //is the current gate below threshold
                if (currgood != lastgood) { //only update gui when going above/below threshold
                    this.scroll.addEvent(buff.toString(), (lastgood = currgood));
                    buff = new StringBuilder();
                    try {Thread.sleep(50);} catch (InterruptedException ie) {}
                } else {
                    try {Thread.sleep(5);} catch (InterruptedException ie) {}
                }
                buff.append(rf.format(range += width));
                buff.append("\t").append(Double.isNaN(dataCurr[gate]) ? "N/A" : vf.format(dataCurr[gate]));
                if (rayThresh != null) buff.append("\t").append(Double.isNaN(dataThresh[gate]) ? "N/A" : vf.format(dataThresh[gate]));
                buff.append("\n");
            }
            this.scroll.addEvent(buff.append("\n").toString(), false);
        } else { //output to file
            for (int gate = 0; gate < dataCurr.length; ++gate) {
                buff.append(rf.format(range += width));
                buff.append("\t").append(Double.isNaN(dataCurr[gate]) ? "N/A" : vf.format(dataCurr[gate]));
                if (rayThresh != null) buff.append("\t").append(Double.isNaN(dataThresh[gate]) ? "N/A" : vf.format(dataThresh[gate]));
                buff.append("\n");
            }
            this.out.println(buff.toString());
            this.scroll.addEvent(".", false); //put a little something in the window so we can see it's alive
        }
    }

    public void redirectTo (final File file)
    {
        if (this.out != null) this.out.close();
        if (file != null) {
	    try {
	        this.out = new PrintStream(new FileOutputStream(file), false, "ISO-8859-1");
	    } catch (IOException ioe) {
		this.out = null; 
	    }
	} else {
	    this.out = null;
	}
        this.scroll.addEvent((this.out == null ? "Redirect off" : "redirecting to " + file.getAbsolutePath()) + "\n", false);
    }

    @Override public void addMouseListener (final MouseListener l)
    {
        this.scroll.addMouseListener(l);
        super.addMouseListener(l);
    }

    @Override public void setPlotting (final boolean plotting)
    {
        super.setPlotting(plotting);
        if (this.out != null) this.scroll.addEvent(plotting ? "saving" : "done\n", false);
    }

    @Override public String getStyle () { return "Numerical Dump"; }
}
