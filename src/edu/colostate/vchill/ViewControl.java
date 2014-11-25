package edu.colostate.vchill;

import edu.colostate.vchill.ChillDefines.Mode;
import edu.colostate.vchill.ascope.ViewAScopeWindow;
import edu.colostate.vchill.bookmark.Bookmark;
import edu.colostate.vchill.bookmark.BookmarkControl;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.color.XMLControl;
import edu.colostate.vchill.connection.Controller;
import edu.colostate.vchill.data.Ray;
import edu.colostate.vchill.gui.ViewFileBrowser;
import edu.colostate.vchill.gui.WindowManager;
import edu.colostate.vchill.map.MapInstruction;
import edu.colostate.vchill.map.MapTextParser;
import edu.colostate.vchill.numdump.NumDumpWindow;
import edu.colostate.vchill.plot.ViewPlotMethod;
import edu.colostate.vchill.plot.ViewPlotWindow;

import javax.security.auth.login.LoginException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * This is the central control of the entire program and attempts to
 * handle the "single choice" principle of code design. This is the
 * module all of the GUI code accesses before any requests are passed
 * down to the lower levels for actual data requests and storage. This
 * module, combined with all the other ViewConrol* classes provide a
 * method of making requests alter data in one location, making
 * modification easier. NOTE: Major updates have been made and Doc is
 * somewhat out of date. Future design decisions may alter the structure
 * again.
 *
 * @author Justin Carlson
 * @author Jochen Deyke
 * @author Alexander Deyke
 * @author jpont
 * @version 2010-08-02
 * @created April 7, 2003
 */
public final class ViewControl {
    /**
     * Source for default settings - must load this first
     */
    private static final Config config = Config.getInstance();

    /**
     * Scaling information
     */
    private static final ScaleManager sm = ScaleManager.getInstance();

    /**
     * Used for opening/closing windows etc
     */
    private static final WindowManager wm = WindowManager.getInstance();

    /**
     * Singleton reference
     */
    private static final ViewControl vc = new ViewControl();

    /**
     * Color management; stores the currentl color map
     */
    private final XMLControl colors = new XMLControl();

    /**
     * The Controller
     */
    private final Controller controller;

    /**
     * The message for dir requests etc
     */
    private ControlMessage controlMsg;

    /**
     * The message passing utility that is used to communicate wtih the ViewControlThread.  Uses ControlMessages.
     */
    private final ControlSyncQueue<ControlMessage> plotQueue;

    /**
     * Instructions for plotting the map overlay
     */
    private List<MapInstruction> map = new LinkedList<MapInstruction>();

    /**
     * for advancing in ray mode
     */
    private int lastRayPlotted;

    private ViewControlThread vct;

    /**
     * This returns a reference to the only single ViewControl
     * class.  This enables the program to be more modular, and
     * reduce every class in the GUI from needing direct access
     * to the central module.
     *
     * @return The ViewControl object
     */
    public static ViewControl getInstance() {
        return vc;
    }

    public ViewControlThread getViewControlThread() {
        return vct;
    }

    /**
     * Private default constructor prevents instantiation
     */
    private ViewControl() {
        this.controller = new Controller();
        this.controlMsg = new ControlMessage(null, null, null, null);
        this.plotQueue = new ControlSyncQueue<ControlMessage>();

        //Create the thread that will handle all the actual plotting requests.
        this.vct = new ViewControlThread(this.plotQueue, this.controller);
        Thread plotThread = new Thread(this.vct, "ViewControlThread");
        plotThread.setPriority(Thread.MIN_PRIORITY);
        plotThread.start();
    }

    /**
     * @param controlURL server:port to connect to for antenna control
     * @param dataURL    server:port to connect to for realtime data
     */
    public synchronized void startRadarControl(final String controlURL, final String dataURL) {
        this.setCurrentURL(dataURL);
        try {
            this.controller.createRealtimeConnection(dataURL);
        } catch (SocketTimeoutException ste) {
            System.err.println("Can't connect: Connection timed out");
            DialogUtil.showErrorDialog("Connection failed", ste.getMessage());
        } catch (Exception e) {
            System.err.println("Can't connect:");
            e.printStackTrace();
            DialogUtil.showErrorDialog("Connection failed", e.getMessage());
        }
    }

