package edu.colostate.vchill.plot;

import edu.colostate.vchill.LocationManager;
import edu.colostate.vchill.ViewUtil;
import edu.colostate.vchill.chill.ChillOldExtTrackInfo;
import edu.colostate.vchill.chill.ChillNewExtTrackInfo;
import edu.colostate.vchill.chill.ChillTrackInfo;
import edu.colostate.vchill.data.Ray;
import edu.colostate.vchill.gui.MapServerConfig;
import edu.colostate.vchill.map.MapInstruction;
import edu.colostate.vchill.map.MapInstruction.Shape;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.awt.image.BufferedImage;




// Rausch
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.*;

import java.awt.*;
import javax.swing.*;








/**
 * This class is the plotting method for PPI data of the Chill format.
 *
 * @author  Justin Carlson
 * @author  Jochen Deyke
 * @author  jpont
 * @created April 25, 2003
 * @version 2010-08-02
 */
public class ViewPlotMethodPPI extends ViewPlotMethod
{
    protected static final double piOverTwo = Math.PI / 2;
    protected static final double threePiOverTwo = 3 * piOverTwo;
    private double centerX = 0;
    private double centerY = 0;

    /**
     * Sole constructor
     */
    public ViewPlotMethodPPI (final String type)
    {
        super(type);
    }

    /**
     * This method will plot the two lines perpendicular to
     * each other that intersect the midpoint.
     *
     * @param g the graphics context to use to draw the grid to.
     *  Used in this manner to allow easy switching of where the
     *  information is actually drawn.
     */
    @Override public void plotGrid (final Graphics g)
    {
        if (g == null) return;
        g.setColor(Color.WHITE);

        int maxDistance = getPixelsFromKm(Math.max(config.getPlotRange(), 150)); //maximum range should be determined by data: gatewidth * numgates

        //Plot the Vertical line down the center of the plotting area.
        g.drawLine(this.width / 2 + offsetX,
                   offsetY - maxDistance,
                   this.width / 2 + offsetX,
                   this.height + offsetY + maxDistance);

        //Plot the Horizontal line across the middle of the plotting area.
        g.drawLine(offsetX - maxDistance,
                   this.height / 2 + offsetY,
                   this.width + offsetX + maxDistance,
                   this.height / 2 + offsetY);

        //Plot diagonals
        g.drawLine(this.width + offsetX + maxDistance,
                   offsetY - maxDistance,
                   offsetX - maxDistance,
                   this.height + offsetY + maxDistance);
        g.drawLine(offsetX - maxDistance,
                   offsetY - maxDistance,
                   this.width + offsetX + maxDistance,
                   this.height + offsetY + maxDistance);

        plotRangeRings(g);
    }

    /**
     * Draws the circles that are used to show expanding distance
     * in a PPI plot.  The rings are drawn outwards from the center
     * at intervals determined by the config.getGridSpacing() method
     * in the superclass.  This value can be set by the user.
     *
     * @param g Graphics that should be drawn to
     */
    private void plotRangeRings (final Graphics g)
    {
        int topLeftX = 1;
        int topLeftY = 1;

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int currXOffset = 0;
        int currYOffset = 0;

        int currDistance = 0;

        //While we have not plotted out to a spcified maximum distance
        //continue to draw the grid.  NOTE:  0 must not be allowed as a
        //incr for the distance, or -1.  Must be checked in the set
        //method or this infinite loops.
        while (currDistance <= Math.max(config.getPlotRange(), 150)) { //maximum range should be determined by data: gatewidth * numgates
            //This is the ammounts that must be increased pixels wise for the
            //increase in kilometers.
            currXOffset = getPixelsFromKm(currDistance);
            currYOffset = getPixelsFromKm(currDistance);

            //The circle coordinates that are determine by midpoint
            //calculations.  This should be changed to use helper functions.
            topLeftX = centerX - currXOffset;
            topLeftY = centerY - currYOffset;

            //Draw the circle part of this grid.
            g.drawOval(topLeftX + offsetX,
                       topLeftY + offsetY,
                       2 * currXOffset,
                       2 * currYOffset
                       );

            //This will draw the "distance" that each ring is currently
            //considered to be at.
            g.drawString(String.valueOf(currDistance),
                    centerX + offsetX + currXOffset + 1,
                    centerY + offsetY + 11);

            g.drawString(String.valueOf(currDistance),
                    centerX + offsetX - currXOffset + 1,
                    centerY + offsetY + 11);

            //Get the ever increasing distance to plot by adding on another
            //distance interval.
            currDistance += config.getGridSpacing();
        }
    }

