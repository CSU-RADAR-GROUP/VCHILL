package edu.colostate.vchill.socket;

import edu.colostate.vchill.ChillDefines;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * CHILL socket protocol response packet
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-03-06
 */
public final class SocketResponse
{
    public static final int BYTE_SIZE = ChillDefines.INT_BYTE_SIZE * 7;

    /** status codes */
    public enum Status {
        /** not used **/
        unknown,

        /** error opening file or directory */
        OPEN_ERROR,

        /** sweep number out of range */
        POSITION_ERROR,

        /** not used by Java VCHILL **/
        BLOCKLIM,

        END_VOLUME,
        END_SWEEP,
        END_FILE,

        /** sent immediately after the listing of files/directories/bookmarks */
        DIRECTORY_SENT,

        FORMAT_ERROR,
        NO_SWEEPS,
        RAY_READ,
        STATUS_UPDATE,
        STOPPED,

        /** reserved for bit running message; not used */
        running,

        /** There will be a (text) listing of files/directories/bookmarks of length <code>extStatus</code> bytes immediately following this header */
        DIRECTORY_FOLLOWS,

        SERVER_BUSY,
        SERVER_READY,
        SERVER_TIMEOUT,

        /** malformed/unclear request */
        REQUEST_ERROR,

        /** bad username/signon */
        SIGNON_UNKNOWN,

        /** bad password */
        PASSWD_FAIL,

        /** There will be a (text) message of length <code>extStatus</code> bytes immediately following this header */
        MESSAGE_FOLLOWS,

        SERVER_FAILURE,

        /** placeholder; not yet used **/
        UnknownStatus23,

        /** placeholder; not yet used **/
        UnknownStatus24,

        /** placeholder; not yet used **/
        UnknownStatus25,

        /** placeholder; not yet used **/
        UnknownStatus26,

        /** placeholder; not yet used **/
        UnknownStatus27,
    }
    /* ARCH_RUNNING is a status bit which is or'd with other condition codes */
    public static final int ARCH_RUNNING  = 256;
    /* ARCH_CAL_FILE is a status bit which is or'd with other condition codes */
    public static final int ARCH_CAL_FILE = 512;

    /** result code */
    private final Status status;
    private final boolean running;
    private final boolean calibrationPresent;

    /** additional status or number of additional bytes */
    private final int extStatus;

    /** current data position: volume # */
    private final int vol;

    /** current data position: sweep # */
    private final int sweep;

    /** current data position: ray # */
    private final int ray;

    /** current scan mode */
    private final int scanMode;

    /** max number of sweeps this volume */
    private final int maxSweeps;

    /**
     * @param in the DataInput to load values from
     */
    public SocketResponse (final DataInput in) throws IOException
    {
        int tmp        = in.readInt();
        this.running            = (tmp & ARCH_RUNNING) != 0;
        this.calibrationPresent = (tmp & ARCH_CAL_FILE) != 0;
        this.status    = Status.values()[tmp & (ARCH_RUNNING - 1)]; //&0xff
        this.extStatus = in.readInt();
        this.vol       = in.readInt();
        this.sweep     = in.readInt();
        this.ray       = in.readInt();
        this.scanMode  = in.readInt();
        this.maxSweeps = in.readInt();
    }

