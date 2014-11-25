package edu.colostate.vchill.plot;

import edu.colostate.vchill.*;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.chill.ChillNewExtTrackInfo;
import edu.colostate.vchill.chill.ChillOldExtTrackInfo;
import edu.colostate.vchill.chill.ChillTrackInfo;
import edu.colostate.vchill.color.XMLControl;
import edu.colostate.vchill.data.Ray;
import edu.colostate.vchill.gui.MapServerConfig;
import edu.colostate.vchill.gui.MapServerConfigWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.zip.ZipOutputStream;
//Rausch


/**
 * This is the super class for the different types of plotting modes. The
 * methods and fields are both common to the lower level plots. NOTE: Mouse
 * click distance finds must be moved to this level instead of the
 * ViewPlotWindow.
 *
 * @author Justin Carlson
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-07-07
 * @created April 25, 2003
 */
public abstract class ViewPlotMethod {


    protected Boolean NeedToPlotMap = false;
    protected boolean Mappable = false;
    protected final static MapServerConfig msConfig = MapServerConfig.getInstance();
    protected final static Config config = Config.getInstance();
    protected final static ScaleManager sm = ScaleManager.getInstance();
    protected final static ViewControl vc = ViewControl.getInstance();
    protected static final LocationManager lm = LocationManager.getInstance();
    private final static NumberFormat nf = config.getNumberFormat();
    protected static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Color management; stores the current color map
     */
    private final XMLControl colorControl = vc.getColorControl();

    /**
     * The elevation that this data set was collected at
     */
    protected double radarElevation = 0;

    /**
     * The azimuth that this data set was collected at
     */
    protected double radarAzimuth = 0;

    /**
     * The distance in km to the first gate
     */
    protected double startRange = 0;

    protected String type = null;

    protected long beginTime = -1;
    protected long currentTime = 0;

    protected int height;
    protected int width;

    protected int offsetX, offsetY;

    protected String radarId;

    /**
     * Number of meters in one gate. Default to 150, found from Ray data.
     */
    protected double metersPerGate = 150;

    /**
     * Number of gates in one ray. Found from Ray data.
     */
    protected int numGates = 0;

    /**
     * Location the user clicked on. Used to highlight that point.
     */
    protected double clickAz, clickEl, clickRng;

    /**
     * stores Points of where aircraft were
     */
    protected Map<String, LimitedList<Point>> aircraftInfo;

    protected void clearAircraftInfo() {
        this.aircraftInfo.clear();
    }

