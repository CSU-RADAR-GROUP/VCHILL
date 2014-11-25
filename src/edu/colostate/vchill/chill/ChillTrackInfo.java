package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.socket.SocketUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

/**
 * Aircraft tracking position update header
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-02
 */
public class ChillTrackInfo extends ChillHeader {
    /**
     * maximum length (in bytes) of ident
     */
    public static final int identLength = 16;

    /**
     * size (in bytes) of this header (including ChillHeaderHeader, but not including extraData)
     */
    public static final int BYTE_SIZE = ChillHeaderHeader.BYTE_SIZE +
            3 * ChillDefines.FLOAT_BYTE_SIZE + ChillDefines.INT_BYTE_SIZE +
            identLength;

    /**
     * position of vehicle with respect to radar, in km x+ = east, y+ = north
     */
    public float xKm, yKm;

    /**
     * altitude above ground level, km
     */
    public float altKm;


    /**
     * unix style time of position report
     */
    public long trkTime;

    /**
     * name of vehicle - UTF-8 coding
     */
    public String ident;

    /**
     * Constructs a header by reading initial values from a DataInput.
     *
     * @param in the DataInput to read initialization values from
     */
    public ChillTrackInfo(final DataInput in, final ChillHeaderHeader header) throws IOException {
        super(header);
        assert header.recordType == ChillDefines.TRACK_DATA;
        assert header.headerLength - BYTE_SIZE >= 0;
        this.xKm = in.readFloat();
        this.yKm = in.readFloat();
        this.altKm = in.readFloat();
        this.trkTime = SocketUtil.readUnsignedInt(in);
        this.ident = SocketUtil.readString(in, identLength);
        in.readFully(this.extraData = new byte[header.headerLength - BYTE_SIZE]);
    }

    /**
     * Writes this header to a DataOut
     *
     * @param out the DataOutput to write values to
     */
    public void write(final DataOutput out) throws IOException {
        assert header.headerLength == ChillTrackInfo.BYTE_SIZE + extraData.length;
        super.header.write(out);
        out.writeFloat(this.xKm);
        out.writeFloat(this.yKm);
        out.writeFloat(this.altKm);
        SocketUtil.writeUnsignedInt(this.trkTime, out);
        SocketUtil.writeString(this.ident, out, identLength);
        out.write(this.extraData);
    }

    public String toString() {
        return "Aircraft " + ident + " was at " +
                xKm + ", " +
                yKm + ", " +
                altKm + " at " +
                new Date(trkTime * 1000).toString();
    }
}
