package edu.colostate.vchill.gui;

import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.DialogUtil;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.ascope.ViewAScopeWindow;
import edu.colostate.vchill.ascope.ViewAScopeWindowFactory;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.numdump.NumDumpWindow;
import edu.colostate.vchill.numdump.NumDumpWindowFactory;
import edu.colostate.vchill.plot.ViewPlotWindow;
import edu.colostate.vchill.plot.ViewPlotWindowFactory;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class manages all the different types of window - size, initial position, etc.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-06-26
 */
public class WindowManager {
    /**
     * Singleton instance - only one WindowManager allowed per program
     */
    private static final WindowManager wm = new WindowManager();

    /**
     * Source for default settings - must load this first
     */
    private static final edu.colostate.vchill.Config config = edu.colostate.vchill.Config.getInstance();

    /**
     * Scaling information
     */
    private static final ScaleManager sm = ScaleManager.getInstance();

    /**
     * Panel to control scaling etc
     */
    private static final ViewRemotePanel vrp = ViewRemotePanel.getInstance();

    /**
     * The number of open windows.
     * Used to determine placement of new windows.
     */
    private int numWins;

    /**
     * Reference to add the created windows to
     */
    private ViewMain viewMain;

    /**
     * in degrees
     */
    private double clickAz = 0;
    private double clickEl = 0;
    /**
     * in km from radar
     */
    private double clickRange = 0;

    private int ascopeWidth;
    private int ascopeHeight;

    private int plotWidth;
    private int plotHeight;
    private int plotDivider;

    /**
     * Creates the different plotting windows as required
     */
    private final ViewPlotWindowFactory plotFactory = new ViewPlotWindowFactory();
    private final ViewAScopeWindowFactory ascopeFactory = new ViewAScopeWindowFactory();
    private final NumDumpWindowFactory numdumpFactory = new NumDumpWindowFactory();

    /**
     * Handles all the different window actions and keeps track of active windows.
     */
    private final List<ViewPlotWindow> plots = new CopyOnWriteArrayList<ViewPlotWindow>();
    private final List<ViewAScopeWindow> ascopes = new CopyOnWriteArrayList<ViewAScopeWindow>();
    private final List<NumDumpWindow> numdumps = new CopyOnWriteArrayList<NumDumpWindow>();

    /**
     * Buffer for saved images
     */
    private ViewImageDisplay imageDisplay;

    /**
     * Bitmask of data displayed by open windows; includes thresholding if enabled
     */
    private long openWindows = 0;

    /**
     * Private default constructor prevents instantiation
     */
    private WindowManager() {
        this.plotWidth = this.ascopeWidth = 640 + 2; //image+annotation + border
        this.ascopeHeight = 240 + 56; //image + title+border
        this.plotHeight = 480 + 56; //image + title+border
        this.plotDivider = 512;
    }

    public static WindowManager getInstance() {
        return wm;
    }

    public synchronized void showImages() {
        if (this.imageDisplay == null)
            this.imageDisplay = new ViewImageDisplay();
        this.imageDisplay.displayImage(plotWidth, plotHeight);
    }

    public synchronized void export(final ControlMessage msg) {
        for (ViewPlotWindow win : this.plots) this.export(win, msg);
    }

