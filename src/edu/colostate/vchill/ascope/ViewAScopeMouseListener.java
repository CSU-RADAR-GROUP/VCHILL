package edu.colostate.vchill.ascope;

import edu.colostate.vchill.gui.ViewWindowMouseListener;
import java.awt.event.MouseEvent;

/**
 * This class is the mouse listener that will be used by
 * the display window to allow mouse interaction.  It will
 * forward all requests to some form of communication interface
 * That will handle all the update requests etc.
 *
 * NOTE:  Before making right clicks the popupInit() method should
 * be called.  This needs to be changed at a later point.
 *
 * @author  Justin Carlson
 * @author  Jochen Deyke
 * @created January 07, 2003
 * @version 2006-08-31
 */
class ViewAScopeMouseListener extends ViewWindowMouseListener
{
    //The popup menu that the window will bring up on a right click.
    private ViewAScopePopup popup;

    /**
     * Call in order to actually use the popup window.  This should
     * be done in a constructer and BEFORE the user can right click.
     */
    public void initPopup (final ViewAScopeWindow win)
    {
        this.popup = new ViewAScopePopup(win);
    }

    @Override protected void markData (final MouseEvent e)
    {
        ((ViewAScopeWindow)e.getComponent()).markData(e.getX(), e.getY());
    }

    @Override protected void showPopup (final MouseEvent e)
    {
        this.popup.getPopup().show(e.getComponent(), e.getX(), e.getY());
    }

    @Override protected void hidePopup ()
    {
        this.popup.getPopup().setVisible(false);
    }
}
