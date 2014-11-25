package edu.colostate.vchill.bookmark;

import edu.colostate.vchill.DialogUtil;
import edu.colostate.vchill.ScaleManager;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Controller class for VCHILL's bookmark module.
 * This module is synchronized.
 *
 * @author Jochen Deyke
 * @author Alexander Deyke
 * @author jpont
 * @version 2009-06-30
 */
public class BookmarkControl {
    public static final String USER_PREFIX = "my:";

    public static final ScaleManager sm = ScaleManager.getInstance();

    private final Map<String, Map<String, Bookmark>> bookmarks;
    private final BookmarkTreeModel model;
    private static final BookmarkControl bmc = new BookmarkControl();

    public static BookmarkControl getInstance() {
        return bmc;
    }

    /**
     * Private default constructor prevents instantiation
     */
    private BookmarkControl() {
        this.bookmarks = new TreeMap<String, Map<String, Bookmark>>();
        this.model = new BookmarkTreeModel(new DefaultMutableTreeNode("Bookmarks"));
    }

    /**
     * Selects the category matching <code>cat</code>.
     * Always returns a valid category - if it did not previously exist, it is created.
     *
     * @param cat String naming the desired category
     * @return The selected category
     */
    private Map<String, Bookmark> selectCat(final String cat) {
        Map<String, Bookmark> tmp = this.bookmarks.get(cat);
        if (tmp == null) {
            tmp = new TreeMap<String, Bookmark>();
            this.bookmarks.put(cat, tmp);
            this.model.insertNodeSorted(new DefaultMutableTreeNode(cat));
        }
        return tmp;
    }

    /**
     * Add a new bookmark.  The tree is also updated.
     *
     * @param cat      The category to add the bookmark to
     * @param name     The name for the new bookmark
     * @param bookmark The new bookmark itself
     */
    public synchronized void addBookmark(final String cat, final String name, final Bookmark bookmark) {
        String fullname = name + " " + bookmark.scan_type;
        if (selectCat(cat).put(fullname, bookmark) == null) {
            this.model.insertNodeToCategory(cat, new DefaultMutableTreeNode(fullname));
        } //only add to tree if actually new
    }

    /**
     * Retrieve a specific bookmark
     *
     * @param cat  The same category the bookmark was added to
     * @param name The name as returned by getBookmarkList(<code>cat</code>)
     * @return The desired bookmark
     */
    public synchronized Bookmark getBookmark(final String cat, final String name) {
        return selectCat(cat).get(name);
    }

    /**
     * Renames a category.  If the named category does not exist,
     * nothing happens.  The tree is <b>NOT</b> updated.
     *
     * @param from the old name of the category
     * @param to   the new name for the category
     */
    public synchronized void renameCategory(final String from, final String to) {
        Map<String, Bookmark> tmp = this.bookmarks.remove(from);
        if (tmp == null) return; //didn't exist
        this.bookmarks.put(to, tmp);
    }

    /**
     * Renames a bookmark.  If the named bookmark does not exist,
     * nothing happens.  The tree is <b>NOT</b> updated.
     *
     * @param cat  the category of the bookmark to rename
     * @param from the old name of the bookmark
     * @param to   the new name for the bookmark
     */
    public synchronized void renameBookmark(final String cat, final String from, final String to) {
        Map<String, Bookmark> tmp = this.bookmarks.get(cat);
        if (tmp == null) return; //didn't exist
        Bookmark b = tmp.remove(from);
        if (b == null) return; //didn't exist
        tmp.put(to, b);
    }

    /**
     * Moves a bookmark from one category to another.
     * If the named bookmark does not exist, nothing happens.
     * The tree is <b>NOT</b> updated.
     *
     * @param cat  the category of the bookmark to move
     * @param name the name of the bookmark to move
     * @param to   the name of the new category for the bookmark.
     *             If it did not exist before, it is created.
     */
    public synchronized void moveBookmark(final String cat, final String name, final String to) {
        Map<String, Bookmark> from = this.bookmarks.get(cat);
        if (from == null) return; //didn't exist
        Bookmark b = from.remove(name);
        if (b == null) return; //didn't exist
        selectCat(to).put(name, b);
    }

