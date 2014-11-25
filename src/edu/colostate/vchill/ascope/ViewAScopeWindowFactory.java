package edu.colostate.vchill.ascope;

import edu.colostate.vchill.gui.ViewWindowFactory;

/**
 * A factory for creating ViewAScopeWindows.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class ViewAScopeWindowFactory extends ViewWindowFactory {
    /**
     * Constructor for the ViewPlotWindowFactory object
     */
    public ViewAScopeWindowFactory() {
    }

    /**
     * @param type The initial data type the new window will show
     * @return The new plot window
     */
    public ViewAScopeWindow createWindow(final String type) {
        if (type == null) return null;
        ViewAScopeWindow win = new ViewAScopeWindow(type);
        String thresh = vcc.getThresholdType();
        win.setSecondary(type.equals(thresh) ? null : thresh);
        ViewAScopeMouseListener listen = new ViewAScopeMouseListener();
        listen.initPopup(win);
        win.addMouseListener(listen);
        return win;
    }
}
