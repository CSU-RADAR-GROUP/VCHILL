package edu.colostate.vchill.proxy;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ChillDefines.Channel;
import edu.colostate.vchill.cache.CacheMainLRU;
import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.gui.GUIUtil;
import edu.colostate.vchill.socket.SocketArchCtl.Command;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Driver class for an archive proxy server.  Listens to socket requests and
 * creates new threads to handle them.
 *
 * @author Alexander Deyke
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public final class ArchiveProxy extends Proxy {
    /**
     * list of ProxyControlThreads
     */
    private ArrayList<ProxyControlThread> controlThreads = new ArrayList<ProxyControlThread>();

    /**
     * private constructor prevents instantiation
     */
    private ArchiveProxy() {
    }

    public static void main(final String[] args) throws IOException {
        new ArchiveProxy().parseCommandLineArguments(args).run();
        System.exit(0);
    }

    public void run() throws IOException {
        if (guiflag) GUIUtil.startGUI("Java VCHILL Archive Proxy Server"); //gui requested

        // instantiate cache
        cache = new CacheMainLRU(cacheSize);
        ServerSocket clientsocket = null;
        try {
            clientsocket = new ServerSocket(listenPort);
            System.out.println("Proxy: Listening on port " + listenPort);
        } catch (IOException ioe) {
            System.err.println("Proxy: Could not listen on port " + listenPort);
            throw new Error(ioe);
        }

        while (true) { // repeat forever
            Socket socket = clientsocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            int fromSocket = in.readInt(); //hello
            if (fromSocket == Command.HALT_COMMAND.ordinal()) {
                String pwd = in.readUTF();
                if (password.equals(pwd)) {
                    System.out.println("Proxy server shutting down");
                    return;
                } else {
                    System.out.println("Shutdown attempt with bad password: " + pwd);
                    System.out.println("Proxy server NOT shutting down");
                }
            } else if (fromSocket == ChillDefines.HELLO) {
                fromSocket = in.readInt(); //channel
                if (fromSocket == Channel.ARCH_CTL.ordinal()) {
                    System.out.println("Proxy: Control socket request identified from " + socket.getInetAddress().getHostName());
                    ProxyControlThread controlThread = new ProxyControlThread(socket, this);
                    controlThreads.add(controlThread);
                    controlThread.start();
                } else if ((fromSocket & 0xffff) == Channel.GEN_MOM_DAT.ordinal()) {
                    int sessionID = (fromSocket >> 16) & 0xffff;
                    System.out.println("Proxy: Data socket request identified from " + socket.getInetAddress().getHostName());
                    removeDead(); // get rid of dead control threads first
                    for (ProxyControlThread controlThread : controlThreads) {
                        if (sessionID == controlThread.getSessionID()) {
                            ProxyDataThread dataThread = new ProxyDataThread(socket, controlThread, cache);
                            controlThread.addDataThread(dataThread);
                            dataThread.start();
                            break;
                        }
                    }
                } else {
                    System.out.println("Bad request from " + socket.getInetAddress().getHostName() + ": unknown channel type");
                }
            } else {
                System.out.println("Bad request from " + socket.getInetAddress().getHostName() + ": outdated version?");
            }

            Proxy.sleep(100);
        }
    }

    /**
     * Sets the global setting variables according to the arguments
     * passed in.  If an error is encountered, the application is
     * terminated.
     *
     * @param args The command line arguments as passed to main
     * @return this object; useful for chaining this method into creation
     */
    private ArchiveProxy parseCommandLineArguments(final String[] args) {
        //parse commandline arguments
        if (args.length % 2 != 0) die();
        for (int i = 0; i < args.length; i += 2) {
            if (args[i].equals("-server") || args[i].equals("-s")) {
                serverName = args[i + 1];
            } else if (args[i].equals("-serverport") || args[i].equals("-sp")) {
                try {
                    serverPort = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    die();
                }
                if (serverPort < 1 || serverPort > 65535) die();
            } else if (args[i].equals("-listenport") || args[i].equals("-lp")) {
                try {
                    listenPort = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    die();
                }
                if (listenPort < 1 || listenPort > 65535) die();
            } else if (args[i].equals("-timeout") || args[i].equals("-t") || args[i].equals("-to")) {
                try {
                    timeout = 60000l * Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    die();
                }
                if (timeout < 0) die();
            } else if (args[i].equals("-cachesize") || args[i].equals("-cs")) {
                try {
                    cacheSize = ChillFieldInfo.types.length * Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    die();
                }
                if (cacheSize < 0) die();
            } else if (args[i].equals("-calculation") || args[i].equals("-c")) {
                if (args[i + 1].equals("on")) {
                    calcflag = true;
                } else if (args[i + 1].equals("off")) {
                    calcflag = false;
                } else {
                    die();
                }
            } else if (args[i].equals("-password") || args[i].equals("-p")) {
                password = args[i + 1];
            } else if (args[i].equals("-gui") || args[i].equals("-g")) {
                if (args[i + 1].equals("on")) {
                    guiflag = true;
                } else if (args[i + 1].equals("off")) {
                    guiflag = false;
                } else {
                    die();
                }
            } else {
                die();
            }
        }
        addCalculatedTypeScales();
        return this;
    }

    /**
     * Looks at list of control threads and removes inactive ones from the list.
     */
    private void removeDead() {
        for (int i = controlThreads.size() - 1; i > 0; i--) {
            if (!(controlThreads.get(i)).isAlive()) {
                System.out.println("Proxy: Removing dead thread");
                controlThreads.remove(i);
            }
        }
    }

    /**
     * Prints usage message and exits.
     */
    private static void die() {
        System.err.println("Usage: java Proxy [-server name] [-serverport port] [-listenport port]");
        System.err.println("    [-timeout minutes] [-cachesize numsweeps] [-calculation on|off]");
        System.err.println("    [-password pwd] [-gui on|off]");
        System.err.println("Defaults: radar.chill.colostate.edu 2510 2510 60 20 on secret");
        System.err.println("server: what server the proxy should connect to");
        System.err.println("serverport: what port the proxy should connect to");
        System.err.println("listenport: what port the proxy should listen on");
        System.err.println("timeout: how long the control socket can idle before being terminated");
        System.err.println("    0 means forever");
        System.err.println("cachesize: size is in number of sweeps, each with " + ChillFieldInfo.types.length + " types");
        System.err.println("calculation: if the proxy should calculate hybrid data types");
        System.err.println("    Turn this off if you're connecting to another proxy");
        System.err.println("password: a password which must be matched to shut down the proxy");
        System.exit(1);
    }
}
