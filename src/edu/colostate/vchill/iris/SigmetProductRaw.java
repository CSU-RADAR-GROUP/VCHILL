/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import edu.colostate.vchill.file.FileFunctions;

/**
 *
 * @author Joseph Hardin
 */
public class SigmetProductRaw {

    private product_hdr top_product_hdr;
    private ingest_header top_ingest_header;
    private ArrayList<ingest_data_header> idh_array;
    private ArrayList<Sweep> sweeplist;
    private byte[] RecordBuffer = new byte[6144];
    private ByteBuffer TempByteBuffer;
    private raw_prod_bhdr temp_raw_prod_bhdr;
    private ArrayList<ingest_data_header> temp_ingest_data_header;
    private byte[] TempBuffer;
    private int range_bins;
    private int rays;
    private int sweeps;
    boolean start_new_ray = true;
    private int currDataType = -1;
    private Sweep currSweep;
    int bytesleft;
    ByteArrayOutputStream recordstream;
    int total_vars;
    
    public int getTotal_vars() {
      return total_vars;
    }

/**
 * Read in just the headers from a file.
 * @param fin File to read headers from 
 */
     
  public SigmetProductRaw(String f_name){ // This mode just reads in Headers
  
    FileInputStream fstream_in;
    DataInputStream dis;
    
    
    //String path = FileFunctions.stripFileName(command.getDir()) + "/" + FileFunctions.stripFileName(command.getFile());
    String path = f_name;
    System.out.println("IrisRawFile Object Initialized");
    try {

      fstream_in = new FileInputStream(path);

      dis = new DataInputStream(fstream_in);

    } catch (Exception e) {
      System.err.println("Exception: " + e);
      return;
    }    
    
    try {
      dis.read(RecordBuffer);
      TempByteBuffer = ByteBuffer.wrap(RecordBuffer);
      TempByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    } catch (Exception e) {
      System.err.println("Exception:" + e);
    }
    top_product_hdr = new product_hdr(getTempByteBuffer());

    try {
      dis.read(RecordBuffer);
      TempByteBuffer = ByteBuffer.wrap(RecordBuffer);
      TempByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    } catch (Exception e) {
      System.err.println("Exception:" + e);
    }

    top_ingest_header = new ingest_header(getTempByteBuffer());
    //
    // Read top ingest_header
    //
    range_bins = top_ingest_header.getAtask_configuration().getAtask_range_info().getOutput_bins();
    sweeps = top_ingest_header.getAtask_configuration().getAtask_scan_info().getNum_sweeps();

    total_vars = Integer.bitCount((int) (top_ingest_header.getAtask_configuration().getAtask_dsp_info()
      .getCurrent_data_type().getMask_word_0()));

    total_vars += Integer.bitCount((int) (top_ingest_header.getAtask_configuration().getAtask_dsp_info()
      .getCurrent_data_type().getMask_word_1()));
    total_vars += Integer.bitCount((int) (top_ingest_header.getAtask_configuration().getAtask_dsp_info()
      .getCurrent_data_type().getMask_word_2()));
    total_vars += Integer.bitCount((int) (top_ingest_header.getAtask_configuration().getAtask_dsp_info()
      .getCurrent_data_type().getMask_word_3()));
    total_vars += Integer.bitCount((int) (top_ingest_header.getAtask_configuration().getAtask_dsp_info()
      .getCurrent_data_type().getMask_word_4()));

    System.out.println("Data Mask Word 1:"
      + (int) (top_ingest_header.getAtask_configuration().getAtask_dsp_info().getCurrent_data_type().getMask_word_2()));

    System.out.println("Input Bins to be processed:" + range_bins);
    System.out.println("Sweeps to be processed:" + sweeps);
    System.out.println("Total Variables:" + total_vars);
    
  }
    
