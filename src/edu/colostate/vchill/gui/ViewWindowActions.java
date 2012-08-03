package edu.colostate.vchill.gui;

import edu.colostate.vchill.Config;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.ViewControl;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
/*
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
*/

/**
 * Superclass of ViewPlotActions and ViewAScopeActions
 *
 * @author  Jochen Deyke
 * @version 2007-08-15
 */
public abstract class ViewWindowActions
{
    protected static final WindowManager wm = WindowManager.getInstance();
    protected static final Config config = Config.getInstance();
    protected static final ScaleManager sm = ScaleManager.getInstance();
    protected static final ViewControl vc = ViewControl.getInstance();

    protected final ViewFileBrowserActions vfba = ViewFileBrowser.getInstance().getActions();
    protected final ViewWindow win;

    public ViewWindowActions (final ViewWindow win)
    {
        this.win = win;
    }

    public Action[] makeNavigationActions ()
    {
        return new Action[] {
            new AbstractAction("Next") {
                /**
               * 
               */
              private static final long serialVersionUID = 7785577892493924229L;

                public void actionPerformed (final ActionEvent ae) {
                    vfba.selectNext();
                }},
            new AbstractAction("Previous") {
                /**
               * 
               */
              private static final long serialVersionUID = 7149480796360073848L;

                public void actionPerformed (final ActionEvent ae) {
                    vfba.selectPrev();
                }},
            new AbstractAction("Replot") {
                /**
               * 
               */
              private static final long serialVersionUID = -4205171199107498866L;

                public void actionPerformed (final ActionEvent ae) {
                    vc.rePlot();
                }},
        };
    }

    public JMenuItem[] makePopupItems ()
    {
        JMenuItem saveImage = new JMenuItem(new AbstractAction("Save Image") {
            /**
           * 
           */
          private static final long serialVersionUID = 8657839466523536283L;

            public void actionPerformed (final ActionEvent ae) {
                System.out.println("Saving image to buffer");
                wm.saveImage(win, vc.getControlMessage());
            }});
        //saveImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0)); //doesn't work as expected
        return new JMenuItem[] {saveImage};
    }

    public ViewWindow getWindow ()
    {
        return this.win;
    }
}
