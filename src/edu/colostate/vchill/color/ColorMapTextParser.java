package edu.colostate.vchill.color;

import edu.colostate.vchill.ChillDefines.ColorType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.awt.Color;
import java.util.ArrayList;

/**
 * Parser for old-style color definition txt files.
 *
 * @author Jochen Deyke
 * @version 2007-03-08
 */
public class ColorMapTextParser
{
    private final ColorMap colors;
    
    /**
     * Create a new ColorMapTextParser
     *
     * @param colors a shared ColorMap to add parsed colors to
     */
    public ColorMapTextParser (final ColorMap colors)
    {
        this.colors = colors;
    }

    /**
     * Parses a file for color information.
     * The color data is stored in a map shared among all classes in the module.
     *
     * @param filename the name of the file to parse 
     */
    public void parse (final String filename) throws IOException
    {
        BufferedReader file = new BufferedReader(new FileReader(filename));
        ArrayList<Color> tmpColors = null;
        ColorType tmpType = null;
        int tmpLevel = 0;
        String[] tmpColor = null;

        while (file.ready()) {
            String line = file.readLine();
            line = line == null ? "" : line.trim();
            if (line.startsWith("#") || line.equals("")) { //comment / blank line
                //ignore it
            } else if (line.startsWith(".")) { //header (eg ".Z16:"
                switch (line.charAt(1)) {
                    case 'Z': tmpType = ColorType.Z;   break;
                    case 'V': tmpType = ColorType.V;   break;
                    case 'W': tmpType = ColorType.W;   break;
                    case 'C': tmpType = ColorType.CZ;  break;
                    case 'D': tmpType = ColorType.Zdr; break;
                    case 'L': tmpType = ColorType.Ldr; break;
                    case 'P': tmpType = ColorType.Phi; break;
                    case 'R': tmpType = ColorType.Rho; break;
                    default: tmpType = null; break;
                }
                tmpColors = new ArrayList<Color>(Integer.parseInt(line.substring(2, line.indexOf(':'))));
                tmpLevel = 0;
            } else if (line.matches("^[0-9]{1,3}[ \t]*[0-9]{1,3}[ \t]*[0-9]{1,3}$")) { //better be a color
                tmpColor = line.split("[ \t]++");
                tmpColors.set(tmpLevel++, new Color(
                    Integer.parseInt(tmpColor[0]),
                    Integer.parseInt(tmpColor[1]),
                    Integer.parseInt(tmpColor[2])
                ));
                if (tmpLevel == tmpColors.size()) this.colors.addType(tmpType, tmpColors);
            } else {
                System.err.println("Don't know what to do with line \"" + line + "\"");
            }
        }

        file.close();
    }
}
