/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class task_scan_info {
    private int scan_mode;
    private short angular_resolution;
    private short num_sweeps;

    private task_rhi_scan_info atask_rhi_scan_info = null;
    private task_ppi_scan_info atask_ppi_scan_info = null;
    private task_file_scan_info atask_file_scan_info = null;
    private task_manual_scan_info atask_manual_scan_info = null;


    task_scan_info(ByteBuffer in_buf) {
        scan_mode = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        angular_resolution = in_buf.getShort();
        in_buf.position(in_buf.position() + 2);

        num_sweeps = in_buf.getShort();
        switch (scan_mode) {
            case 1:
                atask_ppi_scan_info = new task_ppi_scan_info(in_buf);
                break;
            case 2:
                atask_rhi_scan_info = new task_rhi_scan_info(in_buf);
                break;
            case 3:
                atask_manual_scan_info = new task_manual_scan_info(in_buf);
                break;
            case 4:
                atask_ppi_scan_info = new task_ppi_scan_info(in_buf);
                break;
            case 5:
                atask_file_scan_info = new task_file_scan_info(in_buf);
                break;
        }
        System.out.println("Scan Mode:" + scan_mode);
        in_buf.position(in_buf.position() + 112);

    }

    /**
     * @return the scan_mode
     */
    public int getScan_mode() {
        return scan_mode;
    }

    /**
     * @param scan_mode the scan_mode to set
     */
    public void setScan_mode(int scan_mode) {
        this.scan_mode = scan_mode;
    }

    /**
     * @return the angular_resolution
     */
    public short getAngular_resolution() {
        return angular_resolution;
    }

    /**
     * @param angular_resolution the angular_resolution to set
     */
    public void setAngular_resolution(short angular_resolution) {
        this.angular_resolution = angular_resolution;
    }

    /**
     * @return the num_sweeps
     */
    public short getNum_sweeps() {
        return num_sweeps;
    }

    /**
     * @param num_sweeps the num_sweeps to set
     */
    public void setNum_sweeps(short num_sweeps) {
        this.num_sweeps = num_sweeps;
    }

    /**
     * @return the atask_rhi_scan_info
     */
    public task_rhi_scan_info getAtask_rhi_scan_info() {
        return atask_rhi_scan_info;
    }

    /**
     * @param atask_rhi_scan_info the atask_rhi_scan_info to set
     */
    public void setAtask_rhi_scan_info(task_rhi_scan_info atask_rhi_scan_info) {
        this.atask_rhi_scan_info = atask_rhi_scan_info;
    }

    /**
     * @return the atask_ppi_scan_info
     */
    public task_ppi_scan_info getAtask_ppi_scan_info() {
        return atask_ppi_scan_info;
    }

    /**
     * @param atask_ppi_scan_info the atask_ppi_scan_info to set
     */
    public void setAtask_ppi_scan_info(task_ppi_scan_info atask_ppi_scan_info) {
        this.atask_ppi_scan_info = atask_ppi_scan_info;
    }

    /**
     * @return the atask_file_scan_info
     */
    public task_file_scan_info getAtask_file_scan_info() {
        return atask_file_scan_info;
    }

    /**
     * @param atask_file_scan_info the atask_file_scan_info to set
     */
    public void setAtask_file_scan_info(task_file_scan_info atask_file_scan_info) {
        this.atask_file_scan_info = atask_file_scan_info;
    }

    /**
     * @return the atask_manual_scan_info
     */
    public task_manual_scan_info getAtask_manual_scan_info() {
        return atask_manual_scan_info;
    }

    /**
     * @param atask_manual_scan_info the atask_manual_scan_info to set
     */
    public void setAtask_manual_scan_info(task_manual_scan_info atask_manual_scan_info) {
        this.atask_manual_scan_info = atask_manual_scan_info;
    }

}
