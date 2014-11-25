/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 * Class to represent task_dsp_info structure.
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 *         Completed
 */
public class task_dsp_info {
    private int major_mode;
    private int dsp_type;
    private dsp_data_mask current_data_type;
    private dsp_data_mask original_data_type;
    private task_dsp_mode_batch batch1;
    private int PRF;
    private int pulse_width;
    private int multi_PRF;
    private short dual_PRF_delay;
    private int AGC_feedback_code;
    private short sample_size;
    private int gain_control_flag;
    private String clutter_file;
    private byte linear_filter_number;
    private byte log_filter_number;
    private short attenuation;
    private int gas_attenuation;
    private int cluttermap_used;
    private int XMT_phase_sequence;
    private long ray_header_mask;
    private int ts_playback_flags;
    private String name_custom_ray_header;

    private int BeginPosition;
    private byte[] TempBuf;


    task_dsp_info(ByteBuffer in_buf) {

        BeginPosition = in_buf.position();
        major_mode = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        dsp_type = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        current_data_type = new dsp_data_mask(in_buf);
        original_data_type = new dsp_data_mask(in_buf);
        batch1 = new task_dsp_mode_batch(in_buf);
        in_buf.position(in_buf.position() + 52);
        PRF = in_buf.getInt();
        pulse_width = in_buf.getInt();
        multi_PRF = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        dual_PRF_delay = in_buf.getShort();
        AGC_feedback_code = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        sample_size = in_buf.getShort();
        gain_control_flag = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        try {
            TempBuf = new byte[12];
            in_buf.get(TempBuf);
            clutter_file = new String(TempBuf, "UTF-8");
        } catch (Exception e) {
            System.err.println("Exception:" + e);
        }

        linear_filter_number = in_buf.get();
        log_filter_number = in_buf.get();
        attenuation = in_buf.getShort();
        gas_attenuation = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        cluttermap_used = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        XMT_phase_sequence = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        ray_header_mask = UtilityClass.UINT4_to_long(in_buf.getInt());
        ts_playback_flags = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        in_buf.position(in_buf.position() + 2);
        try {
            TempBuf = new byte[16];
            in_buf.get(TempBuf);
            name_custom_ray_header = new String(TempBuf, "UTF-8");
        } catch (Exception e) {
            System.err.println("Exception:" + e);
        }
        in_buf.position(in_buf.position() + 120);

    }

    /**
     * @return the major_mode
     */
    public int getMajor_mode() {
        return major_mode;
    }

    /**
     * @param major_mode the major_mode to set
     */
    public void setMajor_mode(int major_mode) {
        this.major_mode = major_mode;
    }

    /**
     * @return the dsp_type
     */
    public int getDsp_type() {
        return dsp_type;
    }

    /**
     * @param dsp_type the dsp_type to set
     */
    public void setDsp_type(int dsp_type) {
        this.dsp_type = dsp_type;
    }

    /**
     * @return the current_data_type
     */
    public dsp_data_mask getCurrent_data_type() {
        return current_data_type;
    }

    /**
     * @param current_data_type the current_data_type to set
     */
    public void setCurrent_data_type(dsp_data_mask current_data_type) {
        this.current_data_type = current_data_type;
    }

    /**
     * @return the original_data_type
     */
    public dsp_data_mask getOriginal_data_type() {
        return original_data_type;
    }

    /**
     * @param original_data_type the original_data_type to set
     */
    public void setOriginal_data_type(dsp_data_mask original_data_type) {
        this.original_data_type = original_data_type;
    }

    /**
     * @return the batch1
     */
    public task_dsp_mode_batch getBatch1() {
        return batch1;
    }

    /**
     * @param batch1 the batch1 to set
     */
    public void setBatch1(task_dsp_mode_batch batch1) {
        this.batch1 = batch1;
    }

    /**
     * @return the PRF
     */
    public int getPRF() {
        return PRF;
    }

    /**
     * @param PRF the PRF to set
     */
    public void setPRF(int PRF) {
        this.PRF = PRF;
    }

    /**
     * @return the pulse_width
     */
    public int getPulse_width() {
        return pulse_width;
    }

    /**
     * @param pulse_width the pulse_width to set
     */
    public void setPulse_width(int pulse_width) {
        this.pulse_width = pulse_width;
    }

    /**
     * @return the multi_PRF
     */
    public int getMulti_PRF() {
        return multi_PRF;
    }

