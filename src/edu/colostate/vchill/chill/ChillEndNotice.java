package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * End of sweep/volume notice
 * @author Jochen Deyke
 * @version 2007-09-19
 */
public class ChillEndNotice extends ChillHeader
{
    /** size (in bytes) of this header (including ChillHeaderHeader, but not including extraData) */
    public static final int BYTE_SIZE = ChillHeaderHeader.BYTE_SIZE +
        2 * ChillDefines.INT_BYTE_SIZE;

    /** possible flag bits */
    private static final int endSweep = 1 << 0;
    private static final int endVolume = 1 << 1;
    private static final int startSweep = 1 << 2;

    /** Flags indicating type of end-notice */
    public /*unsigned*/ int flags;

    /** Indicates the reason this scan terminated */
    public Cause cause;

    /** Reasons for scan termination */
    public enum Cause
    {
        /** Scan completed normally */
        DONE,
        /** Scan has timed out */
        TIMEOUT,
        /** Timer caused this scan to abort */
        TIMER,
        /** Operator issued an abort */
        ABORT,
        /** Scan Controller detected error */
        ERROR_ABORT,
        /** Communication fault with DTau was recovered, restarting scan */
        RESTART,
    }

    public ChillEndNotice ()
    {
        super(new ChillHeaderHeader(ChillDefines.GEN_MOM_DATA, BYTE_SIZE));
        super.extraData = new byte[0];
    }

    public boolean isStart ()
    {
        return (this.flags & startSweep) != 0;
    }

    public boolean isEnd ()
    {
        return (this.flags & endSweep) != 0;
    }

    /**
     * Constructs a header by reading initial values from a DataInput.
     *
     * @param in the DataInput to read initialization values from
     */
    public ChillEndNotice (final DataInput in, final ChillHeaderHeader header) throws IOException
    {
        super(header);
        assert header.recordType == ChillDefines.HSK_ID_END_NOTICE;
        assert header.headerLength - ChillEndNotice.BYTE_SIZE >= 0;
        this.flags = in.readInt();
        this.cause = Cause.values()[in.readInt()];
        in.readFully(super.extraData = new byte[header.headerLength - ChillEndNotice.BYTE_SIZE]);
    }

    /**
     * Writes this header to a DataOut
     *
     * @param out the DataOutput to write values to
     */
    public void write (final DataOutput out) throws IOException
    {
        assert header.recordType == ChillDefines.HSK_ID_END_NOTICE;
        assert header.headerLength == ChillEndNotice.BYTE_SIZE + extraData.length;
        super.header.write(out);
        out.writeInt(this.flags);
        out.writeInt(this.cause.ordinal());
        out.write(this.extraData);
    }
}
