package edu.colostate.vchill.gui;

import edu.colostate.vchill.Config;
import edu.colostate.vchill.ConfigUtil;
import edu.colostate.vchill.ViewControl;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

/**
 * Panel to control the automatic saving of images after plotting a sweep
 * (This is the "Advanced" tab panel on the bottom left of the program).
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class ImageControlPanel extends JPanel
{
    private static final ViewControl vc = ViewControl.getInstance();
    private static final Config config = Config.getInstance();

    private final JRadioButton allButton, autoButton;

    public ImageControlPanel ()
    {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //clear cache panel
        JPanel clearPanel = new JPanel();

        JButton clearPrefsButton = new JButton(new AbstractAction("Clear preferences") {
            public void actionPerformed (final ActionEvent ae) {
                ConfigUtil.clear();
                System.out.println("Default settings restored");
                System.exit(0);
            }});
        clearPrefsButton.setToolTipText("<html>Reset all preferences to default values and <b>exit</b></html>");
        clearPanel.add(clearPrefsButton);

        JButton clearCacheButton = new JButton(new AbstractAction("Clear cache") {
            public void actionPerformed (final ActionEvent ae) {
                vc.clearCache();
                System.out.println("Cache cleared");
            }});
        clearCacheButton.setToolTipText("Remove all cached sweeps from memory");
        clearPanel.add(clearCacheButton);

        this.add(clearPanel);

        this.add(new JSeparator()); //--------------------------------

        //init these first to allow en-/disabling
        final ButtonGroup group = new ButtonGroup();
        this.allButton = new JRadioButton(new AbstractAction("All sweeps  ") {
            public void actionPerformed (final ActionEvent ae) {
                config.setSaveAllEnabled(true); }});
        this.autoButton = new JRadioButton(new AbstractAction("Webtilt  ") {
            public void actionPerformed (final ActionEvent ae) {
                config.setSaveAllEnabled(false); }});

        //auto panel
        JPanel autoPanel = new JPanel();
        final JCheckBox autoCB = new JCheckBox(new AbstractAction("Automatically save from Plot windows:") {
            public void actionPerformed (final ActionEvent ae) {
                boolean selected = ((JCheckBox)ae.getSource()).isSelected();
                config.setImageAutosaveEnabled(selected);
                updateEnabledness();
            }});
        autoCB.setSelected(config.isImageAutosaveEnabled());
        autoPanel.add(autoCB);
        this.add(autoPanel);

        //export panel
        JPanel exportPanel = new JPanel();
        final JCheckBox exportCB = new JCheckBox(new AbstractAction("Automatically export to Google Earth:") {
            public void actionPerformed (final ActionEvent ae) {
                boolean selected = ((JCheckBox)ae.getSource()).isSelected();
                config.setImageAutoExportEnabled(selected);
                updateEnabledness();
            }});
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
            public void actionPerformed (final ActionEvent ae) {
                filterInput.setEnabled(((JCheckBox)ae.getSource()).isSelected());
            }});
        filterCB.setSelected(filterString != null);

        filterPanel.add(filterCB);
        filterPanel.add(filterInput);

        this.add(filterPanel);

        JPanel sweepPanel = new JPanel();
        final JTextField tiltInput = new JTextField(5);
        tiltInput.setText(saveTilt > 0 ? String.valueOf(saveTilt) : null);
        tiltInput.setEnabled(saveTilt > 0);
        final JCheckBox customCB = new JCheckBox(new AbstractAction("Sweep number matches:") {
            public void actionPerformed (final ActionEvent ae) {
                tiltInput.setEnabled(((JCheckBox)ae.getSource()).isSelected());
            }});
        customCB.setSelected(saveTilt > 0);
        sweepPanel.add(customCB);
        sweepPanel.add(tiltInput);
        this.add(sweepPanel);

        //apply button -> description panel; create here due to referencing items not available before
        JButton applyButton = new JButton(new AbstractAction("Apply settings") {
            public void actionPerformed (final ActionEvent ae) {
                config.setImageAutosaveEnabled(autoCB.isSelected());
                config.setScanFilter(filterCB.isSelected() ? filterInput.getText() : null);
                try { config.setAutosaveTilt(customCB.isSelected() ? Integer.parseInt(tiltInput.getText()) : 0); }
                catch (NumberFormatException nfe) { config.setAutosaveTilt(0); }
            }});
        filterDescPanel.add(new JPanel());
        filterDescPanel.add(applyButton);
    } //end contructor

    private void updateEnabledness ()
    {
        boolean autosave = config.isImageAutosaveEnabled() || config.isImageAutoExportEnabled();
        this.allButton.setEnabled(autosave);
        this.autoButton.setEnabled(autosave);
    }
}
