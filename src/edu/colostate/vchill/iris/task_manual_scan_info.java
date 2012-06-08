/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 *Class to hold the semi useless structure task_manual_scan_info
 * @author Joseph Hardin <josephhardinee@gmail.com>
 * Completed
 */


public class task_manual_scan_info {
  
    private int flags;
    
    task_manual_scan_info(ByteBuffer in_buf){
        flags=UtilityClass.UINT2_to_SINT(in_buf.getShort());
        in_buf.position(in_buf.position()+198);
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
