/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 * Class for dsp_data_mask structure
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class dsp_data_mask {

    private long mask_word_0;
    private long mask_word_1;
    private long mask_word_2;
    private long mask_word_3;
    private long mask_word_4;
    private long extended_header_type;

    dsp_data_mask(ByteBuffer in_buf) {

        mask_word_0 = UtilityClass.UINT4_to_long(in_buf.getInt());
        extended_header_type = UtilityClass.UINT4_to_long(in_buf.getInt());
        mask_word_1 = UtilityClass.UINT4_to_long(in_buf.getInt());
        mask_word_2 = UtilityClass.UINT4_to_long(in_buf.getInt());
        mask_word_3 = UtilityClass.UINT4_to_long(in_buf.getInt());
        mask_word_4 = UtilityClass.UINT4_to_long(in_buf.getInt());
    }

    /**
     * @return the mask_word_0
     */
    public long getMask_word_0() {
        return mask_word_0;
    }

    /**
     * @param mask_word_0 the mask_word_0 to set
     */
    public void setMask_word_0(long mask_word_0) {
        this.mask_word_0 = mask_word_0;
    }

    /**
     * @return the mask_word_1
     */
    public long getMask_word_1() {
        return mask_word_1;
    }

    /**
     * @param mask_word_1 the mask_word_1 to set
     */
    public void setMask_word_1(long mask_word_1) {
        this.mask_word_1 = mask_word_1;
    }

    /**
     * @return the mask_word_2
     */
    public long getMask_word_2() {
        return mask_word_2;
    }

    /**
     * @param mask_word_2 the mask_word_2 to set
     */
    public void setMask_word_2(long mask_word_2) {
        this.mask_word_2 = mask_word_2;
    }

    /**
     * @return the mask_word_3
     */
    public long getMask_word_3() {
        return mask_word_3;
    }

    /**
     * @param mask_word_3 the mask_word_3 to set
     */
    public void setMask_word_3(long mask_word_3) {
        this.mask_word_3 = mask_word_3;
    }

    /**
     * @return the mask_word_4
     */
    public long getMask_word_4() {
        return mask_word_4;
    }

    /**
     * @param mask_word_4 the mask_word_4 to set
     */
    public void setMask_word_4(long mask_word_4) {
        this.mask_word_4 = mask_word_4;
    }

    /**
     * @return the extended_header_type
     */
    public long getExtended_header_type() {
        return extended_header_type;
    }

    /**
     * @param extended_header_type the extended_header_type to set
     */
    public void setExtended_header_type(long extended_header_type) {
        this.extended_header_type = extended_header_type;
    }

}
