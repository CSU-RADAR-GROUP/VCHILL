package edu.colostate.vchill;

import edu.colostate.vchill.ChillDefines.Mode;
import edu.colostate.vchill.bookmark.BookmarkControl;
import edu.colostate.vchill.gui.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * The class containing the main() method used to run VCHILL.
 *
 * @author Justin Carlson
 * @author Jochen Deyke
 * @author jpont
 * @author Joseph C. Hardin
 * @version 2014-11-24
 */
public class Loader {
    /**
     * Private default constructor prevents instantiation
     */
    private Loader() {
    }

    private static final int adminPort = 2500;

    private static final String eduBookmarkURL = "http://chill.colostate.edu/java/bookmarks-edu.xml";
    private static final String sysBookmarkURL = "http://chill.colostate.edu/java/bookmarks-sys.xml";

    /**
     * Prints usage message and exits.
     */
    private static void die() {
        System.err.println("Usage: java -jar vchill.jar [-bookmark <category>,\"<name> <scantype>\"]");
        System.err.println("    [-plot <list>] [-ascope <list>] [-numerical <list>]");
        System.err.println("    [-realtime server:port] [-save off|<format>,<tilt>,<to>]");
        System.err.println("    [-gui on|off] [-noisered on|off] [-range <km>] [-grid <km>]");
        System.err.println("    [-smoothing on|off] [-mode <mode>] [-center <x>,<y>]");
        System.err.println("    [-range <km>] [-rhistretch <factor> [-sweep <sweep>]");
        System.err.println("  list: comma separated moment names (eg. \"dBZ,NCP\")");
        System.err.println("  format: png|kmz|both");
        System.err.println("  tilt: sweeps to save: all|web");
        System.err.println("  to: ram|<path>");
        System.err.println("  mode: Ray|Sweep|Volume|Continuous");
        System.err.println("  factor: desired magnification, decimal OK");
        System.err.println("  sweep: desired sweep, including complete '*' separated path");
        System.err.println("  Do NOT specify a bookmark if using realtime mode");
        System.exit(1);
    }

    /**
     * Parse command line arguments and load specified settings into config
     *
     * @param args   the command line arguments to parse
     * @param config the Config instance to store settings in
     * @return an array of arrays containing bookmark, plot, ascope, numeric, and sweep details:
     * bookmark: {category, name}
     * plot: plot windows to open
     * ascope: ascope windows to open
     * numeric: numerical windows to open
     * sweep: sweeps to display
     */
    private static String[][] parseCommandLineArguments(final String[] args, final Config config) {
        final String[][] a = new String[5][]; //bookmark, plot, ascope, numeric, sweep
        // So many times no...why not just use something like optparse? TODO: Fix this Monstrosity!
        if (args.length % 2 != 0) die();
        for (int i = 0; i < args.length; i += 2) {
            if (args[i].equals("-bookmark")) {
                a[0] = args[i + 1].split(",");
                assert a[0].length == 2;
            } else if (args[i].equals("-sweep")) {
                a[4] = args[i + 1].split(",");
            } else if (args[i].equals("-plot")) {
                a[1] = args[i + 1].split(",");
            } else if (args[i].equals("-ascope")) {
                a[2] = args[i + 1].split(",");
            } else if (args[i].equals("-numerical")) {
                a[3] = args[i + 1].split(",");
            } else if (args[i].equals("-realtime")) {
                config.setDirName(null);
                config.setSocketName(null);
                config.setControlName(null);
                config.setRealtimeName(args[i + 1]);
            } else if (args[i].equals("-mode")) {
                try {
                    Mode mode = Mode.valueOf(Mode.class, args[i + 1]);
                    config.setPlottingMode(mode);
                } catch (IllegalArgumentException iae) {
                    System.out.println("Bad mode: " + args[i + 1]);
                    die();
                }
            } else if (args[i].equals("-range")) {
                config.setPlotRange(Double.parseDouble(args[i + 1]));
            } else if (args[i].equals("-grid")) {
                config.setGridSpacing(Integer.parseInt(args[i + 1]));
            } else if (args[i].equals("-center")) {
                String[] tmp = args[i + 1].split(",");
                config.setCenterX(Double.parseDouble(tmp[0]));
                config.setCenterY(Double.parseDouble(tmp[1]));
            } else if (args[i].equals("-rhistretch")) {
                config.setRHIHVFactor(Double.parseDouble(args[i + 1]));
            } else if (args[i].equals("-smoothing")) {
                if (args[i + 1].equals("off")) config.setSmoothingEnabled(false);
                else if (args[i + 1].equals("on")) config.setSmoothingEnabled(true);
                else die();
            } else if (args[i].equals("-noisered")) {
                if (args[i + 1].equals("off")) config.setNoiseReductionEnabled(false);
                else if (args[i + 1].equals("on")) config.setNoiseReductionEnabled(true);
                else die();
            } else if (args[i].equals("-gui")) {
                if (args[i + 1].equals("off")) config.setGUIEnabled(false);
                else if (args[i + 1].equals("on")) config.setGUIEnabled(true);
            } else if (args[i].equals("-save")) {
                String[] tmp = args[i + 1].split(",");
                if (tmp.length == 1 && tmp[0].equals("off")) {
                    config.setImageAutosaveEnabled(false);
                } else if (tmp.length == 3) {
                    config.setImageAutosaveEnabled(tmp[0].equals("png") || tmp[0].equals("both"));
                    config.setImageAutoExportEnabled(tmp[0].equals("kmz") || tmp[0].equals("both"));
                    config.setSaveAllEnabled(tmp[1].equals("all"));
                    if (tmp[2].equals("ram")) {
                        config.setSaveToDiskEnabled(false);
                    } else {
                        config.setSaveToDiskEnabled(true);
                        config.setSaveToDiskPath(tmp[2]);
                    }
                } else {
                    die();
                }
            } else {
                die();
            }
        }

        return a;
    }

