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

  private static final ScaleManager sm = ScaleManager.getInstance();

  private static final Map<String, ChillFieldInfo> infos = new HashMap<String, ChillFieldInfo>();

  public static final ChillFieldInfo Z = new ChillFieldInfo("dBZ", "Reflectivity", 8, 7500000, -1000000, 0, 0);
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

  private static final ChillFieldInfo[] types = new ChillFieldInfo[]{Z};
  static { // Static Initializer
    for (ChillFieldInfo info : types)
      infos.put(info.longFieldName, info);
  }
 
  public static void load(final ControlMessage command, final CacheMain cache) throws IOException {
    String path = FileFunctions.stripFileName(command.getDir()) + "/" + FileFunctions.stripFileName(command.getFile());
    System.out.println("IrisRawFile Object Initialized");
    try {

      fstream_in = new FileInputStream(path);

      dstream_in = new DataInputStream(fstream_in);

    } catch (Exception e) {
      System.err.println("Exception: " + e);
    }

    SPR_input = new SigmetProductRaw(dstream_in);

    List<ChillMomentFieldScale> scales = new ArrayList<ChillMomentFieldScale>();
    int fieldNum = 0;
    // Setup Scale Manager stuff
    for (int currentVariable = 0; currentVariable < SPR_input.getTotal_vars(); currentVariable++) {
      while (sm.getScale(fieldNum) != null) {
        ++fieldNum;
      }

    }
    // Variable var = (Variable) obj;
    // if (var.getDataType() == DataType.FLOAT) {
    // vars.add(var);
    // String units = var.findAttribute("Units").getStringValue().trim();
    String description = new String("Reflectivity");
    // // System.out.println("Found data type: " + description + " in " +
    // // units);
    ChillFieldInfo info = infos.get(description);
    if (info == null) { // unknown
      System.out.println("Null Info Discovered");
      info = new ChillFieldInfo(description.substring(0, 4) + fieldNum, description, fieldNum++, 12800000, -12800000,
        0, 0);
    }
    String units = new String("dBz");
    ChillMomentFieldScale scale = new ChillMomentFieldScale(info, -1, units, 100000, 1, 0);
    scales.add(scale);
    cache.addRay(command, ChillDefines.META_TYPE, scale);
    sm.putScale(scale);
    // }
    // }
    //

    int sz = 1;

    Array[] data = new Array[sz];
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

    hskH.radarLatitude = (int) (1e6 * 5000);
    hskH.radarLongitude = (int) (1e6 * 4000);
    hskH.radarId = SPR_input.getTop_ingest_header().getTop_ingest_config().getHardware_name_of_site();
    hskH.angleScale = 0x7fffffff;
    cache.addRay(command, ChillDefines.META_TYPE, hskH);

    int numgates = SPR_input.getRange_bins();
    int numrays = SPR_input.getSweeplist().get(1).getIdh_list().get(1).getRays_present();
    System.out.println("Number of rays:" + numrays);
    for (int radialI = 0; radialI < numrays; ++radialI) {// radial.getLength();
      // ++radialI) { //Here we add
      // rays individually
      Ray currRay = SPR_input.getSweeplist().get(0).getRays().get(radialI);
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
      dataH.startAz = dataH.endAz = (int) (((double) radialI) / 360 * hskH.angleScale);
      System.out.println("DataH" + dataH.endAz);
      dataH.startEl = dataH.endEl = (int) 5 / 360 * hskH.angleScale;
      dataH.numGates = numgates;
      // dataH.startRange = startRange.getInt(i1);
      // dataH.dataTime = time.getInt(i1) & 0xffffffff;
      // dataH.fractionalSecs = timenSec == null ? 0 : timenSec.getInt(i1);
      dataH.startRange = 0;
      dataH.dataTime = 0;
      dataH.fractionalSecs = 0;

      // hskH.gateWidth = (int) gateWidth.getFloat(i1);

      hskH.gateWidth = (int) 100000;

      cache.addRay(command, ChillDefines.META_TYPE, dataH);
      for (int typeI = 0; typeI < sz; ++typeI) {
        // if (data[typeI] == null){
        // System.out.println("Choosing Continue");
        // continue;
        // }
        // Index i2 = data[typeI].getIndex().set0(radialI);
        // double[] typeData = new double[dataH.numGates];
        double[] typeData = currRay.getDatarays().get(1).getData();
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
