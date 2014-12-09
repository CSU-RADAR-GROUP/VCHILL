package edu.colostate.vchill;

import edu.colostate.vchill.ChillDefines.Mode;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides configuration settings relevant to data presentation.
 * All exceptions/errors encountered while processing data are simply ignored.
 *
 * @author Justin Carlson
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class Config {
    private static final Config config = new Config();

    private final NumberFormat nf = new DecimalFormat("###0.0#");

    /**
     * The type of gradient filtering options available
     */
    public enum GradientType {
        Range("drng"),
        Azimuth("daz"),
        Off("");

        public final String suffix;

        GradientType(final String suffix) {
            this.suffix = suffix;
        }
    }

    private volatile boolean drawGrid;
    private volatile boolean drawMap;
    private volatile boolean drawAircraft;
    private volatile boolean drawClickpt;
    private volatile boolean useThreshold;
    private volatile String thresholdType;

    /**
     * The value below which data is discarded when using the threshold filter
     */
    private volatile double thresholdFilterCutoff;

    /**
     * Should the absolute value of the data be taken before thresholding?
     */
    private volatile boolean thresholdIsAbsoluteValue;

    /**
     * Should thresholding remove data GREATER than the specified cutoff?
     */
    private volatile boolean thresholdIsGreaterThan;

    private volatile boolean useSmoothing;
    private volatile boolean reduceNoise;
    private volatile boolean unfoldVelocities;
    private volatile boolean unfoldingAutomatic;
    /**
     * The range (km) where the unfolding algorithm will start.
     */
    private volatile double unfoldStartRange;
    private volatile GradientType gradientType;

    private volatile int clutterWindowSize;
    private volatile double clutterThreshold;

    private volatile double rhiHvFactor;

    private volatile double plotRange;
    private volatile double maxPlotRange = 150;
    private volatile double maxPlotHeight;
    private volatile int gridSpacing;

    /**
     * in km
     */
    private volatile double centerX;
    private volatile double centerY;

    private volatile int rayStep;

    private volatile String directoryName;
    private volatile String socketName;
    private volatile String controlName;
    private volatile String realtimeName;

    private volatile List<String> socketHistory;
    private volatile List<String> controlHistory;
    private volatile List<String> realtimeHistory;

    private volatile Mode plottingMode;

    private volatile int imageBufferSize;

    private volatile boolean autosave;
    private volatile boolean autoexport;
    private volatile boolean saveToDisk;
    private volatile String saveToDiskPath;
    private volatile boolean saveAll;
    private volatile int saveTilt;
    private volatile String scanFilter;

    private volatile boolean useGUI;

    private volatile String signon;
    private volatile String password;

    private volatile boolean realtimeMode;

    public static final int ARCHIVE_CONN = 1;
    public static final int REALTIME_CONN = 2;
    private volatile int lastConnectionType;

    /**
     * Private default constructor prevents instantiation
     */
    private Config() {
        this.loadPreferences(); //load values (or default if not available)
    }

    /**
     * Load preferences saved on disc - useful after resetting/clearing
     */
    public void loadPreferences() {
        this.plotRange = ConfigUtil.getDouble("Maximum Plot Range", 150);
        this.maxPlotHeight = ConfigUtil.getDouble("Maximum Plot Height", 25);
        this.gridSpacing = ConfigUtil.getInt("Grid Spacing", 25);
        this.rayStep = ConfigUtil.getInt("Ray Step", 10);
        this.rhiHvFactor = ConfigUtil.getDouble("RHI Vertical Stretch Factor", 3);
        //this.plottingMode = Mode.valueOf(ConfigUtil.getString("Plot Mode", Mode.Sweep.name()));
        this.plottingMode = Mode.Sweep;
        this.drawMap = ConfigUtil.getBoolean("Draw Map", true);
        this.drawAircraft = ConfigUtil.getBoolean("Draw Aircraft", true);
        this.drawGrid = ConfigUtil.getBoolean("Draw Grid/Rings", true);
        this.drawClickpt = ConfigUtil.getBoolean("Draw Click Point", true);
        this.useThreshold = ConfigUtil.getBoolean("Use Threshold Filter", true);
        this.thresholdFilterCutoff = ConfigUtil.getDouble("Threshold Filter Cutoff", 0.25);
        this.thresholdIsAbsoluteValue = ConfigUtil.getBoolean("Threshold Is Absolute Value", false);
        this.thresholdIsGreaterThan = ConfigUtil.getBoolean("Threshold Removes Data Greater Than Cutoff", false);
        this.thresholdType = ConfigUtil.getString("Threshold Type", "NCP");
        this.useSmoothing = ConfigUtil.getBoolean("Use Smoothing Filter", false);
        this.reduceNoise = ConfigUtil.getBoolean("Use Noise Filter", false);
        this.unfoldVelocities = ConfigUtil.getBoolean("Unfold Velocities", false);
        this.unfoldingAutomatic = ConfigUtil.getBoolean("Unfolding Automatic", true);
        this.unfoldStartRange = ConfigUtil.getDouble("Unfold Start Range", 0.0);
        this.gradientType = GradientType.valueOf(ConfigUtil.getString("Gradient Type", GradientType.Off.toString()));
        this.clutterWindowSize = ConfigUtil.getInt("Clutter Filter Window Size", 3);
        this.clutterThreshold = ConfigUtil.getDouble("Clutter Filter Threshold", 1.5);
        this.centerX = ConfigUtil.getDouble("Center X", 0);
        this.centerY = ConfigUtil.getDouble("Center Y", 0);
        this.signon = ConfigUtil.getString("Signon", null);
        this.password = ConfigUtil.getString("Password", null);
        this.socketName = ConfigUtil.getString("Archive Server", "vchill.chill.colostate.edu:2510");
        this.controlName = ConfigUtil.getString("Control Server", "");
        this.realtimeName = ConfigUtil.getString("Realtime Server", "vchill.chill.colostate.edu:2511");
        this.directoryName = ConfigUtil.getString("Local Data Directory", null);
        String[] socketHistory = ConfigUtil.getStringArray("Archive History", new String[]{this.socketName});
        this.socketHistory = new ArrayList<String>(socketHistory.length);
        String[] controlHistory = ConfigUtil.getStringArray("Control History", new String[]{this.controlName});
        this.controlHistory = new ArrayList<String>(controlHistory.length);
        String[] realtimeHistory = ConfigUtil.getStringArray("Realtime History", new String[]{this.realtimeName});
        this.realtimeHistory = new ArrayList<String>(realtimeHistory.length);
        this.realtimeHistory.add(realtimeName);
        this.imageBufferSize = ConfigUtil.getInt("Image Buffer Size", 75);
        this.autosave = ConfigUtil.getBoolean("Autosave Images", false);
        this.autoexport = ConfigUtil.getBoolean("Automatically Export", false);
        this.saveToDisk = ConfigUtil.getBoolean("Save To Disk", false);
        this.saveToDiskPath = ConfigUtil.getString("Save To Disk Path", ".");
        this.saveAll = ConfigUtil.getBoolean("Save All Sweeps", true);
        this.saveTilt = ConfigUtil.getInt("Autosave Tilt Number", 0);
        this.scanFilter = ConfigUtil.getString("Scan Name Filter", null);
        this.useGUI = ConfigUtil.getBoolean("Use GUI", true);
        this.lastConnectionType = ConfigUtil.getInt("Last Connection Type", ARCHIVE_CONN);
    }

    public void savePreferences() {
        try {
            ConfigUtil.put("Signon", this.signon);
            //don't save password
            //don't save server names
            //don't save image buffer size
            System.out.println("Preferences saved.");
        } catch (Throwable t) {
            System.err.println("Error saving preferences:" + t);
        }
    }

    public static Config getInstance() {
        return config;
    }

    public synchronized void setGridEnabled(final boolean drawGrid) {
        ConfigUtil.put("Draw Grid/Rings", this.drawGrid = drawGrid);
    }

    public void toggleGridEnabled() {
        this.setGridEnabled(!this.drawGrid);
    }

    public boolean isGridEnabled() {
        return this.drawGrid;
    }

    public synchronized void setClickPointEnabled(final boolean drawClickpt) {
        ConfigUtil.put("Draw Click Point", this.drawClickpt = drawClickpt);
    }

    public void toggleClickPointEnabled() {
        this.setClickPointEnabled(!this.drawClickpt);
    }

    public boolean isClickPointEnabled() {
        return this.drawClickpt;
    }

    public synchronized void setMapEnabled(final boolean drawMap) {
        ConfigUtil.put("Draw Map", this.drawMap = drawMap);
    }

    public void toggleMapEnabled() {
        this.setMapEnabled(!this.drawMap);
    }

    public boolean isMapEnabled() {
        return this.drawMap;
    }

    public synchronized void setAircraftEnabled(final boolean drawAircraft) {
        ConfigUtil.put("Draw Aircraft", this.drawAircraft = drawAircraft);
    }

    public void toggleAircraftEnabled() {
        this.setAircraftEnabled(!this.drawAircraft);
    }

    public boolean isAircraftEnabled() {
        return this.drawAircraft;
    }

    public synchronized void setThresholdEnabled(final boolean useThreshold) {
        ConfigUtil.put("Use Threshold Filter", this.useThreshold = useThreshold);
    }

    public void toggleThresholdEnabled() {
        this.setThresholdEnabled(!this.useThreshold);
    }

    public boolean isThresholdEnabled() {
        return this.useThreshold;
    }

    public synchronized void setThresholdType(final String thresholdType) {
        ConfigUtil.put("Threshold Type", this.thresholdType = thresholdType);
    }

    public synchronized String getThresholdType() {
        return this.thresholdType;
    }

    public synchronized void setThresholdFilterCutoff(final double thresholdFilterCutoff) {
        ConfigUtil.put("Threshold Filter Cutoff", this.thresholdFilterCutoff = thresholdFilterCutoff);
    }

    public synchronized double getThresholdFilterCutoff() {
        return this.thresholdFilterCutoff;
    }

    public synchronized void setThresholdAbsoluteValueEnabled(final boolean thresholdIsAbsoluteValue) {
        ConfigUtil.put("Threshold Is Absolute Value", this.thresholdIsAbsoluteValue = thresholdIsAbsoluteValue);
    }

    public synchronized boolean isThresholdAbsoluteValue() {
        return this.thresholdIsAbsoluteValue;
    }

    public synchronized void setThresholdGreaterThanEnabled(final boolean thresholdIsGreaterThan) {
        ConfigUtil.put("Threshold Removes Data Greater Than Cutoff", this.thresholdIsGreaterThan = thresholdIsGreaterThan);
    }

    public synchronized boolean isThresholdGreaterThan() {
        return this.thresholdIsGreaterThan;
    }

    public synchronized void setSmoothingEnabled(final boolean useSmoothing) {
        ConfigUtil.put("Use Smoothing Filter", this.useSmoothing = useSmoothing);
    }

    public void toggleSmoothingEnabled() {
        this.setSmoothingEnabled(!this.useSmoothing);
    }

    public boolean isSmoothingEnabled() {
        return this.useSmoothing;
    }

    public synchronized void setNoiseReductionEnabled(final boolean reduceNoise) {
        ConfigUtil.put("Use Noise Filter", this.reduceNoise = reduceNoise);
    }

    public void toggleNoiseReductionEnabled() {
        this.setNoiseReductionEnabled(!this.reduceNoise);
    }

    public boolean isNoiseReductionEnabled() {
        return this.reduceNoise;
    }

    public synchronized void setUnfoldingEnabled(final boolean unfoldVelocities) {
        ConfigUtil.put("Unfold Velocities", this.unfoldVelocities = unfoldVelocities);
    }

    public boolean isUnfoldingEnabled() {
        return this.unfoldVelocities;
    }

    public synchronized void setUnfoldingAutomatic(final boolean unfoldingAutomatic) {
        ConfigUtil.put("Unfolding Automatic", this.unfoldingAutomatic = unfoldingAutomatic);
    }

    public boolean isUnfoldingAutomatic() {
        return this.unfoldingAutomatic;
    }

    public synchronized void setUnfoldStartRange(final double startRange) {
        ConfigUtil.put("Unfold Start Range", this.unfoldStartRange = startRange);
    }

    public double getUnfoldStartRange() {
        return this.unfoldStartRange;
    }

    public synchronized void setGradientType(final GradientType gradientType) {
        ConfigUtil.put("Gradient Type", (this.gradientType = gradientType).toString());
    }

    public synchronized boolean isGradientEnabled() {
        return this.gradientType != GradientType.Off;
    }

    public synchronized GradientType getGradientType() {
        return this.gradientType;
    }

    public int getClutterWindowSize() {
        return this.clutterWindowSize;
    }

    public double getClutterThreshold() {
        return this.clutterThreshold;
    }

    public synchronized void setRHIHVFactor(final double rhiHvFactor) {
        ConfigUtil.put("RHI Vertical Stretch Factor", this.rhiHvFactor = rhiHvFactor);
    }

    public synchronized void setRHIHVFactor(final String rhiHvFactor) {
        try {
            this.setRHIHVFactor(Double.parseDouble(rhiHvFactor));
        } catch (NumberFormatException nfe) {
            System.err.println("Ignoring badly formatted RHI HV factor");
        }
    }

    public synchronized double getRHIHVFactor() {
        return this.rhiHvFactor;
    }

    public synchronized void setDirName(final String directoryName) {
        this.directoryName = directoryName;
    }

    public synchronized void setDefaultDirName(final String directoryName) {
        ConfigUtil.put("Local Data Directory", this.directoryName = directoryName);
    }

    public synchronized String getDefaultDirName() {
        return this.directoryName;
    }

    public synchronized void setSocketName(final String socketName) {
        this.socketName = socketName;
    }

    public synchronized void setDefaultSocketName(final String socketName) {
        ConfigUtil.put("Archive Server", this.socketName = socketName);
    }

    public synchronized String getDefaultSocketName() {
        return this.socketName;
    }

    public synchronized void setControlName(final String controlName) {
        this.controlName = controlName;
    }

    public synchronized void setDefaultControlName(final String controlName) {
        ConfigUtil.put("Control Server", this.controlName = controlName);
    }

    public synchronized String getDefaultControlName() {
        return this.controlName;
    }

    public synchronized void setRealtimeName(final String realtimeName) {
        this.realtimeName = realtimeName;
    }

    public synchronized void setDefaultRealtimeName(final String realtimeName) {
        ConfigUtil.put("Realtime Server", this.realtimeName = realtimeName);
    }

    public synchronized String getDefaultRealtimeName() {
        return this.realtimeName;
    }

    public synchronized void addToSocketHistory(final String entry) {
        if (this.socketHistory.contains(entry)) this.socketHistory.remove(entry);
        this.socketHistory.add(0, entry);
        ConfigUtil.put("Archive History", this.getSocketHistory());
    }

    public synchronized String[] getSocketHistory() {
        return this.socketHistory.toArray(new String[this.socketHistory.size()]);
    }

    public synchronized void addToControlHistory(final String entry) {
        if (entry == null) return; //no need to store
        if (this.controlHistory.contains(entry)) this.controlHistory.remove(entry);
        this.controlHistory.add(0, entry);
        ConfigUtil.put("Control History", this.getControlHistory());
    }

    public synchronized String[] getControlHistory() {
        return this.controlHistory.toArray(new String[this.controlHistory.size()]);
    }

    public synchronized void addToRealtimeHistory(final String entry) {
        if (this.realtimeHistory.contains(entry)) this.realtimeHistory.remove(entry);
        this.realtimeHistory.add(0, entry);
        ConfigUtil.put("Realtime History", this.getRealtimeHistory());
    }

    public synchronized String[] getRealtimeHistory() {
        return this.realtimeHistory.toArray(new String[this.realtimeHistory.size()]);
    }

    public synchronized void setPlottingMode(final Mode plottingMode) {
        ConfigUtil.put("Plot Mode", (this.plottingMode = plottingMode).toString());
    }

    public synchronized Mode getPlottingMode() {
        return this.plottingMode;
    }

    public int getDefaultImageSizeX() {
        return 512;
    }

    public int getDefaultImageSizeY() {
        return 512;
    }

    public int getDefaultImageMax() {
        return this.imageBufferSize;
    }

    public synchronized void setRayStep(final int rayStep) {
        ConfigUtil.put("Ray Step", this.rayStep = rayStep);
    }

    public synchronized void setRayStep(final String rayStep) {
        try {
            this.setRayStep((int) Double.parseDouble(rayStep));
        } catch (NumberFormatException nfe) {
            System.err.println("Ignoring badly formatted ray step");
        }
    }

    public synchronized int getRayStep() {
        return this.rayStep;
    }

    public synchronized void setPlotRange(double plotRange) {
        plotRange = Math.min(plotRange, this.maxPlotRange * 4); //ensure we are not zoomed out too far, but allow some leeway
        ConfigUtil.put("Maximum Plot Range", this.plotRange = plotRange);
    }

    public void setPlotRange(final String plotRange) {
        try {
            this.setPlotRange(Double.parseDouble(plotRange));
        } catch (NumberFormatException nfe) {
            System.err.println("Ignoring badly formatted max plot range");
        }
    }

    public synchronized double getPlotRange() {
        return this.plotRange;
    }

    public synchronized void setMaxPlotRange(final double maxPlotRange) {
        if (maxPlotRange < this.plotRange) this.setPlotRange(this.plotRange); //ensure we are not zoomed out too far
        this.maxPlotRange = maxPlotRange;
    }

    public synchronized double getMaxPlotRange() {
        return this.maxPlotRange;
    }

    public synchronized void setMaxPlotHeight(final double maxPlotHeight) {
        ConfigUtil.put("Maximum Plot Height", this.maxPlotHeight = maxPlotHeight);
    }

    public synchronized void setMaxPlotHeight(final String maxPlotHeight) {
        try {
            this.setMaxPlotHeight(Double.parseDouble(maxPlotHeight));
        } catch (NumberFormatException nfe) {
            System.err.println("Ignoring badly formatted max plot height");
        }
    }

    public synchronized double getMaxPlotHeight() {
        return this.maxPlotHeight;
    }

    public synchronized void setGridSpacing(final int gridSpacing) {
        ConfigUtil.put("Grid Spacing", this.gridSpacing = gridSpacing);
    }

    public synchronized void setGridSpacing(final String gridSpacing) {
        try {
            this.setGridSpacing((int) Double.parseDouble(gridSpacing));
        } catch (NumberFormatException nfe) {
            System.err.println("Ignoring badly formatted grid spacing");
        }
    }

    public synchronized int getGridSpacing() {
        return this.gridSpacing;
    }

    public synchronized void setCenterX(final double centerX) {
        ConfigUtil.put("Center X", this.centerX = centerX);
    }

    public synchronized void setCenterX(final String centerX) {
        try {
            this.setCenterX(Double.parseDouble(centerX));
        } catch (NumberFormatException nfe) {
            System.err.println("Ignoring badly formatted center x");
        }
    }

    public synchronized double getCenterX() {
        return this.centerX;
    }

    public synchronized void setCenterY(final double centerY) {
        ConfigUtil.put("Center Y", this.centerY = centerY);
    }

    public synchronized void setCenterY(final String centerY) {
        try {
            this.setCenterY(Double.parseDouble(centerY));
        } catch (NumberFormatException nfe) {
            System.err.println("Ignoring badly formatted center y");
        }
    }

    public synchronized double getCenterY() {
        return this.centerY;
    }

    public synchronized void setImageAutosaveEnabled(final boolean autosave) {
        this.autosave = autosave;
    }

    public synchronized boolean isImageAutosaveEnabled() {
        return this.autosave;
    }

    public synchronized void setImageAutoExportEnabled(final boolean autoexport) {
        this.autoexport = autoexport;
    }

    public synchronized boolean isImageAutoExportEnabled() {
        return this.autoexport;
    }

    public synchronized void setSaveToDiskEnabled(final boolean saveToDisk) {
        this.saveToDisk = saveToDisk;
    }

    public synchronized boolean isSaveToDiskEnabled() {
        return this.saveToDisk;
    }

    public synchronized void setSaveToDiskPath(final String saveToDiskPath) {
        this.saveToDiskPath = saveToDiskPath;
    }

    public synchronized String getSaveToDiskPath() {
        return this.saveToDiskPath;
    }

    public synchronized void setSaveAllEnabled(final boolean saveAll) {
        this.saveAll = saveAll;
    }

    public synchronized boolean isSaveAllEnabled() {
        return this.saveAll;
    }

    public synchronized void setAutosaveTilt(final int saveTilt) {
        this.saveTilt = saveTilt;
    }

    public synchronized int getAutosaveTilt() {
        return this.saveTilt;
    }

    public synchronized void setScanFilter(final String scanFilter) {
        this.scanFilter = scanFilter;
    }

    public synchronized String getScanFilter() {
        return this.scanFilter;
    }

    public synchronized void setGUIEnabled(final boolean useGUI) {
        this.useGUI = useGUI;
    }

    public synchronized boolean isGUIEnabled() {
        return this.useGUI;
    }

    public NumberFormat getNumberFormat() {
        return this.nf;
    }

    public synchronized void setSignon(final String signon) {
        this.signon = signon;
    }

    public synchronized String getSignon() {
        return this.signon;
    }

    public synchronized void setPassword(final String password) {
        this.password = password;
    }

    public synchronized String getPassword() {
        return this.password;
    }

    public synchronized void setRealtimeModeEnabled(final boolean realtimeMode) {
        this.realtimeMode = realtimeMode;
    }

    public synchronized boolean isRealtimeModeEnabled() {
        return this.realtimeMode;
    }

    public synchronized void setLastConnectionType(final int connType) {
        ConfigUtil.put("Last Connection Type", this.lastConnectionType = connType);
    }

    public synchronized int getLastConnectionType() {
        return this.lastConnectionType;
    }

    public static void main(final String[] args) {
        try {
            ConfigUtil.getPreferences().remove((String) DialogUtil.showOptionDialog("Cleaning preferences", "Remove which item?", ConfigUtil.getPreferences().keys()));
            return;
        } catch (Exception e) {
        }
    }
}
