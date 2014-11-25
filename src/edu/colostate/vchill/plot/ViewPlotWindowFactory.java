package edu.colostate.vchill.plot;

import edu.colostate.vchill.gui.ViewWindowFactory;

/**
 * Factory for creating ViewPlotWindow objects
 *
 * @author Justin Carlson
 * @author Jochen Deyke
 * @version 2006-08-30
 * @created April 25, 2003
 */
public class ViewPlotWindowFactory extends ViewWindowFactory {
    /**
     * Constructor for the ViewPlotWindowFactory object
     */
    public ViewPlotWindowFactory() {
    }

    /**
     * This method is far to complex and most of its setup should in
     * fact be moved to the ViewPlotWindow class in its constructer.
     * There is no reason for the factory to create so much that the
     * ViewPlotWindow will have to change internally anyway.
     *
     * @param type The initial data type the new window will show
     * @return The new plot window
     */
    public ViewPlotWindow createWindow(final String type) {
        ViewPlotWindow win = new ViewPlotWindow(type);

        //This is the mouse listener that is used to detect mouse clicks in
        //the user space.  It passes a reference to the window in order to
        //allow this module to remain pretty much totally independent of
        //any others.
        ViewPlotMouseListener listen = new ViewPlotMouseListener();
        listen.initPopup(win);
        win.addMouseListener(listen);
        win.addMouseMotionListener(listen);
        win.addMouseWheelListener(listen);
        return win;
    }
}
