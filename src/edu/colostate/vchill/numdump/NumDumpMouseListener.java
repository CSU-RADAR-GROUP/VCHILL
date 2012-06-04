package edu.colostate.vchill.numdump;

import edu.colostate.vchill.gui.ViewWindowMouseListener;
import java.awt.event.MouseEvent;

/**
 * NOTE:  Before making right clicks the popupInit() method should
 * be called.  This needs to be changed at a later point.
 *
 * @author  Jochen Deyke
 * @version 2006-08-31
 */
class NumDumpMouseListener extends ViewWindowMouseListener
{
    /** The popup menu that the window will bring up on a right click */
    private NumDumpPopup popup;

    /**
     * Call in order to actually use the popup window.  This should
     * be done in a constructer and BEFORE the user can right click.
     */
    public void initPopup (final NumDumpWindow win)
    {
        this.popup = new NumDumpPopup(win);
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
