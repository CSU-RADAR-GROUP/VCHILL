package edu.colostate.vchill;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

/**
 * This class contains static methods for displaying various dialogs.
 * @author  Jochen Deyke
 * @author  Alexander Deyke
 * @version 2007-09-10
 */
public final class DialogUtil
{
    private static final Config config = Config.getInstance();

    /**
     * The (default) parent for the dialogs to be displayed.
     * Dialogs are automatically minimized and restored along with their parent,
     * and modal dialogs prevent interaction with their parent.
     */
    static Component parent = null;

    /** Private default constructor prevents instantiation */
    private DialogUtil () {}

    /**
     * Shows an error message that blocks user interaction until cleared.
     * The window title is "Error".
     *
     * @param message the actual message to display, or the url of the message to display
     */
    public static void showErrorDialog (final String message)
    {
        showErrorDialog("Error", message);
    }

    /**
     * Shows an error message that blocks user interaction until cleared.
     * The window title can be specified.
     *
     * @param title the title for the dialog box
     * @param message the actual message to display, or the url of the message to display
     */
    public static void showErrorDialog (final String title, final String message)
    {
        showErrorDialog(parent, title, message);
    }

    /**
     * Shows an error message that blocks user interaction until cleared.
     * The window title can be specified.
     *
     * @param title the title for the dialog box
     * @param message the actual message to display, or the url of the message to display
     * @param parent the parent for the dialog
     */
    public static void showErrorDialog (final Component parent, final String title, final String message)
    {
        if (!config.isGUIEnabled()) { //text only
            System.err.println("Error: " + title);
            System.err.println(message);
        } else if (message.startsWith("http://")) { //html
            new HTMLDialog(parent, message, title, JOptionPane.ERROR_MESSAGE).setVisible(true);
        } else { //normal dialog
            JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows an informational message that does not interfere with
     * program operation.
     * The window title is "Help".
     *
     * @param message the actual message to display, or the url of the message to display
     */
    public static void showHelpDialog (final String message)
    {
        showHelpDialog("Help", message);
    }

    /**
     * Shows an informational message that does not interfere with
     * program operation.
     * The window title can be specified.
     *
     * @param title the title for the dialog box
     * @param message the actual message to display, or the url of the message to display
     */
    public static void showHelpDialog (final String title, final String message)
    {
        showHelpDialog(parent, title, message);
    }

    /**
     * Shows an informational message that does not interfere with
     * program operation.
     * The window title can be specified.
     *
     * @param title the title for the dialog box
     * @param message the actual message to display, or the url of the message to display
     * @param parent the parent for the dialog
     */
    public static void showHelpDialog (final Component parent, final String title, final String message)
    {
        if (!config.isGUIEnabled()) { //text only
            System.out.println("Help: " + title);
            System.out.println(message);
        } else if (message.startsWith("http://")) { //html
            HTMLDialog dialog = new HTMLDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
            dialog.setModal(false);
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
        } else { //normal dialog
            JDialog dialog = (new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE)).createDialog(parent, title);
            dialog.setModal(false);
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
        }
    }

    /**
     * Shows an informational message that does not interfere with
     * program operation.
     * The window title can be specified.
     *
     * @param title the title for the dialog box
     * @param source the url of the message to display
     */
    public static void showHelpDialog (final String title, final URL source)
    {
        showHelpDialog(parent, title, source);
    }

    /**
     * Shows an informational message that does not interfere with
     * program operation.
     * The window title can be specified.
     *
     * @param title the title for the dialog box
     * @param source the url of the message to display
     * @param parent the parent for the dialog
     */
    public static void showHelpDialog (final Component parent, final String title, final URL source)
    {
        if (!config.isGUIEnabled()) { //text only
            System.out.println("Help: " + title);
            System.out.println(source.toString());
        } else { //html
            HTMLDialog dialog = new HTMLDialog(parent, source, title, JOptionPane.INFORMATION_MESSAGE);
            dialog.setModal(false);
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
        }
    }

    /**
     * Shows an informational message that blocks user interaction until cleared.
     * The window title is "Information".
     *
     * @param message the actual message to display, or the url of the message to display
     */
    public static void showInformationDialog (final String message)
    {
        showInformationDialog("Information", message);
    }

    /**
     * Shows an informational message that blocks user interaction until cleared.
     * The window title can be specified.
     *
     * @param title the title for the dialog box
     * @param message the actual message to display, or the url of the message to display
     */
    public static void showInformationDialog (final String title, final String message)
    {
        showInformationDialog(parent, title, message);
    }

    /**
     * Shows an informational message that blocks user interaction until cleared.
     * The window title can be specified.
     *
     * @param title the title for the dialog box
     * @param message the actual message to display, or the url of the message to display
     * @param parent the parent for the dialog
     */
    public static void showInformationDialog (final Component parent, final String title, final String message)
    {
        if (!config.isGUIEnabled()) { //text only
            System.out.println("Information: " + title);
            System.out.println(message);
        } else if (message.startsWith("http://")) { //html
            new HTMLDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE).setVisible(true);
        } else { //normal dialog
            JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Shows a dialog for obtaining a single line of input.
     * The window title is "Input".
     *
     * @param prompt the string to prompt the user with
     */
    public static String showInputDialog (final String prompt)
    {
        return showInputDialog("Input", prompt);
    }

    /**
     * Shows a dialog for obtaining a single line of input.
     * The window title can be specified.
     *
     * @param title window title
     * @param prompt the string to prompt the user with
     */
    public static String showInputDialog (final String title, final String prompt)
    {
        return showInputDialog(parent, title, prompt);
    }

    /**
     * Shows a dialog for obtaining a single line of input.
     * The window title can be specified.
     *
     * @param title window title
     * @param prompt the string to prompt the user with
     * @param parent the parent for the dialog
     */
    public static String showInputDialog (final Component parent, final String title, final String prompt)
    {
        if (!config.isGUIEnabled()) { //text only
            System.out.println("Input: " + title);
            System.out.println(prompt);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String result = null;
            while (result == null) {
                try {
                    result = in.readLine();
                } catch (IOException ioe) {
                    System.err.println(ioe.toString());
                }
            }
            return result;
        } else { //normal dialog
            JOptionPane pane = new JOptionPane(prompt, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            pane.setWantsInput(true);
            JDialog dialog = pane.createDialog(parent, title);
            dialog.pack();
            dialog.setVisible(true);
            Object value = pane.getInputValue();
            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                return null;
            }
            return (String)value;
        }
    }

    /**
     * Shows a dialog for obtaining a single line of input.
     * The window title can be specified.
     *
     * @param title window title
     * @param prompt the string to prompt the user with
     * @param content the default contents of the input field
     */
    public static String showInputDialog (final String title, final String prompt, final String content)
    {
        return showInputDialog(parent, title, prompt, content);
    }

    /**
     * Shows a dialog for obtaining a single line of input.
     * The window title can be specified.
     *
     * @param parent the parent for the dialog
     * @param title window title
     * @param prompt the string to prompt the user with
     * @param content the default contents of the input field
     */
    public static String showInputDialog (final Component parent, final String title, final String prompt, final String content)
    {
        final JTextField text = new JTextField(content, 30);
        JOptionPane pane = new JOptionPane(new Object[] {prompt, text}, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        pane.setWantsInput(false);
        JDialog dialog = pane.createDialog(parent, title);
        dialog.pack();
        dialog.setVisible(true);
        Integer value = (Integer)pane.getValue();
        if (value == null || value.intValue() == JOptionPane.CANCEL_OPTION || value.intValue() == JOptionPane.CLOSED_OPTION)
        {
            return null;
        }
        return text.getText();
    }

    /**
     * Shows an input dialog that allows the user to enter multiple lines of text.
     * The window title is "Input".
     *
     * @param prompt the string to prompt the user with
     */
    public static String showMultilineInputDialog (final String prompt)
    {
        return showMultilineInputDialog("Input", prompt);
    }

    /**
     * Shows an input dialog that allows the user to enter multiple lines of text.
     * The window title can be specified.
     *
     * @param title window title
     * @param prompt the String to prompt the user with
     */
    public static String showMultilineInputDialog (final String title, final String prompt)
    {
        return showMultilineInputDialog(title, prompt, "");
    }

    /**
     * Shows an input dialog that allows the user to enter multiple lines of text.
     * The window title can be specified.
     *
     * @param title window title
     * @param prompt the String to prompt the user with
     * @param content the default contents of the input field
     */
    public static String showMultilineInputDialog (final String title, final String prompt, final String content)
    {
        return showMultilineInputDialog(parent, title, prompt, content);
    }

    /**
     * Shows an input dialog that allows the user to enter multiple lines of text.
     * The window title can be specified.
     *
     * @param parent the parent for the dialog
     * @param title window title
     * @param prompt the String to prompt the user with
     * @param content the default contents of the input field
     */
    public static String showMultilineInputDialog (final Component parent, final String title, final String prompt, final String content)
    {
        final JTextArea text = new JTextArea(content, 20, 40);
        JOptionPane pane = new JOptionPane(new Object[] { prompt, new JScrollPane(text) }, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        pane.setWantsInput(false);
        JDialog dialog = pane.createDialog(parent, title);
        dialog.pack();
        dialog.setVisible(true);
        Integer value = (Integer)pane.getValue();
        if (value == null || value.intValue() == JOptionPane.CANCEL_OPTION || value.intValue() == JOptionPane.CLOSED_OPTION)
        {
            return null;
        }
        return text.getText();
    }

    /**
     * Shows an option dialog box.
     * The window title is "Input".
     *
     * @param prompt the string to prompt the user with
     * @param possibleValues array of options to display
     */
    public static Object showOptionDialog (final String prompt, final Object[] possibleValues)
    {
        return showOptionDialog("Input", prompt, possibleValues);
    }

    /**
     * Shows an option dialog box.
     * The window title can be specified.
     *
     * @param title window title
     * @param prompt the string to prompt the user with
     * @param possibleValues array of options to display
     */
    public static Object showOptionDialog (final String title, final String prompt, final Object[] possibleValues)
    {
        return showOptionDialog(parent, title, prompt, possibleValues);
    }

    /**
     * Shows an option dialog box.
     * The window title can be specified.
     *
     * @param parent the parent for the dialog
     * @param title window title
     * @param prompt the string to prompt the user with
     * @param possibleValues array of options to display
     */
    public static Object showOptionDialog (final Component parent, final String title, final String prompt, final Object[] possibleValues)
    {
        return JOptionPane.showInputDialog(parent, prompt, title,
            JOptionPane.QUESTION_MESSAGE, null, possibleValues, possibleValues[0]);
    }

    /**
     * Shows an editable option dialog box.
     * The window title is "Input".
     *
     * @param prompt the string to prompt the user with
     * @param possibleValues array of options to display
     * @return the toString()ed item the user selected, or whatever was typed in
     */
    public static String showOptionInputDialog (final String prompt, final Object[] possibleValues)
    {
        return showOptionInputDialog("Input", prompt, possibleValues, possibleValues[0]);
    }

    /**
     * Shows an editable option dialog box.
     * The window title can be specified.
     *
     * @param title window title
     * @param prompt the string to prompt the user with
     * @param possibleValues array of options to display
     * @return the toString()ed item the user selected, or whatever was typed in
     */
    public static String showOptionInputDialog (final String title, final String prompt, final Object[] possibleValues)
    {
        return showOptionInputDialog(title, prompt, possibleValues, possibleValues[0]);
    }

    /**
     * Shows an editable option dialog box.
     * The window title can be specified.
     *
     * @param title window title
     * @param prompt the string to prompt the user with
     * @param possibleValues array of options to display
     * @param defaultValue the initial contents of the combobox
     * @return the toString()ed item the user selected, or whatever was typed in
     */
    public static String showOptionInputDialog (final String title, final String prompt, final Object[] possibleValues, final Object defaultValue)
    {
        return showOptionInputDialog(parent, title, prompt, possibleValues, defaultValue);
    }

    /**
     * Shows an editable option dialog box.
     * The window title can be specified.
     *
     * @param parent the parent for the dialog
     * @param title window title
     * @param prompt the string to prompt the user with
     * @param possibleValues array of options to display
     * @param defaultValue the initial contents of the combobox
     * @return the toString()ed item the user selected, or whatever was typed in
     */
    public static String showOptionInputDialog (final Component parent, final String title, final String prompt, final Object[] possibleValues, final Object defaultValue)
    {
        final JComboBox box = new JComboBox(possibleValues);
        box.setEditable(true);
        box.setSelectedItem(defaultValue);
        JOptionPane pane = new JOptionPane(new Object[] { prompt, box }, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        pane.setWantsInput(false);
        JDialog dialog = pane.createDialog(parent, title);
        dialog.pack();
        dialog.setVisible(true);
        Integer value = (Integer)pane.getValue();
        if (value == null || value.intValue() == JOptionPane.CANCEL_OPTION || value.intValue() == JOptionPane.CLOSED_OPTION)
        {
            return null;
        }
        return box.getSelectedItem().toString();
    }

    /**
     * Shows a dialog for obtaining a single line of input with obscured echo.
     * The window title is "Password".
     *
     * @param prompt the string to prompt the user with
     */
    public static String showPasswordDialog (final String prompt)
    {
        return showPasswordDialog("Password", prompt);
    }

    /**
     * Shows a dialog for obtaining a single line of input with obscured echo.
     * The window title can be specified.
     *
     * @param title window title
     * @param prompt the string to prompt the user with
     */
    public static String showPasswordDialog (final String title, final String prompt)
    {
        return showPasswordDialog(parent, title, prompt, null);
    }

    /**
     * Shows a dialog for obtaining a single line of input with obscured echo.
     * The window title can be specified.
     *
     * @param title window title
     * @param prompt the string to prompt the user with
     * @param content the default contents of the input field
     */
    public static String showPasswordDialog (final String title, final String prompt, final String content)
    {
        return showPasswordDialog(parent, title, prompt, content);
    }

    /**
     * Shows a dialog for obtaining a single line of input with obscured echo.
     * The window title can be specified.
     *
     * @param parent the parent for the dialog
     * @param title window title
     * @param prompt the string to prompt the user with
     * @param content the default contents of the input field
     */
    public static String showPasswordDialog (final Component parent, final String title, final String prompt, final String content)
    {
        if (!config.isGUIEnabled()) { //text only
            System.out.println("Password: " + title);
            char[] result = null;
            while (result == null) {
                try {
                    result = getPassword(System.in, prompt);
                } catch (IOException ioe) {
                    System.err.println(ioe.toString());
                }
            }
            return new String(result);
        } else { //normal dialog
            final JPasswordField text = new JPasswordField(content);
            text.setEchoChar('*');
            JOptionPane pane = new JOptionPane(new Object[] { prompt, text }, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            pane.setWantsInput(false);
            JDialog dialog = pane.createDialog(parent, title);
            dialog.pack();
            dialog.setVisible(true);
            Integer value = (Integer)pane.getValue();
            if (value == null || value.intValue() == JOptionPane.CANCEL_OPTION || value.intValue() == JOptionPane.CLOSED_OPTION)
            {
                return null;
            }
            return new String(text.getPassword());
        }
    }

    /**
     * Shows a dialog for obtaining two single lines of input, one with normal and one with obscured echo.
     * The window title can be specified.
     *
     * @param title window title
     * @param namePrompt the 1st string to prompt the user with
     * @param nameContent the default contents of the 1st input field
     * @param passPrompt the 2nd string to prompt the user with
     * @param passContent the default contents of the 2nd input field
     */
    public static String[] showLoginDialog (final String title, final String namePrompt, final String nameContent, final String passPrompt, final String passContent)
    {
        return showLoginDialog(parent, title, namePrompt, nameContent, passPrompt, passContent);
    }

    /**
     * Shows a dialog for obtaining two single lines of input, one with normal and one with obscured echo.
     * The window title can be specified.
     *
     * @param parent the parent for the dialog
     * @param title window title
     * @param namePrompt the 1st string to prompt the user with
     * @param nameContent the default contents of the 1st input field
     * @param passPrompt the 2nd string to prompt the user with
     * @param passContent the default contents of the 2nd input field
     */
    public static String[] showLoginDialog (final Component parent, final String title, final String namePrompt, final String nameContent, final String passPrompt, final String passContent)
    {
        if (!config.isGUIEnabled()) { //text only
            System.out.println("If you need to obtain a signon, visit:\n  http://chill.colostate.edu/cgi-bin/user_service");
            System.out.println("Input: " + title);
            System.out.println(namePrompt);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String nameResult = null;
            while (nameResult == null) {
                try { nameResult = in.readLine(); }
                catch (IOException ioe) { System.err.println(ioe.toString()); }
            }
            System.out.println("Password: " + title);
            char[] passResult = null;
            while (passResult == null) {
                try { passResult = getPassword(System.in, passPrompt); }
                catch (IOException ioe) { System.err.println(ioe.toString()); }
            }
            return new String[] { nameResult, new String(passResult) };
        } else { //normal dialog
            boolean properHTMLsupport = !System.getProperty("java.version").startsWith("1.5"); //need newer than 1.5 for proper support
            final JTextField name = new JTextField(nameContent, 10);
            final JPasswordField pass = new JPasswordField(passContent);
            final JCheckBox rememberpass = new JCheckBox("Save Password");

            if(ConfigUtil.getString("Password", null) != null) {
                rememberpass.setSelected(true);
            }

            pass.setEchoChar('*');
            final JPanel panel = new JPanel();
            final JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            panel.setLayout(new GridLayout(4,2, 5,10));
            panel.add(new JLabel(namePrompt, SwingConstants.RIGHT));
            panel.add(name);
            panel.add(new JLabel(passPrompt, SwingConstants.RIGHT));
            panel.add(pass);
            panel.add(new JLabel(" ", SwingConstants.RIGHT));
            panel.add(rememberpass);
            panel.add(new JLabel("If you need a signon, visit:", SwingConstants.RIGHT));
            if (properHTMLsupport) {
                final JButton userserv = new JButton(new AbstractAction("User Services") {
                    /**
                   * 
                   */
                  private static final long serialVersionUID = -7826677105074146774L;

                    public void actionPerformed (final ActionEvent ae) {
                        showHelpDialog(pane, "User Services", "http://chill.colostate.edu/cgi-bin/user_service");
                    }});
                panel.add(userserv);
            } else {
                final JTextField url = new JTextField("http://chill.colostate.edu/cgi-bin/user_service", 15);
                panel.add(url);
            }

            EventQueue.invokeLater(new Runnable() {
                public void run () {
                    name.requestFocusInWindow();
                }});

            name.addActionListener(new ActionListener() {
                public void actionPerformed (final ActionEvent ae) {
                    pass.requestFocusInWindow();
                }});

            pane.setWantsInput(false);
            JDialog dialog = pane.createDialog(parent, title);
            dialog.pack();
            dialog.setVisible(true);
            Integer value = (Integer)pane.getValue();
            if (value == null || value.intValue() == JOptionPane.CANCEL_OPTION || value.intValue() == JOptionPane.CLOSED_OPTION)
            {
                return null;
            }
            if (rememberpass.isSelected()) {
                ConfigUtil.put("Password", new String(pass.getPassword()));
            }
            else {
                ConfigUtil.put("Password", "");
            }
            return new String[]{name.getText(), new String(pass.getPassword())};
        }
    }

    /**
     * Shows an warning message that blocks user interaction until cleared.
     * The window title is "Warning".
     *
     * @param message the actual message to display, or the url of the message to display
     */
    public static void showWarningDialog (final String message)
    {
        showWarningDialog("Warning", message);
    }

    /**
     * Shows an warning message that blocks user interaction until cleared.
     * The window title can be specified.
     *
     * @param title the title for the dialog box
     * @param message the actual message to display, or the url of the message to display
     */
    public static void showWarningDialog (final String title, final String message)
    {
        showWarningDialog(parent, title, message);
    }

    /**
     * Shows an warning message that blocks user interaction until cleared.
     * The window title can be specified.
     *
     * @param title the title for the dialog box
     * @param message the actual message to display, or the url of the message to display
     * @param parent the parent for the dialog
     */
    public static void showWarningDialog (final Component parent, final String title, final String message)
    {
        if (!config.isGUIEnabled()) { //text only
            System.err.println("Warning: " + title);
            System.err.println(message);
        } else if (message.startsWith("http://")) { //html
            new HTMLDialog(parent, message, title, JOptionPane.WARNING_MESSAGE).setVisible(true);
        } else { //normal dialog
            JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * An <code>HTMLDialog</code> displays its message (an HTML page) in a scrollable <code>JEditorPane</code>.
     * Clicking a link within that page opens another <code>HTMLDialog<code> instance.
     */
    public static class HTMLDialog
    {
        protected final JEditorPane text = new JEditorPane();
        protected final JPanel root = new JPanel();
        protected JOptionPane pane;
        protected JDialog dialog;
        protected boolean modal;
        protected boolean alwaysOnTop;

        /**
         * Constructs an initially invisible HTMLDialog displaying the contents of the specified webpage.
         * If the page cannot be loaded, the URL itself is instead shown as plain text.
         * @param parent the parent for the dialog
         * @param source the URL to load the message from
         * @param title the title for the dialog
         * @param type the messageType as in <code>JOptionDialog</code>
         */
        public HTMLDialog (final Component parent, final String source, final String title, final int type)
        {
            try {
                this.init(parent, new URL(source), title, type);
            } catch (IOException ioe) {
                this.dialog = (this.pane = new JOptionPane(source, type)).createDialog(parent, title);
            }
        }

        /**
         * Constructs an initially invisible HTMLDialog displaying the contents of the specified webpage.
         * If the page cannot be loaded, the URL itself is instead shown as plain text.
         * @param parent the parent for the dialog
         * @param source the URL to load the message from
         * @param title the title for the dialog
         * @param type the messageType as in <code>JOptionDialog</code>
         */
        public HTMLDialog (final Component parent, final URL source, final String title, final int type)
        {
            try {
                this.init(parent, source, title, type);
            } catch (IOException ioe) {
                this.dialog = (this.pane = new JOptionPane(source, type)).createDialog(parent, title);
            }
        }

        /**
         * Does the actual construction work
         */
        private void init (final Component parent, final URL source, final String title, final int type) throws IOException
        {
            this.text.setContentType("text/html");
            this.text.setEditable(false);
            this.root.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            this.pane = new JOptionPane(this.root, type, JOptionPane.DEFAULT_OPTION);
            pane.setWantsInput(false);
            this.dialog = pane.createDialog(parent, title);
            this.text.setPage(source); //throws IOException
            this.text.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(final HyperlinkEvent he) {
                    if (he.getEventType() == EventType.ACTIVATED) {
                        HTMLDialog d = new HTMLDialog(parent, he.getURL(), title, type);
                        d.setModal(modal);
                        d.setAlwaysOnTop(alwaysOnTop);
                        d.setVisible(true);
                    }}});
            this.root.add(new JScrollPane(text));
            this.root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
            this.setPreferredSize(new Dimension(600, 450));
            this.setModal(true);
            this.setAlwaysOnTop(true);
            this.dialog.setResizable(true);
            this.dialog.setLocationRelativeTo(parent); //centered
        }

        public void setVisible (final boolean visible)
        {
            this.dialog.setVisible(visible);
        }

        public void setPreferredSize (final Dimension dim)
        {
            this.root.setPreferredSize(dim);
            this.dialog.pack();
        }

        public void setModal (final boolean modal)
        {
            this.dialog.setModal(this.modal = modal);
        }

        public void setAlwaysOnTop (final boolean alwaysOnTop)
        {
            this.dialog.setAlwaysOnTop(this.alwaysOnTop = alwaysOnTop);
        }
    }

    /**
     * Open an exported file using the system default program - will hopefully be unnecessary with java 6
     *
     * @param filename the path/name of the exported file
     */
    public static void open (final String filename)
    {
        try {
            final String osname = System.getProperty("os.name");
           String command;
            if (osname.startsWith("Windows")) { //ME not supported
                if (osname.indexOf("9") == -1) command = "cmd /c start"; //nt, 2k, xp
                else command = "start"; //9x
            } else if (osname.equals("Linux")) {
                command = "googleearth";
            } else if (osname.equals("Mac OS X")) {
                command = "open";
            } else {
                System.err.println("Operating system not supported");
                command = "rm";
            }
            Runtime.getRuntime().exec(command + " " + filename);
        } catch (IOException ioe) { ioe.printStackTrace(); }
    }

//--------------------------------------------------------------------------------------------------------//
//** Textmode password reading from http://java.sun.com/developer/technicalArticles/Security/pwordmask/ **//
//************* Should be replaced with Java 6 password reading ASAP *************************************//
//--------------------------------------------------------------------------------------------------------//

    /**
     * Prompts the user for a password and attempts to mask input with "*"
     * @param in stream to be used (e.g. System.in)
     * @param prompt The prompt to display to the user.
     * @return The password as entered by the user.
     */
    @SuppressWarnings("fallthrough") public static final char[] getPassword (InputStream in, final String prompt) throws IOException {
      MaskingThread maskingthread = new MaskingThread(prompt);
      Thread thread = new Thread(maskingthread, "MaskingThread");
      thread.start();
	
      char[] lineBuffer;
      char[] buf;
      int i;

      buf = lineBuffer = new char[128];

      int room = buf.length;
      int offset = 0;
      int c;

      loop:   while (true) {
         switch (c = in.read()) {
            case -1:
            case '\n':
               break loop;

            case '\r':
               int c2 = in.read();
               if ((c2 != '\n') && (c2 != -1)) {
                  if (!(in instanceof PushbackInputStream)) {
                     in = new PushbackInputStream(in);
                  }
                  ((PushbackInputStream)in).unread(c2);
                } else {
                  break loop;
                }

            default:
               if (--room < 0) {
                  buf = new char[offset + 128];
                  room = buf.length - offset - 1;
                  System.arraycopy(lineBuffer, 0, buf, 0, offset);
                  Arrays.fill(lineBuffer, ' ');
                  lineBuffer = buf;
               }
               buf[offset++] = (char) c;
               break;
         }
      }
      maskingthread.stopMasking();
      if (offset == 0) {
         return null;
      }
      char[] ret = new char[offset];
      System.arraycopy(buf, 0, ret, 0, offset);
      Arrays.fill(buf, ' ');
      return ret;
   }

    /**
     * This class attempts to erase characters echoed to the console.
     */
    private static class MaskingThread extends Thread {
       private volatile boolean stop;
       private char echochar = '*';

      /**
       *@param prompt The prompt displayed to the user
       */
       public MaskingThread(String prompt) {
          System.out.print(prompt);
       }

      /**
       * Begin masking until asked to stop.
       */
       public void run() {

          int priority = Thread.currentThread().getPriority();
          Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

          try {
             stop = true;
             while(stop) {
               System.out.print("\010" + echochar);
               try {
                  Thread.currentThread();
                  // attempt masking at this rate
                  Thread.sleep(1);
               }catch (InterruptedException iex) {
                  Thread.currentThread().interrupt();
                  return;
               }
             }
          } finally { // restore the original priority
             Thread.currentThread().setPriority(priority);
          }
       }

      /**
       * Instruct the thread to stop masking.
       */
       public void stopMasking() {
          this.stop = false;
       }
    }

//--------------------------------------------------------------------------------------------------------//
//** Textmode password reading from http://java.sun.com/developer/technicalArticles/Security/pwordmask/ **//
//************* Should be replaced with Java 6 password reading ASAP *************************************//
//--------------------------------------------------------------------------------------------------------//

}
