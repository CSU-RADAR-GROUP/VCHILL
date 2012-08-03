package edu.colostate.vchill.gui;

/**
 * A <code>ViewTabbedPane</code> with different tabs: Connections and Bookmarks
 *
 * @author Jochen Deyke
 * @version 2007-08-15
 */
public class ViewTabbedBrowserPane extends ViewTabbedPane
{
    /**
   * 
   */
  private static final long serialVersionUID = 5047095007066418742L;

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
