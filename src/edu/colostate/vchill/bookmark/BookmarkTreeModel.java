package edu.colostate.vchill.bookmark;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * A model for the bookmark tree.  Extends DefaultTreeModel to have most of the
 * important interaction.  Also has a methods for adding elements in sorted
 * order instead of simply adding them to the end.  Overrides the isLeaf method
 * to fix the appearance of non-leaf nodes.
 *
 * @author  Alexander Deyke
 * @author  Jochen Deyke
 * @version 2005-09-19
 */
public class BookmarkTreeModel extends DefaultTreeModel
{
    /**
   * 
   */
  private static final long serialVersionUID = -319860178138853771L;
    private static final BookmarkControl bmc = BookmarkControl.getInstance();

    /**
     * Constructor.  Simply passes the argument to DefaultTreeModel.
     *
     * @param root root of the tree
     */
    public BookmarkTreeModel (final DefaultMutableTreeNode root) {
        super(root);
        /*
        this.addTreeModelListener(new TreeModelListener() {
            public void treeNodesChanged (final TreeModelEvent tme) {
                DefaultMutableTreeNode node;
                node = (DefaultMutableTreeNode)(tme.getTreePath().getLastPathComponent());

                // If the event lists children, then the changed
                // node is the child of the node we've already
                // gotten.  Otherwise, the changed node and the
                // specified node are the same.
                try {
                    int index = tme.getChildIndices()[0]; //index of first changed node
                    node = (DefaultMutableTreeNode)(node.getChildAt(index));
                } catch (NullPointerException npe) {}

                switch (node.getLevel()) {
                    case 2: //actual bookmark
                    case 1: //category
        
                System.out.println("The user has finished editing the node.");
                System.out.println("New value: " + node.getUserObject() + " (level " + node.getLevel() + ")");
            }

            public void treeNodesInserted (final TreeModelEvent tme) { }
            public void treeNodesRemoved (final TreeModelEvent tme) { }
            public void treeStructureChanged (final TreeModelEvent tme) { }
        });
        */
    }

    /**
     * Inserts a node in the correct sorted order.
     *
     * @param newNode node to add
     */
    public void insertNodeSorted (final DefaultMutableTreeNode newNode)
    {
        insertNodeSorted((DefaultMutableTreeNode)getRoot(), newNode);
    }

    /**
     * Inserts a node in the correct sorted order.
     *
     * @param parent parent node under which to add the node
     * @param newNode node to add
     */
    public void insertNodeSorted (final DefaultMutableTreeNode parent, final DefaultMutableTreeNode newNode)
    {
        if (parent.getChildCount() == 0) {
            insertNodeInto(newNode, parent, 0);
        } else if (newNode.toString().compareTo(((DefaultMutableTreeNode)parent.getChildAt(parent.getChildCount() - 1)).toString()) > 0) {
            insertNodeInto(newNode, parent, parent.getChildCount());
        } else {
            for (int i = 0; i < parent.getChildCount(); i++) {
                if ((newNode.toString()).compareTo(((DefaultMutableTreeNode)parent.getChildAt(i)).toString()) <= 0) {
                    insertNodeInto(newNode, parent, i);
                    break;
                }
            }
        }
    }

    /**
     * Inserts a node to a category in the correct sorted order.
     *
     * @param category name of category
     * @param newNode node to add
     */
    public void insertNodeToCategory (final String category, final DefaultMutableTreeNode newNode)
    {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            if (((DefaultMutableTreeNode)root.getChildAt(i)).toString().equals(category)) {
                insertNodeSorted((DefaultMutableTreeNode)root.getChildAt(i), newNode);
                break;
            }
        }
    }

    /**
     * Determines if a node should be rendered as a leaf.  This is based solely
     * on the length of the path.  This means that even if the root node or the
     * category nodes have no children, they're still rendered as "folders".
     *
     * @param node node to check
     */
    public boolean isLeaf (final Object node)
    {
        return ((DefaultMutableTreeNode)node).getLevel() == 2;
    }

}
