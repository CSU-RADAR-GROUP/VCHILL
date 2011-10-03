package edu.colostate.vchill.plot;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.Config;
import edu.colostate.vchill.Config.GradientType;
import edu.colostate.vchill.DialogUtil;
import edu.colostate.vchill.EstimateParser;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.ViewControl;
import edu.colostate.vchill.ViewUtil;
import edu.colostate.vchill.gui.ViewFilterPanel;
import edu.colostate.vchill.gui.ViewRemotePanel;
import edu.colostate.vchill.gui.ViewWindowActions;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * This class is used to create a popup window. The user creates the class and
 * then calls "getPopup()" To actually have access to it.
 *
 * @author  Justin Carlson
 * @author  Jochen Deyke
 * @created January 04, 2003
 * @version 2007-09-26
 */
class ViewPlotPopup extends ViewWindowActions
{
    private static final ViewControl vc = ViewControl.getInstance();
    private static final Config config = Config.getInstance();
    private static final ScaleManager sm = ScaleManager.getInstance();
    private static final ViewRemotePanel vrp = ViewRemotePanel.getInstance();
    private static final ViewFilterPanel vfp = ViewFilterPanel.getInstance();
    private JPopupMenu popup;
    private JMenu typeMenu;
    private JMenu thresholdMenu;

    private JCheckBoxMenuItem checkBoxGrid;
    private JCheckBoxMenuItem checkBoxMap;
    private JCheckBoxMenuItem checkBoxAircraft;
    private JCheckBoxMenuItem checkBoxClickPoint;

    private JCheckBoxMenuItem checkBoxSmoothing;
    private JCheckBoxMenuItem checkBoxNoise;
    private JCheckBoxMenuItem checkBoxThreshold;

    private ButtonGroup modeGroup;

    private JRadioButtonMenuItem unfoldingOff;
    private JRadioButtonMenuItem unfoldingAutomatic;
    private JRadioButtonMenuItem unfoldingManual;

    private JRadioButtonMenuItem gradientOff;
    private JRadioButtonMenuItem gradientRange;
    private JRadioButtonMenuItem gradientAzimuth;

    private JMenuItem export;

    private volatile int x, y;

    protected final ViewPlotWindow win;

    /**
     * This creates the basic items, and then calls methods to actually
     * initialize all the different items we want to create.
     *
     * @param win The window this menu will be shown in
     */
    public ViewPlotPopup (final ViewPlotWindow win)
    {
        super(win);
        this.win = win;

        //Init the main popup.
        this.popup = new JPopupMenu();

        this.popup.add(new AbstractAction("Center here") {
            public void actionPerformed (final ActionEvent ae) {
                recenter(x, y);
            }});
        this.popup.add(new AbstractAction("Reset center") {
            public void actionPerformed (final ActionEvent ae) {
                config.setCenterX(0);
                config.setCenterY(0);
                wm.setCenterInKm();
                wm.clearScreen();
                vc.rePlot();
            }});

        //Add a seperator on the popup menu, pure apperance.
        this.popup.addSeparator();

        makeZoomSubmenu();
        makeOverlaySubmenu();
        makePlotFiltersSubmenu();
        makeWindowTypeSubmenu();
        //makeThresholdTypeSubmenu();
        makePlotTypeSubmenu();

        //Add a seperator on the popup menu, pure apperance.
        this.popup.addSeparator();

        //Make the items that will not go into a submenu, these
        //are actions such as set PPI Range etc.
        makeMenuItems();

        sm.addObserver(new Observer () {
            public void update (final Observable o, final Object arg) {
                updateTypeMenu();
                //updateThresholdMenu();
            }});
    }

    public void recenter (final int x, final int y)
    {
        win.recenter(x, y);
        wm.clearScreen();
        vc.rePlot();
    }