    @Override public void plotMapServerOverlay(final Graphics g)
    {

    	NeedToPlotMap = true;	    

    	
    	if( MapServerConfig.userMapOverlay == "")
    	{
    		System.out.println("No layers to display");
    		
    		return;
    		
    	}
    	
	    String testString = MapServerConfig.userMapLayers;
	    	    	
	   	double BBnorth, BBsouth, BBeast, BBwest;

	   	double[] NWLatLong = ViewUtil.getDegrees(getKmFromPixels(-getCenterX()), getKmFromPixels(getCenterY()));
    	double[] SELatLong = ViewUtil.getDegrees(getKmFromPixels(-getCenterX() + this.width), getKmFromPixels(getCenterY() - this.height));
/*	    	
	    	System.out.println("W: " + NWLatLong[0]);
	    	System.out.println("N: " + NWLatLong[1]);
	    	System.out.println("E: " + SELatLong[0]);
	    	System.out.println("S: " + SELatLong[1]);    	
*/	    	

    	BBwest = NWLatLong[0];
    	BBeast = SELatLong[0];
    	BBnorth = NWLatLong[1];
    	BBsouth = SELatLong[1];
	    	    	
	    // Rausch
	    	
		Image image = null;
	    	
		try 
		{
			// Read from a URL
				
//				URL url = new URL("http://wms.chill.colostate.edu/cgi-bin/mapserv?REQUEST=GetMap&VERSION=1.1.1&SRS=epsg:4326&SERVICE=WMS&map=/var/www/html/maps/test.map&BBOX=-110,36,-100,42&WIDTH=400&HEIGHT=400&FORMAT=image/png;%20mode=24bit&LAYERS=" + layerString);//shaded_relief_natural_earth,state_boundaries,cities")
//			    URL url = new URL("http://wms.chill.colostate.edu/cgi-bin/mapserv?REQUEST=GetMap&VERSION=1.1.1&SRS=epsg:4326&SERVICE=WMS&map=/var/www/html/maps/test.map&BBOX=-110,36,-100,42&WIDTH=400&HEIGHT=400&FORMAT=image/png;%20mode=24bit&LAYERS=shaded_relief_natural_earth,state_boundaries,cities");
		    URL url = new URL("http://wms.chill.colostate.edu/cgi-bin/mapserv?REQUEST=GetMap&VERSION=1.1.1&SRS=epsg:4326&SERVICE=WMS&map=/var/www/html/maps/test.map&BBOX=" + BBwest + "," + BBsouth + "," + BBeast + "," + BBnorth + "&WIDTH=" + (this.width) + "&HEIGHT=" + (this.height) + "&FORMAT=image/png;%20mode=24bit&LAYERS=" + MapServerConfig.userMapOverlay); 

		    System.out.println("Overlay");
		    System.out.println(MapServerConfig.userMapOverlay);
				
			image = ImageIO.read(url);
		} 
		catch(Exception e)
		{
			System.out.println("Something went wrong with getting the overlay image from the MapServer");			
		}

		g.drawImage(image, 0, 0, null); 	
	        	
    }
    
    
    @Override public void plotMap (final Graphics g)
    {
		
    	
        if (g == null) return;
        if (vc.getMap() == null) return;
        g.setColor(Color.WHITE);
        Font oldFont = g.getFont();
        g.setFont(oldFont.deriveFont(9f));
        int xOffset = this.width / 2 + offsetX;
        int yOffset = this.height / 2 + offsetY;
        MapInstruction prevInstr = null;
        int radius;

        double[] oldkm = null;
        for (MapInstruction instr : vc.getMap()) {
            double[] km = ViewUtil.getKm(instr.getX(), instr.getY(), true);
            if (prevInstr != null && prevInstr.getType() == Shape.CIRCLE) { //special case
                radius = getPixelsFromKm(instr.getX()); //radius doesn't need adjustment
                g.drawOval(getPixelsFromKm(oldkm[0] + this.centerX) - radius + xOffset,
                           getPixelsFromKm(0-oldkm[1] + this.centerY) - radius + yOffset,
                           2 * radius,
                           2 * radius);
                prevInstr = instr;
            } else switch (instr.getType()) { //normal cases
                case CIRCLE: //handled in special case above
                case START_LINE: //handled by CONT_LINE
                    break;
                case CONT_LINE:
                    if (prevInstr == null || oldkm == null) return;
                    g.drawLine(getPixelsFromKm(km[0] + this.centerX) + xOffset,
                            getPixelsFromKm(0-km[1] + this.centerY) + yOffset,
                            getPixelsFromKm(oldkm[0] + this.centerX) + xOffset,
                            getPixelsFromKm(0-oldkm[1] + this.centerY) + yOffset);
                    break;
                case POINT:
                    radius = 3; //pixels
                    g.drawLine(getPixelsFromKm(km[0] + this.centerX) - radius + xOffset,
                               getPixelsFromKm(0-km[1] + this.centerY) + yOffset,
                               getPixelsFromKm(km[0] + this.centerX) + radius + xOffset,
                               getPixelsFromKm(0-km[1] + this.centerY) + yOffset);
                    g.drawLine(getPixelsFromKm(km[0] + this.centerX) + xOffset,
                               getPixelsFromKm(0-km[1] + this.centerY) - radius + yOffset,
                               getPixelsFromKm(km[0] + this.centerX) + xOffset,
                               getPixelsFromKm(0-km[1] + this.centerY) + radius + yOffset);
                    break;
                default: throw new Error("Don't know how to handle map shape " + instr.getType());
            }
            String comment = instr.getComment();
            if (comment.length() > 0 && !comment.equals("!") && !comment.startsWith("#")) {
                g.drawString(comment,
                    getPixelsFromKm(km[0] + this.centerX) + xOffset + 1,
                    getPixelsFromKm(0-km[1] + this.centerY) + yOffset + 10);
            }
            oldkm = km;
            prevInstr = instr;
        }
        g.setFont(oldFont);

    }

