package edu.colostate.vchill.file;

import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.TypedControlMessage;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.connection.Connection;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.io.File;

/**
 * Adapter class to make disk files act like a network connection.
 * This implementation is not synchronized; it must be synchronized externally.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-05-14
 */
public final class FileConnection extends Connection
{
    private Collection<String> dirList;

    /**
     * @param cache the cache shared by the entire backend 
     */
    public FileConnection (final CacheMain cache)
    {
        super(cache);
        this.dirList = new LinkedHashSet<String>();
        Thread prefetch = new Thread(new CacheThread(this), "FileCacheThread");
        prefetch.setDaemon(true);
        prefetch.setPriority(Thread.NORM_PRIORITY);
        prefetch.start();
    }

    @Override public boolean disconnect ()
    {
        return false; //always keep in list
    }
    
    @Override public void reconnect () {}
    
    @Override public boolean isConnected ()
    {
        return false;   //used so the menu doesn't enable the disconnect option
    }

    public void addFile (final File file)
    {
        String name = FileFunctions.getDecoratedName(file);
        if (name != null) this.dirList.add(name);
    }

    @Override public Collection<String> getDirectory (final ControlMessage key)
    {
        String dir = key.getDir();
        if (dir.length() <  1) return this.connected ? this.dirList : this.cache.getDirectory(key);
        return this.connected ? FileFunctions.getDirectory(dir) : this.cache.getDirectory(key);
    }

    @Override public Collection<String> getSweepList (final ControlMessage key)
    {
        return this.connected ? FileFunctions.getSweepList(key.getDir(), key.getFile()) : this.cache.getSweepList(key);
    }

    protected class CacheThread implements Runnable
    {
		private FileConnection fileConn;

		CacheThread (FileConnection fileConn)
		{
			this.fileConn = fileConn;
		}

        public void run ()
        {
            TypedControlMessage command;
            while (true) {
                do { //get new command
                    command = commands.peek();
                    if (command == null || //cache.getCompleteFlag(command.message, command.types) ||
                            command.message.getDir() == null || command.message.getFile() == null ||
                            command.message.getSweep() == null) {
                        commands.remove(command);
                        Thread.yield();
                    } else {
                        break;
                    }
                } while (true);
                try { //add rays to cache
                    FileFunctions.load(fileConn, command.message, cache);
                } catch (Exception e) {
					fileConn.setIsSweepDone( true );
                    System.out.println("Exception in FileConnection.CacheThread: " + e);
                    e.printStackTrace();
                }
                commands.remove(command); //done with request
            }
        }
    }
}
