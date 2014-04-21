package edu.colostate.vchill.gui;

import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.ViewControl;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Actions for ViewFileBrowser.
 *
 * @author Alexander Deyke
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-06-30
 */
public final class ViewFileBrowserActions
{
    private JTree tree;

    /**
     * Package protected constructor prevents instantiation
     * @param tree the ViewFileBrowser's tree
     */
    ViewFileBrowserActions (final JTree tree)
    {
        this.tree = tree;
    }

	/**
	 * Tries to find the sweep specified by the
	 * provided control message.
	 *
	 * @param msg The message containing information
	 *            for finding the sweep.
	 *
	 * @return A potentially modified control message
	 *         indicating where the sweep is or null
	 *         if the sweep couldn't be found.
	 */
	public ControlMessage findSweep (ControlMessage msg)
	{
        System.out.println("FNAME:"+msg.getFile());
		String year = msg.getFile().substring( 3, 7 );
		String month = msg.getFile().substring( 7, 9 );
		String day = msg.getFile().substring( 9, 11 );
        //I'll fix this later, it's so ugly it makes me want to cry.}
		ViewControl vc = ViewControl.getInstance();
		for( String url : vc.getConnections() ) {
            System.out.println("URL:"+url);
			//determine the hidden part of the directories
			FileTreeNode urlNode = this.getNode( url );
			FileTreeNode dirNode = (FileTreeNode) urlNode.getFirstChild();
			String fullPath = (String) dirNode.getUserObject();
			String displayedPath = dirNode.toString();
			int hiddenPathEnd = fullPath.indexOf( displayedPath );
			if( hiddenPathEnd == -1 ) continue;
			String hiddenPath = fullPath.substring( 0, hiddenPathEnd );
            if(msg.getFile().charAt(2) == 'X'){
                hiddenPath="/dsk/nas/archives/x_band/";
            }

			if( this.getNode(url, hiddenPath + year) == null )
				continue;
			if( this.getNode(url, hiddenPath + year + "/" + month) == null )
				continue;
			if( this.getNode(url, hiddenPath + year + "/" + month + "/" + day) == null )
				continue;
			if( this.getNode(url, hiddenPath + year + "/" + month + "/" + day, msg.getFile()) == null )
				continue;
			if( this.getNode(url, hiddenPath + year + "/" + month + "/" + day, msg.getFile(), msg.getSweep()) == null )
				continue;

			//we found the sweep
			msg = msg.setURL( url );
			msg = msg.setDir( hiddenPath + year + "/" + month + "/" + day );
			return msg;
		}

		return null;
	}

    /**
     * Changes the active selection in the tree to the path identified by the
     * given control message.  Each level of the tree is expanded as needed,
     * allowing the expansion event handler to create the nodes at each level
     * as needed.  Assuming the desired node exists, it is then selected.
     * Since this triggers a selection event, the sweep will be also be plotted.
     *
     * @param newSelection The desired selection.
     */
    public void changeSelection (final ControlMessage newSelection)
    {
        String pathToFind = newSelection.getDir().split(" ")[0];
        String fileToFind = newSelection.getFile().split(" ")[0];

        TreePath newPath = this.getTreePath(newSelection.getURL(), pathToFind, fileToFind, newSelection.getSweep());
		if( newPath == null ) return;

        this.tree.scrollPathToVisible(newPath.getParentPath());
        this.tree.scrollPathToVisible(newPath);
        this.tree.clearSelection();
        this.tree.setSelectionPath(newPath);
    }

    /**
     * Finds and returns a node in the tree based on Strings.  To select
     * a file, leave the sweep null.  To select a directory, leave
     * the file null.  To select a connection, leave the directory
     * null.  To select the root node, leave the connection null.
     * Returns null if there's no match.
     *
     * @param url The connection URL.
     * @param dir The directory.
     * @param file The file.
     * @param sweep The sweep.
     * @return the requested node
     */
    public FileTreeNode getNode (final String url, final String dir, final String file, final String sweep)
    {
		TreePath path = this.getTreePath(url, dir, file, sweep);
		if( path == null ) return null;
		else return (FileTreeNode) path.getLastPathComponent();
    }

    public TreePath getTreePath (final String url, final String dir, final String file, final String sweep)
    {
//System.out.println("looking for node");
        FileTreeNode root = (FileTreeNode)tree.getModel().getRoot();
        TreePath path = new TreePath(root);
        this.tree.expandPath(path);

//System.out.println("looking for " + url);
        if (url == null) return null;
        FileTreeNode connection = searchNodeForString(root, url);
        if (connection == null) return null;
        path = path.pathByAddingChild(connection);
        this.tree.expandPath(path);
//System.out.println("found connection " + connection);

//System.out.println("looking for " + dir + " in " + connection);
        if (dir == null) return path;
        FileTreeNode directory = searchNodeForString(connection, dir);
        if (directory == null) return null;
        path = path.pathByAddingChild(directory);
        this.tree.expandPath(path);
//System.out.println("found dir " + directory);

        while (true) {
            directory = searchNodeForString(directory, dir);
            if (directory == null) break;
            path = path.pathByAddingChild(directory);
            this.tree.expandPath(path);
//System.out.println("found dir " + directory);
        }
        directory = (FileTreeNode)path.getLastPathComponent();

//System.out.println("looking for " + file + " in " + directory);
        if (file == null) return path;
        FileTreeNode filenode = searchNodeForString(directory, file);
        if (filenode == null) return null;
        path = path.pathByAddingChild(filenode);
        this.tree.expandPath(path);
//System.out.println("found file " + filenode);

//System.out.println("looking for " + sweep + " in " + filenode);
        if (sweep == null) return path;
        FileTreeNode sweepnode = searchNodeForString(filenode, sweep);
        if (sweepnode == null) return null;
        path = path.pathByAddingChild(sweepnode);
        this.tree.expandPath(path);
//System.out.println("found sweep " + sweepnode);

        return path;
    }

