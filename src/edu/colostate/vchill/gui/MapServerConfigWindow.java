package edu.colostate.vchill.gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MapServerConfigWindow extends JPanel implements ChangeListener {

    private final int maxSliderSize = 255;
    private final int minSliderSize = 0;
    private static int Opacity = 255;

    ArrayList<String> layers;

    JList dragFromJList;
    JList overlayJList;
    JList underlayJList;

    DefaultListModel choices = new DefaultListModel();
    DefaultListModel overlay = new DefaultListModel();
    DefaultListModel underlay = new DefaultListModel();

    private JButton displayMapButton;
    private JButton removeFromOverlayButton;
    private JButton removeFromUnderlayButton;
    private JButton addToOverlayButton;
    private JButton addToUnderlayButton;

    private JSlider weatherDataOpacitySlider;
    private JLabel weatherDataOpacityLabel;

    private static int[] selectedUnderlayIndices = null;
    private static int[] selectedOverlayIndices = null;

    public MapServerConfigWindow(ArrayList<String> passedLayers) {

        super(new BorderLayout());

        layers = passedLayers;

        weatherDataOpacitySlider = new JSlider(JSlider.HORIZONTAL, minSliderSize, maxSliderSize, Opacity);
        weatherDataOpacitySlider.addChangeListener(this);

        for (int i = 0; i < layers.size(); i++) {
            choices.addElement(layers.get(i));
        }

        populateOverlayAndUnderlay();

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        dragFromJList = new JList(choices);
        dragFromJList.setTransferHandler(new FromTransferHandler());
        dragFromJList.setDragEnabled(true);
        dragFromJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JLabel label = new JLabel("Drag and drop choices to these boxes");
        JScrollPane sp = new JScrollPane(dragFromJList);
        // sp.setAlignmentX(0f);
        p.add(label, BorderLayout.NORTH);
        p.add(sp, BorderLayout.WEST);

        JPanel subP = new JPanel();
        subP.setLayout(new BoxLayout(subP, BoxLayout.X_AXIS));

        addToOverlayButton = new JButton("Move to overlay");
        addToOverlayButton.setActionCommand("Move to overlay");
        addToOverlayButton.addActionListener(new overlayJListListener());
        subP.add(addToOverlayButton, BorderLayout.WEST);
    /*
     * subP.add(Box.createHorizontalStrut(5)); subP.add(new
     * JSeparator(SwingConstants.VERTICAL));
     * 
     * subP.add(Box.createHorizontalStrut(5));
     */
        addToUnderlayButton = new JButton("Move to underlay");
        addToUnderlayButton.setActionCommand("Move to underlay");
        addToUnderlayButton.addActionListener(new underlayJListListener());
        subP.add(addToUnderlayButton, BorderLayout.EAST);

        p.add(subP);
        add(p, BorderLayout.LINE_START);

        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        overlayJList = new JList(overlay);
        overlayJList.setTransferHandler(new OverlayTransferHandler(TransferHandler.MOVE));
        overlayJList.setDropMode(DropMode.INSERT);
        overlayJList.setDragEnabled(true);
        overlayJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        overlayJList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    removeFromOverlay();
                }
            }
        });

        underlayJList = new JList(underlay);
        underlayJList.setTransferHandler(new UnderlayTransferHandler(TransferHandler.MOVE));
        underlayJList.setDropMode(DropMode.INSERT);
        underlayJList.setDragEnabled(true);
        underlayJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        underlayJList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    removeFromUnderlay();
                }
            }
        });

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        label.setAlignmentX(0f);
        p.add(label);
        sp = new JScrollPane(overlayJList);

        sp.setAlignmentX(0f);
        p.add(sp);

        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        sp.setAlignmentX(0f);
        p.add(sp);
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(p, BorderLayout.CENTER);

        label = new JLabel("Overlay:");
        label.setAlignmentX(0f);
        p.add(label);
        sp.setAlignmentX(0f);

        p.add(sp);
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(p, BorderLayout.CENTER);

        removeFromOverlayButton = new JButton("Remove from Overlay");
        removeFromOverlayButton.setActionCommand("Remove from Overlay");
        removeFromOverlayButton.addActionListener(new RemoveFromOverlayListener());

        p.add(removeFromOverlayButton);

        sp = new JScrollPane(underlayJList);

        sp.setAlignmentX(0f);
        p.add(sp);
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        label = new JLabel("Underlay");
        label.setAlignmentX(0f);
        p.add(label);
        sp.setAlignmentX(0f);
        p.add(sp);
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(p, BorderLayout.CENTER);

        removeFromUnderlayButton = new JButton("Remove from Underlay");
        removeFromUnderlayButton.setActionCommand("Remove from Underlay");
        removeFromUnderlayButton.addActionListener(new RemoveFromUnderlayListener());
        p.add(removeFromUnderlayButton);

        weatherDataOpacityLabel = new JLabel("Weather Data Opacity " + (Integer.toString((100 * Opacity / 255))) + "%");

        p.add(weatherDataOpacityLabel);

        p.add(weatherDataOpacitySlider);

        // getContentPane().setPreferredSize(new Dimension(250, 300));

        displayMapButton = new JButton("Display Map");
        displayMapButton.setActionCommand("Display Map");
        displayMapButton.addActionListener(new DisplayMapListener());

        // Create a panel that uses BoxLayout.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(displayMapButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // add(listScrollPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
    }

    private int[] matchIndicesToTitles(DefaultListModel myDefaultListModel) {
        int[] myIndices = new int[myDefaultListModel.getSize()];

        for (int i = 0; i < myDefaultListModel.getSize(); i++) {
            for (int j = 0; j < layers.size(); j++) {
                if (layers.get(j).equals(myDefaultListModel.get(i))) {
                    myIndices[i] = j;
                }

            }
        }
        return myIndices;
    }

    public static int getOpacity() {

        return Opacity;
    }

    private void populateOverlayAndUnderlay() {

        if (selectedOverlayIndices == null) {
            // System.out.println("No overlay layers to display");
        } else {

            for (int i = 0; i < selectedOverlayIndices.length; i++) {
                for (int j = 0; j < choices.getSize(); j++) {
                    if (layers.get(selectedOverlayIndices[i]).equals(choices.get(j))) {
                        String removedValue = (String) choices.get(j);

                        choices.removeElementAt(j);

                        overlay.insertElementAt(removedValue, overlay.getSize());

                    }
                }
            }
        }

        if (selectedUnderlayIndices == null) {
            // System.out.println("No underlay layers to display");
        } else {

            for (int i = 0; i < selectedUnderlayIndices.length; i++) {
                for (int j = 0; j < choices.getSize(); j++) {
                    if (layers.get(selectedUnderlayIndices[i]).equals(choices.get(j))) {
                        String removedValue = (String) choices.get(j);

                        choices.removeElementAt(j);

                        underlay.insertElementAt(removedValue, underlay.getSize());

                    }
                }
            }
        }

    }

    public void removeFromOverlay() {
        int selectedIndex = overlayJList.getSelectedIndex();

        String removedValue = (String) overlay.get(selectedIndex);

        overlay.removeElementAt(selectedIndex);

        choices.insertElementAt(removedValue, 0);
    }

    public void removeFromUnderlay() {
        int selectedIndex = underlayJList.getSelectedIndex();

        String removedValue = (String) underlay.get(selectedIndex);

        underlay.removeElementAt(selectedIndex);

        choices.insertElementAt(removedValue, 0);
    }

    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();

        weatherDataOpacityLabel.setText("Weather Data Opacity "
                + (Integer.toString((100 * weatherDataOpacitySlider.getValue() / 255))) + "%");

        if (!source.getValueIsAdjusting()) {
            Opacity = source.getValue();
        }
    }

    class DisplayMapListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // int index = dragToOverlay.getSelectedIndex();

            selectedUnderlayIndices = matchIndicesToTitles(underlay);
            selectedOverlayIndices = matchIndicesToTitles(overlay);

            // int[] selectedUnderlayIndices = underlayJList.getSelectedIndices();
            // int[] selectedOverlayIndices = overlayJList.getSelectedIndices();

            MapServerConfig msConfig = MapServerConfig.getInstance();

            msConfig.displayMap(selectedUnderlayIndices, selectedOverlayIndices);
        }
    }

    class RemoveFromOverlayListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            removeFromOverlay();
        }
    }

    class RemoveFromUnderlayListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            removeFromUnderlay();
        }
    }

    class underlayJListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = dragFromJList.getSelectedIndex();

            String removedValue = (String) choices.get(selectedIndex);

            choices.removeElementAt(selectedIndex);

            underlay.insertElementAt(removedValue, underlay.getSize());
        }
    }

    class overlayJListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = dragFromJList.getSelectedIndex();

            String removedValue = (String) choices.get(selectedIndex);

            choices.removeElementAt(selectedIndex);

            overlay.insertElementAt(removedValue, overlay.getSize());
        }
    }

    class FromTransferHandler extends TransferHandler {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public int getSourceActions(JComponent comp) {
            return MOVE;
        }

        private int index = 0;

        public Transferable createTransferable(JComponent comp) {
            index = dragFromJList.getSelectedIndex();
            if (index < 0 || index >= choices.getSize()) {
                return null;
            }

            return new StringSelection((String) dragFromJList.getSelectedValue());
        }

        public void exportDone(JComponent comp, Transferable trans, int action) {
            if (action != MOVE) {
                return;
            }

            choices.removeElementAt(index);
        }

    }

    class OverlayTransferHandler extends TransferHandler {
        int previousSize;
        int currentSize;

        public int getSourceActions(JComponent comp) {
            previousSize = overlay.getSize();
            currentSize = overlay.getSize();

            return MOVE;
        }

        private int index = 0;

        public Transferable createTransferable(JComponent comp) {
            index = overlayJList.getSelectedIndex();
            if (index < 0 || index >= overlay.getSize()) {
                return null;
            }

            return new StringSelection((String) overlayJList.getSelectedValue());
        }

        public void exportDone(JComponent comp, Transferable trans, int action) {
            try {

                String deletedValue = (String) overlay.get(index + 1);
                String otherDeletedValue = (String) overlay.get(index);

                if ((overlayJList.getSelectedIndex() <= index) && ((index + 1) <= overlay.getSize())) {
                    overlay.removeElementAt(index + 1);
                } else
                    overlay.removeElementAt(index);

                currentSize = overlay.getSize();

                if (currentSize < previousSize) {
                    overlay.insertElementAt(deletedValue, index);

                    for (int i = 0; i < underlay.getSize(); i++) {

                        if (i == 0) {
                            String placeHolder = (String) overlay.get(index + 1);
                            overlay.set(index + 1, deletedValue);
                            overlay.set(index, placeHolder);
                            overlayJList.setSelectedIndex(index);
                        }

                        if (otherDeletedValue.equals(underlay.get(i)))
                            underlay.removeElementAt(i);
                    }
                }
            } catch (Exception e) {
                for (int i = 0; i < underlay.getSize(); i++) {
                    if (underlay.get(i).equals(overlay.get(overlayJList.getSelectedIndex()))) {
                        underlay.removeElementAt(i);
                    }
                }

            }
        }

        int action;

        public OverlayTransferHandler(int action) {
            this.action = action;
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            // for the demo, we'll only support drops (not clipboard paste)
            if (!support.isDrop()) {
                return false;
            }

            // we only import Strings

            if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }

            boolean actionSupported = (action & support.getSourceDropActions()) == action;

            if (actionSupported) {
                support.setDropAction(action);
                return true;
            }
            return false;

        }

        public boolean importData(TransferHandler.TransferSupport support) {
            // if we can't handle the import, say so
            if (!canImport(support)) {
                return false;
            }
            // fetch the drop location

            JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
            int index = dl.getIndex();
            // fetch the data and bail if this fails
            String data;
            try {
                data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException e) {
                return false;
            } catch (java.io.IOException e) {
                return false;
            }

            JList list = (JList) support.getComponent();
            DefaultListModel model = (DefaultListModel) list.getModel();
            model.insertElementAt(data, index);
            Rectangle rect = list.getCellBounds(index, index);
            list.scrollRectToVisible(rect);
            list.setSelectedIndex(index);
            list.requestFocusInWindow();
            return true;
        }
    }

    class UnderlayTransferHandler extends TransferHandler {
        int previousSize;
        int currentSize;

        public int getSourceActions(JComponent comp) {
            previousSize = underlay.getSize();
            currentSize = underlay.getSize();

            return MOVE;
        }

        private int index = 0;

        public Transferable createTransferable(JComponent comp) {
            index = underlayJList.getSelectedIndex();
            if (index < 0 || index >= underlay.getSize()) {
                return null;
            }

            return new StringSelection((String) underlayJList.getSelectedValue());
        }

        public void exportDone(JComponent comp, Transferable trans, int action) {
            try {
                String deletedValue = (String) underlay.get(index + 1);
                String otherDeletedValue = (String) underlay.get(index);

                if ((underlayJList.getSelectedIndex() <= index) && ((index + 1) <= underlay.getSize())) {
                    underlay.removeElementAt(index + 1);
                } else
                    underlay.removeElementAt(index);

                currentSize = underlay.getSize();

                if (currentSize < previousSize) {
                    underlay.insertElementAt(deletedValue, index);

                    for (int i = 0; i < overlay.getSize(); i++) {

                        if (i == 0) {
                            String placeHolder = (String) underlay.get(index + 1);
                            underlay.set(index + 1, deletedValue);
                            underlay.set(index, placeHolder);
                            underlayJList.setSelectedIndex(index);
                        }

                        if (otherDeletedValue.equals(overlay.get(i))) {
                            overlay.removeElementAt(i);
                        }
                    }

                }
            } catch (Exception e) {
                for (int i = 0; i < overlay.getSize(); i++) {
                    if (overlay.get(i).equals(underlay.get(underlayJList.getSelectedIndex()))) {
                        overlay.removeElementAt(i);
                    }
                }

            }
        }

        int action;

        public UnderlayTransferHandler(int action) {
            this.action = action;
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            // for the demo, we'll only support drops (not clipboard paste)
            if (!support.isDrop()) {
                return false;
            }

            // we only import Strings

            if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }

            boolean actionSupported = (action & support.getSourceDropActions()) == action;

            if (actionSupported) {
                support.setDropAction(action);
                return true;
            }
            return false;

        }

        public boolean importData(TransferHandler.TransferSupport support) {
            // if we can't handle the import, say so
            if (!canImport(support)) {
                return false;
            }
            // fetch the drop location

            JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
            int index = dl.getIndex();
            // fetch the data and bail if this fails
            String data;
            try {
                data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException e) {
                return false;
            } catch (java.io.IOException e) {
                return false;
            }

            JList list = (JList) support.getComponent();
            DefaultListModel model = (DefaultListModel) list.getModel();
            model.insertElementAt(data, index);
            Rectangle rect = list.getCellBounds(index, index);
            list.scrollRectToVisible(rect);
            list.setSelectedIndex(index);
            list.requestFocusInWindow();
            return true;
        }
    }
}