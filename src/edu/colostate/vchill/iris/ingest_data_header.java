/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 * Class responsible for representing and reading structure ingest_data_header
 * @author Joseph Hardin
 */
public class ingest_data_header {
    
    private structure_header size_of_file;
    private ymds_time sweep_start_time;
    private short sweep_number;
    private short resolution;
    private short index_first_ray;
    private short rays_expected;
    private short rays_present;
    private int fixed_angle;
    private short bits_per_bin;
    private int data_type;
    
    int BeginPosition;
    
    
    /**
     * Primary Constructor.
     * @param Input Stream
     **/
    
    public ingest_data_header(ByteBuffer in_buf)
    {

        BeginPosition = in_buf.position();
        
        size_of_file = new structure_header(in_buf);
        sweep_start_time = new ymds_time(in_buf);
        sweep_number = in_buf.getShort();
        resolution = in_buf.getShort();
        index_first_ray = in_buf.getShort();
        rays_expected = in_buf.getShort();
        rays_present = in_buf.getShort();
        fixed_angle = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        bits_per_bin = in_buf.getShort();
        data_type = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        in_buf.position(in_buf.position()+36);
//        System.out.println("data_type:"+data_type);
//        System.out.println("BitsPerBin:"+bits_per_bin);
    }

    /**
     * @return the size_of_file
     */
    public structure_header getSize_of_file() {
        return size_of_file;
    }

    /**
     * @param size_of_file the size_of_file to set
     */
    public void setSize_of_file(structure_header size_of_file) {
        this.size_of_file = size_of_file;
    }

    /**
     * @return the sweep_start_time
     */
    public ymds_time getSweep_start_time() {
        return sweep_start_time;
    }

    /**
     * @param sweep_start_time the sweep_start_time to set
     */
    public void setSweep_start_time(ymds_time sweep_start_time) {
        this.sweep_start_time = sweep_start_time;
    }

    /**
     * @return the sweep_number
     */
    public short getSweep_number() {
        return sweep_number;
    }

    /**
     * @param sweep_number the sweep_number to set
     */
    public void setSweep_number(short sweep_number) {
        this.sweep_number = sweep_number;
    }

    /**
     * @return the resolution
     */
    public short getResolution() {
        return resolution;
    }

    /**
     * @param resolution the resolution to set
     */
    public void setResolution(short resolution) {
        this.resolution = resolution;
    }

    /**
     * @return the index_first_ray
     */
    public short getIndex_first_ray() {
        return index_first_ray;
    }

    /**
     * @param index_first_ray the index_first_ray to set
     */
    public void setIndex_first_ray(short index_first_ray) {
        this.index_first_ray = index_first_ray;
    }

    /**
     * @return the rays_expected
     */
    public short getRays_expected() {
        return rays_expected;
    }

    /**
     * @param rays_expected the rays_expected to set
     */
    public void setRays_expected(short rays_expected) {
        this.rays_expected = rays_expected;
    }

    /**
     * @return the rays_present
     */
    public short getRays_present() {
        return rays_present;
    }

    /**
     * @param rays_present the rays_present to set
     */
    public void setRays_present(short rays_present) {
        this.rays_present = rays_present;
    }

    /**
     * @return the fixed_angle
     */
    public int getFixed_angle() {
        return fixed_angle;
    }

    /**
     * @param fixed_angle the fixed_angle to set
     */
    public void setFixed_angle(int fixed_angle) {
        this.fixed_angle = fixed_angle;
    }

    /**
     * @return the bits_per_bin
     */
    public short getBits_per_bin() {
        return bits_per_bin;
    }

    /**
     * @param bits_per_bin the bits_per_bin to set
     */
    public void setBits_per_bin(short bits_per_bin) {
        this.bits_per_bin = bits_per_bin;
    }

    /**
     * @return the data_type
     */
    public int getData_type() {
        return data_type;
    }

    /**
     * @param data_type the data_type to set
     */
    public void setData_type(int data_type) {
        this.data_type = data_type;
    }
}
