package edu.colostate.vchill.gui;

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
import javax.imageio.*;
import java.awt.*;

import javax.swing.*;

import java.awt.event.*;



public class MapServerConfig extends JPanel
{

    private JList list;
    private DefaultListModel listModel;

    private static final String displayMapString = "Display Map";
    private JButton displayMapButton;	
	
	
	public static String userMapLayers = "";
	
	private static ArrayList<String> layerArrayList;
	
	public MapServerConfig() 
	{		
		
		layerArrayList = new ArrayList<String>();

		// Get XML Document from server
		InputSource xmlSource = getXMLDoc();
		
		// Parse it
		parseInputSource(xmlSource);		
				
		// Display it for the user
		// createAndShowPreferencesFrame();
		
	
	}
 
	public static void displayMap(int[] selectedIndices)
	{
		
		
		
		String layerString = getLayerString(selectedIndices);	

		userMapLayers = layerString;
				
	}

	private static String getLayerString(int[] selectedIndices)
	{
		String theLayerString = "";
		
		for(int i = 0; i < selectedIndices.length; i++)
		{
			if(i == 0)
			{
				theLayerString = layerArrayList.get(selectedIndices[0]);
			}
			else
			{
				theLayerString = theLayerString + "," + layerArrayList.get(selectedIndices[i]);
			}
		}		
		
		return theLayerString;
		
	}
	
	private static String getTagValue(String sTag, Element eElement) 
	{
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}
	
	
	private static InputSource getXMLDoc()
	{
		InputSource is = null;
		try
		{
			URL mapServerRequest = new URL("http://wms.chill.colostate.edu/cgi-bin/mapserv?REQUEST=GetCapabilities&VERSION=1.1.1&SERVICE=WMS&map=/var/www/html/maps/test.map&SERVICE=WMS&VERSION=1.1.1");
			BufferedReader in = new BufferedReader(new InputStreamReader(mapServerRequest.openStream()));			
			
			String inputLine;
			String wholeXmlFile = "";
		
			while((inputLine=in.readLine())!=null)
			{
				System.out.println(inputLine);
				wholeXmlFile += inputLine;
			}
		
			in.close();
		
			is = new InputSource(new StringReader(wholeXmlFile));
		
			return is;
		}
		catch(Exception e)
		{
			System.out.println("Something went wrong with getting the XML document");
		}
		
		return is;
	} 

	private static void parseInputSource(InputSource is)
	{
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
 
			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("Layer");
			System.out.println("-----------------------");
 
			
			
			for (int temp = 0; temp < nList.getLength(); temp++) 
			{
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element eElement = (Element) nNode;

					layerArrayList.add(getTagValue("Name", eElement));
					System.out.println("Name : " + getTagValue("Name", eElement));
				}
			}			
			
		}
		catch(Exception e)
		{
			System.out.println("Something went wrong with the parsing");			
		}
				
	}

    public static void createAndShowPreferencesFrame() 
    {

    	//Create and set up the window.
        JFrame frame = new JFrame("Map");
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new MapServerConfigWindow(layerArrayList);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }


}
