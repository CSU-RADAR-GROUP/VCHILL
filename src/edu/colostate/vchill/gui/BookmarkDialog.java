package edu.colostate.vchill.gui;

import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.bookmark.Bookmark;
import edu.colostate.vchill.bookmark.BookmarkControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for viewing/editing bookmarks
 *
 * @author Jochen Deyke
 * @version 2006-09-14
 */
public class BookmarkDialog {
    private static final BookmarkControl bmc = BookmarkControl.getInstance();
    private static final ScaleManager sm = ScaleManager.getInstance();

    private final Bookmark bookmark;

    public BookmarkDialog(final Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    public void show() {
        final JTabbedPane tabs = new JTabbedPane(/*JTabbedPane.LEFT*/);

        final JPanel general = new JPanel();
        general.setLayout(new BoxLayout(general, BoxLayout.Y_AXIS));
        //comment
        final JTextArea comment = new JTextArea(this.bookmark.comment, 15, 30);
        tabs.addTab("Comment", new JScrollPane(comment));

        final JPanel target = new JPanel();
        target.setLayout(new BoxLayout(target, BoxLayout.X_AXIS));
        final JPanel left = new JPanel();
        left.setLayout(new GridLayout(12, 1));
        final JPanel right = new JPanel();
        right.setLayout(new GridLayout(12, 1));
        //url
        final JTextField url = new JTextField(this.bookmark.url, 30);
        final JLabel urlLabel = new JLabel(" Connection: ");
        urlLabel.setDisplayedMnemonic('C');
        urlLabel.setLabelFor(url);
        left.add(urlLabel);
        right.add(url);
        //dir
        final JTextField dir = new JTextField(this.bookmark.dir, 30);
        final JLabel dirLabel = new JLabel(" Directory: ");
        dirLabel.setDisplayedMnemonic('D');
        dirLabel.setLabelFor(dir);
        left.add(dirLabel);
        right.add(dir);
        //file
        final JTextField file = new JTextField(this.bookmark.file, 30);
        final JLabel fileLabel = new JLabel(" File: ");
        fileLabel.setDisplayedMnemonic('F');
        fileLabel.setLabelFor(file);
        left.add(fileLabel);
        right.add(file);
        //sweep
        final JTextField sweep = new JTextField(this.bookmark.sweep, 30);
        final JLabel sweepLabel = new JLabel(" Sweep: ");
        sweepLabel.setDisplayedMnemonic('S');
        sweepLabel.setLabelFor(sweep);
        left.add(sweepLabel);
        right.add(sweep);
        //sweep
        final JTextField scan = new JTextField(this.bookmark.scan_type, 30);
        final JLabel scanLabel = new JLabel(" Scan Type: ");
        scanLabel.setDisplayedMnemonic('a');
        scanLabel.setLabelFor(scan);
        left.add(scanLabel);
        right.add(scan);
        //separator
        left.add(new JLabel(""));
        right.add(new JLabel(""));
        //center etc
        final JTextField centerX = new JTextField(String.valueOf(bookmark.x), 30);
        final JLabel centerXLabel = new JLabel(" Center X: ");
        centerXLabel.setDisplayedMnemonic('X');
        centerXLabel.setLabelFor(centerX);
        left.add(centerXLabel);
        right.add(centerX);
        final JTextField centerY = new JTextField(String.valueOf(bookmark.y), 30);
        final JLabel centerYLabel = new JLabel(" Center Y: ");
        centerYLabel.setDisplayedMnemonic('Y');
        centerYLabel.setLabelFor(centerY);
        left.add(centerYLabel);
        right.add(centerY);
        final JTextField range = new JTextField(String.valueOf(bookmark.range), 30);
        final JLabel rangeLabel = new JLabel(" Plot Range: ");
        rangeLabel.setDisplayedMnemonic('R');
        rangeLabel.setLabelFor(range);
        left.add(rangeLabel);
        right.add(range);
        final JTextField ring = new JTextField(bookmark.ring, 30);
        final JLabel ringLabel = new JLabel(" Ring/Grid Spacing: ");
        ringLabel.setDisplayedMnemonic('S');
        ringLabel.setLabelFor(ring);
        left.add(ringLabel);
        right.add(ring);
        final JTextField height = new JTextField(bookmark.rhi_height, 30);
        final JLabel heightLabel = new JLabel(" RHI Height Limit: ");
        heightLabel.setDisplayedMnemonic('H');
        heightLabel.setLabelFor(height);
        left.add(heightLabel);
        right.add(height);
        //put it together
        target.add(left);
        target.add(right);
        tabs.addTab("Target", target);

        final JPanel scale = new JPanel();
        scale.setLayout(new BoxLayout(scale, BoxLayout.X_AXIS));
        final int numTypes = this.bookmark.scale.size();
        final JPanel labelCol = new JPanel();
        labelCol.setLayout(new GridLayout(numTypes + 1, 1));
        labelCol.add(new JLabel(""));
        final JCheckBox[] autos = new JCheckBox[numTypes];
        final JPanel autoCol = new JPanel();
        autoCol.setLayout(new GridLayout(numTypes + 1, 1));
        autoCol.add(new JLabel("Autoscale"));
        final JTextField[] mins = new JTextField[numTypes];
        final JPanel minCol = new JPanel();
        minCol.setLayout(new GridLayout(numTypes + 1, 1));
        minCol.add(new JLabel("Minimum"));
        final JTextField[] maxs = new JTextField[numTypes];
        final JPanel maxCol = new JPanel();
        maxCol.setLayout(new GridLayout(numTypes + 1, 1));
        maxCol.add(new JLabel("Maximum"));
        {
            int i = 0;
            for (String type : bookmark.scale.keySet()) {
                final JCheckBox cb = new JCheckBox();
                labelCol.add(new JLabel(" " + type + ": "));
                autoCol.add(autos[i] = cb);
                Bookmark.Scale bmScale = this.bookmark.scale.get(type);
                cb.setSelected(bmScale.autoscale);
                final JTextField min = new JTextField(bmScale.minval, 10);
                final JTextField max = new JTextField(bmScale.maxval, 10);
                minCol.add(mins[i] = min);
                maxCol.add(maxs[i] = max);
                min.setEnabled(!cb.isSelected());
                max.setEnabled(!cb.isSelected());
                cb.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent ae) {
                        boolean selected = cb.isSelected();
                        min.setEnabled(!selected);
                        max.setEnabled(!selected);
                    }
                });
                ++i;
            }
        }
        scale.add(labelCol);
        scale.add(autoCol);
        scale.add(minCol);
        scale.add(maxCol);
        tabs.addTab("Scale", scale);

        JOptionPane pane = new JOptionPane(new Object[]{
                tabs,
        }, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        pane.setWantsInput(false);
        JDialog dialog = pane.createDialog(null, "Bookmark Properties");
        dialog.pack();
        dialog.setVisible(true);
        //get result
        Integer value = (Integer) pane.getValue();
        if (value == null || value.intValue() == JOptionPane.CANCEL_OPTION || value.intValue() == JOptionPane.CLOSED_OPTION)
            return;
        this.bookmark.url = url.getText();
        this.bookmark.dir = dir.getText();
        this.bookmark.file = file.getText();
        this.bookmark.sweep = sweep.getText();
        this.bookmark.scan_type = scan.getText();
        this.bookmark.comment = comment.getText();
        {
            int i = 0;
            for (String type : this.bookmark.scale.keySet()) {
                Bookmark.Scale bmScale = this.bookmark.scale.get(type);
                bmScale.autoscale = autos[i].isSelected();
                bmScale.minval = mins[i].getText();
                bmScale.maxval = maxs[i].getText();
                ++i;
            }
        }
        this.bookmark.x = Double.parseDouble(centerX.getText());
        this.bookmark.y = Double.parseDouble(centerY.getText());
        this.bookmark.range = Double.parseDouble(range.getText());
        this.bookmark.ring = ring.getText();
        this.bookmark.rhi_height = height.getText();
    }
}