    /**
     * @param data the byte[] to load values from
     * @param offset the index of the first byte to read
     */
    public SocketResponse (final byte[] data, int offset)
    {
        if( data.length < offset + this.BYTE_SIZE - 1 ) {
            throw new IllegalArgumentException( "not enough data in input array" );
        }
        int tmp        = SocketUtil.readInt(data, offset); offset += ChillDefines.INT_BYTE_SIZE;
        this.running            = (tmp & ARCH_RUNNING) != 0;
        this.calibrationPresent = (tmp & ARCH_CAL_FILE) != 0;
        this.status    = Status.values()[tmp & (ARCH_RUNNING - 1)];
        this.extStatus = SocketUtil.readInt(data, offset); offset += ChillDefines.INT_BYTE_SIZE;
        this.vol       = SocketUtil.readInt(data, offset); offset += ChillDefines.INT_BYTE_SIZE;
        this.sweep     = SocketUtil.readInt(data, offset); offset += ChillDefines.INT_BYTE_SIZE;
        this.ray       = SocketUtil.readInt(data, offset); offset += ChillDefines.INT_BYTE_SIZE;
        this.scanMode  = SocketUtil.readInt(data, offset); offset += ChillDefines.INT_BYTE_SIZE;
        this.maxSweeps = SocketUtil.readInt(data, offset); offset += ChillDefines.INT_BYTE_SIZE;
        assert offset == this.BYTE_SIZE : "number of bytes read != size";
    }

    /**
     * Assumes an offset of 0. 
     * @param data the byte[] to load values from
     */
    public SocketResponse (final byte[] data)
    {
        this(data, 0);
    }

    /**
     * Assumes an offset of 0 and a new/uninitialized byte[].
     */
    public SocketResponse ()
    {
        this(new byte[BYTE_SIZE]);
    }

    /**
     * @param out the DataOutput to write values to
     */
    public void write (final DataOutput out)
    { try {
        int tmp = this.status.ordinal();
        if (this.running) tmp |= ARCH_RUNNING;
        if (this.calibrationPresent) tmp |= ARCH_CAL_FILE;
        out.writeInt(tmp);
        out.writeInt(this.extStatus);
        out.writeInt(this.vol);
        out.writeInt(this.sweep);
        out.writeInt(this.ray);
        out.writeInt(this.scanMode);
        out.writeInt(this.maxSweeps);
    } catch (IOException ioe) { throw new Error(ioe); }}

    /**
     * Translates this SocketResponse object to a byte[] for transmission over the network 
     *
     * @return byte[] representation of this SocketResponse object
     */
    public byte[] getBytes ()
    {
        final byte[] output = new byte[this.BYTE_SIZE];
        int offset = 0;
        int tmp = this.status.ordinal();
        if (running) tmp |= ARCH_RUNNING;
        if (calibrationPresent) tmp |= ARCH_CAL_FILE;
        offset += SocketUtil.writeInt(tmp, output, offset);
        offset += SocketUtil.writeInt(this.extStatus, output, offset);
        offset += SocketUtil.writeInt(this.vol, output, offset);
        offset += SocketUtil.writeInt(this.sweep, output, offset);
        offset += SocketUtil.writeInt(this.ray, output, offset);
        offset += SocketUtil.writeInt(this.scanMode, output, offset);
        offset += SocketUtil.writeInt(this.maxSweeps, output, offset);
        assert offset == this.BYTE_SIZE : "Num bytes returned != size";
        return output;
    }

    public String toString ()
    {
        return this.getStatusString() +
            "Ext status:          " + this.extStatus  + "\n" +
            "Volume number:       " + this.vol        + "\n" +
            "Sweep number:        " + this.sweep      + "\n" +
            "Ray number:          " + this.ray        + "\n" +
            "Scan mode:           " + this.scanMode   + "\n" +
            "Max sweeps this vol: " + this.maxSweeps;
    }

    public Status getStatus () { return this.status; }
	public boolean isRunning () { return this.running; }
    public boolean isCalibrationPresent () { return this.calibrationPresent; }
    //public boolean isArchiveRunning () { return this.running; }
    public int getExtStatus () { return this.extStatus; }
    public int getMaxSweeps () { return this.maxSweeps; }
    public int getRayNumber () { return this.ray; }

    private String getStatusString ()
    {
        StringBuilder result = new StringBuilder();
        result.append("Status code:         ");
        result.append(this.status.ordinal());
        result.append(" (");
        result.append(this.status.toString());
        if (running) result.append(" (Archive running)");
        if (calibrationPresent) result.append(" (Calibration file present)");
        result.append(")\n");
        return result.toString();
    }
}
