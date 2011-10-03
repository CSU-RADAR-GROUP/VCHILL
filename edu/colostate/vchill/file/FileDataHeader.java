package edu.colostate.vchill.file;

import edu.colostate.vchill.ChillDefines;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

/**
 * A data header for a CHILL DRX format file.
 *
 * @author  Justin Carlson
 * @author  Brian Eriksson
 * @author  Jochen Deyke
 * @author  jpont
 * @created June 17, 2003
 * @version 2010-08-30
 */
public class FileDataHeader
{
    /** number of bytes used in fields from the file */
    public static final int BYTE_SIZE = ChillDefines.INT_BYTE_SIZE * 15;
    
    /** Size of the data expected for the read in */
    public int size_of_data;
    
    /** number of bytes in header, or offset to first data byte */
    public int header_len;
    
    /** secs since Jan 1 1970 */
    public int time;
    
    /** deg * 1000000 */
    public int azimuth;
    
    /** deg * 1000000 */
    public int elevation;
    
    /** deg * 1000000 */
    public int target_elev;
    
    /** the volume scan number */
    public int vol_num;
    
    /** the tilt number in the volume scan */
    public int tilt_num;
    
    /** TRUE (1) if scan limits have changed, FALSE (0) otherwise */
    public int new_scan_limits;
    
    /**
     * flag - TRUE(1) or FALSE (0) - not essential
     * set to (-1)  if not operational
     */
    public int end_of_tilt;
    
    /**
     * flag - TRUE(1) or FALSE (0) - not essential
     * set to (-1)  if not operational
     */
    public int end_of_volume;
    
    /**
     * for csu-chill, set to volume term code (non-zero) at end vol.
     * for csu-chill, ray number (within volume)
     */
    public int ray_num;
    
    /** horizontal peak transmit power dBm*100 */
    public int txmit_power_H;
    
    /** vertical peak transmit power  dBm*100 */
    public int txmit_power_V;
    
    /** average power in last half of range */
    public int drx_rec1_average_power;
    
    /** average power in last half of range */
    public int drx_rec2_average_power;

    /**
     * Constructor for the FileDataHeader object
     *
     * @param size_of_data Size of the header
     */
    public FileDataHeader (final int size_of_data) {
        /*
         * To get the amount of data to read into the storage array
         * we must subtract the size of the fields in the gate header from
         * the actual data it contains.
         */
        this.size_of_data = size_of_data - BYTE_SIZE;
    }

    /**
     * Reads in the data from the header 
     *
     * @param in DataInput object containing the CHILL file being read
     * @return true if successful, false if EOF hit
     */
    public boolean inputData (final DataInput in) {
        try {
            this.header_len = in.readInt();
            this.time = in.readInt();
            this.azimuth = in.readInt();
            this.elevation = in.readInt();
            this.target_elev = in.readInt();
            this.vol_num = in.readInt();
            this.tilt_num = in.readInt();
            this.new_scan_limits = in.readInt();
            this.end_of_tilt = in.readInt();
            this.end_of_volume = in.readInt();
            this.ray_num = in.readInt();
            this.txmit_power_H = in.readInt();
            this.txmit_power_V = in.readInt();
            this.drx_rec1_average_power = in.readInt();
            this.drx_rec2_average_power = in.readInt();
            return true;
        } catch (EOFException eofe) {
            return false;
        } catch (IOException ioe) {
            System.err.println("IO message = " + ioe.getMessage());
            System.err.println("IO trace = ");
            ioe.printStackTrace();
            return false;
        }
    }

    /**
     * Writes out the data from the header 
     *
     * @param out DataOutput object containing the CHILL file being written
     */
    public void outputData (final DataOutput out) {
        try {
            out.writeInt(this.header_len);
            out.writeInt(this.time);
            out.writeInt(this.azimuth);
            out.writeInt(this.elevation);
            out.writeInt(this.target_elev);
            out.writeInt(this.vol_num);
            out.writeInt(this.tilt_num);
            out.writeInt(this.new_scan_limits);
            out.writeInt(this.end_of_tilt);
            out.writeInt(this.end_of_volume);
            out.writeInt(this.ray_num);
            out.writeInt(this.txmit_power_H);
            out.writeInt(this.txmit_power_V);
            out.writeInt(this.drx_rec1_average_power);
            out.writeInt(this.drx_rec2_average_power);
        } catch (IOException e) {
            System.err.println("IO message = " + e.getMessage());
            System.err.println("IO trace = ");
            e.printStackTrace();
        }
    }
}
