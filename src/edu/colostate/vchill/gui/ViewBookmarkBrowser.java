package edu.colostate.vchill.gui;

import edu.colostate.vchill.Config;
import edu.colostate.vchill.*;
import edu.colostate.vchill.bookmark.Bookmark;
import edu.colostate.vchill.bookmark.BookmarkControl;
import edu.colostate.vchill.chill.ChillMomentFieldScale;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

/**
 * This is a "bookmark browser" that can be used to visually represent bookmarks
 * by categories and names.  It creates a JTree, adds the selection handling,
 * sets some options and renders it.  The BookmarkTreeModel is responsible for
 * most of the internal workings.
 *
 * @author Justin Carlson
 * @author Jochen Deyke
 * @author Alexander Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class ViewBookmarkBrowser extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 2609212773365339587L;
    private static final WindowManager wm = WindowManager.getInstance();
    private static final ViewControl vc = ViewControl.getInstance();
    private static final Config config = Config.getInstance();
    private static final ScaleManager sm = ScaleManager.getInstance();

    private final JTree tree;
    private final ViewBookmarkBrowserPopup popup;

    private static final ViewBookmarkBrowser vbb = new ViewBookmarkBrowser();

    private static enum Match {
        /**
         * match only the beginning of the string
         */
        BEGINNING,

        /**
         * match anywhere in the string
         */
        MIDDLE,

        /**
         * match only the end of the string
         */
        END,

        /**
         * match the entire string
         */
        ENTIRE,
    }

    /**
     * Constructor for the ViewBookmarkBrowser object
     */
    private ViewBookmarkBrowser() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.tree = new JTree(BookmarkControl.getInstance().getModel());
        this.popup = new ViewBookmarkBrowserPopup(this.tree);

        this.tree.addMouseListener(new MouseInputAdapter() {
            private Bookmark getBookmark(final TreePath click) {
                if (click == null) return null;
                Object[] pathObjects = click.getPath();
                if (!tree.isPathSelected(click)) tree.setSelectionPath(click);
                if (tree.getSelectionPaths() == null || tree.getSelectionPaths().length > 1) return null;
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) click.getLastPathComponent();
                if (node == null || !node.isLeaf()) return null;
                return BookmarkControl.getInstance().getBookmark(
                        pathObjects[1].toString(),
                        pathObjects[2].toString());
            }

            @Override
            public void mouseClicked(final MouseEvent e) {
                TreePath click = tree.getPathForLocation(e.getX(), e.getY());
                switch (e.getButton()) {
                    case MouseEvent.BUTTON1: //left click
                        setBookmark(getBookmark(click));
                        break;
                    case MouseEvent.BUTTON2: //middle click
                        Bookmark bookmark = getBookmark(click);
                        if (bookmark != null) DialogUtil.showHelpDialog("Bookmark comment", bookmark.comment);
                        break;
                    case MouseEvent.BUTTON3: //right click
                        if (!tree.isPathSelected(click)) tree.setSelectionPath(click);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                        break;
                }
            }
        });
        //this.tree.setEditable(true);
        this.tree.setEditable(false);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.tree.setShowsRootHandles(true);

        this.tree.setCellRenderer(new DefaultTreeCellRenderer() {
            /**
             *
             */
            private static final long serialVersionUID = -3738819540553277433L;

            @Override
            public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                switch (node.getLevel()) {
                    case 0:
                        setIcon(new ImageIcon(Loader.getResource("icons/top" + (expanded ? "Open" : "Closed") + ".gif")));
                        break;
                    case 1:
                        setIcon(new ImageIcon(Loader.getResource("icons/dir" + (expanded ? "Open" : "Closed") + ".gif")));
                        break;
                    case 2:
                        setIcon(new ImageIcon(Loader.getResource("icons/bookmark.png")));
                        break;
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(this.tree);
        add(scrollPane);
    }

    /**
     * Set view characteristics to those specified by the given bookmark
     *
     * @param bookmark The bookmark to load
     */
    public static void setBookmark(final Bookmark bookmark) {
        if (bookmark == null) return;
        System.out.println("Loading bookmark: " + bookmark.toString());
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                wm.setMode(bookmark.scan_type, false); //do not force
                config.setMaxPlotHeight(bookmark.rhi_height); //km
                config.setGridSpacing(bookmark.ring); //km
                config.setPlotRange(bookmark.range); //km
                for (String type : sm.getTypes()) { //each type
                    Bookmark.Scale bmScale = bookmark.scale.get(type);
                    if (bmScale == null) continue;
                    if (bmScale.autoscale) { //use default vs custom scale
                        ChillMomentFieldScale scale = sm.getScale(type);
                        vc.setPlotInterval(type,
                                String.valueOf(scale.maxValue),
                                String.valueOf(scale.minValue));
                    } else {
                        vc.setPlotInterval(type,
                                bmScale.maxval,
                                bmScale.minval);
                    }
                }
                ViewRemotePanel.getInstance().update();
                config.setCenterX(bookmark.x);
                config.setCenterY(bookmark.y);

                ViewFileBrowserActions actions = ViewFileBrowser.getInstance().getActions();
                ControlMessage message = bookmark.getControlMessage();
                message = actions.findSweep(message);
                if (message != null) {
                    //we found the bookmark sweep

                    System.out.println("FOUND BOOKMARK: bookmark control message: " + message);
                    actions.changeSelection(message); //this triggers plot
                } else {
                    System.out.println("BookMark Not Found Correctly");
                }

                wm.setCenterInKm();
                DialogUtil.showHelpDialog("Bookmark comment", bookmark.comment);
            }
        });
    }

    /**
     * Tries to select the specified bookmark.
     * If it exists, it will also be set as per setBookmark().
     *
     * @param category the (end of) the category to select
     * @param name     the (start of) the name of the bookmark to select
     */
    public void selectBookmark(final String category, final String name) {
        TreePath newPath = getTreePath(category, name);
        tree.scrollPathToVisible(newPath.getParentPath());
        tree.scrollPathToVisible(newPath);
        tree.clearSelection();
        tree.setSelectionPath(newPath);
        Object[] pathObjs = newPath.getPath();
        if (pathObjs.length != 3) return; //bookmarks, category, bookmark
        String foundCategory = (String) ((DefaultMutableTreeNode) pathObjs[1]).getUserObject();
        String foundBookmark = (String) ((DefaultMutableTreeNode) pathObjs[2]).getUserObject();
        Bookmark bookmark = BookmarkControl.getInstance().getBookmark(foundCategory, foundBookmark);
        if (bookmark != null) setBookmark(bookmark);
    }

    /**
     * @param category the (end of) the category to select
     * @param name     the (start of) the name of the bookmark to select
     * @return the path to the specified bookmark if it exists, a partial path if it does not.
     */
    public TreePath getTreePath(final String category, final String name) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        TreePath path = new TreePath(root);
        tree.expandPath(path);

        if (category == null) return path;
        DefaultMutableTreeNode categoryNode = searchNodeForString(root, category, Match.END);
        if (categoryNode == null) return path;
        path = path.pathByAddingChild(categoryNode);
        tree.expandPath(path);

        if (name == null) return path;
        DefaultMutableTreeNode bookmarkNode = searchNodeForString(categoryNode, name, Match.BEGINNING);
        if (bookmarkNode == null) return path;
        path = path.pathByAddingChild(bookmarkNode);
        tree.expandPath(path);

        return path;
    }

    /**
     * @param node   the node in whose children to search for the named node
     * @param toFind the name (userObject) of the child node to find
     * @param match  whether the beginning, end, or entire string must match
     * @return the child node with the specified name if it exists, null if it does not
     */
    private static DefaultMutableTreeNode searchNodeForString(final DefaultMutableTreeNode node, final String toFind, final Match match) {
        Enumeration children = node.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            String nodeName = (String) child.getUserObject();
            switch (match) {
                case BEGINNING:
                    if (nodeName.startsWith(toFind)) return child;
                    break;
                case MIDDLE:
                    if (nodeName.contains(toFind)) return child;
                    break;
                case END:
                    if (nodeName.endsWith(toFind)) return child;
                    break;
                case ENTIRE:
                    if (nodeName.equals(toFind)) return child;
                    break;
            }
        }
        return null;
    }

    public static ViewBookmarkBrowser getInstance() {
        return vbb;
    }
}
