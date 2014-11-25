/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;


/**
 * Class to represent C struct ingest_configuration
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class ingest_configuration {

    private byte[] tempBuf;

    private String name_of_file;
    private short num_extant;

    private int size_of_all_files;
    private ymds_time vol_scan_start_time;

    private short bytes_in_ray_headers;
    private short bytes_in_ext_ray_headers;
    private short num_task_config;
    private short playback_version;

    private String iris_version;
    private String hardware_name_of_site;
    private short local_timezone_west_GMT;

    private String name_of_site;
    private short recorded_minutes_west_GMT;
    private double latitude;
    private double longitude;
    private short height_of_ground;
    private short height_of_radar;
    private int resolution;
    private int index_of_first_ray;
    private int num_rays_in_sweep;
    private int num_bytes_in_gparam;
    private int altitude_radar_cm;
    private int[] velocity_of_radar = new int[3];
    private int[] antenna_offset_from_INU = new int[3];
    private long fault_status;
    private short height_melting_layer;

    private String local_timezone;
    private long flags;
    private String config_name;


    private int BeginPosition;
    private byte[] TempBuf;
    private byte TempByte;

    public String getName_of_file() {
        return name_of_file;
    }


    public void setName_of_file(String name_of_file) {
        this.name_of_file = name_of_file;
    }


    public short getNum_extant() {
        return num_extant;
    }


    public void setNum_extant(short num_extant) {
        this.num_extant = num_extant;
    }


    public int getSize_of_all_files() {
        return size_of_all_files;
    }


    public void setSize_of_all_files(int size_of_all_files) {
        this.size_of_all_files = size_of_all_files;
    }


    public ymds_time getVol_scan_start_time() {
        return vol_scan_start_time;
    }


    public void setVol_scan_start_time(ymds_time vol_scan_start_time) {
        this.vol_scan_start_time = vol_scan_start_time;
    }


    public short getBytes_in_ray_headers() {
        return bytes_in_ray_headers;
    }


    public void setBytes_in_ray_headers(short bytes_in_ray_headers) {
        this.bytes_in_ray_headers = bytes_in_ray_headers;
    }


    public short getBytes_in_ext_ray_headers() {
        return bytes_in_ext_ray_headers;
    }


    public void setBytes_in_ext_ray_headers(short bytes_in_ext_ray_headers) {
        this.bytes_in_ext_ray_headers = bytes_in_ext_ray_headers;
    }


    public short getNum_task_config() {
        return num_task_config;
    }


    public void setNum_task_config(short num_task_config) {
        this.num_task_config = num_task_config;
    }


    public short getPlayback_version() {
        return playback_version;
    }


    public void setPlayback_version(short playback_version) {
        this.playback_version = playback_version;
    }


    public String getIris_version() {
        return iris_version;
    }


    public void setIris_version(String iris_version) {
        this.iris_version = iris_version;
    }


    public String getHardware_name_of_site() {
        return hardware_name_of_site;
    }


    public void setHardware_name_of_site(String hardware_name_of_site) {
        this.hardware_name_of_site = hardware_name_of_site;
    }


    public short getLocal_timezone_west_GMT() {
        return local_timezone_west_GMT;
    }


    public void setLocal_timezone_west_GMT(short local_timezone_west_GMT) {
        this.local_timezone_west_GMT = local_timezone_west_GMT;
    }


    public String getName_of_site() {
        return name_of_site;
    }


    public void setName_of_site(String name_of_site) {
        this.name_of_site = name_of_site;
    }


    public short getRecorded_minutes_west_GMT() {
        return recorded_minutes_west_GMT;
    }


    public void setRecorded_minutes_west_GMT(short recorded_minutes_west_GMT) {
        this.recorded_minutes_west_GMT = recorded_minutes_west_GMT;
    }


    public double getLatitude() {
        return latitude;
    }


    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    public double getLongitude() {
        return longitude;
    }


    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public short getHeight_of_ground() {
        return height_of_ground;
    }


    public void setHeight_of_ground(short height_of_ground) {
        this.height_of_ground = height_of_ground;
    }


    public short getHeight_of_radar() {
        return height_of_radar;
    }


    public void setHeight_of_radar(short height_of_radar) {
        this.height_of_radar = height_of_radar;
    }


    public int getResolution() {
        return resolution;
    }


    public void setResolution(int resolution) {
        this.resolution = resolution;
    }


    public int getIndex_of_first_ray() {
        return index_of_first_ray;
    }


    public void setIndex_of_first_ray(int index_of_first_ray) {
        this.index_of_first_ray = index_of_first_ray;
    }


    public int getNum_rays_in_sweep() {
        return num_rays_in_sweep;
    }


    public void setNum_rays_in_sweep(int num_rays_in_sweep) {
        this.num_rays_in_sweep = num_rays_in_sweep;
    }


    public int getNum_bytes_in_gparam() {
        return num_bytes_in_gparam;
    }


    public void setNum_bytes_in_gparam(int num_bytes_in_gparam) {
        this.num_bytes_in_gparam = num_bytes_in_gparam;
    }


    public int getAltitude_radar_cm() {
        return altitude_radar_cm;
    }


    public void setAltitude_radar_cm(int altitude_radar_cm) {
        this.altitude_radar_cm = altitude_radar_cm;
    }


    public int[] getVelocity_of_radar() {
        return velocity_of_radar;
    }


    public void setVelocity_of_radar(int[] velocity_of_radar) {
        this.velocity_of_radar = velocity_of_radar;
    }


    public int[] getAntenna_offset_from_INU() {
        return antenna_offset_from_INU;
    }


    public void setAntenna_offset_from_INU(int[] antenna_offset_from_INU) {
        this.antenna_offset_from_INU = antenna_offset_from_INU;
    }


    public long getFault_status() {
        return fault_status;
    }


    public void setFault_status(long fault_status) {
        this.fault_status = fault_status;
    }


    public short getHeight_melting_layer() {
        return height_melting_layer;
    }


    public void setHeight_melting_layer(short height_melting_layer) {
        this.height_melting_layer = height_melting_layer;
    }


    public String getLocal_timezone() {
        return local_timezone;
    }


    public void setLocal_timezone(String local_timezone) {
        this.local_timezone = local_timezone;
    }


    public long getFlags() {
        return flags;
    }


    public void setFlags(long flags) {
        this.flags = flags;
    }


    public String getConfig_name() {
        return config_name;
    }


    public void setConfig_name(String config_name) {
        this.config_name = config_name;
    }


    public ingest_configuration(ByteBuffer in_buf) {
        try {
            BeginPosition = in_buf.position();

            tempBuf = new byte[80];
            in_buf.get(tempBuf);//Name of File on disk
            name_of_file = new String(tempBuf, "UTF-8");
            num_extant = in_buf.getShort();

            in_buf.position(in_buf.position() + 2);

            size_of_all_files = in_buf.getInt();
            vol_scan_start_time = new ymds_time(in_buf);

            in_buf.position(in_buf.position() + 12);

            bytes_in_ray_headers = in_buf.getShort();
            bytes_in_ext_ray_headers = in_buf.getShort();
            num_task_config = in_buf.getShort();
            playback_version = in_buf.getShort();
            in_buf.position(in_buf.position() + 4);

            TempBuf = new byte[8];
            in_buf.get(TempBuf);
            iris_version = new String(TempBuf, "UTF-8");
            TempBuf = new byte[16];
            in_buf.get(TempBuf);
            hardware_name_of_site = new String(TempBuf, "UTF-8");
            local_timezone_west_GMT = in_buf.getShort();
            TempBuf = new byte[16];
            in_buf.get(TempBuf);
            name_of_site = new String(TempBuf, "UTF-8");
            recorded_minutes_west_GMT = in_buf.getShort();
            latitude = UtilityClass.UINT4_to_long(in_buf.getInt());
            latitude = 360 * (latitude / java.lang.Math.pow(2, 8 * 4));
            longitude = UtilityClass.UINT4_to_long(in_buf.getInt());
            longitude = 360 * (longitude / java.lang.Math.pow(2, 8 * 4));
            if (longitude > 180) longitude = -1 * (360 - longitude);
            height_of_ground = in_buf.getShort();
            height_of_radar = in_buf.getShort();
            resolution = UtilityClass.UINT2_to_SINT(in_buf.getShort());
            index_of_first_ray = UtilityClass.UINT2_to_SINT(in_buf.getShort());
            num_rays_in_sweep = UtilityClass.UINT2_to_SINT(in_buf.getShort());
            num_bytes_in_gparam = in_buf.getShort();
            altitude_radar_cm = in_buf.getInt();
            for (int i = 0; i < 3; i++) {
                velocity_of_radar[i] = in_buf.getInt();
            }
            for (int i = 0; i < 3; i++) {
                antenna_offset_from_INU[i] = in_buf.getInt();
            }
            fault_status = UtilityClass.UINT4_to_long(in_buf.getInt());
            height_melting_layer = in_buf.getShort();
            in_buf.position(in_buf.position() + 2);
            TempBuf = new byte[8];
            in_buf.get(TempBuf);
            local_timezone = new String(TempBuf, "UTF-8");
            flags = UtilityClass.UINT4_to_long(in_buf.getInt());
            TempBuf = new byte[16];
            in_buf.get(TempBuf);
            config_name = new String(TempBuf, "UTF-8");
            in_buf.position(in_buf.position() + 228);


        } catch (Exception e) {
            System.err.println("Exception:" + e);
        }
    }


}