    @Override public void plotClickPoint (final Graphics g)
    {
        g.setColor(Color.WHITE);
        double clickX = Math.sin(Math.toRadians(clickAz)) * clickRng;
        double clickY = Math.cos(Math.toRadians(clickAz)) * clickRng;
        int x = getPixelsFromKm(clickX) + this.width / 2 + offsetX;
        int y = -getPixelsFromKm(clickY) + this.height / 2 + offsetY;
        int radius = 10; //pixels
        g.drawLine(x - radius, y - radius,
                   x + radius, y + radius);
        g.drawLine(x + radius, y - radius,
                   x - radius, y + radius);
        //Double the thickness of the lines

        x=x+1;
        
            g.drawLine(x - radius, y - radius,
                   x + radius, y + radius);
        g.drawLine(x + radius, y - radius,
                   x - radius, y + radius);
        x=x+1;
             g.drawLine(x - radius, y - radius,
                   x + radius, y + radius);
        g.drawLine(x + radius, y - radius,
                   x - radius, y + radius);

    }

    @Override protected void plotAircraft (final ChillTrackInfo loc)
    {
        addAircraftPoint(loc.ident, new Point(
                getOriginX() + getPixelsFromKm(loc.xKm),
                getOriginY() - getPixelsFromKm(loc.yKm)));
        System.out.println(loc.toString());
    }
    
    @Override protected void plotAircraft (final ChillOldExtTrackInfo coeti)
    {
        addAircraftPoint(coeti.trackID, new Point(
                getOriginX() + getPixelsFromKm(coeti.posX),
                getOriginY() - getPixelsFromKm(coeti.posY)));
        System.out.println(coeti.toString());
    }

    @Override protected void plotAircraft (final ChillNewExtTrackInfo cneti)
    {
        addAircraftPoint(cneti.trackID, new Point(
                getOriginX() + getPixelsFromKm(cneti.posX),
                getOriginY() - getPixelsFromKm(cneti.posY)));
        System.out.println(cneti.toString());
    }