  /**
   * Read in a Sigmet Product raw file and populate the variables and structures
   * 
   * @param DataInputStream
   *          dis the Product Raw Stream to be read
   */
  
  
  public SigmetProductRaw(DataInputStream dis) {

    //
    // Read and Create product_hdr
    //
    try {
      dis.read(RecordBuffer);
      TempByteBuffer = ByteBuffer.wrap(RecordBuffer);
      TempByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    } catch (Exception e) {
      System.err.println("Exception:" + e);
    }
    top_product_hdr = new product_hdr(getTempByteBuffer());

    try {
      dis.read(RecordBuffer);
      TempByteBuffer = ByteBuffer.wrap(RecordBuffer);
      TempByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    } catch (Exception e) {
      System.err.println("Exception:" + e);
    }

    top_ingest_header = new ingest_header(getTempByteBuffer());
    //
    // Read top ingest_header
    //
    range_bins = top_ingest_header.getAtask_configuration().getAtask_range_info().getOutput_bins();
    sweeps = top_ingest_header.getAtask_configuration().getAtask_scan_info().getNum_sweeps();

    total_vars = Integer.bitCount((int) (top_ingest_header.getAtask_configuration().getAtask_dsp_info()
      .getCurrent_data_type().getMask_word_0()));

    total_vars += Integer.bitCount((int) (top_ingest_header.getAtask_configuration().getAtask_dsp_info()
      .getCurrent_data_type().getMask_word_1()));
    total_vars += Integer.bitCount((int) (top_ingest_header.getAtask_configuration().getAtask_dsp_info()
      .getCurrent_data_type().getMask_word_2()));
    total_vars += Integer.bitCount((int) (top_ingest_header.getAtask_configuration().getAtask_dsp_info()
      .getCurrent_data_type().getMask_word_3()));
    total_vars += Integer.bitCount((int) (top_ingest_header.getAtask_configuration().getAtask_dsp_info()
      .getCurrent_data_type().getMask_word_4()));

    System.out.println("Data Mask Word 1:"
      + (int) (top_ingest_header.getAtask_configuration().getAtask_dsp_info().getCurrent_data_type().getMask_word_2()));

    System.out.println("Input Bins to be processed:" + range_bins);
    System.out.println("Sweeps to be processed:" + sweeps);
    System.out.println("Total Variables:" + total_vars);

    temp_ingest_data_header = new ArrayList<ingest_data_header>();

    Ray currRay = new Ray();

    // At this point we have the two top structures, so now we read in the
    // individual data type records.
    sweeplist = new ArrayList<Sweep>();
    DataDecoderBuffer datadecoder;

    int sraycount = 0;
    boolean new_sweep = true;
    try {
      for (int currSweep = 0; currSweep < sweeps; currSweep++) {
        System.out.println("Processing sweep " + currSweep + ".");
        sweeplist.add(new Sweep());

        try {
          dis.read(RecordBuffer);
          TempByteBuffer = ByteBuffer.wrap(RecordBuffer);
          TempByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
          TempByteBuffer.get(new byte[12]);
        } catch (Exception e) {
          e.printStackTrace();
        }

        for (int cv = 0; cv < total_vars; cv++) {
          sweeplist.get(currSweep).getIdh_list().add(new ingest_data_header(getTempByteBuffer()));
          // System.out.println("Total Rays of var:" + cv + " is :" +
          // sweeplist.get(currSweep).getIdh_list().get(cv).getRays_present());

        }// Data Headers for current sweep finished.

        datadecoder = new DataDecoderBuffer(dis, getTempByteBuffer());
        System.out.println("Number of rays is :"+ sweeplist.get(currSweep).getIdh_list().get(1).getRays_present());
        for (int raycount = 0; raycount < sweeplist.get(currSweep).getIdh_list().get(1).getRays_present(); raycount++) {
          currRay = new Ray();
          // System.out.println("Working on Ray:" + raycount);

          for (int cv = 0; cv < total_vars; cv++) {// By Variable
            DataRay currDataRay = new DataRay();
            currDataRay.setRayheader(new RayHeader(datadecoder.getData(12)));
            int numbins = currDataRay.getRayheader().getBins_in_ray();
            // System.out.println("Number of bins:" + numbins);
            currDataRay.setRangeBins(numbins);
            // System.out.println("Reading(" + sraycount++ + ")Variable " + cv +
            // "of type " +
            // sweeplist.get(currSweep).getIdh_list().get(cv).getData_type());
            // System.out.println("CurrDataRay elv:" +
            // currDataRay.getRayheader().getBegin_elv());
            if (sweeplist.get(currSweep).getIdh_list().get(cv).getBits_per_bin() == 160) {
              byte[] byteli = new byte[sweeplist.get(currSweep).getIdh_list().get(cv).getBits_per_bin() / 8 * numbins];
              datadecoder.getData(sweeplist.get(currSweep).getIdh_list().get(cv).getBits_per_bin() / 8 * numbins).get(
                byteli); // Yeah I know thats ugly.
              currDataRay.setByteArray(byteli);
            } else if (sweeplist.get(currSweep).getIdh_list().get(cv).getBits_per_bin() == 16) {
              short[] tempsBuffer = new short[numbins];
              if (numbins > 4000) {
                System.err.println("BAD RAYYYYYY");
              }
              datadecoder.getData(numbins * 2).asShortBuffer().get(tempsBuffer);
              currDataRay.setBulkData(tempsBuffer);
              // for (int rb = 0; rb < numbins; rb++) {
              // currDataRay.setData(datadecoder.getData(2).getShort(), rb);
              // }
            }
            currDataRay.setDtype(sweeplist.get(currSweep).getIdh_list().get(cv).getData_type());
            currDataRay.translateData();
            currRay.getDatarays().add(currDataRay);
            
            datadecoder.checkEndRay();

          }
          sweeplist.get(currSweep).getRays().add(currRay);
          //System.out.println("Added Ray number"+raycount);
        }
        

      }// End of Current Record
    } catch (Exception e) {
      e.printStackTrace();

    }

  }

