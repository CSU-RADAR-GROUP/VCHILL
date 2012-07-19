package edu.colostate.vchill.plot;

import java.awt.*;  
import edu.colostate.vchill.LimitedList;
import edu.colostate.vchill.Loader;
import edu.colostate.vchill.ViewUtil;
import edu.colostate.vchill.chill.ChillOldExtTrackInfo;
import edu.colostate.vchill.chill.ChillNewExtTrackInfo;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.chill.ChillTrackInfo;
import edu.colostate.vchill.data.Ray;
import edu.colostate.vchill.gui.ViewPaintPanel;
import edu.colostate.vchill.gui.ViewRemotePanel;
import edu.colostate.vchill.gui.ViewSplitPane;
import edu.colostate.vchill.gui.ViewWindow;
import edu.colostate.vchill.gui.MapServerConfig;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;







/**
 * This window type displays data as colored graphics.
 *
 * @author  Justin Carlson
 * @author  Jochen Deyke 
 * @author  jpont
 * @created April 25, 2003
 * @version 2010-08-02
 */
public class ViewPlotWindow extends ViewWindow
{
    private boolean plotting; //are we currently plotting?
    private ViewSplitPane containAll;
    
    private int prevWidth;
    private int prevHeight;
    private int plotWidth;
    private int plotHeight;
    
    //The Current plotting methods to be used based on the data
    //that is being received.
    
	private final static MapServerConfig msConfig = MapServerConfig.getInstance();
    
    private ViewPlotMethod plotMethod;
    private boolean modeOverriden = false;

    //Used to plot information with a graphics, this includes
    //things like user mouse clicks, type of plots etc.
    private ViewPlotInformation plotInformation;

    //used to set colors correctly for the various plot types.

    //Used to draw data onto
    private BufferedImage dataBuffer;

    private BufferedImage aircraftBuffer;
    private BufferedImage overlayBuffer;
    private BufferedImage annotationBuffer;

    private boolean overlayReplotNeeded = true;

    private Map<String, LimitedList<Point>> aircraftInfo;

    private volatile int dragOffsetX = 0;
    private volatile int dragOffsetY = 0;

    private volatile int dragRectStartX = 0;
    private volatile int dragRectStartY = 0;
    private volatile int dragRectEndX = 0;
    private volatile int dragRectEndY = 0;

    
    /**
     * Constructor for the ViewPlotWindow object
     *
     * @param type The datatype to display (eg Z, V, ...)
     */
    public ViewPlotWindow (final String type)
    {
        super();
        this.type = type;

       
        
        
        //Set the layout to add components to the left as they are added.
        plotMethod = new ViewPlotMethodPPI(this.type);
        this.aircraftInfo = plotMethod.getAircraftInfo();
        plotInformation = new ViewPlotInformation();
        dataBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        this.aircraftBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        this.overlayBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        this.annotationBuffer = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);
        
