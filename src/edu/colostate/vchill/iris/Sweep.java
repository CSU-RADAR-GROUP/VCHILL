/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.util.ArrayList;


/**
 * Class to represent and hold data for each individual sweep.
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class Sweep {

    private String[] Varlist;
    private int number_of_rays;
    private ArrayList<Ray> rays; //Product x Range Bin
    private ArrayList<ingest_data_header> idh_list;


    public Sweep(String[] Varlist) {

        rays = new ArrayList<Ray>();
        idh_list = new ArrayList<ingest_data_header>();
    }

    public Sweep() {

        rays = new ArrayList<Ray>();
        idh_list = new ArrayList<ingest_data_header>();

    }

    /**
     * @return the Varlist
     */
    public String[] getVarlist() {
        return Varlist;
    }

    /**
     * @param Varlist the Varlist to set
     */
    public void setVarlist(String[] Varlist) {
        this.Varlist = Varlist;
    }

    /**
     * @return the number_of_rays
     */
    public int getNumber_of_rays() {
        return number_of_rays;
    }

    /**
     * @param number_of_rays the number_of_rays to set
     */
    public void setNumber_of_rays(int number_of_rays) {
        this.number_of_rays = number_of_rays;
    }

    /**
     * @return the rays
     */
    public ArrayList<Ray> getRays() {
        return rays;
    }

    /**
     * @param rays the rays to set
     */
    public void setRays(ArrayList<Ray> rays) {
        this.rays = rays;
    }

    /**
     * @return the idh_list
     */
    public ArrayList<ingest_data_header> getIdh_list() {
        return idh_list;
    }

    /**
     * @param idh_list the idh_list to set
     */
    public void setIdh_list(ArrayList<ingest_data_header> idh_list) {
        this.idh_list = idh_list;
    }
}
