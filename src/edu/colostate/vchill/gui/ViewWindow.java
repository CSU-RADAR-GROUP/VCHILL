package edu.colostate.vchill.gui;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.Config;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.ViewControl;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

/**
 * Superclass of ViewPlotWindow and ViewAScopeWindow
 *
 * @author Jochen Deyke
 * @version 2007-03-08
 */
public abstract class ViewWindow extends JPanel
{
    protected static final Config config = Config.getInstance();
    protected static final ViewControl vc = ViewControl.getInstance();
    protected static final WindowManager wm = WindowManager.getInstance();
    protected static final ScaleManager sm = ScaleManager.getInstance();
    protected String type;
    protected JInternalFrame parent;
    private boolean plotting;

    public ViewWindow ()
    {
        this.plotting = false;
    }

    public void setType (final String type)
    {
        this.type = type;
        parent.setTitle(sm.getScale(type).fieldDescription + " (" + this.type + ") " + getStyle());
        wm.calculateOpenWindows();
    }

    /**
     * @return the type being displayed
     */
    public String getType ()
    {
        return this.type;
    }

    /**
     * @param parent the encapsulating frame (used for resizing)
     */
    public void setParent (final JInternalFrame parent)
    {
        this.parent = parent;
    }

    /**
     * @param width the new width for the parent
     * @param height the new height for the parent
     */
    public void setSizes (final int width, final int height)
    {
        this.parent.setSize(width, height);
    }

    /**
     * @param plotting is a sweep currently being plotted?
     */
    public void setPlotting (final boolean plotting)
    {
        this.plotting = plotting;
    }

    /**
     * @return is a sweep currently being plotted?
     */
    public boolean isPlotting ()
    {
        return this.plotting;
    }

    /**
     * @return "Numerical", "Plot", "AScope", or "Histogram" 
     */
    public abstract String getStyle ();

    /**
     * @return a BufferedImage containing the same picture as the window,
     * or null if the operation is not supported
     */
    public BufferedImage getBufferedImage () { return null; }
}
