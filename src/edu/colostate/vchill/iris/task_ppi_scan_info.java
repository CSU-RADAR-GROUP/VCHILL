/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 * Class to represent task_ppi_scan_info structure.
 * @author Joseph Hardin <josephhardinee@gmail.com>
 * Complete
 */
public class task_ppi_scan_info {
    
    private int start_azimuth;
    private int end_azimuth;
    private int[] list_elevations = new int[40];
    private byte start_limit;
    
    task_ppi_scan_info(ByteBuffer in_buf){
        int i=0;
        start_azimuth=UtilityClass.UINT2_to_SINT(in_buf.getShort());
        end_azimuth=UtilityClass.UINT2_to_SINT(in_buf.getShort());
        for(i=0;i < 40; i++){
            list_elevations[i]=UtilityClass.UINT2_to_SINT(in_buf.getShort());
        }
        in_buf.position(in_buf.position()+115);
        in_buf.get(start_limit);
    }

    /**
     * @return the start_azimuth
     */
    public int getStart_azimuth() {
        return start_azimuth;
    }

    /**
     * @param start_azimuth the start_azimuth to set
     */
    public void setStart_azimuth(int start_azimuth) {
        this.start_azimuth = start_azimuth;
    }

    /**
     * @return the end_azimuth
     */
    public int getEnd_azimuth() {
        return end_azimuth;
    }

    /**
     * @param end_azimuth the end_azimuth to set
     */
    public void setEnd_azimuth(int end_azimuth) {
        this.end_azimuth = end_azimuth;
    }

    /**
     * @return the list_elevations
     */
    public int[] getList_elevations() {
        return list_elevations;
    }

    /**
     * @param list_elevations the list_elevations to set
     */
    public void setList_elevations(int[] list_elevations) {
        this.list_elevations = list_elevations;
    }

    /**
     * @return the start_limit
     */
    public byte getStart_limit() {
        return start_limit;
    }

    /**
     * @param start_limit the start_limit to set
     */
    public void setStart_limit(byte start_limit) {
        this.start_limit = start_limit;
    }
}
