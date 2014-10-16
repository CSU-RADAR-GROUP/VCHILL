package edu.colostate.vchill.gui;

import edu.colostate.vchill.ViewControl;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import java.lang.*;

/**
 * @author MichaelRausch
 * 
 */
public class MapServerConfig extends JPanel {

  private static final MapServerConfig MSC = new MapServerConfig();

  private String userMapUnderlayLayers = "";
  private String userMapOverlayLayers = "";

  private Boolean EPSG4326Boolean = false;
  private Boolean AUTO42003Boolean = false;

  private Boolean plottedUnderlayOnce = false;

  // Array of titles of layers
  private static ArrayList<String> layerTitleArrayList;
  // Array of names of layers
  private static ArrayList<String> layerNameArrayList;

  private final static ViewControl vc = ViewControl.getInstance();
  private final static WindowManager wm = WindowManager.getInstance();

  /**
     *
     * 
     */

  private MapServerConfig() {

    layerTitleArrayList = new ArrayList<String>();
    layerNameArrayList = new ArrayList<String>();

    // Get XML Document from server
    InputSource xmlSource = getXMLDoc();

    // Parse it
    parseInputSource(xmlSource);

  }

  /**
   * @param selectedUnderlayIndices
   * @param selectedOverlayIndices
   */

  public void displayMap(int[] selectedUnderlayIndices, int[] selectedOverlayIndices) {

    userMapUnderlayLayers = getLayerString(selectedUnderlayIndices);
    userMapOverlayLayers = getLayerString(selectedOverlayIndices);

    wm.replotOverlay();
    vc.rePlot();

  }

  /**
   * 
   * @param indices
   * @return theLayerString
   */

  private static String getLayerString(int[] indices) {
    String theLayerString = "";

    for (int i = indices.length - 1; i >= 0; i--) {
      if (i == indices.length - 1) {
        theLayerString = layerNameArrayList.get(indices[i]);
      } else {
        theLayerString = theLayerString + "," + layerNameArrayList.get(indices[i]);
      }
    }

    return theLayerString;

  }

  private static String getTagValue(String sTag, Element eElement) {
    NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();

    Node nValue = (Node) nlList.item(0);

    return nValue.getNodeValue();
  }

  private static InputSource getXMLDoc() {
    InputSource is = null;
      System.setProperty("http.agent", "VCHILL");

      try {
      URL mapServerRequest = new URL(
        "http://wms.chill.colostate.edu/cgi-bin/mapserv?REQUEST=GetCapabilities&VERSION=1.1.1&SERVICE=WMS&map=/var/www/html/maps/test.map&SERVICE=WMS&VERSION=1.1.1");
      // URL mapServerRequest = new
      // URL("http://sedac.ciesin.columbia.edu/geoserver/gwc/service/wms?REQUEST=GetCapabilities&VERSION=1.1.1&SERVICE=WMS");

      BufferedReader in = new BufferedReader(new InputStreamReader(mapServerRequest.openStream()));

      String inputLine;
      String wholeXmlFile = "";

      while ((inputLine = in.readLine()) != null) {
        // System.out.println(inputLine);
        wholeXmlFile += inputLine;
      }

      in.close();

      is = new InputSource(new StringReader(wholeXmlFile));

      return is;
    } catch (Exception e) {
      System.out.println("Something went wrong with getting the XML document");
    }

    return is;
  }

  private void parseInputSource(InputSource is) {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(is);
      doc.getDocumentElement().normalize();

      Node capabilityNode = null;
      Node parentLayerNode = null;

      // Find all children of first node, search for Capability node
      for (Node child = doc.getDocumentElement().getFirstChild(); child != null; child = child.getNextSibling()) {

        String capabilities = "Capability";
        if (capabilities.equals((String) child.getNodeName()))
          capabilityNode = child;
      }

      // Once we have found the capability node, we look for the layer node
      // amongst the children.
      if (capabilityNode != null) {
        for (Node child = capabilityNode.getFirstChild(); child != null; child = child.getNextSibling()) {
          String parentLayer = "Layer";
          if (parentLayer.equals((String) child.getNodeName()))
            parentLayerNode = child;
        }
      }

      // once we have the first layer node we search all of its children to find
      // the names and titles of layers
      if (parentLayerNode != null) {
        for (Node child = parentLayerNode.getFirstChild(); child != null; child = child.getNextSibling()) {
          String parentLayer = "Layer";
          if (parentLayer.equals((String) child.getNodeName())) {
            Element eElement = (Element) child;

            // System.out.println("Title: " + getTagValue("Title", eElement));
            layerTitleArrayList.add(getTagValue("Title", eElement));
            // System.out.println("Name: " + getTagValue("Name", eElement));
            layerNameArrayList.add(getTagValue("Name", eElement));
          }
          String SRSLayer = "SRS";
          if (SRSLayer.equals(child.getNodeName())) {
            String EPSG4326String = "EPSG:4326";
            String AUTO42003String = "AUTO:42003";
            if (EPSG4326String.equals(child.getTextContent()))
              EPSG4326Boolean = true;
            if (AUTO42003String.equals(child.getTextContent()))
              AUTO42003Boolean = true;
          }

        }
      }

    } catch (Exception e) {
      System.out.println("Something went wrong with the parsing");
    }

  }

  public static void createAndShowPreferencesFrame() {

    // Create and set up the window.
    JFrame frame = new JFrame("Map");
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Create and set up the content pane.
    JComponent newContentPane = new MapServerConfigWindow(layerTitleArrayList);
    newContentPane.setOpaque(true); // content panes must be opaque
    frame.setContentPane(newContentPane);

    // Display the window.
    frame.pack();
    frame.setVisible(true);
  }

  public Boolean EPSG4326IsEnabled() {
    return EPSG4326Boolean;
  }

  public Boolean AUTO42003IsEnabled() {
    return AUTO42003Boolean;
  }

  public static MapServerConfig getInstance() {
    return MSC;
  }

  public Boolean plottedUnderlayOnce() {
    return plottedUnderlayOnce;
  }

  public void plottedOnce(Boolean value) {
    plottedUnderlayOnce = value;
  }

  public String getUserMapOverlayLayers() {
    return userMapOverlayLayers;
  }

  public String getUserMapUnderlayLayers() {
    return userMapUnderlayLayers;
  }
}