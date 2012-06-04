package edu.colostate.vchill.gui;

/**
 * A <code>ViewTabbedPane</code> with different tabs: Connections and Bookmarks
 *
 * @author Jochen Deyke
 * @version 2007-08-15
 */
public class ViewTabbedBrowserPane extends ViewTabbedPane
{
    public ViewTabbedBrowserPane (final int tabPlacement)
    {
        super(tabPlacement);
    }

    protected void addTabs ()
    {
        addTab("Connections", null, ViewFileBrowser.getInstance(), "Archive, realtime, and local connections");
        addTab("Bookmarks", null, ViewBookmarkBrowser.getInstance(), "Shortcuts to specific data");
    }
}
