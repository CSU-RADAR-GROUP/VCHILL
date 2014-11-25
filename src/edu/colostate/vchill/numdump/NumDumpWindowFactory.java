package edu.colostate.vchill.numdump;

import edu.colostate.vchill.gui.ViewWindowFactory;

/**
 * Factory for creating NumDumpWindow objects
 *
 * @author Jochen Deyke
 * @version 2006-01-04
 * @created April 25, 2003
 */
public class NumDumpWindowFactory extends ViewWindowFactory {
    /**
     * Constructor for the NumDumpWindowFactory object
     */
    public NumDumpWindowFactory() {
    }

    /**
     * @param type "Z", "V", etc
     */
    public NumDumpWindow createWindow(final String type) {
        NumDumpWindow win = new NumDumpWindow(type);
        NumDumpMouseListener listen = new NumDumpMouseListener();
        listen.initPopup(win);
        win.addMouseListener(listen);
        return win;
    }
}
