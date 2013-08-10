package edu.colostate.vchill.ascope;

import edu.colostate.vchill.Loader;
import edu.colostate.vchill.ViewUtil;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.data.Ray;
import edu.colostate.vchill.gui.ViewWindow;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;

/**
 * A window to plot data AScope style.
 *
 * Methods are synchronized on this.formatter to prevent conflict with
 * synchronized JPanel methods 
 *
 * @author  Jochen Deyke
 * @author  jpont
 * @version 2009-06-26
 */
public class ViewAScopeWindow extends ViewWindow
{
    /**
   * 
   */
  private static final long serialVersionUID = 420048517751841846L;

    public static final String TYPE_NOT_SET = "<none>";

    private static final Config aconf = Config.getInstance();

    private static final Color bg = Color.BLACK;
    private static final Color fg = Color.WHITE;
    private static final Color color1 = Color.GREEN;
    private static final Color color2 = Color.RED;

    private final SimpleDateFormat formatter = new SimpleDateFormat("EEE d MMM yy");

    /** Used to draw data onto */
    private BufferedImage dataBuffer;

    /** Z, V, etc */
    private String type2 = null;

    /** Data of last ray plotted */
    private Ray ray1;
    private Ray ray2;
    private double[] data1;
    private double[] data2;

    /** {date, UTC} */
    private String[] time = {"Date", "Time"};

    private double azimuth;
    private double elevation;
    private int numGates;
    private double gateWidth;

