package edu.colostate.vchill.gui;

import edu.colostate.vchill.Config;

/**
 * Superclass of ViewPlotWindowFactory and ViewAScopeWindowFactory
 *
 * @author Jochen Deyke
 * @version 2006-01-04
 */
public abstract class ViewWindowFactory
{
    protected static final Config vcc = Config.getInstance();

    public abstract ViewWindow createWindow (final String type);
}
