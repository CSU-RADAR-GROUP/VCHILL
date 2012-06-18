package edu.colostate.vchill.netcdf;

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
 * Class for reading CASA NetCDF archive files
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class CASANetCDFFile
{
    private static final ScaleManager sm = ScaleManager.getInstance();

    private static final Map<String, ChillFieldInfo> infos = new HashMap<String, ChillFieldInfo>();

    public static final ChillFieldInfo Z = new ChillFieldInfo("dBZ", "Reflectivity", 12, 7500000, -1000000, 0, 0);
    public static final ChillFieldInfo V = new ChillFieldInfo("Vel", "Velocity", 13, 5500000, -5500000, 16, 1);
    public static final ChillFieldInfo W = new ChillFieldInfo("Wid", "SpectralWidth", 14, 800000, 0, 0, 2);
    public static final ChillFieldInfo NCP = new ChillFieldInfo("NCP", "NormalizedCoherentPower", 15, 100000, 0, 0, 3);
    public static final ChillFieldInfo ZDR = new ChillFieldInfo("ZDR", "DifferentialReflectivity", 16, 9030899, -3010299, 0, 4);
    public static final ChillFieldInfo PHIDP = new ChillFieldInfo("PHIDP", "DifferentialPhase", 19, 90000000, -90000000, 0, 6);
    public static final ChillFieldInfo RHOHV = new ChillFieldInfo("RhoHV", "CrossPolCorrelation", 20, 1000000, 0, 0, 7);
    public static final ChillFieldInfo KDP = new ChillFieldInfo("KDP", "SpecificPhase", 22, 3000000, -1000000, 0, 7);
    public static final ChillFieldInfo CZ = new ChillFieldInfo("CdBZ", "CorrectedReflectivity", 32, 7500000, -1000000, 0, 0); //change number?
    public static final ChillFieldInfo CZDR = new ChillFieldInfo("CZDR", "CorrectedDifferentialReflectivity", 36, 9030899, -3010299, 0, 4); //change number?
    public static final ChillFieldInfo AZ = new ChillFieldInfo("AdBZ", "AdjustedReflectivity", 42, 7500000, -1000000, 0, 0); //change number?
    public static final ChillFieldInfo AZDR = new ChillFieldInfo("AZDR", "AdjustedDifferentialReflectivity", 46, 9030899, -3010299, 0, 4); //change number?
    public static final ChillFieldInfo ASDP = new ChillFieldInfo("ASDP", "AdptSpecificDifferentialPhase", 0, 25500000, 0, 0, 1); //fix scale, colors, etc
    public static final ChillFieldInfo VFilt = new ChillFieldInfo("VelFilt", "FilteredVelocity", 26, 5500000, -5500000, 16, 1);	
    public static final ChillFieldInfo VFast = new ChillFieldInfo("VelFast", "VelocityFast", 27, 5500000, -5500000, 16, 1);
    public static final ChillFieldInfo VSlow = new ChillFieldInfo("VelSlow", "VelocitySlow", 28, 5500000, -5500000, 16, 1);
    private static final ChillFieldInfo[] types = new ChillFieldInfo[] {
        Z, V, W, NCP, ZDR, PHIDP, RHOHV, KDP, CZ, CZDR, AZ, AZDR, ASDP, VFilt, VFast, VSlow
    };
    static {
        for (ChillFieldInfo info : types) infos.put(info.longFieldName, info);
    }

    public static void load (final ControlMessage command, final CacheMain cache) throws IOException
    {
        String path = FileFunctions.stripFileName(command.getDir()) + "/" + FileFunctions.stripFileName(command.getFile());
        
        NetcdfFile ncFile = NetcdfFile.open(path);
        Dimension radial = ncFile.hasUnlimitedDimension() ?
            ncFile.getUnlimitedDimension() : ncFile.getRootGroup().findDimension("Radial");
        Dimension gate = ncFile.getRootGroup().findDimension("Gate");

        List<Variable> vars = new ArrayList<Variable>();
        List<ChillMomentFieldScale> scales = new ArrayList<ChillMomentFieldScale>();
        int fieldNum = 0;
        for (Object obj : ncFile.getVariables()) {
            while (sm.getScale(fieldNum) != null) ++fieldNum;
            Variable var = (Variable)obj;
            if (var.getDataType() == DataType.FLOAT) {
                vars.add(var);
                String units = var.findAttribute("Units").getStringValue().trim();
                String description = var.getShortName();
                //System.out.println("Found data type: " + description + " in " + units);
                ChillFieldInfo info = infos.get(description);
                if (info == null) { //unknown
                    info = new ChillFieldInfo(description.substring(0, 4) + fieldNum, description, fieldNum++, 12800000, -12800000, 0, 0);
                }
                ChillMomentFieldScale scale = new ChillMomentFieldScale(info, -1, units, 100000, 1, 0);
                scales.add(scale);
                cache.addRay(command, ChillDefines.META_TYPE, scale);
                sm.putScale(scale);
            }
        }

        Array azimuth     = ncFile.findVariable("Azimuth").read();
        Array elevation   = ncFile.findVariable("Elevation").read();
        Array gateWidth   = ncFile.findVariable("GateWidth").read();
        Array startRange  = ncFile.findVariable("StartRange").read();
        Array time        = ncFile.findVariable("Time").read();
        Array timenSec    = null;
        {
            Variable tns = ncFile.findVariable("TimenSec");
            if (tns != null) timenSec = tns.read();
        }

        Array[] data = new Array[vars.size()];
        long availableData = 0;
        for (int typeI = 0; typeI < vars.size(); ++typeI) {
            Variable var = vars.get(typeI);
            data[typeI] = var.read();
            availableData |= 1l << scales.get(typeI).fieldNumber;
        }

        ChillHSKHeader hskH = new ChillHSKHeader();
        hskH.radarLatitude = (int)(1e6 * ncFile.findGlobalAttribute("Latitude").getNumericValue().doubleValue());
        hskH.radarLongitude = (int)(1e6 * ncFile.findGlobalAttribute("Longitude").getNumericValue().doubleValue());
        hskH.radarId = ncFile.findGlobalAttribute("RadarName").getStringValue();
        hskH.angleScale = 0x7fffffff;
        cache.addRay(command, ChillDefines.META_TYPE, hskH);
        for (int radialI = 0 ; radialI < radial.getLength(); ++radialI) {
            ChillDataHeader dataH = new ChillDataHeader();
            Index i1 = azimuth.getIndex().set(radialI);
            dataH.availableData = availableData;
            //dataH.availableData = -1;
            dataH.startAz = dataH.endAz = (int)(azimuth.getDouble(i1) / 360 * hskH.angleScale);
            dataH.startEl = dataH.endEl = (int)(elevation.getDouble(i1) / 360 * hskH.angleScale);
            dataH.numGates = gate.getLength();
            dataH.startRange = startRange.getInt(i1);
            dataH.dataTime = time.getInt(i1) & 0xffffffff;
            dataH.fractionalSecs = timenSec == null ? 0 : timenSec.getInt(i1);
            hskH.gateWidth = (int)gateWidth.getFloat(i1);
            cache.addRay(command, ChillDefines.META_TYPE, dataH);
            for (int typeI = 0; typeI < vars.size(); ++typeI) {
                if (data[typeI] == null) continue;
                Index i2 = data[typeI].getIndex().set0(radialI);
                double[] typeData = new double[dataH.numGates];
                for (int gateI = 0; gateI < dataH.numGates; ++gateI) {
                    typeData[gateI] = data[typeI].getDouble(i2.set1(gateI));
 //                   System.out.println("Type Data"+typeData[gateI]);
                }
                cache.addRay(command, scales.get(typeI).fieldName, new ChillGenRay(hskH, dataH, scales.get(typeI).fieldName, typeData));
            }
        }

        for (String type : sm.getTypes()) {
            cache.setCompleteFlag(command, type);
            System.out.println("marked " + type + " complete; cached " + cache.getNumberOfRays(command, type) + " rays");
        }
        cache.setCompleteFlag(command, ChillDefines.META_TYPE);
        ncFile.close();
    }
}
