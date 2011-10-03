package edu.colostate.vchill.file;

import edu.colostate.vchill.ChillDefines;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Reads SKU Header of CHILL file
 *
 * @author  Justin Carlson
 * @author  Brian Eriksson
 * @author  Jochen Deyke
 * @created June 17, 2003
 * @version 2006-03-31
 */
class FileSKUHeader
{
    public static final int BYTE_SIZE = 5 * ChillDefines.INT_BYTE_SIZE;

	/** Set to all 0xF0 thus making an 8 byte block of F0s. */
	public int code1, code2;
    public static final int correctCode = 0xf0f0f0f0;
	
	/** Set to either GATE_PARAMS_PACKET_CODE or a ray data block GATE_DATA_PACKET_CODE */
	public int id;
	
	/** The length word indicates the length in bytes of the data block which follows. */
	public int length;

	public int msgnum;

	/**
	 * Returns whether or not the SKU Header is the header for a sweep
	 *
	 * @return True if SKU Header is for a sweep
	 */
	public boolean isSweep ()
    {
		if (id == 0) return true;
		return false;
	}

	/**
	 * Reads the SKUHeader from the CHILL file
	 *
	 * @param in DataInput object of CHILL file being read
	 * @return Boolean false if IOException is thrown, true if not thrown
	 */
	public boolean inputData (final DataInput in)
    {
		try {
			this.code1 = in.readInt(); //assert this.code1 == correctCode : "code1 was " + this.code1;
            if (this.code1 != correctCode) { System.err.println("code1 was " + this.code1); return false; }
			this.code2 = in.readInt(); //assert this.code2 == correctCode : "code2 was " + this.code2;
            if (this.code2 != correctCode) { System.err.println("code2 was " + this.code2); return false; }
			this.id = in.readInt();
			this.length = in.readInt();
			this.msgnum = in.readInt();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Writes the SKUHeader to the CHILL file
	 *
	 * @param out DataOutput object of CHILL file being read
	 * @return Boolean false if IOException is thrown, true if not thrown
	 */
	public boolean outputData (final DataOutput out)
    {
		try {
			out.writeInt(this.code1);
			out.writeInt(this.code2);
			out.writeInt(this.id);
			out.writeInt(this.length);
			out.writeInt(this.msgnum);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
