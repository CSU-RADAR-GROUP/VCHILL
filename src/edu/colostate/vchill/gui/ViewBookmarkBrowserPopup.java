package edu.colostate.vchill.gui;

import edu.colostate.vchill.DialogUtil;
import edu.colostate.vchill.ViewControl;
import edu.colostate.vchill.bookmark.BookmarkControl;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Right-click menu for bookmark browser
 *
 * @author Jochen Deyke
 * @version 2007-09-26
 */
class ViewBookmarkBrowserPopup extends JPopupMenu
{
    protected static final edu.colostate.vchill.Config vcc = edu.colostate.vchill.Config.getInstance();
    protected static final ViewControl vc = ViewControl.getInstance();
    private static final BookmarkControl bmc = BookmarkControl.getInstance();

    private final JTree tree;
    private final int[] coords = new int[2]; //x, y
    
    private final File[] lastDir = new File[1];
    
    public ViewBookmarkBrowserPopup (final JTree tree)
    {
        super("Bookmarks");
        this.tree = tree;
        
        JMenuItem file = new JMenuItem(new AbstractAction("Import from File") {
            public void actionPerformed (final ActionEvent ae) {
                JFileChooser chooser = new JFileChooser(lastDir[0]);
                chooser.setFileFilter(new XMLFileFilter());
                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
		    lastDir[0] = chooser.getSelectedFile();
                    bmc.load(lastDir[0]);
                }
            }});
        file.setMnemonic(KeyEvent.VK_F);
        file.setDisplayedMnemonicIndex(12);
        this.add(file);
        
        JMenuItem url = new JMenuItem(new AbstractAction("Import from URL") {
            public void actionPerformed (final ActionEvent ae) {
                try {
                    String url = DialogUtil.showInputDialog("URL of boomark file to import:");
                    if (url != null && url.length() > 0) {
                        bmc.load(new URL(url).openStream(), BookmarkControl.USER_PREFIX);
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    DialogUtil.showErrorDialog(ioe.toString());
                }
            }});
        url.setMnemonic(KeyEvent.VK_U);
        this.add(url);
        
        this.addSeparator(); //------------------------------

        JMenuItem export = new JMenuItem(new AbstractAction("Export to File") {
            public void actionPerformed (final ActionEvent ae) {
                JFileChooser chooser = new JFileChooser(lastDir[0]);
                chooser.setFileFilter(new XMLFileFilter());
                int returnVal = chooser.showSaveDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
		    lastDir[0] = file;
                    if (!file.getName().toLowerCase().endsWith(".xml")) {
                        file = new File(file.getAbsolutePath() + ".xml");
                    }
                    if (tree.isPathSelected(new TreePath(tree.getModel().getRoot()))) bmc.save(file);
                    else bmc.export(file, tree.getSelectionPaths());
                }
            }});
        export.setMnemonic(KeyEvent.VK_E);
        this.add(export);

        this.addSeparator(); //------------------------------

        JMenuItem remove = new JMenuItem(new AbstractAction("Delete") {
            public void actionPerformed (final ActionEvent ae) {
                TreePath[] sel = tree.getSelectionPaths();
                tree.clearSelection();
                bmc.remove(sel);
            }});
        remove.setMnemonic(KeyEvent.VK_D);
        this.add(remove);

        JMenuItem rename = new JMenuItem(new AbstractAction("Rename") {
            public void actionPerformed (final ActionEvent ae) {
                bmc.rename(tree.getPathForLocation(coords[0], coords[1]));
            }});
        rename.setMnemonic(KeyEvent.VK_N);
        this.add(rename);

        JMenuItem move = new JMenuItem(new AbstractAction("Move") {
            public void actionPerformed (final ActionEvent ae) {
                TreePath[] sel = tree.getSelectionPaths();
                tree.clearSelection();
                bmc.move(sel);
            }});
        move.setMnemonic(KeyEvent.VK_M);
        this.add(move);

        JMenuItem duplicate = new JMenuItem(new AbstractAction("Duplicate") {
            public void actionPerformed (final ActionEvent ae) {
                TreePath[] sel = tree.getSelectionPaths();
                tree.clearSelection();
                bmc.duplicate(sel);
            }});
        duplicate.setMnemonic(KeyEvent.VK_C);
        this.add(duplicate);

        this.addSeparator(); //------------------------------
        
        JMenuItem bookmark = new JMenuItem(new AbstractAction("Create Bookmark") {
            public void actionPerformed (final ActionEvent ae) {
                vc.createBookmark();
            }});
        bookmark.setMnemonic(KeyEvent.VK_B);
        this.add(bookmark);

        this.addSeparator(); //------------------------------

        JMenuItem comment = new JMenuItem(new AbstractAction("View Comment") {
            public void actionPerformed (final ActionEvent ae) {
                TreePath path = tree.getPathForLocation(coords[0], coords[1]);
                if (path == null) return;
                Object[] objs = path.getPath();
                if (objs.length < 3) return;
                DialogUtil.showHelpDialog("Bookmark comment", bmc.getBookmark(objs[1].toString(), objs[2].toString()).comment);
            }});
        comment.setMnemonic(KeyEvent.VK_V);
        this.add(comment);
        
        JMenuItem properties = new JMenuItem(new AbstractAction("Properties") {
            public void actionPerformed (final ActionEvent ae) {
                TreePath path = tree.getPathForLocation(coords[0], coords[1]);
                if (path == null) return;
                Object[] objs = path.getPath();
                if (objs.length < 3) return;
                new BookmarkDialog(bmc.getBookmark(objs[1].toString(), objs[2].toString())).show();
            }});
        properties.setMnemonic(KeyEvent.VK_P);
        this.add(properties);
    }

    public void show (final Component invoker, final int x, final int y)
    {
        super.show(invoker, coords[0] = x, coords[1] = y);
    }

    protected static class XMLFileFilter extends FileFilter
    {
        public boolean accept (final File file)
        {
            if (file.isDirectory()) return true;

            String filename = file.getName().toLowerCase();
            return filename.endsWith(".xml");
        }

        public String getDescription ()
        {
            return "XML Bookmark Files";
        }
    }
}
