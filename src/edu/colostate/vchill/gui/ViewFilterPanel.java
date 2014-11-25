package edu.colostate.vchill.gui;

import edu.colostate.vchill.Config;
import edu.colostate.vchill.Config.GradientType;
import edu.colostate.vchill.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Observable;
import java.util.Observer;

/**
 * The ViewFilterPanel allows access to various options for
 * plot windows without constantly right-clicking.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-03-19
 */
public class ViewFilterPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = -1728050194660302405L;
    private final static ViewFilterPanel me = new ViewFilterPanel();
    private final ViewActions actions = new ViewActions();
    private final Config config = Config.getInstance();
    private final ViewControl vc = ViewControl.getInstance();
    private final ScaleManager sm = ScaleManager.getInstance();
    private final WindowManager wm = WindowManager.getInstance();

    private final JTextField thresholdField = new JTextField(String.valueOf(config.getThresholdFilterCutoff()), 4);

    private final JComboBox threshBox = new JComboBox(sm.getTypes().toArray());
    private final JCheckBox enableThreshCheckBox = new JCheckBox(new AbstractAction("Threshold:") {
        /**
         *
         */
        private static final long serialVersionUID = -2602346584637503100L;

        public void actionPerformed(final ActionEvent ae) {
            config.setThresholdEnabled(enableThreshCheckBox.isSelected());
            vc.rePlot();
        }
    });
    private final JCheckBox absoluteValueCheckBox = new JCheckBox(new AbstractAction("Absolute") {
        /**
         *
         */
        private static final long serialVersionUID = -930118262416813946L;

        public void actionPerformed(final ActionEvent ae) {
            config.setThresholdAbsoluteValueEnabled(absoluteValueCheckBox.isSelected());
            vc.rePlot();
        }
    });
    private final JCheckBox enableThreshGreaterThanCheckBox = new JCheckBox(new AbstractAction(">") {
        /**
         *
         */
        private static final long serialVersionUID = 596592460487632904L;

        public void actionPerformed(final ActionEvent ae) {
            config.setThresholdGreaterThanEnabled(enableThreshGreaterThanCheckBox.isSelected());
            vc.rePlot();
        }
    });

    private final JCheckBox enableSmoothingCheckBox = new JCheckBox("Smoothing");
    private final JCheckBox enableNoiseFilterCheckBox = new JCheckBox("Noise Reduction");

    private final JRadioButton unfoldingOff = new JRadioButton(new AbstractAction("Off") {
        /**
         *
         */
        private static final long serialVersionUID = -1717451052853371776L;

        public void actionPerformed(final ActionEvent ae) {
            System.out.println("Disable Velocity Unfolding");
            config.setUnfoldingEnabled(false);
            vc.rePlot();
        }
    });
    private final JRadioButton unfoldingAutomatic = new JRadioButton(new AbstractAction("Automatic") {
        /**
         *
         */
        private static final long serialVersionUID = 6980738617850652817L;

        public void actionPerformed(final ActionEvent ae) {
            System.out.println("Automatic Velocity Unfolding");
            config.setUnfoldingEnabled(true);
            config.setUnfoldingAutomatic(true);
            vc.rePlot();
        }
    });
    private final JRadioButton unfoldingManual = new JRadioButton(new AbstractAction("Manual") {
        /**
         *
         */
        private static final long serialVersionUID = 8532588443991534687L;

        public void actionPerformed(final ActionEvent ae) {
            System.out.println("Manual Velocity Unfolding");
            config.setUnfoldingEnabled(true);
            config.setUnfoldingAutomatic(false);
            vc.rePlot();
        }
    });

    private final JTextField unfoldingStartRange = new JTextField(String.valueOf(config.getUnfoldStartRange()), 4);

    private final JRadioButton gradientOff = new JRadioButton(new AbstractAction("Off") {
        /**
         *
         */
        private static final long serialVersionUID = 7378725759773571609L;

        public void actionPerformed(final ActionEvent ae) {
            System.out.println("Gradient Off");
            config.setGradientType(GradientType.Off);
            vc.rePlot();
        }
    });
    private final JRadioButton gradientRange = new JRadioButton(new AbstractAction("Range") {
        /**
         *
         */
        private static final long serialVersionUID = 6959511413938691924L;

        public void actionPerformed(ActionEvent ae) {
            System.out.println("Z Range Gradient");
            config.setGradientType(GradientType.Range);
            vc.rePlot();
        }
    });
    private final JRadioButton gradientAzimuth = new JRadioButton(new AbstractAction("Azimuth") {
        /**
         *
         */
        private static final long serialVersionUID = -555090219952498927L;

        public void actionPerformed(ActionEvent ae) {
            System.out.println("Z Azimuth Gradient");
            config.setGradientType(GradientType.Azimuth);
            vc.rePlot();
        }
    });

    private final AbstractAction threshBoxAction = new AbstractAction() {
        /**
         *
         */
        private static final long serialVersionUID = 4231079353073560442L;

        public void actionPerformed(final ActionEvent ae) {
            String type = (String) threshBox.getSelectedItem();
            if (type == null) return;
            System.out.println("Setting thresh type to " + type);
            config.setThresholdType(type);
            ViewRemotePanel.getInstance().update();
            vc.rePlot();
        }
    };

    /**
     * This basic constructor will call the helper methods and setup the
     * size and layout of the inner components.  Private default constructor
     * prevents instantiation.
     */
    private ViewFilterPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(createThresholdOptions());
        add(createThresholdBox());
        add(new JSeparator()); //--------------------------------
        add(createSmoothingOptions());
        add(new JSeparator()); //--------------------------------
        add(createUnfoldingOptions());
        add(createUnfoldingBox());
        add(createUnfoldingButton());
        add(new JSeparator()); //--------------------------------
        add(createGradientOptions());
        sm.addObserver(new Observer() {
            public void update(final Observable o, final Object arg) {
                threshBox.setAction(null);
                threshBox.removeAllItems();
                for (String type : sm.getTypes()) threshBox.addItem(type);
                String thresh = config.getThresholdType();
                threshBox.setSelectedItem(thresh);
                threshBox.setAction(threshBoxAction);
            }
        });
        update();
    }

    public static ViewFilterPanel getInstance() {
        return me;
    }

    private void setThresholdCutoff(final String cutoff) {
        if (cutoff == null || cutoff.length() < 1) return;
        config.setThresholdFilterCutoff(Double.parseDouble(cutoff));
    }

    private JPanel createThresholdOptions() {
        JPanel inputPanel = new JPanel();
        MyAction action = new MyAction() {
            protected void update() {
                setThresholdCutoff(thresholdField.getText());
            }
        };

        enableThreshCheckBox.setToolTipText("Remove below given cutoff value");
        inputPanel.add(enableThreshCheckBox);

        enableThreshGreaterThanCheckBox.setToolTipText("Remove data above instead of below cutoff value");
        inputPanel.add(enableThreshGreaterThanCheckBox);

        thresholdField.addActionListener(action);
        thresholdField.addFocusListener(action);
        thresholdField.setToolTipText("Cutoff value for threshold filter");
        inputPanel.add(thresholdField);

        absoluteValueCheckBox.setToolTipText("Take absolute value of data before checking threshold");
        inputPanel.add(absoluteValueCheckBox);

        return inputPanel;
    }

    private JPanel createThresholdBox() {
        JPanel inputPanel = new JPanel();
        inputPanel.add(threshBox);
        return inputPanel;
    }

    private JPanel createSmoothingOptions() {
        JPanel inputPanel = new JPanel();

        enableSmoothingCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                config.setSmoothingEnabled(enableSmoothingCheckBox.isSelected());
                vc.rePlot();
            }
        });
        enableSmoothingCheckBox.setToolTipText("Smooth image by averaging data in a 3x3 grid");
        inputPanel.add(enableSmoothingCheckBox);

        inputPanel.add(new JPanel());

        enableNoiseFilterCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                config.setNoiseReductionEnabled(enableNoiseFilterCheckBox.isSelected());
                vc.rePlot();
            }
        });
        enableNoiseFilterCheckBox.setToolTipText("Clean image by removing isolated data points");
        inputPanel.add(enableNoiseFilterCheckBox);

        return inputPanel;
    }

    private JPanel createUnfoldingOptions() {
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Unfolding: "));

        ButtonGroup group = new ButtonGroup();
        group.add(unfoldingOff);
        group.add(unfoldingAutomatic);
        group.add(unfoldingManual);
        unfoldingOff.setToolTipText("No unfolding will be done");
        unfoldingAutomatic.setToolTipText("Unfold supported fields on a best-guess basis");
        unfoldingManual.setToolTipText("Unfold supported fields based on user-entered numbers");
        inputPanel.add(unfoldingOff);
        inputPanel.add(unfoldingAutomatic);
        inputPanel.add(unfoldingManual);

        return inputPanel;
    }

    private JPanel createUnfoldingBox() {
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Unfolding start range (km): "));
        unfoldingStartRange.setToolTipText("The range (km) where the unfolding algorithm will start");
        MyAction action = new MyAction() {
            protected void update() {
                String rangeText = unfoldingStartRange.getText();
                if (rangeText != null && rangeText.length() >= 1) {
                    double range = Double.parseDouble(rangeText);
                    if (range >= 0.0) {
                        config.setUnfoldStartRange(range);
                        if (config.isUnfoldingEnabled())
                            vc.rePlot();
                    }
                }
            }
        };
        unfoldingStartRange.addActionListener(action);

        inputPanel.add(unfoldingStartRange);
        return inputPanel;
    }

    private JPanel createUnfoldingButton() {
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JButton(new AbstractAction("Input New Velocity Estimates") {
            /**
             *
             */
            private static final long serialVersionUID = -3563303124266525281L;

            public void actionPerformed(final ActionEvent ae) {
                System.out.println("Input Manual Unfolding Estimates");
                String result = DialogUtil.showMultilineInputDialog("Input", "Please enter estimated velocities, one per line.\nFormat is <height in km><space><estimated velocity in m/s>\nEntered values remain in effect until the program is shut down or new numbers are entered.\nOrdering of heights is not important. Faulty lines are ignored.", EstimateParser.getInstance().toString());
                if (result == null) return; //canceled;
                EstimateParser.getInstance().parse(result);
                config.setUnfoldingEnabled(true);
                config.setUnfoldingAutomatic(false);
                update();
                vc.rePlot();
            }
        }));
        return inputPanel;
    }

    private JPanel createGradientOptions() {
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Gradient: "));

        ButtonGroup group = new ButtonGroup();
        group.add(gradientOff);
        group.add(gradientRange);
        group.add(gradientAzimuth);
        gradientOff.setToolTipText("Normal view of data");
        gradientRange.setToolTipText("For supported fields, display the difference between the current point and the next in range instead of raw data");
        gradientAzimuth.setToolTipText("For supported fields, display the difference between the current point and the next in azimuth instead of raw data");
        inputPanel.add(gradientOff);
        inputPanel.add(gradientRange);
        inputPanel.add(gradientAzimuth);

        return inputPanel;
    }

    /**
     * Reloads text fields based on current settings in Config
     */
    public void update() {
        enableThreshCheckBox.setSelected(config.isThresholdEnabled());
        absoluteValueCheckBox.setSelected(config.isThresholdAbsoluteValue());
        enableThreshGreaterThanCheckBox.setSelected(config.isThresholdGreaterThan());
        threshBox.setAction(null);
        threshBox.setSelectedItem(config.getThresholdType());
        threshBox.setAction(threshBoxAction);
        enableSmoothingCheckBox.setSelected(config.isSmoothingEnabled());
        enableNoiseFilterCheckBox.setSelected(config.isNoiseReductionEnabled());
        if (config.isUnfoldingEnabled()) {
            if (config.isUnfoldingAutomatic()) unfoldingAutomatic.setSelected(true);
            else unfoldingManual.setSelected(true);
        } else unfoldingOff.setSelected(true);
        switch (config.getGradientType()) {
            case Range:
                gradientRange.setSelected(true);
                break;
            case Azimuth:
                gradientAzimuth.setSelected(true);
                break;
            default:
                gradientOff.setSelected(true);
                break;
        }
    }

    private abstract class MyAction implements ActionListener, FocusListener {
        public void actionPerformed(final ActionEvent ae) {
            update();
            vc.rePlot();
        }

        public void focusLost(final FocusEvent fe) {
            update();
        }

        public void focusGained(final FocusEvent fe) {
        } //do nothing

        protected abstract void update();
    }
}
