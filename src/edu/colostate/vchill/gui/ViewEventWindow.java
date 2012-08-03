package edu.colostate.vchill.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * Displays text output inside the GUI.  Text sent to stdout is displayed
 * as plain text, while text sent to stderr is italicized.
 *
 * @author  Justin Carlson
 * @author  Alexander Deyke
 * @author  Jochen Deyke
 * @created Oct 09, 2002
 * @version 2006-05-11
 */
public class ViewEventWindow extends JScrollPane
{
    /**
   * 
   */
  private static final long serialVersionUID = -8304558476393001830L;
    private final JTextPane pane;

    /**
     * Constructor for the ViewEventWindow object
     *
     * @param size Desired preferred size
     */
    public ViewEventWindow (final Dimension size)
    {
        setPreferredSize(size);
        pane = new JTextPane();
        pane.setEditable(false);

        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style regular = pane.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");
        Style s = pane.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);

        setViewportView(pane);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        pane.addComponentListener(new ComponentAdapter() {
            public void componentResized (final ComponentEvent ce) {
                pane.setCaretPosition(Math.max(pane.getDocument().getLength() - 1, 0));
            }});
    }

    public void addMouseListener (final MouseListener l)
    {
        this.pane.addMouseListener(l);
        super.addMouseListener(l);
    }

    /**
     * Add a String to the pane.
     *
     * @param eventString A string describing the event to add
     * @param error If true, text is italicized
     */
    public synchronized void addEvent (final String eventString, final boolean error)
    {
        EventQueue.invokeLater(new Runnable() { public void run () {
            Document doc = pane.getDocument();
            Style style = pane.getStyle(error ? "italic" : "regular");
            try {
                doc.insertString(doc.getLength(), eventString, style);
                //pane.setCaretPosition(doc.getLength());
            } catch (BadLocationException ble) {
                System.err.println("Couldn't insert text.");
            }
        }});
    }
}
