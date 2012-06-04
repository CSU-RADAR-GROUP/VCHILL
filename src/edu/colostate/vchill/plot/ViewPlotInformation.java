package edu.colostate.vchill.plot;

import edu.colostate.vchill.color.Config;
import java.awt.Color;
import java.awt.Graphics;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.swing.JPanel;

/**
 * This class handles all the String plotting on the display.
 *
 * @author  Justin Carlson
 * @author  Jochen Deyke
 * @author  jpont
 * @created June 29, 2003
 * @version 2009-06-29
 */
public class ViewPlotInformation extends JPanel
{
    protected final static Config config = Config.getInstance();
    protected final SimpleDateFormat formatter;
    protected final static TimeZone utc = TimeZone.getTimeZone("UTC");

    //These are used to describe information about the current plot.
    private String fieldName = "";
    private String plotType = "";
    private String gateInfo = "";

    private String radarName = "";

    private String azimuth = "";
    private String elevation = "";

    //A String that will be used to describe what each of the data
    //values the colors currently represent.
    private List<String> annotationString;

    //Mouse clicks will update this information;
    private String azimuthAtLocation = "";
    private String elevationAtLocation = "";
    private String rangeAtLocation = "";
    private String dataAtLocation = "";
    private String heightAtLocation = "";

    //A String to display the current date and time.
    private Date date;
    private String dateStr = "";
    private String timeStr = "";

    private List<Color> colors;

    /**
     * Constructor for the ViewPlotInformation object
     */
    public ViewPlotInformation ()
    {
        this.formatter = new SimpleDateFormat();
        this.formatter.setTimeZone(utc);
        this.date = new Date();
    }

    /**
     * This will plot all of the different strings onto the display.
     *
     * @param g Graphics reference to where the strings should be plotted.
     */
    @Override public void paintComponent (final Graphics g)
    {
        //Paint the entire background Black.
        g.setColor(Color.BLACK);
        g.fillRect(0,
                   0,
                   getWidth(),
                   getHeight());

        paintColorBar(g);

        g.setColor(Color.WHITE);

        //g.setFont(new Font(String type, int style, int size));
        //g.drawString(String str, int x, int y)

        int xAlignment = getWidth() / 32;
        int yAlignment = 0;
        double yStepSize = (getTextBlockSizeY() * 3) / 4;
        if (getHeight() < 480) yStepSize *= 1.5;

        yAlignment += yStepSize;
        g.drawString(this.fieldName, xAlignment, yAlignment);

        yAlignment += yStepSize;
        g.drawString(this.plotType, xAlignment, yAlignment);

        //Plots the information dealing with the color to value data hashing intervals.
        drawLabels(g);

        yAlignment = (getHeight() * 2) / 3 - (int)yStepSize;
        yAlignment += getTextBlockSizeY();

        g.drawString(this.radarName, xAlignment, yAlignment);
        yAlignment += yStepSize;
        g.drawString(this.elevation, xAlignment, yAlignment);
        yAlignment += yStepSize;
        g.drawString(this.azimuth, xAlignment, yAlignment);
        yAlignment += yStepSize;
        g.drawString(this.dateStr, xAlignment, yAlignment);
        yAlignment += yStepSize;
        g.drawString(this.timeStr, xAlignment, yAlignment);
        yAlignment += yStepSize;
        g.drawString(this.gateInfo, xAlignment, yAlignment);

        if (getHeight() < 480) return;
        yAlignment += 3 * yStepSize;

        g.drawString("            Marker: ", xAlignment, yAlignment);
        yAlignment += yStepSize;
        g.drawString(this.elevationAtLocation, xAlignment, yAlignment);
        yAlignment += yStepSize;
        g.drawString(this.azimuthAtLocation, xAlignment, yAlignment);
        yAlignment += yStepSize;
        g.drawString(this.rangeAtLocation, xAlignment, yAlignment);
        yAlignment += yStepSize;
        g.drawString(this.heightAtLocation, xAlignment, yAlignment);
        yAlignment += yStepSize;
        g.drawString(this.dataAtLocation, xAlignment, yAlignment);
    }

