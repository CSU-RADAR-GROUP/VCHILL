package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.socket.SocketUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Per-sweep housekeeping information
 *
 * @author Jochen Deyke
 * @version 2006-05-15
 */
public class ChillHSKHeader extends ChillHeader {
    /**
     * maximum length (in bytes) of radarId
     */
    public static final int radarIdLength = 32;

    /**
     * size (in bytes) of this header (including ChillHeaderHeader, but not including extraData)
     */
    public static final int BYTE_SIZE = ChillHeaderHeader.BYTE_SIZE +
            radarIdLength + 12 * ChillDefines.INT_BYTE_SIZE;

    /**
     * radar name/id in UTF-8 coded string
     */
    public String radarId;

    /**
     * radar location in degrees*10**6
     */
    public int radarLatitude, radarLongitude;

    /**
     * radar altitude above msl in mm
     */
    public int radarAltitudeMsl;

    /**
     * Antenna mode word - indicates scan type.
     * 1 and 4 are RHI scans, 0 and 3 are PPI scans
     */
    public int antMode;

    /**
     * basic Nyquist interval in mm/sec (2 * velocity range )
     */
    public int nyquistVel;

    /**
     * gate spacing in mm
     */
    public int gateWidth;

    /**
     * the number of transmit pulses per integration cycle
     */
    public int pulses;

    /**
     * 0 = V only, 1 = H only, 2 = VH alternating, 3 = VH simultaneous
     */
    public int polarizationMode;

    /**
     * the tilt/sweep sequence number (begins at 1 for each volume)
     */
    public int tiltNum;

    /**
     * the tilt number selected by operator for auto-save image
     */
    public int saveTilt;

    /**
     * angle value representing 360 degrees
     */
    public int angleScale;

    /**
     * unix time word for start of this sweep (seconds)
     */
    public long sweepStartTime;

    public ChillHSKHeader() {
        super(new ChillHeaderHeader(ChillDefines.BRIEF_HSK_DATA, BYTE_SIZE));
        super.extraData = new byte[0];
    }

    /**
     * Constructs a header by reading initial values from a DataInput.
     *
     * @param in     the DataInput to read initialization values from
     * @param header the header header containing the expected length of this header
     */
    public ChillHSKHeader(final DataInput in, final ChillHeaderHeader header) throws IOException {
        super(header);
        assert header.recordType == ChillDefines.BRIEF_HSK_DATA;
        assert header.headerLength - ChillHSKHeader.BYTE_SIZE >= 0;
        this.radarId = SocketUtil.readString(in, radarIdLength);
        this.radarLatitude = in.readInt();
        this.radarLongitude = in.readInt();
        this.radarAltitudeMsl = in.readInt();
        this.antMode = in.readInt();
        this.nyquistVel = in.readInt();
        this.gateWidth = in.readInt();
        this.pulses = in.readInt();
        this.polarizationMode = in.readInt();
        this.tiltNum = in.readInt();
        this.saveTilt = in.readInt();
        this.angleScale = in.readInt();
        this.sweepStartTime = SocketUtil.readUnsignedInt(in);
        in.readFully(super.extraData = new byte[header.headerLength - ChillHSKHeader.BYTE_SIZE]);
    }

    /**
     * Writes this header to a DataOut
     *
     * @param out the DataOutput to write values to
     */
    public void write(final DataOutput out) throws IOException {
        assert header.headerLength == ChillHSKHeader.BYTE_SIZE + extraData.length;
        super.header.write(out);
        SocketUtil.writeString(this.radarId, out, radarIdLength);
        out.writeInt(this.radarLatitude);
        out.writeInt(this.radarLongitude);
        out.writeInt(this.radarAltitudeMsl);
        out.writeInt(this.antMode);
        out.writeInt(this.nyquistVel);
        out.writeInt(this.gateWidth);
        out.writeInt(this.pulses);
        out.writeInt(this.polarizationMode);
        out.writeInt(this.tiltNum);
        out.writeInt(this.saveTilt);
        out.writeInt(this.angleScale);
        SocketUtil.writeUnsignedInt(this.sweepStartTime, out);
        out.write(super.extraData);
    }

    public String getMode() {
        switch (this.antMode) {
            case 0:
            case 3:
                return "PPI";
            case 1:
            case 4:
                return "RHI";
            default:
                return "MAN";
        }
    }
}
