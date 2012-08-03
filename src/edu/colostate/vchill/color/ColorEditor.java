package edu.colostate.vchill.color;

import edu.colostate.vchill.ViewControl;
import edu.colostate.vchill.ChillDefines.ColorType;
import edu.colostate.vchill.gui.GUIUtil;
import edu.colostate.vchill.gui.WindowManager;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

/**
 * GUI for the creation and modification of color maps.
 *
 * @author Alexander Deyke
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class ColorEditor extends JFrame
{
    /**
   * 
   */
  private static final long serialVersionUID = 5342794593875360747L;
    private static final Config config = Config.getInstance();
    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final int DEFAULT_NUM_COLORS = 16;
    private static final int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    private JColorChooser colorChooser;
    private JPanel colorListPanel;
    private JComboBox comboBox;
    private XMLControl control;
    private File currentFile;
    private boolean modified = false;
    private int numColors = 0;
    private ButtonGroup group;
    private int selectedColor;
    private final boolean isStandalone;

    public ColorEditor (final XMLControl control)
    {
        this(control, false);
    }

    public ColorEditor (final XMLControl control, final boolean isStandalone)
    {
        super("Java VCHILL Color Editor");
        this.control = control;
        this.isStandalone = isStandalone;

        group = new ButtonGroup();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter () {
            @Override public void windowClosing (final WindowEvent we) {
                quit();
            }});

        JPanel pane = new JPanel();

        colorChooser = new JColorChooser(DEFAULT_COLOR);
        colorChooser.getSelectionModel().addChangeListener(new ChangeListener () {
            public void stateChanged (final ChangeEvent ce) {
                if (!colorListPanel.getComponent(selectedColor).getBackground().equals(colorChooser.getColor())) {
                    colorListPanel.getComponent(selectedColor).setBackground(colorChooser.getColor());
                    writeColors((ColorType)comboBox.getSelectedItem());
                    modified = true;
                }
            }});
        pane.add(colorChooser);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        comboBox = new JComboBox(ColorType.values());
        comboBox.addItemListener(new ItemListener() {
            public void itemStateChanged (final ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    readColors((ColorType)ie.getItem());
                } else {
                    writeColors((ColorType)ie.getItem());
                }
            }});
        rightPanel.add(comboBox);

        colorListPanel = new JPanel();
        colorListPanel.setLayout(new GridLayout(numColors, 1));
        //colorListPanel.setLayout(new BoxLayout(colorListPanel, BoxLayout.Y_AXIS));

        rightPanel.add(colorListPanel);
        pane.add(rightPanel);

        setContentPane(pane);
        setJMenuBar(createMenuBar());
        
        newColors();
        
        pack();
    }

    private void addColor ()
    {
        final int colorIndex = numColors;
        ++numColors;
        JRadioButton radioButton = new JRadioButton();
        radioButton.setOpaque(true);
        radioButton.setBackground(DEFAULT_COLOR);
        radioButton.addActionListener(new ActionListener () {
            public void actionPerformed (final ActionEvent e) {
                selectedColor = colorIndex;
                colorChooser.setColor(colorListPanel.getComponent(selectedColor).getBackground());
            }});
        group.add(radioButton);
        colorListPanel.add(radioButton);
        colorListPanel.setLayout(new GridLayout(numColors, 1));
        pack();
    }

    private void removeColor ()
    {
        --numColors;
        group.remove((JRadioButton)(colorListPanel.getComponent(numColors)));
        colorListPanel.remove(numColors);
        colorListPanel.setLayout(new GridLayout(numColors, 1));
        pack();
    }

    private JMenu createSystemMenu ()
    {
        JMenu systemMenu = new JMenu("Open System");
        systemMenu.setMnemonic(KeyEvent.VK_Y);
        JMenuItem menuItem;

        List<String> colorNames = XMLControl.getListOfFiles();
        for (final String file : colorNames) {
            menuItem = new JMenuItem(file.substring(0, file.lastIndexOf('.')));
            menuItem.setMnemonic(file.charAt(0));
            menuItem.addActionListener(new AbstractAction(file) {
                /**
               * 
               */
              private static final long serialVersionUID = -4512496507068939419L;

                public void actionPerformed (final ActionEvent ae) {
                    openSystem(file);
                }});
            systemMenu.add(menuItem);
        }
        return systemMenu;
    }

    private void openSystem (final String file)
    {
        if (!confirmDialog()) return;
        currentFile = null;
        control.load(file);
        readColors((ColorType)comboBox.getSelectedItem());
        modified = false;
    }

    private JMenuBar createMenuBar ()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem menuItem;

        menuItem = new JMenuItem("New");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = 8859818260160890726L;

            public void actionPerformed (final ActionEvent ae) {
                newColors();
            }});
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, MENU_MASK));
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Open");
        menuItem.setMnemonic(KeyEvent.VK_O);
        menuItem.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = -6961569591930409964L;

            public void actionPerformed (final ActionEvent ae) {
                openColors();
            }});
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MENU_MASK));
        fileMenu.add(menuItem);

        menuItem = createSystemMenu();
        fileMenu.add(menuItem);

        fileMenu.add(new JSeparator());

        menuItem = new JMenuItem("Save");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = -4295828485516762399L;

            public void actionPerformed (final ActionEvent ae) {
                saveColors();
            }});
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MENU_MASK));
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Save As...");
        menuItem.setMnemonic(KeyEvent.VK_A);
        menuItem.setDisplayedMnemonicIndex(5);
        menuItem.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = 4583531422459778410L;

            public void actionPerformed (final ActionEvent ae) {
                saveColorsAs();
            }});
        fileMenu.add(menuItem);

        fileMenu.add(new JSeparator());

        menuItem = new JMenuItem("Exit");
        menuItem.setMnemonic(KeyEvent.VK_X);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, MENU_MASK));
        menuItem.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = 3445866735609309851L;

            public void actionPerformed (final ActionEvent ae) {
                quit();
            }});
        fileMenu.add(menuItem);

        menuBar.add(fileMenu);
        return menuBar;
    }

    private void quit ()
    {
        if (!confirmDialog()) return;
        this.setVisible(false);
        if (this.isStandalone) System.exit(0);
    }

    private boolean confirmDialog ()
    {
        while (modified) {
            String message = "Save changes" + ((currentFile != null) ? " to " + currentFile : "") + " first?";
            int answer = JOptionPane.showConfirmDialog(this, message, "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            switch (answer) {
                case JOptionPane.YES_OPTION:
                    saveColors();
                    continue;
                case JOptionPane.CANCEL_OPTION:
                case JOptionPane.CLOSED_OPTION:
                    return false;
                case JOptionPane.NO_OPTION:
                    return true;
            }
        }
        return true;
    }

    private void writeColors (final ColorType type)
    {
        List<Color> colors = new ArrayList<Color>(numColors);
        for (int i = 0; i < numColors; ++i) {
            colors.add(colorListPanel.getComponent(numColors - i - 1).getBackground());
        }
        control.setType(type, colors);
        if (!this.isStandalone) {
            ViewControl vc = ViewControl.getInstance();
            vc.getColorControl().setType(type, colors);
            WindowManager.getInstance().changeWindowColors();
        }
    }

    private void readColors (final ColorType type)
    {
        List<Color> colors = control.getType(type);
        if (colors == null) {
            colors = new ArrayList<Color>(numColors);
            for (int i = 0; i < numColors; ++i) colors.add(DEFAULT_COLOR);
        }
        while (colors.size() > numColors) addColor();
        while (colors.size() < numColors) removeColor();

        for (int i = 0; i < numColors; ++i) {
            colorListPanel.getComponent(i).setBackground(colors.get(numColors - i - 1));
        }

        if (selectedColor >= numColors) selectedColor = 0;
        colorChooser.setColor(colorListPanel.getComponent(selectedColor).getBackground());
        if (!this.isStandalone) {
            ViewControl vc = ViewControl.getInstance();
            vc.getColorControl().setType(type, colors);
            WindowManager.getInstance().changeWindowColors();
        }
    }

    private void newColors ()
    {
        if (!confirmDialog()) return;
        while (DEFAULT_NUM_COLORS > numColors) addColor();
        while (DEFAULT_NUM_COLORS < numColors) removeColor();
        if (selectedColor >= numColors) {
            selectedColor = 0;
            colorChooser.setColor(colorListPanel.getComponent(0).getBackground());
        }
        for (ColorType type : ColorType.values()) {
            List<Color> colors = new ArrayList<Color>(numColors);
            for (int i = 0; i < numColors; ++i) colors.add(DEFAULT_COLOR);
            control.setType(type, colors);
        }
        currentFile = null;
        readColors((ColorType)comboBox.getSelectedItem());
        modified = false;
    }

    private void openColors ()
    {
        if (!confirmDialog()) return;
        JFileChooser chooser = new JFileChooser(currentFile);
        chooser.setFileFilter(new XMLFileFilter());
        chooser.setAcceptAllFileFilterUsed(false);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            control.load(currentFile.getAbsolutePath());
            readColors((ColorType)comboBox.getSelectedItem());
            modified = false;
        }
    }

    /**
     * If we have a current file, save to it; otherwise save as
     */
    private void saveColors ()
    {
        if (currentFile == null) {
            saveColorsAs();
        } else {
            doSave();
        }
    } 

    /**
     * Browse for a file to save to; then save to it
     */
    private void saveColorsAs ()
    {
        JFileChooser chooser = new JFileChooser(currentFile);
        chooser.setFileFilter(new XMLFileFilter());
        chooser.setAcceptAllFileFilterUsed(false);
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            String path = currentFile.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".xml")) {
                currentFile = new File(path + ".xml");
            }
            doSave();
        }
    }

    /**
     * Do the actual saving
     */
    private void doSave ()
    {
        writeColors((ColorType)comboBox.getSelectedItem());
        control.save(currentFile.getAbsolutePath());
        modified = false;
        if (!this.isStandalone) {
            ViewControl vc = ViewControl.getInstance();
            vc.loadColors(currentFile.getAbsolutePath());
            vc.rePlot();
        }
    }

    public static void main (final String[] args)
    {
        GUIUtil.setLnF();
        ColorEditor editor = new ColorEditor(new XMLControl(false), true); //standalone mode
        editor.setVisible(true);
    }

	/** A file filter for accepting XML files. */
    protected static class XMLFileFilter extends FileFilter
    {
        public boolean accept (final File file)
        {
            return file.isDirectory() || file.getName().toLowerCase().endsWith(".xml");
        }

        public String getDescription ()
        {
            return "XML Files";
        }
    }
}