    public synchronized void setCurrentURL(final String url) {
        String oldUrl = this.controlMsg.getURL();
        this.controlMsg = this.controlMsg.setURL(url);
        if (url == null || !url.equals(oldUrl)) sm.switchServer();
    }

    public synchronized void setCurrentFile(final String filename) {
        this.controlMsg = this.controlMsg.setFile(filename);
    }

    public synchronized void setCurrentDirectory(final String dir) {
        this.controlMsg = this.controlMsg.setDir(dir);
    }

    public synchronized void setCurrentSweep(final String sweep) {
        this.controlMsg = this.controlMsg.setSweep(sweep);
        newPlot();
    }

    public synchronized ControlMessage getControlMessage() {
        return this.controlMsg;
    }

    public void addDirectory(final String dir) {
        this.addFile(new File(dir));
    }

    public void addFile(final File file) {
        this.controller.addFile(file);
    }

    public Collection<String> getConnections() {
        return this.controller.getConnectionList();
    }

    /**
     * Gets the list of available (sub-)directories and files
     */
    public Collection<String> getDirectory() {
        try {
            ControlMessage currMsg = this.getControlMessage();
            return this.controller.getDirectory(currMsg);
        } catch (Exception e) {
            System.err.println("Error in get dir:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the list of available sweeps
     *
     * @return The sweeps value
     */
    public Collection<String> getSweeps() {
        try {
            ControlMessage currMsg = this.getControlMessage();
            return this.controller.getSweepList(currMsg);
        } catch (Exception e) {
            System.err.println("Error in get Sweeps:");
            e.printStackTrace();
            return null;
        }
    }

    //Connection options.
    public synchronized void connect(String url) {
        System.out.println("ViewControl: trying to connect to: " + url);
        this.setCurrentURL(url);
        try {
            this.controller.connect(this.controlMsg);
            /*
			 * The 2 servers on vchill together serve all the data and so it's
			 * important to connect to both.
			 */
            if (url.equalsIgnoreCase("vchill.chill.colostate.edu:2510")) {
				/* Connect to the new server as well. */
                this.setCurrentURL("vchill.chill.colostate.edu:2513");
                this.controller.connect(this.controlMsg);
            } else if (url.equalsIgnoreCase("vchill.chill.colostate.edu:2513")) {
				/* Connect to the old server as well. */
                this.setCurrentURL("vchill.chill.colostate.edu:2510");
                this.controller.connect(this.controlMsg);
            }
        } catch (LoginException le) {
            this.setCurrentURL(null);
        } catch (SocketTimeoutException ste) {
            System.err.println("Can't connect: Connection timed out");
            this.setCurrentURL(null);
            DialogUtil.showErrorDialog("Connection failed", ste.getMessage());
        } catch (Exception e) {
            System.err.println("Can't connect:");
            e.printStackTrace();
            DialogUtil.showErrorDialog("Connection failed", e.getMessage());
        }
    }

    public boolean isConnected(final String url) {
        ControlMessage currMsg = this.getControlMessage();
        try {
            return this.controller.isConnected(currMsg.setURL(url));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConnected() {
        ControlMessage currMsg = this.getControlMessage();
        try {
            return this.controller.isConnected(currMsg);
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void reconnect() {
        System.out.println("ViewControl: trying to reconnect to: " + this.controlMsg.getURL());

        try {
            this.controller.reconnect(this.controlMsg);
        } catch (IOException ioe) {
            System.out.println("Error reconnecting");
            ioe.printStackTrace();
        }
    }

    public synchronized void reconnect(String url) {
        System.out.println("ViewControl: trying to reconnect to: " + url);

        try {
            this.controller.reconnect(this.controlMsg.setURL(url));
        } catch (IOException ioe) {
            System.out.println("Error reconnecting");
            ioe.printStackTrace();
        }
    }

    public synchronized void disconnect() {
        this.disconnect(this.controlMsg);
    }

    public synchronized void disconnect(final String url) {
        this.setCurrentURL(url);
        this.disconnect();
    }

    public synchronized void disconnect(final ControlMessage msg) {
        System.out.println("ViewControl: attempting to disconnect");
        try {
            this.controller.disconnect(msg);
        } catch (Exception e) {
            System.err.println("Disconnection failed: " + e);
            e.printStackTrace();
            DialogUtil.showErrorDialog("Disconnect failed", e.toString());
        }
    }

    public synchronized void stopPlot() {
        System.out.println("ViewControl: Stop has been called.");
        this.plotQueue.stop(); //empty the list of sweeps to plot in the view.
        this.controller.stop(this.controlMsg); //stop the socket from getting enqueued requests.
    }

    public synchronized void setMessage(final ControlMessage msg) {
        if (msg == null) return;
        String oldUrl = this.controlMsg.getURL();
        this.controlMsg = msg;
        if (!msg.getURL().equals(oldUrl)) sm.switchServer();
        newPlot();
    }

    public synchronized void rePlot() {
        if (this.controlMsg == null) return;
        switch (config.getPlottingMode()) {
            case Ray:
                this.plotRay(this.lastRayPlotted);
                break;
            default:
                this.plotQueue.add(this.controlMsg);
        }
    }

    /**
     * Enqueues a request to plot
     */
    private void newPlot() {
        if (this.controlMsg == null || config.isRealtimeModeEnabled()) return;
        switch (config.getPlottingMode()) {
            case Ray:
                this.plotRay(0);
                break;
            default:
                this.plotQueue.add(this.controlMsg);
        }
    }

    public XMLControl getColorControl() {
        return this.colors;
    }

    /**
     * Load a specific color scheme.
     *
     * @param filename The name of the file containing the color scheme to load
     */
    public synchronized void loadColors(final String filename) {
        //Check to ensure that there are in fact xml files that have been found.
        if (filename == null) throw new IllegalArgumentException("Can't load colors from null");

        //Load the color file if the String seems legal.
        this.colors.load(filename);
        wm.changeWindowColors();
        edu.colostate.vchill.color.Config.getInstance().setColorFileName(filename);
    }

    /**
     * Load map points from files specified in gui.Config
     */
    public synchronized void loadMaps() {
        edu.colostate.vchill.gui.Config guiconfig = edu.colostate.vchill.gui.Config.getInstance();
        Collection<String> filenames = guiconfig.getMapFileNames();
        Collection<String> badfiles = new LinkedList<String>();

        if (filenames.size() == 0) {
            this.map.clear(); //for loop won't affect, so just clear
        } else {
            this.map = null; //for loop will create/append, so get rid of old
        }

        config.setMapEnabled(filenames.size() > 0);

        for (String name : filenames)
            try {
                this.map = new MapTextParser().parse(name, this.map);
            } catch (Exception e) {
                System.err.println("ERROR: Failed to load map from " + name);
                e.printStackTrace();
                badfiles.add(name);
            }

        guiconfig.removeMapFileNames(badfiles);
        wm.replotOverlay();
    }

    /**
     * Sets plot color interval
     *
     * @param type The data type to set the scale for
     * @param max  The maximum value to put on the scale
     * @param min  The minimum value to put on the scale
     */
    public synchronized void setPlotInterval(final String type, final String max, final String min) {
        ChillMomentFieldScale scale = sm.getScale(type);
        if (max != null) scale.setMax(Double.parseDouble(max));
        if (min != null) scale.setMin(Double.parseDouble(min));
    }


    /**
     * Gets the ray at the specified x, y location.
     *
     * @param plotMethod the plot method to use for determining needed info
     * @param type       the type of data desired
     * @param x          X coordinate of where the mouse was clicked
     * @param y          Y coordinate of where the mouse was clicked
     * @return An object array consisting of the actual
     * ray and the ray number.
     */
    public Object[] getRay(final ViewPlotMethod plotMethod, final String type, final int x, final int y) {
        ControlMessage currMsg = this.getControlMessage();

        if (x != -1 && y != -1) {
            int rayNum = plotMethod.getRayNumFromXY(x, y);
            int maxDisplayableRays = plotMethod.getMaxDisplayableRays();
            if (rayNum != -1) {
                Ray ray = (Ray) this.controller.getRay(currMsg, type, rayNum);
                if (ray == null)
                    return null;
                //TH plots can wrap around and so make sure to get the
                //newest ray at this particular location
                while (ray != null) {
                    rayNum += maxDisplayableRays;
                    ray = (Ray) this.controller.getRay(currMsg, type, rayNum);
                }
                rayNum -= maxDisplayableRays;
                ray = (Ray) this.controller.getRay(currMsg, type, rayNum);
                return new Object[]{ray, rayNum};
            }
        }

        return null;
    }


    /**
     * Gets the ray at the specified azimuth.
     *
     * @param type    the type of ray desired
     * @param azimuth the angle desired in degrees
     */
    public Ray getRayAtAz(final String type, final double azimuth) {
        ControlMessage currMsg = this.getControlMessage();
        if (!currMsg.isValid())
            return null;

        int i = 0;
        double prevAngle, currAngle;
        Ray prevRay = (Ray) this.controller.getRay(currMsg, type, i++);
        Ray currRay = (Ray) this.controller.getRay(currMsg, type, i++);

        prevAngle = prevRay.getStartAzimuth();
        currAngle = ((Ray) this.controller.getRay(currMsg, type, 10)).getStartAzimuth();
        double diff = currAngle - prevAngle;
        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;
        // Note that when figuring out which ray to get that
        // gaps in the display are filled in by using color
        // data from the current ray and not the previous ray.
        if (diff > 0) { //increasing azimuth
            while (currRay != null) {
                prevAngle = prevRay.getStartAzimuth();
                currAngle = currRay.getStartAzimuth();
                if (azimuth > prevAngle && azimuth <= currAngle) {
                    plotRay(i - 1);
                    return currRay;
                }
                prevRay = currRay;
                currRay = (Ray) this.controller.getRay(currMsg, type, i++);
            }
        } else if (diff < 0) { //decreasing azimuth
            while (currRay != null) {
                prevAngle = prevRay.getStartAzimuth();
                currAngle = currRay.getStartAzimuth();
                if (azimuth <= prevAngle && azimuth > currAngle) {
                    plotRay(i - 2);
                    return prevRay;
                }
                prevRay = currRay;
                currRay = (Ray) this.controller.getRay(currMsg, type, i++);
            }
        }

        return null;
    }

    /**
     * Gets the ray at the specified elevation.
     *
     * @param type      the type of ray desired
     * @param elevation the desired elevation angle
     */
    public Ray getRayAtEl(final String type, final double elevation) {
        ControlMessage currMsg = this.getControlMessage();
        if (!currMsg.isValid())
            return null;

        int i = 0;
        double prevAngle, currAngle;
        Ray prevRay = (Ray) this.controller.getRay(currMsg, type, i++);
        Ray currRay = (Ray) this.controller.getRay(currMsg, type, i++);

        prevAngle = prevRay.getStartElevation();
        if (prevAngle > 180) prevAngle -= 360;
        currAngle = ((Ray) this.controller.getRay(currMsg, type, this.controller.getNumberOfRays(currMsg, type) - 1)).getStartElevation();
        if (currAngle > 180) currAngle -= 360;
        if (currAngle > prevAngle) { //increasing elevation
            while (currRay != null) {
                prevAngle = prevRay.getStartElevation();
                currAngle = currRay.getStartElevation();
                if (elevation >= prevAngle && elevation < currAngle) {
                    plotRay(i - 2);
                    return prevRay;
                }
                prevRay = currRay;
                currRay = (Ray) this.controller.getRay(currMsg, type, i++);
            }
        } else if (currAngle < prevAngle) { //decreasing elevation
            while (currRay != null) {
                prevAngle = prevRay.getStartElevation();
                currAngle = currRay.getStartElevation();
                if (elevation < prevAngle && elevation >= currAngle) {
                    plotRay(i - 1);
                    return currRay;
                }
                prevRay = currRay;
                currRay = (Ray) this.controller.getRay(currMsg, type, i++);
            }
        }

        return null;
    }

    /**
     * Retrieves the data value matching a given azimuth and range
     *
     * @param mode       the plot mode of data desired
     * @param type       the type of data desired
     * @param azimuth    the angle desired in degrees
     * @param elevation  the desired elevation angle
     * @param gateNum    the desired gate number
     * @param plotMethod the plot method to use for determining needed info
     * @param x          X coordinate of where the mouse was clicked
     * @param y          Y coordinate of where the mouse was clicked
     */
    public double getDataValue(final String mode, final String type,
                               final double azimuth, final double elevation, final int gateNum, final ViewPlotMethod plotMethod, final int x, final int y) {
        ControlMessage currMsg = this.getControlMessage();

        if (!currMsg.isValid())
            return Double.NaN;

        try {
            if (mode.equals("PPI")) {
                Ray ray = getRayAtAz(type, azimuth);
                if (ray == null)
                    return Double.NaN;
                else
                    return ray.getData()[gateNum];
            } else if (mode.equals("RHI")) {
                Ray ray = getRayAtEl(type, elevation);
                if (ray == null)
                    return Double.NaN;
                else
                    return ray.getData()[gateNum];
            } else { //constant azimuth & elevation
                Object[] rayInfo = this.getRay(plotMethod, type, x, y);
                if (rayInfo != null) {
                    plotRay((Integer) rayInfo[1]);
                    return ((Ray) rayInfo[0]).getData()[gateNum];
                }
            }
        } catch (NullPointerException npe) {
            System.out.println("data value readout not available");
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        } catch (Exception e) {
            System.out.println("exception: " + e);
            e.printStackTrace();
        }
        return Double.NaN;
    }

    public void plotRay(final int index) {
        ControlMessage currMsg = this.getControlMessage();
        this.lastRayPlotted = index;

        if (config.isRealtimeModeEnabled() || config.getPlottingMode() == Mode.Ray)
            for (final ViewPlotWindow win : wm.getPlotList()) {
                Ray prev = null;
                Ray next = null;
                Ray thresh = null;
                try {
                    prev = (Ray) this.controller.getRay(currMsg, win.getType(), index - 1);
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                } //ignore - previous not vital
                try {
                    next = (Ray) this.controller.getRay(currMsg, win.getType(), index + 1);
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                } //ignore - next not vital
                try {
                    thresh = (Ray) this.controller.getRay(currMsg, config.getThresholdType(), index);
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                } //ignore - threshold not vital
                try {
                    Ray curr = (Ray) this.controller.getRay(currMsg, win.getType(), index);
                    win.plot(prev, curr, next, thresh);
                    win.rePlotDrawingArea();
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    continue;
                } //better luck next window
            }

        for (final ViewAScopeWindow win : wm.getAScopeList()) {
            Ray secondary = null;
            try {
                secondary = (Ray) this.controller.getRay(currMsg, win.getSecondary(), index);
            } catch (ArrayIndexOutOfBoundsException aioobe) {
            } //ignore - secondary not vital
            try {
                Ray primary = (Ray) this.controller.getRay(currMsg, win.getType(), index);
                win.plot(primary, secondary);
                win.repaint(win.getVisibleRect());
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                continue;
            } //better luck next window
        }

        for (final NumDumpWindow win : wm.getNumDumpList()) {
            try {
                Ray ray = (Ray) this.controller.getRay(currMsg, win.getType(), index);
                Ray thr = (Ray) this.controller.getRay(currMsg, config.getThresholdType(), index);
                win.plot(ray, thr);
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                continue;
            } //better luck next window
        }


    }

    /**
     * Goes to the next sweep in the current volume if volume mode is enabled
     */
    public synchronized void sweepDone() {
        if (!this.plotQueue.stopping()) {
            switch (config.getPlottingMode()) {
                case Volume:
                    ViewFileBrowser.getInstance().getActions().selectNext();
                    break;
                case Continuous:
                    ViewFileBrowser.getInstance().getActions().selectNextAcrossBounds();
                    break;
            }
        }
    }

    /**
     * Goes to the first ray in the current sweep
     */
    public synchronized void rayFirst() {
        plotRay(0);
    }

    /**
     * Goes to the previous ray in the current sweep
     */
    public synchronized void rayPrev() {
        plotRay(Math.max(0, this.lastRayPlotted - config.getRayStep()));
    }

    /**
     * Goes to the next ray in the current sweep
     */
    public synchronized void rayNext() {
        plotRay(Math.min(
                this.controller.getNumberOfRays(this.controlMsg, config.getThresholdType()) - 1,
                this.lastRayPlotted + config.getRayStep()));
    }

    /**
     * Goes to the last ray in the current sweep
     */
    public synchronized void rayLast() {
        plotRay(this.controller.getNumberOfRays(
                this.controlMsg, config.getThresholdType()) - 1);
    }

    /**
     * Returns the map overlay for plotting
     *
     * @return a List of MapInstructions for plotting the map overlay
     */
    public synchronized List<MapInstruction> getMap() {
        return this.map;
    }


    public synchronized void createBookmark() {
        this.createBookmark(this.controlMsg);
    }

    public synchronized void createBookmark(final ControlMessage msg) {
        if (msg == null) {
            DialogUtil.showErrorDialog("No sweep selected", "Please select a sweep first");
            return;
        }
        BookmarkControl bmc = BookmarkControl.getInstance();

        Bookmark bookmark = new Bookmark();
        bookmark.url = msg.getURL();
        bookmark.dir = msg.getDir();
        bookmark.file = msg.getFile();
        bookmark.sweep = msg.getSweep();
        if (bookmark.url == null || bookmark.dir == null || bookmark.file == null || bookmark.sweep == null) {
            DialogUtil.showErrorDialog("No sweep selected", "Please select a sweep first");
            return;
        }
        bookmark.dir = bookmark.dir.split(" ")[0]; //chop off extra data
        String[] bits = bookmark.file.split(" ");
        bookmark.scan_type = bits.length > 1 ? bits[bits.length - 1] : "???";
        bookmark.file = bookmark.file.split(" ")[0];
        for (String type : sm.getTypes()) {
            ChillMomentFieldScale scale = sm.getScale(type);
            Bookmark.Scale bmScale = new Bookmark.Scale();
            bmScale.autoscale = false;
            bmScale.minval = Double.toString(scale.getMin());
            bmScale.maxval = Double.toString(scale.getMax());
            bookmark.scale.put(type, bmScale);
        }
        bookmark.x = config.getCenterX();
        bookmark.y = config.getCenterY();
        bookmark.range = config.getPlotRange();
        bookmark.rhi_height = Double.toString(config.getMaxPlotHeight());
        bookmark.ring = Integer.toString(config.getGridSpacing());

        //strip off prefix
        Collection<String> cats = bmc.getCategoryList();
        ArrayList<String> choices = new ArrayList<String>(cats.size());
        for (String cat : cats)
            if (cat.startsWith(BookmarkControl.USER_PREFIX))
                choices.add(cat.substring(cat.indexOf(":") + 1, cat.length()));
        //category
        final JComboBox category = new JComboBox(choices.toArray());
        category.setEditable(true);
        final JLabel categoryLabel = new JLabel("Category: ");
        categoryLabel.setDisplayedMnemonic('C');
        categoryLabel.setLabelFor(category);
        //name
        final JTextField name = new JTextField();
        final JLabel nameLabel = new JLabel("Name: ");
        nameLabel.setDisplayedMnemonic('N');
        nameLabel.setLabelFor(name);
        //comment
        final JTextArea comment = new JTextArea(25, 40);
        final JLabel commentLabel = new JLabel("Comment: ");
        commentLabel.setDisplayedMnemonic('m');
        commentLabel.setLabelFor(comment);
        //put it together
        JOptionPane pane = new JOptionPane(new Object[]{
                categoryLabel, category, "\n",
                nameLabel, name, "\n",
                commentLabel, new JScrollPane(comment),
        }, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        pane.setWantsInput(false);
        JDialog dialog = pane.createDialog(null, "Create Bookmark");
        dialog.pack();
        dialog.setVisible(true);
        //get result
        Integer value = (Integer) pane.getValue();
        if (value == null || value.intValue() == JOptionPane.CANCEL_OPTION || value.intValue() == JOptionPane.CLOSED_OPTION)
            return;
        bookmark.comment = comment.getText();
        bmc.addBookmark(BookmarkControl.USER_PREFIX + category.getSelectedItem(), name.getText(), bookmark);
    }

    public void clearCache() {
        System.out.println("Clearing cache");
        this.controller.clearCache();
    }
}
