/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

/**
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
import java.nio.ByteBuffer;


public class RayHeader {

    private double begin_azi;
    private double begin_elv;
    private double end_azi;
    private double end_elv;
    private int bins_in_ray;
    private int s_from_start;
    private int flag;
    
    public RayHeader(ByteBuffer in_buf)
    {
        begin_azi=UtilityClass.BIN2_to_double(in_buf.getShort());
        begin_elv=UtilityClass.BIN2_to_double(in_buf.getShort());
        end_azi=UtilityClass.BIN2_to_double(in_buf.getShort());
        end_elv=UtilityClass.BIN2_to_double(in_buf.getShort());
        bins_in_ray=in_buf.getShort();
        s_from_start=UtilityClass.UINT2_to_SINT(in_buf.getShort());
        
    }

    /**
     * @return the begin_azi
     */
    public double getBegin_azi() {
        return begin_azi;
    }

    /**
     * @param begin_azi the begin_azi to set
     */
    public void setBegin_azi(double begin_azi) {
        this.begin_azi = begin_azi;
    }

    /**
     * @return the begin_elv
     */
    public double getBegin_elv() {
        return begin_elv;
    }

    /**
     * @param begin_elv the begin_elv to set
     */
    public void setBegin_elv(double begin_elv) {
        this.begin_elv = begin_elv;
    }

    /**
     * @return the end_azi
     */
    public double getEnd_azi() {
        return end_azi;
    }

    /**
     * @param end_azi the end_azi to set
     */
    public void setEnd_azi(double end_azi) {
        this.end_azi = end_azi;
    }

    /**
     * @return the end_elv
     */
    public double getEnd_elv() {
        return end_elv;
    }

    /**
     * @param end_elv the end_elv to set
     */
    public void setEnd_elv(double end_elv) {
        this.end_elv = end_elv;
    }

    /**
     * @return the bins_in_ray
     */
    public int getBins_in_ray() {
        return bins_in_ray;
    }

    /**
     * @param bins_in_ray the bins_in_ray to set
     */
    public void setBins_in_ray(int bins_in_ray) {
        this.bins_in_ray = bins_in_ray;
    }

    /**
     * @return the s_from_start
     */
    public int getS_from_start() {
        return s_from_start;
    }

    /**
     * @param s_from_start the s_from_start to set
     */
    public void setS_from_start(int s_from_start) {
        this.s_from_start = s_from_start;
    }

    /**
     * @return the flag
     */
    public int getFlag() {
        return flag;
    }

    /**
     * @param flag the flag to set
     */
    public void setFlag(int flag) {
        this.flag = flag;
    }
        
}
