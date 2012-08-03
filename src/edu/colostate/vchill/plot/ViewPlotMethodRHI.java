package edu.colostate.vchill.plot;

import edu.colostate.vchill.ViewUtil;
import edu.colostate.vchill.chill.ChillOldExtTrackInfo;
import edu.colostate.vchill.chill.ChillNewExtTrackInfo;
import edu.colostate.vchill.chill.ChillTrackInfo;
import edu.colostate.vchill.data.Ray;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class will be used to do all of the RHI plotting
 * That is required by the window.  It also draws RHI grids
 * and handles information like mouse clicks to information.
 *
 * @author  Justin Carlson
 * @author  Jochen Deyke
 * @author  jpont
 * @created June 2, 2003
 * @version 2010-08-02
 */
class ViewPlotMethodRHI extends ViewPlotMethod
{
    private static final int startOffset = 16;

    public ViewPlotMethodRHI (final String type)
    {
        super(type);
        this.Mappable = false;
    }

    /**
     * When passed a Graphics object, this method will then plot an
     * expanding grid that represents the distance at each pixel.
     * The major superclass methods are getPixelsFromKm which
     * uses the gate width and zoom level to determine a distance value
     * for each pixel.  Then the grid is plotted expanding outwards from
     * the center at an interval determined by the config.getPlotRange() function
     * which returns km.  This grid is drawn with lines and the idea is to
     * start with the x,y 0,0 coordinates at the bottom left.
     *
     * @param g Description of the Parameter
     */
    @Override public void plotGrid (final Graphics g)
    {
        if (g == null) return;
        g.setColor(Color.WHITE);

        //Used to represent how many km have been plotting representing
        //distance away and elevation markers.

        //Start the x,y location at a lower right corner offset by the
        //panning info. with a starting offset such that the bottom and
        //side are visible.
        int startX = getOriginX();
        int startY = getOriginY();

        //While we haven't yet plotted out to the maximum possible distance,
        //for the x term, continue to do so.  This may be better served with
        //the y distance but will result in a lot of wasted lines that are
        //WAY off the screen, though if a user were to pan up a lot, this
        //would be useful.
        for (int currDist = 0; //Vertical Lines.
             currDist <= config.getPlotRange();
             currDist += config.getGridSpacing()) {
            int x = startX + getPixelsFromKmX(currDist);
            g.drawLine(x, startY - getPixelsFromKmY(config.getMaxPlotHeight()),
                       x, startY);

            //Label the line
            g.drawString(String.valueOf(currDist), x, startY + 15);
        }

        int maxX = startX + getPixelsFromKmX(config.getPlotRange());
        for (int currDist = 0; //Horizontal Lines.
             currDist <= config.getMaxPlotHeight();
             currDist += config.getRHIHVFactor() > 3 ? 1 : 2) {
            int y = startY - getPixelsFromKmY(currDist);
            g.drawLine(startX, y, maxX, y);

            //Label the line
            g.drawString(String.valueOf(currDist), startX - 15, y);
        }
    }

    @Override public void plotClickPoint (final Graphics g)
    {
        if (g == null) return;
        g.setColor(Color.WHITE);
        int radius = 10; //pixels
        int range = getPixelsFromKmX(clickRng) + startOffset + offsetX;
        double clickZ = Math.tan(Math.toRadians(clickEl)) * clickRng;
        int height = -getPixelsFromKmY(clickZ) + this.height - startOffset + offsetY;
        g.drawLine(range - radius, height - radius,
                   range + radius, height + radius);
        g.drawLine(range + radius, height - radius,
                   range - radius, height + radius);
        range++;
        g.drawLine(range - radius, height - radius,
                   range + radius, height + radius);
        g.drawLine(range + radius, height - radius,
                   range - radius, height + radius);
        range++;
        g.drawLine(range - radius, height - radius,
                   range + radius, height + radius);
        g.drawLine(range + radius, height - radius,
                   range - radius, height + radius);
    }

    @Override protected void plotAircraft (final ChillTrackInfo loc)
    {
        addAircraftPoint(loc.ident, new Point(
                getOriginX() + getPixelsFromKmX(Math.sqrt(loc.xKm * loc.xKm + loc.yKm * loc.yKm)),
                getOriginY() - getPixelsFromKmY(loc.altKm)));
        System.out.println(loc.toString());
    }
    
