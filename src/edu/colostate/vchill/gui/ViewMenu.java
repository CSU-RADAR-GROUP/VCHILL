package edu.colostate.vchill.gui;

import java.awt.*; // Rausch
import java.awt.event.*; // Rausch
import javax.swing.*; // Rausch


import edu.colostate.vchill.DialogUtil;
import edu.colostate.vchill.Loader;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.Version;
import edu.colostate.vchill.ViewControl;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.color.ColorEditor;
import edu.colostate.vchill.color.XMLControl;
import edu.colostate.vchill.map.MapTextParser;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;


// Rausch
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






/**
 * This is class that is used to create JMenuBars that can be used to
 * add into JFrames.  It is just a reasonable way to make the menu a
 * bit more modular.
 *
 * @author  Justin Carlson
 * @author  Jochen Deyke
 * @author  Alexander Deyke
 * @author  Michael Rausch
 * @author  jpont
 * @created January 03, 2003
 * @version 2010-08-02
 */

public class ViewMenu extends JMenuBar
{
	
    private MapServerConfig MapServerConfigObject = MapServerConfig.getInstance();
	
	
    private static final WindowManager wm = WindowManager.getInstance();
    private static final ViewControl vc = ViewControl.getInstance();
    private final static ScaleManager sm = ScaleManager.getInstance();
    private final static Config gcc = Config.getInstance();
    private final static edu.colostate.vchill.color.Config ccc = edu.colostate.vchill.color.Config.getInstance();
    private static final int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    private final Map<String, JCheckBoxMenuItem> mapItems;
    private final ColorEditor editor = new ColorEditor(new XMLControl(false)); //interpolateable

    private final static String NONE = "<none>";
    private final static String CUSTOM = "Custom...";
    
    private final JMenu plotMenu, ascopeMenu, numdumpMenu;

    /**
     * Creates the Actions that will be used to make up the menu items.
     */
    public ViewMenu ()
    {
        List<String> colors = XMLControl.getListOfFiles();
        List<String> maps = MapTextParser.getListOfFiles();
        this.mapItems = new HashMap<String, JCheckBoxMenuItem>(maps.size() + 2); //also none & custom

        this.add(createFileMenu());
        this.add(this.plotMenu = createPlotMenu());
        this.add(this.ascopeMenu = createAScopeMenu());
        this.add(this.numdumpMenu = createNumDumpMenu());
        this.add(createColorMenu(colors));
        this.add(createMapMenu(maps));
        this.add(createMapServerMenu());
        
        
        this.add(createHelpMenu());
        updateWindowMenus();
        sm.addObserver(new Observer () {
            public void update (final Observable o, final Object arg) {
                updateWindowMenus();
            }});
    }

