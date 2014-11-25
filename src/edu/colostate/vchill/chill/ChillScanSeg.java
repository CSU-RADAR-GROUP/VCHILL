package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.socket.SocketUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Scan segment header.
 * Defines a single scan segment or volume
 * A scan segment describes a portion (or a complete) volume
 *
 * @author Jochen Deyke
 * @version 2006-08-09
 */
public class ChillScanSeg extends ChillHeader {
    public static final int MAX_SEGNAME_LENGTH = 16;

    /**
     * size (in bytes) of this header (including ChillHeaderHeader, but not including extraData)
     */
    public static final int BYTE_SIZE = ChillHeaderHeader.BYTE_SIZE +
            5 * ChillDefines.FLOAT_BYTE_SIZE +
            MAX_SEGNAME_LENGTH +
            3 * ChillDefines.FLOAT_BYTE_SIZE +
            2 * ChillDefines.INT_BYTE_SIZE + //enums
            5 * ChillDefines.INT_BYTE_SIZE + //unsigned
            5 * ChillDefines.FLOAT_BYTE_SIZE +
            4 * ChillDefines.INT_BYTE_SIZE + //unsigned
            MAX_SEGNAME_LENGTH +
            ChillDefines.FLOAT_BYTE_SIZE;

    /**
     * Modes in which the antenna can scan
     */
    enum ScanType {
        PPI, /**
         * PPI (Plan Position Indicator) Mode
         */
        RHI, /**
         * RHI (Range Height Indicator) Mode
         */
        FIXED, /**
         * Fixed pointing angle mode
         */
        MANPPI, /**
         * Manual PPI mode (elevation does not step automatically)
         */
        MANRHI, /**
         * Manual RHI mode (azimuth does not step automatically)
         */
        IDLE, /**
         * IDLE mode, radar is not scanning
         */
        LAST
    }

    /**
     * Indicates the object the antenna is currently tracking
     */
    enum FollowMode {
        NONE, /**
         * Radar is not tracking any object
         */
        SUN, /**
         * Radar is tracking the sun
         */
        VEHICLE, /**
         * Radar is tracking a vehicle
         */
        LAST
    }

    /**
     * Scan Optimizer parameters
     */
    static class ScanOptimizer {
        public float rmax_km;
        public float htmax_km;
        public float res_m;
    }


    /**
     * Manual azimuth position. Used in pointing and Manual PPI/RHI modes
     */
    public float az_manual;
    /**
     * Manual elevation position. Used in pointing and Manual PPI/RHI modes
     */
    public float el_manual;
    /**
     * Azimuth start. If>360 implies don't care
     */
    public float az_start;
    /**
     * Elevation start. If>360 implies don't care
     */
    public float el_start;
    /**
     * Antenna scan rate, in degrees/sec
     */
    public float scan_rate;

    public String segname; //[MAX_SEGNAME_LENGTH]; /**< Name of this scan segment */

    /**
     * Scan optimizer parameters
     */
    ScanOptimizer opt = new ScanOptimizer();
    /**
     * Indicates the object being followed (tracked) by the antenna
     */
    FollowMode follow_mode;
    /**
     * Antenna scanning mode
     */
    ScanType scan_type;

    /**
     * Scan segment flags. See SF_... above
     */
    public /*unsigned*/ int scan_flags;
    /**
     * Volume number, increments linearly
     */
    public /*unsigned*/ int volume_num;
    /**
     * Sweep number within the current volume
     */
    public /*unsigned*/ int sweep_num;
    /**
     * Timeout for this scan, in seconds, if nonzero
     */
    public /*unsigned*/ int time_limit;
    /**
     * if nonzero, archive image of specified sweep
     */
    public /*unsigned*/ int webtilt;

    /**
     * Left limit used in sector scan
     */
    public float left_limit;
    /**
     * Right limit used in sector scan
     */
    public float right_limit;
    /**
     * Upper limit used in sector scan
     */
    public float up_limit;
    /**
     * Lower limit used in sector scan
     */
    public float down_limit;
    /**
     * Antenna step, used to increment az (in RHI) or el (in PPI) if max_sweeps is zero
     */
    public float step;

