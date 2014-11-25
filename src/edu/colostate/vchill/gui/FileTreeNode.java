package edu.colostate.vchill.gui;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * A DefaultMutableTreeNode subclass for use by ViewFileBrowser.  In addition
 * to the usual DefaultMutableTreeNode variables and methods, it has a boolean
 * for marking if the node has already been successfully expanded or not.  If
 * it has, it won't be regenerated when expanded again.  There's also a boolean
 * for marking if the node should be displayed in bold text or not.  This is
 * used to indicate whether or not a file node has calibration data.
 *
 * @author Alexander Deyke
 * @author Jochen Deyke
 * @version 2007-03-15
 */
public class FileTreeNode extends DefaultMutableTreeNode {
    /**
     *
     */
    private static final long serialVersionUID = 1931924465148378724L;

    public static final String LOADING = "Loading...";

    /**
     * Has the node been completely expanded?
     */
    public boolean complete;

    /**
     * Should the node be rendered differently?
     */
    public boolean special;

    /**
     * Constructor.  Gets a string, assumes the node has not yet been
     * completely expanded and is not special.
     *
     * @param userObject name of node
     */
    public FileTreeNode(final String userObject) {
        super(userObject);
        this.complete = false;
        this.special = false;
    }

    /**
     * Constructor.  Gets a string, assumes the node has not yet been
     * completely expanded.
     *
     * @param userObject name of node
     * @param special    if the node should be made bold or not
     */
    public FileTreeNode(final String userObject, final boolean special) {
        super(userObject);
        this.complete = false;
        this.special = special;
    }

    public String toString() {
        String userObject = (String) this.getUserObject();
        //int lastSpace = userObject.lastIndexOf(" ");
        //if (lastSpace > -1) userObject = userObject.substring(0, lastSpace); //trim DIR, PPI, RHI, etc
        int lastSlash = -1;
        if (userObject.endsWith("/ DIR")) {
            lastSlash = userObject.lastIndexOf("/", userObject.length() - 6); //ignore trailing /
        } else if (userObject.endsWith(" DIR")) {
            lastSlash = userObject.lastIndexOf("/");
        }
        if (lastSlash > -1) userObject = userObject.substring(lastSlash + 1); //don't include lastSlash
        try {
            userObject = URLDecoder.decode(userObject, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
        }
        return userObject;
    }
}