    /**
     * Process the result of the parseCommandLineArguments method.
     * Separate due to need to connect first.
     *
     * @param a an array of arrays containing bookmark, plot, ascope, numeric, and sweep details:
     *          bookmark: {category, name}
     *          plot: plot windows to open
     *          ascope: ascope windows to open
     *          numeric: numerical windows to open
     *          sweep: sweeps to display
     */
    private static final void processArguments(final String[][] a) {
        Config config = Config.getInstance();
        WindowManager wm = WindowManager.getInstance();
        wm.getMainWindow().setVisible(config.isGUIEnabled());
        if (a[1] != null) for (String name : a[1]) wm.createPlotWindow(name);
        if (a[2] != null) for (String name : a[2]) wm.createAScopeWindow(name);
        if (a[3] != null) for (String name : a[3]) wm.createNumDumpWindow(name);

        if (a[0] != null) { //bookmark to load
            if (a[0].length == 2) {
                ViewBookmarkBrowser.getInstance().selectBookmark(a[0][0], a[0][1]);
            } else {
                System.err.println("Can't load bookmark; bad format");
            }
        } else {
            //ensure center etc are applied properly
            wm.setCenterInKm();
            wm.clearScreen();
        }

        if (a[4] != null) for (String tmp : a[4]) {
            ControlMessage newMessage = new ControlMessage(tmp);
            ViewFileBrowserActions actions = ViewFileBrowser.getInstance().getActions();
            newMessage = actions.findSweep(newMessage);
            if (newMessage != null) {
                actions.changeSelection(newMessage);
            }
        }
    }

