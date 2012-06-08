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
public class task_dsp_mode_other {
    
    task_dsp_mode_other(ByteBuffer in_buf){
     in_buf.position(in_buf.position()+32);   
    }
    
}
