package edu.colostate.vchill.gui;

import edu.colostate.vchill.ChillDefines.Mode;
import edu.colostate.vchill.Config;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.ViewControl;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * The ViewRemotePanel is the panel for the "Basic" tab on the bottom
 * left of the program. It is a panel of actions that the user is likely
 * to do browsing actions with. It also contains the actions for setting
 * the ray step, and finally for selecting a mode to do these actions in.
 *
 * Also contained are the interval setting text fields, the max range and
 * grid interval setting fields.  Finally the ability to set if popup
 * window actions will affect all the windows, or just the one being
 * altered.
 *
 * @author  Justin Carlson
 * @author  Jochen Deyke 
 * @author  jpont
 * @created June 1, 2003
 * @version 2010-08-30
 */
public class ViewRemotePanel extends JPanel
{
    /**
   * 
   */
  private static final long serialVersionUID = -4935730876904901145L;
    private final static ViewRemotePanel vrp = new ViewRemotePanel();
    private final ViewActions actions = new ViewActions();
    private final Config config = Config.getInstance();
    private final ViewControl vc = ViewControl.getInstance();
    private final ScaleManager sm = ScaleManager.getInstance();
    private final WindowManager wm = WindowManager.getInstance();

    private final JTextField rayField = new JTextField(String.valueOf(config.getRayStep()), 2);
    private final JTextField minField = new JTextField(5);
    private final JTextField maxField = new JTextField(5);
    private final JTextField gridIntervalField = new JTextField(String.valueOf(config.getGridSpacing()), 5);
    private final JTextField plotRange = new JTextField(String.format("%.3g", config.getPlotRange()), 5);
    private final JTextField RHIvField = new JTextField(String.format("%.3g", config.getRHIHVFactor()), 5);
    private final JTextField RHIhField = new JTextField(String.format("%.3g", config.getMaxPlotHeight()), 5);

    private final JComboBox typesBox = new JComboBox(sm.getTypes().toArray());

