package edu.colostate.vchill.ascope;

import edu.colostate.vchill.gui.ViewWindowActions;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * This class is used to create a popup window.
 * The user creates the class and then calls "getPopup()"
 * to actually have access to it. This class could just
 * as easily extend JPopupMenu.
 *
 * @author  Jochen Deyke
 * @version 2007-03-08
 */
class ViewAScopePopup extends ViewWindowActions
{
    private JPopupMenu popup;
    private JMenu primaryMenu;
    private JMenu secondaryMenu;

    protected final ViewAScopeWindow win;

    /**
     * This creates the basic items, and then calls methods to actually
     * initialize all the different items we want to create.
     *
     * @param win the window this popup menu is to appear in
     */
    public ViewAScopePopup (final ViewAScopeWindow win)
    {
        super(win);
        this.win = win;

        //Init the main popup.
        this.popup = new JPopupMenu();

        makePrimaryTypeSubmenu();
        makeSecondaryTypeSubmenu(win);

        popup.addSeparator();

        //Make the items that will not go into a submenu
        makeMenuItems();

        sm.addObserver(new Observer () {
            public void update (final Observable o, final Object arg) {
                updatePrimaryMenu();
                updateSecondaryMenu();
            }});
    }

    /**
     * @return The popup menu for the client class to later call show on.
     */
    public JPopupMenu getPopup ()
    {
        return popup;
    }

    /**
     * A Submenu for changing the type of data the window is associated
     * with.  For example, if the Z submenu item is called, this window
     * will now request Z data.
     */
    public void makePrimaryTypeSubmenu ()
    {
        this.primaryMenu = new JMenu("Primary Type");
        this.updatePrimaryMenu();
        this.popup.add(primaryMenu);
    }

    public void updatePrimaryMenu ()
    {
        primaryMenu.removeAll();
        ButtonGroup group = new ButtonGroup();
        for (final String type : sm.getTypes()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                new AbstractAction(type) {
                    /**
                   * 
                   */
                  private static final long serialVersionUID = 869057841579678472L;

                    public void actionPerformed (final ActionEvent ae) {
                        win.setType(type); vc.rePlot();
                    }});
            item.setSelected(type.equals(win.getType()));
            group.add(item);
            primaryMenu.add(item).setIcon(null);
        }
    }

    public void makeSecondaryTypeSubmenu (final ViewAScopeWindow win)
    {
        this.secondaryMenu = new JMenu("Secondary Type");
        this.updateSecondaryMenu();
        this.popup.add(this.secondaryMenu);
    }

    public void updateSecondaryMenu ()
    {
        this.secondaryMenu.removeAll();
        ButtonGroup group = new ButtonGroup();
        for (final String type : sm.getTypes()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(
                new AbstractAction(type) {
                    /**
                   * 
                   */
                  private static final long serialVersionUID = 2611931680559758440L;

                    public void actionPerformed (final ActionEvent ae) {
                        win.setSecondary(type); vc.rePlot();
                    }});
            item.setSelected(type.equals(win.getSecondary()));
            group.add(item);
            this.secondaryMenu.add(item).setIcon(null);
        }
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(
            new AbstractAction(ViewAScopeWindow.TYPE_NOT_SET) {
                /**
               * 
               */
              private static final long serialVersionUID = -2034549514522756984L;

                public void actionPerformed (final ActionEvent ae) {
                    win.setSecondary(null); vc.rePlot();
                }});
        item.setSelected(win.getSecondary() == null);
        group.add(item);
        this.secondaryMenu.add(item).setIcon(null);
    }

    /**
     * Sets up the Menu Options such as Saving
     */
    private void makeMenuItems ()
    {
        for (JMenuItem i : makePopupItems()) this.popup.add(i).setIcon(null);
    }
}