    /**
     * This method  will simply use the set height and width
     * functions to then plot a color bar along with a
     * starting coordinate offset.  These methods are set elsewhere
     * and need to be updated correctly to keep the bar in the same place.
     *
     * @param g Description of the Parameter
     */
    private void paintColorBar (final Graphics g)
    {
        if (this.colors == null) return;

        int xLocation = getWidth() / 5;
        double yLocation = getHeight() / 10.5;

        for (int i = this.colors.size(); i > 0; --i) {
            g.setColor(this.colors.get(i - 1));

            g.fillRect(
                    xLocation, //start x
                    (int)yLocation, //start y
                    getBlockSizeX(), //width
                    (int)getBlockSizeY() + 1//height
                    );

            if (config.isOutlineColorsEnabled()) {
                g.setColor(Color.WHITE);
                g.drawRect(
                        xLocation, //start x
                        (int)yLocation, //start y
                        getBlockSizeX(), //width
                        (int)getBlockSizeY() + 1//height
                        );
            }

            //Increment y location of the bar in order to plot the next
            //block of color of the annotation.
            yLocation += getBlockSizeY();
        }
    }

    /**
     * This will draw the strings that deal with different color values.
     *
     * @param g Description of the Parameter
     */
    private void drawLabels (final Graphics g)
    {
		if( this.annotationString == null )
			return;

        for (int i = 0; i < this.annotationString.size(); ++i) {
            g.drawString(this.annotationString.get(i),
                getWidth() / 3,
                (int)(i * this.getTextBlockSizeY() * 16 / (this.annotationString.size() - 1) + (getHeight() / 10.0)));
        }
    }

    private int getBlockSizeX ()
    {
        return getWidth() / 10;
    }

    private double getBlockSizeY ()
    {
		if( this.colors != null )
			return getHeight() * 16 / 30.0 / this.colors.size();
		else
			return getHeight() * 16 / 30.0 / 16; //assume a default of 16 colors
    }

    private double getTextBlockSizeY ()
    {
        return getHeight() / 30.0;
    }

    /**
     * @param colors A non-null list of colors.  Must have &gt;0 entries.
     */
    public void setColors (final List<Color> colors)
    {
        this.colors = colors;
    }

    public void setRadarName (final String radarName) {
        this.radarName = "Radar : " + radarName; }

    public void setGateInfo (final String gateInfo) {
        this.gateInfo = "Gates : " + gateInfo; }

    public void setType (final String plotType) {
        this.plotType = "Plot Type : " + plotType; }

    public void setDataKey (final String fieldName) {
        this.fieldName = fieldName; }

    public void setAnnotationString (final List<String> values) {
        this.annotationString = values; }

    public void setAzimuth (final String azimuth) {
        this.azimuth = "Azimuth : " + azimuth + "\u00b0"; }

    public void setClickElevation (final String elevation) {
        this.elevationAtLocation = "Elevation : " + elevation + "\u00b0"; }

    public void setClickAzimuth (final String azimuth) {
        this.azimuthAtLocation = "Azimuth : " + azimuth + "\u00b0"; }

    public void setClickRange (final String range) {
        this.rangeAtLocation = "Range : " + range + "km"; }

    public void setClickHeight (final String km, final String kft) {
        this.heightAtLocation = "Height : " + km + "km (" + kft + "kft)"; }

    public void setDataInfo (final String data) {
        this.dataAtLocation = "Value : " + data; }

    public void setElevation (final String elevation) {
        this.elevation = "Elevation : " + elevation + "\u00b0"; }

    /**
     * @param time milliseconds since 1970
     */
    public void setDateAndTime (final long time)
    {
        this.date.setTime(time);
        this.formatter.applyPattern("EEE d MMM yy");
        this.dateStr  = "Date : " + this.formatter.format(date);
        this.formatter.applyPattern("HH:mm:ss 'UTC'");
        this.timeStr  = "Time : " + this.formatter.format(date);
    }
}
