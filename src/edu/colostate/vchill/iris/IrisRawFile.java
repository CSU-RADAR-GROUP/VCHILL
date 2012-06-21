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
  public static final ChillFieldInfo ZT = new ChillFieldInfo("DBT2", "DBT2", 8, 7500000, -1000000, 0, 0);
  public static final ChillFieldInfo Z = new ChillFieldInfo("DBZ2", "DBZ2", 9, 7500000, -1000000, 0, 0);
  public static final ChillFieldInfo V = new ChillFieldInfo("VEL2", "VEL2", 10, 5500000, -5500000, 16, 1);
  public static final ChillFieldInfo W = new ChillFieldInfo("WIDTH2", "WIDTH2", 11, 800000, 0, 0, 2);
  public static final ChillFieldInfo ZDR2 = new ChillFieldInfo("ZDR2", "ZDR2", 12, 9030899, -3010299,0, 4);
  public static final ChillFieldInfo PHIDP2 = new ChillFieldInfo("PHIDP2", "PHIDP2", 24, 36000000, -36000000,0, 6);
  public static final ChillFieldInfo PHIH2 = new ChillFieldInfo("PHIH2", "PHIH2", 16, 36000000, -36000000,0, 6);

  public static final ChillFieldInfo RHOHV2 = new ChillFieldInfo("RHOHV2", "RHOHV2", 20, 1000000, 0, 0, 7);
  public static final ChillFieldInfo RHOH2 = new ChillFieldInfo("RHOH2", "RHOH2", 47, 1000000, 0, 0, 7);

  public static final ChillFieldInfo KDP2 = new ChillFieldInfo("KDP2", "KDP2", 15, 3000000, -1000000, 0, 7);
  public static final ChillFieldInfo CZ = new ChillFieldInfo("CdBZ", "CorrectedReflectivity", 21, 7500000, -1000000, 0,0); // change
  // number?
  public static final ChillFieldInfo SQI2 = new ChillFieldInfo("SQI2", "SQI2", 23, 100000, 0, 0, 7);

  public static final ChillFieldInfo CZDR = new ChillFieldInfo("CZDR", "CorrectedDifferentialReflectivity", 36,9030899, -3010299, 0, 4); 
  public static final ChillFieldInfo AZ = new ChillFieldInfo("AdBZ", "AdjustedReflectivity", 42, 7500000, -1000000, 0,0); 
  public static final ChillFieldInfo AZDR = new ChillFieldInfo("AZDR", "AdjustedDifferentialReflectivity", 46, 9030899,-3010299, 0, 4); 
  public static final ChillFieldInfo ASDP = new ChillFieldInfo("ASDP", "AdptSpecificDifferentialPhase", 0, 25500000, 0,  0, 1); 
  public static final ChillFieldInfo LDRH2 = new ChillFieldInfo("LDRH2", "LDRH2", 26, 5500000, -5500000,0, 4);
  public static final ChillFieldInfo VFast = new ChillFieldInfo("VelFast", "VelocityFast", 27, 5500000, -5500000, 16, 1);
  public static final ChillFieldInfo VSlow = new ChillFieldInfo("VelSlow", "VelocitySlow", 28, 5500000, -5500000, 16, 1);
  public static final ChillFieldInfo HCLASS2 = new ChillFieldInfo("HCLASS2", "HCLASS2", 56,  700000,0, 0, 1);
  public static final ChillFieldInfo DB_ZDRC2 = new ChillFieldInfo("DB_ZDRC2", "Corrected ZDR", 58,9030899, -3010299,0 ,4 );
 // private static final ChillFieldInfo[] types = new ChillFieldInfo[] { Z, V, W, ZDR, PHIDP, RHOHV, KDP, CZ, CZDR, AZ,
 //   AZDR, ASDP, VFilt, VFast, VSlow, DB_HCLASS2, DB_ZDRC2 };

  public static String[] dtype_name =  {"XHDR","DBT","DBZ","VEL","WIDTH","ZDR","ORAIN","DBZC","DBT2","DBZ2","VEL2","WIDTH2",
	  "ZDR2","RAINRATE2","KDP","KDP2","PHIDP","VELC","SQI","RHOHV","RHOHV2","DBZC2","VELC2","SQI2","PHIDP2","LDRH","LDRH2","LDRV",
	  "LDRV2","FLAGS","FLAGS2","31_UNUSED_NOW","HEIGHT","VIL2","NULL","SHEAR","DIVERGE2","FLIQUID2","USER","OTHER","DEFORM2",
	  "VVEL2","HVEL2","HDIR2","AXDIL2","TIME2","RHOH","RHOH2","RHOV","RHOV2","PHIH","PHIH2","PHIV","PHIv2","USER2","HCLASS","HCLASS2",
	  "ZDRC","ZDRC2","VIR","VIR2"};
  
  
  
  private static  ChillFieldInfo[] types = new ChillFieldInfo[]{null,null,null,null,null,null,null,null,ZT,Z,V,W,ZDR2,null,null,KDP2,null,null,null,null,RHOHV2,null,null,SQI2,PHIDP2,null,LDRH2,null,
	  null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,RHOH2,null,null,null,PHIH2,null,null,null,null,HCLASS2,null,null,null,null};
  
 
  public static void load(final ControlMessage command, final CacheMain cache) throws IOException {
    //for (ChillFieldInfo info : types)
    //  infos.put(info.longFieldName, info);
    
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
    int sweepnum=Integer.parseInt((sweepstring.split(" ")[1]));//Grumble Grumble, why would you do it this way?
    int has_xdr=0;
    
    List<ChillMomentFieldScale> scales = new ArrayList<ChillMomentFieldScale>();
    int fieldNum = 0;
    // Setup Scale Manager stuff
   // int sz=5;
    for (int currentVariable = 0; currentVariable < SPR_input.getTotal_vars(); currentVariable++) {

      //if(currentVariable>5) continue;//TODO Get rid of this when vars are finished.
      
      
      while (sm.getScale(fieldNum) != null) {
        ++fieldNum;
      }

      
      int dtype_num= SPR_input.getSweeplist().get(sweepnum).getIdh_list().get(currentVariable).getData_type();
      if(dtype_num == 0) {
    	  has_xdr=1;
    	  continue; //Skip over Extended Headers for now
      }
      
      infos.put(types[dtype_num].longFieldName,types[dtype_num]);
      
      
      String description= null;
      String units=null;
      
      description = dtype_name[dtype_num];
      units = dtype_name[dtype_num];
//      
//      if(currentVariable==0) {
//        description = new String("Total Power");
//        units= new String("dBZ-ZT");
//      }
//      if(currentVariable==1){
//        description = new String("Reflectivity");
//        units = new String("dBZ");
//      }else if(currentVariable ==2){
//        description = new String("Velocity");
//        units = new String("Vel");
//      }else if(currentVariable==3){
//        description = new String("SpectralWidth");
//        units = new String("Wid");
//      }
      
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
    
    ymds_time time_structure= SPR_input.getTop_product_hdr().getAproduct_configuration().getTime_ingest_sweep();
    current_day = Calendar.getInstance();
    current_day.clear();
    current_day.set(time_structure.getYear()-1900,time_structure.getMonth()-1, time_structure.getDay(),-7+(int)(time_structure.getSeconds()/3600.0),(int)( (time_structure.getSeconds() % 3600)/60) );
    current_day.getTimeInMillis();
System.out.println("Quick Test:"+(int) (time_structure.getSeconds()/3600.0));
System.out.println("Quick Test2:"+(int)( (time_structure.getSeconds() % 3600)/60));
    long availableData = 0;
    for (int typeI = 0; typeI < SPR_input.getTotal_vars()-has_xdr; ++typeI) {
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
    int mode=SPR_input.getTop_ingest_header().getAtask_configuration().getAtask_scan_info().getScan_mode();
    if(mode== 1 || mode ==4) hskH.antMode=0;
    if(mode==2 || mode ==7) hskH.antMode =1;
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
      dataH.dataTime = current_day.getTimeInMillis()/1000L;
      dataH.fractionalSecs = 0;

      hskH.gateWidth =10*SPR_input.getTop_ingest_header().getAtask_configuration().getAtask_range_info().getInput_bins_step();

      cache.addRay(command, ChillDefines.META_TYPE, dataH);
      for (int typeI = 0; typeI < SPR_input.getTotal_vars()-has_xdr; ++typeI) {
        // if (data[typeI] == null){
        // System.out.println("Choosing Continue");
        // continue;
        // }
        // Index i2 = data[typeI].getIndex().set0(radialI);
        // double[] typeData = new double[dataH.numGates];
        double[] typeData = currRay.getDatarays().get(typeI+has_xdr).getData();
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
