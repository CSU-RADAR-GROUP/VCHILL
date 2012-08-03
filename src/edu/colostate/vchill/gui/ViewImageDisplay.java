package edu.colostate.vchill.gui;

import edu.colostate.vchill.Config;
import edu.colostate.vchill.DialogUtil;
import edu.colostate.vchill.ScaleManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.SimpleDoc;
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import net.jmge.gif.Gif89Encoder;

/**
 * This class is the cycling image buffer described in the 
 * C-Requirements.  It will hold up a user specified number of
 * images and display them as the user clicks next and previous.
 *
 * Methods are synchronized on this.map to prevent conflict with
 * synchronized JPanel methods 
 *
 * @author  Justin Carlson
 * @author  Jochen Deyke
 * @author  jpont
 * @created August 15, 2003
 * @version 2010-08-30
 */
public class ViewImageDisplay extends JPanel
{
    /**
   * 
   */
  private static final long serialVersionUID = -2860693420417672170L;
    private static final ScaleManager sm = ScaleManager.getInstance();
    private static final Config config = Config.getInstance();

    private static final String GIF_COMMENT = "Created by Java VCHILL";
    
    private final File[] lastDir = new File[1];

    /** Selects from the different plotted images */
    private final JTextField imageNum;
    private final JTextField imageMax;
    private final JTextField imageLim;

    /** Selects from the different plotted images */
    private final JComboBox typeSelector;

    /** Contains the pane, display to the user */
    private JFrame imageFrame = null;

    /** Holds the lists of images */
    private final LinkedHashMap<String, ArrayList<SavedImage<BufferedImage>>> map = new LinkedHashMap<String, ArrayList<SavedImage<BufferedImage>>>(); //map of ArrayLists, one per type

    /** Holds the images */
    private ArrayList<SavedImage<BufferedImage>> images; //images of current type
        
    /** Maximum number of images to store for each data type */
    private int maxImages;
        
    /** Index of the current image to display */
    private int currIndex;

    /** Used to create a slideshow */
    private Timer loop;

    /**
     * Creates the panel etc to be used to display the images.
     */
    public ViewImageDisplay ()
    {
        setMaxImages(config.getDefaultImageMax());

        imageNum = new JTextField("0", 3);
        imageNum.addActionListener(new ActionListener() {
            public void actionPerformed (final ActionEvent ae) { try {
                setCurrIndex(Integer.parseInt(imageNum.getText()) - 1);
                repaint();
            } catch (NumberFormatException nfe) {}}});
        imageMax = new JTextField("0", 3);
        imageMax.setEnabled(false);
        imageLim = new JTextField(String.valueOf(this.maxImages), 3);
        imageLim.addActionListener(new ActionListener() {
            public void actionPerformed (final ActionEvent ae) { try {
                setMaxImages(Integer.parseInt(imageLim.getText()));
            } catch (NumberFormatException nfe) {}}});

        Object[] types = sm.getTypes().toArray();
        this.typeSelector = new JComboBox(types);

        currIndex = 0;

        this.loop = new Timer(200, new ActionListener() {
            public void actionPerformed (final ActionEvent ae) { next(); }});
        this.loop.setRepeats(true);
        this.loop.setInitialDelay(0);
        if (types.length > 0) this.selectType((String)types[0]);
        sm.addObserver(new Observer () {
            public void update (final Observable o, final Object arg) {
                if (arg == null) updateTypes();
                else typeSelector.addItem(arg);
            }});
    }

    public void updateTypes ()
    { synchronized (this.map) {
        this.typeSelector.removeAllItems();
        for (String type : sm.getTypes()) this.typeSelector.addItem(type);
    }}

    private void setCurrIndex (final int newIndex)
    { synchronized (this.map) {
		if( images == null ) {
			this.currIndex = 0;
			this.imageNum.setText( "0" );
		} else
			this.currIndex = newIndex % images.size();
    }}

    private void setMaxImages (final int maxImages)
    { synchronized (this.map) {
        this.maxImages = maxImages > 1 ? maxImages : 8;
		if( this.maxImages == 8 )
			this.imageLim.setText( "8" );
    }}

    private int getMaxImages ()
    { synchronized (this.map) {
        return this.maxImages;
    }}

