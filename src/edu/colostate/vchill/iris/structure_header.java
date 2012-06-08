/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.io.DataInputStream;
import java.nio.ByteBuffer;
/**
 * Class to represent the structure header struct
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class structure_header {
    
    private short structure_identifier;
    private short format_version;
    private int bytes_in_struct;
    short resint2;
    private short flags;
    
    public structure_header(ByteBuffer in_buf){
     try{
        structure_identifier=in_buf.getShort();
        format_version=in_buf.getShort();
        bytes_in_struct=in_buf.getInt();
        resint2=in_buf.getShort();
        flags=in_buf.getShort();
     }catch(Exception e){
         System.err.println("Exception e:"+e);
     }
    
    }

    /**
     * @return the structure_identifier
     */
    public short getStructure_identifier() {
        return structure_identifier;
    }

    /**
     * @param structure_identifier the structure_identifier to set
     */
    public void setStructure_identifier(short structure_identifier) {
        this.structure_identifier = structure_identifier;
    }

    /**
     * @return the format_version
     */
    public short getFormat_version() {
        return format_version;
    }

    /**
     * @param format_version the format_version to set
     */
    public void setFormat_version(short format_version) {
        this.format_version = format_version;
    }

    /**
     * @return the bytes_in_struct
     */
    public int getBytes_in_struct() {
        return bytes_in_struct;
    }

    /**
     * @param bytes_in_struct the bytes_in_struct to set
     */
    public void setBytes_in_struct(int bytes_in_struct) {
        this.bytes_in_struct = bytes_in_struct;
    }

    /**
     * @return the flags
     */
    public short getFlags() {
        return flags;
    }

    /**
     * @param flags the flags to set
     */
    public void setFlags(short flags) {
        this.flags = flags;
    }
    
}
