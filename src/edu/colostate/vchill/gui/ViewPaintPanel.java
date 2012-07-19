package edu.colostate.vchill.gui;


import java.awt.*; import java.awt.event.*; import javax.swing.*;
import edu.colostate.vchill.Config;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.ViewControl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;


public class ViewPaintPanel extends JPanel implements ItemListener
{
    private final static ViewPaintPanel me = new ViewPaintPanel();
    private final ViewActions actions = new ViewActions();
    private final Config config = Config.getInstance();
    private final ViewControl vc = ViewControl.getInstance();
    private final ScaleManager sm = ScaleManager.getInstance();
    private final WindowManager wm = WindowManager.getInstance();
    private final JCheckBox annotationCheckBox;
    
    
    public static Boolean GreasePencilAnnotationEnabled = false;
    
    private static Color paintColor = new Color(255,255,255); //= new Color.black;
    
    // pen size in pixels
    private static int penSize = 2;
    
    
    public static JTextField PPIDisplayString;
 
    private JButton redButton;
    private JButton blackButton;
    private JButton greenButton;
    private JButton blueButton;
    
    private JButton clearButton;
    
    private JButton smallPenSizeButton;
    private JButton mediumPenSizeButton;
    private JButton largePenSizeButton;
    
    /**
     * This basic constructor will call the helper methods and setup the
     * size and layout of the inner components.  Private default constructor
     * prevents instantiation.
     */
    private ViewPaintPanel ()
    {
    	
    	GreasePencilAnnotationEnabled = false;
    	
    	setLayout(new BorderLayout()); 
    	
        JPanel panel = new JPanel(); 
        
        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.PAGE_AXIS));
        
        JPanel penSizePanel = new JPanel();
        
        JPanel checkBoxPanel = new JPanel();
        JPanel textFieldPanel = new JPanel();
        
        panel.setPreferredSize(new Dimension(70, 140)); 
        panel.setMinimumSize(new Dimension(40, 75));  
        panel.setMaximumSize(new Dimension(40, 75));  
          
        clearButton = new JButton("Clear"); 
        annotationCheckBox = new JCheckBox("Enable Annotation Capability");
        annotationCheckBox.addItemListener(this);
        
        clearButton.addActionListener(new ActionListener()
        { 
                public void actionPerformed(ActionEvent e)
                {                         
                        wm.clearAnnotationLayer();
                        wm.replotOverlay();
                        vc.rePlot();
                } 
        }); 
        
        redButton = new JButton("Red"); 

        redButton.addActionListener(new ActionListener()
        {  
                public void actionPerformed(ActionEvent e)
                {  
                        paintColor = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue());                        
                }  
        }); 

        blackButton = new JButton("Black"); 


        blackButton.addActionListener(new ActionListener()
        { 
                public void actionPerformed(ActionEvent e)
                {  
                        paintColor = new Color(Color.black.getRed(), Color.black.getGreen(), Color.black.getBlue());
                        
                        //paintColor = new Color(Color.black);
                } 

        }); 

        blueButton = new JButton("Blue"); 

        blueButton.addActionListener(new ActionListener()
        {  
                public void actionPerformed(ActionEvent e)
                {  
                        paintColor = new Color(Color.blue.getRed(), Color.blue.getGreen(), Color.blue.getBlue());
                } 
        }); 

        greenButton = new JButton("Green"); 

        greenButton.addActionListener(new ActionListener()
        { 
                public void actionPerformed(ActionEvent e)
                { 
                        paintColor = new Color(Color.green.getRed(), Color.green.getGreen(), Color.green.getBlue());                        
                } 
        }); 

        
        smallPenSizeButton = new JButton("Small");
        smallPenSizeButton.addActionListener(new ActionListener()
        { 
                public void actionPerformed(ActionEvent e)
                { 
                        penSize = 1;                        
                } 
        });

        mediumPenSizeButton = new JButton("Medium");
        mediumPenSizeButton.addActionListener(new ActionListener()
        { 
                public void actionPerformed(ActionEvent e)
                { 
                        penSize = 2;                        
                } 
        });        
        
        largePenSizeButton = new JButton("Large");
        largePenSizeButton.addActionListener(new ActionListener()
        { 
                public void actionPerformed(ActionEvent e)
                { 
                        penSize = 4;                        
                } 
        });        
        PPIDisplayString = new JTextField(20);
        
        checkBoxPanel.add(annotationCheckBox);
        colorPanel.add(greenButton);  
        colorPanel.add(blueButton);   
        colorPanel.add(blackButton); 
        colorPanel.add(redButton);  
        checkBoxPanel.add(clearButton);
        
        penSizePanel.add(smallPenSizeButton);
        penSizePanel.add(mediumPenSizeButton);
        penSizePanel.add(largePenSizeButton);

        textFieldPanel.add(PPIDisplayString);
        

		blueButton.setEnabled(GreasePencilAnnotationEnabled);
		blackButton.setEnabled(GreasePencilAnnotationEnabled);
		greenButton.setEnabled(GreasePencilAnnotationEnabled);
		redButton.setEnabled(GreasePencilAnnotationEnabled);
		
		clearButton.setEnabled(GreasePencilAnnotationEnabled);
		
		smallPenSizeButton.setEnabled(GreasePencilAnnotationEnabled);
		mediumPenSizeButton.setEnabled(GreasePencilAnnotationEnabled);
		largePenSizeButton.setEnabled(GreasePencilAnnotationEnabled);
		PPIDisplayString.setEnabled(GreasePencilAnnotationEnabled);        
        
        //add(panel, BorderLayout.CENTER); 
        add(colorPanel, BorderLayout.WEST);
        add(penSizePanel, BorderLayout.EAST);
        add(textFieldPanel, BorderLayout.SOUTH);
        add(checkBoxPanel, BorderLayout.NORTH);
        
        setVisible(true); 

        
    	
/*        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JSeparator()); //--------------------------------
        add(new JSeparator()); //--------------------------------
        add(new JSeparator()); //--------------------------------
*/    }

    public static ViewPaintPanel getInstance () { return me; }

    public void itemStateChanged(ItemEvent e)
    {
    	if(e.getStateChange() == ItemEvent.DESELECTED)
    	{
    		GreasePencilAnnotationEnabled = false;   		
    		
    	}
    	else
    		GreasePencilAnnotationEnabled = true;
    	
		blueButton.setEnabled(GreasePencilAnnotationEnabled);
		blackButton.setEnabled(GreasePencilAnnotationEnabled);
		greenButton.setEnabled(GreasePencilAnnotationEnabled);
		redButton.setEnabled(GreasePencilAnnotationEnabled);
		
		clearButton.setEnabled(GreasePencilAnnotationEnabled);
		
		smallPenSizeButton.setEnabled(GreasePencilAnnotationEnabled);
		mediumPenSizeButton.setEnabled(GreasePencilAnnotationEnabled);
		largePenSizeButton.setEnabled(GreasePencilAnnotationEnabled);
		PPIDisplayString.setEnabled(GreasePencilAnnotationEnabled);
		
    }
    
    
    public static Color getPaintColor()
    {
    	return paintColor;
    	
    }
    
    public static int getPenSize()
    {
    	return penSize;
    }

}

	
	