    public void addImage (final Set<String> type, final SavedImage<BufferedImage> image)
    { synchronized (this.map) {
        this.addImage(type.iterator().next(), image);
    }}

    /**
     * Adds an image to the queue.  Currently it expects a buffered
     * image, this might be best changed to use any sort of image
     * file.
     *
     * @param type the data type to select
     * @param image The Image object that is going to be drawn.
     */
    public void addImage (String type, final SavedImage<BufferedImage> image)
    { synchronized (this.map) {
        if (type == null || image == null) return;
        if (config.isSaveToDiskEnabled()) {
            File file = new File(config.getSaveToDiskPath(), type + "." + image.getDescription() + ".png");
            System.out.println("Saving " + file.getName());
            saveImageToFile(image.getImage(), file);
        } else {
            this.selectType(type);
            while (images.size() >= this.maxImages) images.remove(0);
            images.add(image);
            imageNum.setText(String.valueOf(images.size() > 0 ? currIndex + 1 : 0));
            imageMax.setText(String.valueOf(images.size()));
            repaint();
        }
    }}

    /**
     * Move the image to the next in the buffer (or cycle) and repaint.
     */
    public void next ()
    { synchronized (this.map) {
        if (images == null || images.size() < 1) return;
        currIndex = (currIndex + 1) % images.size();
        imageNum.setText(String.valueOf(images.size() > 0 ? currIndex + 1 : 0));
        repaint();
    }}

    /**
     * This will return the actual image to draw using the current
     * image index to get it out of the list.
     *
     * @return the image referenced by the current index.
     */
    public BufferedImage getCurrentImage ()
    { synchronized (this.map) {
        if (images == null || images.size() < 1) return null;
        if (currIndex >= images.size()) currIndex = 0;
        return images.get(currIndex).getImage();
    }}

    /**
     * This will display the image "previous" to the one currently viewed in
     * the image buffer.
     */
    public void prev ()
    { synchronized (this.map) {
        if (images == null || images.size() < 1) return;
        if (currIndex > 0) --currIndex;
        else currIndex = images.size() - 1;
        imageNum.setText(String.valueOf(images.size() > 0 ? currIndex + 1 : 0));
        repaint();
    }}

