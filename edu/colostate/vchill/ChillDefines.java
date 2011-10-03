package edu.colostate.vchill;

import java.awt.event.KeyEvent;

/**
 * Utility class containing definitions of VCHILL's constants.
 * All fields are public, static, and final.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2008-08-25
 */
public final class ChillDefines
{
    /** new generalized display stuff */
    public static final int GEN_MOM_DATA       = 0x9090;
    public static final int BRIEF_HSK_DATA     = 0x9191;
    public static final int FIELD_SCALE_DATA   = 0x9292;
    public static final int TRACK_DATA         = 0x9393;
    public static final int OLD_EXT_TRACK_DATA = 0x9494;
    public static final int NEW_EXT_TRACK_DATA = 0x5aa50009;
    public static final int HELLO              = 0xf0f00f0f;
/*
This is the largest likely houskeeping pkt size (bytes)
It's only used for memory allocation, so it can be
much larger than required.
*/
    public static final int MAX_HSK_SIZE = 1024;

    public static final int HSK_ID_RADAR_INFO     = 0x5aa50001;
    public static final int HSK_ID_SCAN_SEG       = 0x5aa50002;
    public static final int HSK_ID_PROCESSOR_INFO = 0x5aa50003;
    public static final int HSK_ID_PWR_UPDATE     = 0x5aa50004;
    public static final int HSK_ID_END_NOTICE     = 0x5aa50005;
    public static final int HSK_ID_CAL_TERMS      = 0x5aa50006;
    public static final int HSK_ID_VERSION        = 0x5aa50007;
    public static final int HSK_ID_XMIT_INFO      = 0x5aa50008;

    public static final int DAT_ID_CHILL_AIQ  = 0x5aa50016;
    public static final int DAT_ID_PAWNEE_AIQ = 0x5aa50017;

    /** the maximum total number of data types */
    public static final int MAX_NUM_TYPES = 64;

    /** the maximum number of simultaneous data types allowed per data channel */
    public static final int MAX_PER_CHANNEL = 16;

    /** the altitude above sea level of the radar site (in km) */
    public static final double CHILL_ALTITUDE = 1.432;

    /** in degrees north */
    public static final double CHILL_LATITUDE = 40.44625;

    /** in degrees east */
    public static final double CHILL_LONGITUDE = -104.63708;

    /** the default number of different colors in each plot */
    public static final int numColorLevels = 16;

    /** the maximum number of aircraft supported */
    public static final int TOTAL_AIRCRAFTS = 4;

    /** size in bytes of primitive coming in on the socket */
    public static final int SHORT_BYTE_SIZE = 2;
    public static final int INT_BYTE_SIZE = 4;
    public static final int LONG_BYTE_SIZE = 8;
    public static final int FLOAT_BYTE_SIZE = INT_BYTE_SIZE;

    public static final String REALTIME_DIR = "realtime pseudodir";
    public static final String REALTIME_FILE = "realtime pseudofile";
    public static final String REALTIME_SWEEP = "realtime data";

    public static final String META_TYPE = "   h34der m3t4d4t4   ";
    public static final String CONTROL_TYPE = "   c0ntr0l m3t4d4t4   ";

//---------------- from ReplayDocumentation.txt

    public static final int MAX_SOCK = 10;
    public static final float WAVELENGTH = 0.110092f;

//---------------- from glb_define.h

    /** the types of communication channel between RDP and data system */
    public enum Channel {
        /** not used */
        unknown,
        /** not used by Java VCHILL */
        CMD_CTL,
        /** old style moment data channel - no longer used by Java VCHILL */
        MOM_DAT,
        /** not used by Java VCHILL */
        STRIP_DAT,
        /** not used by Java VCHILL */
        RAW_DAT,
        /** not used by Java VCHILL */
        IQ_DAT,
        /** not used by Java VCHILL */
        FT_DAT,
        /** not used by Java VCHILL */
        PLOT_DAT,
        /** not used by Java VCHILL */
        STA_DAT,
        /** not used by Java VCHILL */
        EDGE_CTL,
        /** not used by Java VCHILL */
        ARCHIVE,
        /** not used by Java VCHILL */
        GFFT_DAT,
        /** archive server control channel */
        ARCH_CTL, // used to control archive data server  d.brunkow  3/2/99
        /** not used by Java VCHILL */
        ANT_CTL, // used to control antenna/radar server  d.brunkow  12/01
        /** not used by Java VCHILL */
        SOCK_CTL, // used to monitor and control moment data sockets d.brunkow 2/1/02
        /** generalized moment data channel */
        GEN_MOM_DAT, // generalized moment data channel - data to display or other clients, d.brunkow 12/9/05
    }

//---------------- from moments.h

/* 8 bit values in archive data set are recorded as offset binary
   so from a floating point dbZ value,  to get the 8 bit value, 
   multiply by DBZ_FACTOR, add  dbz offset and limit at 0,255
   (in the case of dbz, offset is 64  (32 db), range is: -32.0 to +95.5 dBZ
   (in the case of zdr, offset is 64, (3.01 dB)  -3.01 to 9.03 dB
   (in the case of ldr, offset is 255 (48.165 dB) 0 to -48.165 dB
   In the case of phidp, the scale is a funtion of mode, such that
   it ranges from -pi to +pi for VHS mode, and -pi/2 to +pi/2 in VH mode.
*/

    public static final float DBZ_OFFSET =  64f;
    public static final float DBZ_FACTOR =   2f;
    public static final float ZDR_OFFSET =  64f;
    public static final float ZDR_FACTOR =  21.26034f;
    public static final float LDR_OFFSET = 255f;
    public static final float LDR_FACTOR =   5.294301f;
    public static final float HDR_OFFSET = 15640f/107f;
    public static final float HDR_FACTOR =   170f/107f;

    public static final int CHILL_V_MODE   = 0;
    public static final int CHILL_H_MODE   = 1;
    public static final int CHILL_VH_MODE  = 2;
    public static final int CHILL_VHS_MODE = 3;

    /** Color tables */
    public enum ColorType {
        /** reflectivity */
        Z,
        /** velocity */
        V,
        /** spectral width */
        W,
        /** NCP */
        CZ,
        /** ZDR */
        Zdr,
        /** LDR */
        Ldr,
        /** PhiDP */
        Phi,
        /** RhoHV */
        Rho,
    }

    /** Autoadvancement mode */
    public enum Mode {
        /** Manually step through rays (best with AScope windows */
        Ray ("Manually step through rays (best with AScope windows)"),
        /** Stop plot at end of sweep (default) */
        Sweep ("Stop plot at end of sweep (default)"),
        /** Automatically advance through sweeps within the same volume */
        Volume ("Automatically advance through sweeps within the same volume"),
        /** Display all data until the end of the directory */
        Continuous ("Display all data until the end of the directory");

        /** descriptive String */
        public final String DESCRIPTION;

        Mode (final String description)
        {
            this.DESCRIPTION = description;
        }
    }

//----------- from gate_data.h
    public static final int GATE_PARAMS_PACKET_CODE = 0;
    public static final int GATE_DATA_PACKET_CODE = 1;
    public static final int TRACK_DATA_CODE = 2;

    public static final int GATE_DATA_UNKNOWN_MODE = -1;
    public static final int GATE_DATA_SECTOR_MODE = 1;
    public static final int GATE_DATA_RHI_MODE = 3;
    public static final int GATE_DATA_SURVEILLANCE_MODE = 8;

    /** Private default constructor prevents instantiation */
    private ChillDefines () {}
}