    /**
     * Creates the menus that will have basic connection options,
     * connect, disconnect etc.
     *
     * @return A Menu with the connection options inside it.
     */
    private JMenu createFileMenu ()
    {
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        JMenuItem menuItem;

        for (JMenuItem item : ViewFileBrowserPopup.createConnectionItems()) menu.add(item);

        menu.addSeparator();

        menuItem = new JMenuItem(new AbstractAction("Disconnect") {
            public void actionPerformed (final ActionEvent ae) {
                vc.disconnect();
            }}) {
            @Override public void paint (final java.awt.Graphics g) {
                setEnabled(vc.isConnected());
                super.paint(g);
            }};
        menuItem.setMnemonic(KeyEvent.VK_D);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, MENU_MASK));
        menu.add(menuItem);

        menuItem = new JMenuItem(new AbstractAction("Reconnect") {
            public void actionPerformed (final ActionEvent ae) {
                vc.reconnect();
            }}) {
            @Override public void paint (final java.awt.Graphics g) {
                setEnabled(vc.getControlMessage().getURL() != null);
                super.paint(g);
            }};
        menuItem.setMnemonic(KeyEvent.VK_R);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, MENU_MASK));
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem(new AbstractAction("Replot") {
            public void actionPerformed (final ActionEvent ae) {
                vc.rePlot();
            }});
        menuItem.setMnemonic(KeyEvent.VK_P);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        menu.add(menuItem);

        menuItem = new JMenuItem(new AbstractAction("Saved Image Browser") {
            public void actionPerformed (final ActionEvent ae) {
                wm.showImages();
            }});
        menuItem.setMnemonic(KeyEvent.VK_I);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem(new AbstractAction("Exit") {
            public void actionPerformed (final ActionEvent ae) {
                Loader.exit();
            }});
        menuItem.setMnemonic(KeyEvent.VK_X);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, MENU_MASK));
        menu.add(menuItem);

        return menu;
    }

    /**
     * Creates a Menu for launching various different window types.
     *
     * @return the newly created menu
     */
    private JMenu createAScopeMenu ()
    {
        JMenu menu = new JMenu("AScope");
        //windowMenu.setIcon(new ImageIcon(Loader.getResource("resources/icons/ascope.png")));
        menu.setMnemonic(KeyEvent.VK_A);
        return menu;
    }

    private void updateAscopeMenu ()
    {
        while (this.ascopeMenu.getItemCount() > 0) this.ascopeMenu.remove(0);

        JMenuItem menuItem;
        for (final String type : sm.getTypes()) {
            ChillMomentFieldScale scale = sm.getScale(type);
            if (scale == null) continue; //data type has been removed
            menuItem = new JMenuItem(type/*,
                    new ImageIcon(Loader.getResource("resources/icons/ascope.png"))*/);
            int accelerator = scale.keyboardAccelerator;
            if (accelerator > 0) {
                menuItem.setMnemonic(accelerator);
                menuItem.setAccelerator(KeyStroke.getKeyStroke(accelerator, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
            }
            menuItem.addActionListener(new AbstractAction(type) {
                public void actionPerformed (final ActionEvent ae) {
                    wm.createAScopeWindow(type);
                }});
            this.ascopeMenu.add(menuItem);
        }

        this.ascopeMenu.addSeparator();

        menuItem = new JMenuItem("Save Images");
        menuItem.setMnemonic(KeyEvent.VK_I);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        menuItem.addActionListener(new AbstractAction() {
            public void actionPerformed (final ActionEvent ae) {
                wm.saveAScopeImages(vc.getControlMessage());
            }});
        this.ascopeMenu.add(menuItem);
    }

    /**
     * Creates a Menu for launching various different window types.
     *
     * @return the newly created menu
     */
    private JMenu createNumDumpMenu ()
    {
        JMenu menu = new JMenu("Numerical");
        menu.setMnemonic(KeyEvent.VK_N);
        return menu;
    }

    private void updateNumDumpMenu ()
    {
        while (this.numdumpMenu.getItemCount() > 0) this.numdumpMenu.remove(0);

        JMenuItem menuItem;
        for (final String type : sm.getTypes()) {
            ChillMomentFieldScale scale = sm.getScale(type);
            if (scale == null) continue; //data type has been removed
            menuItem = new JMenuItem(type);
            int accelerator = scale.keyboardAccelerator;
            if (accelerator > 0) {
                menuItem.setMnemonic(accelerator);
                menuItem.setAccelerator(KeyStroke.getKeyStroke(accelerator, InputEvent.CTRL_MASK | InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
            }
            menuItem.addActionListener(new AbstractAction() {
                public void actionPerformed (final ActionEvent ae) {
                    wm.createNumDumpWindow(type);
                }});
            numdumpMenu.add(menuItem);
        }
    }

    /**
     * Creates a Menu for launching various different window types.
     *
     * @return the newly created menu
     */
    private JMenu createPlotMenu ()
    {
        JMenu menu = new JMenu("Plot");
        menu.setMnemonic(KeyEvent.VK_P);
        //windowMenu.setIcon(new ImageIcon(Loader.getResource("resources/icons/sweepPPI.png")));
        return menu;
    }

    private void updatePlotMenu ()
    {
        while (this.plotMenu.getItemCount() > 0) this.plotMenu.remove(0);

        JMenuItem menuItem;
        for (final String type : sm.getTypes()) {
            ChillMomentFieldScale scale = sm.getScale(type);
            if (scale == null) continue; //data type has been removed
            menuItem = new JMenuItem(type/*,
                    new ImageIcon(Loader.getResource("resources/icons/sweepPPI.png"))*/);
            int accelerator = scale.keyboardAccelerator;
            if (accelerator > 0) {
                menuItem.setMnemonic(accelerator);
                menuItem.setAccelerator(KeyStroke.getKeyStroke(accelerator, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
            }
            menuItem.addActionListener(new AbstractAction() {
                public void actionPerformed (final ActionEvent ae) {
                    wm.createPlotWindow(type);
                }});
            this.plotMenu.add(menuItem);
        }

        this.plotMenu.addSeparator();

        menuItem = new JMenuItem("Save Images");
        menuItem.setMnemonic(KeyEvent.VK_I);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        menuItem.addActionListener(new AbstractAction() {
            public void actionPerformed (final ActionEvent ae) {
                wm.savePlotImages(vc.getControlMessage());
            }});
        this.plotMenu.add(menuItem);
    }



    
  
        
    public void updateWindowMenus ()
    {
        this.updatePlotMenu();
        this.updateAscopeMenu();
        this.updateNumDumpMenu();
    }

    /**
     * Creates a Menu for changing the colors.
     *
     * @return the newly created menu
     */
    private JMenu createColorMenu (final List<String> colorNames)
    {
        JMenu menu = new JMenu("Colors");
        menu.setMnemonic(KeyEvent.VK_C);
        ButtonGroup colorGroup = new ButtonGroup();
        final String activeName = edu.colostate.vchill.color.Config.getInstance().getColorFileName();

        { //custom colors
            //create (selected) custom option first so we have a default selection
            JRadioButtonMenuItem customItem = new JRadioButtonMenuItem("Custom...");
            colorGroup.add(customItem);
            customItem.setSelected(true);

            //then create standard options
            for (final String file : colorNames) {
                JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(file.substring(0, file.lastIndexOf('.')));
                colorGroup.add(menuItem);
                menuItem.setSelected(file.equals(activeName)); //will unselect all others if true
                menuItem.setMnemonic(file.charAt(0));
                menuItem.addActionListener(new AbstractAction(file) {
                    public void actionPerformed (final ActionEvent ae) {
                        vc.loadColors(file);
                        vc.rePlot();
                    }});
                menu.add(menuItem);
            }

            //add custom option last so it is on bottom of the menu
            customItem.setMnemonic(KeyEvent.VK_C);
            customItem.addActionListener(new AbstractAction() {
                private String lastDir = null;
                public void actionPerformed (final ActionEvent ae) {
                    JFileChooser chooser = new JFileChooser(this.lastDir);
                    int returnVal = chooser.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        try {
                            vc.loadColors(file.getCanonicalPath());
                            vc.rePlot();
                            this.lastDir = file.getParent();
                        } catch (IOException ioe) {
                            System.err.println(ioe);
                        }
                    }
                }});
            menu.add(customItem);

            JMenuItem menuItem = new JMenuItem(new AbstractAction("Editor...") {
                public void actionPerformed (final ActionEvent ae) {
                    editor.setVisible(true);
                }});
            menuItem.setMnemonic(KeyEvent.VK_E);
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, MENU_MASK));
            menu.add(menuItem);
        }

        menu.addSeparator();

        { //additional options - change the way colors are displayed
            //interpolate colortable to ~256 entries
            JCheckBoxMenuItem interpolateItem = new JCheckBoxMenuItem(new AbstractAction("Interpolate colors") {
                public void actionPerformed (final ActionEvent ae) {
                    ccc.toggleInterpolateColorsEnabled();
                    vc.loadColors(ccc.getColorFileName());
                    vc.rePlot();
                }});
            interpolateItem.setSelected(ccc.isInterpolateColorsEnabled());
            interpolateItem.setMnemonic(KeyEvent.VK_I);
            interpolateItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, MENU_MASK));
            menu.add(interpolateItem);

            JCheckBoxMenuItem outlineItem = new JCheckBoxMenuItem(new AbstractAction("Outline colors") {
                public void actionPerformed (final ActionEvent ae) {
                    ccc.toggleOutlineColorsEnabled();
                    vc.loadColors(ccc.getColorFileName());
                    vc.rePlot();
                }});
            outlineItem.setSelected(ccc.isOutlineColorsEnabled());
            outlineItem.setMnemonic(KeyEvent.VK_O);
            outlineItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MENU_MASK));
            menu.add(outlineItem);
        }

        return menu;
    }

    /**
     * Creates a Menu for selecting a map.
     *
     * @return the newly created menu
     */
    private JMenu createMapMenu (final List<String> mapNames)
    {
        JMenu menu = new JMenu("Maps");
        menu.setMnemonic(KeyEvent.VK_M);
        JCheckBoxMenuItem menuItem;

        mapItems.put(NONE, menuItem = new JCheckBoxMenuItem(NONE));
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.addActionListener(new AbstractAction() {
            public void actionPerformed (final ActionEvent ae) {
                for (JCheckBoxMenuItem item : mapItems.values()) item.setSelected(false); //unselect all
                mapItems.get(NONE).setSelected(true); //reselect "none"
                gcc.clearMapFileNames();
                vc.loadMaps();
                wm.replotOverlay();
				wm.repaintPlotWindows();
            }});
        menu.add(menuItem);

        for (final String file : mapNames) { //1st N entries
            mapItems.put(file, menuItem = new JCheckBoxMenuItem(file.substring(0, file.lastIndexOf('.'))));
            menuItem.setMnemonic(file.charAt(0));
            menuItem.addActionListener(new AbstractAction(file) {
                public void actionPerformed (final ActionEvent ae) {
                    gcc.toggleMapFileName(file);
                    mapItems.get(NONE).setSelected(gcc.getMapFileNames().size() == 0); //deselect "none"?
                    vc.loadMaps();
					wm.repaintPlotWindows();
                }});
            menuItem.setSelected(gcc.isMapActive(file)); //initial state
            menu.add(menuItem);
        }

        mapItems.put(CUSTOM, menuItem = new JCheckBoxMenuItem(CUSTOM));
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.addActionListener(new AbstractAction() {
            private String lastDir;
            public void actionPerformed (final ActionEvent ae) {
                JFileChooser chooser = new JFileChooser(this.lastDir);
                chooser.setMultiSelectionEnabled(true); //allow multiple selection
                int returnVal = chooser.showOpenDialog(null); //no parent
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] files = chooser.getSelectedFiles();
                    try {
                        for (int i = 0; i < files.length; ++i) {
                            String file = files[i].getCanonicalPath();
                            gcc.toggleMapFileName(file);
                            this.lastDir = files[i].getParent();
                        }
                        vc.loadMaps();
						wm.repaintPlotWindows();
                    } catch (IOException ioe) {
                        System.err.println(ioe);
                    }
                }
                updateBoxes();
            }});
        menu.add(menuItem);

        //initial state
        updateBoxes();

        return menu;
    }

    
    /**
     * Creates a Menu for selecting a map from MapServer.
     * Rausch
     * @return the newly created menu
     */
    private JMenu createMapServerMenu ()//final List<String> mapNames)
    {
        JMenu menu = new JMenu("MapServer");
        menu.setMnemonic(KeyEvent.VK_S);
        JCheckBoxMenuItem menuItem;

        
        mapItems.put("MapServer", menuItem = new JCheckBoxMenuItem("MapServer"));
        
        /*
        menuItem.addActionListener(new AbstractAction() {
            public void actionPerformed (final ActionEvent ae) {
                for (JCheckBoxMenuItem item : mapItems.values()) item.setSelected(false); //unselect all
                
            }});
        */
        
        menuItem.addActionListener(new AbstractAction() {
            private String lastDir;
            public void actionPerformed (final ActionEvent ae) {

            	MapServerConfigObject.createAndShowPreferencesFrame();              
                
            }});            
        menu.add(menuItem);


        //initial state
        updateBoxes();

        return menu;
    }
    
    /*
     * 		This function let's the user select options for displaying the maps
     * 		from MapServer.
     * 		Rausch
     */
    

