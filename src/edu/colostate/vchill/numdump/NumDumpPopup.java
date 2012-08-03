package edu.colostate.vchill.numdump;

import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.ViewControl;
import edu.colostate.vchill.gui.ViewWindowActions;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * This class is used to create the popup context menu for numdump windows.
 * To access the menu, call getPopup() on an instance of this class.
 * Since the menu is fairly small, the actions are defined in the same class. 
 *
 * @author  Jochen Deyke
 * @version 2007-09-26
 */
class NumDumpPopup extends ViewWindowActions
{
    private JPopupMenu popup;
    private static final ViewControl vc = ViewControl.getInstance();
    private JMenu typeMenu;

    public NumDumpPopup (final NumDumpWindow win)
    {
        super(win);
        this.popup = new JPopupMenu();

        { //window type submenu
            this.typeMenu = new JMenu("Data Type");
            this.updateTypeMenu();
            this.popup.add(typeMenu);
            sm.addObserver(new Observer () {
                public void update (final Observable o, final Object arg) {
                    updateTypeMenu();
                }});
        }

        { //items not in a submenu
            (this.popup.add(new AbstractAction("Redirect to file...") {
		/**
               * 
               */
              private static final long serialVersionUID = -5037694088704534477L;
    File lastDir;
                public void actionPerformed (final ActionEvent ae) {
                    JFileChooser chooser = new JFileChooser(lastDir);
                    int returnVal = chooser.showSaveDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        win.redirectTo(lastDir = chooser.getSelectedFile());
                    } else {
                        win.redirectTo(null); //draw to screen
                    }
                }})).setIcon(null);
        }
    }

    public void updateTypeMenu ()
    {
        this.typeMenu.removeAll();
        ButtonGroup group = new ButtonGroup();
        for (final String m : ScaleManager.getInstance().getTypes()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(new AbstractAction(m) {
                /**
               * 
               */
              private static final long serialVersionUID = -2045770180194940986L;

                public void actionPerformed (final ActionEvent ae) { win.setType(m); }});
            item.setSelected(win.getType().equals(m));
            group.add(item);
            this.typeMenu.add(item).setIcon(null);
        }
    }

    public JPopupMenu getPopup ()
    {
        return this.popup;
    }
}
