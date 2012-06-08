
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 * Class to represent structure for task_calib_info
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class task_calib_info {
    private short reflectivity_slope;
    private short reflectivity_noise_threshold;
    private short clutter_corr_threshold;
    private short SQI_threshold;
    private short power_threshold;
    
    private short calibration_reflectivity;
    private int thresh_flag_uncorrected_reflectivity;
    private int thresh_flag_corrected_reflectivity;
    private int thresh_flag_velocity;
    private int thresh_flag_width;
    private int thresh_flag_zdr;
    
    private int flags;
    
    private short ldr_bias;
    private short zdr_bias;
    private short nexrad_point_clutter_thresh;
    private int nexrad_point_clutter_bin_skip;
    private short io_cal_h;
    private short io_cal_v;
    private short noise_cal_h;
    private short noise_cal_v;
    private short C_h;
    private short C_v;
    private int receiver_bandwidth;
    
    int BeginPosition;
    
    public task_calib_info(ByteBuffer in_buf){
        BeginPosition = in_buf.position();
        
       reflectivity_slope=in_buf.getShort();
       reflectivity_noise_threshold=in_buf.getShort();
       clutter_corr_threshold=in_buf.getShort();
       SQI_threshold=in_buf.getShort();
       power_threshold=in_buf.getShort();
       
       in_buf.position(in_buf.position()+8);
       
       calibration_reflectivity=in_buf.getShort();
       thresh_flag_uncorrected_reflectivity=UtilityClass.UINT2_to_SINT(in_buf.getShort());
       thresh_flag_corrected_reflectivity=UtilityClass.UINT2_to_SINT(in_buf.getShort());
       thresh_flag_velocity=UtilityClass.UINT2_to_SINT(in_buf.getShort());
       thresh_flag_width=UtilityClass.UINT2_to_SINT(in_buf.getShort());
       thresh_flag_zdr=UtilityClass.UINT2_to_SINT(in_buf.getShort());
       
       in_buf.position(in_buf.position()+6);
       
       flags=UtilityClass.UINT2_to_SINT(in_buf.getShort());
       
       in_buf.position(in_buf.position()+2);
       
       ldr_bias=in_buf.getShort();
       zdr_bias=in_buf.getShort();
       nexrad_point_clutter_thresh=in_buf.getShort();
       nexrad_point_clutter_bin_skip=UtilityClass.UINT2_to_SINT(in_buf.getShort());
       io_cal_h=in_buf.getShort();
       io_cal_v=in_buf.getShort();
       noise_cal_h=in_buf.getShort();
       noise_cal_v=in_buf.getShort();
       C_h=in_buf.getShort();
       C_v=in_buf.getShort();
       receiver_bandwidth=UtilityClass.UINT2_to_SINT(in_buf.getShort());
       in_buf.position(in_buf.position()+258);//For correct Spacing
       
       
    }
    
    
}
