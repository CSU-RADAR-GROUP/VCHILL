package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Information about headers
 *
 * @author Jochen Deyke
 * @version 2007-03-08
 */
public class ChillHeaderHeader
{
    /** Type of following header. Ie. FIELD_SCALE_DATA = 0x9292 */
    public int recordType;

    /** Length (in bytes) of the following header (including this header) */
    public int headerLength;

    /** Size (in bytes) of this header */
    public static final int BYTE_SIZE = 2 * ChillDefines.INT_BYTE_SIZE;

    public ChillHeaderHeader (final int recordType, final int headerLength)
    {
        this.recordType = recordType;
        this.headerLength = headerLength;
    }
    
    /** Copy constructor */
    public ChillHeaderHeader (final ChillHeaderHeader other)
    {
        this.recordType = other.recordType;
        this.headerLength = other.headerLength;
    }

    /**
     * Constructs a header by reading initial values from a DataInput.
     *
     * @param in the DataInput to read initialization values from
     */
    public ChillHeaderHeader (final DataInput in) throws IOException
    {
        this.recordType   = in.readInt();
        this.headerLength = in.readInt();
    }

    /**
     * Writes this header to a DataOut
     *
     * @param out the DataOutput to write values to
     */
    public void write (final DataOutput out) throws IOException
    {
        out.writeInt(this.recordType);
        out.writeInt(this.headerLength);
    }
}
