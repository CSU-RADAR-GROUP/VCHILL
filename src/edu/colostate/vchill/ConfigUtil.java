package edu.colostate.vchill;

import java.util.prefs.Preferences;

/**
 * Utility class for loading/saving preferences using java.util.prefs.Preferences.
 * This class catches Exception, so it may work even when normal Preferences fails.
 * It used to catch Throwable, so it would work even in the case of an Error (which
 * would reliably happen on 1.4.2 on Sparc/Solaris, but this is no longer needed as
 * of 1.5.0.  It still returns the default value in the case of any exception.
 *
 * @author Jochen Deyke
 * @version 2007-03-15
 */
public final class ConfigUtil {
    /**
     * The preferences node to load/save from/to
     */
    private static final Preferences prefs = Preferences.userRoot().node("/edu/colostate/vchill");

    /**
     * Private default constructor prevents instantiation
     */
    private ConfigUtil() {
    }

    /**
     * Provides direct access to the preferences node where VCHILL configuration information is stored.
     * This can be used when other operations than this class provides are needed.
     */
    public static Preferences getPreferences() {
        return prefs;
    }

    /**
     * Gets a boolean value from the preferences object
     *
     * @param key          the string used to save the boolean to be retrieved
     * @param defaultValue the value to be returned in case an error is encountered
     * @return the stored value if possible, <code>defaultValue</code> otherwise
     */
    public static boolean getBoolean(final String key, final boolean defaultValue) {
        try {
            return prefs.getBoolean(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets a double value from the preferences object
     *
     * @param key          the string used to save the double to be retrieved
     * @param defaultValue the value to be returned in case an error is encountered
     * @return the stored value if possible, <code>defaultValue</code> otherwise
     */
    public static double getDouble(final String key, final double defaultValue) {
        try {
            return prefs.getDouble(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets an integer value from the preferences object
     *
     * @param key          the string used to save the integer to be retrieved
     * @param defaultValue the value to be returned in case an error is encountered
     * @return the stored value if possible, <code>defaultValue</code> otherwise
     */
    public static int getInt(final String key, final int defaultValue) {
        try {
            return prefs.getInt(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets a string value from the preferences object
     *
     * @param key          the string used to save the string to be retrieved
     * @param defaultValue the value to be returned in case an error is encountered
     * @return the stored value if possible, <code>defaultValue</code> otherwise
     */
    public static String getString(final String key, final String defaultValue) {
        try {
            return prefs.get(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets an array of string values from the preferences object
     *
     * @param key          the string used to save the string to be retrieved
     * @param defaultValue the value to be returned in case an error is encountered
     * @return the stored value if possible, <code>defaultValue</code> otherwise
     */
    public static String[] getStringArray(final String key, final String[] defaultValue) {
        try {
            String val = prefs.get(key, arrayToString(defaultValue));
            if (val.equals("")) return new String[0];
            return val.split("\\*");
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Stores a boolean value to the preferences object
     *
     * @param key   the string that will be used to retrieve the value
     * @param value the value to be stored
     */
    public static void put(final String key, final boolean value) {
        prefs.putBoolean(key, value);
    }

    /**
     * Stores a double value to the preferences object
     *
     * @param key   the string that will be used to retrieve the value
     * @param value the value to be stored
     */
    public static void put(final String key, final double value) {
        prefs.putDouble(key, value);
    }

    /**
     * Stores an integer value to the preferences object
     *
     * @param key   the string that will be used to retrieve the value
     * @param value the value to be stored
     */
    public static void put(final String key, final int value) {
        prefs.putInt(key, value);
    }

    /**
     * Stores a string value to the preferences object
     *
     * @param key   the string that will be used to retrieve the value
     * @param value the value to be stored
     */
    public static void put(final String key, final String value) {
        prefs.put(key, value);
    }

    /**
     * Stores a string array value to the preferences object
     *
     * @param key   the string that will be used to retrieve the value
     * @param value the value to be stored (none of the strings in the array may contain "<code>*</code>")
     */
    public static void put(final String key, final String[] value) {
        prefs.put(key, arrayToString(value));
    }

    /**
     * Converts a String[] to a single String
     *
     * @param value the String[] to convert
     * @return the contents of <code>value</code> concatenated, with "<code>*</code>" as separator
     */
    private static String arrayToString(final String[] value) {
        if (value == null || value.length == 0) return "";
        StringBuilder buff = new StringBuilder(value[0]);
        for (int i = 1; i < value.length; ++i) buff.append("*").append(value[i]);
        return buff.toString();
    }

    /**
     * Removes all stored key, value pairs; useful if preferences become corrupted
     */
    public static void clear() {
        try {
            prefs.clear();
        } catch (Exception e) {
        }
    }
}