    public synchronized void export(final ViewPlotWindow win, final ControlMessage msg) {
        try {
            if (config.isSaveToDiskEnabled()) {
                String filename = config.getSaveToDiskPath() + "/" + win.getType() + "." + msg.getFile() + " " + msg.getSweep() + " " + win.getStyle() + ".kmz";
                win.export(filename);
            } else {
                String filename = win.export(null); //tmpfile
                if (filename != null) DialogUtil.open(filename);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Saves an image to the cyclic image buffer
     *
     * @param win the window to save
     * @param msg the ControlMessage to get details from
     */
    public synchronized void saveImage(final ViewWindow win, final ControlMessage msg) {
        if (this.imageDisplay == null)
            this.imageDisplay = new ViewImageDisplay();
        this.imageDisplay.addImage(win.getType(), new SavedImage<BufferedImage>(
                win.getBufferedImage(), msg.getFile() + " " + msg.getSweep() + " " + win.getStyle()));
    }

    public synchronized void savePlotImages(final ControlMessage msg) {
        System.out.println("Saving all plots to image buffer");
        for (final ViewWindow win : this.plots) saveImage(win, msg);
    }

    public synchronized void saveAScopeImages(final ControlMessage msg) {
        System.out.println("Saving all ascopes to image buffer");
        for (final ViewWindow win : this.ascopes) saveImage(win, msg);
    }

    /**
     * Sets the main window to add created windows to
     *
     * @param main The main window (used when creating new plot (etc) windows)
     */
    public synchronized void setMain(final ViewMain main) {
        this.viewMain = main;
    }

    public synchronized void createPlotWindow(final String type) {
        if (sm.getScale(type) == null) return;
        for (ViewPlotWindow win : this.plots) if (win.getType().equals(type)) return;
        ViewPlotWindow win = plotFactory.createWindow(type);
        win.setParent(this.createWindow(win, this.plotWidth, this.plotHeight));
        win.setType(type);
        this.plots.add(win);
        this.calculateOpenWindows();
        win.updateSizes(); //causes replot
        win.setClickRay(this.clickAz, this.clickEl, this.clickRange, -1, -1);
        win.setCenterInKm();
    }

    public synchronized void createAScopeWindow(final String type) {
        if (sm.getScale(type) == null) return;
        for (ViewAScopeWindow win : this.ascopes) if (win.getType().equals(type)) return;
        ViewAScopeWindow win = ascopeFactory.createWindow(type);
        win.setParent(this.createWindow(win, this.ascopeWidth, this.ascopeHeight));
        win.setType(type);
        this.ascopes.add(win);
        this.calculateOpenWindows();
        win.setSizes(this.ascopeWidth, this.ascopeHeight);
        win.setClickRange(this.clickRange);
    }

    public synchronized void createNumDumpWindow(final String type) {
        if (sm.getScale(type) == null) return;
        for (NumDumpWindow win : this.numdumps) if (win.getType().equals(type)) return;
        NumDumpWindow win = numdumpFactory.createWindow(type);
        win.setParent(this.createWindow(win, 320, 480));
        win.setType(type);
        this.numdumps.add(win);
        this.calculateOpenWindows();
    }


    public synchronized long calculateOpenWindows() {
        ChillMomentFieldScale scale;
        this.openWindows = this.numWins = 0;

        for (final ViewPlotWindow win : this.plots) {
            ++numWins;
            scale = sm.getScale(win.getType());
            if (scale != null) this.openWindows |= 1l << scale.fieldNumber;
        }
        for (final ViewAScopeWindow win : this.ascopes) {
            ++numWins;
            scale = sm.getScale(win.getType());
            if (scale != null) this.openWindows |= 1l << scale.fieldNumber;
            String secondary = win.getSecondary();
            if (secondary != null) {
                scale = sm.getScale(secondary);
                this.openWindows |= 1l << scale.fieldNumber; //watch for nulls
            }
        }
        for (final NumDumpWindow win : this.numdumps) {
            ++numWins;
            scale = sm.getScale(win.getType());
            this.openWindows |= 1l << scale.fieldNumber;
        }

        //System.out.println("Open windows = 0x" + Integer.toHexString(this.openWindows));
        scale = sm.getScale(config.getThresholdType());
        if (this.openWindows != 0) this.openWindows |= scale != null ? 1l << scale.fieldNumber : 0;
        return this.openWindows;
    }

    public synchronized long getOpenWindows() {
        return this.openWindows;
    }

    public synchronized void resizeAScopes(final int width, final int height) {
        this.ascopeWidth = width;
        this.ascopeHeight = height;
        for (ViewAScopeWindow win : this.ascopes) win.setSizes(width, height);
    }

    public synchronized void resizePlots() {
        for (ViewPlotWindow win : this.plots) win.updateSizes();
    }

    public synchronized void resetPlotDividerLocation() {
        for (ViewPlotWindow win : this.plots) win.updateDividerLocation();
    }

    /**
     * Removes the specified window from the active list
     *
     * @param win the window to remove
     * @return true if the window was successfully removed, false otherwise
     */
    public synchronized boolean removeWindow(final ViewWindow win) {
        if (this.plots.remove(win) || this.ascopes.remove(win) || this.numdumps.remove(win)) {
            this.calculateOpenWindows();
            return true; //successfully removed
        }
        return false;
    }

    public synchronized void changeWindowColors() {
        for (ViewPlotWindow win : this.plots) win.setColors();
    }

    public synchronized void setMode(final String mode) {
        this.setMode(mode, true);
    }

    public synchronized void setMode(final String mode, final boolean override) {
        for (ViewPlotWindow win : this.plots) win.setMode(mode, override);
    }

    public synchronized void setCenterInKm() {
        for (ViewPlotWindow win : this.plots) win.setCenterInKm();
    }

    public synchronized void clearScreen() {
        for (ViewPlotWindow win : this.plots) win.clearScreen();
    }

    public void replotOverlay() {
        for (ViewPlotWindow win : this.plots) win.replotOverlay();
    }

    public void clearAnnotationLayer() {

        for (ViewPlotWindow win : this.plots)
            win.clearAnnotationBuffer();

    }

    public void repaintPlotWindows() {
        for (ViewPlotWindow win : this.plots) win.repaint(win.getVisibleRect());
    }

    /**
     * Sets the click point of every plotting window to the specified coordinates.
     * The click point is used to highlight features of interest.
     *
     * @param azimuth   the azimuth angle (in degrees) of the radar
     * @param elevation the elevation angle (in degrees) of the radar
     * @param range     the distance (in km) from the radar
     * @param x         X coordinate of where the mouse was clicked
     * @param y         Y coordinate of where the mouse was clicked
     */
    public synchronized void setClickRay(final double azimuth, final double elevation, final double range, final int x, final int y) {
        this.clickAz = azimuth;
        this.clickEl = elevation;
        this.clickRange = range;

        for (ViewPlotWindow win : this.plots) win.setClickRay(azimuth, elevation, range, x, y);
        for (ViewAScopeWindow win : this.ascopes) win.setClickRange(range);
    }

    /**
     * Sets the horizontal click line's height in all AScope windows
     *
     * @param height the fractional portion of the height that should be
     *               below the line
     */
    public synchronized void setAScopeClickY(final double height) {
        for (ViewAScopeWindow win : this.ascopes) win.setClickY(height);
    }

    public List<ViewAScopeWindow> getAScopeList() {
        return Collections.unmodifiableList(this.ascopes);
    }

    public List<NumDumpWindow> getNumDumpList() {
        return Collections.unmodifiableList(this.numdumps);
    }

    public List<ViewPlotWindow> getPlotList() {
        return Collections.unmodifiableList(this.plots);
    }

    public synchronized void setPlotWindowWidth(final int plotWidth) {
        this.plotWidth = plotWidth;
    }

    public synchronized int getPlotWindowWidth() {
        return this.plotWidth;
    }

    public synchronized void setPlotWindowHeight(final int plotHeight) {
        this.plotHeight = plotHeight;
    }

    public synchronized int getPlotWindowHeight() {
        return this.plotHeight;
    }

    public synchronized void setPlotDividerLocation(final int plotDivider) {
        this.plotDivider = plotDivider;
    }

    public synchronized int getPlotDividerLocation() {
        return this.plotDivider;
    }

    public synchronized void setPlotting(final boolean plotting) {
        for (final ViewWindow win : this.plots) win.setPlotting(plotting);
        for (final ViewWindow win : this.ascopes) win.setPlotting(plotting);
        for (final ViewWindow win : this.numdumps) win.setPlotting(plotting);
    }

    /**
     * This method creates an internal window and encapsulates it into a
     * JInternalFrame.
     * This window is then added to the internal desktop for display
     * purposes.
     *
     * @param win the ViewWindow to make into a JInternalFrame
     * @return the created window
     */
    private synchronized JInternalFrame createWindow(final ViewWindow win, final int width, final int height) {
        int offset = numWins / 4 * 24;
        JDesktopPane desktop = viewMain.getDesktop();

        JInternalFrame frame = new JInternalFrame(
                null,  //title
                true,  //resizable
                true,  //closable
                false, //maximizable
                true); //iconifiable
        frame.setContentPane(win);
        frame.pack();
        frame.setSize(width, height);
        switch (numWins % 4) { //set the upper left corner's position
            case 0:
                frame.setLocation(offset, offset);
                break;
            case 1:
                frame.setLocation(desktop.getWidth() - width - offset, desktop.getHeight() - height - offset);
                break;
            case 2:
                frame.setLocation(desktop.getWidth() - width - offset, offset);
                break;
            case 3:
                frame.setLocation(offset, desktop.getHeight() - height - offset);
                break;
        }

        win.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent fe) {
                vrp.setSelection(win.getType());
            }
        });

        frame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent ife) {
                wm.removeWindow(win);
            }
        });

        frame.setVisible(true);
        desktop.add(frame);
        frame.toFront();
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException pve) {
            System.out.println(pve.toString());
        }
        return frame;
    }

    public JFrame getMainWindow() {
        return this.viewMain.getWindow();
    }

    public JDesktopPane getDesktop() {
        return this.viewMain.getDesktop();
    }
}
