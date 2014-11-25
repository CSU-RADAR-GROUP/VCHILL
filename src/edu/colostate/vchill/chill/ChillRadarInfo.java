package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.socket.SocketUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Contains general radar info and fixed calibration terms
 *
 * @author Jochen Deyke
 * @version 2006-08-10
 */
public class ChillRadarInfo extends ChillHeader {
    /**
     * maximum length (in bytes) of radar_name (UTF-8 encoded)
     */
    public static final int MAX_RADAR_NAME = 32;

    /**
     * UTF-8 encoded radar name
     */
    public String radar_name; //[MAX_RADAR_NAME];
    /**
     * Current latitude, in degrees
     */
    public float latitude_d;
    /**
     * Current longitude, in degrees
     */
    public float longitude_d;
    /**
     * Current altitude, in meters
     */
    public float altitude_m;
    /**
     * Antenna beamwidth, in degrees
     */
    public float beamwidth_d;
    /**
     * Radar wavelength, in centimeters
     */
    public float wavelength_cm;

    /**
     * Reserved field
     */
    public float unused1;
    /**
     * Reserved field
     */
    public float unused2;
    /**
     * Reserved field
     */
    public float unused3;
    /**
     * Reserved field
     */
    public float unused4;

    /**
     * Antenna gain, H pol, from ref point through antenna
     */
    public float gain_ant_h_db;
    /**
     * Antenna gain, V pol, from ref point through antenna
     */
    public float gain_ant_v_db;
    /**
     * Operator-settable ZDR cal base, in dB
     */
    public float zdr_cal_base_db;
    /**
     * Operator-settable phidp rotation, in degrees
     */
    public float phidp_rot_d;
    /**
     * Used to calculate dBZ, using
     * dBz = base_radar_const_db - pk_txmit_power - 2*ant_gain -
     * reciever_gain + dbu + 20*log(range/100km)
     * where dbu = 10*log10(i^2 + q^2)
     */
    public float base_radar_constant_db;
    /**
     * Range offset to first gate, in meters
     */
    public float range_offset_m;
    /**
     * Loss from ref. point power to power meter sensor, H channel
     */
    public float power_measurement_loss_h_db;
    /**
     * Loss from ref. point power to power meter sensor, V channel
     */
    public float power_measurement_loss_v_db;
    /**
     * Operator-settable ZDR cal base for VHS mode
     */
    public float zdr_cal_base_vhs_db;
    /**
     * Power into directional coupler when test set commanded to 0 dBm, H channel
     */
    public float test_power_h_db;
    /**
     * Power into directional coupler when test set commanded to 0 dBm, V channel
     */
    public float test_power_v_db;
    /**
     * Directional coupler forward loss, V channel
     */
    public float dc_loss_h_db;
    /**
     * Directional coupler forward loss, V channel
     */
    public float dc_loss_v_db;


    /**
     * size (in bytes) of this header (including ChillHeaderHeader, but not including extraData)
     */
    public static final int BYTE_SIZE = ChillHeaderHeader.BYTE_SIZE +
            MAX_RADAR_NAME +
            5 * ChillDefines.FLOAT_BYTE_SIZE +
            4 * ChillDefines.FLOAT_BYTE_SIZE +
            13 * ChillDefines.FLOAT_BYTE_SIZE;

    public ChillRadarInfo() {
        super(new ChillHeaderHeader(ChillDefines.GEN_MOM_DATA, BYTE_SIZE));
        super.extraData = new byte[0];
    }

    /**
     * Constructs a header by reading initial values from a DataInput.
     *
     * @param in the DataInput to read initialization values from
     */
    public ChillRadarInfo(final DataInput in, final ChillHeaderHeader header) throws IOException {
        super(header);
        assert header.recordType == ChillDefines.HSK_ID_RADAR_INFO;
        assert header.headerLength - ChillRadarInfo.BYTE_SIZE >= 0;
        this.radar_name = SocketUtil.readString(in, MAX_RADAR_NAME);
        this.latitude_d = in.readFloat();
        this.longitude_d = in.readFloat();
        this.altitude_m = in.readFloat();
        this.beamwidth_d = in.readFloat();
        this.wavelength_cm = in.readFloat();
        this.unused1 = in.readFloat();
        this.unused2 = in.readFloat();
        this.unused3 = in.readFloat();
        this.unused4 = in.readFloat();
        this.gain_ant_h_db = in.readFloat();
        this.gain_ant_v_db = in.readFloat();
        this.zdr_cal_base_db = in.readFloat();
        this.phidp_rot_d = in.readFloat();
        this.base_radar_constant_db = in.readFloat();
        this.range_offset_m = in.readFloat();
        this.power_measurement_loss_h_db = in.readFloat();
        this.power_measurement_loss_v_db = in.readFloat();
        this.zdr_cal_base_vhs_db = in.readFloat();
        this.test_power_h_db = in.readFloat();
        this.test_power_v_db = in.readFloat();
        this.dc_loss_h_db = in.readFloat();
        this.dc_loss_v_db = in.readFloat();
        in.readFully(super.extraData = new byte[header.headerLength - ChillRadarInfo.BYTE_SIZE]);
    }

    /**
     * Writes this header to a DataOut
     *
     * @param out the DataOutput to write values to
     */
    public void write(final DataOutput out) throws IOException {
        assert header.recordType == ChillDefines.HSK_ID_RADAR_INFO;
        assert header.headerLength == ChillRadarInfo.BYTE_SIZE + extraData.length;
        super.header.write(out);
        SocketUtil.writeString(this.radar_name, out, MAX_RADAR_NAME);
        out.writeFloat(this.latitude_d);
        out.writeFloat(this.longitude_d);
        out.writeFloat(this.altitude_m);
        out.writeFloat(this.beamwidth_d);
        out.writeFloat(this.wavelength_cm);
        out.writeFloat(this.unused1);
        out.writeFloat(this.unused2);
        out.writeFloat(this.unused3);
        out.writeFloat(this.unused4);
        out.writeFloat(this.gain_ant_h_db);
        out.writeFloat(this.gain_ant_v_db);
        out.writeFloat(this.zdr_cal_base_db);
        out.writeFloat(this.phidp_rot_d);
        out.writeFloat(this.base_radar_constant_db);
        out.writeFloat(this.range_offset_m);
        out.writeFloat(this.power_measurement_loss_h_db);
        out.writeFloat(this.power_measurement_loss_v_db);
        out.writeFloat(this.zdr_cal_base_vhs_db);
        out.writeFloat(this.test_power_h_db);
        out.writeFloat(this.test_power_v_db);
        out.writeFloat(this.dc_loss_h_db);
        out.writeFloat(this.dc_loss_v_db);
        out.write(this.extraData);
    }
}
