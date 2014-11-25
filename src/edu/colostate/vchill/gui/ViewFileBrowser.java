package edu.colostate.vchill.gui;

import edu.colostate.vchill.Loader;
import edu.colostate.vchill.ViewControl;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;

/**
 * This is a "file browser" that can be used to visualize directory structure
 * data / files
 * <p/>
 * NOTE:  This currently uses a Control Message for ease of use.  It may need
 * to be altered in order to be more dynamic about level, the nodes may need
 * to know about what type of data they are holding for multi directory access.
 *
 * @author Justin Carlson
 * @author Jochen Deyke
 * @author Alexander Deyke
 * @author jpont
 * @version 2010-08-30
 * @created June 2, 2003
 */
public class ViewFileBrowser extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = -2063995646756926200L;

    public static final ViewControl vc = ViewControl.getInstance();

    private final JTree tree;
    private final DefaultTreeModel model;
    private final ViewFileBrowserActions actions;
    private final ViewFileBrowserPopup popup;
    private static final ViewFileBrowser vfb = new ViewFileBrowser();

    public static ViewFileBrowser getInstance() {
        return vfb;
    }

    /**
     * Private constructor prevents instantiation
     */
    private ViewFileBrowser() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.model = new DefaultTreeModel(createNode("Connections"));
        createChildNodes((FileTreeNode) model.getRoot(), vc.getConnections(), true);
        this.tree = new JTree(this.model);
        this.actions = new ViewFileBrowserActions(this.tree);
        this.popup = new ViewFileBrowserPopup(this.actions);

        this.tree.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                TreePath click = tree.getPathForLocation(e.getX(), e.getY());
                switch (e.getButton()) {
                    case MouseEvent.BUTTON1: //left click
                        if (click == null) return;
                        FileTreeNode node = (FileTreeNode) click.getLastPathComponent();
                        if (node == null || !node.isLeaf()) return;
                        if (tree.isPathSelected(click) && e.getClickCount() < 2) return;
                        tree.setSelectionPath(click);
                        vc.setMessage(ViewFileBrowserActions.getControlMessage(click));
                        break;
                    case MouseEvent.BUTTON3: //right click
                        if (!tree.isPathSelected(click)) tree.setSelectionPath(click);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                        break;
                }
            }
        });

        this.tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent tse) {
                FileTreeNode node = (FileTreeNode) tree.getLastSelectedPathComponent();
                if (node == null || !node.isLeaf()) return;
                vc.setMessage(ViewFileBrowserActions.getControlMessage(tse.getPath()));
            }
        });

        this.tree.addTreeExpansionListener(new TreeExpansionListener() {
            public void treeExpanded(final TreeExpansionEvent tee) {
                TreePath path = tee.getPath();
                FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
                Object pathObjects[] = path.getPath();
                int pathLength = pathObjects.length;
                switch (pathLength) {
                    case 1:
                        createChildNodes(node, vc.getConnections(), true);
                        break;
                    case 2:
                        vc.setCurrentURL((String) (((FileTreeNode) pathObjects[1]).getUserObject()));
                        vc.setCurrentDirectory("");
                        createChildNodes(node, vc.getDirectory(), true);
                        break;
                    default:
                        vc.setCurrentURL((String) (((FileTreeNode) pathObjects[1]).getUserObject()));
                        String name = (String) (((FileTreeNode) pathObjects[pathLength - 1]).getUserObject());
                        if (name.endsWith(" DIR")) { //subdirectory - get contents
                            vc.setCurrentDirectory(name);
                            createChildNodes(node, vc.getDirectory(), true);
                        } else { //file - get sweeps
                            String dir = (String) (((FileTreeNode) pathObjects[pathLength - 2]).getUserObject());
                            vc.setCurrentDirectory(dir);
                            vc.setCurrentFile(name);
                            createChildNodes(node, vc.getSweeps(), false);
                        }
                }
            }

            public void treeCollapsed(final TreeExpansionEvent tee) {
            }
        });

        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        //this.tree.setShowsRootHandles(true);
        this.tree.setShowsRootHandles(false);

        this.tree.setCellRenderer(new DefaultTreeCellRenderer() {
            /**
             *
             */
            private static final long serialVersionUID = 4265159595747277418L;

            @Override
            public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                FileTreeNode node = (FileTreeNode) value;
                Font current = getFont();
                setFont(new Font(current.getFamily(), node.special ? Font.BOLD : Font.PLAIN, current.getSize()));
                switch (node.getLevel()) {
                    case 0:
                        setIcon(new ImageIcon(Loader.getResource("icons/top" + (expanded ? "Open" : "Closed") + ".gif")));
                        break;
                    case 1:
                        setIcon(new ImageIcon(Loader.getResource("icons/conn" + (expanded ? "Open" : "Closed") + ".gif")));
                        break;
                    default:
                        if (node.isLeaf()) {
                            try {
                                String[] parts = ((String) ((FileTreeNode) node.getParent()).getUserObject()).split(" ");
                                setIcon(new ImageIcon(Loader.getResource("icons/sweep" + parts[parts.length - 1] + ".png")));
                            } catch (Exception e) {
                                setIcon(new ImageIcon(Loader.getResource("icons/error.png")));
                            }
                        } else {
                            String name = (String) node.getUserObject();
                            if (name.endsWith(" DIR"))
                                setIcon(new ImageIcon(Loader.getResource("icons/dir" + (expanded ? "Open" : "Closed") + ".gif")));
                            else
                                setIcon(new ImageIcon(Loader.getResource("icons/file" + (expanded ? "Open" : "Closed") + ".gif")));
                        }
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(this.tree);
        add(scrollPane);
    }

    /**
     * @return a placeholder node
     */
    private FileTreeNode loadingNode() {
        return createNode(FileTreeNode.LOADING);
    }

    /**
     * This method will create new child nodes for the specified parent.
     * Depending on the fill_with_loading variable these will be leaves or further nodes in the structure.
     *
     * @param parentNode      The node that the information will be added to.
     * @param nodeNames       A list of names for the new child nodes of <code>parentNode</code>.
     * @param fillWithLoading If true, this will cause the new children to have a
     *                        default String created in them to allow them to be changed to a valid leaf later.
     *                        When the tree is to end branching, this variable should be set to false in order
     *                        to create the end leaves.
     */
    private void createChildNodes(final FileTreeNode parentNode, final Collection<String> nodeNames, final boolean fillWithLoading) {
        int numchildren = model.getChildCount(parentNode);
        if (!parentNode.complete) {
            if (nodeNames == null) {
                model.insertNodeInto(createNode("ERROR%20-%20Connection%20failed?"), parentNode, parentNode.getChildCount());
            } else if (nodeNames.size() == 0) {
                model.insertNodeInto(createNode("Nothing%20to%20display"), parentNode, parentNode.getChildCount());
            } else {
                for (String name : nodeNames) {
                    FileTreeNode child = createNode(name);
                    if (fillWithLoading) model.insertNodeInto(loadingNode(), child, 0);
                    model.insertNodeInto(child, parentNode, parentNode.getChildCount());
                }
                parentNode.complete = true;
            }
            for (int i = 0; i < numchildren; ++i) {
                model.removeNodeFromParent((FileTreeNode) model.getChild(parentNode, 0));
            }
            model.nodeStructureChanged(parentNode);
        }
    }

    /**
     * This will simply create a new node, mostly here for the sake of cutting down on space use.
     *
     * @param nodeName name of the node to create
     * @return the new node
     */
    private FileTreeNode createNode(final String nodeName) {
        return new FileTreeNode(nodeName);
    }

    public ViewFileBrowserActions getActions() {
        return this.actions;
    }
}
