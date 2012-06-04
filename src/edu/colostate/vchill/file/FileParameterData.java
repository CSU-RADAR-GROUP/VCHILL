package edu.colostate.vchill.file;

import edu.colostate.vchill.ChillDefines;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

/**
 * Reads Parameter data header
 *
 * @author  Justin Carlson
 * @author  Brian Eriksson
 * @author  Jochen Deyke
 * @created June 17, 2003
 * @version 2005-11-09
 */
public class FileParameterData
{
    private static final int RANGE_OFFSET = 600000; //600m
    private static final int STR_LEN = 8;

    /** unique number */
    public int radar_id;

    /** bytes */
    public int added_struct_size;

    /** meters */
    public int altitude;

    /** degrees * 1000000 */
    public int latitude;

    /** degrees * 1000000 */
    public int longitude;

    /** number of gates archived */
    public int ngates;

    /** millimeters - this includes the affect of any rangeaveraging */
    public int gate_spacing;

    /** millimeters */
    public int start_range;

    /** degrees * 1000000 */
    public int beam_width;

    /** Transmit pulses per integration cycle */
    public int samples_per_beam;

    /** nano-seconds */
    public int pulse_width;

    /** pulse repetition frequency * 1000 */
    public int prf;

    /** micrometers */
    public int wavelength;

    /** Number of fields */
    public int nfields;

    /** scan start time */
    public int volume_start_time;

    /**
     * GATE_DATA_SURVEILLANCE_MODE or
     * GATE_DATA_SECTOR_MODE or
     * GATE_DATA_RHI_MODE or
     * GATE_DATA_UNKNOWN_MODE
     */
    public int scan_mode;

    /**
     * TRUE (1) if radar field data is stored
     * as contiguous blocks, FALSE (0) if each
     * field for a given gate is stored
     * consecutively followed by the fields
     * for the next gate etc.
     */
    public int data_field_by_field;

    /**
     * the number of fields currently being
     * sent - the positions of the fields
     * are indicated by the
     * bits set in the field_flag
     */
    public int nfields_current;

    /**
     * for each field included in the beam data,
     * the relevant bit is set in this long.
     * For example, suppose there are a total
     * of 6 fields referred to in the params
     * struct, and only fields
     * 0, 1, 3, and 5 are currently in
     * the data stream.
     * Then, field_flag = 00.....0101011
     */
    public int field_flag;

    /**
     * number of bytes till next param pkt,
     * (includes this parm pkt)
     */
    public int sweep_bytes;

    /** volume number */
    public int vol_num;

    /** sweep number */
    public int tilt_num;

    /**
     * 0 = V only, 1 = H only,
     * 2 = VH alternating, 3 = VHS simultaneously
     */
    public int processor_mode;

    /** digitization width in nanoseconds */
    public int drx_gate_width;

    /** current clutter filter setting */
    public int drx_clutter_filter;

    /** zdr calibration applied, dB*1000 */
    public int drx_zdr_bias;

    /** dB * 1000 bias applied for ldr when H transmits */
    public int drx_ldrh_bias;

    /** dB * 1000 bias applied for ldr when V transmits */
    public int drx_ldrv_bias;

    /** rotation applied to phidp,  degress*1000000 */
    public int phidp_rot;

    /** receiver 1 noise level setting dB*1000 */
    public int drx_rec1_noise;

    /** receiver 2 noise level setting dB*1000 */
    public int drx_rec2_noise;

    /** constant added to get dBZ at 1km, dB*1000 */
    public int drx_rec1_Zcon;

    /** constant added to get dBZ at 1km, dB*1000 */
    public int drx_rec2_Zcon;

    /** average power in last half of range */
    public int drx_rec1_average_power;

    /** average power in last half of range */
    public int drx_rec2_average_power;

    /**
     * range averaging of covariances,
     * #gates averaged together
     */
    public int cov_range_avg;

    /** range to first time series gate in mm */
    public int ts_range1;

    /** time series gate spacing in mm */
    public int ts_gate_space;

    /** from norm_packet - NON_CONTIG_TS */
    public int ts_flags;

    /** number of gates processed in DRX */
    public int proc_gates;

    /** max height above ground to be archived - in meters */
    public int max_top;

    /**
     * 0 = ppi,  3 = ppi with manual elevation control
     * 1 = rhi,  4 = rhi with manual azimuth control
     * 2 = manual position scan
     * 5 = idle  (not scanning)
     */
    public int chill_scan_mode;

