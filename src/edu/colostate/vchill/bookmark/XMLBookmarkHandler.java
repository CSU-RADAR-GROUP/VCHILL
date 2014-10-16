package edu.colostate.vchill.bookmark;

import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.file.FileFunctions.Moment;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * ContentHandler for VCHILL's XML bookmark file.
 * Unlike the old-style text parser, this parser adds all bookmarks it finds to the controller.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-07-07
 */
public class XMLBookmarkHandler extends DefaultHandler
{
    private static final ScaleManager sm = ScaleManager.getInstance();

    private Bookmark bookmark;
    private boolean inCat;
    private boolean inName;
    private boolean inUrl;
    private boolean inDir;
    private boolean inFile;
    private boolean inSweep;
    private boolean inScan_type;
    private boolean inColor;
    private boolean inAutoscale;
    private boolean inMinval;
    private boolean inMaxval;
    private boolean inZoom;
    private boolean inPan;
    private boolean inX;
    private boolean inY;
    private boolean inRange;
    private boolean inRing;
    private boolean inRhi_height;
    private boolean inComment;
    private StringBuilder tmpCat;
    private StringBuilder tmpName;
    private StringBuilder tmpUrl;
    private StringBuilder tmpDir;
    private StringBuilder tmpFile;
    private StringBuilder tmpSweep;
    private StringBuilder tmpScan_type;
    private StringBuilder tmpAutoscale;
    private StringBuilder tmpMinval;
    private StringBuilder tmpMaxval;
    private StringBuilder tmpZoom;
    private StringBuilder tmpX;
    private StringBuilder tmpY;
    private StringBuilder tmpRange;
    private StringBuilder tmpRing;
    private StringBuilder tmpRhi_height;
    private StringBuilder tmpComment;
    private double zoom = 1;

    private final String prefix;

    public XMLBookmarkHandler (final String prefix)
    {
        this.prefix = prefix;
    }
    
    @Override public void startDocument () {
        this.bookmark = new Bookmark();
		this.zoom = 1.0;
        this.inCat = false;
        this.inName = false;
        this.inUrl = false;
        this.inDir = false;
        this.inFile = false;
        this.inSweep = false;
        this.inScan_type = false;
        this.inColor = false;
        this.inAutoscale = false;
        this.inMinval = false;
        this.inMaxval = false;
        this.inZoom = false;
        this.inPan = false;
        this.inX = false;
        this.inY = false;
        this.inRange = false;
        this.inRing = false;
        this.inRhi_height = false;
        this.inComment = false;
        this.tmpCat = new StringBuilder();
        this.tmpName = new StringBuilder();
        this.tmpUrl = new StringBuilder();
        this.tmpDir = new StringBuilder();
        this.tmpFile = new StringBuilder();
        this.tmpSweep = new StringBuilder();
        this.tmpScan_type = new StringBuilder();
        this.tmpAutoscale = new StringBuilder();
        this.tmpMinval = new StringBuilder();
        this.tmpMaxval = new StringBuilder();
        this.tmpZoom = new StringBuilder();
        this.tmpX = new StringBuilder();
        this.tmpY = new StringBuilder();
        this.tmpRange = new StringBuilder();
        this.tmpRing = new StringBuilder();
        this.tmpRhi_height = new StringBuilder();
        this.tmpComment = new StringBuilder();
    }

