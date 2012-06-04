package edu.colostate.vchill.color;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ChillDefines.ColorType;
import java.awt.Color;
import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * ContentHandler for VCHILL's XML color description files
 *
 * @author Jochen Deyke
 * @version 2006-01-18
 */
public class XMLColorMapHandler extends DefaultHandler
{
    private ColorMap colorMap;
    private ArrayList<Color> tmpColors;
    private boolean inName;
    private boolean inRed;
    private boolean inGreen;
    private boolean inBlue;
    private String tmpName;
    private String tmpRed;
    private String tmpGreen;
    private String tmpBlue;

    public XMLColorMapHandler (final ColorMap colorMap)
    {
        this.colorMap   = colorMap;
        this.tmpColors  = new ArrayList<Color>(ChillDefines.numColorLevels);
        this.inName     = false;
        this.inRed      = false;
        this.inGreen    = false;
        this.inBlue     = false;
        this.tmpName    = "";
        this.tmpRed     = "";
        this.tmpGreen   = "";
        this.tmpBlue    = "";
    }

    public void startElement (final String namespaceURI, final String localName, final String qualifiedName, final Attributes attribs) throws SAXException
    {
        if ((qualifiedName.toLowerCase()).equals("name")) {
            this.inName = true;
        } else if ((qualifiedName.toLowerCase()).equals("red")) {
            this.inRed = true;
        } else if ((qualifiedName.toLowerCase()).equals("green")) {
            this.inGreen = true;
        } else if ((qualifiedName.toLowerCase()).equals("blue")) {
            this.inBlue = true;
        }
    }

    public void characters (final char[] text, final int start, final int length) throws SAXException
    {
        if (this.inName) {
            this.tmpName += new String(text, start, length);
        } else if (this.inRed) {
            this.tmpRed += new String(text, start, length);
        } else if (this.inGreen) {
            this.tmpGreen += new String(text, start, length);
        } else if (this.inBlue) {
            this.tmpBlue += new String(text, start, length);
        }
    }

    public void endElement (final String namespaceURI, final String localName, final String qualifiedName) throws SAXException
    {
        if ((qualifiedName.toLowerCase()).equals("type")) {
            this.colorMap.addType(ColorType.valueOf(this.tmpName), this.tmpColors);
            this.tmpColors = new ArrayList<Color>(ChillDefines.numColorLevels);
            this.tmpName = "";
        } else if ((qualifiedName.toLowerCase()).equals("name")) {
            this.inName = false;
        } else if ((qualifiedName.toLowerCase()).equals("color")) {
            this.tmpColors.add(new Color(
                Integer.parseInt(this.tmpRed),
                Integer.parseInt(this.tmpGreen),
                Integer.parseInt(this.tmpBlue)
            ));
            this.tmpRed = "";
            this.tmpGreen = "";
            this.tmpBlue = "";
        } else if ((qualifiedName.toLowerCase()).equals("red")) {
            this.inRed = false;
        } else if ((qualifiedName.toLowerCase()).equals("green")) {
            this.inGreen = false;
        } else if ((qualifiedName.toLowerCase()).equals("blue")) {
            this.inBlue = false;
        }
    }

    //do nothing:
    public void startDocument () {}
    public void endDocument () {}
    public void startPrefixMapping (final String prefix, final String uri) {}
    public void endPrefixMapping (final String prefix) {}
    public void ignorableWhitespace(final char[] text, final int start, final int length) throws SAXException {}
    public void processingInstruction (final String target, final String data) {}
    public void skippedEntity (final String name) {}
    public void setDocumentLocator (final Locator locator) {}
}
