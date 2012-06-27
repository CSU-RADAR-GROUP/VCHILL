package edu.colostate.vchill.gui;


import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class MapServerConfigWindow extends JPanel
{
    private JList availableChoicesList;
    private DefaultListModel listModel;

    private static final String displayMapString = "Display Map";
    private JButton displayMapButton;

    public MapServerConfigWindow(ArrayList<String> layers) 
    {
        super(new BorderLayout()); 	

        listModel = new DefaultListModel();
        
        for(int i = 0; i < layers.size(); i++)
        {
        	listModel.addElement(layers.get(i));        	
        }

        //Create the list and put it in a scroll pane.
        availableChoicesList = new JList(listModel);
        availableChoicesList.setSelectedIndex(0);
        availableChoicesList.setVisibleRowCount(10);
        availableChoicesList.setDragEnabled(true);
        JScrollPane listScrollPane = new JScrollPane(availableChoicesList);

        displayMapButton = new JButton(displayMapString);
        displayMapButton.setActionCommand(displayMapString);
        displayMapButton.addActionListener(new DisplayMapListener());

        //Create a panel that uses BoxLayout.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,BoxLayout.LINE_AXIS));
        buttonPane.add(displayMapButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        add(listScrollPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
    }

    class DisplayMapListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) 
        {

            int index = availableChoicesList.getSelectedIndex();
            
            int[] selectedIndices = availableChoicesList.getSelectedIndices();
            
            MapServerConfig.displayMap(selectedIndices);
            
        }
    }		
}