        //Swing setup.
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        setBackground(Color.BLACK);
        setForeground(Color.BLACK);
        setDoubleBuffered(false);
        //setType(type);
        this.prevWidth = getWidth(); 
        this.prevHeight = getHeight();
        this.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized (final ComponentEvent ce) {
                wm.setPlotWindowWidth(parent.getWidth());
                wm.setPlotWindowHeight(parent.getHeight());
                wm.resizePlots();
                vc.rePlot();
            }});
    }

    public void setColors ()
    {
        this.setColors(this.plotMethod.getColors());
    }

    public void setColors (final List<Color> colors)
    {
        plotInformation.setColors(colors);
        rePlotDrawingArea();
    }

    /**
     * Changes the type of data that will be plotted
     *
     * @param type The new type value
     */
    @Override public void setType (final String type)
    {
        super.setType(type);
        plotMethod.setType(type);
        this.setColors();
    }

    private void resetInformation ()
    {
        plotInformation.setRadarName(plotMethod.getRadarName());
        ChillMomentFieldScale scale = sm.getScale(type);
        if (scale == null) return; //no longer available
        plotInformation.setType(type
             + (scale.isGradientable() ? config.getGradientType().suffix :
             (scale.isUnfoldable() ? (config.isUnfoldingEnabled() ? "UnF" : "") :
              ""))); //append string according to filter used
        plotInformation.setDataKey(scale.fieldDescription);
        plotInformation.setAnnotationString(plotMethod.getAnnotationString());
        plotInformation.setElevation(ViewUtil.format(plotMethod.getRadarElevation()));
        plotInformation.setAzimuth(ViewUtil.format(plotMethod.getRadarAzimuth()));
        plotInformation.setDateAndTime(plotMethod.getDateAndTime());
        plotInformation.setGateInfo(plotMethod.getNumberOfGates() + "x" +
                                     ((int)plotMethod.getMetersPerGate()) + "m");
    }

    //Which plotting Method should be used, this needs to be changed
    //to something much simpler.
    /**
     * Changes the plotting method
     *
     * @param mode The new plotting mode (PPI, RHI, etc)
     * @param overrideMode Whether the new mode was explicitly specified
     */
    public void setMode (final String mode, final boolean overrideMode)
    {
        if (modeOverriden && !overrideMode) return; //ignore if override is on
        if (mode == null) return; //can't plot null mode
        if (mode.equals(plotMethod.getPlotMode()) && !overrideMode) return; //same mode
        if (mode.equals("auto")) {
            modeOverriden = false;
            return;
        }

        modeOverriden = overrideMode; //won't turn off due to return above

        if (mode.equals("RHI")) {
            plotMethod = new ViewPlotMethodRHI(type);
        } else if (mode.equals("PPI")) {
            plotMethod = new ViewPlotMethodPPI(type);
        } else if (mode.equals("MAN")) {
            plotMethod = new ViewPlotMethodTH(type);
        } else {
            System.err.println("Unknown mode: " + mode + " (plotting as TH)");
            plotMethod = new ViewPlotMethodTH(type);
        }

        parent.setFrameIcon(new ImageIcon(Loader.getResource("resources/icons/sweep" + plotMethod.getPlotMode() + ".png")));

        this.aircraftInfo = plotMethod.getAircraftInfo();
        setupPlottingSizes();
        overlayReplotNeeded = true;
        clearScreen();
        rePlotDrawingArea();
    }

    @Override public void setParent (final JInternalFrame parent)
    {
        super.setParent(parent);
        this.containAll = new ViewSplitPane(this, plotInformation);
        this.containAll.addPropertyChangeListener(ViewSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            public void propertyChange (final PropertyChangeEvent evt) {
                wm.setPlotDividerLocation(((ViewSplitPane)evt.getSource()).getDividerLocation());
                wm.resetPlotDividerLocation();
            }});
        this.containAll.setOneTouchExpandable(true);
        parent.setContentPane(this.containAll);
        parent.setFrameIcon(new ImageIcon(Loader.getResource("resources/icons/sweep" + plotMethod.getPlotMode() + ".png")));
    }

    public String getPlotMode ()
    {
        return modeOverriden ? plotMethod.getPlotMode() : "<auto>";
    }

    public void rePlotDrawingArea ()
    {
        resetInformation();
        parent.repaint(parent.getVisibleRect());
    }

    public void clearScreen ()
    {
        Graphics g = getGraphics();
        plotMethod.clearScreen(g);
        Graphics2D gBuff = dataBuffer.createGraphics();
        plotMethod.clearScreen(gBuff);
        repaint();
    }

    public void clearAircraftInfo ()
    {
        plotMethod.clearAircraftInfo();
    }

    /**
     * This method will reset where everything is drawing, and also how
     * much area the various operations will be taking.  This is to allow
     * the window to be dynamically resized and still have the center
     * remain consistent.
     */
    private void setupPlottingSizes ()
    {
        plotWidth = /*config.isAnnotationEnabled() ? getWidth() * 4 / 5 : */getWidth();
        plotHeight = getHeight();

        //Set the size of the plotting area.
        plotMethod.setPreferredSize(plotWidth, plotHeight);

        if (plotWidth == this.prevWidth && plotHeight == this.prevHeight) return;
        if (getWidth() <= 0 || getHeight() <= 0) return;

        this.prevWidth = plotWidth;
        this.prevHeight = plotHeight;
        dataBuffer = new BufferedImage(plotWidth, plotHeight, BufferedImage.TYPE_INT_ARGB);
        this.aircraftBuffer = new BufferedImage(plotWidth, plotHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D acg = this.aircraftBuffer.createGraphics();
        acg.setColor(new Color(1f, 1f, 1f, 1f));
        this.overlayBuffer = new BufferedImage(plotWidth, plotHeight, BufferedImage.TYPE_INT_ARGB);
        this.annotationBuffer = new BufferedImage(plotWidth, plotHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D olg = this.overlayBuffer.createGraphics();
        olg.setColor(new Color(1f, 1f, 1f, 1f));
        overlayReplotNeeded = true;
    }

    @Override public void paintComponent (final Graphics g)
    {
    	
    	super.paintComponent(g);
        if (g == null) return;

         
        
        
        g.drawImage(dataBuffer, dragOffsetX, dragOffsetY, this);
        this.clearAircraftBuffer();
        this.plotAircraft();
        if (overlayReplotNeeded) {
            clearOverlayBuffer();
            plotOverlay();
            clearAnnotationBuffer();
        }
        
        if(ViewPaintPanel.GreasePencilAnnotationEnabled)
        {
        
        
            if(image == null)
            { 
            		
                    image = createImage(getSize().width, getSize().height);
                    Graphics2D graphics2D = annotationBuffer.createGraphics();
                    graphics2D = (Graphics2D)image.getGraphics(); 
                    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
                    clearAnnotationBuffer();  
            } 
        	//clear();

            
            
            //g.drawImage(image, 0, 0, null);        	
        	
        }
        
        g.drawImage(this.overlayBuffer, dragOffsetX, dragOffsetY, null);
        g.drawImage(this.aircraftBuffer, dragOffsetX, dragOffsetY, null);
        if (config.isClickPointEnabled() && dragOffsetX == 0 && dragOffsetY == 0) plotMethod.plotClickPoint(g);
        if (dragRectStartX != 0 || dragRectStartY != 0 || dragRectEndX != 0 || dragRectEndY !=0) g.draw3DRect(dragRectStartX, dragRectStartY, dragRectEndX - dragRectStartX, dragRectEndY - dragRectStartY, false);
        g.drawImage(annotationBuffer, dragOffsetX, dragOffsetY, null);

    
    }

    public void replotOverlay ()
    {
        overlayReplotNeeded = true;
    }

    private void clearOverlayBuffer ()
    {
        Graphics2D olg = overlayBuffer.createGraphics();
        olg.setBackground(new Color(0f, 0f, 0f, 0f));
        olg.clearRect(0, 0, overlayBuffer.getWidth(), overlayBuffer.getHeight());
    }

    private void plotOverlay ()
    {
        if (!overlayReplotNeeded) return;
        overlayReplotNeeded = false;
        Graphics2D overlay = overlayBuffer.createGraphics();

        plotMethod.NeedToPlotMap = true;
        
        if(msConfig.plottedUnderlayOnce() == false)
        {
        	
          	
          plotMethod.plotMapServerUnderlay(overlay);        
        	
        }        
        
        if (config.isGridEnabled()) plotMethod.plotGrid(overlay);
        if (config.isMapEnabled())
        {
        	plotMethod.plotMap(overlay);
        }

        //plotMethod.plotMapServerOverlay(overlay);
        

        plotMethod.plotMapServerOverlay(overlay);
        
    }
    private void clearAircraftBuffer ()
    {
        Graphics2D acg = this.aircraftBuffer.createGraphics();
        acg.setBackground(new Color(0f, 0f, 0f, 0f));
        acg.clearRect(0, 0, aircraftBuffer.getWidth(), aircraftBuffer.getHeight());
    }

    /**
     * Add an aircraft location to the list to be plotted
     */
    public void plotAircraft (final ChillTrackInfo loc)
    {
        this.plotMethod.plotAircraft(loc);
    }
    
    /**
     * Add an aircraft location to the list to be plotted
     */
    public void plotAircraft (final ChillOldExtTrackInfo coeti)
    {
	    this.plotMethod.plotAircraft(coeti);
    }

    /**
     * Add an aircraft location to the list to be plotted
     */
    public void plotAircraft (final ChillNewExtTrackInfo cneti)
    {
        this.plotMethod.plotAircraft(cneti);
    }

    /**
     * Draw aircraft locations in list to buffer
     */
    private void plotAircraft ()
    {
        Graphics2D acg = this.aircraftBuffer.createGraphics();
        for (String name : this.aircraftInfo.keySet()) {
            for (Point p : this.aircraftInfo.get(name)) {
                if (name.hashCode() % 2 == 0) drawRhombus(acg, (int)p.getX(), (int)p.getY());
                else drawSquare(acg, (int)p.getX(), (int)p.getY());
            }
        }
    }

    private static void drawRhombus (final Graphics g, final int x, final int y)
    {
        g.drawLine(x - 3, y, x, y - 3);
        g.drawLine(x, y - 3, x + 3, y);
        g.drawLine(x + 3, y, x, y + 3);
        g.drawLine(x, y + 3, x - 3, y);
    }

    private static void drawSquare (final Graphics g, final int x, final int y)
    {
        g.drawLine(x - 3, y - 3, x + 3, y - 3);
        g.drawLine(x - 3, y + 3, x + 3, y + 3);
        g.drawLine(x - 3, y - 3, x - 3, y + 3);
        g.drawLine(x + 3, y - 3, x + 3, y + 3);
    }


    
    public void plot (final Ray prevRay, final Ray currRay,
            final Ray nextRay, final Ray threshRay)
    {
    	
    	msConfig.plottedOnce(true);	    
    	

    	if (currRay == null) return; //can't plot without data...
        setMode(currRay.getMode(), false);

        //This translation to a specific type may require updates to be made
        //that determine data translation and values.  Inform the data hash
        //of these qaulifiers.
        Graphics g = dataBuffer.getGraphics();
    	plotMethod.plotMapServerUnderlay(g);    	    	
        
        //Send the object which holds the data type into the translation method
        //in order to get useful data for plotting.
        plotMethod.plotData(prevRay, currRay, nextRay, threshRay, g);
                        /*
        plotInformation.setDataInfo(ViewUtil.format(vc.getDataValue(
            plotMethod.getPlotMode(), type, plotMethod.getClickAz(), plotMethod.getClickEl(),
            (int)((plotMethod.getClickRng() - plotMethod.getStartRange()) / (plotMethod.getMetersPerGate() / 1000.0)))));
            */
    }

    @Override public BufferedImage getBufferedImage ()
    {
        return this.containAll.getBufferedImage();
    }

    /**
     * Converts a left mouse click into a km-based ClickPoint instruction
     * 
     * @param x X coordinate of where the mouse was clicked
     * @param y Y coordinate of where the mouse was clicked
     */
    public void markData (final int x, final int y)
    {
        double az = this.plotMethod.getAzimuthDegrees(x, y);
        double el = this.plotMethod.getElevationDegrees(x, y);
        double rng = this.plotMethod.getRangeInKm(x, y);
        if (rng > 0) wm.setClickRay(az, el, rng, x, y);
        else wm.setClickRay(0, 0, 0, x, y);
    }

    /**
     * Converts a middle mouse click into a km-based recentering instruction
     *
     * @param x X coordinate of where the mouse was clicked
     * @param y Y coordinate of where the mouse was clicked
     */
    public void recenter (final int x, final int y)
    {
        config.setCenterX((x - plotMethod.getCenterX() > 0 ? 1 : -1 ) *
            plotMethod.getRangeInKm(x, plotMethod.getCenterY()));
        config.setCenterY((y - plotMethod.getCenterY() > 0 ? -1 : 1 ) *
            plotMethod.getRangeInKm(plotMethod.getCenterX(), y));
        wm.setCenterInKm();
        repaint();
    }

    /**
     * Updates the display with information about the ClickPoint
     *
     * @param azimuth the azimuth angle (in degrees) of the radar
     * @param elevation the elevation angle (in degrees) of the radar
     * @param range the distance (in km) from the radar
	 * @param x X coordinate of where the mouse was clicked
     * @param y Y coordinate of where the mouse was clicked
     */
    public void setClickRay (final double azimuth, final double elevation, final double range, final int x, final int y)
    {
        plotMethod.setClickPoint(azimuth, elevation, range); 
        double elKm = ViewUtil.getKmElevation(elevation, range);
        double elKft = ViewUtil.getKFtFromKm(elKm);
        plotInformation.setClickAzimuth(ViewUtil.format(azimuth));
        plotInformation.setClickElevation(ViewUtil.format(elevation));
        plotInformation.setClickHeight(ViewUtil.format(elKm), ViewUtil.format(elKft));
        plotInformation.setClickRange(ViewUtil.format(range));
	Double dataValue = vc.getDataValue(
            plotMethod.getPlotMode(), type, azimuth, elevation,
			(int) Math.round((range - plotMethod.getStartRange()) / (plotMethod.getMetersPerGate() / 1000.0)),
			plotMethod, x, y );
        plotInformation.setDataInfo(Double.isNaN(dataValue) ? "N/A" : ViewUtil.format(dataValue));
        rePlotDrawingArea();
    }

    /**
     * Sets the center of the plot to the coordinates set in ViewControlConfig.
     * The coordinates are in km north/east of the radar.
     */
    public void setCenterInKm ()
    {
        double kmEast = config.getCenterX();
        double kmNorth = config.getCenterY();
        int x = 0 - plotMethod.getPixelsX(kmEast, kmNorth);
        int y = plotMethod.getPixelsY(kmEast, kmNorth);
        plotMethod.setPlottingOffset(x, y);
        this.replotOverlay();
    }

    public void rePlot ()
    {
        clearScreen();
        vc.rePlot();
    }

    @Override public synchronized void setPlotting (final boolean plotting) {
        super.setPlotting(plotting); if (plotting) plotMethod.setNewPlot();}

    public void updateSizes ()
    {
        super.setSizes(wm.getPlotWindowWidth(), wm.getPlotWindowHeight());
        setupPlottingSizes();
    }

    public void updateDividerLocation ()
    {
        this.containAll.setDividerLocation(wm.getPlotDividerLocation());
    }

    public void setDragOffset (final int dragOffsetX, final int dragOffsetY)
    {
        this.dragOffsetX = dragOffsetX;
        this.dragOffsetY = dragOffsetY;
        repaint();
    }

    public void resetDragOffset ()
    {
        this.recenter(this.getWidth() / 2 - this.dragOffsetX, this.getHeight() / 2 - this.dragOffsetY);
        this.dragOffsetX = 0;
        this.dragOffsetY = 0;
        rePlot();
    }

    public void setDragRect (final int startX, final int startY, final int endX, final int endY)
    {
        this.dragRectStartX = startX;
        this.dragRectStartY = startY;
        this.dragRectEndX = endX;
        this.dragRectEndY = endY;
        repaint();
    }

    public void resetDragRect ()
    {
        int x = (this.dragRectStartX + this.dragRectEndX) / 2;
        int y = (this.dragRectStartY + this.dragRectEndY) / 2;
        int size = Math.max(this.dragRectEndX - this.dragRectStartX, this.dragRectEndY - this.dragRectStartY);
        config.setCenterX((x - this.plotMethod.getCenterX() > 0 ? 1 : -1 ) *
            plotMethod.getRangeInKm(x, this.plotMethod.getCenterY()));
        config.setCenterY((y - plotMethod.getCenterY() > 0 ? -1 : 1 ) *
            plotMethod.getRangeInKm(this.plotMethod.getCenterX(), y));
        config.setPlotRange(this.plotMethod.getKmFromPixels(size) / 2);
        ViewRemotePanel.getInstance().update();
        this.dragRectStartX = 0;
        this.dragRectStartY = 0;
        this.dragRectEndX = 0;
        this.dragRectEndY = 0;
        wm.setCenterInKm();
        rePlot();
    }

    /**
     * @return Transparentified background version of data buffer
     */
    private BufferedImage getDataBuffer ()
    {
        BufferedImage result = new BufferedImage(this.dataBuffer.getWidth(), this.dataBuffer.getHeight(), this.dataBuffer.getType());
        for (int w = 0; w < result.getWidth(); ++w) for (int h = 0; h < result.getHeight(); ++h) {
            int rgb = this.dataBuffer.getRGB(w, h);
            if (rgb == Color.BLACK.getRGB()) rgb = 0x00000000; //transparent black
            result.setRGB(w, h, rgb);
        }
        return result;
    }

    private BufferedImage getColorbar ()
    {
        BufferedImage buff = new BufferedImage(plotInformation.getWidth(), plotInformation.getHeight() * 64 / 100, BufferedImage.TYPE_INT_ARGB);
        plotInformation.paintComponent(buff.createGraphics());
        return buff;
    }

    @Override public String getStyle () { return "Plot"; }

    /**
     * Creates a temporary kmz file for use with external display programs
     *
     * @param filename path/name to save to or null for a temporary, automatically deleted file
     * @return the name of the file written to
     */
    public String export (final String filename) throws IOException
    {
        if (!this.isExportable()) return null;
        File zipfile = filename == null ? File.createTempFile("vchill", ".kmz") : new File(filename);
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipfile));

        //write image
        zip.putNextEntry(new ZipEntry("vchillimg.png"));
        ImageIO.write(this.getDataBuffer(), "png", zip);
        zip.closeEntry();

        //write colorbar
        zip.putNextEntry(new ZipEntry("vchillbar.png"));
        ImageIO.write(this.getColorbar(), "png", zip);
        zip.closeEntry();

        //write kml
        this.plotMethod.export(zip);

        zip.close();
        if (filename == null) zipfile.deleteOnExit();
        return zipfile.getAbsolutePath();
    }

    public boolean isExportable ()
    {
        return this.plotMethod.isExportable();
    }

    
    
    
	Image image;
    
    
    public void updateAnnotationLayer(int currentX, int currentY, int oldX, int oldY)
    {
    	
    	Graphics2D graphics2D = annotationBuffer.createGraphics();
    	graphics2D.setPaint(ViewPaintPanel.getPaintColor());
    	
    	graphics2D.setStroke(new BasicStroke(ViewPaintPanel.getPenSize()));
    	
    	if(graphics2D != null)  
        	//graphics2D.fillOval(currentX, currentY, ViewPaintPanel.getPenSize(), ViewPaintPanel.getPenSize()); 
    		graphics2D.drawLine(oldX, oldY, currentX, currentY); 

        repaint();
    	
    }

    public void updateTextAnnotationLayer(int currentX, int currentY)
    {
    	
    	Graphics2D graphics2D = annotationBuffer.createGraphics();
    	graphics2D.setPaint(ViewPaintPanel.getPaintColor());
    	
    	if(graphics2D != null)
    		if(ViewPaintPanel.PPIDisplayString.getText() != null)
        	graphics2D.drawString(ViewPaintPanel.PPIDisplayString.getText(), currentX, currentY); 

        repaint();
    	
    }    
    
    
    public void clearAnnotationBuffer()
    {     	
    	
    		Graphics2D graphics2D = annotationBuffer.createGraphics();
    		graphics2D.setBackground(new Color(0f, 0f, 0f, 0f));
    		graphics2D.clearRect(0, 0, getSize().width, getSize().height);

    		repaint();
    }        
}
