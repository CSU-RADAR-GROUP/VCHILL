package edu.colostate.vchill.color;

import edu.colostate.vchill.ChillDefines.ColorType;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Hash based map for storing color data
 *
 * @author Jochen Deyke
 * @version 2006-09-27
 */
public class ColorMap {
    private static final Config config = Config.getInstance();
    private final HashMap<ColorType, List<Color>> types;
    private final boolean interpolateable;

    public ColorMap(final boolean interpolateable) {
        this.types = new HashMap<ColorType, List<Color>>();
        this.interpolateable = interpolateable;
    }
    public boolean get_interpolation_value(){
        return this.interpolateable;
    }

    /**
     * Adds or replaces the requested type with the given data
     *
     * @param typeName the name of the type to add or replace
     * @param colors   the color data to store for that type
     */
    public void addType(final ColorType typeName, final List<Color> colors) {
        this.types.put(typeName, Collections.unmodifiableList(interpolateable &&
                config.isInterpolateColorsEnabled() ? interpolate(colors, config.getNumberOfColors()) : colors));
    }

    /**
     * Retrieves the data for the requested type
     *
     * @param typeName the name of the type to add or replace
     * @return the color data to store for that type
     */
    public List<Color> getType(final ColorType typeName) {
        return this.types.get(typeName);
    }

    /**
     * Determines which types have been stored
     *
     * @return the set of all keys stored in the map
     */
    public Set<ColorType> keySet() {
        return this.types.keySet();
    }

    /**
     * Removes all entries from the map
     */
    public void clear() {
        this.types.clear();
    }

    public static List<Color> interpolate(final List<Color> input, final int newSize) {
        ArrayList<Color> output = new ArrayList<Color>(newSize);
        for (int oi = 0; oi < newSize; ++oi) {
            if (oi < (newSize / (double) input.size()) / 2) {
                output.add(input.get(0));
            } else if (oi > newSize - 1 - (newSize / (double) input.size()) / 2) {
                output.add(input.get(input.size() - 1));
            } else {
                double ii = oi * (double) (input.size() - 1) / (newSize - 1);
                int whole = (int) ii;
                double fraction = ii - whole;
                int red = (int) (input.get(whole).getRed() * (1 - fraction) + input.get(whole + 1).getRed() * fraction);
                int green = (int) (input.get(whole).getGreen() * (1 - fraction) + input.get(whole + 1).getGreen() * fraction);
                int blue = (int) (input.get(whole).getBlue() * (1 - fraction) + input.get(whole + 1).getBlue() * fraction);
                output.add(new Color(red, green, blue));
            }
        }
        assert output.size() == newSize;
        return output;
    }
}