    @Override public void startElement (final String namespaceURI, final String localName, final String qualifiedName, final Attributes attribs) throws SAXException
    {
        String lowname = qualifiedName.toLowerCase();
        if (lowname.equals("category")) {
            this.inCat = true;
        } else if (lowname.equals("name")) {
            this.inName = true;
        } else if (lowname.equals("url")) {
            this.inUrl = true;
        } else if (lowname.equals("directory")) {
            this.inDir = true;
        } else if (lowname.equals("file")) {
            this.inFile = true;
        } else if (lowname.equals("sweep")) {
            this.inSweep = true;
        } else if (lowname.equals("scantype")) {
            this.inScan_type = true;
        } else if (lowname.equals("color")) {
            this.inColor = true;
        } else if (lowname.equals("autoscale")) {
            this.inAutoscale = true;
        } else if (lowname.equals("minval")) {
            this.inMinval = true;
        } else if (lowname.equals("maxval")) {
            this.inMaxval = true;
        } else if (lowname.equals("zoomfactor")) {
            this.inZoom = true;
        } else if (lowname.equals("pan")) {
            this.inPan = true;
        } else if (lowname.equals("x")) {
            this.inX = true;
        } else if (lowname.equals("y")) {
            this.inY = true;
        } else if (lowname.equals("range")) {
            this.inRange = true;
        } else if (lowname.equals("ring")) {
            this.inRing = true;
        } else if (lowname.equals("rhiheight")) {
            this.inRhi_height = true;
        } else if (lowname.equals("comment")) {
            this.inComment = true;
        } else if (this.inComment) { //allow arbitrary tags within comments
            this.tmpComment.append("<").append(lowname);
            for (int i = 0; i < attribs.getLength(); ++i) {
                this.tmpComment.append(" ").append(attribs.getQName(i)).append("=\"").append(attribs.getValue(i)).append("\"");
            }
            this.tmpComment.append(">");
        }
    }

    @Override public void characters (final char[] text, final int start, final int length) throws SAXException
    {
        String chars = new String(text, start, length);
        if (this.inCat) {
            this.tmpCat.append(chars);
        } else if (this.inName) {
            this.tmpName.append(chars);
        } else if (this.inUrl) {
            this.tmpUrl.append(chars);
        } else if (this.inDir) {
            this.tmpDir.append(chars);
        } else if (this.inFile) {
            this.tmpFile.append(chars);
        } else if (this.inSweep) {
            this.tmpSweep.append(chars);
        } else if (this.inScan_type) {
            this.tmpScan_type.append(chars);
        } else if (this.inColor) {
            if (this.inAutoscale) {
                this.tmpAutoscale.append(chars);
            } else if (this.inMinval) {
                this.tmpMinval.append(chars);
            } else if (this.inMaxval) {
                this.tmpMaxval.append(chars);
            }
        } else if (this.inZoom) {
            this.tmpZoom.append(chars);
        } else if (this.inPan) {
            if (this.inX) {
                this.tmpX.append(chars);
            } else if (this.inY) {
                this.tmpY.append(chars);
            }
        } else if (this.inRange) {
            this.tmpRange.append(chars);
        } else if (this.inRing) {
            this.tmpRing.append(chars);
        } else if (this.inRhi_height) {
            this.tmpRhi_height.append(chars);
        } else if (this.inComment) {
            this.tmpComment.append(chars);
        }
    }

