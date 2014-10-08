package edu.colostate.vchill.netcdf;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.chill.*;
import edu.colostate.vchill.file.FileFunctions;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for reading CF/Radial Files
 *
 * @author Joseph C. Hardin
 * @version 2014-04-28
 */
public class CFRadialFile
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
        System.out.println(path);
        NetcdfFile ncFile;
        try {
            ncFile = NetcdfFile.open(path);
        } catch (Exception e)
        {
            System.out.println("Exception e:" + e.toString());
            e.printStackTrace();
            return;
        }
        Dimension radial = ncFile.hasUnlimitedDimension() ?
            ncFile.getUnlimitedDimension() : ncFile.getRootGroup().findDimension("time");
        Dimension gate = ncFile.getRootGroup().findDimension("range");

        List<Variable> vars = new ArrayList<Variable>();
        List<ChillMomentFieldScale> scales = new ArrayList<ChillMomentFieldScale>();
        int fieldNum = 0;
        for (Object obj : ncFile.getVariables()) {
            while (sm.getScale(fieldNum) != null) ++fieldNum;
            Variable var = (Variable)obj;
            if (    var.getDimensions().contains(radial) && var.getDimensions().contains(gate)){
                vars.add(var);
                String units = var.findAttribute("units").getStringValue().trim();
                String description = var.getShortName();
                //System.out.println("Found data type: " + description + " in " + units);
                ChillFieldInfo info = infos.get(description);
                if (info == null) { //unknown
                    if(description.length() > 4)
                        info = new ChillFieldInfo(description.substring(0, 4) + fieldNum, description, fieldNum++, 12800000, -12800000, 0, 0)  ;
                    else
                        info = new ChillFieldInfo(description + fieldNum, description, fieldNum++, 12800000, -12800000, 0, 0);

                }
                ChillMomentFieldScale scale = new ChillMomentFieldScale(info, -1, units,(int) (1/var.findAttribute("scale_factor").getNumericValue().doubleValue()),1, 0);
                scales.add(scale);
                cache.addRay(command, ChillDefines.META_TYPE, scale);
                sm.putScale(scale);
            }
        }

        Array azimuth     = ncFile.findVariable("azimuth").read();
        Array elevation   = ncFile.findVariable("elevation").read();
        Array time        = ncFile.findVariable("time").read();
        Array range       = ncFile.findVariable("range").read();
       // Array gateWidth   = ncFile.findVariable("GateWidth").read();
        //Array startRange  = ncFile.findVariable("StartRange").read();
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
        hskH.radarLatitude = (int) ncFile.findVariable("latitude").read().getDouble(0);
        hskH.radarLongitude =(int) ncFile.findVariable("longitude").read().getDouble(0);
        hskH.radarId = ncFile.findGlobalAttribute("instrument_name").getStringValue();
        hskH.angleScale = 10000;
        cache.addRay(command, ChillDefines.META_TYPE, hskH);
        for (int radialI = 0 ; radialI < radial.getLength(); ++radialI) {
            ChillDataHeader dataH = new ChillDataHeader();
            Index i1 = azimuth.getIndex().set(radialI);
            dataH.availableData = availableData;
            //dataH.availableData = -1;
            dataH.startAz = dataH.endAz = (int)(azimuth.getDouble(i1)/360.0*hskH.angleScale );
            dataH.startEl = dataH.endEl = (int)(elevation.getDouble(i1)/360.0 *hskH.angleScale);
            dataH.numGates = gate.getLength();
            dataH.startRange = range.getInt(0);
            dataH.dataTime = time.getInt(i1);
            dataH.fractionalSecs = 0;
            hskH.gateWidth = (int)(1000*(range.getInt(1)-range.getInt(0)));
            cache.addRay(command, ChillDefines.META_TYPE, dataH);
            for (int typeI = 0; typeI < vars.size(); ++typeI) {
                if (data[typeI] == null) continue;
                Index i2 = data[typeI].getIndex().set0(radialI);
                double[] typeData = new double[dataH.numGates];
                for (int gateI = 0; gateI < dataH.numGates; ++gateI) {
                    typeData[gateI] =  (1.0/scales.get(typeI).factor)*data[typeI].getDouble(i2.set1(gateI));
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
