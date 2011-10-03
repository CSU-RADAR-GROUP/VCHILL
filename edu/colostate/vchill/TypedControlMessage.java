/*
 * TypedControlMessage.java
 *
 * Created on September 28, 2007, 3:36 PM
 */

package edu.colostate.vchill;

import java.util.Set;

/**
 * A TypedControlMessage is an extended control message;
 * it not only contains the control message but the types
 * involved as well.
 *
 * @author jpont
 * @version 2010-08-30
 */
public class TypedControlMessage {
    
    public ControlMessage message;
    public Set<String> types;
    
    public TypedControlMessage (ControlMessage message, Set<String> types) {
	this.message = message;
	this.types = types;
    }
    
    @Override public String toString ()
    {
		String typesString = "";
		for( String type : types ) {
			typesString += type + " ";
		}

		return message.toString() + ControlMessage.separator + typesString;
    }

	@Override
	public boolean equals (Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TypedControlMessage other = (TypedControlMessage) obj;
		if (this.message != other.message && (this.message == null || !this.message.equals( other.message ))) {
			return false;
		}
		if (this.types != other.types && (this.types == null || !this.types.equals( other.types ))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode () {
		int hash = 7;
		hash = 89 * hash + (this.message != null ? this.message.hashCode() : 0);
		hash = 89 * hash + (this.types != null ? this.types.hashCode() : 0);
		return hash;
	}


}
