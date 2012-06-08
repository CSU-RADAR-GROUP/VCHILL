/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 * Class to represent structure product_specific_info
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class product_specific_info {
    
    public product_specific_info(ByteBuffer in_buf){
        //TODO Write this structure
        in_buf.position(in_buf.position()+80);
    }
}
