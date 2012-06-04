package edu.colostate.vchill.color;

import edu.colostate.vchill.ConfigUtil;

/**
 * This class provides configuration settings specific to the color module.
 * All exceptions/errors encountered while processing data are simply
 * ignored.
 *
 * @author  Jochen Deyke
 * @author  jpont
 * @version 2009-06-01
 */
public class Config
{
    private static final Config me = new Config();

    private volatile String colorFileName;
    private volatile boolean interpolateColors;
    private volatile boolean outlineColors;
    private volatile int numColors;

    /** Private default constructor prevents instantiation */
    private Config ()
    {
        this.colorFileName = ConfigUtil.getString("Colors", XMLControl.getListOfFiles().get(0));
        this.interpolateColors = ConfigUtil.getBoolean("Interpolate Colors", false);
        this.outlineColors = ConfigUtil.getBoolean("Outline Colors", true);
        this.numColors = ConfigUtil.getInt("Number of Colors", 254);
    }

    public static Config getInstance () { return me; }

    public synchronized void setColorFileName (final String colorFileName) {
        ConfigUtil.put("Colors", this.colorFileName = colorFileName); }
    public synchronized String getColorFileName () { return this.colorFileName; }

    public synchronized void setInterpolateColorsEnabled (final boolean interpolateColors) {
        ConfigUtil.put("Interpolate Colors", this.interpolateColors = interpolateColors); }
    public synchronized void toggleInterpolateColorsEnabled () {
        this.setInterpolateColorsEnabled(!this.interpolateColors); }
    public synchronized boolean isInterpolateColorsEnabled () { return this.interpolateColors; }

    public synchronized void setOutlineColorsEnabled (final boolean outlineColors) {
        ConfigUtil.put("Outline Colors", this.outlineColors = outlineColors); }
    public synchronized void toggleOutlineColorsEnabled () {
        this.setOutlineColorsEnabled(!this.outlineColors); }
    public synchronized boolean isOutlineColorsEnabled () { return this.outlineColors; }

    public synchronized void setNumberOfColors (final int numColors) {
        ConfigUtil.put("Number of Colors", this.numColors = numColors); }
    public synchronized int getNumberOfColors () { return this.numColors; }
}
