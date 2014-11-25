/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;


/**
 * Class to handle reading actual ray data, including undoing the ray compression
 * that sigmet uses.
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class RayReader {

    private ArrayList<DataRay> raylist;
    private ByteBuffer in_buf;
    private int currProduct = 0;


    private DataInputStream dis;
    private ArrayList<ingest_data_header> idh_list;

    public RayReader(DataInputStream disa) {
        dis = disa;


    }

//    public Ray readRay(){
//        int codeword=0;
//        
//        while(codeword !=1){
//            codeword = in_buf.getShort();
//            
//            
//        }


}