    /**
     * flags 'or'd together:
     * 1 = sector mode on
     * 2 = scan optimizer on
     * 4 = calibration power unstable
     * 8 = received samples adjusted based on transmit sample
     */
    public int scan_flags;

    /** left scan limit, degrees*1000000 */
    public int left_limit;

    /** right scan limit, degrees*1000000 */
    public int right_limit;

    /** up scan limit, degrees*1000000 */
    public int up_limit;

    /** down scan limit, degrees*1000000 */
    public int down_limit;

    /** antenna scan rate, degrees*1000000/sec */
    public int scan_rate;

    /** scan segment name, null terminated */
    public char scan_seg[];

    /** horizontal peak transmit power dBm*100 */
    public int txmit_power_H;

    /** vertical peak transmit power  dBm*100 */
    public int txmit_power_V;

    /**
     * current test type
     *
     * 0 = no test, 1 = ZDR cal, 2 = Sun Fixed, 3 = H rec, 4 = V rec,
     * 5 = Sun ppi, 6 = Blue Sky (noise sample)
     */
    public int test_type;

    /** current test set power, dBm*100 */
    public int test_power;

    /**
     * 0 = unknown, 1 = test power to both chan
     * 2 = power to h only, 3 = power to v only,
     * 4 = power to h meter,  5 = power to v meter
     */
    public int test_hardware_setup;

    /**
     * Track mode identfier:  'NO' = track mode off
     * 'SUN' = tracking Sun
     * name = tracking named vehicle
     */
    public char track_mode[];

    /** scan optimizer maximum range, meters */
    public int optimizer_rmax;

    /** scan optimizer maximum height, meters */
    public int optimizer_htmax;

    /** scan optimizer desired minimum resolution, meters */
    public int optimizer_resol;

    /** extra data (added_struct_size bytes); not interpreted */
    public byte[] extra_data;

    /**
     * Constructor for the FileParameterData object
     */
    public FileParameterData ()
    {
        this.track_mode = new char[STR_LEN];
        this.scan_seg = new char[STR_LEN];
    }

    public int byteSize ()
    {
        return
            ChillDefines.INT_BYTE_SIZE +
            ChillDefines.SHORT_BYTE_SIZE * 2+
            ChillDefines.INT_BYTE_SIZE * 45 +
            STR_LEN +
            ChillDefines.INT_BYTE_SIZE * 5 +
            STR_LEN +
            ChillDefines.INT_BYTE_SIZE * 3 +
            added_struct_size;
    }

