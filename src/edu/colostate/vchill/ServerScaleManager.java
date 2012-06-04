/* -*- Mode: Java; indent-tabs-mode: t -*- */
/**
$Id: ServerScaleManager.java,v 1.2 2009/11/10 20:14:32 jpont Exp $
$Source: /home/cvs/vchill/edu/colostate/vchill/ServerScaleManager.java,v $

Created on May 14, 2009, 3:38:17 PM

$Revision: 1.2 $
*/

package edu.colostate.vchill;

import edu.colostate.vchill.chill.ChillMomentFieldScale;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * ServerScaleManager manages scale
 * information for a particular server.
 *
 * @author  jpont
 * @version 2009-11-10
 */
public final class ServerScaleManager
{
	/** The url (hostname:port) of the server. */
	private String serverURL;

	/** Maps field names to field scales. */
	private HashMap<String, ChillMomentFieldScale> nameToScaleMap;

	/** Contains the field names according to field number. */
	private ArrayList<String> names;

	/** A bitmask containing which fields are available from the server. */
	private long availableMask;

	public ServerScaleManager (final String serverURL)
	{
		this.serverURL = serverURL;
		this.nameToScaleMap = new HashMap<String, ChillMomentFieldScale>();
		this.names = new ArrayList<String>();
		this.availableMask = -1; //all bits initially set
	}

	/**
     * Removes ALL types from the list of known types.
     */
    public synchronized void clear ()
	{
		this.nameToScaleMap.clear();
		this.names.clear();
		this.availableMask = -1; //all bits initially set
	}

	public long getAvailableMask ()
	{
		return this.availableMask;
	}

	public void setAvailableMask (final long available)
	{
		this.availableMask = available;
	}

	public synchronized ChillMomentFieldScale getScale (final String name)
	{
		if( name == null )
			return null;

		return this.nameToScaleMap.get( name );
	}

	public synchronized ChillMomentFieldScale getScale (final int number)
	{
		for( ChillMomentFieldScale scale : this.nameToScaleMap.values() )
		{
			if( scale.fieldNumber == number )
				return scale;
		}

		return null;
	}

	public synchronized Collection<ChillMomentFieldScale> getScales ()
	{
		return new ArrayList<ChillMomentFieldScale>( this.nameToScaleMap.values() );
	}

	public synchronized ArrayList<String> getTypes ()
	{
		return this.names;
	}

	public synchronized void putScale (final ChillMomentFieldScale scale)
	{
		this.nameToScaleMap.put( scale.fieldName, scale );
		this.names.add( scale.fieldName );
	}

	/**
     * Attempts to remove a type from the list of known types.
     * If that type does not exist, nothing happens.
     */
    public synchronized ChillMomentFieldScale removeScale (final ChillMomentFieldScale scale)
	{
		this.names.remove( scale.fieldName );
		return this.nameToScaleMap.remove( scale.fieldName );
	}
}
