package edu.colostate.vchill.gui;

import edu.colostate.vchill.Config;
import edu.colostate.vchill.ConfigUtil;
import edu.colostate.vchill.ViewControl;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Panel to control the automatic saving of images after plotting a sweep
 * (This is the "Advanced" tab panel on the bottom left of the program).
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class ImageControlPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = -7700937488703078642L;
    private static final ViewControl vc = ViewControl.getInstance();
    private static final Config config = Config.getInstance();

    private final JRadioButton allButton, autoButton;

    public ImageControlPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //clear cache panel
        JPanel clearPanel = new JPanel();

        JButton clearPrefsButton = new JButton(new AbstractAction("Clear preferences") {
            /**
             *
             */
            private static final long serialVersionUID = 6441644772876821062L;

            public void actionPerformed(final ActionEvent ae) {
                ConfigUtil.clear();
                System.out.println("Default settings restored");
                System.exit(0);
            }
        });
        clearPrefsButton.setToolTipText("<html>Reset all preferences to default values and <b>exit</b></html>");
        clearPanel.add(clearPrefsButton);

        JButton clearCacheButton = new JButton(new AbstractAction("Clear cache") {
            /**
             *
             */
            private static final long serialVersionUID = -9098284931829769091L;

            public void actionPerformed(final ActionEvent ae) {
                vc.clearCache();
                System.out.println("Cache cleared");
            }
        });
        clearCacheButton.setToolTipText("Remove all cached sweeps from memory");
        clearPanel.add(clearCacheButton);

        this.add(clearPanel);

        this.add(new JSeparator()); //--------------------------------

        //init these first to allow en-/disabling
        final ButtonGroup group = new ButtonGroup();
        this.allButton = new JRadioButton(new AbstractAction("All sweeps  ") {
            /**
             *
             */
            private static final long serialVersionUID = -1699324759273344291L;

            public void actionPerformed(final ActionEvent ae) {
                config.setSaveAllEnabled(true);
            }
        });
        this.autoButton = new JRadioButton(new AbstractAction("Webtilt  ") {
            /**
             *
             */
            private static final long serialVersionUID = 2176297588900454123L;

            public void actionPerformed(final ActionEvent ae) {
                config.setSaveAllEnabled(false);
            }
        });

        //auto panel
        JPanel autoPanel = new JPanel();
        final JCheckBox autoCB = new JCheckBox(new AbstractAction("Automatically save from Plot windows:") {
            /**
             *
             */
            private static final long serialVersionUID = -2815060317366574222L;

            public void actionPerformed(final ActionEvent ae) {
                boolean selected = ((JCheckBox) ae.getSource()).isSelected();
                config.setImageAutosaveEnabled(selected);
                updateEnabledness();
            }
        });
        autoCB.setSelected(config.isImageAutosaveEnabled());
        autoPanel.add(autoCB);
        this.add(autoPanel);

        //export panel
        JPanel exportPanel = new JPanel();
        final JCheckBox exportCB = new JCheckBox(new AbstractAction("Automatically export to Google Earth:") {
            /**
             *
             */
            private static final long serialVersionUID = 6566359680967342918L;

            public void actionPerformed(final ActionEvent ae) {
                boolean selected = ((JCheckBox) ae.getSource()).isSelected();
                config.setImageAutoExportEnabled(selected);
                updateEnabledness();
            }
        });
        exportCB.setSelected(config.isImageAutoExportEnabled());
        exportPanel.add(exportCB);
        this.add(exportPanel);

        //tilt panel
        JPanel tiltPanel = new JPanel();
        int saveTilt = config.getAutosaveTilt();

        group.add(allButton);
        allButton.setSelected(config.isSaveAllEnabled());
        allButton.setToolTipText("Every sweep");
        tiltPanel.add(allButton);

        group.add(autoButton);
        autoButton.setSelected(!config.isSaveAllEnabled());
        autoButton.setToolTipText("Sweep matching number sent by server");
        tiltPanel.add(autoButton);

        updateEnabledness();

        this.add(tiltPanel);

        this.add(new JSeparator()); //--------------------------------

        //filter panel description
        JPanel filterDescPanel = new JPanel();
        filterDescPanel.add(new JLabel("Only plot if:"));
        this.add(filterDescPanel);

        //filter panel
        JPanel filterPanel = new JPanel();
        String filterString = config.getScanFilter();

        final JTextField filterInput = new JTextField(8);
        filterInput.setText(filterString);
        filterInput.setEnabled(filterString != null);
        final JCheckBox filterCB = new JCheckBox(new AbstractAction("Filename contains: ") {
            /**
             *
             */
            private static final long serialVersionUID = -3222321555612890750L;

            public void actionPerformed(final ActionEvent ae) {
                filterInput.setEnabled(((JCheckBox) ae.getSource()).isSelected());
            }
        });
        filterCB.setSelected(filterString != null);

        filterPanel.add(filterCB);
        filterPanel.add(filterInput);

        this.add(filterPanel);

        JPanel sweepPanel = new JPanel();
        final JTextField tiltInput = new JTextField(5);
        tiltInput.setText(saveTilt > 0 ? String.valueOf(saveTilt) : null);
        tiltInput.setEnabled(saveTilt > 0);
        final JCheckBox customCB = new JCheckBox(new AbstractAction("Sweep number matches:") {
            /**
             *
             */
            private static final long serialVersionUID = -1620222125314661228L;

            public void actionPerformed(final ActionEvent ae) {
                tiltInput.setEnabled(((JCheckBox) ae.getSource()).isSelected());
            }
        });
        customCB.setSelected(saveTilt > 0);
        sweepPanel.add(customCB);
        sweepPanel.add(tiltInput);
        this.add(sweepPanel);

        //apply button -> description panel; create here due to referencing items not available before
        JButton applyButton = new JButton(new AbstractAction("Apply settings") {
            /**
             *
             */
            private static final long serialVersionUID = 5049963229002185319L;

            public void actionPerformed(final ActionEvent ae) {
                config.setImageAutosaveEnabled(autoCB.isSelected());
                config.setScanFilter(filterCB.isSelected() ? filterInput.getText() : null);
                try {
                    config.setAutosaveTilt(customCB.isSelected() ? Integer.parseInt(tiltInput.getText()) : 0);
                } catch (NumberFormatException nfe) {
                    config.setAutosaveTilt(0);
                }
            }
        });
        filterDescPanel.add(new JPanel());
        filterDescPanel.add(applyButton);
    } //end contructor

    private void updateEnabledness() {
        boolean autosave = config.isImageAutosaveEnabled() || config.isImageAutoExportEnabled();
        this.allButton.setEnabled(autosave);
        this.autoButton.setEnabled(autosave);
    }
}