    /**
     * Indicates that a set of discrete angles is specified for az or el
     */
    public /*unsigned*/ int max_sweeps;
    /**
     * Sweep at which the clutter filter switch from filter 1 to 2
     */
    public /*unsigned*/ int filter_break_sweep;
    /**
     * Clutter filter for sweeps 1 to filter_break_sweep
     */
    public /*unsigned*/ int clutter_filter1;
    /**
     * Clutter filter for sweeps >= filter_break_sweep
     */
    public /*unsigned*/ int clutter_filter2;

    public String project; //[MAX_SEGNAME_LENGTH];   /**< Project name - used to organize scans + data */

    /**
     * Current fixed angle (az in RHI, el in PPI)
     */
    public float current_fixed_angle;

    public ChillScanSeg() {
        super(new ChillHeaderHeader(ChillDefines.GEN_MOM_DATA, BYTE_SIZE));
        super.extraData = new byte[0];
    }

    /**
     * Constructs a header by reading initial values from a DataInput.
     *
     * @param in the DataInput to read initialization values from
     */
    public ChillScanSeg(final DataInput in, final ChillHeaderHeader header) throws IOException {
        super(header);
        assert header.recordType == ChillDefines.HSK_ID_SCAN_SEG;
        assert header.headerLength - ChillScanSeg.BYTE_SIZE >= 0;
        this.az_manual = in.readFloat();
        this.el_manual = in.readFloat();
        this.az_start = in.readFloat();
        this.el_start = in.readFloat();
        this.scan_rate = in.readFloat();
        this.segname = SocketUtil.readString(in, MAX_SEGNAME_LENGTH);
        this.opt.rmax_km = in.readFloat();
        this.opt.htmax_km = in.readFloat();
        this.opt.res_m = in.readFloat();
        this.follow_mode = FollowMode.values()[in.readInt()];
        this.scan_type = ScanType.values()[in.readInt()];
        this.scan_flags = in.readInt();
        this.volume_num = in.readInt();
        this.sweep_num = in.readInt();
        this.time_limit = in.readInt();
        this.webtilt = in.readInt();
        this.left_limit = in.readFloat();
        this.right_limit = in.readFloat();
        this.up_limit = in.readFloat();
        this.down_limit = in.readFloat();
        this.step = in.readFloat();
        this.max_sweeps = in.readInt();
        this.filter_break_sweep = in.readInt();
        this.clutter_filter1 = in.readInt();
        this.clutter_filter2 = in.readInt();
        this.project = SocketUtil.readString(in, MAX_SEGNAME_LENGTH);
        this.current_fixed_angle = in.readFloat();
        in.readFully(super.extraData = new byte[header.headerLength - ChillScanSeg.BYTE_SIZE]);
    }

    /**
     * Writes this header to a DataOut
     *
     * @param out the DataOutput to write values to
     */
    public void write(final DataOutput out) throws IOException {
        assert header.recordType == ChillDefines.HSK_ID_SCAN_SEG;
        assert header.headerLength == ChillScanSeg.BYTE_SIZE + extraData.length;
        super.header.write(out);
        out.writeFloat(this.az_manual);
        out.writeFloat(this.el_manual);
        out.writeFloat(this.az_start);
        out.writeFloat(this.el_start);
        out.writeFloat(this.scan_rate);
        SocketUtil.writeString(this.segname, out, MAX_SEGNAME_LENGTH);
        out.writeFloat(this.opt.rmax_km);
        out.writeFloat(this.opt.htmax_km);
        out.writeFloat(this.opt.res_m);
        out.writeInt(this.follow_mode.ordinal());
        out.writeInt(this.scan_type.ordinal());
        out.writeInt(this.scan_flags);
        out.writeInt(this.volume_num);
        out.writeInt(this.sweep_num);
        out.writeInt(this.time_limit);
        out.writeInt(this.webtilt);
        out.writeFloat(this.left_limit);
        out.writeFloat(this.right_limit);
        out.writeFloat(this.up_limit);
        out.writeFloat(this.down_limit);
        out.writeFloat(this.step);
        out.writeInt(this.max_sweeps);
        out.writeInt(this.filter_break_sweep);
        out.writeInt(this.clutter_filter1);
        out.writeInt(this.clutter_filter2);
        SocketUtil.writeString(this.project, out, MAX_SEGNAME_LENGTH);
        out.writeFloat(this.current_fixed_angle);
        out.write(this.extraData);
    }
}