    /**
     * Searches a node's children's userObjects for one matching the beginning of the specified String
     * @param node the node to search
     * @param toFind the String to search for
     * @return the matching node; null if not found
     */
    private static FileTreeNode searchNodeForString (final FileTreeNode node, final String toFind)
    {
        Enumeration children = node.children();
        while (children.hasMoreElements()) {
            FileTreeNode child = (FileTreeNode)(children.nextElement());
            String nodeName = ((String)child.getUserObject());
            if (!nodeName.startsWith("Sweep")) nodeName = nodeName.split(" ")[0];
//System.out.println("nodeName = " + nodeName);
            if (toFind.startsWith(nodeName)) return child;
            if (nodeName.equals(toFind + "/")) return child;
        }
        return null;
    }

    /**
     * Finds and returns a file node in the tree based on Strings.
     *
     * @param url The connection URL.
     * @param dir The directory.
     * @param file The file.
     * @return the requested node
     */
    public FileTreeNode getNode (final String url, final String dir, final String file)
    {
        return this.getNode(url, dir, file, null);
    }

    /**
     * Finds and returns a directory node in the tree based on Strings.
     *
     * @param url The connection URL.
     * @param dir The directory.
     * @return the requested node
     */
    public FileTreeNode getNode (final String url, final String dir)
    {
        return this.getNode(url, dir, null, null);
    }

    /**
     * Finds and returns a connection node in the tree based on Strings.
     *
     * @param url The connection URL.
     * @return the requested node
     */
    public FileTreeNode getNode (final String url)
    {
        return this.getNode(url, null, null, null);
    }

    /**
     * Finds and returns the root node.
     *
     * @return the requested node
     */
    public FileTreeNode getNode ()
    {
        return this.getNode(null, null, null, null);
    }

    /**
     * Finds and returns a node based on a ControllMessage
     * 
     * @param message ControlMessage corresponding to that node.
     * @return the requested node
     */
    public FileTreeNode getNode (final ControlMessage message)
    {
        return this.getNode(message.getURL(), message.getDir(), message.getFile(), message.getSweep());
    }

    /**
     * Selects the first sweep in the current file.  If this is not possible
     * (e.g. no previous selection), selects the first sweep in the tree.  This
     * method will not move the selection out of the current file.
     */
    public void selectFirst ()
    {
        TreePath newSelection = null;
        try {
            Object[] nodes = this.tree.getSelectionPath().getPath();
            TreeNode[] newNodes = new TreeNode[nodes.length];
            for (int i = 0; i < nodes.length; ++i) newNodes[i] = (TreeNode)nodes[i];
            newNodes[nodes.length - 1] = ((FileTreeNode)nodes[nodes.length - 2]).getFirstLeaf();
            newSelection = new TreePath(newNodes);
        } catch (Exception e) { //no previous selection - select first sweep in tree
            ArrayList<FileTreeNode> path = new ArrayList<FileTreeNode>(5);
            path.add(((FileTreeNode)this.tree.getModel().getRoot()));
            tree.expandPath(new TreePath(path.toArray()));
            FileTreeNode connection = (FileTreeNode)(((FileTreeNode)tree.getModel().getRoot()).getFirstChild());
            path.add(connection);
            tree.expandPath(new TreePath(path.toArray()));
            FileTreeNode dir = (FileTreeNode)(connection.getFirstChild());
            path.add(dir);
            tree.expandPath(new TreePath(path.toArray()));
            FileTreeNode file = (FileTreeNode)(dir.getFirstChild());
            path.add(file);
            tree.expandPath(new TreePath(path.toArray()));
            FileTreeNode sweep = (FileTreeNode)(file.getFirstChild());
            path.add(sweep);
            newSelection = new TreePath(path.toArray());
        }
        this.tree.scrollPathToVisible(newSelection);
        this.tree.setSelectionPath(newSelection);
    }