    /**
     * @param multi_PRF the multi_PRF to set
     */
    public void setMulti_PRF(int multi_PRF) {
        this.multi_PRF = multi_PRF;
    }

    /**
     * @return the dual_PRF_delay
     */
    public short getDual_PRF_delay() {
        return dual_PRF_delay;
    }

    /**
     * @param dual_PRF_delay the dual_PRF_delay to set
     */
    public void setDual_PRF_delay(short dual_PRF_delay) {
        this.dual_PRF_delay = dual_PRF_delay;
    }

    /**
     * @return the AGC_feedback_code
     */
    public int getAGC_feedback_code() {
        return AGC_feedback_code;
    }

    /**
     * @param AGC_feedback_code the AGC_feedback_code to set
     */
    public void setAGC_feedback_code(int AGC_feedback_code) {
        this.AGC_feedback_code = AGC_feedback_code;
    }

    /**
     * @return the sample_size
     */
    public short getSample_size() {
        return sample_size;
    }

    /**
     * @param sample_size the sample_size to set
     */
    public void setSample_size(short sample_size) {
        this.sample_size = sample_size;
    }

    /**
     * @return the gain_control_flag
     */
    public int getGain_control_flag() {
        return gain_control_flag;
    }

    /**
     * @param gain_control_flag the gain_control_flag to set
     */
    public void setGain_control_flag(int gain_control_flag) {
        this.gain_control_flag = gain_control_flag;
    }

    /**
     * @return the clutter_file
     */
    public String getClutter_file() {
        return clutter_file;
    }

    /**
     * @param clutter_file the clutter_file to set
     */
    public void setClutter_file(String clutter_file) {
        this.clutter_file = clutter_file;
    }

    /**
     * @return the linear_filter_number
     */
    public byte getLinear_filter_number() {
        return linear_filter_number;
    }

    /**
     * @param linear_filter_number the linear_filter_number to set
     */
    public void setLinear_filter_number(byte linear_filter_number) {
        this.linear_filter_number = linear_filter_number;
    }

    /**
     * @return the log_filter_number
     */
    public byte getLog_filter_number() {
        return log_filter_number;
    }

    /**
     * @param log_filter_number the log_filter_number to set
     */
    public void setLog_filter_number(byte log_filter_number) {
        this.log_filter_number = log_filter_number;
    }

    /**
     * @return the attenuation
     */
    public short getAttenuation() {
        return attenuation;
    }

    /**
     * @param attenuation the attenuation to set
     */
    public void setAttenuation(short attenuation) {
        this.attenuation = attenuation;
    }

    /**
     * @return the gas_attenuation
     */
    public int getGas_attenuation() {
        return gas_attenuation;
    }

    /**
     * @param gas_attenuation the gas_attenuation to set
     */
    public void setGas_attenuation(int gas_attenuation) {
        this.gas_attenuation = gas_attenuation;
    }

    /**
     * @return the cluttermap_used
     */
    public int getCluttermap_used() {
        return cluttermap_used;
    }

    /**
     * @param cluttermap_used the cluttermap_used to set
     */
    public void setCluttermap_used(int cluttermap_used) {
        this.cluttermap_used = cluttermap_used;
    }

    /**
     * @return the XMT_phase_sequence
     */
    public int getXMT_phase_sequence() {
        return XMT_phase_sequence;
    }

    /**
     * @param XMT_phase_sequence the XMT_phase_sequence to set
     */
    public void setXMT_phase_sequence(int XMT_phase_sequence) {
        this.XMT_phase_sequence = XMT_phase_sequence;
    }

    /**
     * @return the ray_header_mask
     */
    public long getRay_header_mask() {
        return ray_header_mask;
    }

    /**
     * @param ray_header_mask the ray_header_mask to set
     */
    public void setRay_header_mask(long ray_header_mask) {
        this.ray_header_mask = ray_header_mask;
    }

    /**
     * @return the ts_playback_flags
     */
    public int getTs_playback_flags() {
        return ts_playback_flags;
    }

    /**
     * @param ts_playback_flags the ts_playback_flags to set
     */
    public void setTs_playback_flags(int ts_playback_flags) {
        this.ts_playback_flags = ts_playback_flags;
    }

    /**
     * @return the name_custom_ray_header
     */
    public String getName_custom_ray_header() {
        return name_custom_ray_header;
    }

    /**
     * @param name_custom_ray_header the name_custom_ray_header to set
     */
    public void setName_custom_ray_header(String name_custom_ray_header) {
        this.name_custom_ray_header = name_custom_ray_header;
    }


}
