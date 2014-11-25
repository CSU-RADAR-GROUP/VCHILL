package edu.colostate.vchill.netcdf;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.chill.*;
import edu.colostate.vchill.file.FileFunctions;
import edu.colostate.vchill.file.FileFunctions.Moment;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;

/**
 * Class for reading NCAR NetCDF archive files
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class NCARNetCDFFile {
    private static final ScaleManager sm = ScaleManager.getInstance();

    public static final ChillFieldInfo AVG_I = new ChillFieldInfo("AVG_I", "average inphase measurement", 1, 32767, -32768, 0, 0);
    public static final ChillFieldInfo AVG_Q = new ChillFieldInfo("AVG_Q", "average quadrature measurement", 2, 32767, -32768, 0, 0);
    public static final ChillFieldInfo DBZ = new ChillFieldInfo("DBZ", "reflectivity", 12, 7500, -1000, 0, 0);
    public static final ChillFieldInfo VE = new ChillFieldInfo("VE", "velocity", 13, 5500, -5500, 0, 1);
    public static final ChillFieldInfo SW = new ChillFieldInfo("SW", "spectral width", 14, 5500, -5500, 0, 2);
    public static final ChillFieldInfo NCP = new ChillFieldInfo("NCP", "normalized coherent power", 15, 100, 0, 0, 3);
    public static final ChillFieldInfo DM = new ChillFieldInfo("DM", "power", 33, 5000, -20000, 0, 0);
    public static final ChillFieldInfo AIQ = new ChillFieldInfo("AIQ", "I & Q phase", 34, 18000, -18000, 0, 0);
    public static final ChillFieldInfo NIQ = new ChillFieldInfo("NIQ", "I & Q magnitude", 35, 8000, -8000, 0, 0);
    public static final ChillFieldInfo DELTA_N = new ChillFieldInfo("DELTA_N", "DELTA_N", 36, 3000, -3000, 0, 0); //fix long name
    public static final ChillFieldInfo N = new ChillFieldInfo("N", "N", 37, 50000, 10000, 0, 0); //fix long name
    public static final ChillFieldInfo SIGMA_DN = new ChillFieldInfo("SIGMA_DN", "SIGMA_DN", 38, 3000, 0, 0, 0); //fix long name
    public static final ChillFieldInfo SIGMA_N = new ChillFieldInfo("SIGMA_N", "SIGMA_N", 39, 3000, 0, 0, 0); //fix long name
    private static final ChillFieldInfo[] types = new ChillFieldInfo[]{
            AVG_I, AVG_Q, DBZ, VE, SW, NCP, DM, AIQ, NIQ, DELTA_N, N, SIGMA_DN, SIGMA_N,
    };

    public static void load(final ControlMessage command, final CacheMain cache) throws IOException {
        String path = FileFunctions.stripFileName(command.getDir()) + "/" + FileFunctions.stripFileName(command.getFile());
        NetcdfFile ncFile = NetcdfFile.open(path);
        Dimension radial = ncFile.hasUnlimitedDimension() ?
                ncFile.getUnlimitedDimension() : ncFile.getRootGroup().findDimension("Time");
        Dimension gate = ncFile.getRootGroup().findDimension("maxCells");

        ChillMomentFieldScale[] types = null;
        Variable fields = ncFile.findVariable("fields");
        if (fields == null) fields = ncFile.findVariable("field_names");
        if (fields == null) { //use hardcoded list
            types = new ChillMomentFieldScale[NCARNetCDFFile.types.length];
            for (int i = 0; i < types.length; ++i) {
                if (NCARNetCDFFile.types[i].fieldNumber < Moment.values().length) {
                    Moment type = Moment.values()[NCARNetCDFFile.types[i].fieldNumber];
                    types[i] = new ChillMomentFieldScale(NCARNetCDFFile.types[i], type.ACCELERATOR, type.UNITS, 100, 0, 0);
                } else {
                    types[i] = new ChillMomentFieldScale(NCARNetCDFFile.types[i], -1, null, 100, 1, 0);
                }
                cache.addRay(command, ChillDefines.META_TYPE, types[i]);
                sm.putScale(types[i]);
            }
        } else {
            char[][] fieldnames = (char[][]) fields.read().copyToNDJavaArray();
            types = new ChillMomentFieldScale[fieldnames.length];
            for (int fieldI = 0; fieldI < fieldnames.length; ++fieldI) {
                String name = new String(fieldnames[fieldI]);
//System.out.println("Found field '" + name + "'");
                name = name.trim();
//System.out.println(" trimmed '" + name + "'");
                Variable field = ncFile.findVariable(name);
                if (field == null) continue; //should not be possible
                String description = field.getDescription();
//System.out.println("  is " + description);
                //String units =  field.findAttribute("units").getStringValue().trim();
                String units = field.getUnitsString();
//System.out.println("  in " + units);
                ChillFieldInfo info = new ChillFieldInfo(name, description, fieldI, 32767, -32768, 0, 0);
                types[fieldI] = new ChillMomentFieldScale(info, -1, units, 100, 1, 0);
                cache.addRay(command, ChillDefines.META_TYPE, types[fieldI]);
                sm.putScale(types[fieldI]);
            }
        }

        Array azimuth = ncFile.findVariable("Azimuth").read();
        Array elevation = ncFile.findVariable("Elevation").read();
        int baseTime = ncFile.findVariable("base_time").readScalarInt();
        Array time = ncFile.findVariable("time_offset").read();
        //Array timenSec    = ncFile.findVariable("TimenSec").read();
        int startRange = (int) (1e3 * ncFile.findVariable("Range_to_First_Cell").readScalarFloat());

        Array[] data = new Array[types.length];
        double[] missing = new double[types.length];
        double[] scale = new double[types.length];
        double[] offset = new double[types.length];
        long availableData = 0;
        for (int typeI = 0; typeI < types.length; ++typeI) {
            if (types[typeI] == null) continue; //couldn't read scaling info
            Variable var = ncFile.findVariable(types[typeI].fieldName);
            if (var == null) var = ncFile.findVariable(types[typeI].fieldDescription); //retry with long name
            if (var == null) continue; //type not available
            data[typeI] = var.read();
            availableData |= 1l << types[typeI].fieldNumber;
            missing[typeI] = var.findAttribute("missing_value").getNumericValue().doubleValue();
            Attribute sf = var.findAttribute("scale_factor");
            scale[typeI] = sf == null ? 1 : sf.getNumericValue().doubleValue();
            Attribute ao = var.findAttribute("add_offset");
            offset[typeI] = ao == null ? 0 : ao.getNumericValue().doubleValue();
        }

        ChillHSKHeader hskH = new ChillHSKHeader();
        if (ncFile.findGlobalAttribute("Scan_Mode").getStringValue().equals("RHI")) hskH.antMode = 1;
        hskH.radarLatitude = (int) (1e6 * ncFile.findVariable("Latitude").readScalarDouble());
        hskH.radarLongitude = (int) (1e6 * ncFile.findVariable("Longitude").readScalarDouble());
        hskH.gateWidth = (int) (1e3 * ncFile.findVariable("Cell_Spacing").readScalarFloat());
        hskH.radarId = ncFile.findGlobalAttribute("Instrument_Name").getStringValue();
        hskH.angleScale = 0x7fffffff;
        cache.addRay(command, ChillDefines.META_TYPE, hskH);
        for (int radialI = 0; radialI < radial.getLength(); ++radialI) {
            ChillDataHeader dataH = new ChillDataHeader();
            Index i1 = azimuth.getIndex().set(radialI);
            dataH.availableData = availableData;
            dataH.startAz = dataH.endAz = (int) (azimuth.getDouble(i1) / 360 * hskH.angleScale);
            dataH.startEl = dataH.endEl = (int) (elevation.getDouble(i1) / 360 * hskH.angleScale);
            dataH.numGates = gate.getLength();
            dataH.startRange = startRange;
            double t = time.getDouble(i1);
            dataH.dataTime = baseTime + (long) t;
            dataH.fractionalSecs = (int) ((t - (long) t) * 1e9);
            cache.addRay(command, ChillDefines.META_TYPE, dataH);
            for (int typeI = 0; typeI < types.length; ++typeI) {
                if (data[typeI] == null) continue;
                Index i2 = data[typeI].getIndex().set0(radialI);
                double[] typeData = new double[dataH.numGates];
                for (int gateI = 0; gateI < dataH.numGates; ++gateI) {
                    double value = data[typeI].getDouble(i2.set1(gateI));
                    if (value == missing[typeI]) typeData[gateI] = Double.NaN;
                    else typeData[gateI] = scale[typeI] * (value - offset[typeI]);
                }
                cache.addRay(command, types[typeI].fieldName, new ChillGenRay(hskH, dataH, types[typeI].fieldName, typeData));
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
