package edu.colostate.vchill.proxy;

import edu.colostate.vchill.socket.SocketArchCtl.Command;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Program to connect to proxy server and tell it to shut down
 *
 * @author Jochen Deyke
 * @version 2004-12-07
 */
final class ProxyShutdown {
    static String serverName = "localhost";
    static int serverPort = 2510;
    static String password = "secret";

    /**
     * Private default constructor prevents instantiation
     */
    private ProxyShutdown() {
    }

    public static void main(final String[] args) throws IOException {
        parseCommandLineArguments(args);
        Socket socket = new Socket(serverName, serverPort);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeInt(Command.HALT_COMMAND.ordinal());
        out.writeUTF(password);
        System.exit(0);
    }

    /**
     * Sets the global setting variables according to the arguments
     * passed in.  If an error is encountered, the application is
     * terminated.
     *
     * @param args The command line arguments as passed to main
     */
    private static void parseCommandLineArguments(final String[] args) {
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
            } else if (args[i].equals("-password") || args[i].equals("-p")) {
                password = args[i + 1];
            } else {
                die();
            }
        }
    }

    /**
     * Prints usage message and exits.
     */
    private static void die() {
        System.err.println("Usage: java ProxyShutdown [-server name] [-serverport port]");
        System.err.println("    [-password pwd]");
        System.err.println("Defaults: radar.chill.colostate.edu 2510 secret");
        System.err.println("server: what server the proxy to shut down is running on");
        System.err.println("serverport: what port the proxy to shut down is running on");
        System.err.println("password: the password matching the proxy's");
        System.exit(1);
    }
}
