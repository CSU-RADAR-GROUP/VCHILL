/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;
/**
 *
 * @author Joseph Hardin<josephhardinee@gmail.com>
 */
public class product_hdr {
    
    private static int product_hdr_size=6144;

    private int BeginPosition;
    private int currPosition;
        
    /** 
     * Constructor to parse a buffer and populate the structure
     * 
     **/
    public product_hdr(ByteBuffer in_buf)
    {
        BeginPosition=in_buf.position();
        
        //Read Structure Header
        astructure_header = new structure_header(in_buf);
        
        aproduct_configuration = new product_configuration(in_buf);

        aproduct_end = new product_end(in_buf);        

    
    
    }
    
    
    
    /**
     * @return the product_hdr_size
     */
    public static int getProduct_hdr_size() {
        return product_hdr_size;
    }

    /**
     * @param aProduct_hdr_size the product_hdr_size to set
     */
    public static void setProduct_hdr_size(int aProduct_hdr_size) {
        product_hdr_size = aProduct_hdr_size;
    }
    
    private structure_header astructure_header;
    private product_configuration aproduct_configuration;
    private product_end aproduct_end;
    


    /**
     * @return the astructure_header
     */
    public structure_header getAstructure_header() {
        return astructure_header;
    }

    /**
     * @param astructure_header the astructure_header to set
     */
    public void setAstructure_header(structure_header astructure_header) {
        this.astructure_header = astructure_header;
    }

    /**
     * @return the aproduct_configuration
     */
    public product_configuration getAproduct_configuration() {
        return aproduct_configuration;
    }

    /**
     * @param aproduct_configuration the aproduct_configuration to set
     */
    public void setAproduct_configuration(product_configuration aproduct_configuration) {
        this.aproduct_configuration = aproduct_configuration;
    }

    /**
     * @return the aproduct_end
     */
    public product_end getAproduct_end() {
        return aproduct_end;
    }

    /**
     * @param aproduct_end the aproduct_end to set
     */
    public void setAproduct_end(product_end aproduct_end) {
        this.aproduct_end = aproduct_end;
    }
    
    
}