    /**
     * Inputs data from the header
     *
     * @param in DataInput object containing CHILL file inputted
     * @return false if an IOException ocurred while reading
     * @throws EOFException Thrown if end of file is found
     */
    public boolean inputData (final DataInput in)
    {
        try {
            this.radar_id = in.readInt();
            this.added_struct_size = 0xffff & in.readShort();
            this.altitude = 0xffff & in.readShort();
            this.latitude = in.readInt();
            this.longitude = in.readInt();
            this.ngates = in.readInt();
            this.gate_spacing = in.readInt();
            this.start_range = in.readInt() - RANGE_OFFSET;
            this.beam_width = in.readInt();
            this.samples_per_beam = in.readInt();
            this.pulse_width = in.readInt();
            this.prf = in.readInt();
            this.wavelength = in.readInt();
            this.nfields = in.readInt();
            this.volume_start_time = in.readInt();
            this.scan_mode = in.readInt();
            this.data_field_by_field = in.readInt();
            this.nfields_current = in.readInt();
            this.field_flag = in.readInt();
            this.sweep_bytes = in.readInt();
            this.vol_num = in.readInt();
            this.tilt_num = in.readInt();
            this.processor_mode = in.readInt();
            this.drx_gate_width = in.readInt();
            this.drx_clutter_filter = in.readInt();
            this.drx_zdr_bias = in.readInt();
            this.drx_ldrh_bias = in.readInt();
            this.drx_ldrv_bias = in.readInt();
            this.phidp_rot = in.readInt();
            this.drx_rec1_noise = in.readInt();
            this.drx_rec2_noise = in.readInt();
            this.drx_rec1_Zcon = in.readInt();
            this.drx_rec2_Zcon = in.readInt();
            this.drx_rec1_average_power = in.readInt();
            this.drx_rec2_average_power = in.readInt();
            this.cov_range_avg = in.readInt();
            this.ts_range1 = in.readInt();
            this.ts_gate_space = in.readInt();
            this.ts_flags = in.readInt();
            this.proc_gates = in.readInt();
            this.max_top = in.readInt();
            this.chill_scan_mode = in.readInt();
            this.scan_flags = in.readInt();
            this.left_limit = in.readInt();
            this.right_limit = in.readInt();
            this.up_limit = in.readInt();
            this.down_limit = in.readInt();
            this.scan_rate = in.readInt();
            for (int i = 0; i < scan_seg.length; i++) {
                this.scan_seg[i] = (char)in.readByte();
                //char array
            }
            this.txmit_power_H = in.readInt();
            this.txmit_power_V = in.readInt();
            this.test_type = in.readInt();
            this.test_power = in.readInt();
            this.test_hardware_setup = in.readInt();
            for (int i = 0; i < track_mode.length; i++) {
                this.track_mode[i] = (char)in.readByte();
            }
            this.optimizer_rmax = in.readInt();
            this.optimizer_htmax = in.readInt();
            this.optimizer_resol = in.readInt();
            in.readFully(this.extra_data = new byte[this.added_struct_size]);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Outputs data from the header
     *
     * @param out DataOutput object containing CHILL file outputted
     * @return false if an IOException ocurred while reading
     * @throws EOFException Thrown if end of file is found
     */
    public boolean outputData (final DataOutput out) throws EOFException
    {
        try {
            out.writeInt(this.radar_id);
            out.writeShort((short)this.added_struct_size);
            out.writeShort((short)this.altitude);
            out.writeInt(this.latitude);
            out.writeInt(this.longitude);
            out.writeInt(this.ngates);

            out.writeInt(this.gate_spacing);
            out.writeInt(this.start_range + RANGE_OFFSET);
            out.writeInt(this.beam_width);
            out.writeInt(this.samples_per_beam);
            out.writeInt(this.pulse_width);
            out.writeInt(this.prf);
            out.writeInt(this.wavelength);
            out.writeInt(this.nfields);

            out.writeInt(this.volume_start_time);
            out.writeInt(this.scan_mode);
            out.writeInt(this.data_field_by_field);
            out.writeInt(this.nfields_current);
            out.writeInt(this.field_flag);
            out.writeInt(this.sweep_bytes);
            out.writeInt(this.vol_num);
            out.writeInt(this.tilt_num);

            out.writeInt(this.processor_mode);
            out.writeInt(this.drx_gate_width);
            out.writeInt(this.drx_clutter_filter);
            out.writeInt(this.drx_zdr_bias);
            out.writeInt(this.drx_ldrh_bias);
            out.writeInt(this.drx_ldrv_bias);
            out.writeInt(this.phidp_rot);
            out.writeInt(this.drx_rec1_noise);
            out.writeInt(this.drx_rec2_noise);
            out.writeInt(this.drx_rec1_Zcon);
            out.writeInt(this.drx_rec2_Zcon);
            out.writeInt(this.drx_rec1_average_power);
            out.writeInt(this.drx_rec2_average_power);
            out.writeInt(this.cov_range_avg);
            out.writeInt(this.ts_range1);
            out.writeInt(this.ts_gate_space);
            out.writeInt(this.ts_flags);
            out.writeInt(this.proc_gates);
            out.writeInt(this.max_top);
            out.writeInt(this.chill_scan_mode);

            out.writeInt(this.scan_flags);

            out.writeInt(this.left_limit);
            out.writeInt(this.right_limit);
            out.writeInt(this.up_limit);
            out.writeInt(this.down_limit);
            out.writeInt(this.scan_rate);

            for (int i = 0; i < scan_seg.length; i++) {
                out.writeByte((byte)this.scan_seg[i]);
                //char array
            }

            out.writeInt(this.txmit_power_H);
            out.writeInt(this.txmit_power_V);
            out.writeInt(this.test_type);

            out.writeInt(this.test_power);
            out.writeInt(this.test_hardware_setup);

            for (int i = 0; i < track_mode.length; i++) {
                out.writeByte((byte)this.track_mode[i]);
            }

            out.writeInt(this.optimizer_rmax);
            out.writeInt(this.optimizer_htmax);
            out.writeInt(this.optimizer_resol);
            out.write(this.extra_data);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public String getRadarName ()
    {
        switch (this.radar_id) {
            case 3: return "CHILL";
            case 4: return "Pawnee";
            default: return String.valueOf(this.radar_id);
        }
    }

    public String getScanMode ()
    {
        switch (this.chill_scan_mode) {
            case 0: case 3: return "PPI";
            case 1: case 4: return "RHI";
            default: return "MAN";
        }
    }
}