    /**
     * Selects the previous sweep in the current file.  If this is not possible
     * (e.g. no previous selection), selects the first sweep in the tree.  This
     * method will not move the selection out of the current file.
     */
    public void selectPrev ()
    {
        try {
            TreePath newSelection = null;
            Object[] nodes = tree.getSelectionPath().getPath();
            int last = nodes.length - 1;
            TreeNode[] newNodes = new TreeNode[nodes.length];
            for (int i = 0; i < nodes.length; ++i) newNodes[i] = (TreeNode)nodes[i];
            newNodes[last] = ((FileTreeNode)nodes[last]).getPreviousSibling();
            if (newNodes[last] == null) return;
            if (((FileTreeNode)newNodes[last]).getUserObject().equals(FileTreeNode.LOADING)) return;
            newSelection = new TreePath(newNodes);
            this.tree.scrollPathToVisible(newSelection);
            this.tree.setSelectionPath(newSelection);
        } catch (Exception e) { //no previous selection
            selectFirst();
        }
    }

    /**
     * Selects the next sweep in the current file.  If this is not possible
     * (e.g. no previous selection), selects the first sweep in the tree.  This
     * method will not move the selection out of the current file.
     */
    public void selectNext ()
    {
        try {
            Object[] nodes = this.tree.getSelectionPath().getPath();
            int last = nodes.length - 1;
            TreeNode[] newNodes = new TreeNode[nodes.length];
            for (int i = 0; i < nodes.length; ++i) newNodes[i] = (TreeNode)nodes[i];
            newNodes[last] = ((FileTreeNode)nodes[last]).getNextSibling();
            if (newNodes[last] == null) return;
            if (((FileTreeNode)newNodes[last]).getUserObject().equals(FileTreeNode.LOADING)) {
                System.out.println("Hit loading in selectNext");
                return;
            }
            TreePath newSelection = new TreePath(newNodes);
            this.tree.scrollPathToVisible(newSelection);
            this.tree.setSelectionPath(newSelection);
        } catch (Exception e) { //no previous selection
            selectFirst();
        }
    }

    /**
     * Selects the next sweep in the current file, or, if the last sweep is
     * already selected, the first sweep of the next file.  If this is not
     * possible (e.g. no previous selection), selects the first sweep in the
     * tree.  This method will not move the selection out of the current file.
     */
    public void selectNextAcrossBounds ()
    {
        try {
            Object[] nodes = this.tree.getSelectionPath().getPath();
            int oldRow = tree.getLeadSelectionRow();
            int last = nodes.length - 1;
            TreeNode[] newNodes = new TreeNode[nodes.length];
            for (int i = 0; i < last; ++i) newNodes[i] = (TreeNode)nodes[i];
            newNodes[last] = ((FileTreeNode)nodes[last]).getNextSibling();
            if (newNodes[last] == null) {
                tree.expandRow(oldRow + 1);
                tree.setSelectionRow(oldRow + 2);
                return;
            }
            TreePath newSelection = new TreePath(newNodes);
            this.tree.scrollPathToVisible(newSelection);
            this.tree.setSelectionPath(newSelection);
        } catch (Exception e) { //no previous selection
            e.printStackTrace();
            selectFirst();
        }
    }

    /**
     * Selects the last sweep in the current file.  If this is not possible
     * (e.g. no previous selection), selects the first sweep in the tree.  This
     * method will not move the selection out of the current file.
     */
    public void selectLast ()
    {
        TreePath newSelection = null;
        try {
            Object[] nodes = this.tree.getSelectionPath().getPath();
            TreeNode[] newNodes = new TreeNode[nodes.length];
            for (int i = 0; i < nodes.length; ++i) newNodes[i] = (TreeNode)nodes[i];
            newNodes[nodes.length - 1] = ((FileTreeNode)nodes[nodes.length - 2]).getLastLeaf();
            newSelection = new TreePath(newNodes);
            this.tree.scrollPathToVisible(newSelection);
            this.tree.setSelectionPath(newSelection);
        } catch (Exception e) { //no previous selection
            selectFirst();
        }
    }

    /**
     * Completely rebuilds tree.
     * Called on connect and on disconnect.
     */
    public void refreshConnections ()
    {
        if (tree != null) {
            FileTreeNode root = (FileTreeNode)this.tree.getModel().getRoot();
            root.complete = false;
            this.tree.collapseRow(0);
            this.tree.expandRow(0);

			/* Automatically expand connections. */
			int numConnections = root.getChildCount();
			for( int i = 0; i < numConnections; i++ ) {
				FileTreeNode connection = (FileTreeNode) root.getChildAt(i);
				if( connection.toString().equals("Local Filesystem") )
					continue;
				TreePath path = new TreePath( connection.getPath() );
				this.tree.expandPath( path );
			}
        }
    }

    public TreePath getPathForLocation (final int x, final int y)
    {
        return this.tree.getPathForLocation(x, y);
    }

    public static ControlMessage getControlMessage (final TreePath path)
    {
        if (path == null) return null;
        Object[] pathObjects = path.getPath();
        if (pathObjects.length < 4) return null;
        return new ControlMessage(
            (String)(((FileTreeNode)pathObjects[1]).getUserObject()), //host:port
            pathObjects.length > 4 ? (String)(((FileTreeNode)pathObjects[pathObjects.length - 3]).getUserObject()) : "", //dir
            (String)(((FileTreeNode)pathObjects[pathObjects.length - 2]).getUserObject()), //file
            (String)(((FileTreeNode)pathObjects[pathObjects.length - 1]).getUserObject())); //sweep
    }
}
