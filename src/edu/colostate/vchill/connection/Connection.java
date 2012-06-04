package edu.colostate.vchill.connection;

import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.ControlSyncQueue;
import edu.colostate.vchill.TypedControlMessage;
import edu.colostate.vchill.cache.CacheMain;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Abstract class representing connections to various data acquisition methods.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-04-08
 */
public abstract class Connection
{
    protected final ControlSyncQueue<TypedControlMessage> commands;
    protected final CacheMain cache; //shared cache
    protected boolean connected;
	protected boolean isSweepDone; //indicates that a sweep request has finished
    
    public Connection (final CacheMain cache)
    {
        this.commands = new ControlSyncQueue<TypedControlMessage>();
        this.cache = cache;
        this.connected = false;
		this.isSweepDone = true;
    }

    /**
     * Establishes a connection to this connection's server and port
     */
    public void connect () throws IOException
    {
        this.connected = true;
    }

    /**
     * Disconnects from this connection's server and port
     *
     * @return should this connection be removed from the browser?
     */
    public boolean disconnect () throws IOException
    {
        this.connected = false;
        return true; //allow connection to be removed by default
    }
    
    /**
     * Reconnects to this connection's server and port
     */
    public void reconnect () throws IOException
    {
        if (this.connected) this.disconnect();
        this.connect();
    }

    /**
     * Checks whether this connection is currently active
     *
     * @return true if the specified connection is active; false otherwise 
     */
    public boolean isConnected () throws IOException
    {
        return this.connected;
    }

    /**
     * Clears all enqueued requests
     */
    public void stop ()
    {
        this.commands.stop();
    }

    /**
     * Gets the list of contents (files and/or subdirectories) available in a given directory
     * @param key ControlMessage containing the directory, server and port (other fields are ignored)
     * @return a Collection of String objects containing all available (sub-)directories and files.
     * Directories are detected thorugh the presence of the suffix " DIR".
     */
    public abstract Collection<String> getDirectory (final ControlMessage key) throws IOException;

    /**
     * Gets the list of all sweeps available through this connection in a given file in a given directory
     *
     * @param key ControlMessage containing the file, directory, server and port (other fields are ignored)
     * @return a Collection of String objects containing all available sweeps
     */ 
    public abstract Collection<String> getSweepList (final ControlMessage key) throws IOException;

    public void requestSweep (final ControlMessage key, final Set<String> wanted)
    {
        if (!this.connected) return; //Can't request without a connection
        Set<String> needed = new LinkedHashSet<String>();
        for (String type : wanted) if (this.cache.isEmpty(key, type)) needed.add(type);
        if (needed.size() == 0) return;
		TypedControlMessage command = new TypedControlMessage(key, needed);
		//don't add the request if it already exists
		if( this.commands.isEmpty() || !this.commands.peek().equals(command) ) {
			setIsSweepDone( false );
			this.commands.add(command); //first request: enqueue a request for the data
		}
    }

	public void setIsSweepDone (final boolean isSweepDone)
	{
		this.isSweepDone = isSweepDone;
	}

	public boolean isSweepDone ()
	{
		return this.isSweepDone;
	}

    /**
     * Gets a specific ray
     *
     * @param key ControlMessage containing the desired data type, sweep, file, directory, server and port
     * @return the requested ray (or null if it does not exist)
     */ 
    public Object getRay (final ControlMessage key, final String type, final int index)
    {
        /*
        if (this.connected) { //Can't request without a connection
            if (this.cache.isEmpty(key)) this.commands.add(key); //first request: enqueue a request for the data
        }
        */
        return this.cache.getData(key, type, index);
    }

    /**
     * Gets a specific ray.  If the ray is not yet available, waits
     * for the ray to become available or the sweep to be marked complete.
     *
     * @param key ControlMessage containing the desired data type, sweep, file, directory, server and port
     * @return the requested ray (or null if it does not exist)
     */ 
    public Object getRayWait (final ControlMessage key, final String type, final int index)
    {
        /*
        if (this.connected) { //Can't request without a connection
            if (this.cache.isEmpty(key)) this.commands.add(key); //first request: enqueue a request for the data
        }
        */
        return this.cache.getDataWait(key, type, index);
    }

    /**
     * Gets a specific ray.  If the ray is not yet available, waits
     * for the ray to become available or the sweep to be marked complete.
     *
     * @param key ControlMessage containing the desired data type, sweep, file, directory, server and port
     * @param timeout the maximum number of milliseconds to wait 
     * @return the requested ray (or null if it does not exist)
     */ 
    public Object getRayWait (final ControlMessage key, final String type, final int index, final long timeout)
    {
        /*
        if (this.connected) { //Can't request without a connection
            if (this.cache.isEmpty(key)) this.commands.add(key); //first request: enqueue a request for the data
        }
        */
        return this.cache.getDataWait(key, type, index, timeout);
    }

   /**
    * Clears the cache if applicable
    */
   public void clearCache ()
   {
       this.cache.clear();
   }
}
