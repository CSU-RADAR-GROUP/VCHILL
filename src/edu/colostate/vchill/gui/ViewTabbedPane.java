package edu.colostate.vchill.gui;

import javax.swing.JTabbedPane;

/**
 * Contains the default set of tabs: Basic (ViewRemotePanel),
 * Filters (ViewFilterPanel), and Advanced (ImageControlPanel). It's a basic
 * <code>JTabbedPane</code> with an automatically called addTabs method to more
 * easily allow the creation of tabbed panes. Child classes override the
 * addTabs() method to create their own set of tabs.
 *
 * @author  Justin Carlson
 * @author  Jochen Deyke
 * @author  jpont
 * @created January 20, 2003
 * @version 2010-08-30
 */
public class ViewTabbedPane extends JTabbedPane
{
    protected JTabbedPane viewPane;

    /**
     * Constructs a ViewTabbedPane.
     * Automatically adds tabs using the protected addTabs() method;
     * child classes should override this method to add their own tabs instead.
     * @param tabPlacement passed to JTabbedPane constructor
     */
    public ViewTabbedPane (int tabPlacement)
    {
        super(tabPlacement);
        this.addTabs();
    }

    protected void addTabs ()
    {
        //Buttons that control, next, prev, stop etc.
        this.addTab("Basic", null, ViewRemotePanel.getInstance(), "Browsing tools");
        this.addTab("Filters", null, ViewFilterPanel.getInstance(), "Filters for plot windows");

        this.addTab("Paint", null, ViewPaintPanel.getInstance(), "Grease pencil annotation");
        
        
        this.addTab("Advanced", null, new ImageControlPanel(), "Automatic image saving and scan/date/etc filter setup");
    }
}
