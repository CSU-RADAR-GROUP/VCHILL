package edu.colostate.vchill.gui;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyKrupp;
//import com.jgoodies.looks.windows.WindowsLookAndFeel;
import edu.colostate.vchill.Loader;
import java.awt.Dimension;
import java.awt.Image;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Utility class for setting up GUI functions for VCHILL 
 * @author Jochen Deyke
 * @version 2007-09-14
 */
public final class GUIUtil
{
    public static final Image ICON = new ImageIcon(Loader.getResource("resources/icons/sweepPPI.png")).getImage();

    /** private default constructor prevents instantiation */
    private GUIUtil () {}

    /**
     * Sets the Look and Feel to JGoodies' Windows on Windows,
     * and JGoodies' Plastic elsewhere.
     * If this should fail for whatever reason, simply returns silently.
     */
    public static void setLnF ()
    {
        System.out.println("Setting LnF");
        try { //set up look-and-feel
            UIManager.put("ClassLoader", LookUtils.class.getClassLoader());
            PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            try {
                UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel"); //fails on non-windows
            } catch (UnsupportedLookAndFeelException ulafe) {
                UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
            }
            //ClearLookManager.setMode(ClearLookMode.DEBUG);
        } catch (Exception e) { System.out.println(e.toString()); } //ignore; look-and-feel is not that important
    }

    /**
     * Opens a window to display text (obtained from stdout and stderr).
     * If this should fail for whatever reason, simply returns silently.
     *
     * @param frameName the title to use for the window
     * @return the opened window (or null on failure)
     */
    public static JFrame openWindow (final String frameName)
    {
        System.out.println("Starting GUI...");
        try { //divert stdout/err to a gui window
            JFrame frame = new JFrame(frameName);
            frame.setIconImage(ICON);
            ViewEventWindow win = new ViewEventWindow(new Dimension(640, 480));
            frame.setContentPane(win);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
            System.setOut(new EventStream(System.out, win, false));
            System.setErr(new EventStream(System.err, win, true));
            return frame;
        } catch (Exception e) { //ignore; automatic fallback to text
            return null;
        }
    }

    /**
     * Sets the Look and Feel, then opens a window with the specified title
     *
     * @param title the title for the opened window
     * @return the opened window (or null on failure)
     */
    public static JFrame startGUI (final String title)
    {
        setLnF();
        return openWindow(title);
    }

    public static JProgressBar addProgressBar (final JFrame win)
    {
        JProgressBar progressBar = new JProgressBar(0, 1);
        JPanel newRoot = new JPanel();
        newRoot.setLayout(new BoxLayout(newRoot, BoxLayout.Y_AXIS));
        newRoot.add(win.getContentPane());
        newRoot.add(progressBar);
        win.setContentPane(newRoot);
        win.pack();
        return progressBar;
    }
}
