package edu.colostate.vchill.gui;

import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.ViewControl;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * These Actions are used to create the toolbars and Menus that are used in the
 * main window. None of the internal frame menus access these actions. This
 * list may still need some serious work to ensure things remain modular.
 *
 * @author  Justin Carlson
 * @author  Jochen Deyke
 * @author  Alexander Deyke
 * @created January 06, 2003
 * @version 2007-08-15
 */
public class ViewActions
{
    public static final WindowManager wm = WindowManager.getInstance();
    public static final ViewControl vc = ViewControl.getInstance();

    private final ViewFileBrowserActions vfba = ViewFileBrowser.getInstance().getActions();
    //All of the following reference only the main model through singleton
    //calls, these actions do nothing themselves, they only pass control to
    //the correct areas in the ViewMain part of the view.

    //Actions used by the Remote Panel.   NOTE:   Currently can't be
    //changed into a Action[] due to layout issues.  May not be needed.
    public final Action doNextAction;
    public final Action doPrevAction;
    public final Action doLastAction;
    public final Action doFirstAction;
    public final Action doStopAction;
    public final Action doFirstRayAction;
    public final Action doPrevRayAction;
    public final Action doNextRayAction;
    public final Action doLastRayAction;

    private final Collection<String> types = ScaleManager.getInstance().getTypes();
    
    /**
     * Constructor for the ViewActions object
     */
    public ViewActions()
    {
        doNextAction =
            new AbstractAction("Next") {
                /**
               * 
               */
              private static final long serialVersionUID = 2532701619168902002L;

                public void actionPerformed (ActionEvent ae) {
                    vfba.selectNext();
                }};
        doPrevAction =
            new AbstractAction("Prev") {
                /**
               * 
               */
              private static final long serialVersionUID = 7137140978150622160L;

                public void actionPerformed (ActionEvent ae) {
                    vfba.selectPrev();
                }};
        doLastAction =
            new AbstractAction("Last") {
                /**
               * 
               */
              private static final long serialVersionUID = 6476880971754906454L;

                public void actionPerformed (ActionEvent ae) {
                    vfba.selectLast();
                }};
        doFirstAction =
            new AbstractAction("First") {
                /**
               * 
               */
              private static final long serialVersionUID = 7783824171702505442L;

                public void actionPerformed (ActionEvent ae) {
                    vfba.selectFirst();
                }};
        doStopAction =
            new AbstractAction("Stop") {
                /**
               * 
               */
              private static final long serialVersionUID = 3460939122739788620L;

                public void actionPerformed (ActionEvent ae) {
                    vc.stopPlot();
                }};
        doFirstRayAction =
            new AbstractAction("|<") {
                /**
               * 
               */
              private static final long serialVersionUID = 3032844275929918298L;

                public void actionPerformed (ActionEvent ae) {
                    vc.rayFirst();
                }};
        doPrevRayAction =
            new AbstractAction("<<") {
                /**
               * 
               */
              private static final long serialVersionUID = -4853391380146285808L;

                public void actionPerformed (ActionEvent ae) {
                    vc.rayPrev();
                }};
        doNextRayAction =
            new AbstractAction(">>") {
                /**
               * 
               */
              private static final long serialVersionUID = -7261907786214603837L;

                public void actionPerformed (ActionEvent ae) {
                    vc.rayNext();
                }};
        doLastRayAction =
            new AbstractAction(">|") {
                /**
               * 
               */
              private static final long serialVersionUID = 6813861441794895471L;

                public void actionPerformed (ActionEvent ae) {
                    vc.rayLast();
                }};
    }

    public void connect(String connection)
    {
        vc.connect(connection);
    }

    public Action[] makeWindowCreationActions()
    {
        Action[] theWindowActions = new Action[this.types.size()];
        { int i = 0; for (final String type : this.types) {
            theWindowActions[i] = 
                new AbstractAction(type) {
                    /**
                   * 
                   */
                  private static final long serialVersionUID = 6821822947347147168L;

                    public void actionPerformed (ActionEvent ae) {
                        wm.createPlotWindow(type);
                    }};
        ++i; }}
        return theWindowActions;
    }
 
    public Action[] makeAScopeWindowCreationActions()
    {
        Action[] theWindowActions = new Action[this.types.size()];
        { int i = 0; for (final String type : this.types) {
            theWindowActions[i] = 
                new AbstractAction(type) {
                    /**
                   * 
                   */
                  private static final long serialVersionUID = -5318191173271453764L;

                    public void actionPerformed (ActionEvent ae) {
                        wm.createAScopeWindow(type);
                        vc.rePlot();
                    }};
        ++i; }}
        return theWindowActions;
    }

    public Action[] makeDataManipulationActions()
    {
        Action[] arr = new Action[1];
        arr[0] =
            new AbstractAction("Replot") {
                /**
               * 
               */
              private static final long serialVersionUID = -4278335215193660348L;

                public void actionPerformed (ActionEvent ae) {
                    vc.rePlot();
                }};
        arr[0].putValue("Type", "Replot");
        arr[0].putValue("Hint", "Causes all of the windows to replot the last sweep.");
        return arr;
    }
}
