/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;


/**
 * Class for the structure task_rhi_scan_info
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class task_rhi_scan_info {

    private int start_elevation;
    private int end_elevation;
    private int[] list_azimuths = new int[40];
    private byte start_limit;

    task_rhi_scan_info(ByteBuffer in_buf) {
        int i = 0;
        start_elevation = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        end_elevation = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        for (i = 0; i < 40; i++) {
            list_azimuths[i] = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        }
        in_buf.position(in_buf.position() + 115);
        in_buf.get(start_limit);
    }

    /**
     * @return the start_elevation
     */
    public int getStart_elevation() {
        return start_elevation;
    }

    /**
     * @param start_elevation the start_elevation to set
     */
    public void setStart_elevation(int start_elevation) {
        this.start_elevation = start_elevation;
    }

    /**
     * @return the end_elevation
     */
    public int getEnd_elevation() {
        return end_elevation;
    }

    /**
     * @param end_elevation the end_elevation to set
     */
    public void setEnd_elevation(int end_elevation) {
        this.end_elevation = end_elevation;
    }

    /**
     * @return the list_azimuths
     */
    public int[] getList_azimuths() {
        return list_azimuths;
    }

    /**
     * @param list_azimuths the list_azimuths to set
     */
    public void setList_azimuths(int[] list_azimuths) {
        this.list_azimuths = list_azimuths;
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
