package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.socket.SocketUtil;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

/**
 * Extended aircraft tracking position update header
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2008-08-25
 */
public class ChillOldExtTrackInfo extends ChillHeader
{       
    /** maximum length (in bytes) of trackID */
    public static final int trackIDLength = 32;
    
    /** maximum length (in bytes) of trackInfo */
    public static final int trackInfoLength = 32;
    
    /** size (in bytes) of this header (including ChillHeaderHeader, but not including extraData) */
    public static final int BYTE_SIZE = ChillHeaderHeader.BYTE_SIZE +
        ChillDefines.LONG_BYTE_SIZE + 4 * ChillDefines.FLOAT_BYTE_SIZE +
        trackIDLength + trackInfoLength;

    /** Seconds since UNIX Epoch for this report */
    public /*unsigned*/ long time;
    
    /** Vehicle position east of radar */
    public float posX;
    
    /** Vehicle position north of radar */
    public float posY;

    /** Vehicle altitude in meters AGL */
    public float altitudeM;

    /**< Vehicle heading in degrees (if available) */
    public float headingD;

    /** Name of this vehicle */
    public String trackID;

    /** Additional track information */
    public String trackInfo;

    /**
     * Constructs a header by reading initial values from a DataInput.
     *
     * @param in the DataInput to read initialization values from
     */
    public ChillOldExtTrackInfo (final DataInput in, final ChillHeaderHeader header) throws IOException
    {
        super(header);
        assert header.recordType == ChillDefines.OLD_EXT_TRACK_DATA;
        assert header.headerLength - ChillOldExtTrackInfo.BYTE_SIZE >= 0;
        this.time = in.readLong();
        this.posX = in.readFloat();
        this.posY = in.readFloat();
        this.altitudeM = in.readFloat();
        this.headingD = in.readFloat();
        this.trackID = SocketUtil.readString(in, trackIDLength);
        this.trackInfo = SocketUtil.readString(in, trackInfoLength);
        in.readFully(this.extraData = new byte[header.headerLength - ChillOldExtTrackInfo.BYTE_SIZE]);
    }

    /**
     * Writes this header to a DataOut
     *
     * @param out the DataOutput to write values to
     */
    @Override public void write (final DataOutput out) throws IOException
    {
        assert header.headerLength == ChillOldExtTrackInfo.BYTE_SIZE + extraData.length;
        super.header.write(out);
        out.writeLong(this.time);
        out.writeFloat(this.posX);
        out.writeFloat(this.posY);
        out.writeFloat(this.altitudeM);
        out.writeFloat(this.headingD);
        SocketUtil.writeString(this.trackID, out, trackIDLength);
        SocketUtil.writeString(this.trackInfo, out, trackInfoLength);
        out.write(this.extraData);
    }

    @Override public String toString ()
    {
        return "Aircraft " + this.trackID + " was at " +
            this.posX + ", " +
            this.posY + ", " +
            (this.altitudeM * 1e-3) + " at " +
            new Date(this.time * 1000).toString() + " \"" +
            this.trackInfo + "\"";
    }
}