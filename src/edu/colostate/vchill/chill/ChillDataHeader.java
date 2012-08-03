package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.socket.SocketUtil;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Data header
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-06-01
 */
public class ChillDataHeader extends ChillHeader
{
    private static final ScaleManager sm = ScaleManager.getInstance();
    
    /** size (in bytes) of this header (including ChillHeaderHeader, but not including extraData) */
    public static final int BYTE_SIZE = ChillHeaderHeader.BYTE_SIZE +
        2 * ChillDefines.LONG_BYTE_SIZE + 9 * ChillDefines.INT_BYTE_SIZE;

    /** Bit map of data types requested by client (ppi).
        See moment.h  e.g. ZDR_MOM for definitions */
    public long requestedData;

    /** bit map as above for what fields are available from processor */
    public long availableData;

    /** Ant position at start of integration cycle.
        All angles scaled as indicated by angleScale in ChillHSKHeader.java */
    public int startAz, startEl;

    /** Ant position at end of integration cycle.
        All angles scaled as indicated by angleScale in ChillHSKHeader.java */
    public int endAz, endEl;

    /** number of gates in this ray */
    public int numGates;

    /** the range to the first data gate in millimeters */
    public int startRange;

    /** unix time word for (first txmit pulse in) this ray (seconds) */
    public long dataTime;

    /** aditional nanoseconds to first txmit pulse */
    public int fractionalSecs;

    /** ray number within this volume, starts at 1 */
    public int rayNumber;

    public ChillDataHeader ()
    {
        super(new ChillHeaderHeader(ChillDefines.GEN_MOM_DATA, BYTE_SIZE));
        super.extraData = new byte[0];
    }

    public ChillDataHeader (final ChillDataHeader other)
    {
        super(new ChillHeaderHeader(other.header));
        assert header.recordType == ChillDefines.GEN_MOM_DATA;
        assert header.headerLength - ChillDataHeader.BYTE_SIZE >= 0;
        this.requestedData    = other.requestedData;
        this.availableData    = other.availableData;
        this.startAz          = other.startAz;
        this.startEl          = other.startEl;
        this.endAz            = other.endAz;
        this.endEl            = other.endEl;
        this.numGates         = other.numGates;
        this.startRange       = other.startRange;
        this.dataTime         = other.dataTime;
        this.fractionalSecs   = other.fractionalSecs;
        this.rayNumber        = other.rayNumber;
        this.extraData        = new byte[other.extraData.length];
        for (int i = 0; i < this.extraData.length; ++i)
            this.extraData[i] = other.extraData[i];
    }

    /**
     * Constructs a header by reading initial values from a DataInput.
     *
     * @param in the DataInput to read initialization values from
     */
    public ChillDataHeader (final DataInput in, final ChillHeaderHeader header) throws IOException
    {
        super(header);
        assert header.recordType == ChillDefines.GEN_MOM_DATA;
        assert header.headerLength - ChillDataHeader.BYTE_SIZE >= 0;
        this.requestedData    = in.readLong();
        this.availableData    = in.readLong();
        this.startAz          = in.readInt();
        this.startEl          = in.readInt();
        this.endAz            = in.readInt();
        this.endEl            = in.readInt();
        this.numGates         = in.readInt();
        this.startRange       = in.readInt();
        this.dataTime         = SocketUtil.readUnsignedInt(in);
        this.fractionalSecs   = in.readInt();
        this.rayNumber        = in.readInt();
        in.readFully(super.extraData = new byte[header.headerLength - ChillDataHeader.BYTE_SIZE]);
    }

    /**
     * Writes this header to a DataOut
     *
     * @param out the DataOutput to write values to
     */
    public void write (final DataOutput out) throws IOException
    {
        assert header.recordType == ChillDefines.GEN_MOM_DATA;
        assert header.headerLength == ChillDataHeader.BYTE_SIZE + extraData.length;
        super.header.write(out);
        out.writeLong(this.requestedData);
        out.writeLong(this.availableData);
        out.writeInt(this.startAz);
        out.writeInt(this.startEl);
        out.writeInt(this.endAz);
        out.writeInt(this.endEl);
        out.writeInt(this.numGates);
        out.writeInt(this.startRange);
        SocketUtil.writeUnsignedInt(this.dataTime, out);
        out.writeInt(this.fractionalSecs);
        out.writeInt(this.rayNumber);
        out.write(this.extraData);
    }

    public ArrayList<String> calculateTypes ()
    {
        long available = this.requestedData & this.availableData;
		int numAvailableFields = Long.bitCount( available );
		ArrayList<String> types = new ArrayList<String>(numAvailableFields);
		for( int i = 0; i < numAvailableFields; i++ ) {
			/* Get the bit position of the first available field. */
			int fieldNumber = Long.numberOfTrailingZeros( available );
			/* Add the field to the list if the field header exists. */
			ChillMomentFieldScale scale = sm.getScale( fieldNumber );
			if( scale != null ) {
				types.add( scale.fieldName );
			}
			else {
				/*
				 * Just because we don't know about the field, VCHILL
				 * still needs to know how many fields to read in so
				 * add null to increase the size.
				 */
				types.add( null );
			}
			/* Remove this field bit from the mask. */
			available ^= (1l << fieldNumber);
		}

        return types;
    }
}