    @Override protected void plotAircraft (final ChillOldExtTrackInfo coeti)
    {
        addAircraftPoint(coeti.trackID, new Point(
                getOriginX() + getPixelsFromKmX(Math.sqrt(coeti.posX * coeti.posX + coeti.posY * coeti.posY)),
                getOriginY() - getPixelsFromKmY(coeti.altitudeM * 1e-3)));
        System.out.println(coeti.toString());
    }

    @Override protected void plotAircraft (final ChillNewExtTrackInfo cneti)
    {
        addAircraftPoint(cneti.trackID, new Point(
                getOriginX() + getPixelsFromKmX(Math.sqrt(cneti.posX * cneti.posX + cneti.posY * cneti.posY)),
                getOriginY() - getPixelsFromKmY(cneti.altitudeM * 1e-3)));
        System.out.println(cneti.toString());
    }

    @Override protected double getStartAngle (final Ray currRay)
    {
        double startDegs = currRay.getStartElevation();
        if (startDegs > 180) startDegs -= 360;
        return Math.toRadians(startDegs);
    }

    @Override protected double getEndAngle (final Ray currRay)
    {
        double endDegs = currRay.getEndElevation();
        if (endDegs > 180) endDegs -= 360;
            return Math.toRadians(endDegs);
    }

    @Override protected int getX (final Angle angle, final double offset)
    {
        return getOriginX() + (int)(offset * angle.getCos());
    }

    @Override protected int getY (final Angle angle, final double offset, final int xPos)
    {
        int yPos = (int)(25 * this.height * config.getRHIHVFactor() * offset * angle.getSin() / (config.getMaxPlotHeight() * this.width));
        return getOriginY() - getPixelsFromKmY(getKmFromPixelsY(yPos) + ViewUtil.getKmElevation(angle.getAngle(), getKmFromPixelsX(xPos)));
    }

    @Override protected boolean outOfRange (final int[] xVals, final int[] yVals)
    {
        assert xVals.length == yVals.length;
        double maxDistance = config.getPlotRange();
        double maxHeight = config.getMaxPlotHeight();
        for (int i = 0; i < xVals.length; ++i) {
            if (getKmFromPixelsX(xVals[i]) > maxDistance &&
                getKmFromPixelsY(this.height - yVals[i]) > maxHeight) continue;
            return false;
        }
        return true;
    }

    @Override public int getOriginX ()
    {
        return startOffset + offsetX;
    }

    @Override public int getOriginY ()
    {
        return (this.height - startOffset) + offsetY;
    }

    @Override public double getRangeInKm (final int x, final int y)
    {
        return getKmFromPixelsX(Math.abs(getOriginX() - x));
    }

    @Override public double getElevationInKm (final int x, final int y)
    {
        return getKmFromPixelsY(Math.abs(getOriginY() - y));
    }

    @Override public String getPlotMode ()
    {
        return "RHI";
    }

    @Override protected int getPixelsFromKm (final double distance)
    {
        return this.getPixelsFromKmX(distance);
    }

    @Override protected double getKmFromPixels (final int distance)
    {
        return this.getKmFromPixelsX(distance);
    }

    /**
     * @param distance the distance in km that needs to be found from pixels.
     * @return The number of pixels that represent this dist in km.
     */
    private int getPixelsFromKmX (final double distance)
    {
        return (int)((this.width * distance * 1e3) / (config.getPlotRange() * 1024));
    }

    /**
     * @param distance The distance in km that is to be translated to pixels.
     * @return This returns the pixels * scaling.
     */
    private int getPixelsFromKmY (final double distance)
    {
        return (int)((this.height * 150 * config.getRHIHVFactor() * distance * 1e3) /
                (config.getMaxPlotHeight() * config.getPlotRange() * 1024 * 3 * 2));
    }

    private double getKmFromPixelsX (final int numPixels)
    {
        return (config.getPlotRange() * numPixels * 1024) /
                (1e3 * this.width);
    }

    private double getKmFromPixelsY (final int numPixels)
    {
        return (config.getMaxPlotHeight() * numPixels * config.getPlotRange() * 1024 * 3 * 2) /
                (1e3 * this.height * 150 * config.getRHIHVFactor());
    }

    @Override public double getKmEast (final int x, final int y)
    {
        return getKmFromPixelsX(x - getOriginX()) * Math.sin(Math.toRadians(radarAzimuth));
    }

    @Override public double getKmNorth (final int x, final int y)
    {
        return getKmFromPixelsX(x - getOriginX()) * Math.cos(Math.toRadians(radarAzimuth));
    }

