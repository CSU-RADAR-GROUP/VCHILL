package edu.colostate.vchill.gui;


import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import javax.swing.event.*;


import java.awt.datatransfer.*; 


public class MapServerConfigWindow extends JPanel implements ChangeListener
{

	final int maxSliderSize = 255;
	final int minSliderSize = 0;	
	public static int Transparency = 255;
	ArrayList<String> layers;
	
	JList dragFrom;
	JList moveToOverlay;
	JList moveToUnderlay;    
    
	DefaultListModel choices = new DefaultListModel();     
	DefaultListModel overlay = new DefaultListModel();
	DefaultListModel underlay = new DefaultListModel();
	
	
    private static final String displayMapString = "Display Map";
    private JButton displayMapButton;

    public MapServerConfigWindow(ArrayList<String> passedLayers) 
    {
    	
        super(new BorderLayout()); 	

        
        layers = passedLayers;
		JSlider weatherDataOpacitySlider = new JSlider(JSlider.HORIZONTAL, minSliderSize, maxSliderSize, maxSliderSize);
		weatherDataOpacitySlider.addChangeListener(this);        
        
        
        for(int i = 0; i < layers.size(); i++)
        {
        	choices.addElement(layers.get(i));        	
        }

		JPanel p = new JPanel();         
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));         
		
		dragFrom = new JList(choices);         
		dragFrom.setTransferHandler(new FromTransferHandler());         
		dragFrom.setDragEnabled(true);         
		dragFrom.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);         
		
		JLabel label = new JLabel("Drag choices here:");         
		label.setAlignmentX(0f);         
		p.add(label, BorderLayout.WEST);         
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));         
		JScrollPane sp = new JScrollPane(dragFrom);         
		sp.setAlignmentX(0f);         
		p.add(sp);         
		add(p, BorderLayout.WEST);                   
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));         

		moveToOverlay = new JList(overlay);         
		moveToOverlay.setTransferHandler(new OverlayTransferHandler(TransferHandler.MOVE));         
		moveToOverlay.setDropMode(DropMode.INSERT);
		moveToOverlay.setDragEnabled(true);
		moveToOverlay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		

		moveToUnderlay = new JList(underlay);         
		moveToUnderlay.setTransferHandler(new UnderlayTransferHandler(TransferHandler.MOVE));         
		moveToUnderlay.setDropMode(DropMode.INSERT);
		moveToUnderlay.setDragEnabled(true);
		moveToUnderlay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
		
		p = new JPanel();         
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));         
		label.setAlignmentX(0f);         
		p.add(label);         
		sp = new JScrollPane(moveToOverlay);         
		
		sp.setAlignmentX(0f);         
		p.add(sp);      
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));         
		
		label = new JLabel("Drop to MOVE to here:");         
		label.setAlignmentX(0f);         
		p.add(label);         
		sp.setAlignmentX(0f);         
		p.add(sp);         
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));         
		add(p, BorderLayout.CENTER);                   

		
		sp = new JScrollPane(moveToUnderlay);         
		
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

	
		label = new JLabel("Weather Data Opacity");

		p.add(label);

		p.add(weatherDataOpacitySlider);
		
		
		//getContentPane().setPreferredSize(new Dimension(250, 300));



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

        
      
        
        //add(listScrollPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
    }

    private int[] matchIndicesToTitles(DefaultListModel myDefaultListModel)
    {
    	int[] myIndices = new int[myDefaultListModel.getSize()];
    	
   // 	moveToUnderlay.
    	
    	for(int i = 0; i < myDefaultListModel.getSize(); i++)
    	{
    		for(int j = 0; j < layers.size(); j++)
    		{
    			if(layers.get(j).equals((String) myDefaultListModel.get(i)))
    			{
    				myIndices[i] = j;
    			}
    			
    		}
    	}
    	return myIndices;
    }
    
    
	public void stateChanged(ChangeEvent e)
	{
		JSlider source = (JSlider)e.getSource();
		
		if(!source.getValueIsAdjusting())
		{
			System.out.println(source.getValue());
			
			Transparency = source.getValue();
		}
		
	}    
    
    
    class DisplayMapListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) 
        {
            //int index = dragToOverlay.getSelectedIndex();
        	
        	int[] selectedUnderlayIndices = matchIndicesToTitles(underlay);
            int[] selectedOverlayIndices = matchIndicesToTitles(overlay);        	
        	
            //int[] selectedUnderlayIndices = moveToUnderlay.getSelectedIndices();
            //int[] selectedOverlayIndices = moveToOverlay.getSelectedIndices();
            MapServerConfig.displayMap(selectedUnderlayIndices, selectedOverlayIndices);            
        }
    }
    
	class FromTransferHandler extends TransferHandler 
	{         	
		public int getSourceActions(JComponent comp) 
		{             
			return MOVE;         
		}           
				
		private int index = 0;           
		public Transferable createTransferable(JComponent comp) 
		{             
			index = dragFrom.getSelectedIndex();             
			if (index < 0 || index >= choices.getSize()) 
			{                 
				return null;             
			}               
			
			return new StringSelection((String)dragFrom.getSelectedValue());         
		}                   
		
		public void exportDone(JComponent comp, Transferable trans, int action) 
		{             
			if (action != MOVE) 
			{                 
				return;             			
			}               
			
			choices.removeElementAt(index);         
		}     
		
	}           
	
	class OverlayTransferHandler extends TransferHandler 
	{   
		int previousSize;
		int currentSize;
		
		public int getSourceActions(JComponent comp) 
		{   
			previousSize = overlay.getSize();
			currentSize = overlay.getSize();
			
			return MOVE;         
		}           
		
		private int index = 0;           
		public Transferable createTransferable(JComponent comp) 
		{             
			index = moveToOverlay.getSelectedIndex();             
			if (index < 0 || index >= overlay.getSize()) 
			{                 
				return null;             
			}               
			
			return new StringSelection((String)moveToOverlay.getSelectedValue());         
		}                   
		
		public void exportDone(JComponent comp, Transferable trans, int action) 
		{     
			try
			{

				String deletedValue = (String) overlay.get(index+1);
				String otherDeletedValue = (String) overlay.get(index);
				
				if((moveToOverlay.getSelectedIndex() <= index) && ((index+1) <= overlay.getSize()))
				{
					overlay.removeElementAt(index+1);
				}
				else
					overlay.removeElementAt(index);

				
				currentSize = overlay.getSize();
				
				if(currentSize < previousSize)
				{					
					overlay.insertElementAt(deletedValue, index);
					
					for(int i = 0; i < underlay.getSize(); i++)
					{

						
						if(i == 0)
						{
							String placeHolder = (String) overlay.get(index+1);
							overlay.set(index+1, deletedValue);
							overlay.set(index, placeHolder);
							moveToOverlay.setSelectedIndex(index);				
						}
						
						
						System.out.println("When does this happen?");
						if(otherDeletedValue.equals((String) underlay.get(i)))
							underlay.removeElementAt(i);						
					}
				}			
			}
			catch(Exception e)
			{
				for(int i = 0; i < underlay.getSize(); i++)
				{
					if(underlay.get(i).equals(overlay.get(moveToOverlay.getSelectedIndex())))
					{
						underlay.removeElementAt(i);
					}					
				}

				System.out.println("It was caught: line 274 of MapServerConfigWindow");				
			}
		}		
		
		
		int action;
		
		public OverlayTransferHandler(int action) 
		{             
			this.action = action;        
		}                   
		
		public boolean canImport(TransferHandler.TransferSupport support) 
		{             
			// for the demo, we'll only support drops (not clipboard paste)             
			if (!support.isDrop()) 
			{                 
				return false;            
			}               
			
			// we only import Strings             
			
			if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) 
			{                 
				return false;             
			}               
			
			boolean actionSupported = (action & support.getSourceDropActions()) == action;             
			
			if (actionSupported) 
			{                 
				support.setDropAction(action);                 
				return true;             
			}               
			return false;         
			
		}           
		
		public boolean importData(TransferHandler.TransferSupport support) 
		{             
			// if we can't handle the import, say so             
			if (!canImport(support)) 
			{                 
				return false;             
			}               
			// fetch the drop location             
			
			JList.DropLocation dl = (JList.DropLocation)support.getDropLocation();               
			int index = dl.getIndex();               
			// fetch the data and bail if this fails             
			String data;             
			try 
			{                 
				data = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);             
			} 
			catch (UnsupportedFlavorException e) 
			{                 
				return false;             
			} 
			catch (java.io.IOException e) 
			{                 
				return false;             
			}               
			
			JList list = (JList)support.getComponent();             
			DefaultListModel model = (DefaultListModel)list.getModel();             
			model.insertElementAt(data, index);               
			Rectangle rect = list.getCellBounds(index, index);             
			list.scrollRectToVisible(rect);             
			list.setSelectedIndex(index);             
			list.requestFocusInWindow();               
			return true;         
		}       
	}  

	class UnderlayTransferHandler extends TransferHandler 
	{   
		int previousSize;
		int currentSize;
		
		public int getSourceActions(JComponent comp) 
		{   
			previousSize = underlay.getSize();
			currentSize = underlay.getSize();
			
			return MOVE;         
		}           
		
		private int index = 0;           
		public Transferable createTransferable(JComponent comp) 
		{             
			index = moveToUnderlay.getSelectedIndex();             
			if (index < 0 || index >= underlay.getSize()) 
			{                 
				return null;             
			}               
			
			return new StringSelection((String)moveToUnderlay.getSelectedValue());         
		}                   
		
		public void exportDone(JComponent comp, Transferable trans, int action) 
		{     			
			try
			{
				String deletedValue = (String) underlay.get(index + 1);
				String otherDeletedValue = (String) underlay.get(index);
				
				
				if((moveToUnderlay.getSelectedIndex() <= index) && ((index+1) <= underlay.getSize()))
				{
					underlay.removeElementAt(index+1);
				}
				else
					underlay.removeElementAt(index);

				
				currentSize = underlay.getSize();
				
				if(currentSize < previousSize)
				{					
					underlay.insertElementAt(deletedValue, index);
					
					
					for(int i = 0; i < overlay.getSize(); i++)
					{						

						if(i == 0)
						{
							String placeHolder = (String) underlay.get(index+1);
							underlay.set(index+1, deletedValue);
							underlay.set(index, placeHolder);
							moveToUnderlay.setSelectedIndex(index);				
						}						
						
						if(otherDeletedValue.equals((String) overlay.get(i)))
						{
							overlay.removeElementAt(i);
						}
					}
				
				}			
			}
			catch(Exception e)
			{				
				for(int i = 0; i < overlay.getSize(); i++)
				{
					if(overlay.get(i).equals(underlay.get(moveToUnderlay.getSelectedIndex())))
					{
						overlay.removeElementAt(i);
					}					
				}
								
				System.out.println("It was caught: line 426 of MapServer ConfigWindow");				
			}
		}		
		
		
		int action;
		
		public UnderlayTransferHandler(int action) 
		{             
			this.action = action;        
		}                   
		
		public boolean canImport(TransferHandler.TransferSupport support) 
		{             
			// for the demo, we'll only support drops (not clipboard paste)             
			if (!support.isDrop()) 
			{                 
				return false;            
			}               
			
			// we only import Strings             
			
			if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) 
			{                 
				return false;             
			}               
			
			boolean actionSupported = (action & support.getSourceDropActions()) == action;             
			
			if (actionSupported) 
			{                 
				support.setDropAction(action);                 
				return true;             
			}               
			return false;         
			
		}           
		
		public boolean importData(TransferHandler.TransferSupport support) 
		{             
			// if we can't handle the import, say so             
			if (!canImport(support)) 
			{                 
				return false;             
			}               
			// fetch the drop location             
			
			JList.DropLocation dl = (JList.DropLocation)support.getDropLocation();               
			int index = dl.getIndex();               
			// fetch the data and bail if this fails             
			String data;             
			try 
			{                 
				data = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);             
			} 
			catch (UnsupportedFlavorException e) 
			{                 
				return false;             
			} 
			catch (java.io.IOException e) 
			{                 
				return false;             
			}               
			
			JList list = (JList)support.getComponent();             
			DefaultListModel model = (DefaultListModel)list.getModel();             
			model.insertElementAt(data, index);               
			Rectangle rect = list.getCellBounds(index, index);             
			list.scrollRectToVisible(rect);             
			list.setSelectedIndex(index);             
			list.requestFocusInWindow();               
			return true;         
		}       
	}    
    
    
    
    
    
    
    
}


/*
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
*/