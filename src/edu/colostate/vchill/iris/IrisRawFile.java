/**
 * 
 */
package edu.colostate.vchill.iris;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.chill.ChillDataHeader;
import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.chill.ChillHSKHeader;
import edu.colostate.vchill.chill.ChillGenRay;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.file.FileFunctions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * @author Joseph Hardin Description: Top level class to represent IRIS raw file
 *         format.
 */
public class IrisRawFile {

  private File InputFile;
  private File OutputFile;
  private static FileInputStream fstream_in;
  private static DataInputStream dstream_in;
  private static SigmetProductRaw SPR_input;

  static Calendar current_day;
  
  private static final ScaleManager sm = ScaleManager.getInstance();

  private static final Map<String, ChillFieldInfo> infos = new HashMap<String, ChillFieldInfo>();
  public static final ChillFieldInfo ZT = new ChillFieldInfo("dBZ-ZT", "Total Power", 8, 7500000, -1000000, 0, 0);
  public static final ChillFieldInfo Z = new ChillFieldInfo("dBZ", "Reflectivity", 9, 7500000, -1000000, 0, 0);
  public static final ChillFieldInfo V = new ChillFieldInfo("Vel", "Velocity", 10, 5500000, -5500000, 16, 1);
  public static final ChillFieldInfo W = new ChillFieldInfo("Wid", "SpectralWidth", 11, 800000, 0, 0, 2);
  public static final ChillFieldInfo ZDR = new ChillFieldInfo("ZDR", "DifferentialReflectivity", 12, 9030899, -3010299,
    0, 4);
  public static final ChillFieldInfo PHIDP = new ChillFieldInfo("PHIDP", "DifferentialPhase", 16, 90000000, -90000000,
    0, 6);
  public static final ChillFieldInfo RHOHV = new ChillFieldInfo("RhoHV", "CrossPolCorrelation", 20, 1000000, 0, 0, 7);
  public static final ChillFieldInfo KDP = new ChillFieldInfo("KDP", "SpecificPhase", 15, 3000000, -1000000, 0, 7);
  public static final ChillFieldInfo CZ = new ChillFieldInfo("CdBZ", "CorrectedReflectivity", 21, 7500000, -1000000, 0,
    0); // change
  // number?
  public static final ChillFieldInfo CZDR = new ChillFieldInfo("CZDR", "CorrectedDifferentialReflectivity", 36,
    9030899, -3010299, 0, 4); // change
  // number?
  public static final ChillFieldInfo AZ = new ChillFieldInfo("AdBZ", "AdjustedReflectivity", 42, 7500000, -1000000, 0,
    0); // change
  // number?
  public static final ChillFieldInfo AZDR = new ChillFieldInfo("AZDR", "AdjustedDifferentialReflectivity", 46, 9030899,
    -3010299, 0, 4); // change
  // number?
  public static final ChillFieldInfo ASDP = new ChillFieldInfo("ASDP", "AdptSpecificDifferentialPhase", 0, 25500000, 0,
    0, 1); // fix
  // scale,
  // colors,
  // etc
  public static final ChillFieldInfo VFilt = new ChillFieldInfo("VelFilt", "FilteredVelocity", 26, 5500000, -5500000,
    16, 1);
  public static final ChillFieldInfo VFast = new ChillFieldInfo("VelFast", "VelocityFast", 27, 5500000, -5500000, 16, 1);
  public static final ChillFieldInfo VSlow = new ChillFieldInfo("VelSlow", "VelocitySlow", 28, 5500000, -5500000, 16, 1);
  public static final ChillFieldInfo DB_HCLASS2 = new ChillFieldInfo("DB_HCLASS2", "Hydrometeor ID Class", 56, 0, 128, 0, 1);
  public static final ChillFieldInfo DB_ZDRC2 = new ChillFieldInfo("DB_ZDRC2", "Corrected ZDR", 58,9030899, -3010299,0 ,4 );
 // private static final ChillFieldInfo[] types = new ChillFieldInfo[] { Z, V, W, ZDR, PHIDP, RHOHV, KDP, CZ, CZDR, AZ,
 //   AZDR, ASDP, VFilt, VFast, VSlow, DB_HCLASS2, DB_ZDRC2 };


  
  
  
  private static  ChillFieldInfo[] types = new ChillFieldInfo[]{ZT,Z,V,W};
  
 
  public static void load(final ControlMessage command, final CacheMain cache) throws IOException {
    for (ChillFieldInfo info : types)
      infos.put(info.longFieldName, info);
    
    String path = FileFunctions.stripFileName(command.getDir()) + "/" + FileFunctions.stripFileName(command.getFile());
    System.out.println("IrisRawFile Object Initialized");
    try {

      fstream_in = new FileInputStream(path);

      dstream_in = new DataInputStream(fstream_in);

    } catch (Exception e) {
      System.err.println("Exception: " + e);
    }

    SPR_input = new SigmetProductRaw(dstream_in);
 
    String sweepstring=command.getSweep();
    int sweepnum=Integer.parseInt((sweepstring.split(" ")[1]));
    
    List<ChillMomentFieldScale> scales = new ArrayList<ChillMomentFieldScale>();
    int fieldNum = 0;
    // Setup Scale Manager stuff
    for (int currentVariable = 0; currentVariable < SPR_input.getTotal_vars(); currentVariable++) {

      if(currentVariable>3) continue;
      while (sm.getScale(fieldNum) != null) {
        ++fieldNum;
      }

    
      String description= null;
      String units=null;
      if(currentVariable==0) {
        description = new String("Total Power");
        units= new String("dBZ-ZT");
      }
      if(currentVariable==1){
        description = new String("Reflectivity");
        units = new String("dBZ");
      }else if(currentVariable ==2){
        description = new String("Velocity");
        units = new String("Vel");
      }else if(currentVariable==3){
        description = new String("SpectralWidth");
        units = new String("Wid");
      }
      
    // // System.out.println("Found data type: " + description + " in " +
    // // units);
    ChillFieldInfo info = infos.get(description);
    if (info == null) { // unknown
      System.out.println("Null Info Discovered");
      info = new ChillFieldInfo(description.substring(0, 4) + fieldNum, description, fieldNum++, 12800000, -12800000,
        0, 0);
    }
    ChillMomentFieldScale scale = new ChillMomentFieldScale(info, -1, units, 100000, 1, 0);
    scales.add(scale);
    cache.addRay(command, ChillDefines.META_TYPE, scale);
    sm.putScale(scale);
    // }
    // }
    //
    }
    int sz = 4;
    int var=1;
    
    ymds_time time_structure= SPR_input.getTop_product_hdr().getAproduct_configuration().getTime_ingest_sweep();
    current_day = Calendar.getInstance();
    //current_day.clear();
    current_day.set(time_structure.getYear(),time_structure.getMonth(), time_structure.getDay(),(int)(Math.round(time_structure.getSeconds()/3600.0)),(int)(Math.round(time_structure.getSeconds()/60.0)) );
    

    long availableData = 0;
    for (int typeI = 0; typeI < sz; ++typeI) {
      // Variable var = vars.get(typeI);
      // data[typeI] = Array(1);//var.read();
      availableData |= 1l << scales.get(typeI).fieldNumber;
    }
    //
    ChillHSKHeader hskH = new ChillHSKHeader();
    // hskH.radarLatitude = (int) (1e6 *
    // ncFile.findGlobalAttribute("Latitude").getNumericValue().doubleValue());
    // hskH.radarLongitude = (int) (1e6*
    // ncFile.findGlobalAttribute("Longitude").getNumericValue().doubleValue());
    // hskH.radarId = ncFile.findGlobalAttribute("RadarName").getStringValue();
    // hskH.angleScale = 0x7fffffff;
    // cache.addRay(command, ChillDefines.META_TYPE, hskH);

    hskH.radarLatitude = (int) (SPR_input.getTop_ingest_header().getTop_ingest_config().getLatitude()*1e6);
    hskH.radarLongitude = (int) (SPR_input.getTop_ingest_header().getTop_ingest_config().getLongitude()*1e6);
    hskH.radarId = SPR_input.getTop_ingest_header().getTop_ingest_config().getHardware_name_of_site();
    hskH.angleScale = 0x7fffffff;
    cache.addRay(command, ChillDefines.META_TYPE, hskH);

  //  int numgates = SPR_input.getRange_bins();
    int numgates = SPR_input.getSweeplist().get(sweepnum).getRays().get(0).getDatarays().get(1).getNumber_of_bins();
    int numrays = SPR_input.getSweeplist().get(sweepnum).getIdh_list().get(1).getRays_present();
    System.out.println("Number of rays:" + numrays);
    for (int radialI = 0; radialI < numrays; ++radialI) {// radial.getLength();
      // ++radialI) { //Here we add
      // rays individually
      Ray currRay = SPR_input.getSweeplist().get(sweepnum).getRays().get(radialI);
      ChillDataHeader dataH = new ChillDataHeader();
      // Index i1 = azimuth.getIndex().set(radialI);
      dataH.availableData = availableData;
      // dataH.availableData= -1;
      // // dataH.availableData = -1;
      // dataH.startAz = dataH.endAz = (int) (azimuth.getDouble(i1) / 360 *
      // hskH.angleScale);
      // dataH.startEl = dataH.endEl = (int) (elevation.getDouble(i1) / 360 *
      // hskH.angleScale);
      // dataH.numGates = gate.getLength();
      dataH.startAz = (int) (currRay.getDatarays().get(0).getRayheader().getBegin_azi()/360*hskH.angleScale);
       dataH.endAz = (int) (currRay.getDatarays().get(0).getRayheader().getEnd_azi()/360*hskH.angleScale);
      dataH.startEl = (int) (currRay.getDatarays().get(0).getRayheader().getBegin_elv()/360*hskH.angleScale);
      dataH.endEl = (int) (currRay.getDatarays().get(0).getRayheader().getEnd_elv()/360*hskH.angleScale);
      dataH.numGates = numgates;
      // dataH.startRange = startRange.getInt(i1);
      // dataH.dataTime = time.getInt(i1) & 0xffffffff;
      // dataH.fractionalSecs = timenSec == null ? 0 : timenSec.getInt(i1);
      dataH.startRange = 0;
      dataH.dataTime = current_day.getTimeInMillis()*1000;
      dataH.fractionalSecs = 0;

      // hskH.gateWidth = (int) gateWidth.getFloat(i1);

      hskH.gateWidth =10*SPR_input.getTop_ingest_header().getAtask_configuration().getAtask_range_info().getInput_bins_step();

      cache.addRay(command, ChillDefines.META_TYPE, dataH);
      for (int typeI = 0; typeI < sz; ++typeI) {
        // if (data[typeI] == null){
        // System.out.println("Choosing Continue");
        // continue;
        // }
        // Index i2 = data[typeI].getIndex().set0(radialI);
        // double[] typeData = new double[dataH.numGates];
        double[] typeData = currRay.getDatarays().get(typeI+1).getData();
        // for (int gateI = 0; gateI < dataH.numGates; ++gateI) {
        // // typeData[gateI] = data[typeI].getDouble(i2.set1(gateI));
        // typeData[gateI] = radialI/18 + gateI/20 ;
        // // typeData[gateI] =
        // currRay.getDatarays().get(radialI).getData()[gateI];
        // }
        cache.addRay(command, scales.get(typeI).fieldName, new ChillGenRay(hskH, dataH, scales.get(typeI).fieldName,
          typeData));
      }
      
    }
    //
    for (String type : sm.getTypes()) {
      cache.setCompleteFlag(command, type);
      System.out.println("marked " + type + " complete; cached " + cache.getNumberOfRays(command, type) + " rays");
    }
    cache.setCompleteFlag(command, ChillDefines.META_TYPE);
    // ncFile.close();
  }
  

  
}