    /**
     * Makes the options dealing with zooming.
     */
    private void makeZoomSubmenu ()
    {
        JMenu subMenu = new JMenu("Zoom");
        subMenu.add(new AbstractAction("Reset zoom & center") {
            public void actionPerformed (final ActionEvent ae) {
                config.setPlotRange(config.getMaxPlotRange());
                vrp.getInstance().update();
                config.setCenterX(0);
                config.setCenterY(0);
                wm.setCenterInKm();
                wm.replotOverlay();
                wm.clearScreen();
                System.out.println("Reset zoom & center");
                vc.rePlot();
            }}).setIcon(null);
        final int maxZoom = 7;
        for (int z = 0; z < maxZoom; ++z) {
            final double zoom = Math.pow(2, z);
            final String label = zoom + "x Zoom";
            subMenu.add(new AbstractAction(label) {
                public void actionPerformed (final ActionEvent ae) {
                    config.setPlotRange(config.getMaxPlotRange() / zoom);
                    vrp.getInstance().update();
                    wm.setCenterInKm();
                    wm.replotOverlay();
                    wm.clearScreen();
                    System.out.println(label);
                    vc.rePlot();
                }}).setIcon(null);
        }
        this.popup.add(subMenu);
    }

    /**
     * Creates the two checkbox items that are used to turn on and off the
     * map underlays and ring overlays.
     */
    //NOTE: array indeces depend on order in ViewPlotActions.java
    private void makeOverlaySubmenu ()
    {
        JMenu subMenu = new JMenu("Overlays");

        checkBoxGrid = new JCheckBoxMenuItem(new AbstractAction("Draw Grids") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Grid on/off");
                config.toggleGridEnabled();
		wm.replotOverlay();
		wm.repaintPlotWindows();
            }});
        subMenu.add(checkBoxGrid);
        checkBoxMap = new JCheckBoxMenuItem(new AbstractAction("Draw Map") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Map on/off");
                config.toggleMapEnabled();
		wm.replotOverlay();
		wm.repaintPlotWindows();
            }});
        subMenu.add(checkBoxMap); //functionality now in main menu
        checkBoxAircraft = new JCheckBoxMenuItem(new AbstractAction("Draw Aircraft") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Aircraft on/off");
                config.toggleAircraftEnabled();
                //vc.rePlot();
		wm.repaintPlotWindows();
            }});
        subMenu.add(checkBoxAircraft);
        checkBoxClickPoint = new JCheckBoxMenuItem(new AbstractAction("Draw Click Point") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Click Point on/off");
                config.toggleClickPointEnabled();
		wm.repaintPlotWindows();
            }});
        subMenu.add(checkBoxClickPoint);

        this.popup.add(subMenu);
    }

    //NOTE: array indeces depend on order in ViewPlotActions.java
    private void makePlotFiltersSubmenu ()
    {
        JMenu subMenu = new JMenu("Filters");
        ButtonGroup unfoldingGroup = new ButtonGroup();
        ButtonGroup gradientGroup = new ButtonGroup();

        checkBoxThreshold = new JCheckBoxMenuItem(new AbstractAction("Threshold Filter") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Threshold Toggle");
                config.toggleThresholdEnabled();
                vrp.getInstance().update();
                vfp.update();
                wm.calculateOpenWindows();
                vc.rePlot();
            }});
        subMenu.add(checkBoxThreshold);

        checkBoxSmoothing = new JCheckBoxMenuItem(new AbstractAction("Smoothing") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Smooth Toggle");
                config.toggleSmoothingEnabled();
                vfp.update();
                vc.rePlot();
            }});
        subMenu.add(checkBoxSmoothing);

        checkBoxNoise = new JCheckBoxMenuItem(new AbstractAction("Noise Filter") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Noise Toggle");
                config.toggleNoiseReductionEnabled();
                vfp.update();
                vc.rePlot();
            }});
        subMenu.add(checkBoxNoise);

        JMenu subSubMenu = new JMenu("Velocity Unfolding");
        unfoldingOff = new JRadioButtonMenuItem(new AbstractAction("No Unfolding") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Disable Velocity Unfolding");
                config.setUnfoldingEnabled(false);
                vfp.update();
                vc.rePlot();
            }});
        unfoldingOff.setIcon(null);
        unfoldingGroup.add(unfoldingOff);
        subSubMenu.add(unfoldingOff);
        unfoldingAutomatic = new JRadioButtonMenuItem(new AbstractAction("Automatic Unfolding") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Automatic Velocity Unfolding");
                config.setUnfoldingEnabled(true);
                config.setUnfoldingAutomatic(true);
                vfp.update();
                vc.rePlot();
            }});
        unfoldingAutomatic.setIcon(null);
        unfoldingGroup.add(unfoldingAutomatic);
        subSubMenu.add(unfoldingAutomatic);
        unfoldingManual = new JRadioButtonMenuItem(new AbstractAction("Manual Unfolding") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Manual Velocity Unfolding");
                config.setUnfoldingEnabled(true);
                config.setUnfoldingAutomatic(false);
                vfp.update();
                vc.rePlot();
            }});
        unfoldingManual.setIcon(null);
        unfoldingGroup.add(unfoldingManual);
        subSubMenu.add(unfoldingManual);
        JMenuItem inputEstimates = new JMenuItem(new AbstractAction("Input New Velocity Estimates") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Input Manual Unfolding Estimates");
                String result = DialogUtil.showMultilineInputDialog("Input", "Please enter estimated velocities, one per line.\nFormat is <height in km><space><estimated velocity in m/s>\nEntered values remain in effect until the program is shut down or new numbers are entered.\nOrdering of heights is not important. Faulty lines are ignored.", EstimateParser.getInstance().toString());
                if (result == null) return; //canceled;
                EstimateParser.getInstance().parse(result);
                config.setUnfoldingEnabled(true);
                config.setUnfoldingAutomatic(false);
                vfp.update();
                vc.rePlot();
            }});
        subSubMenu.add(inputEstimates);
        subMenu.add(subSubMenu);

        subSubMenu = new JMenu("Z as Gradient");
        gradientOff = new JRadioButtonMenuItem(new AbstractAction("Gradient Off") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Gradient Off");
                config.setGradientType(GradientType.Off);
                vfp.update();
                vc.rePlot();
            }});
        gradientOff.setIcon(null);
        gradientGroup.add(gradientOff);
        subSubMenu.add(gradientOff);
        gradientRange = new JRadioButtonMenuItem(new AbstractAction("Range Gradient") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Z Range Gradient");
                config.setGradientType(GradientType.Range);
                vfp.update();
                vc.rePlot();
            }});
        gradientRange.setIcon(null);
        gradientGroup.add(gradientRange);
        subSubMenu.add(gradientRange);
        gradientAzimuth = new JRadioButtonMenuItem(new AbstractAction("Azimuth Gradient") {
            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Z Azimuth Gradient");
                config.setGradientType(GradientType.Azimuth);
                vfp.update();
                vc.rePlot();
            }});
        gradientAzimuth.setIcon(null);
        gradientGroup.add(gradientAzimuth);
        subSubMenu.add(gradientAzimuth);
        subMenu.add(subSubMenu);

        this.popup.add(subMenu);
    }

    public void resetInfo ()
    {
        export.setEnabled(win.isExportable());
        checkBoxGrid.setState(config.isGridEnabled());
        checkBoxMap.setState(config.isMapEnabled());
        checkBoxAircraft.setState(config.isAircraftEnabled());
        checkBoxClickPoint.setState(config.isClickPointEnabled());

        checkBoxThreshold.setState(config.isThresholdEnabled());
        checkBoxSmoothing.setState(config.isSmoothingEnabled());
        checkBoxNoise.setState(config.isNoiseReductionEnabled());

        if (config.isUnfoldingEnabled()) {
            if (config.isUnfoldingAutomatic()) unfoldingAutomatic.setSelected(true);
            else unfoldingManual.setSelected(true);
        } else unfoldingOff.setSelected(true);

        switch (config.getGradientType()) {
            case Range: gradientRange.setSelected(true); break;
            case Azimuth: gradientAzimuth.setSelected(true); break;
            default: gradientOff.setSelected(true); break;
        }

        Enumeration buttons = modeGroup.getElements();
        while (buttons.hasMoreElements()) {
            JRadioButtonMenuItem tmp = (JRadioButtonMenuItem)buttons.nextElement();
            if (win.getPlotMode().equals(tmp.getText())) {
                tmp.setSelected(true);
                break;
            }
        }
    }

    /**
     * A Submenu for changing the type of data the window is associated
     * with.  For example, if the Z submenu item is called, this window
     * will now request Z data.
     */
    public void makeWindowTypeSubmenu ()
    {
        this.typeMenu = new JMenu("Window Type");
        this.updateTypeMenu();
        this.popup.add(this.typeMenu);
    }

    public void updateTypeMenu ()
    {
        this.typeMenu.removeAll();
        ButtonGroup typeGroup = new ButtonGroup();
        for (final String type : sm.getTypes()) {
            JRadioButtonMenuItem button = new JRadioButtonMenuItem(new AbstractAction(type) {
                public void actionPerformed (final ActionEvent ae) {
                    System.out.println("Type set to: " + type);
                    win.setType(type);
                    win.rePlotDrawingArea();
                    vc.rePlot();
                }});
            button.setSelected(win.getType().equals(type));
            typeGroup.add(button);
            this.typeMenu.add(button).setIcon(null);

        }
    }

    public void makeThresholdTypeSubmenu ()
    {
        this.thresholdMenu = new JMenu("Threshold Type");
        this.updateThresholdMenu();
        this.popup.add(this.thresholdMenu);
    }

    public void updateThresholdMenu ()
    {
        this.thresholdMenu.removeAll();
        ButtonGroup thresholdGroup = new ButtonGroup();
        for (final String type : sm.getTypes()) {
            JRadioButtonMenuItem button = new JRadioButtonMenuItem(new AbstractAction(type) {
                public void actionPerformed (final ActionEvent ae) {
                    System.out.println("Threshold type set to: " + type);
                    config.setThresholdType(type);
                    wm.calculateOpenWindows();
                    vc.rePlot();
                }});
            button.setSelected(type.equals(config.getThresholdType()));
            thresholdGroup.add(button);
            this.thresholdMenu.add(button).setIcon(null);
        }
    }

    private void makePlotTypeSubmenu ()
    {
        JMenu subMenu = new JMenu("Plot as");
        modeGroup = new ButtonGroup();
        for (Action a : new Action[] {
            new AbstractAction("<auto>") {
                public void actionPerformed (final ActionEvent ae) {
                    wm.setMode("auto");
                    vc.rePlot();
                }},
            new AbstractAction("PPI") {
                public void actionPerformed (final ActionEvent ae) {
                    wm.setMode("PPI");
                    vc.rePlot();
                }},
            new AbstractAction("RHI") {
                public void actionPerformed (final ActionEvent ae) {
                    wm.setMode("RHI");
                    vc.rePlot();
                }},
            new AbstractAction("Time/Height") {
                public void actionPerformed (final ActionEvent ae) {
                    wm.setMode("MAN");
                    vc.rePlot();
                }},
        }) {
            JRadioButtonMenuItem button = new JRadioButtonMenuItem(a);
            button.setIcon(null);
            modeGroup.add(button);
            subMenu.add(button);
        }
        this.popup.add(subMenu);
    }

    /**
     * Sets up the Menu Options such as Saving
     */
    private void makeMenuItems ()
    {
        for (JMenuItem i : makePopupItems()) this.popup.add(i).setIcon(null);
        final String osname = System.getProperty("os.name");
        export = new JMenuItem(new AbstractAction("Launch Google Earth") {
            public void actionPerformed (final ActionEvent ae) {
                try {
                    String zipfile = win.export(null); //to tmpfile
                    if (zipfile != null) DialogUtil.open(zipfile);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }});
        if (osname.startsWith("Windows") || osname.equals("Mac OS X") || osname.equals("Linux")) this.popup.add(export).setIcon(null);
    }

    void show (final MouseEvent e)
    {
        resetInfo();
        this.popup.show(e.getComponent(), x = e.getX(), y = e.getY());
    }

    void hide ()
    {
        this.popup.setVisible(false);
    }
}