/*    
    private void actionPerformed(ActionEvent e)
    {
    	MapServerConfigObject.createAndShowPreferencesFrame();
    }
*/    

    
 
	
    private void updateBoxes ()
    {
        mapItems.get(NONE).setSelected(gcc.getMapFileNames().size() == 0); //deselect "none"?
        int selected = 0;
        for (JCheckBoxMenuItem item : mapItems.values()) if (item.isSelected()) ++selected;
        if (mapItems.get(NONE).isSelected()) --selected; //don't count none
        if (mapItems.get(CUSTOM).isSelected()) --selected; //don't count custom
        mapItems.get(CUSTOM).setSelected(gcc.getMapFileNames().size() > selected);
    }

    /**
     * Creates a Menu that will give the user help with common problem.
     *
     * @return the newly created menu
     */
    private JMenu createHelpMenu ()
    {
        JMenu menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        JMenuItem menuItem;

        menuItem = new JMenuItem(new AbstractAction("Using VCHILL") {
            public void actionPerformed (final ActionEvent ae) {
                DialogUtil.showHelpDialog("Help: Using Java VCHILL", Loader.getResource("resources/help/using.html"));
            }});
        menuItem.setMnemonic(KeyEvent.VK_U);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        //menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HELP, 0));
        menu.add(menuItem);

        menuItem = new JMenuItem(new AbstractAction("Mouse Commands") {
            public void actionPerformed (final ActionEvent ae) {
                DialogUtil.showHelpDialog("Help: Mouse Commands", Loader.getResource("resources/help/mouse.html"));
            }});
        menuItem.setMnemonic(KeyEvent.VK_M);
        menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("About Java VCHILL");
        menuItem.setMnemonic(KeyEvent.VK_A);
        menuItem.addActionListener(new AbstractAction("About Java VCHILL") {
            public void actionPerformed (final ActionEvent ae) {
                DialogUtil.showHelpDialog("About Java VCHILL",
                    "<html>" + 
                    "<table><tr><td><b>"+
                    "<font size=\"+4\">Java VCHILL</font><br>" +
                    "<font size=\"+1\">" + Version.string + "</font><br>"+
                    "build " + Version.buildDate + "</b><br>" +
                    "\u00a9 2002-2010<br>" + //(c)
                    "</td><td>" + 
                    "<img align=\"right\" alt=\"Colorado State University\" src=\"http://chill.colostate.edu/images/glde_fr_med.png\">" +
                    "</td></tr></table>" +
                    "<hr align=\"left\" width=\"88%\">" +
                    "Lead programmer:<br>" +
                    //"Jochen Deyke (<a href=\"mailto:jdeyke@chill.colostate.edu\">jdeyke@chill.colostate.edu</a>)<br>" +
                    "Jochen Deyke (jdeyke@chill.colostate.edu)<br>" +
                    "<br>" +
                    "Graphics programmer:<br>" +
                    "Justin Carlson (justinc4@hotmail.com)<br>" +
                    "<br>" +
                    "Radar control programmer:<br>" +
                    "Brian Eriksson (bceriksson@students.wisc.edu)<br>" +
                    "<br>" +
                    "Dialog/menu/tree programmer:<br>" +
                    "Alexander Deyke (adeyke@gmx.net)<br>" +
                    "<hr align=\"left\" width=\"88%\">" +
                    "VCHILL uses the following third-party libraries:<br>" +
                    "Gif89Encoder version 0.90 beta<br>" +
                    "JGoodies Looks version 2.1.4<br>" +
                    "Unidata NetCDF 2.2.22</html>");
            }});
        menu.add(menuItem);

        menuItem = new JMenuItem("About Gif89Encoder");
        menuItem.setMnemonic(KeyEvent.VK_G);
        menuItem.addActionListener(new AbstractAction("About Gif89Encoder") {
            public void actionPerformed (final ActionEvent ae) {
                DialogUtil.showHelpDialog("About Gif89Encoder",
                    "Legal\n" +
                    "-----\n" +
                    "\n" +
                    "Since Gif89Encoder includes significant sections of code from Jef Poskanzer's\n" +
                    "GifEncoder.java, I'm including its notice in this distribution as requested (appended\n" +
                    "below).\n" +
                    "\n" +
                    "As for my part of the code, I hereby release it, on a strictly \"as is\" basis,\n" +
                    "to the public domain.\n" +
                    "\n" +
                    "J. M. G. Elliott\n" +
                    "15-Jul-2000\n" +
                    "\n" +
                    "--------------------- from Jef Poskanzer's GifEncoder.java ---------------------\n" +
                    "\n" +
                    "// GifEncoder - write out an image as a GIF\n" +
                    "//\n" +
                    "// Transparency handling and variable bit size courtesy of Jack Palevich.\n" +
                    "//\n" +
                    "// Copyright (C) 1996 by Jef Poskanzer <jef@acme.com>.  All rights reserved.\n" +
                    "//\n" +
                    "// Redistribution and use in source and binary forms, with or without\n" +
                    "// modification, are permitted provided that the following conditions\n" +
                    "// are met:\n" +
                    "// 1. Redistributions of source code must retain the above copyright\n" +
                    "//    notice, this list of conditions and the following disclaimer.\n" +
                    "// 2. Redistributions in binary form must reproduce the above copyright\n" +
                    "//    notice, this list of conditions and the following disclaimer in the\n" +
                    "//    documentation and/or other materials provided with the distribution.\n" +
                    "//\n" +
                    "// THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND\n" +
                    "// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE\n" +
                    "// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE\n" +
                    "// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE\n" +
                    "// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL\n" +
                    "// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS\n" +
                    "// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)\n" +
                    "// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT\n" +
                    "// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY\n" +
                    "// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF\n" +
                    "// SUCH DAMAGE.\n" +
                    "//\n" +
                    "// Visit the ACME Labs Java page for up-to-date versions of this and other\n" +
                    "// fine Java utilities: http://www.acme.com/java/");
            }});
        menu.add(menuItem);

        menuItem = new JMenuItem("About JGoodies' Looks");
        menuItem.setMnemonic(KeyEvent.VK_L);
        menuItem.addActionListener(new AbstractAction("About JGoodies' Looks") {
            public void actionPerformed (final ActionEvent ae) {
                DialogUtil.showHelpDialog("About JGoodies' Looks",
                "           The BSD License for the JGoodies Looks\n" +
                "           ======================================\n" +
                "\n" +
                "Copyright (c) 2001-2007 JGoodies Karsten Lentzsch. All rights reserved.\n" +
                "\n" +
                "Redistribution and use in source and binary forms, with or without\n" +
                "modification, are permitted provided that the following conditions are met:\n" +
                "\n" +
                " o Redistributions of source code must retain the above copyright notice,\n" +
                "   this list of conditions and the following disclaimer.\n" +
                "\n" +
                " o Redistributions in binary form must reproduce the above copyright notice,\n" +
                "   this list of conditions and the following disclaimer in the documentation\n" +
                "   and/or other materials provided with the distribution.\n" +
                "\n" +
                " o Neither the name of JGoodies Karsten Lentzsch nor the names of\n" +
                "   its contributors may be used to endorse or promote products derived\n" +
                "   from this software without specific prior written permission.\n" +
                "\n" +
                "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"\n" +
                "AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,\n" +
                "THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR\n" +
                "PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR\n" +
                "CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,\n" +
                "EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,\n" +
                "PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;\n" +
                "OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,\n" +
                "WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR\n" +
                "OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,\n" +
                "EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.");
            }});
        menu.add(menuItem);

        menuItem = new JMenuItem("About Unidata NetCDF");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.setDisplayedMnemonicIndex(14);
        menuItem.addActionListener(new AbstractAction("About Unidata NetCDF") {
            public void actionPerformed (final ActionEvent ae) {
                DialogUtil.showHelpDialog("About Unidata NetCDF",
                "http://www.gnu.org/copyleft/lesser.txt");
            }});
        menu.add(menuItem);

        return menu;
    }
}

