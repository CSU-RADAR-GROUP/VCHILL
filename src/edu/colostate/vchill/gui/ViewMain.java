package edu.colostate.vchill.gui;

import edu.colostate.vchill.ViewControl;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

/**
 * The main window of the VCHILL program.
 *
 * @author  Justin Carlson
 * @author  Jochen Deyke
 * @author  jpont
 * @created December 22, 2002
 * @version 2010-08-30
 */
public class ViewMain
{
    //The Top JPanel to add everything into, and the window to place
    //that component into.
    private final JFrame topWindow = new JFrame("Java VCHILL") {
		/**
       * 
       */
      private static final long serialVersionUID = 7161598585920864891L;

    /**
		 * Processes a window event.
		 *
		 * @param event The window event that occurred.
		 */
		@Override protected void processWindowEvent (final WindowEvent event)
		{
			if( event.getID() == WindowEvent.WINDOW_CLOSING ) {
				// Always disconnect from servers before closing.
				ViewControl vc = ViewControl.getInstance();
				for( String url : vc.getConnections() ) {
					if( vc.isConnected(url) )
						vc.disconnect( url );
				}

				super.processWindowEvent( event );
			}
		}
	};

    //A Reference to the desktop pane, this
    //will be needed to add internal windows into the internal desktop.
    private final JDesktopPane desktop;

    public ViewMain ()
    {
        topWindow.setIconImage(GUIUtil.ICON);
        //get desktop size
        Config gcc = Config.getInstance();
        int topWindowWidth = gcc.getDefaultWidth();
        int topWindowHeight = gcc.getDefaultHeight();

        //Create the menubar.
        ViewMenu menu = new ViewMenu();
        topWindow.setJMenuBar(menu);

        int leftPanelWidth = 264;
        int desktopWidth = topWindowWidth - leftPanelWidth;
        int desktopHeight = topWindowHeight - menu.getHeight();
        
        //Setup the JDesktopPane that will be used to hold the display windows
        this.desktop = new JDesktopPane();
        this.desktop.putClientProperty("JDesktopPane.dragMode", "outline");
        //this.desktop.setPreferredSize(new Dimension(desktopWidth, desktopHeight));

        //The various panels that are on the side.
        //---------------------------------------------------------------------------
        JComponent top = new ViewTabbedBrowserPane(SwingConstants.TOP);
        JComponent bottom = new ViewTabbedPane(SwingConstants.BOTTOM);
        bottom.setMinimumSize(bottom.getPreferredSize());
        bottom.setMaximumSize(bottom.getMinimumSize());
        JSplitPane leftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        leftPanel.setOneTouchExpandable(true);
        leftPanel.setDividerLocation(desktopHeight - bottom.getPreferredSize().height - leftPanel.getInsets().top);
        leftPanel.setResizeWeight(1);
        leftPanel.setBorder(BorderFactory.createEmptyBorder());
        leftPanel.setMinimumSize(new Dimension(leftPanelWidth, topWindowHeight));

        //The output panel.
        //-----------------------------------------------------------------------------
        ViewEventWindow win = new ViewEventWindow(new Dimension(desktopWidth, 1));
        win.setPreferredSize(win.getMinimumSize());
        System.setOut(new EventStream(System.out, win, false));
        System.setErr(new EventStream(System.err, win, true));

		//The desktop (where the windows go) and the output panel go on the right side.
		//-----------------------------------------------------------------------------
        JSplitPane rightPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, desktop, win);
        rightPanel.setOneTouchExpandable(true);
        rightPanel.setResizeWeight(1);
        rightPanel.setDividerLocation(desktopHeight - win.getMinimumSize().height);
        rightPanel.setBorder(BorderFactory.createEmptyBorder());

        JSplitPane containAll = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        containAll.setOneTouchExpandable(true);
        containAll.setDividerLocation(leftPanelWidth);
        containAll.setBorder(BorderFactory.createEmptyBorder());

        //Set the top window to use the fully created JPanel with all of the additions.
        topWindow.setContentPane(containAll);
        leftPanel.resetToPreferredSizes();
    }

    /**
     * @return Get the top level frame out of this class.
     */
    public JFrame getWindow ()
    {
        return this.topWindow;
    }

    public JDesktopPane getDesktop ()
    {
        return this.desktop;
    }
}
