/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 * Completed
 */
public class task_configuration {
    
    private structure_header astructure_header;
    private task_sched_info atask_sched_info;
    private task_dsp_info atask_dsp_info;
    private task_calib_info atask_calib_info;
    private task_range_info atask_range_info;
    private task_misc_info atask_misc_info;
    private task_end_info atask_end_info;
    private task_scan_info atask_scan_info;
    
    int BeginPosition;
    
    
    
    public task_configuration(ByteBuffer in_buf){
        BeginPosition=in_buf.position();
        
        astructure_header=new structure_header(in_buf);
        in_buf.position(BeginPosition+12);
        
        atask_sched_info = new task_sched_info(in_buf);
        in_buf.position(BeginPosition+12+120);
        
        atask_dsp_info = new task_dsp_info(in_buf);
        in_buf.position(BeginPosition+132+320);
        
        atask_calib_info = new task_calib_info(in_buf);
        in_buf.position(BeginPosition+452+320);
        
        atask_range_info = new task_range_info(in_buf);
        in_buf.position(BeginPosition+772+160);
        
        atask_scan_info = new task_scan_info(in_buf);
        in_buf.position(BeginPosition+932+320);
        
        atask_misc_info = new task_misc_info(in_buf);
        in_buf.position(BeginPosition+1252+320);
        
        atask_end_info = new task_end_info(in_buf);
        in_buf.position(BeginPosition+1572+320+720);
        
                
        
        
    }

    /**
     * @return the astructure_header
     */
    public structure_header getAstructure_header() {
        return astructure_header;
    }

    /**
     * @param astructure_header the astructure_header to set
     */
    public void setAstructure_header(structure_header astructure_header) {
        this.astructure_header = astructure_header;
    }

    /**
     * @return the atask_sched_info
     */
    public task_sched_info getAtask_sched_info() {
        return atask_sched_info;
    }

    /**
     * @param atask_sched_info the atask_sched_info to set
     */
    public void setAtask_sched_info(task_sched_info atask_sched_info) {
        this.atask_sched_info = atask_sched_info;
    }

    /**
     * @return the atask_dsp_info
     */
    public task_dsp_info getAtask_dsp_info() {
        return atask_dsp_info;
    }

    /**
     * @param atask_dsp_info the atask_dsp_info to set
     */
    public void setAtask_dsp_info(task_dsp_info atask_dsp_info) {
        this.atask_dsp_info = atask_dsp_info;
    }

    /**
     * @return the atask_calib_info
     */
    public task_calib_info getAtask_calib_info() {
        return atask_calib_info;
    }

    /**
     * @param atask_calib_info the atask_calib_info to set
     */
    public void setAtask_calib_info(task_calib_info atask_calib_info) {
        this.atask_calib_info = atask_calib_info;
    }

    /**
     * @return the atask_range_info
     */
    public task_range_info getAtask_range_info() {
        return atask_range_info;
    }

    /**
     * @param atask_range_info the atask_range_info to set
     */
    public void setAtask_range_info(task_range_info atask_range_info) {
        this.atask_range_info = atask_range_info;
    }

    /**
     * @return the atask_misc_info
     */
    public task_misc_info getAtask_misc_info() {
        return atask_misc_info;
    }

    /**
     * @param atask_misc_info the atask_misc_info to set
     */
    public void setAtask_misc_info(task_misc_info atask_misc_info) {
        this.atask_misc_info = atask_misc_info;
    }

    /**
     * @return the atask_end_info
     */
    public task_end_info getAtask_end_info() {
        return atask_end_info;
    }

    /**
     * @param atask_end_info the atask_end_info to set
     */
    public void setAtask_end_info(task_end_info atask_end_info) {
        this.atask_end_info = atask_end_info;
    }

    /**
     * @return the atask_scan_info
     */
    public task_scan_info getAtask_scan_info() {
        return atask_scan_info;
    }

    /**
     * @param atask_scan_info the atask_scan_info to set
     */
    public void setAtask_scan_info(task_scan_info atask_scan_info) {
        this.atask_scan_info = atask_scan_info;
    }
    
}
