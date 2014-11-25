package edu.colostate.vchill.color;

import edu.colostate.vchill.ChillDefines.ColorType;
import edu.colostate.vchill.Loader;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.chill.ChillMomentFieldScale;

import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Main/Control class for VCHILL's color module
 *
 * @author Jochen Deyke
 * @author Alexander Deyke
 * @version 2007-03-15
 */
public class XMLControl {
    private static final ScaleManager sm = ScaleManager.getInstance();
    private final ColorMap activeColorMap;

    public XMLControl() {
        this(true);
    }

    public XMLControl(final boolean interpolateable) {
        this.activeColorMap = new ColorMap(interpolateable);
    }

    /**
     * Get the Colors to render a plot of the specified data type
     *
     * @param type the data type to get the Colors for
     * @return the requested Listof Color
     */
    public List<Color> getType(final String type) {
        return activeColorMap.getType(translate(type));
    }

    /**
     * Get the Colors of a certain color type
     *
     * @param type the color type to get the Colors for
     * @return the requested Color[]
     */
    public List<Color> getType(final ColorType type) {
        return activeColorMap.getType(type);
    }

    /**
     * Set the Colors to render a plot of the specified data type
     *
     * @param type the data type to set the Colors for
     */
    public void setType(final String type, final List<Color> colors) {
        activeColorMap.addType(translate(type), colors);
    }

    /**
     * Set the Colors of a certain color type
     *
     * @param type the data type to set the Colors for
     */
    public void setType(final ColorType type, final List<Color> colors) {
        activeColorMap.addType(type, colors);
    }

    /**
     * Translate an old-style text identifier into the new XML name
     *
     * @param type the data type to translate
     * @return the corresponding ColorType
     */
    public static ColorType translate(final String type) {
        ChillMomentFieldScale scale = sm.getScale(type);
        if (scale == null) return null;
        return scale.colorMapType;
    }

    /**
     * Load color definitions from the specified file.
     * If the specified file exists, it is loaded from the local filesystem.
     * In that case, the parser is determined by the suffix -
     * anything other than ".xml" is treated as old-style text format.
     * If the file does not exist on the local filesystem, it is presumed to be an
     * XML file to be retrieved from the resource jar.
     *
     * @param filename the name of the file to parse
     */
    public void load(final String filename) {
        this.activeColorMap.clear();
        File file = new File(filename);
        if (!file.exists()) { //not on local filesys; get from resource jar
            try {
                ClassLoader cl = Loader.class.getClassLoader();
                JarInputStream jar_in = new JarInputStream(cl.getResourceAsStream("colors.jar"));
                JarEntry curr_entry = jar_in.getNextJarEntry();
                while (true) {
                    if (curr_entry == null) return;
                    if (curr_entry.getName().equals(filename)) break;
                    curr_entry = jar_in.getNextJarEntry();
                }
                SAXParserFactory.newInstance().newSAXParser().parse(jar_in, new XMLColorMapHandler(activeColorMap));
            } catch (Exception e) {
                System.err.println("Exception while loading colors: " + e);
                e.printStackTrace();
            }
        } else { //file DOES exist on local filesystem
            try {
                if (filename.endsWith(".xml")) {
                    SAXParserFactory.newInstance().newSAXParser().parse(file, new XMLColorMapHandler(activeColorMap));
                } else { //assume old-school text file -- not for release use
                    new ColorMapTextParser(activeColorMap).parse(file.getCanonicalPath());
                }
            } catch (Exception e) {
                System.err.println("Exception while loading colors: " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Save the currently active color definitions to the specified file.
     * Output is in XML format.
     *
     * @param filename the name of the file to save as.
     *                 It is <b><em>*highly*</em></b> recommended this name end in ".xml" so load() will use the correct parser.
     *                 Failure to follow this guideline will render the file unuseable by VCHILL until manually renamed.
     */
    public void save(final String filename) {
        PrintStream file;

        try {
            file = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(filename))), false, "UTF-8");
        } catch (Exception e) {
            throw new Error(e);
        }

        file.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        file.println("<colormap name=\"" + filename + "\">");
        for (ColorType type : ColorType.values()) {
            file.println("    <type>");
            file.println("        <name>" + type + "</name>");
            List<Color> colors = activeColorMap.getType(type);
            for (int j = 0; j < colors.size(); ++j) {
                file.println("        <color><!-- level " + j + " -->");
                file.println("            <red>" + colors.get(j).getRed() + "</red>");
                file.println("            <green>" + colors.get(j).getGreen() + "</green>");
                file.println("            <blue>" + colors.get(j).getBlue() + "</blue>");
                file.println("        </color>");
            }
            file.println("    </type>");
        }
        file.println("</colormap>");
        file.flush();
        file.close();
    }

    /**
     * Get the list of all available XML color definition files
     *
     * @return a List of available filenames
     */
    public static List<String> getListOfFiles() {
        try {
            ClassLoader cl = Loader.class.getClassLoader();
            JarInputStream jar_in = new JarInputStream(cl.getResourceAsStream("colors.jar"));
            LinkedList<String> names = new LinkedList<String>();
            while (jar_in.available() > 0) {
                JarEntry entry = jar_in.getNextJarEntry();
                if (entry == null) break;
                String name = (entry).getName();
                if (name.endsWith(".xml")) {
                    names.add(name);
                }
            }
            return Collections.unmodifiableList(names);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.unmodifiableList(new LinkedList<String>());
        }
    }

    /**
     * Test method for XML module - translates args[0] to args[1]
     *
     * @param args file to load - default is defclrs.txt,
     *             file to save - default is defclrs.xml
     */
    public static void main(final String[] args) //test method
    {
        XMLControl x = new XMLControl();
        System.out.println("Loading...");
        x.load((args.length > 0) ? args[0] : "defclrs.txt");
        System.out.println("Saving...");
        x.save((args.length > 1) ? args[1] : "defclrs.xml");
        System.out.println("Done!");
    }
}