    /**
     * The start point for Java VCHILL.  Run this to start the display.
     *
     * @param args command line arguments to override default values or open windows/bookmarks/servers
     */
    public static void main(final String args[]) throws Exception {
        final BookmarkControl bmc = BookmarkControl.getInstance();
        final Config config = Config.getInstance();
        /*TODO: This part is causing issues. I need to rework it to fail more gracefully*/
        //check to see if VCHILL is already running
/*        try {
            final ServerSocket socket = new ServerSocket(adminPort, 50, InetAddress.getByName("localhost"));
            new Thread(new Runnable() { public void run () {
                while (true) { try { //check to see if another instance gets started
                    Socket inSocket = socket.accept();
                    ObjectInputStream inStream = new ObjectInputStream(inSocket.getInputStream());
                    String[] newArgs = (String[])inStream.readObject();
                    final String[][] a = parseCommandLineArguments(newArgs, config);
                    inStream.close();
                    inSocket.close();

                    EventQueue.invokeLater(new Runnable() { public void run () {
                        processArguments(a);
                    }});
                } catch (Exception e) { throw new Error(e); }}
            }}, "ServerThread").start();
        } catch (BindException be) { //socket in use -> already running
            //attempt to connect to already-running instance
            try{
            	Socket socket = new Socket("localhost", adminPort);
            System.out.println("connected to already running VCHILL instance");
            ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
            outStream.writeObject(args);
            outStream.flush();
            outStream.close();
            socket.close();
            System.exit(0);
            }catch(SocketTimeoutException e){
            	System.err.println("Could not pass arguments to currently running VCHILL session.\n)" +
            		"Additionally, could not bind to current server port. There may be some erratic behavior. Attempting to continue");
            }

        }
  */
        //parse commandline args
        final String[][] a = parseCommandLineArguments(args, config); //bookmark, plot, ascope, numeric
        final ProgressMonitor progressMon = new ProgressMonitor(null, "Java VCHILL Startup", "Connecting...", 0, 3);
        final boolean[] loadFailed = new boolean[1];
        loadFailed[0] = false;

        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                GUIUtil.setLnF();

                ViewMain vm = new ViewMain();
                DialogUtil.parent = vm.getDesktop();
                JFrame win = vm.getWindow();

                //Quit this app when the big window closes.
                win.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(final WindowEvent e) {
                        exit();
                    }
                });

                final edu.colostate.vchill.gui.Config gcc = edu.colostate.vchill.gui.Config.getInstance();

                win.pack();
                win.setBounds(gcc.getInset(), gcc.getInset(), gcc.getDefaultWidth(), gcc.getDefaultHeight());
                win.setExtendedState(Frame.MAXIMIZED_BOTH);
                System.out.println("VCHILL: " + Version.string + " Build Date: " + Version.buildDate);

                WindowManager wm = WindowManager.getInstance();
                wm.setMain(vm);

            }
        });

        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                progressMon.setMillisToDecideToPopup(500);
                progressMon.setProgress(0);
            }
        });

        if (!progressMon.isCanceled()) {
            ViewControl vc = ViewControl.getInstance();
            //connect before trying to load bookmarks
            int connectionType = config.getLastConnectionType();
            String archive = config.getDefaultSocketName();
            if (connectionType == Config.REALTIME_CONN || archive == null) { //realtime mode
                //Trigger the realtime connect action as if the user had selected
                //it from the file menu. This is done instead of calling the
                //appropriate connect method so that the user has a chance to cancel.
                ViewFileBrowserPopup.realtimeAction.actionPerformed(null);
            } else {
                if (archive.length() > 0) vc.connect(archive);
                String dir = config.getDefaultDirName();
                if (dir != null && dir.length() > 0) vc.addDirectory(dir);
            }
        }

        if (!progressMon.isCanceled()) EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                progressMon.setNote("Loading edu bookmarks...");
                progressMon.setProgress(1);
            }
        });

        if (!progressMon.isCanceled()) {
            try {
                URLConnection urlConn = new URL(eduBookmarkURL).openConnection();
                urlConn.setReadTimeout(5000);
                urlConn.setConnectTimeout(5000);
                urlConn.connect();
                bmc.load(urlConn.getInputStream(), "edu:");
            } catch (IOException ioe) {
                //progressMon.setNote("Loading edu bookmarks...Failed");
                loadFailed[0] = true;
                System.err.println("Failed to load bookmarks from " + eduBookmarkURL);
            } catch (IllegalArgumentException iae) {
                loadFailed[0] = true;
                System.err.println("Failed to load bookmarks: Java 8 Error. Hopefully fixed in future.");
            }

        }

        if (!progressMon.isCanceled() && loadFailed[0]) {
            progressMon.setNote("Loading edu bookmarks...Failed");
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
            }
            loadFailed[0] = false;
        }

        if (!progressMon.isCanceled()) EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                progressMon.setProgress(2);
                progressMon.setNote("Loading sys bookmarks...");
            }
        });

        if (!progressMon.isCanceled()) {
            try {
                URLConnection urlConn = new URL(sysBookmarkURL).openConnection();
                urlConn.setReadTimeout(5000);
                urlConn.setConnectTimeout(5000);
                urlConn.connect();
                bmc.load(urlConn.getInputStream(), "sys:");
            } catch (IOException ioe) {
                //progressMon.setNote("Loading sys bookmarks...Failed");
                loadFailed[0] = true;
                System.err.println("Failed to load bookmarks from " + sysBookmarkURL);
            }catch (IllegalArgumentException iae) {
                loadFailed[0] = true;
                System.err.println("Failed to load bookmarks: Java 8 Error. Hopefully fixed in future.");
            }
        }

        if (!progressMon.isCanceled() && loadFailed[0]) {
            progressMon.setNote("Loading sys bookmarks...Failed");
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
            }
            loadFailed[0] = false;
        }

        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                progressMon.setProgress(3);

                ViewControl vc = ViewControl.getInstance();
                bmc.load(new File(FileSystemView.getFileSystemView().getHomeDirectory(), "bookmarks.xml"));
                vc.loadColors(edu.colostate.vchill.color.Config.getInstance().getColorFileName());
                vc.loadMaps();

                processArguments(a);
            }
        });
    }

    /**
     * Saves the user's bookmarks and display settings
     * and terminates the program
     */
    public static void exit() {
        BookmarkControl.getInstance().save(new File(
                FileSystemView.getFileSystemView().getHomeDirectory(), "bookmarks.xml"));
        Config.getInstance().savePreferences();
        ScaleManager.getInstance().savePreferences();
        System.exit(0);
    }

    /**
     * Method to allow shortened lines when getting resources
     */
    public static URL getResource(final String resourceName) {
        return Loader.class.getClassLoader().getResource(resourceName);
    }
}
