package edu.colostate.vchill.gui;

import edu.colostate.vchill.ConfigUtil;
import edu.colostate.vchill.map.MapTextParser;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides configuration settings specific to the GUI.
 * All exceptions/errors encountered while processing data are simply
 * ignored.
 *
 * @author Justin Carlson
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-02-06
 */
public class Config {
    private static final Config me = new Config();

    private static final int inset = 50;
    private final int screenX;
    private final int screenY;

    private final Set<String> mapFileNames;

    /**
     * Private default constructor prevents instantiation
     */
    private Config() {
        this.mapFileNames = new HashSet<String>();
        int screenX, screenY;
        try {
            Dimension desktop = Toolkit.getDefaultToolkit().getScreenSize();
            screenX = desktop.width - Config.inset * 2;
            screenY = desktop.height - Config.inset * 2;
        } catch (Throwable t) {
            screenX = 1280;
            screenY = 1024;
        }
        this.screenX = screenX;
        this.screenY = screenY;

        String[] tmp = ConfigUtil.getStringArray("Map", new String[]{MapTextParser.getListOfFiles().get(0)});
        for (String name : tmp) this.mapFileNames.add(name);
    }

    public static Config getInstance() {
        return me;
    }

    public int getInset() {
        return Config.inset;
    }

    public int getDefaultWidth() {
        return this.screenX;
    }

    public int getDefaultHeight() {
        return this.screenY;
    }

    public synchronized void removeMapFileNames(final Collection<String> mapFileNames) {
        this.mapFileNames.removeAll(mapFileNames);
        ConfigUtil.put("Map", this.mapFileNames.toArray(new String[this.mapFileNames.size()]));
    }

    public synchronized void toggleMapFileName(final String mapFileName) {
        if (this.mapFileNames.contains(mapFileName)) this.mapFileNames.remove(mapFileName);
        else this.mapFileNames.add(mapFileName);
        ConfigUtil.put("Map", this.mapFileNames.toArray(new String[this.mapFileNames.size()]));
    }

    public synchronized void clearMapFileNames() {
        this.mapFileNames.clear();
        ConfigUtil.put("Map", new String[0]);
    }

    public synchronized Set<String> getMapFileNames() {
        return Collections.unmodifiableSet(this.mapFileNames);
    }

    public synchronized boolean isMapActive(final String mapFileName) {
        return this.mapFileNames.contains(mapFileName);
    }
}