    @Override public double getAzimuthDegrees (final int x, final int y)
    {
		double elevation = getElevationDegrees( x, y );
		Ray ray = vc.getRayAtEl( this.type, elevation );
        if( ray == null )
			return this.radarAzimuth;
		else
			return ray.getStartAzimuth();
    }

    @Override public double getElevationDegrees (final int x, final int y)
    {
        double u = getElevationInKm(x, y);
        double r = getRangeInKm(x, y);
        return Math.toDegrees(Math.atan(u / r));
    }

    @Override public int getPixelsX (final double kmEast, final double kmNorth)
    {
        //return getPixelsFromKmX(Math.sqrt(kmEast*kmEast + kmNorth*kmNorth)) * (kmEast > 0 ? 1 : -1);
        return 0;
    }

    @Override public int getPixelsY (final double kmEast, final double kmNorth)
    {
        return 0;
    }

    /**
     * based on width (x) only
     */
    @Override public double getPlotStepSize ()
    {
        return (1024 * config.getPlotRange()) / (metersPerGate * this.width);
    }

    @Override public boolean isExportable () { return true; }

    @Override public void export (final ZipOutputStream zip) throws IOException
    {
        OutputStreamWriter out;

        //write kml
        double hypotenuse = config.getPlotRange() / 2;
        double radians = Math.toRadians(radarAzimuth + 90);
        double x = hypotenuse * Math.cos(radians);
        double y = hypotenuse * Math.sin(radians);
        double[] degrees = ViewUtil.getDegrees(x, y);
        zip.putNextEntry(new ZipEntry("vchillrhi.kml"));
        try { out = new OutputStreamWriter(zip, "UTF-8"); }
        catch (UnsupportedEncodingException uee) { out = new OutputStreamWriter(zip); }
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                  "<kml xmlns=\"http://earth.google.com/kml/2.1\">\n" +
                  "<Folder>\n" +
                  "    <LookAt>\n" +
                  "        <longitude>" + degrees[0] + "</longitude>\n" +
                  "        <latitude>" + degrees[1] + "</latitude>\n" +
                  "        <range>" + 2 * config.getMaxPlotRange() * 1000 + "</range>\n" +
                  "        <tilt>75</tilt>\n" +
                  "        <heading>" + (radarAzimuth - 90) + "</heading>\n" +
                  "    </LookAt>\n" +
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
                  "            <heading>0</heading>\n" +
                  "        </LookAt>\n" +
                  "        <Point>\n" +
                  "            <coordinates>" + lm.getLongitude() + "," + lm.getLatitude() + ",0</coordinates>\n" +
                  "        </Point>\n" +
                  "    </Placemark>\n" +
                  "    <Placemark>\n" +
                  "        <name>Radar Data</name>\n" +
                  "        <LookAt>\n" +
                  "            <longitude>" + degrees[0] + "</longitude>\n" +
                  "            <latitude>" + degrees[1] + "</latitude>\n" +
                  "            <range>" + 2 * config.getMaxPlotRange() * 1000 + "</range>\n" +
                  "            <tilt>75</tilt>\n" +
                  "            <heading>" + (radarAzimuth - 90) + "</heading>\n" +
                  "        </LookAt>\n" +
                  "        <Model>\n" +
                  "            <altitudeMode>relativeToGround</altitudeMode>\n" +
                  "            <Location>\n" +
                  "                <longitude>" + lm.getLongitude() + "</longitude>\n" +
                  "                <latitude>" + lm.getLatitude() + "</latitude>\n" +
                  "                <altitude>0.0</altitude>\n" +
                  "            </Location>\n" +
                  "            <Orientation>\n" +
                  "                <heading>" + (radarAzimuth - 90) + "</heading>\n" +
                  "                <tilt>0</tilt>\n" +
                  "                <roll>0</roll>\n" +
                  "            </Orientation>\n" +
                  "            <Scale>\n" +
                  "                <x>1.0</x>\n" +
                  "                <y>1.0</y>\n" +
                  "                <z>1.0</z>\n" +
                  "            </Scale>\n" +
                  "            <Link>\n" +
                  "                <href>rhiscan.dae</href>\n" +
                  "            </Link>\n" +
                  "            </Model>\n" +
                  "    </Placemark>\n" +
                  "    <ScreenOverlay>\n" +
                  "        <name>Legend</name>\n" +
                  "        <Icon>\n" +
                  "            <href>vchillbar.png</href>\n" +
                  "        </Icon>\n" +
                  "        <overlayXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
                  "        <screenXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
                  "    </ScreenOverlay>\n" +
                  "</Folder>\n" +
                  "</kml>\n");
        out.flush();
        zip.closeEntry();

