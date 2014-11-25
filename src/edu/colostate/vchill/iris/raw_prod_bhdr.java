/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;


/**
 * Class to represent raw_prod_bhdr structure
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class raw_prod_bhdr {

    private int BeginPosition;
    private int currPosition;
    private byte TempByte;
    private byte[] TempBuf;

    private short record_number;
    private short sweep_number;
    private short byte_offset;
    private short ray_number;
    private int flags;

    public raw_prod_bhdr(ByteBuffer in_buf) {
        BeginPosition = in_buf.position();

        record_number = in_buf.getShort();
        sweep_number = in_buf.getShort();
        byte_offset = in_buf.getShort();
        ray_number = in_buf.getShort();
        flags = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        in_buf.position(in_buf.position() + 2);
    
   /* 
        System.out.println("Record:"+record_number);
        System.out.println("Sweep:"+sweep_number);
        System.out.println("Ray:"+ray_number);
        System.out.println("ByteOffset"+byte_offset);
 */
    }

    /**
     * @return the record_number
     */
    public short getRecord_number() {
        return record_number;
    }

    /**
     * @param record_number the record_number to set
     */
    public void setRecord_number(short record_number) {
        this.record_number = record_number;
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
     * @return the byte_offset
     */
    public short getByte_offset() {
        return byte_offset;
    }

    /**
     * @param byte_offset the byte_offset to set
     */
    public void setByte_offset(short byte_offset) {
        this.byte_offset = byte_offset;
    }

    /**
     * @return the ray_number
     */
    public short getRay_number() {
        return ray_number;
    }

    /**
     * @param ray_number the ray_number to set
     */
    public void setRay_number(short ray_number) {
        this.ray_number = ray_number;
    }

    /**
     * @return the flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * @param flags the flags to set
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

}
