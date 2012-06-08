/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 * Class to represent task_dsp_mode_batch
 * @author Joseph Hardin <josephhardinee@gmail.com>
 * Completed
 */
public class task_dsp_mode_batch {
    
    private int low_prf;
    private int low_prf_fraction;
    private short low_prf_sample_size;
    private short low_prf_range_averaging;
    private short threshold_for_reflectivity_unfolding;
    private short threshold_for_velocity_unfolding;
    private short threshold_for_width_unfolding;

    public task_dsp_mode_batch(ByteBuffer in_buf){
        low_prf=UtilityClass.UINT2_to_SINT(in_buf.getShort());
        low_prf_fraction=UtilityClass.UINT2_to_SINT(in_buf.getShort());
        low_prf_sample_size=in_buf.getShort();
        low_prf_range_averaging=in_buf.getShort();
        threshold_for_reflectivity_unfolding=in_buf.getShort();
        threshold_for_velocity_unfolding=in_buf.getShort();
        threshold_for_width_unfolding=in_buf.getShort();
        in_buf.position(in_buf.position()+18);
        
    }

    /**
     * @return the low_prf
     */
    public int getLow_prf() {
        return low_prf;
    }

    /**
     * @param low_prf the low_prf to set
     */
    public void setLow_prf(int low_prf) {
        this.low_prf = low_prf;
    }

    /**
     * @return the low_prf_fraction
     */
    public int getLow_prf_fraction() {
        return low_prf_fraction;
    }

    /**
     * @param low_prf_fraction the low_prf_fraction to set
     */
    public void setLow_prf_fraction(int low_prf_fraction) {
        this.low_prf_fraction = low_prf_fraction;
    }

    /**
     * @return the low_prf_sample_size
     */
    public short getLow_prf_sample_size() {
        return low_prf_sample_size;
    }

    /**
     * @param low_prf_sample_size the low_prf_sample_size to set
     */
    public void setLow_prf_sample_size(short low_prf_sample_size) {
        this.low_prf_sample_size = low_prf_sample_size;
    }

    /**
     * @return the low_prf_range_averaging
     */
    public short getLow_prf_range_averaging() {
        return low_prf_range_averaging;
    }

    /**
     * @param low_prf_range_averaging the low_prf_range_averaging to set
     */
    public void setLow_prf_range_averaging(short low_prf_range_averaging) {
        this.low_prf_range_averaging = low_prf_range_averaging;
    }

    /**
     * @return the threshold_for_reflectivity_unfolding
     */
    public short getThreshold_for_reflectivity_unfolding() {
        return threshold_for_reflectivity_unfolding;
    }

    /**
     * @param threshold_for_reflectivity_unfolding the threshold_for_reflectivity_unfolding to set
     */
    public void setThreshold_for_reflectivity_unfolding(short threshold_for_reflectivity_unfolding) {
        this.threshold_for_reflectivity_unfolding = threshold_for_reflectivity_unfolding;
    }

    /**
     * @return the threshold_for_velocity_unfolding
     */
    public short getThreshold_for_velocity_unfolding() {
        return threshold_for_velocity_unfolding;
    }

    /**
     * @param threshold_for_velocity_unfolding the threshold_for_velocity_unfolding to set
     */
    public void setThreshold_for_velocity_unfolding(short threshold_for_velocity_unfolding) {
        this.threshold_for_velocity_unfolding = threshold_for_velocity_unfolding;
    }

    /**
     * @return the threshold_for_width_unfolding
     */
    public short getThreshold_for_width_unfolding() {
        return threshold_for_width_unfolding;
    }

    /**
     * @param threshold_for_width_unfolding the threshold_for_width_unfolding to set
     */
    public void setThreshold_for_width_unfolding(short threshold_for_width_unfolding) {
        this.threshold_for_width_unfolding = threshold_for_width_unfolding;
    }
}
