package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Xmit Info header
 *
 * @author Jochen Deyke
 * @version 2007-05-03
 */
public class ChillXmitInfo extends ChillHeader {
    /**
     * size (in bytes) of this header (including ChillHeaderHeader, but not including extraData)
     */
    public static final int BYTE_SIZE = ChillHeaderHeader.BYTE_SIZE +
            3 * ChillDefines.INT_BYTE_SIZE + 2 * ChillDefines.FLOAT_BYTE_SIZE;

    public static final int XMIT_H_ENABLE = 0x01;
    public static final int XMIT_V_ENABLE = 0x02;

    /**
     * Indicates which transmitters are firing (see XMIT_?_ENABLE)
     */
    public /*unsigned*/ int xmitEnables;

    /**
     * Transmitter polarization mode
     */
    public PolarizationMode polarizationMode;

    /**
     * Transmitter pulse waveform
     */
    public PulseType pulseType;

    /**
     * PRT in microseconds
     */
    public float prtUsec;

    /**
     * Second PRT in microseconds for Dual-PRT mode
     */
    public float prt2Usec;

    public ChillXmitInfo() {
        super(new ChillHeaderHeader(ChillDefines.GEN_MOM_DATA, BYTE_SIZE));
        super.extraData = new byte[0];
    }

    /**
     * Constructs a header by reading initial values from a DataInput.
     *
     * @param in the DataInput to read initialization values from
     */
    public ChillXmitInfo(final DataInput in, final ChillHeaderHeader header) throws IOException {
        super(header);
        assert header.recordType == ChillDefines.HSK_ID_XMIT_INFO;
        assert header.headerLength - ChillXmitInfo.BYTE_SIZE >= 0;
        this.xmitEnables = in.readInt();
        this.polarizationMode = PolarizationMode.values()[in.readInt()];
        this.pulseType = PulseType.values()[in.readInt()];
        this.prtUsec = in.readFloat();
        this.prt2Usec = in.readFloat();
        in.readFully(super.extraData = new byte[header.headerLength - ChillXmitInfo.BYTE_SIZE]);
    }

    /**
     * Writes this header to a DataOut
     *
     * @param out the DataOutput to write values to
     */
    public void write(final DataOutput out) throws IOException {
        assert header.recordType == ChillDefines.GEN_MOM_DATA;
        assert header.headerLength == ChillXmitInfo.BYTE_SIZE + extraData.length;
        super.header.write(out);
        out.writeInt(this.xmitEnables);
        out.writeInt(this.polarizationMode.ordinal());
        out.writeInt(this.pulseType.ordinal());
        out.writeFloat(this.prtUsec);
        out.writeFloat(this.prt2Usec);
        out.write(this.extraData);
    }
}
