/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ucar.ma2.DataType;
import ucar.nc2.*;

/**
 * Driver class for Sigmet Product Raw file conversion to Casa NetCDF
 * @author Joseph Hardin <josephhardinee@gmail.com>
 * @version 8-12-11
 */
public class Iris2NetCDF {

    private File InputFile;
    private File OutputFile;
    private static FileInputStream fstream_in;
    private static DataInputStream dstream_in;
    private static FileOutputStream fstream_out;
    private static DataOutputStream dstream_out;
    private static SigmetProductRaw SPR_input;
    private static String ncfilename = "/Users/jhardin/nctest.nc";

    /**
     * Main function for program
     * @param Input Filename
     * @param Output Filename
     */
    public static void main(String[] args) {



        if (args.length == 2) {
            try {
                fstream_in = new FileInputStream(args[0]);
                dstream_in = new DataInputStream(fstream_in);
            } catch (Exception e) {
                System.err.println("Exception: " + e);
            }

            try {
                fstream_out = new FileOutputStream(args[1]);
                dstream_out = new DataOutputStream(fstream_out);
            } catch (Exception e) {
                System.err.println("Exception: " + e);

            }

        } else {//Revert to default test case for now.
            System.out.println("Usage: Iris2NetCDF Inputfile outputfile\n");
            //System.exit(1);
            try {
//            fstream_in = new FileInputStream("/Users/jhardin/Desktop/PassPortBackup/KUM101227153024.RAWKM9T");
                fstream_in = new FileInputStream("/Volumes/JOE PASSPRT/MC3E/toga3/lda/product_raw/np1110506045924.RAWV9UC");
                dstream_in = new DataInputStream(fstream_in);

            } catch (Exception e) {
                System.err.println("Exception: " + e);
            }
            try {
//                fstream_out = new FileOutputStream("Testout.nc");
//                dstream_out = new DataOutputStream(fstream_out);
            } catch (Exception e) {
                System.err.println("Exception: " + e);

            }

        }


        SPR_input = new SigmetProductRaw(dstream_in);

        //Now at this point we should have the Sigmet Product Raw read in
        //Time to start creating the netCDF file
        System.out.println("Finished Importing Data");

        NetcdfFileWriteable ncfile = null;


        try {
            ncfile = NetcdfFileWriteable.createNew(ncfilename, false);
        } catch (Exception e) {
            System.err.println("Problem opening NetCDF file");
            e.printStackTrace();
        }

        int sweeps = SPR_input.getSweeplist().size();
        int rays = SPR_input.getSweeplist().get(0).getIdh_list().get(1).getRays_present();
        //We're not going to save extended headers just yet so don't count them.
        int products = SPR_input.getSweeplist().get(0).getIdh_list().size();
        if (SPR_input.getSweeplist().get(0).getIdh_list().get(0).getData_type() == 1) {
            products--;
        }
        int range = SPR_input.getRange_bins();
        //add dimensions
        Dimension d_sweep = ncfile.addDimension("sweep", sweeps);
        Dimension d_radial = ncfile.addDimension("radial", rays);
        Dimension d_range = ncfile.addDimension("range", range);

        ArrayList<Dimension> dims = new ArrayList<Dimension>();

        dims.add(d_sweep);
        dims.add(d_radial);
        dims.add(d_range);
        //Add Variables

        ncfile.addVariable("Elevation", DataType.DOUBLE, dims.subList(0, 1));
        ncfile.addVariableAttribute("Elevation", "units", "degrees");

        ncfile.addVariable("Azimuth", DataType.DOUBLE, dims.subList(0, 1));
        ncfile.addVariableAttribute("Azimuth", "units", "degrees");

        //TODO Rest of variables added

        ncfile.addVariable("Reflectivity", DataType.DOUBLE, dims);



        //Now create the actual file:
        try {
            ncfile.create();
        } catch (IOException e) {
            System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
        }

        try {
            ncfile.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //File is created, now lets write out the data

    }
}