    public ViewPlotMethod(final String type) {
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.type = type;
        this.aircraftInfo = new HashMap<String, LimitedList<Point>>();
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public List<String> getAnnotationString() {
        return this.getAnnotationString(sm.getScale(this.type).isUnfoldable() && config.isUnfoldingEnabled() ? 2 : 1);
    }

    private List<String> getAnnotationString(double scaleMultiplier) {
        int maxnum = (int) (16.0 * this.height / 512.0); //number of entries in scale
        int num = getColors().size();
        if (num > maxnum) {
            int i = 2;
            while (num / i > maxnum) ++i;
            num = num / (i - 1);
        }
        ArrayList<String> vals = new ArrayList<String>(num + 1);
        double max = sm.getScale(this.type).getMax() * scaleMultiplier;
        double min = sm.getScale(this.type).getMin() * scaleMultiplier;
        double step = (max - min) / (double) num;
        double value = max;
        for (int i = 0; i < num + 1; ++i) {
            vals.add(ViewUtil.format(value));
            value -= step;
        }
        return Collections.unmodifiableList(vals);
    }

    public void setNewPlot() {
        this.beginTime = -1;
    }

    public double getRadarElevation() {
        return this.radarElevation;
    }

    public double getRadarAzimuth() {
        return this.radarAzimuth;
    }

    public double getStartRange() {
        return this.startRange;
    }

    /**
     * Gets the center of the current plot and grid.
     *
     * @return The x center of the grid that should be plotted in.
     */
    public int getCenterX() {
        return this.width / 2 + this.offsetX;
    }

    /**
     * Get the center of the plot and grid
     *
     * @return The calculated center due to various offsets.
     */
    public int getCenterY() {
        return this.height / 2 + this.offsetY;
    }

    /**
     * Gets the step at which to sample data points. This is also important
     * in determining the distance weight of each pixel and how often to
     * plot the grid intervals.
     *
     * @return How far to advance in the data (number of gates) per pixel
     */
    public double getPlotStepSize() {
        return (2048 * config.getPlotRange()) / (metersPerGate * Math.min(this.height, this.width));
    }

    /**
     * This is a method to mimic a JPanel in case this class will later
     * extend JPanel. It alters the height and width of the area to plot in
     *
     * @param width  The new width of the plot
     * @param height The new height of the plot
     */
    public void setPreferredSize(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public void setPlottingOffset(final int xOffset, final int yOffset) {
//System.out.println("offset changed to " + xOffset + ", " + yOffset);
        this.offsetX = xOffset;
        this.offsetY = yOffset;
    }

    /**
     * Fills the buffer or area refered to by the Graphics2D Object with a black sqaure.
     *
     * @param g The Graphics2D reference to what is to be cleared.
     */
    public void clearScreen(final Graphics g) {
        if (g == null) return;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, this.width, this.height);
    }

    protected void plotAircraft(final ChillTrackInfo loc) {
    }

    protected void plotAircraft(final ChillOldExtTrackInfo coeti) {
    }

    protected void plotAircraft(final ChillNewExtTrackInfo cneti) {
    }

    protected abstract int getPixelsFromKm(final double distance);

    protected abstract double getKmFromPixels(final int distance);

    //angles (in radians)
    protected abstract double getStartAngle(final Ray rayCurr);

    protected abstract double getEndAngle(final Ray rayCurr);

    //coordinates
    protected abstract int getX(final Angle angle, final double offset);

    protected abstract int getY(final Angle angle, final double offset, final int xPos);

    protected boolean outOfRange(final int[] xVals, final int[] yVals) {
        return false;
    }

    protected boolean outOfRange(final double startAngle, final double endAngle) {
        return false;
    }

    /**
     * Caches the sin and cos for an angle (in radians) for quicker calculations
     */
    static class Angle {
        private double angle = Double.NaN; //radians
        private double sin;
        private double cos;

        public double getAngle() {
            return this.angle;
        }

        public double getSin() {
            return this.sin;
        }

        public double getCos() {
            return this.cos;
        }

        /**
         * @param angle in radians
         */
        public void setAngle(final double angle) {
            if (this.angle != angle) {
                this.angle = angle;
                this.sin = Math.sin(angle);
                this.cos = Math.cos(angle);
            }
        }
    }

    private Angle startAngle = new Angle();
    private Angle endAngle = new Angle();


    // Rausch

    private Image plotEPSG4326Underlay() {
        System.out.println("Using EPSG:4326 for underlay");

        double BBnorth, BBsouth, BBeast, BBwest;

        Image image = null;

        double[] NWLatLong = ViewUtil.getDegrees(getKmFromPixels(-getCenterX()), getKmFromPixels(getCenterY()));

        double[] SELatLong = ViewUtil.getDegrees(getKmFromPixels(-getCenterX() + this.width), getKmFromPixels(getCenterY() - this.height));

        BBwest = NWLatLong[0];
        BBeast = SELatLong[0];
        BBnorth = NWLatLong[1];
        BBsouth = SELatLong[1];


        try {
            // Read from a URL

            URL url = new URL("http://wms.chill.colostate.edu/cgi-bin/mapserv?REQUEST=GetMap&VERSION=1.1.1&SRS=epsg:4326&SERVICE=WMS&map=/var/www/html/maps/test.map&BBOX=" + BBwest + "," + BBsouth + "," + BBeast + "," + BBnorth + "&WIDTH=" + (this.width) + "&HEIGHT=" + (this.height) + "&FORMAT=image/png;%20mode=24bit&LAYERS=" + msConfig.getUserMapUnderlayLayers());
            //URL url = new URL("http://sedac.ciesin.columbia.edu/geoserver/gwc/service/wms?REQUEST=GetMap&VERSION=1.1.1&SRS=epsg:4326&SERVICE=WMS&&BBOX=" + round(BBwest) + "," + round(BBsouth) + "," + round(BBeast) + "," + round(BBnorth) + "&WIDTH=" + (this.width) + "&HEIGHT=" + (this.height) + "&FORMAT=image/png;&LAYERS=" + msConfig.getUserMapUnderlayLayers());

            image = ImageIO.read(url);

        } catch (Exception e) {
            System.out.println("Something went very wrong VIew Plot Method: Line 300:" + e);

        }

        return image;
    }

    private Image plotAUTO42003Underlay() {
        System.out.println("Using AUTO:42003 for underlay");

        double[] centerLatLong = ViewUtil.getDegrees(getKmFromPixels(-getCenterX() + this.width / 2), getKmFromPixels(getCenterY() - this.height / 2));
        // 	System.out.println("Lat and Lon is:"+ centerLatLong[1] + " " + centerLatLong[0]);
        Image image = null;

        try {
            // Read from a URL

            URL url = new URL("http://wms.chill.colostate.edu/cgi-bin/mapserv?REQUEST=GetMap&VERSION=1.1.1&SRS=AUTO:42003,9001," + centerLatLong[0] + "," + centerLatLong[1] + "&SERVICE=WMS&map=/var/www/html/maps/test.map&BBOX=" + getKmFromPixels(-this.width / 2) * 1000 + "," + getKmFromPixels(-this.height / 2) * 1000 + "," + getKmFromPixels(this.width / 2) * 1000 + "," + getKmFromPixels(this.height / 2) * 1000 + "&WIDTH=" + (this.width) + "&HEIGHT=" + (this.height) + "&FORMAT=image/png;%20mode=24bit&LAYERS=" + msConfig.getUserMapUnderlayLayers());
            // URL url = new URL("http://sedac.ciesin.columbia.edu/geoserver/gwc/service/wms?REQUEST=GetMap&VERSION=1.1.1&SRS=AUTO:42003,9001," + centerLatLong[0] + "," + centerLatLong[1] + "&SERVICE=WMS&BBOX=" + getKmFromPixels(-this.width/2)*1000 + "," + getKmFromPixels(-this.height/2)*1000 + "," + getKmFromPixels(this.width/2)*1000 + "," + getKmFromPixels(this.height/2)*1000 + "&WIDTH=" + (this.width) + "&HEIGHT=" + (this.height) + "&FORMAT=image/png;%20mode=24bit&LAYERS=" + msConfig.getUserMapUnderlayLayers());

            //    System.err.println("URL is :"+url);
            image = ImageIO.read(url);

        } catch (Exception e) {
            System.out.println("Something went very wrong: ViewPlotMethod Line 327");
        }

        return image;
    }

    public void plotMapServerUnderlay(Graphics g) {

        if (NeedToPlotMap == true) {
            NeedToPlotMap = false;
        } else {
            return;
        }


        if (msConfig.getUserMapUnderlayLayers() == "") {
            //System.out.println("No layers to display");

            return;

        }

        NeedToPlotMap = false;

        if (msConfig.AUTO42003IsEnabled() == true)
            g.drawImage(plotAUTO42003Underlay(), 0, 0, null);
        else if (msConfig.EPSG4326IsEnabled() == true)
            g.drawImage(plotEPSG4326Underlay(), 0, 0, null);


    }


    /**
     * Translates a (set of) rays into sets of x, y endpoints and a color value and draws the result onto the specified Graphics
     *
     * @param prevRay   The previous ray
     * @param currRay   The data that is to be translated.
     * @param nextRay   The next ray
     * @param threshRay The threshold ray corresponding to the current ray
     * @param g         the Graphics context to plot to
     */

    public void plotData(final Ray prevRay, final Ray currRay, final Ray nextRay, final Ray threshRay, final Graphics g) {
        if (currRay == null) throw new IllegalArgumentException("Error: PlotMethod.plotData(): null for data");

        double[] values = currRay.getData();

        radarId = currRay.getRadarId();
        radarAzimuth = currRay.getStartAzimuth();
        radarElevation = currRay.getStartElevation();
        metersPerGate = currRay.getGateWidth() * 1e3;
        numGates = currRay.getNumberOfGates();
        setTime(currRay.getDate());

        //double start = getStartAngle(currRay);
        double end = getEndAngle(currRay);
        double start = prevRay == null ? getStartAngle(currRay) : getEndAngle(prevRay);
        if (Math.abs(start - end) > 0.25) start = getStartAngle(currRay); //radians
        //double end = nextRay == null ? getEndAngle(currRay) : getStartAngle(nextRay);
        if (outOfRange(start, end)) return; //do this here so the sin and cos won't be calculated on offscreen angles
        startAngle.setAngle(start);
        endAngle.setAngle(end);

        //This is the step size for the plotting, or how often we should sample data points.
        double plotStepSize = getPlotStepSize();

        startRange = currRay.getStartRange() * 1e-6; //mm -> km
        double pixelOffset = 0;
        double gateOffset = startRange / (metersPerGate / 1000.0F);
        if (gateOffset < 0) {
            gateOffset = Math.abs(gateOffset);
        } else {
            gateOffset = 0;
            pixelOffset = getPixelsFromKm(startRange); //km -> px
        }

        double prevValue = Double.NaN;

        //While there is more data, and less data than can be handled,
        //continue doing translation of the radar data.  Also, note that
        //the plotStepSize is related to the current zoom value and
        //represents how often to sample the data points.
        int[] xVals = new int[4];
        int[] yVals = new int[4];
        List<Color> colors = this.getColors();
        ViewPlotDataFilter filter = new ViewPlotDataFilter(prevRay, currRay, nextRay, threshRay, this.type);


        for (double i = gateOffset; i < numGates; i += plotStepSize) {
            int k = (int) i;

            //The x,y coordinates of one endpoint of the line/gate data.
            xVals[0] = getX(startAngle, pixelOffset);
            yVals[0] = getY(startAngle, pixelOffset, xVals[0]);

            //The other endpoint of the line.
            xVals[1] = getX(endAngle, pixelOffset);
            yVals[1] = getY(endAngle, pixelOffset, xVals[1]);

            //optimization - draw consecutive same-colored blobs at once
            //smoothing causes same value to possibly != same color
//            if (!config.isSmoothingEnabled()) while (values[(int)i] == values[k]) {
//                if (i + plotStepSize >= numGates) break;
//                i += plotStepSize;
//                ++offset;
//            }

            ++pixelOffset;

            xVals[2] = getX(endAngle, pixelOffset);
            yVals[2] = getY(endAngle, pixelOffset, xVals[2]);

            xVals[3] = getX(startAngle, pixelOffset);
            yVals[3] = getY(startAngle, pixelOffset, xVals[3]);

            if (xVals[0] < 0 && xVals[1] < 0 &&
                    xVals[2] < 0 && xVals[3] < 0) continue;
            if (yVals[0] < 0 && yVals[1] < 0 &&
                    yVals[2] < 0 && yVals[3] < 0) continue;
            if (xVals[0] > this.width && xVals[1] > this.width &&
                    xVals[2] > this.width && xVals[3] > this.width) continue;
            if (yVals[0] > this.height && yVals[1] > this.height &&
                    yVals[2] > this.height && yVals[3] > this.height) continue;

            //if (outOfRange(xVals, yVals)) continue; //check for > max plot range

            //apply filters
            double filteredValue = filter.applyFilters(i, plotStepSize, getElevationInKm(avg(xVals), avg(yVals)), prevValue);
            Color colorValue = this.getColorValue(filteredValue, colors);
            prevValue = filteredValue;

            //Finally set that value in the data so that it can be assigned a color
            //(might make more sense to just assign a color now).

            // Rausch
            if (colorValue != Color.BLACK) {
                int alphaTransparency = MapServerConfigWindow.getOpacity();

                colorValue = new Color(colorValue.getRed(), colorValue.getGreen(), colorValue.getBlue(), alphaTransparency);

                g.setColor(colorValue);

                g.drawPolygon(xVals, yVals, 4);
                g.fillPolygon(xVals, yVals, 4);


            }
        } //End for loop
        startAngle = endAngle;
        endAngle = new Angle();
    }

    /**
     * The Method that is to be used to plot the grid for this specific data type.
     *
     * @param g A Graphics2D ref to what the grid should be plotted into.
     */
    public abstract void plotGrid(Graphics g);

    public abstract void plotClickPoint(Graphics g);

    public void setClickPoint(final double azimuth, final double elevation, final double range) {
        this.clickAz = azimuth;
        this.clickEl = elevation;
        this.clickRng = range;
    }

    public double getClickAz() {
        return this.clickAz;
    }

    public double getClickEl() {
        return this.clickEl;
    }

    public double getClickRng() {
        return this.clickRng;
    }

    public void plotMap(Graphics g) {
    }

    public void plotMapServerOverlay(Graphics g) {
    }

    public abstract int getOriginX();

    public abstract int getOriginY();

    public abstract double getRangeInKm(int x, int y);

    public abstract String getPlotMode();

    public abstract double getKmEast(int x, int y);

    public abstract double getKmNorth(int x, int y);

    public abstract double getElevationInKm(int x, int y);

    public abstract int getPixelsX(double kmEast, double kmNorth);

    public abstract int getPixelsY(double kmEast, double kmNorth);

    public abstract double getAzimuthDegrees(int x, int y);

    public abstract double getElevationDegrees(int x, int y);

    /**
     * Gets the desired ray number from the specified
     * x, y location. It returns -1 if it can't find the ray.
     */
    public int getRayNumFromXY(int x, int y) {
        return -1;
    }

    /**
     * The maximum number of rays displayable. It returns
     * -1 if it can't determine the number of rays.
     */
    public int getMaxDisplayableRays() {
        return -1;
    }

    /**
     * @param elevation elevation angle
     * @param range     distance from the radar (in km)
     */
    public double getElevationInKm(double elevation, double range) {
        return Math.tan(Math.toRadians(elevation)) * range;
    }

    public long getDateAndTime() {
        return this.currentTime;
    }

    public long getBeginDaT() {
        return this.beginTime;
    }

    public double getMetersPerGate() {
        return this.metersPerGate;
    }

    public int getNumberOfGates() {
        return this.numGates;
    }

    public String getRadarName() {
        return this.radarId;
    }

    /**
     * Converts a data value into a color
     *
     * @param val the value to convert
     */
    protected Color getColorValue(final double val) {
        return this.getColorValue(val, this.getColors());
    }

    /**
     * Converts a data value into a color
     *
     * @param val    the value to convert
     * @param colors the current colortable
     */
    protected Color getColorValue(final double val, final List<Color> colors) {
        if (Double.isNaN(val)) return Color.BLACK; //missing
        if (colors == null) return Color.BLACK; //no colortable?
        ChillMomentFieldScale scale = sm.getScale(this.type);
        double min = scale.getMin();
        if (val < min) return scale.shouldLowerBoundBeClipped() ? Color.BLACK : colors.get(0);
        double max = scale.getMax();
        if (val >= max) return colors.get(colors.size() - 1);
        return colors.get((int) ((val - min) / (max - min) * (colors.size())));
    }

    protected List<Color> getColors() {
        return this.colorControl.getType(this.type);
    }

    protected void setTime(final long time) {
        if (this.beginTime < 0) this.beginTime = time;
        this.currentTime = time;
    }

    public Map<String, LimitedList<Point>> getAircraftInfo() {
        return this.aircraftInfo;
    }

    protected void addAircraftPoint(final String name, final Point loc) {
        LimitedList<Point> list = this.aircraftInfo.get(name);
        if (list == null) this.aircraftInfo.put(name, list = new LimitedList<Point>(20));
        list.add(loc);
    }

    private int avg(final int[] nums) {
        int total = 0;
        for (int i = 0; i < nums.length; ++i) total += nums[i];
        return total / nums.length;
    }

    public boolean isExportable() {
        return false;
    }

    /**
     * Write plot-type-specific information to zip stream for export
     */
    public void export(final ZipOutputStream zip) throws IOException {
    }
}
