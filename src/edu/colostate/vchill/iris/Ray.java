/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.util.ArrayList;

/**
 * Class to represent a single ray..Not sure if we will use this or not yet.
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class Ray {
    
    private ArrayList<DataRay> datarays;
//    private int range_bins;
//    private String data_type;
//    private RayHeader rayheader;
    
    public Ray()
    {
        datarays = new ArrayList<DataRay>();
        
    }

    /**
     * @return the datarays
     */
    public ArrayList<DataRay> getDatarays() {
        return datarays;
    }

    /**
     * @param datarays the datarays to set
     */
    public void setDatarays(ArrayList<DataRay> datarays) {
        this.datarays = datarays;
    }
    
    

    
    
}