    @Override protected double getStartAngle (final Ray currRay)
    {
        return Math.toRadians(currRay.getStartAzimuth());
    }

    @Override protected double getEndAngle (final Ray currRay)
    {
        return Math.toRadians(currRay.getEndAzimuth());
    }

    @Override protected int getX (final Angle angle, final double offset)
    {
        return getCenterX() + (int)(offset * angle.getSin());
    }

    @Override protected int getY (final Angle angle, final double offset, final int xPos)
    {
        return getCenterY() - (int)(offset * angle.getCos());
    }

    @Override protected boolean outOfRange (final int[] xVals, final int[] yVals)
    {
        assert xVals.length == yVals.length;
        double maxDistance = config.getPlotRange();
        for (int i = 0; i < xVals.length; ++i) {
            if (getRangeInKm(xVals[i], yVals[i]) > maxDistance) break;
            return false;
        }
        return true;
    }

    @Override protected boolean outOfRange (final double startAngle, final double endAngle)
    {
        if (getCenterY() < 0) {
           if ((startAngle < piOverTwo || startAngle > threePiOverTwo) &&
               (endAngle < piOverTwo || endAngle > threePiOverTwo)) return true;
        } else if (getCenterY() > this.height) {
            if ((startAngle > piOverTwo && startAngle < threePiOverTwo) &&
                (endAngle > piOverTwo && endAngle < threePiOverTwo)) return true;
        }

        if (getCenterX() < 0) {
            if (startAngle > Math.PI && endAngle > Math.PI) return true;
        } else if (getCenterX() > this.width) {
            if (startAngle < Math.PI && endAngle < Math.PI) return true;
        }

        return false;
    }

    @Override public int getOriginX () {
        return getCenterX(); }
    @Override public int getOriginY () {
        return getCenterY(); }

    /**
     * This method translates a kilometer distance into the logical number
     * of pixels determined by the current zoom factor and pixel weight.
     *
     * @param distance The kilometer value to find a pixel distance for.
     * @return The number of pixels for this km distance.
     */
    @Override protected int getPixelsFromKm (final double distance)
    {
        return (int)((Math.min(this.width, this.height) * distance * 1e3) / (config.getPlotRange() * 2048));
    }

    /**
     * This method takes a distance in pixels, and translates it into a
     * logical kilometer distance.
     *
     * @param numPixels The distance in pixels to be converted.
     * @return The Kilometers that this pixel value represents.
     */
    @Override protected double getKmFromPixels (final int numPixels)
    {
        return (config.getPlotRange() * numPixels * 2048) / (1e3 * Math.min(this.width, this.height));
    }

    @Override public double getRangeInKm (final int x, final int y)
    {
        double deltaX = x - getOriginX();
        double deltaY = y - getOriginY();
        double distX = deltaX * deltaX;
        double distY = deltaY * deltaY;
        return getKmFromPixels((int)Math.sqrt(distX + distY));
    }

    @Override public double getKmEast (final int x, final int y)
    {
        return getKmFromPixels(x - getOriginX());
    }

    @Override public int getPixelsX (final double kmEast, final double kmNorth)
    {
        return getPixelsFromKm(kmEast);
    }

    @Override public double getKmNorth (final int x, final int y)
    {
        return getKmFromPixels(getOriginY() - y);
    }

    @Override public double getAzimuthDegrees (final int x, final int y)
    {
        double n = -getKmNorth(x, y);
        double e = getKmEast(x, y);
        return Math.toDegrees(Math.atan(n / e)) + (e < 0 ? 270 : 90);
    }

    @Override public double getElevationDegrees (final int x, final int y)
    {
		double azimuth = getAzimuthDegrees( x, y );
		Ray ray = vc.getRayAtAz( this.type, azimuth );
        if( ray == null )
			return this.radarElevation;
		else
			return ray.getStartElevation();
    }

    @Override public int getPixelsY (final double kmEast, final double kmNorth)
    {
        return getPixelsFromKm(kmNorth);
    }

    @Override public double getElevationInKm (final int x, final int y)
    {
        double range = getRangeInKm(x, y);
        double elevation = Math.toRadians(getRadarElevation());
        return (Math.tan(elevation) * range);
    }