        /*  Not needed?
        //write textures
        zip.putNextEntry(new ZipEntry("textures.txt"));
        try { out = new OutputStreamWriter(zip, "UTF-8"); }
        catch (UnsupportedEncodingException uee) { out = new OutputStreamWriter(zip); }
        out.write("<vchillimg.png> <vchillimg.png>\n");
        out.flush();
        zip.closeEntry();
        */

        //write model
        double startX = 0 - getKmFromPixelsX(getOriginX());
        double endX = getKmFromPixelsX(width - getOriginX());
        double startY = 0 - getKmFromPixelsY(height - getOriginY());
        double endY = getKmFromPixelsY(getOriginY());
        zip.putNextEntry(new ZipEntry("rhiscan.dae"));
        try { out = new OutputStreamWriter(zip, "UTF-8"); }
        catch (UnsupportedEncodingException uee) { out = new OutputStreamWriter(zip); }
        out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                  "<COLLADA xmlns=\"http://www.collada.org/2005/11/COLLADASchema\" version=\"1.4.1\">\n" +
                  "   <asset>\n" +
                  "      <unit name=\"km\" meter=\"1000\"/>\n" +
                  "      <up_axis>Z_UP</up_axis>\n" +
                  "   </asset>\n" +
                  "   <library_images>\n" +
                  "      <image id=\"vchillimg-image\" name=\"vchillimg-image\">\n" +
                  "         <init_from>vchillimg.png</init_from>\n" +
                  "      </image>\n" +
                  "   </library_images>\n" +
                  "   <library_materials>\n" +
                  "      <material id=\"vchillimgID\" name=\"vchillimg\">\n" +
                  "         <instance_effect url=\"#vchillimg-effect\"/>\n" +
                  "      </material>\n" +
                  "   </library_materials>\n" +
                  "   <library_effects>\n" +
                  "      <effect id=\"vchillimg-effect\" name=\"vchillimg-effect\">\n" +
                  "         <profile_COMMON>\n" +
                  "            <newparam sid=\"vchillimg-image-surface\">\n" +
                  "               <surface type=\"2D\">\n" +
                  "                  <init_from>vchillimg-image</init_from>\n" +
                  "               </surface>\n" +
                  "            </newparam>\n" +
                  "            <newparam sid=\"vchillimg-image-sampler\">\n" +
                  "               <sampler2D>\n" +
                  "                  <source>vchillimg-image-surface</source>\n" +
                  "               </sampler2D>\n" +
                  "            </newparam>\n" +
                  "            <technique sid=\"COMMON\">\n" +
                  "               <phong>\n" +
                  "                  <emission>\n" +
                  "                     <color>0.0 0.0 0.0 1</color>\n" +
                  "                  </emission>\n" +
                  "                  <ambient>\n" +
                  "                     <color>0.0 0.0 0.0 1</color>\n" +
                  "                  </ambient>\n" +
                  "                  <diffuse>\n" +
                  "                     <texture texture=\"vchillimg-image-sampler\" texcoord=\"UVSET0\"/>\n" +
                  "                  </diffuse>\n" +
                  "                  <specular>\n" +
                  "                     <color>0.0 0.0 0.0 1</color>\n" +
                  "                  </specular>\n" +
                  "                  <shininess>\n" +
                  "                     <float>20.0</float>\n" +
                  "                  </shininess>\n" +
                  "                  <reflectivity>\n" +
                  "                     <float>0.1</float>\n" +
                  "                  </reflectivity>\n" +
                  "                  <transparent>\n" +
                  "                     <color>1 1 1 1</color>\n" +
                  "                  </transparent>\n" +
                  "                  <transparency>\n" +
                  "                     <float>0.0</float>\n" +
                  "                  </transparency>\n" +
                  "               </phong>\n" +
                  "            </technique>\n" +
                  "         </profile_COMMON>\n" +
                  "      </effect>\n" +
                  "   </library_effects>\n" +
                  "   <library_geometries>\n" +
                  "      <geometry id=\"mesh1-geometry\" name=\"mesh1-geometry\">\n" +
                  "         <mesh>\n" +
                  "            <source id=\"mesh1-geometry-position\">\n" +
                  "                <float_array id=\"mesh1-geometry-position-array\" count=\"12\">\n" +
                  "                    " + startX + "\n" +
                  "                    0.0\n" +
                  "                    " + startY + "\n" +
                  "\n" +
                  "                    " + endX + "\n" +
                  "                    0.0\n" +
                  "                    " + startY + "\n" +
                  "\n" +
                  "                    " + startX + "\n" +
                  "                    0.0\n" +
                  "                    " + endY + "\n" +
                  "\n" +
                  "                    " + endX + "\n" +
                  "                    0.0\n" +
                  "                    " + endY + "\n" +
                  "                </float_array>\n" +
                  "               <technique_common>\n" +
                  "                  <accessor source=\"#mesh1-geometry-position-array\" count=\"4\" stride=\"3\">\n" +
                  "                     <param name=\"X\" type=\"float\"/>\n" +
                  "                     <param name=\"Y\" type=\"float\"/>\n" +
                  "                     <param name=\"Z\" type=\"float\"/>\n" +
                  "                  </accessor>\n" +
                  "               </technique_common>\n" +
                  "            </source>\n" +
                  "            <source id=\"mesh1-geometry-normal\">\n" +
                  "               <float_array id=\"mesh1-geometry-normal-array\" count=\"6\">0.0 -1.0 0.0 0.0 1.0 0.0 </float_array>\n" +
                  "               <technique_common>\n" +
                  "                  <accessor source=\"#mesh1-geometry-normal-array\" count=\"2\" stride=\"3\">\n" +
                  "                     <param name=\"X\" type=\"float\"/>\n" +
                  "                     <param name=\"Y\" type=\"float\"/>\n" +
                  "                     <param name=\"Z\" type=\"float\"/>\n" +
                  "                  </accessor>\n" +
                  "               </technique_common>\n" +
                  "            </source>\n" +
                  "            <source id=\"mesh1-geometry-uv\">\n" +
                  "               <float_array id=\"mesh1-geometry-uv-array\" count=\"8\">0.0 0.0 1.0 0.0 0.0 1.0 1.0 1.0 </float_array>\n" +
                  "               <technique_common>\n" +
                  "                  <accessor source=\"#mesh1-geometry-uv-array\" count=\"4\" stride=\"2\">\n" +
                  "                     <param name=\"S\" type=\"float\"/>\n" +
                  "                     <param name=\"T\" type=\"float\"/>\n" +
                  "                  </accessor>\n" +
                  "               </technique_common>\n" +
                  "            </source>\n" +
                  "            <vertices id=\"mesh1-geometry-vertex\">\n" +
                  "               <input semantic=\"POSITION\" source=\"#mesh1-geometry-position\"/>\n" +
                  "            </vertices>\n" +
                  "            <triangles material=\"vchillimg\" count=\"4\">\n" +
                  "               <input semantic=\"VERTEX\" source=\"#mesh1-geometry-vertex\" offset=\"0\"/>\n" +
                  "               <input semantic=\"NORMAL\" source=\"#mesh1-geometry-normal\" offset=\"1\"/>\n" +
                  "               <input semantic=\"TEXCOORD\" source=\"#mesh1-geometry-uv\" offset=\"2\" set=\"0\"/>\n" +
                  "               <p>0 0 0 1 0 1 2 0 2 0 1 0 2 1 2 1 1 1 3 0 3 2 0 2 1 0 1 3 1 3 1 1 1 2 1 2 </p>\n" +
                  "            </triangles>\n" +
                  "         </mesh>\n" +
                  "      </geometry>\n" +
                  "   </library_geometries>\n" +
                  "   <library_visual_scenes>\n" +
                  "      <visual_scene id=\"RHIScanScene\" name=\"RHIScanScene\">\n" +
                  "         <node id=\"Model\" name=\"Model\">\n" +
                  "            <node id=\"mesh1\" name=\"mesh1\">\n" +
                  "               <instance_geometry url=\"#mesh1-geometry\">\n" +
                  "                  <bind_material>\n" +
                  "                     <technique_common>\n" +
                  "                        <instance_material symbol=\"vchillimg\" target=\"#vchillimgID\">\n" +
                  "                           <bind_vertex_input semantic=\"UVSET0\" input_semantic=\"TEXCOORD\" input_set=\"0\"/>\n" +
                  "                        </instance_material>\n" +
                  "                     </technique_common>\n" +
                  "                  </bind_material>\n" +
                  "               </instance_geometry>\n" +
                  "            </node>\n" +
                  "         </node>\n" +
                  "      </visual_scene>\n" +
                  "   </library_visual_scenes>\n" +
                  "   <scene>\n" +
                  "      <instance_visual_scene url=\"#RHIScanScene\"/>\n" +
                  "   </scene>\n" +
                  "</COLLADA>\n");
        out.flush();
        zip.closeEntry();
     }
}
