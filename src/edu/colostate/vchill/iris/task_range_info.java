/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class task_range_info {
    private int range_first_bin;
    private int range_last_bin;
    private short input_bins;
    private short output_bins;
    private int input_bins_step;
    private int output_bins_step;
    private int variable_range_spacing;
    private short range_bin_averaging;
    
    
    
    public task_range_info(ByteBuffer in_buf){
        range_first_bin=in_buf.getInt();
        range_last_bin=in_buf.getInt();
        input_bins=in_buf.getShort();
        output_bins=in_buf.getShort();
        input_bins_step=in_buf.getInt();
        output_bins_step=in_buf.getInt();
        variable_range_spacing=UtilityClass.UINT2_to_SINT(in_buf.getShort());
        range_bin_averaging=in_buf.getShort();
        System.out.println("Range Bins = "+output_bins);
    }

    /**
     * @return the range_first_bin
     */
    public int getRange_first_bin() {
        return range_first_bin;
    }

    /**
     * @param range_first_bin the range_first_bin to set
     */
    public void setRange_first_bin(int range_first_bin) {
        this.range_first_bin = range_first_bin;
    }

    /**
     * @return the range_last_bin
     */
    public int getRange_last_bin() {
        return range_last_bin;
    }

    /**
     * @param range_last_bin the range_last_bin to set
     */
    public void setRange_last_bin(int range_last_bin) {
        this.range_last_bin = range_last_bin;
    }

    /**
     * @return the input_bins
     */
    public short getInput_bins() {
        return input_bins;
    }

    /**
     * @param input_bins the input_bins to set
     */
    public void setInput_bins(short input_bins) {
        this.input_bins = input_bins;
    }

    /**
     * @return the output_bins
     */
    public short getOutput_bins() {
        return output_bins;
    }

    /**
     * @param output_bins the output_bins to set
     */
    public void setOutput_bins(short output_bins) {
        this.output_bins = output_bins;
    }

    /**
     * @return the input_bins_step
     */
    public int getInput_bins_step() {
        return input_bins_step;
    }

    /**
     * @param input_bins_step the input_bins_step to set
     */
    public void setInput_bins_step(int input_bins_step) {
        this.input_bins_step = input_bins_step;
    }

    /**
     * @return the output_bins_step
     */
    public int getOutput_bins_step() {
        return output_bins_step;
    }

    /**
     * @param output_bins_step the output_bins_step to set
     */
    public void setOutput_bins_step(int output_bins_step) {
        this.output_bins_step = output_bins_step;
    }

    /**
     * @return the variable_range_spacing
     */
    public int getVariable_range_spacing() {
        return variable_range_spacing;
    }

    /**
     * @param variable_range_spacing the variable_range_spacing to set
     */
    public void setVariable_range_spacing(int variable_range_spacing) {
        this.variable_range_spacing = variable_range_spacing;
    }

    /**
     * @return the range_bin_averaging
     */
    public short getRange_bin_averaging() {
        return range_bin_averaging;
    }

    /**
     * @param range_bin_averaging the range_bin_averaging to set
     */
    public void setRange_bin_averaging(short range_bin_averaging) {
        this.range_bin_averaging = range_bin_averaging;
    }
}