    @Override public double getElevationInKm (final double elevation, final double range)
    {
        return ViewUtil.getKmElevation(elevation, range);
    }
 
    @Override public String getPlotMode () {
        return "PPI"; }

    private double[] getNSEWDegrees ()
    {
        double n = ViewUtil.getDegrees(0, (0 - this.getCenterY() > 0 ? -1 : 1 ) *
            this.getRangeInKm(this.getCenterX(), 0))[1];
        double s = ViewUtil.getDegrees(0, (this.height - this.getCenterY() > 0 ? -1 : 1 ) *
            this.getRangeInKm(this.getCenterX(), this.height))[1];
        double e = ViewUtil.getDegrees((this.width - this.getCenterX() > 0 ? 1 : -1 ) *
            this.getRangeInKm(this.width, this.getCenterY()), 0)[0];
        double w = ViewUtil.getDegrees((0 - this.getCenterX() > 0 ? 1 : -1 ) *
            this.getRangeInKm(0, this.getCenterY()), 0)[0];
        return new double[] {n, s, e, w};
    }


    @Override public boolean isExportable () { return true; }

    @Override public void export (final ZipOutputStream zip) throws IOException
    {
        //write kml
        zip.putNextEntry(new ZipEntry("vchillppi.kml"));
        OutputStreamWriter out;
        try { out = new OutputStreamWriter(zip, "UTF-8"); }
        catch (UnsupportedEncodingException uee) { out = new OutputStreamWriter(zip); }
        double[] nsew = this.getNSEWDegrees();
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                  "<kml xmlns=\"http://earth.google.com/kml/2.1\">\n" +
                  "<Folder>\n" +
                  "    <TimeSpan>\n" +
                  "        <begin>" + df.format(this.getBeginDaT()) + "</begin>\n" +
                  "        <end>" + df.format(this.getDateAndTime()) + "</end>\n" +
                  "    </TimeSpan>\n" +
                  "    <name>" + this.getRadarName() + " Radar</name>\n" +
                  "    <Placemark>\n" +
                  "        <description><![CDATA[Created with <a href=\"http://chill.colostate.edu/java/\">Java VCHILL</a>]]></description>\n" +
                  "        <name>" + this.getRadarName() + " Radar Facility</name>\n" +
                  "        <LookAt>\n" +
                  "            <longitude>" + lm.getLongitude() + "</longitude>\n" +
                  "            <latitude>" + lm.getLatitude() + "</latitude>\n" +
                  "            <range>500.0</range>\n" +
                  "            <tilt>0</tilt>\n" +
                  "            <heading>3</heading>\n" +
                  "        </LookAt>\n" +
                  "        <Point>\n" +
                  "            <coordinates>" + lm.getLongitude() + "," + lm.getLatitude() + ",0</coordinates>\n" +
                  "        </Point>\n" +
                  "    </Placemark>\n" +
                  "    <ScreenOverlay>\n" +
                  "        <name>Legend</name>\n" +
                  "        <Icon>\n" +
                  "            <href>vchillbar.png</href>\n" +
                  "        </Icon>\n" +
                  "        <overlayXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
                  "        <screenXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
                  "    </ScreenOverlay>\n" +
                  "    <GroundOverlay>\n" +
                  "        <name>Radar data</name>\n" +
                  "        <color>9effffff</color>\n" +
                  "        <drawOrder>1</drawOrder>\n" +
                  "        <Icon>\n" +
                  "            <href>vchillimg.png</href>\n" +
                  //"            <viewBoundScale>0.75</viewBoundScale>\n" +
                  "        </Icon>\n" +
                  "        <LatLonBox>\n" +
                  "            <north>" + nsew[0] + "</north>\n" +
                  "            <south>" + nsew[1] + "</south>\n" +
                  "            <east>" + nsew[2] + "</east>\n" +
                  "            <west>" + nsew[3] + "</west>\n" +
                  "            <rotation>0</rotation>\n" +
                  "        </LatLonBox>\n" +
                  "    </GroundOverlay> \n" +
                  "</Folder>\n" +
                  "</kml>\n");
        out.flush();
        zip.closeEntry();
    }
}
