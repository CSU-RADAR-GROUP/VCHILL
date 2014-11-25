package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 * Class to represent product_end structure
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class product_end {

    private String site_name;
    private String iris_version_product;
    private String iris_version_ingest;
    private ymds_time oldest_ingest;

    private short local_time_offset;
    private String hardware_name_ingest;
    private String site_name_ingest;
    private short minutes_recorded_west;
    private long lat_of_center;
    private long lon_of_center;
    private short ground_height;
    private short radar_height;
    private int PRF;
    private int pulse_width;
    private int dsp_type;
    private int trigger_rate_scheme;
    private short samples_used;
    private String clutter_filter_file;
    private int num_linear_filter;
    private int wavelength;
    private int truncation_height;
    private int range_first_bin;
    private int range_last_bin;
    private int num_output_bins;
    private int flag;
    private short ingest_files_used;
    private int polarization_type;
    private short io_cal_h;
    private short noise_cal_h;
    private short C_h;
    private int receiver_bandwidth;
    private short current_noise_h;

    private long projection_info;
    private long standard_parallel_2;
    private long equatorial_radius;
    private long flattening;
    private long fault_status;
    private long mask_input_sites;
    private int num_of_log_filters;
    private int cluttermap_applied;
    private long latitude_proj_ref;
    private long longitude_proj_ref;
    private short product_sequence_number;

    private short melting_level;
    private short height_radar_above_ref;
    private short elements_in_product_array;
    private short mean_wind_speed;
    private int mean_wind_direction;

    private String tz_name_data;

    private int BeginPosition;
    private byte[] TempBuf;
    private byte TempByte;

    public product_end(ByteBuffer in_buf) {
        BeginPosition = in_buf.position();
        try {
            TempBuf = new byte[16];
            in_buf.get(TempBuf);
            site_name = new String(TempBuf, "UTF-8");
            TempBuf = new byte[8];
            in_buf.get(TempBuf);
            iris_version_product = new String(TempBuf, "UTF-8");
            TempBuf = new byte[8];
            in_buf.get(TempBuf);
            iris_version_ingest = new String(TempBuf, "UTF-8");
            oldest_ingest = new ymds_time(in_buf);

            in_buf.position(in_buf.position() + 28);

            local_time_offset = in_buf.getShort();
            TempBuf = new byte[16];
            in_buf.get(TempBuf);
            hardware_name_ingest = new String(TempBuf, "UTF-8");
            TempBuf = new byte[16];
            in_buf.get(TempBuf);
            site_name_ingest = new String(TempBuf, "UTF-8");
            minutes_recorded_west = in_buf.getShort();
            lat_of_center = UtilityClass.UINT4_to_long(in_buf.getInt());
            lon_of_center = UtilityClass.UINT4_to_long(in_buf.getInt());
            ground_height = in_buf.getShort();
            radar_height = in_buf.getShort();
            PRF = in_buf.getInt();
            pulse_width = in_buf.getInt();
            dsp_type = UtilityClass.UINT2_to_SINT(in_buf.getShort());
            trigger_rate_scheme = UtilityClass.UINT2_to_SINT(in_buf.getShort());
            samples_used = in_buf.getShort();
            TempBuf = new byte[12];
            in_buf.get(TempBuf);
            clutter_filter_file = new String(TempBuf, "UTF-8");
            num_linear_filter = UtilityClass.UINT2_to_SINT(in_buf.getShort());
            wavelength = in_buf.getInt();
            truncation_height = in_buf.getInt();
            range_first_bin = in_buf.getInt();
            range_last_bin = in_buf.getInt();
            num_output_bins = in_buf.getInt();
            flag = UtilityClass.UINT2_to_SINT(in_buf.getShort());
            ingest_files_used = in_buf.getShort();
            polarization_type = UtilityClass.UINT2_to_SINT(in_buf.getShort());
            io_cal_h = in_buf.getShort();
            noise_cal_h = in_buf.getShort();
            C_h = in_buf.getShort();
            receiver_bandwidth = in_buf.getShort();
            current_noise_h = in_buf.getShort();
            in_buf.position(in_buf.position() + 28);


            projection_info = UtilityClass.UINT4_to_long(in_buf.getInt());
            standard_parallel_2 = UtilityClass.UINT4_to_long(in_buf.getInt());
            equatorial_radius = UtilityClass.UINT4_to_long(in_buf.getInt());
            flattening = UtilityClass.UINT4_to_long(in_buf.getInt());
            fault_status = UtilityClass.UINT4_to_long(in_buf.getInt());
            mask_input_sites = UtilityClass.UINT4_to_long(in_buf.getInt());
            num_of_log_filters = UtilityClass.UINT2_to_SINT(in_buf.getShort());
            cluttermap_applied = UtilityClass.UINT2_to_SINT(in_buf.getShort());
            latitude_proj_ref = UtilityClass.UINT4_to_long(in_buf.getInt());
            longitude_proj_ref = UtilityClass.UINT4_to_long(in_buf.getInt());
            product_sequence_number = in_buf.getShort();

            in_buf.position(in_buf.position() + 32);

            melting_level = in_buf.getShort();
            height_radar_above_ref = in_buf.getShort();
            elements_in_product_array = in_buf.getShort();
            TempByte = in_buf.get();
            mean_wind_speed = TempByte;
            TempByte = in_buf.get();
            mean_wind_direction = TempByte;

            in_buf.position(in_buf.position() + 2);

            TempBuf = new byte[8];
            in_buf.get(TempBuf);
            tz_name_data = new String(TempBuf, "UTF-8");

            in_buf.position(in_buf.position() + 8);


        } catch (Exception e) {
            System.err.println("Exception:" + e);
        }

    }
}