    @Override public void endElement (final String namespaceURI, final String localName, final String qualifiedName) throws SAXException
    {
        String lowname = (qualifiedName.toLowerCase());
        if (lowname.equals("bookmarks")) { //main element
        } else if (lowname.equals("bookmark")) {
            BookmarkControl bmc = BookmarkControl.getInstance();
            bmc.addBookmark(this.prefix + this.tmpCat.toString(), this.tmpName.toString(), this.bookmark);
            this.tmpCat = new StringBuilder();
            this.tmpName = new StringBuilder();
            this.bookmark = new Bookmark();
            this.zoom = 1.0;
        } else if (lowname.equals("category")) {
            this.inCat = false;
        } else if (lowname.equals("name")) {
            this.inName = false;
        } else if (lowname.equals("url")) {
            this.inUrl = false;
            this.bookmark.url = this.tmpUrl.toString();
            this.tmpUrl = new StringBuilder();
        } else if (lowname.equals("directory")) {
            this.inDir = false;
            this.bookmark.dir = this.tmpDir.toString();
            this.tmpDir = new StringBuilder();
        } else if (lowname.equals("file")) {
            this.inFile = false;
            this.bookmark.file = this.tmpFile.toString();
            this.tmpFile = new StringBuilder();
        } else if (lowname.equals("sweep")) {
            this.inSweep = false;
            String[] parts = this.tmpSweep.toString().split(" ");
            if (parts.length > 1 && parts[1].length() == 1) parts[1] = "0" + parts[1];
            this.bookmark.sweep = parts.length > 2 ? this.tmpSweep.toString() : parts[0] + " " + parts[1];
            this.tmpSweep = new StringBuilder();
        } else if (lowname.equals("scantype")) {
            this.inScan_type = false;
            this.bookmark.scan_type = this.tmpScan_type.toString();
            this.tmpScan_type = new StringBuilder();
        } else if (lowname.equals("color")) {
            this.inColor = false;
        } else if (lowname.equals("autoscale")) {
            this.inAutoscale = false;
        } else if (lowname.equals("minval")) {
            this.inMinval = false;
        } else if (lowname.equals("maxval")) {
            this.inMaxval = false;
        } else if (lowname.equals("zoomfactor")) {
            this.inZoom = false;
            this.zoom = Double.parseDouble(this.tmpZoom.toString());
            this.tmpZoom = new StringBuilder();
        } else if (lowname.equals("x")) {
            this.inX = false;
        } else if (lowname.equals("y")) {
            this.inY = false;
        } else if (lowname.equals("pan")) {
            this.inPan = false;
            this.bookmark.x = Double.parseDouble(this.tmpX.toString());
            this.tmpX = new StringBuilder();
            this.bookmark.y = Double.parseDouble(this.tmpY.toString());
            this.tmpY = new StringBuilder();
        } else if (lowname.equals("range")) {
            this.inRange = false;
            this.bookmark.range = Double.parseDouble(this.tmpRange.toString()) / this.zoom;
            this.tmpRange = new StringBuilder();
        } else if (lowname.equals("ring")) {
            this.inRing = false;
            this.bookmark.ring = this.tmpRing.toString();
            this.tmpRing = new StringBuilder();
        } else if (lowname.equals("rhiheight")) {
            this.inRhi_height = false;
            this.bookmark.rhi_height = this.tmpRhi_height.toString();
            this.tmpRhi_height = new StringBuilder();
        } else if (lowname.equals("comment")) {
            this.inComment = false;
            this.bookmark.comment = this.tmpComment.toString().trim();
            this.tmpComment = new StringBuilder();
        } else if (this.inComment) { //allow arbitrary tags within comments
            this.tmpComment.append("</").append(lowname).append(">");
        } else { //scaling info
            this.setScale(qualifiedName);
        }
    }

    private void setScale (String type) //argument not final
    {
        if (sm.getScale(type) == null) { //not a (currently) known data type
            Moment tmp = Moment.translate(type);
            if (tmp != null) { //known old data type
                ChillMomentFieldScale scale = sm.getScale(tmp.ordinal());
                if (scale != null) {
                    type = scale.fieldName; //maps to this new type
                }
            }
        }
        Bookmark.Scale bmScale = new Bookmark.Scale();
        bmScale.autoscale = Boolean.valueOf(this.tmpAutoscale.toString()).booleanValue();
        this.tmpAutoscale = new StringBuilder();
        String min = this.tmpMinval.toString();
        try { Double.parseDouble(min); bmScale.minval = min; }
        catch (Exception e) { bmScale.minval = String.valueOf(Double.MIN_VALUE); }
        this.tmpMinval = new StringBuilder();
        String max = this.tmpMaxval.toString();
        try { Double.parseDouble(max); bmScale.maxval = max; }
        catch (Exception e) { bmScale.maxval = String.valueOf(Double.MAX_VALUE); }
        this.tmpMaxval = new StringBuilder();
        this.bookmark.scale.put(type, bmScale);
    }

    //do nothing:
    @Override public void endDocument () {}
    @Override public void startPrefixMapping (final String prefix, final String uri) {}
    @Override public void endPrefixMapping (final String prefix) {}
    @Override public void ignorableWhitespace (final char[] text, final int start, final int length) throws SAXException {}
    @Override public void processingInstruction (final String target, final String data) {}
    @Override public void skippedEntity (final String name) {}
    @Override public void setDocumentLocator (final Locator locator) {}
}
