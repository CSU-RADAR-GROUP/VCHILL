/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 * Class to represent the task_file_scan_info structure.
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class task_file_scan_info {

    private int first_azimuth;
    private int first_elevation;
    private String antenna_control_file;
    
    private byte[] TempBuf;
    
    task_file_scan_info(ByteBuffer in_buf){
        first_azimuth=UtilityClass.UINT2_to_SINT(in_buf.getShort());
        first_elevation=UtilityClass.UINT2_to_SINT(in_buf.getShort());
        try{
        TempBuf = new byte[12];
        in_buf.get(TempBuf);
        antenna_control_file = new String(TempBuf, "UTF-8");
        }catch(Exception e){
            System.err.println("Exception:"+e);
        }
        
        in_buf.position(in_buf.position()+184);
    }

    /**
     * @return the first_azimuth
     */
    public int getFirst_azimuth() {
        return first_azimuth;
    }

    /**
     * @param first_azimuth the first_azimuth to set
     */
    public void setFirst_azimuth(int first_azimuth) {
        this.first_azimuth = first_azimuth;
    }

    /**
     * @return the first_elevation
     */
    public int getFirst_elevation() {
        return first_elevation;
    }

    /**
     * @param first_elevation the first_elevation to set
     */
    public void setFirst_elevation(int first_elevation) {
        this.first_elevation = first_elevation;
    }

    /**
     * @return the antenna_control_file
     */
    public String getAntenna_control_file() {
        return antenna_control_file;
    }

    /**
     * @param antenna_control_file the antenna_control_file to set
     */
    public void setAntenna_control_file(String antenna_control_file) {
        this.antenna_control_file = antenna_control_file;
    }
            
    
    
}
