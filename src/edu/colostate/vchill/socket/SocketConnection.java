package edu.colostate.vchill.socket;

import edu.colostate.vchill.Config;
import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.TypedControlMessage;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.connection.Connection;

import java.io.IOException;
import java.util.Collection;

/**
 * Caching archive socket connection for VCHILL's data acquisition backend.
 * This implementation is not synchronized; access must be synchronized
 * externally.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public final class SocketConnection extends Connection {
    private final SocketMain socket;
    private final String server;
    private final int port;

    /**
     * @param server      the name of the server to connect to
     * @param port        the port to connect on
     * @param cache       the cache shared by the entire backend
     * @param promptLogin indicates whether or not to ask for login info
     */
    public SocketConnection(final String server, final int port, final CacheMain cache, final boolean promptLogin) {
        super(cache);
        this.socket = new SocketMain(this, promptLogin);
        this.server = server;
        this.port = port;
        Thread prefetch = new Thread(new CacheThread(), "SocketCacheThread");
        prefetch.setDaemon(true);
        prefetch.setPriority(Thread.NORM_PRIORITY);
        prefetch.start();
    }

    @Override
    public void connect() throws IOException {
        this.socket.connect(this.server, this.port);
        if (this.server.equals("vchill.chill.colostate.edu")) {
            /*
			 * If the user is connecting to vchill then they are really going
			 * to connect to 2 servers (one on port 2510 and the other on 2513)
			 * but make the 2510 the default since it's what the user is
			 * familiar with.
			 */
            Config.getInstance().setDefaultSocketName(this.server + ":2510");
        } else
            Config.getInstance().setDefaultSocketName(this.server + ":" + this.port);
        this.connected = true;
        Config.getInstance().setLastConnectionType(Config.ARCHIVE_CONN);
    }

    @Override
    public boolean disconnect() throws IOException {
        this.socket.disconnect();
        this.connected = false;
        return true; //remove from browser
    }

    @Override
    public boolean isConnected() {
        return this.connected = this.socket.connected();
    }

    @Override
    public Collection<String> getDirectory(final ControlMessage key) throws IOException {
        return this.connected ? this.socket.getDirectory(key.getDir()) : this.cache.getDirectory(key);
    }

    @Override
    public Collection<String> getSweepList(final ControlMessage key) throws IOException {
        return this.connected ? this.socket.getSweepList(key.getDir(), key.getFile()) : this.cache.getSweepList(key);
    }

    protected class CacheThread implements Runnable {
        public void run() {
            while (true) {
                do { //get new command
                    TypedControlMessage command = commands.peek();
                    if (command == null ||
                            //cache.getCompleteFlag(command) ||
                            command.message.getURL() == null ||
                            command.message.getDir() == null ||
                            command.message.getFile() == null ||
                            command.message.getSweep() == null) {
                        commands.remove(command.message); //purge invalid entries
                        Thread.yield();
                    } else {
                        break;
                    } //valid command at front of queue
                } while (true);

                try {
                    socket.getSweep(commands, cache); //this removes entries from queue
                } catch (Exception e) {
                    setIsSweepDone(true);
                    System.out.println("Exception in SocketConnection.CacheThread: " + e);
                    e.printStackTrace();
                }
            }
        }
    }
}