    /**
     * This will bring up a popup of the image display, the user can then
     * use the next and previous buttons to navigate.
     *
     * @param sizeX requested width of the popup.
     * @param sizeY requested height of the popup.
     */
    public void displayImage (final int sizeX, final int sizeY)
    {
        if (imageFrame == null) {
            final JPanel encapsulate = new JPanel();
            encapsulate.setLayout(new BorderLayout());
            this.setPreferredSize(new Dimension(sizeX, sizeY - 26));
            encapsulate.add(this, BorderLayout.CENTER);
            final JPanel control = getControlPanel();
            control.setPreferredSize(new Dimension(480, 56));
            encapsulate.add(control, BorderLayout.SOUTH);

            if (images != null) {
                imageNum.setText(String.valueOf(images.size() > 0 ? currIndex + 1 : 0));
                imageMax.setText(String.valueOf(images.size()));
            }
            imageLim.setText(String.valueOf(getMaxImages()));

            imageFrame = new JFrame("Saved Images");
            imageFrame.setIconImage(GUIUtil.ICON);
            imageFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            imageFrame.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing (final WindowEvent e) { closeWindow(); }});
            imageFrame.setContentPane(encapsulate);
            imageFrame.pack();
            imageFrame.setLocationRelativeTo(WindowManager.getInstance().getMainWindow()); //centered
            imageFrame.setVisible(true);
        } else {
            imageFrame.toFront();
            //imageFrame.requestFocus();
            //repaint();
        }
    }

    @Override public void paintComponent (final Graphics g)
    {
        if (g == null) return;
        super.paintComponent(g);
        g.drawImage(getCurrentImage(), 0, 0, null);
    }

    /**
     * This will return a JPanel with all of the control objects in it.
     * For example, the next and previous, the listing methods etc.
     *
     * @return A JPanel with control options.
     */
    private JPanel getControlPanel ()
    {
        JButton button = null;
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel topRow = new JPanel();
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.X_AXIS));

        JPanel bottomRow = new JPanel();
        bottomRow.setLayout(new BoxLayout(bottomRow, BoxLayout.X_AXIS));

        JLabel imageLabel = new JLabel("Image ");
        imageLabel.setLabelFor(imageNum);
        imageLabel.setDisplayedMnemonic('i');
        JLabel limitLabel = new JLabel(" max)   ");
        limitLabel.setLabelFor(imageLim);
        limitLabel.setDisplayedMnemonic('m');
        topRow.add(imageLabel);
        topRow.add(imageNum);
        topRow.add(new JLabel(" of "));
        topRow.add(imageMax);
        topRow.add(new JLabel(" ("));
        topRow.add(imageLim);
        topRow.add(limitLabel);

        button = new JButton("Next");
        button.setMnemonic(KeyEvent.VK_N);
        button.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = 4584521262936562019L;

            public void actionPerformed (final ActionEvent e) {
                loop.stop(); next(); }});
        button.setToolTipText("Advance one image");
        topRow.add(button);

        button = new JButton("Prev");
        button.setMnemonic(KeyEvent.VK_P);
        button.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = -2059500850910679648L;

            public void actionPerformed (final ActionEvent e) {
                loop.stop(); prev(); }});
        button.setToolTipText("Go back one image");
        topRow.add(button);

        button = new JButton("Loop");
        button.setMnemonic(KeyEvent.VK_L);
        button.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = -7151401905075308439L;

            public void actionPerformed (final ActionEvent e) {
                if (loop.isRunning()) loop.stop();
                else loop.start();
            }});
        button.setToolTipText("Start/stop automatically advancing through images");
        topRow.add(button);

        button = new JButton("Speed");
        button.setMnemonic(KeyEvent.VK_S);
        button.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = -1001377916422030622L;

            public void actionPerformed (final ActionEvent e) {
                loop.stop();
                while (true) {
                    String str = DialogUtil.showInputDialog(imageFrame, "Input speed", "Please enter desired delay between images in milliseconds");
                    if (str == null) return;
                    try {
                        loop.setDelay(Integer.parseInt(str));
                        loop.setInitialDelay(0);
                        break;
                    } catch (NumberFormatException nfe) {
                        DialogUtil.showErrorDialog(imageFrame, "Error: Invalid input", "Please enter only integer values");
                    }
                }
                loop.start();
            }});
        button.setToolTipText("Input new speed and start advancing thorugh images");
        topRow.add(button);

        button = new JButton("Remove");
        button.setMnemonic(KeyEvent.VK_R);
        button.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = 4415890045581374769L;

            public void actionPerformed (final ActionEvent e) {
                loop.stop();
                if (images == null || images.size() < 1) return; //no image to remove
                images.remove(currIndex);
                currIndex = images.size() > 0 ? currIndex % images.size() : 0;
                imageNum.setText(String.valueOf(images.size() > 0 ? currIndex + 1 : 0));
                imageMax.setText(String.valueOf(images.size()));
                repaint();
            }});
        button.setToolTipText("Remove the currently displayed image");
        topRow.add(button);

        button = new JButton("Clear");
        button.setMnemonic(KeyEvent.VK_C);
        button.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = -7134180243004889251L;

            public void actionPerformed (final ActionEvent e) {
                loop.stop();
                currIndex = 0;
				if( images == null ) return;
			    images.clear();
                imageNum.setText(String.valueOf(images.size() > 0 ? currIndex + 1 : 0));
                imageMax.setText(String.valueOf(images.size()));
                repaint();
            }});
        button.setToolTipText("Remove all images of the current type");
        topRow.add(button);

        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setDisplayedMnemonic('t');
        typeLabel.setLabelFor(this.typeSelector);
        bottomRow.add(typeLabel);

        this.typeSelector.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = 8741340180851587765L;

            public void actionPerformed (final ActionEvent ae) {
                selectType((String)typeSelector.getSelectedItem());
                repaint(); }});
        this.typeSelector.setToolTipText("Show images of this data type");
        bottomRow.add(this.typeSelector);

        button = new JButton("Image to File");
        button.setMnemonic(KeyEvent.VK_F);
        button.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = 8160242673173152713L;

            public void actionPerformed (final ActionEvent e) {
                loop.stop();
				if( images == null || images.size() < 1 ) return;
                CheckBoxFileChooser chooser = new CheckBoxFileChooser("Printer friendly");
                chooser.setFileFilter(new ImageFileFilter());
                int returnVal = chooser.showSaveDialog(imageFrame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
		            lastDir[0] = file;
                    saveImageToFile(chooser.isSelected() ? switchBlackAndWhite(getCurrentImage()) : getCurrentImage(), file);
                }
            }});
        button.setToolTipText("Save the currently displayed image to a file");
        bottomRow.add(button);

        button = new JButton("Loop to Dir");
        button.setMnemonic(KeyEvent.VK_D);
        button.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = -5399632952029836390L;

            public void actionPerformed (final ActionEvent ae) {
                loop.stop();
				if( images == null || images.size() < 1 ) return;
                new Thread(new Runnable() { public void run () {
                    CheckBoxFileChooser chooser = new CheckBoxFileChooser("Printer friendly");
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int returnVal = chooser.showSaveDialog(imageFrame);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        int progress = 0;
			lastDir[0] = chooser.getSelectedFile();
                        String path = lastDir[0].getAbsolutePath() + File.separator;
                        String prefix =  path + typeSelector.getSelectedItem() + ".";
                        final ProgressMonitor progressMonitor = new ProgressMonitor(imageFrame, "Saving image to:", "", 0, images.size());
                        progressMonitor.setProgress(progress++);
                        progressMonitor.setMillisToDecideToPopup(500);
                        for (final SavedImage<BufferedImage> image : images) {
                            File file; int i = 0;
                            do {
                                file = new File(prefix + image.getDescription() + " " + i++ + ".png");
                            } while (file.exists());
                            String filename = file.getName();
                            progressMonitor.setNote(filename);
                            System.out.println("Saving " + filename);
                            Thread.yield();
                            if (progressMonitor.isCanceled()) return;
                            saveImageToFile(chooser.isSelected() ? switchBlackAndWhite(image.getImage()) : image.getImage(), file);
                            progressMonitor.setProgress(progress++);
                            Thread.yield();
                        }
                    }
                }}, "LoopToDirThread").start();
            }});
        button.setToolTipText("Save all images of this type to separate files");
        bottomRow.add(button);

        button = new JButton("Loop to GIF");
        button.setMnemonic(KeyEvent.VK_G);
        button.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = -5357969871557245856L;

            public void actionPerformed (final ActionEvent ae) {
                loop.stop();
				if( images == null || images.size() < 1 ) return;
                new Thread(new Runnable() { public void run () {
                    CheckBoxFileChooser chooser = new CheckBoxFileChooser("Printer friendly");
                    chooser.setFileFilter(new GifFileFilter());
                    int returnVal = chooser.showSaveDialog(imageFrame);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
			lastDir[0] = file;
                        if (!file.getName().toLowerCase().endsWith(".gif")) file = new File(file.getAbsolutePath() + ".gif");
                        try {
                            List<BufferedImage> i = new ArrayList<BufferedImage>(images.size());
                            for (final SavedImage<BufferedImage> image : images) {
                                i.add(chooser.isSelected() ? switchBlackAndWhite(image.getImage()) : image.getImage());
                            }
                            saveGif(i, file);
                        } catch (IOException ioe) {
                            try {
                                List<BufferedImage> i = new ArrayList<BufferedImage>(images.size());
                                for (final SavedImage<BufferedImage> image : images) {
                                    i.add(reduceColors(chooser.isSelected() ? switchBlackAndWhite(image.getImage()) : image.getImage()));
                                }
                                saveGif(i, file);
                                DialogUtil.showWarningDialog(imageFrame, "Automatically reduced colors",
                                    "GIF supports a maximum of 256 colors per file.  Because the image to be saved had more,\n" +
                                    "the colors have been automatically remapped.  This can produce undesireable dithering.\n" +
                                    "To avoid this effect, try to reduce the number of colors used when capturing images for\n" +
                                    "use in GIFs by disabling color interpolation or changing colors.");
                            } catch (IOException ioe2) {
                                ioe2.printStackTrace();
                                DialogUtil.showErrorDialog(imageFrame, "Failed to create animated GIF", ioe2.toString());
                            }
                        }
                    }
                }}, "LoopToGIFThread").start();
            }});
        button.setToolTipText("Save all images of this type to an animated GIF file");
        bottomRow.add(button);

        button = new JButton("Composite to GIF");
        button.setMnemonic(KeyEvent.VK_C);
        button.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = 3997373801386745039L;

            public void actionPerformed (final ActionEvent ae) {
                loop.stop();
				if( images == null || images.size() < 1 ) return;
                new Thread(new Runnable() { public void run () {
                    String otherType = (String)DialogUtil.showOptionDialog(imageFrame, "Composite animated GIF", "What data type do you want to composite the current type with?", sm.getTypes().toArray());
                    CheckBoxFileChooser chooser = new CheckBoxFileChooser("Printer friendly");
                    chooser.setFileFilter(new GifFileFilter());
                    int returnVal = chooser.showSaveDialog(imageFrame);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
			            lastDir[0] = file;
                        if (!file.getName().toLowerCase().endsWith(".gif")) file = new File(file.getAbsolutePath() + ".gif");
                        List<BufferedImage> comps = new ArrayList<BufferedImage>(images.size());
						ArrayList<SavedImage<BufferedImage>> bottomImages = map.get(otherType);
						if( bottomImages == null ) {
							DialogUtil.showErrorDialog( imageFrame, "Failed to create animated GIF",
								"There are no saved images for type " + otherType );
							return;
						}
                        for (int i = 0; i < images.size() && i < bottomImages.size(); ++i) {
                            BufferedImage top = images.get(i).getImage();
                            BufferedImage bottom = bottomImages.get(i).getImage();
                            BufferedImage comp = new BufferedImage(top.getWidth(), top.getHeight() * 2, top.getType());
                            Graphics g = comp.createGraphics();
                            g.drawImage(top, 0, 0, null);
                            g.drawImage(bottom, 0, top.getHeight(), null);
                            comps.add(chooser.isSelected() ? switchBlackAndWhite(comp) : comp);
                        }
                        try {
                            saveGif(comps, file);
                        } catch (IOException ioe) {
                            try {
                                List<BufferedImage> i = new ArrayList<BufferedImage>(comps.size());
                                for (final BufferedImage image : comps) {
                                    i.add(reduceColors(image));
                                }
                                saveGif(i, file);
                                DialogUtil.showWarningDialog(imageFrame, "Automatically reduced colors",
                                    "GIF supports a maximum of 256 colors per file.  Because the image to be saved had more,\n" +
                                    "the colors have been automatically remapped.  This can produce undesireable dithering.\n" +
                                    "To avoid this effect, try to reduce the number of colors used when capturing images for\n" +
                                    "use in GIFs by using a quality setting of \"High\" or lower and/or disabling color\n" + 
                                    "interpolation.");
                            } catch (IOException ioe2) {
                                ioe2.printStackTrace();
                                DialogUtil.showErrorDialog(imageFrame, "Failed to create animated GIF", ioe2.toString());
                            }
                        }
                    }
                }}, "CompositeToGIFThread").start();
            }});
        button.setToolTipText("Save all images of this type and another to a single animated GIF file");
        bottomRow.add(button);

        button = new JButton("Load");
        button.setMnemonic(KeyEvent.VK_O);
        button.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = -3441419339433777977L;

            public void actionPerformed (final ActionEvent e) {
                loop.stop();
                JFileChooser chooser = new JFileChooser(lastDir[0]);
                chooser.setFileFilter(new ImageFileFilter());
                chooser.setMultiSelectionEnabled(true) ;
                int returnVal = chooser.showOpenDialog(imageFrame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] selected = chooser.getSelectedFiles();
		    if (selected.length > 0) lastDir[0] = selected[0];
                    for (int i = 0; i < selected.length; ++i) {
                        String[] filename = selected[i].getName().split("\\."); //type.description.filetype
                        String type = lookupType(filename[0]);
                        try { addImage(type, new SavedImage<BufferedImage>(ImageIO.read(selected[i]),
                                filename.length > 1 ? filename[1] : filename[0]));
                        } catch (IOException ioe) { System.err.println(ioe); }
                    }
                    currIndex = 0;
					if( images == null || images.size() < 1 ) return;
                    imageNum.setText(String.valueOf(images.size() > 0 ? currIndex + 1 : 0));
                    imageMax.setText(String.valueOf(images.size()));
                    repaint();
                }
            }});
        button.setToolTipText("Load one ore more previously saved images");
        bottomRow.add(button);

        button = new JButton("Exit");
        button.setMnemonic(KeyEvent.VK_X);
        button.addActionListener(new AbstractAction() {
            /**
           * 
           */
          private static final long serialVersionUID = 6476551648402562464L;

            public void actionPerformed (final ActionEvent ae) {
                loop.stop(); closeWindow(); }});
        button.setToolTipText("Close this window");
        bottomRow.add(button);

        controlPanel.add(topRow);
        controlPanel.add(bottomRow);

        return controlPanel;
    }

    /**
     * @param type the String to check
     * @return the entry matching the input; if none match, the current selection
     */
    private String lookupType (final String type)
    {
        for (int i = 0; i < typeSelector.getItemCount(); ++i) {
            if (typeSelector.getItemAt(i).toString().equals(type)) return (String)typeSelector.getItemAt(i);
        }

        return (String)typeSelector.getSelectedItem();
    }

    /**
     * @param type the data type to select
     * @return this.images, now set to the desired type
     */
    private ArrayList<SavedImage<BufferedImage>> selectType (final String type)
    { synchronized (this.map) {
        ArrayList<SavedImage<BufferedImage>> currType = this.map.get(type);
        if (currType == null) {
            currType = new ArrayList<SavedImage<BufferedImage>>();
            this.map.put(type, currType);
        }
        this.typeSelector.setSelectedItem(type);
		this.images = currType;
		imageNum.setText(String.valueOf(images.size() > 0 ? currIndex + 1 : 0));
        imageMax.setText(String.valueOf(images.size()));
        return this.images;
    }}

    private void closeWindow ()
    {
        imageFrame.dispose();
        imageFrame = null;
    }

    /**
     * Saves an image (such as those stored in the cyclic image buffer)
     * to a file on the disk
     *
     * @param image the image to save
     * @param file the file to save to
     */
    public void saveImageToFile (final BufferedImage image, final File file)
    {
        String filename = file.getName();
        try {
            String extension = filename.substring(filename.lastIndexOf('.') + 1, filename.length()).toLowerCase();
            if (extension.equals("png") || extension.equals("gif") || extension.equals("jpg") || extension.equals("jpeg")) {
		ImageIO.write(image, extension, file);
	    } else if (extension.equals("ps") || extension.equals("eps")) {
		PrintableImage printableImage = new PrintableImage(image,
			file,
			DocFlavor.SERVICE_FORMATTED.PRINTABLE,
			DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType());
//            } else if (extension.equals("gif")) {
//                try {
//                    saveGif(image, file);
//                } catch (IOException ioe) {
//                    try {
//                        saveGif(reduceColors(image), file);
//                        DialogUtil.showWarningDialog(imageFrame, "Automatically reduced colors",
//                            "GIF supports a maximum of 256 colors per file.  Because the image to be saved had more,\n" +
//                            "the colors have been automatically remapped.  This can produce undesireable dithering.\n" +
//                            "To avoid this effect, try to reduce the number of colors used when capturing images for\n" +
//                            "use in GIFs by using a quality setting of \"High\" or lower and/or disabling color\n" + 
//                            "interpolation.");
//                    } catch (IOException ioe2) {
//                        ioe2.printStackTrace();
//                        DialogUtil.showErrorDialog(imageFrame, "Failed to create GIF", ioe2.toString());
//                    }
//                }
            } else {
                ImageIO.write(image, "png", new File(file.getParent(), (filename = (filename + ".png"))));
            }
        } catch (Exception e) {
            System.err.println("Exception while saving file " + filename + ": " + e);
        }
    }

    public static void saveGif (final BufferedImage image, final File file) throws IOException
    {
        Gif89Encoder gifenc = new Gif89Encoder(image);
        gifenc.setComments(GIF_COMMENT);
        gifenc.setTransparentIndex(-1);
        gifenc.getFrameAt(0).setInterlaced(false);
        gifenc.encode(new FileOutputStream(file));
    }

    public void saveGif (final List<BufferedImage> images, final File file) throws IOException
    {
        int progress = 0;
        final ProgressMonitor progressMonitor = new ProgressMonitor(imageFrame, "Saving image to: " + file, "", 0, images.size());
        progressMonitor.setMillisToDecideToPopup(500);
        Gif89Encoder gifenc = new Gif89Encoder();
        for (final BufferedImage image : images) {
            if (progressMonitor.isCanceled()) return;
            gifenc.addFrame(image);
            progressMonitor.setProgress(progress++);
            Thread.yield();
        }
        gifenc.setComments(GIF_COMMENT);
        gifenc.setLoopCount(0); //infinite
        gifenc.setUniformDelay(loop.getDelay() / 10); //centiseconds
        gifenc.encode(new FileOutputStream(file));
        progressMonitor.setProgress(progress++);
        Thread.yield();
        System.out.println("Animated GIF saved");
    }

    public static BufferedImage switchBlackAndWhite (final BufferedImage image)
    {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int w = 0; w < image.getWidth(); ++w) for (int h = 0; h < image.getHeight(); ++h) {
            int rgb = image.getRGB(w, h);
            if (rgb == Color.WHITE.getRGB()) rgb = Color.BLACK.getRGB();
            else if (rgb == Color.BLACK.getRGB()) rgb = Color.WHITE.getRGB();
            result.setRGB(w, h, rgb);
        }
        return result;
    }

    public static BufferedImage reduceColors (final BufferedImage image)
    {
        BufferedImage indexedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        indexedImage.createGraphics().drawImage(image, 0, 0, null);
        return indexedImage;
    }

	/** A file filter for accepting GIF files. */
    protected static class GifFileFilter extends FileFilter
    {
        public boolean accept (final File file)
        {
            if (file.isDirectory()) return true;

            String filename = file.getName().toLowerCase();
            return filename.endsWith(".gif");
        }

        public String getDescription ()
        {
            return "GIF images";
        }
    }

	/** A file filter for accepting a file in any of the accepted formats. */
    protected static class ImageFileFilter extends FileFilter
    {
        public boolean accept (final File file)
        {
            if (file.isDirectory()) return true;

            String filename = file.getName().toLowerCase();
            if (filename.endsWith(".gif") ||
                filename.endsWith(".jpg") ||
                filename.endsWith(".jpeg") ||
                filename.endsWith(".png") ||
		filename.endsWith(".ps") ||
		filename.endsWith(".eps")) {
                return true;
            }

            return false;
        }

        public String getDescription ()
        {
            return "GIF, Jpeg, PNG, PS and EPS images";
        }
    }

    protected class CheckBoxFileChooser extends JFileChooser
    {
        /**
       * 
       */
      private static final long serialVersionUID = -1001729783408270200L;
        protected final JCheckBox checkBox;

        public CheckBoxFileChooser (final String label)
        {
            super(lastDir[0]);
            checkBox = new JCheckBox(label);
            checkBox.setToolTipText("Flip black and white to conserve ink when printed");
            super.setAccessory(checkBox);
        }

        public boolean isSelected ()
        {
            return this.checkBox.isSelected();
        }
    }
    
    private class PrintableImage implements Printable
    {
	    private BufferedImage image;
	    
	    public PrintableImage (BufferedImage image, File file, DocFlavor flavor, String mimeType) throws Exception
	    {
		    this.image = image;

		    //find a suitable print service to use
		    StreamPrintServiceFactory[] factories =
		   	    StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
				    flavor, mimeType);
		    if( factories.length == 0 )
		    {
		  	    DialogUtil.showErrorDialog(imageFrame,
				    "Failed to create image",
				    "could not find suitable print service");
			    return;
		    }
		
		    FileOutputStream fileOut = new FileOutputStream(file);
		    StreamPrintService sps = factories[0].getPrintService(fileOut);
		    DocPrintJob printJob = sps.createPrintJob();
		
		    //create the attributes
		    //PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
		
		    //create the printable image and print it
		    Doc imageDoc = new SimpleDoc(this, flavor, null);
		    printJob.print(imageDoc, null);
		    fileOut.close();
	    }
	    
	    public int print (Graphics g, PageFormat pf, int pageIndex)
	    {
		    if( pageIndex == 0 )
		    {
			    Graphics2D g2d = (Graphics2D) g;
			    g2d.translate( pf.getImageableX(), pf.getImageableY() );
			    g2d.drawImage( image, null, 0, 0 );
			    return Printable.PAGE_EXISTS;
		    } else {
			    return Printable.NO_SUCH_PAGE;
		    }
	    }
    }
}