    /**
     * This basic constructor will call the helper methods and setup the
     * size and layout of the inner components.  Private default constructor
     * prevents instantiation.
     */
    private ViewRemotePanel ()
    {
        typesBox.setAction(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = -6270176955419819841L;

            public void actionPerformed (final ActionEvent ae) {
                ChillMomentFieldScale scale = sm.getScale((String)typesBox.getSelectedItem());
                if (scale == null) return;
                minField.setText(String.format("%.3g", scale.getMin()));
                maxField.setText(String.format("%.3g", scale.getMax()));
            }});
        if (sm.getTypes().size() > 0) {
            typesBox.setSelectedIndex(0);
        }
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(createSweepButtons());
        add(createRayButtons());
        add(createModeSelectionBoxes());
        add(createIntervalInput());
        add(createRHIOptions());
        add(createOptionPanel());
        add(createGlobalOptions());
        add(createBookmarkOptions());
        sm.addObserver(new Observer () {
            public void update (final Observable o, final Object arg) {
                typesBox.removeAllItems();
                for (String type : sm.getTypes()) typesBox.addItem(type);
            }});
    }

    public static ViewRemotePanel getInstance () { return vrp; }

    /**
     * Creates buttons for the actions first, previous, next and last.
     *
     * @return the panel containing the four buttons
     */
    private JPanel createSweepButtons ()
    {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        JButton button;

        button = new JButton(actions.doFirstAction);
        button.setToolTipText("Go to the first sweep in this volume");
        inputPanel.add(button);

        button = new JButton(actions.doPrevAction);
        button.setToolTipText("Go to the previous sweep in this volume");
        inputPanel.add(button);

        button = new JButton(actions.doNextAction);
        button.setToolTipText("Go to the next sweep in this volume");
        inputPanel.add(button);

        button = new JButton(actions.doLastAction);
        button.setToolTipText("Go to the last sweep in this volume");
        inputPanel.add(button);

        return inputPanel;
    }

    /**
     * Creates the stop action, the input for ray steps, and the the labels
     * to go along with the input box.
     *
     * @return A Panel with Text fields for setting ray information.
     */
    private JPanel createRayButtons ()
    {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        JButton button;

        button = new JButton(actions.doFirstRayAction);
        button.setToolTipText("Go to the first ray in this sweep");
        inputPanel.add(button);

        button = new JButton(actions.doPrevRayAction);
        button.setToolTipText("Go to the previous ray in this sweep");
        inputPanel.add(button);

        JLabel information = new JLabel(" Step: ");
        inputPanel.add(information);
        MyAction action = new  MyAction() { protected void update () {
	    String rayText = rayField.getText();
	    if( Double.parseDouble(rayText) < 1.0 )
	    {
		    /* the interval must be at least 1 */
		    rayField.setText("1");
	    }
            config.setRayStep(rayField.getText()); }};
        rayField.addActionListener(action);
        rayField.addFocusListener(action);
        rayField.setToolTipText("Step size for the next and previous ray operations");
        inputPanel.add(rayField);

        button = new JButton(actions.doNextRayAction);
        button.setToolTipText("Go to the next ray in this sweep");
        inputPanel.add(button);

        button = new JButton(actions.doLastRayAction);
        button.setToolTipText("Go to the last ray in this sweep");
        inputPanel.add(button);

        return inputPanel;
    }

    private JPanel createRHIOptions ()
    {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        MyAction action;

        inputPanel.add(new JLabel("RHI Vstretch:"));
        action = new MyAction() { protected void update () {
	    String RHIvText = RHIvField.getText();
	    if( RHIvText.indexOf("-") != -1 )
	    {
		    /* don't allow negative numbers */
		    RHIvField.setText("0.0");
	    }
            config.setRHIHVFactor(RHIvField.getText());
            wm.replotOverlay();
        }};
        RHIvField.addActionListener(action);
        RHIvField.addFocusListener(action);
        RHIvField.setToolTipText("Vertical to horizontal stretch ratio");
        inputPanel.add(RHIvField);

        inputPanel.add(new JLabel(" RHI height:"));
        action = new MyAction() { protected void update () {
	    String RHIhText = RHIhField.getText();
	    if( RHIhText.indexOf("-") != -1 )
	    {
		    /* don't allow negative numbers */
		    RHIhField.setText("0.0");
	    }
            config.setMaxPlotHeight(RHIhField.getText());
            wm.replotOverlay();
        }};
        RHIhField.addActionListener(action);
        RHIhField.addFocusListener(action);
        RHIhField.setToolTipText("Limit RHI height to this distance (km)");
        inputPanel.add(RHIhField);

        return inputPanel;
    }

    /**
     * Creates a panel containing the Interval setting fields and the
     * pulldown menu for selecting what data type is affected.
     *
     * @return A Panel with Interval setting options.
     */
    private JPanel createIntervalInput ()
    {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        MyAction action = new MyAction() { protected void update () {
            setPlotInterval((String)typesBox.getSelectedItem(), maxField.getText(), minField.getText()); }};
        typesBox.setToolTipText("Adjust color scale for this data type");
        inputPanel.add(typesBox);

        inputPanel.add(new JLabel(" Min:"));
        minField.addActionListener(action);
        minField.addFocusListener(action);
        minField.setToolTipText("Minimum data value for color scale");
        inputPanel.add(minField);

        inputPanel.add(new JLabel(" Max:"));
        maxField.addActionListener(action);
        maxField.addFocusListener(action);
        maxField.setToolTipText("Maximum data value for color scale");
        inputPanel.add(maxField);

        return inputPanel;
    }

    /**
     * A Panel for setting the grid interval (how often the 
     * lines are drawn in km) and the max plot range (how far
     * out to draw the lines and data).
     *
     * @return The freshly created option panel
     */
    private JPanel createOptionPanel ()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        MyAction action;

        panel.add(new JLabel("Spacing (km):"));
        action = new MyAction() { protected void update () {
	    String gridIntervalText = gridIntervalField.getText();
	    if( Double.parseDouble(gridIntervalText) < 1.0 )
	    {
		    /* the interval must be at least 1 */
		    gridIntervalField.setText("1");
	    }
            config.setGridSpacing(gridIntervalField.getText());
            wm.replotOverlay();
        }};
        gridIntervalField.addActionListener(action);
        gridIntervalField.addFocusListener(action);
        gridIntervalField.setToolTipText("Draw range rings/grid every this many km");
        panel.add(gridIntervalField);

        panel.add(new JLabel(" Range (km):"));
        action = new MyAction() { protected void update () {
	    String plotRangeText = plotRange.getText();
	    if( Double.parseDouble(plotRangeText) <= 0.0 )
	    {
		    /* the range must be at least 1 */
		    plotRange.setText("1");
	    }
            config.setPlotRange(plotRange.getText());
            wm.replotOverlay();
        }};
        plotRange.addActionListener(action);
        plotRange.addFocusListener(action);
        plotRange.setToolTipText("Limit plot range to this distance (km)");
        panel.add(plotRange);

        return panel;
    }

    /**
     * This will set all the currently open windows of a given type to
     * hash their data according to these new max and min values.
     *
     * @param type The Type of data to set these new values into.
     * @param max The maximum value to be used for the data hashing.
     * @param min The minimum value to be used for the data hashing.
     */
    private void setPlotInterval (final String type, final String max, final String min)
    {
        if (type == null || max == null || min == null) return;
        if (max.length() < 1 || min.length() < 1) return;
        vc.setPlotInterval(type, max, min);
    }

    /**
     * This is just a simple private method to allow for the creation of
     * radio buttons that will be encapsulated in a JPanel.  These
     * buttons will effect which plotting method is used, by ray, by sweep
     * or by volume.
     *
     * @return A JPanel with a set of Radio buttons
     */
    private JPanel createModeSelectionBoxes ()
    {
        JPanel modePanel = new JPanel();
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.X_AXIS));
        modePanel.add(new JLabel("Mode: "));
        ButtonGroup group = new ButtonGroup();
        for (final Mode mode : Mode.values()) {
            JRadioButton button = new JRadioButton(mode.toString() + "  ");
            button.setToolTipText(mode.DESCRIPTION);
            if (mode == config.getPlottingMode()) button.setSelected(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed (ActionEvent ae) {
                    config.setPlottingMode(mode);
                }});
            group.add(button);
            modePanel.add(button);
        }
        return modePanel;
    }

    /**
     * This method will create a panel with the apply button and 
     * affect all options box.
     *
     * @return The new options panel
     */
    private JPanel createGlobalOptions ()
    {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        JButton button = new JButton(actions.doStopAction);
        button.setToolTipText("Click to stop this plot");
        inputPanel.add(button);
        inputPanel.add(new JButton(new AbstractAction("Apply / Replot") {
            /**
           * 
           */
          private static final long serialVersionUID = -9061249180042696285L;

            public void actionPerformed (ActionEvent ae) {
                config.setPlotRange(plotRange.getText());
                config.setGridSpacing(gridIntervalField.getText());
                setPlotInterval((String)typesBox.getSelectedItem(), maxField.getText(), minField.getText());
                config.setRayStep(rayField.getText());
                config.setRHIHVFactor(RHIvField.getText());
                config.setMaxPlotHeight(RHIhField.getText());
                wm.replotOverlay();
                vc.rePlot();
            }}));
        return inputPanel;
    }

    private JPanel createBookmarkOptions ()
    {
        JPanel BookmarkPanel = new JPanel();
        BookmarkPanel.setLayout(new BoxLayout(BookmarkPanel, BoxLayout.X_AXIS));
        BookmarkPanel.add(new JButton(new AbstractAction("Create Bookmark") {
            /**
           * 
           */
          private static final long serialVersionUID = 4134687274631014923L;

            public void actionPerformed (ActionEvent ae) {
                vc.createBookmark();
            }}));
        return BookmarkPanel;
    }

    /**
     * Reloads text fields based on current settings in Config
     */
    public void update () {
        plotRange.setText(String.format("%.3g", config.getPlotRange()));
        RHIhField.setText(String.format("%.3g", config.getMaxPlotHeight()));
        gridIntervalField.setText(String.valueOf(config.getGridSpacing()));
        typesBox.setSelectedItem(typesBox.getSelectedItem());
    }

    public void setSelection (final String type)
    {
        typesBox.setSelectedItem(type);
    }

    private abstract class MyAction implements ActionListener, FocusListener
    {
        public void actionPerformed (final ActionEvent ae) {
            update(); vc.rePlot();}
        public void focusLost (final FocusEvent fe) { update(); }
        public void focusGained (final FocusEvent fe) {} //do nothing
        protected abstract void update ();
    }
}