    /**
     * @return the top_product_hdr
     */
    public product_hdr getTop_product_hdr() {
        return top_product_hdr;
    }

    /**
     * @param top_product_hdr the top_product_hdr to set
     */
    public void setTop_product_hdr(product_hdr top_product_hdr) {
        this.top_product_hdr = top_product_hdr;
    }

    /**
     * @return the top_ingest_header
     */
    public ingest_header getTop_ingest_header() {
        return top_ingest_header;
    }

    /**
     * @param top_ingest_header the top_ingest_header to set
     */
    public void setTop_ingest_header(ingest_header top_ingest_header) {
        this.top_ingest_header = top_ingest_header;
    }

    /**
     * @return the idh_array
     */
    public ArrayList<ingest_data_header> getIdh_array() {
        return idh_array;
    }

    /**
     * @param idh_array the idh_array to set
     */
    public void setIdh_array(ArrayList<ingest_data_header> idh_array) {
        this.idh_array = idh_array;
    }

    /**
     * @return the sweeplist
     */
    public ArrayList<Sweep> getSweeplist() {
        return sweeplist;
    }

    /**
     * @param sweeplist the sweeplist to set
     */
    public void setSweeplist(ArrayList<Sweep> sweeplist) {
        this.sweeplist = sweeplist;
    }

    /**
     * @return the RecordBuffer
     */
    public byte[] getRecordBuffer() {
        return RecordBuffer;
    }

    /**
     * @param RecordBuffer the RecordBuffer to set
     */
    public void setRecordBuffer(byte[] RecordBuffer) {
        this.RecordBuffer = RecordBuffer;
    }

    /**
     * @return the TempByteBuffer
     */
    public ByteBuffer getTempByteBuffer() {
        return TempByteBuffer;
    }

    /**
     * @param TempByteBuffer the TempByteBuffer to set
     */
    public void setTempByteBuffer(ByteBuffer TempByteBuffer) {
        this.TempByteBuffer = TempByteBuffer;
    }

    /**
     * @return the temp_raw_prod_bhdr
     */
    public raw_prod_bhdr getTemp_raw_prod_bhdr() {
        return temp_raw_prod_bhdr;
    }

    /**
     * @param temp_raw_prod_bhdr the temp_raw_prod_bhdr to set
     */
    public void setTemp_raw_prod_bhdr(raw_prod_bhdr temp_raw_prod_bhdr) {
        this.temp_raw_prod_bhdr = temp_raw_prod_bhdr;
    }

    /**
     * @return the range_bins
     */
    public int getRange_bins() {
        return range_bins;
    }

    /**
     * @param range_bins the range_bins to set
     */
    public void setRange_bins(int range_bins) {
        this.range_bins = range_bins;
    }

    /**
     * @return the rays
     */
    public int getRays() {
        return rays;
    }

    /**
     * @param rays the rays to set
     */
    public void setRays(int rays) {
        this.rays = rays;
    }

    /**
     * @return the sweeps
     */
    public int getSweeps() {
        return sweeps;
    }

    /**
     * @param sweeps the sweeps to set
     */
    public void setSweeps(int sweeps) {
        this.sweeps = sweeps;
    }
}
