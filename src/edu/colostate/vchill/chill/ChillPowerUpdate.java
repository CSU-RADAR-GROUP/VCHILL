package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.socket.SocketUtil;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Sent each time the transmitter monitoring power meters are read 
 *
 * @author Jochen Deyke
 * @version 2006-08-10
 */
public class ChillPowerUpdate extends ChillHeader
{
    /** H peak power in dBm assuming a rectangular pulse */
	public float h_power_dbm;
    /** V peak power in dBm assuming a rectangular pulse */
	public float v_power_dbm;

    /** size (in bytes) of this header (including ChillHeaderHeader, but not including extraData) */
    public static final int BYTE_SIZE = ChillHeaderHeader.BYTE_SIZE +
        2 * ChillDefines.FLOAT_BYTE_SIZE;

    public ChillPowerUpdate ()
    {
        super(new ChillHeaderHeader(ChillDefines.GEN_MOM_DATA, BYTE_SIZE));
        super.extraData = new byte[0];
    }

    /**
     * Constructs a header by reading initial values from a DataInput.
     *
     * @param in the DataInput to read initialization values from
     */
    public ChillPowerUpdate (final DataInput in, final ChillHeaderHeader header) throws IOException
    {
        super(header);
        assert header.recordType == ChillDefines.HSK_ID_PWR_UPDATE;
        assert header.headerLength - this.BYTE_SIZE >= 0;
        this.h_power_dbm = in.readFloat();
        this.v_power_dbm = in.readFloat();
        in.readFully(super.extraData = new byte[header.headerLength - this.BYTE_SIZE]);
    }

    /**
     * Writes this header to a DataOut
     *
     * @param out the DataOutput to write values to
     */
    public void write (final DataOutput out) throws IOException
    {
        assert header.recordType == ChillDefines.HSK_ID_PWR_UPDATE;
        assert header.headerLength == this.BYTE_SIZE + extraData.length;
        super.header.write(out);
        out.writeFloat(this.h_power_dbm);
        out.writeFloat(this.v_power_dbm);
        out.write(this.extraData);
    }
}
