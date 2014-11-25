/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;


/**
 * Class that represents the ingest_header C structure for Sigmet files.
 *
 * @param Buffer Holding Record
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class ingest_header {

    private structure_header top_st;
    private ingest_configuration top_ingest_config;
    private task_configuration atask_configuration;
    private byte[] GPARM = new byte[128];

    private int currPosition;

    public ingest_header(ByteBuffer in_buf) {

        top_st = new structure_header(in_buf);

        top_ingest_config = new ingest_configuration(in_buf);

        atask_configuration = new task_configuration(in_buf);

        in_buf.position(in_buf.position() + 732); //Spare

        in_buf.get(GPARM);

        in_buf.position(in_buf.position() + 920);


    }

    /**
     * @return the top_st
     */
    public structure_header getTop_st() {
        return top_st;
    }

    /**
     * @param top_st the top_st to set
     */
    public void setTop_st(structure_header top_st) {
        this.top_st = top_st;
    }

    /**
     * @return the top_ingest_config
     */
    public ingest_configuration getTop_ingest_config() {
        return top_ingest_config;
    }

    /**
     * @param top_ingest_config the top_ingest_config to set
     */
    public void setTop_ingest_config(ingest_configuration top_ingest_config) {
        this.top_ingest_config = top_ingest_config;
    }

    /**
     * @return the atask_configuration
     */
    public task_configuration getAtask_configuration() {
        return atask_configuration;
    }

    /**
     * @param atask_configuration the atask_configuration to set
     */
    public void setAtask_configuration(task_configuration atask_configuration) {
        this.atask_configuration = atask_configuration;
    }

    /**
     * @return the GPARM
     */
    public byte[] getGPARM() {
        return GPARM;
    }

    /**
     * @param GPARM the GPARM to set
     */
    public void setGPARM(byte[] GPARM) {
        this.GPARM = GPARM;
    }

}
