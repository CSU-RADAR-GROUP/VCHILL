package edu.colostate.vchill.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import edu.colostate.vchill.Loader;
import edu.colostate.vchill.LocationManager;
import edu.colostate.vchill.ViewUtil;
import edu.colostate.vchill.map.MapInstruction.Shape;

/**
 * Parser for old-style map definition files.
 *
 * @author Jochen Deyke
 * @version 2007-08-20
 */
public class MapTextParser
{
    private static final LocationManager lm = LocationManager.getInstance();

    private double centerLatitude;
    private double centerLongitude;

    public MapTextParser ()
    {
    }

    /**
     * Parses a reader for map information
     *
     * @param reader The reader to parse 
     * @param base A base map to add to (optional) 
     * @return a List of instructions for drawing the map
     * @throws IOException if an error is encountered while parsing 
     */
    public List<MapInstruction> parse (final Reader reader,
            final List<MapInstruction> base) throws IOException
    {
        { //reset center
            this.centerLongitude = lm.getLongitude();
            this.centerLatitude = lm.getLatitude();
        }
        BufferedReader file = new BufferedReader(reader);
        List<MapInstruction> result = (base == null ? new LinkedList<MapInstruction>() : base);
        MapInstruction prevInstr = null;
        while (file.ready()) {
            String line = file.readLine();
            if (line == null) break;
            if (line.startsWith("\ufeff")) line = line.substring(1);
            line = line.trim();
            if (line.length() < 1) continue; //blank line

            if (line.startsWith("#")) { //whole-line comment
                line = line.substring(1);
                if (line.startsWith("! center: ")) {
                    line = line.substring(10); //strip header
                    String[] tmp = line.split("[ \t]++");
                    this.centerLongitude = Double.parseDouble(tmp[0]);
                    this.centerLatitude = Double.parseDouble(tmp[1]);
                }
            } else { //<x y type [label]> format
                String[] tmp = line.split("[ \t]++");
                StringBuilder commentBuffer = new StringBuilder();
                if (tmp.length > 3) { //end-of-line label
                    for (int i = 3; i < tmp.length; ++i) {
                        if (tmp[i].startsWith("#")) break; //comment mark; end of label
                        commentBuffer.append(tmp[i]).append(" ");
                    }
                }

                if (prevInstr != null && prevInstr.getType() == Shape.CIRCLE) { //radius - don't modify
                    result.add(prevInstr = new MapInstruction(
                            Double.parseDouble(tmp[0]),
                            Double.parseDouble(tmp[1]),
                            Shape.values()[Integer.parseInt(tmp[2])],
                            commentBuffer.toString().trim()));
                } else {
                    double[] degrees = ViewUtil.getDegrees(Double.parseDouble(tmp[0]), Double.parseDouble(tmp[1]), this.centerLongitude, this.centerLatitude);
                    result.add(prevInstr = new MapInstruction(
                            degrees[0], degrees[1],
                            Shape.values()[Integer.parseInt(tmp[2])],
                            commentBuffer.toString().trim()));
                }
            }
        } //end while
        return result;
    }

    /**
     * Parses a file for map information.  If the specified file does not exist in the
     * resource jar file, it is loaded from the local filesystem instead.
     *
     * @param filename The file to parse 
     * @param base A base map to add to (optional) 
     * @return a List of instructions for drawing the map
     * @throws IOException if an error is encountered while parsing 
     */
    public List<MapInstruction> parse (final String filename,
            final List<MapInstruction> base) throws IOException
    {
        File file = new File(filename);
        ClassLoader cl = Loader.class.getClassLoader();
        JarInputStream jarIn = new JarInputStream(cl.getResourceAsStream("resources/maps.jar"));
        while (true) {
            ZipEntry entry = jarIn.getNextEntry();
            if (entry == null) return parse(new InputStreamReader(new FileInputStream(file), "UTF-8"), base); //end of resource jar: file not found
            if (entry.getName().equals(filename)) break;
        }
        return parse(new InputStreamReader(jarIn), base);
    }

    public static List<String> getListOfFiles ()
    {
        try {
            ClassLoader cl = Loader.class.getClassLoader();
            JarInputStream jarIn = new JarInputStream(cl.getResourceAsStream("resources/maps.jar"));
            LinkedList<String> names = new LinkedList<String>();
            while (jarIn.available() > 0) {
                ZipEntry entry = jarIn.getNextEntry();
                if (entry == null) break;
                String name = entry.getName();
                if (name.endsWith(".map")) names.add(name);
            }
            return Collections.unmodifiableList(names);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.unmodifiableList(new LinkedList<String>());
        }
    }

    /**
     * Filter for map files for the file open dialog
     */
    public static class MapFileFilter implements FileFilter
    {
        /** Accept only files ending in ".map" */
        public boolean accept (final File pathname)
        {
            if (pathname.isDirectory()) return false;
            if (pathname.getName().endsWith(".map")) return true;
            return false;
        }
    }
}