    /**
     * Rename selected node.  This can be either a bookmark or a catedory.
     * If the given path is neither a bookmark nor a category, do nothing.
     *
     * @param path the path to the node to rename
     */
    public synchronized void rename(final TreePath path) {
        switch (path.getPathCount()) {
            case 3: //actual bookmark
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getPathComponent(1);
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) path.getPathComponent(2);
                String newBName = DialogUtil.showInputDialog("Rename Bookmark", "New name:", stripBookmarkName(child.toString()));
                if (newBName == null || newBName.length() < 1) return;
                newBName += " " + scanType(child.toString());
                this.renameBookmark(parent.toString(), child.toString(), newBName);
                this.model.removeNodeFromParent(child);
                child.setUserObject(newBName);
                this.model.insertNodeSorted(parent, child);
                break;
            case 2: //category
                DefaultMutableTreeNode cat = (DefaultMutableTreeNode) path.getPathComponent(1);
                String newCName = DialogUtil.showInputDialog("Rename Category", "New name:", stripCategoryName(cat.toString()));
                if (newCName == null || newCName.length() < 1) return;
                newCName = USER_PREFIX + newCName;
                this.renameCategory(cat.toString(), newCName);
                this.model.removeNodeFromParent(cat);
                cat.setUserObject(newCName);
                this.model.insertNodeSorted(cat);
                break;
        }
    }

    /**
     * Export one or more nodes to a specified XML file.
     * If no paths are specified, everything is exported.
     *
     * @param to    the file to save to
     * @param paths the path(s) to export
     */
    public synchronized void export(final File to, final TreePath... paths) {
        if (paths == null || paths.length < 1) {
            this.save(to);
            return;
        }

        PrintStream file;
        Bookmark bookmark;

        try {
            file = new PrintStream(new BufferedOutputStream(new FileOutputStream(to)), false, "UTF-8");
        } catch (Exception e) {
            System.err.println(e.toString());
            file = System.out;
        }

        file.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        file.println("<bookmarks>");

        for (TreePath path : paths) {
            DefaultMutableTreeNode cat = (DefaultMutableTreeNode) path.getPathComponent(1);
            String catName = cat.toString();
            switch (path.getPathCount()) {
                case 3: //actual bookmark
                    String bmName = path.getPathComponent(2).toString();
                    bookmark = selectCat(catName).get(bmName);
                    writeBookmark(catName, bmName, bookmark, file);
                    break;
                case 2: //category
                    Map<String, Bookmark> category = this.bookmarks.get(catName);
                    for (String name : category.keySet()) { //each bookmark
                        bookmark = category.get(name);
                        writeBookmark(catName, name, bookmark, file);
                    }
                    break;
            }
        }

        file.println("</bookmarks>");
        file.flush();
        file.close();
    }

    /**
     * Gets a list of available categories
     *
     * @return A Collection of available category name Strings
     */
    public synchronized Collection<String> getCategoryList() {
        return this.bookmarks.keySet();
    }

    /**
     * Gets a list of available bookmarks in a given category
     *
     * @param category the category to list
     * @return a Collection of available bookmark name Strings in that category
     */
    public synchronized Collection<String> getBookmarkList(final String category) {
        return selectCat(category).keySet();
    }

    /**
     * Returns the model from which to generate the tree.
     *
     * @return the model
     */
    public BookmarkTreeModel getModel() {
        return model;
    }

    /**
     * Load XML format bookmarks from a File
     */
    public synchronized void load(final File file) {
        try {
            this.load(new BufferedInputStream(new FileInputStream(file)), USER_PREFIX);
        } catch (FileNotFoundException fnfe) {
            System.err.println("Error loading bookmarks from " + file + ":\nFile not found");
        }
    }

    /**
     * Load XML format bookmarks from an InputStream
     */
    public synchronized void load(final InputStream stream, final String prefix) {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(stream, new XMLBookmarkHandler(prefix));
        } catch (Exception e) {
            System.err.println("Exception while loading bookmarks:");
            e.printStackTrace();
        }
    }

    /**
     * Save bookmarks to "bookmarks.xml"
     */
    public synchronized void save() {
        this.save("bookmarks.xml");
    }

    /**
     * Saves bookmarks to a given filename in XML format
     *
     * @param filename the name of the file to save to
     */
    public synchronized void save(final String filename) {
        this.save(new File(filename));
    }

    /**
     * Saves bookmarks to a given file in UTF-8 encoded XML format
     *
     * @param path the file to save to
     */
    public synchronized void save(final File path) {
        PrintStream file;

        try {
            file = new PrintStream(new BufferedOutputStream(new FileOutputStream(path)), false, "UTF-8");
        } catch (Exception e) {
            System.err.println(e.toString());
            file = System.out;
        }

        file.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        file.println("<bookmarks>");
        for (String cat : this.bookmarks.keySet()) { //each category
            if (!cat.startsWith(USER_PREFIX)) continue; //only save user bookmarks
            Map<String, Bookmark> category = this.bookmarks.get(cat);
            for (String name : category.keySet()) { //each bookmark
                Bookmark bookmark = category.get(name);
                writeBookmark(cat, name, bookmark, file);
            }
        }
        file.println("</bookmarks>");
        file.flush();
        file.close();
    }

    /**
     * Writes a given bookmark to a file
     *
     * @param cat      the name of the category
     * @param name     the name of the bookmark
     * @param bookmark the actual bookmark to write
     * @param file     the stream to write to
     */
    public static void writeBookmark(final String cat, final String name, final Bookmark bookmark, final PrintStream file) {
        file.println("    <bookmark>");
        file.println("        <category>" + stripCategoryName(cat) + "</category>"); //trim prefix
        file.println("        <name>" + stripBookmarkName(name) + "</name>");
        file.println("        <url>" + bookmark.url + "</url>");
        file.println("        <directory>" + bookmark.dir + "</directory>");
        file.println("        <file>" + bookmark.file + "</file>");
        file.println("        <sweep>" + bookmark.sweep + "</sweep>");
        file.println("        <scantype>" + bookmark.scan_type + "</scantype>");
        file.println("        <color>");
        for (String type : bookmark.scale.keySet()) {
            file.println("            <" + type + ">");
            Bookmark.Scale scale = bookmark.scale.get(type);
            file.println("                <autoscale>" + scale.autoscale + "</autoscale>");
            file.println("                <minval>" + scale.minval + "</minval>");
            file.println("                <maxval>" + scale.maxval + "</maxval>");
            file.println("            </" + type + ">");
        }
        file.println("        </color>");
        file.println("        <pan>");
        file.println("            <x>" + bookmark.x + "</x>");
        file.println("            <y>" + bookmark.y + "</y>");
        file.println("        </pan>");
        file.println("        <range>" + bookmark.range + "</range>");
        file.println("        <ring>" + bookmark.ring + "</ring>");
        file.println("        <rhiheight>" + bookmark.rhi_height + "</rhiheight>");
        file.println("        <comment>");
        file.println(bookmark.comment.trim());
        file.println("        </comment>");
        file.println("    </bookmark>");
    }

    /**
     * Removes one or more nodes from the tree.
     * If no path is specified, nothing is removed.
     *
     * @param paths the paths to the nodes to remove
     */
    public synchronized void remove(final TreePath... paths) {
        if (paths == null) return;
        for (TreePath path : paths) {
            if (path == null) continue;
            switch (path.getPathCount()) {
                case 3: //actual bookmark
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getPathComponent(1);
                    Object child = path.getPathComponent(2);
                    this.selectCat(parent.toString()).remove(child.toString());
                    this.model.removeNodeFromParent((DefaultMutableTreeNode) this.model.getChild(parent, this.model.getIndexOfChild(parent, child)));
                    if (parent.getChildCount() == 0) this.model.removeNodeFromParent(parent);
                    break;
                case 2: //category
                    Object cat = path.getPathComponent(1);
                    this.bookmarks.remove(cat.toString());
                    this.model.removeNodeFromParent((DefaultMutableTreeNode) this.model.getChild(this.model.getRoot(), this.model.getIndexOfChild(this.model.getRoot(), cat)));
                    break;
                case 1: //root
                    this.bookmarks.clear();
                    Object root = this.model.getRoot();
                    while (this.model.getChildCount(root) > 0) {
                        this.model.removeNodeFromParent((DefaultMutableTreeNode) this.model.getChild(root, 0));
                    }
                    break;
            }
        }
    }

    /**
     * Move one or more nodes into another category.
     * The user is prompted with a dialog for the new category to move to.
     * If no path is specified, do nothing.
     * If a category is specified, all bookmarks in that category are moved.
     * If this causes a category to become empty, the empty category is removed.
     *
     * @param paths the path(s) to the node(s) to move
     */
    public synchronized void move(final TreePath... paths) {
        if (paths == null || paths.length < 1) return;
        Collection<String> cats = bmc.getCategoryList();
        ArrayList<String> choices = new ArrayList<String>(cats.size());
        for (String cat : cats)
            if (cat.startsWith(BookmarkControl.USER_PREFIX))
                choices.add(stripCategoryName(cat));

        String newCat = null;
        switch (paths[0].getPathCount()) {
            case 2:
            case 3:
                newCat = stripCategoryName(paths[0].getPathComponent(1).toString());
                break;
        }
        if (newCat == null)
            newCat = DialogUtil.showOptionInputDialog("Move Bookmark(s)", "Category to move bookmark(s) to:", choices.toArray());
        else
            newCat = DialogUtil.showOptionInputDialog("Move Bookmark(s)", "Category to move bookmark(s) to:", choices.toArray(), newCat);

        if (newCat == null || newCat.length() < 1) return;
        newCat = USER_PREFIX + newCat;

        for (TreePath path : paths) {
            if (path == null) continue;
            switch (path.getPathCount()) {
                case 3: //actual bookmark
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getPathComponent(1);
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) path.getPathComponent(2);
                    this.moveBookmark(parent.toString(), child.toString(), newCat);
                    this.model.removeNodeFromParent(child);
                    if (parent.getChildCount() == 0) this.model.removeNodeFromParent(parent);
                    this.model.insertNodeToCategory(newCat, child);
                    break;
                case 2: //category
                    DefaultMutableTreeNode cat = (DefaultMutableTreeNode) path.getPathComponent(1);
                    if (newCat.equals(cat.toString())) continue;
                    while (cat.getChildCount() > 0) {
                        DefaultMutableTreeNode node = cat.getFirstLeaf();
                        this.moveBookmark(cat.toString(), node.toString(), newCat);
                        this.model.removeNodeFromParent(node);
                        this.model.insertNodeToCategory(newCat, node);
                    }
                    this.model.removeNodeFromParent(cat);
                    break;
            }
        }
    }

    /**
     * Duplicates one or more nodes.
     * The new nodes are named the same as the original, but prefixed with "Copy of ".
     *
     * @param paths the path(s) to duplicate
     */
    public synchronized void duplicate(final TreePath... paths) {
        for (TreePath path : paths) {
            Bookmark bookmark;
            switch (path.getPathCount()) {
                case 3: //actual bookmark
                    String cat = path.getPathComponent(1).toString();
                    String name = path.getPathComponent(2).toString();
                    bookmark = new Bookmark(this.getBookmark(cat, name));
                    String fullname = "Copy of " + name;
                    if (selectCat(cat).put(fullname, bookmark) == null) {
                        this.model.insertNodeToCategory(cat, new DefaultMutableTreeNode(fullname));
                    } //only add to tree if actually new
                    break;
                case 2: //category
                    String catName = path.getPathComponent(1).toString();
                    Map<String, Bookmark> oldCat = selectCat(catName);
                    Map<String, Bookmark> newCat = selectCat(catName = USER_PREFIX + "Copy of " + stripCategoryName(catName)); //fix name
                    for (String bookmarkName : oldCat.keySet()) {
                        bookmark = new Bookmark(oldCat.get(bookmarkName));
                        if (newCat.put(bookmarkName, bookmark) == null) {
                            this.model.insertNodeToCategory(catName, new DefaultMutableTreeNode(bookmarkName));
                        } //only add to tree if actually new
                    }
                    break;
            }
        }
    }

    /**
     * Removes the prefix from a category name
     *
     * @param the category name to trim
     * @return the trimmed category name
     */
    private static String stripCategoryName(final String cat) {
        return cat.substring(cat.indexOf(":") + 1, cat.length()); //trim prefix
    }

    /**
     * Removes the scan type from a bookmark name
     *
     * @param the bookmark name to trim
     * @return the trimmed bookmark name
     */
    private static String stripBookmarkName(final String name) {
        return name.substring(0, name.lastIndexOf(" ")); //trim scan type
    }

    /**
     * Retrieves the scan type from a bookmark name
     *
     * @param the bookmark name to trim
     * @return the scan type
     */
    private static String scanType(final String name) {
        return name.substring(name.lastIndexOf(" ") + 1, name.length()); //trim actual name
    }
}
