package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Superclass of the various chill headers
 * 
 * @author Jochen Deyke
 * @version 2006-08-09
 */
public class ChillHeader
{
    public ChillHeaderHeader header;
    public byte[] extraData;

    /**
     * Constructor called from subclasses.
     * Need length from ChillHeaderHeader to know size of extraData.
     */
    public ChillHeader (final ChillHeaderHeader header)
    {
        this.header = header;
    }

    /**
     * Constructor for using ChillHeader as a generic/unknown header
     */
    public ChillHeader (final DataInput in, final ChillHeaderHeader header) throws IOException
    {
        this(header);
        assert this.header.headerLength < ChillDefines.MAX_HSK_SIZE : "Ridiculously long header - probably out of alignment";
        in.readFully(this.extraData = new byte[header.headerLength - header.BYTE_SIZE]); //account for length of HeaderHeader
    }

    /**
     * Write this header (including ChillHeaderHeader) to a DataOutput.
     * Subclasses should override this and insert their own data between
     * the HeaderHeader and extraData
     */
    public void write (final DataOutput out) throws IOException
    {
        this.header.write(out);
        out.write(this.extraData);
    }
}