    /**
     * Constructor for the ViewPlotWindow object
     *
     * @param type The datatype to display (eg Z, V, ...)
     */
    public ViewAScopeWindow (final String type)
    {
        super();

        this.type = type;
        this.type2 = null; //set by WindowManager

        this.formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        //Set the layout to add components to the left as they are added.
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.dataBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        //Swing setup.
        setBackground(bg);
        setForeground(bg);
        setDoubleBuffered(false);

        this.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized (final ComponentEvent ce) {
                wm.resizeAScopes(parent.getWidth(), parent.getHeight());
            }});
    }

    @Override public void paintComponent (final Graphics g)
    {
        if (g == null) return;
        super.paintComponent(g);
        g.drawImage(this.dataBuffer, 0, 0, this);
        if (aconf.isDrawClickRangeEnabled()) {
            int clickX = (int)(aconf.getClickRange() * this.getPlotWidth() / config.getPlotRange());
            if (clickX > this.getPlotWidth()) return; //don't draw over annotation
            g.setColor(fg);
            g.drawLine(clickX, 0, clickX, this.getHeight());
            int clickY = this.getPlotHeight() - (int)(this.getPlotHeight() * aconf.getClickY());
            g.drawLine(0, clickY, this.getPlotWidth(), clickY);
        }
    }

    /**
     * @param ray1 the primary Ray to plot
     * @param ray2 the secondary Ray to plot, or
     * null if secondary plot is disabled
     */
    public void plot (final Ray ray1, final Ray ray2)
    { synchronized (this.formatter) {
        if (ray1 == null) return; //no data to plot;
        boolean secondaryEnabled = ray2 != null;
        if (secondaryEnabled) {
            this.ray2 = ray2;
            this.data2 = this.ray2.getData();
        }

        this.ray1 = ray1;
        this.data1 = this.ray1.getData();
        
        Date date = new Date(this.ray1.getDate());
        this.formatter.applyPattern("EEE d MMM yy");
        this.time[0] = this.formatter.format(date);
        this.formatter.applyPattern("HH:mm:ss 'UTC'");
        this.time[1] = this.formatter.format(date);

        this.azimuth = this.ray1.getStartAzimuth();
        this.elevation = this.ray1.getStartElevation();
        this.numGates = this.ray1.getNumberOfGates();
        this.gateWidth = this.ray1.getGateWidth();

        this.plotData(secondaryEnabled);
    }}

    public void plotData (final boolean secondaryEnabled)
    { synchronized (this.formatter) {
        ChillMomentFieldScale scale1 = sm.getScale(this.type);
        ChillMomentFieldScale scale2 = sm.getScale(this.type2);

        Graphics2D g = this.dataBuffer.createGraphics();
        double stepSize;
        int x1, x2, y1, y2;
		double x1Data, x2Data;
        int offset;

        this.clearScreen();
        if (secondaryEnabled) {
            stepSize = (config.getPlotRange() * this.data2.length) /
                (this.getPlotWidth() * this.gateWidth * this.numGates);
            offset = (int)((this.ray2.getStartRange() * 1e-6) / config.getPlotRange() * this.getPlotWidth()); //mm -> km
            x2 = 0;
            g.setColor(color2);
            for (x1 = x2++; x2 < this.getPlotWidth(); x1 = x2++) {
                try {
					x1Data = this.data2[(int)(x1 * stepSize)];
                    y1 = this.getPlotHeight() -
                        (int)(this.getPlotHeight() *
                              (x1Data -
                               scale2.getMin()) /
                              (scale2.getMax() - scale2.getMin()));
					x2Data = this.data2[(int)(x2 * stepSize)];
                    y2 = this.getPlotHeight() -
                        (int)(this.getPlotHeight() *
                              (x2Data -
                               scale2.getMin()) /
                              (scale2.getMax() - scale2.getMin()));
					
					if( Double.isNaN(x1Data) )
					{
						if( !Double.isNaN(x2Data) )
						{
							g.drawLine(x2 + offset, y2, x2 + offset, y2);
						}
					}
					else if( Double.isNaN(x2Data) )
					{
						if( !Double.isNaN(x1Data) )
						{
							g.drawLine(x1 + offset, y1, x1 + offset, y1);
						}
					}
					else
						g.drawLine(x1 + offset, y1, x2 + offset, y2);
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    break; //no more data to plot
                }
            }
        }

        stepSize = (config.getPlotRange() * this.data1.length) /
            (this.getPlotWidth() * this.gateWidth * this.numGates);
        offset = (int)((this.ray1.getStartRange() * 1e-6) / config.getPlotRange() * this.getPlotWidth()); //mm -> km
        x2 = 0;
        g.setColor(color1);
        for (x1 = x2++; x2 < this.getPlotWidth(); x1 = x2++) {
            try {
				x1Data = this.data1[(int)(x1 * stepSize)];
                y1 = this.getPlotHeight() - (int)(this.getPlotHeight() *
                        (x1Data -
                         scale1.getMin()) /
                        (scale1.getMax() - scale1.getMin()));
				x2Data = this.data1[(int)(x2 * stepSize)];
                y2 = this.getPlotHeight() - (int)(this.getPlotHeight() *
                        (x2Data -
                         scale1.getMin()) /
                        (scale1.getMax() - scale1.getMin()));
				
                if( Double.isNaN(x1Data) )
				{
					if( !Double.isNaN(x2Data) )
					{
						g.drawLine(x2 + offset, y2, x2 + offset, y2);
					}
				}
				else if( Double.isNaN(x2Data) )
				{
					if( !Double.isNaN(x1Data) )
					{
						g.drawLine(x1 + offset, y1, x1 + offset, y1);
					}
				}
				else
					g.drawLine(x1 + offset, y1, x2 + offset, y2);
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                break; //no more data to plot
            }
        }

        this.plotAnnotation();
    }}

    public void plotAnnotation ()
    { synchronized (this.formatter) {
        ChillMomentFieldScale scale1 = sm.getScale(this.type);
        ChillMomentFieldScale scale2 = sm.getScale(this.type2);

        Graphics2D g = this.dataBuffer.createGraphics();
        g.setColor(color1);
        int x = this.getPlotWidth() + 5;
        int y;
        int incr = 12 * this.getPlotHeight() / 240;
        //primary scale
        g.drawString(ViewUtil.format(scale1.getMax()), x, 12);
        g.drawString(ViewUtil.format((scale1.getMin() + 3 * scale1.getMax()) / 4.0), x, 12 + (this.getPlotHeight() - 14) / 4);
        g.drawString(ViewUtil.format((scale1.getMin() + scale1.getMax()) / 2.0), x, 12 + (this.getPlotHeight() - 14) / 2);
        g.drawString(ViewUtil.format((3 * scale1.getMin() + scale1.getMax()) / 4.0), x, 12 + (this.getPlotHeight() - 14) * 3 / 4);
        g.drawString(ViewUtil.format(scale1.getMin()), x, 12 + this.getPlotHeight() - 14);
        y = this.getPlotHeight() -
            (int)(this.getPlotHeight() * scale1.getMin() /
                (scale1.getMin() - scale1.getMax()));
        g.drawLine(0, y, this.getPlotWidth(), y);
        //secondary scale
        if (this.type2 != null) {
            g.setColor(color2);
            x += 40;
            g.drawString(ViewUtil.format(scale2.getMax()), x, 12);
            g.drawString(ViewUtil.format((scale2.getMin() + 3 * scale2.getMax()) / 4.0), x, 12 + (this.getPlotHeight() - 14) / 4);
            g.drawString(ViewUtil.format((scale2.getMin() + scale2.getMax()) / 2.0), x, 12 + (this.getPlotHeight() - 14) / 2);
            g.drawString(ViewUtil.format((3 * scale2.getMin() + scale2.getMax()) / 4.0), x, 12 + (this.getPlotHeight() - 14) * 3 / 4);
            g.drawString(ViewUtil.format(scale2.getMin()), x, 12 + this.getPlotHeight() - 14);
            y = this.getPlotHeight() -
                (int)(this.getPlotHeight() * scale2.getMin() /
                      (scale2.getMin() - scale2.getMax()));
            g.drawLine(0, y, this.getPlotWidth(), y);
        }
        
        g.setColor(fg);
        x = 2;
        y = this.getHeight() - 2;
        //range scale
        if (this.ray1 != null) {
            g.drawString(ViewUtil.format(0), x, y);
            g.drawString(ViewUtil.format(config.getPlotRange() / 2.0), this.getPlotWidth() / 2, y);
            g.drawString(ViewUtil.format(config.getPlotRange()), this.getPlotWidth(), y);
        }
        //sidebar
        x = this.getPlotWidth() + 10;
        y = incr * 5 / 2;
        g.drawString("Type:", x, y += incr);
        g.setColor(color1);
        g.drawString(this.type, x + 40, y);
        if (this.type2 != null) {
            g.setColor(color2);
            g.drawString(this.type2, x + 80, y);
        }
        y += incr * 3; //leave room for scale
        g.setColor(fg);
        g.drawString(this.time[0], x, y += incr); //date
        g.drawString(this.time[1], x, y += incr); //utc
        y += incr * 3; //leave room for scale
        g.drawString("Azimuth: " + ViewUtil.format(this.azimuth) + "\u00b0", x, y += incr);
        g.drawString("Elevation: " + ViewUtil.format(this.elevation) + "\u00b0", x, y += incr);
        g.drawString("Range: " + ViewUtil.format(aconf.getClickRange()) + "km", x, y += incr);
        y += incr * 5 / 2; //leave room for scale
        g.drawString("Value: ", x, y += incr);
        if (this.ray1 != null) {
            g.setColor(color1);
            int index = (int)((aconf.getClickRange() - this.ray1.getStartRange() * 1e-6) / this.gateWidth);
            if (index > this.numGates - 1) index = this.numGates - 1;
            if (index < 0) index = 0;
            g.drawString(Double.isNaN(this.data1[index]) ? "N/A" : ViewUtil.format(this.data1[index]), x + 40, y);
            if (this.ray2 != null && this.type2 != null) {
                g.setColor(color2);
                g.drawString(Double.isNaN(this.data2[index]) ? "N/A" : ViewUtil.format(this.data2[index]), x + 80, y);
            }
        }
        g.setColor(fg);
        g.drawString("Marker: ", x, y += incr);
        g.setColor(color1);
        g.drawString(ViewUtil.format(aconf.getClickY() * (scale1.getMax() - scale1.getMin()) + scale1.getMin()), x + 40, y);
        if (this.type2 != null) {
            g.setColor(color2);
            g.drawString(ViewUtil.format(aconf.getClickY() * (scale2.getMax() - scale2.getMin()) + scale2.getMin()), x + 80, y);
        }
    }}

    public void clearScreen ()
    {
        Graphics2D g = this.dataBuffer.createGraphics();
        g.setColor(bg);
        g.fillRect(0, 0, this.dataBuffer.getWidth(), this.dataBuffer.getHeight());
    }
    
    public void clearAnnotation ()
    {
        Graphics2D g = this.dataBuffer.createGraphics();
        g.setColor(bg);
        g.fillRect(this.getPlotWidth(), 0, this.dataBuffer.getWidth(), this.dataBuffer.getHeight());
    }

    /**
     * @return the width to be used for plotting data
     */
    public int getPlotWidth ()
    {
        return super.getWidth() * 4 / 5;
    }

    /**
     * @return the height to be used for plotting data
     */
    public int getPlotHeight ()
    {
        return super.getHeight() - 12;
    }

    @Override public void setType (final String type)
    { synchronized (this.formatter) {
        super.setType(type);
        this.clearScreen();
        this.plotAnnotation();
        this.repaint(this.getVisibleRect());
    }}

    /**
     * @param type the secondary type to be plotted, or
     * <code>null</code> to disable the secondary plot
     */
    public void setSecondary (final String type)
    { synchronized (this.formatter) {
        this.type2 = sm.getScale(type) == null ? null : type;
        wm.calculateOpenWindows();
        this.clearScreen();
        this.plotAnnotation();
        this.repaint(this.getVisibleRect());
    }}

    /**
     * @return the secondary type being plotted, or
     * null if secondary plot is disabled
     */
    public String getSecondary ()
    { synchronized (this.formatter) {
        return this.type2;
    }}

    /**
     * @return a BufferedImage containing the current plot and annotation
     */
    @Override public BufferedImage getBufferedImage ()
    { synchronized (this.formatter) {
        BufferedImage imageBuffer =
            new BufferedImage(this.dataBuffer.getWidth(), this.dataBuffer.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.paintComponent(imageBuffer.createGraphics());
        return imageBuffer;
    }}

    public void markData (double x, final double y)
    { synchronized (this.formatter) {
        if (x > this.getPlotWidth()) x = this.getPlotWidth(); //outside plot area
        double range = x * config.getPlotRange() / this.getPlotWidth();
        wm.setClickRay(this.azimuth, this.elevation, range, (int) x, (int) y);
        wm.setAScopeClickY((this.getPlotHeight() - y) / getPlotHeight());
    }}

    /**
     * @param km the distance from the radar in km to draw the click line
     */
    public void setClickRange (final double km)
    { synchronized (this.formatter) {
        aconf.setClickRange(km);
        this.clearAnnotation();
        this.plotAnnotation();
        this.repaint(this.getVisibleRect());
    }}

    public void setClickY (final double fraction)
    { synchronized (this.formatter) {
        aconf.setClickY(fraction);
    }}

    public void enableClickRangeLine (final boolean shouldClickRangeLineBeDrawn)
    { synchronized (this.formatter) {
        aconf.setDrawClickRangeEnabled(shouldClickRangeLineBeDrawn);
    }}

    @Override public void setSizes (final int width, final int height)
    { synchronized (this.formatter) {
        super.setSizes(width, height);
        this.dataBuffer = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.clearScreen();
        this.plotAnnotation();
        this.plot(ray1, ray2);
        this.repaint(this.getVisibleRect());
    }}

    @Override public void setParent (final JInternalFrame parent)
    {
        super.setParent(parent);
        parent.setFrameIcon(new ImageIcon(Loader.getResource("icons/ascope.png")));
    }

    @Override public String getStyle () { return "AScope"; }
}
