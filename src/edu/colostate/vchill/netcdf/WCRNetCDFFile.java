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
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;

/**
 * Class for reading WCR NetCDF archive files
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class WCRNetCDFFile {
    private static final ScaleManager sm = ScaleManager.getInstance();

    public static final ChillFieldInfo V = new ChillFieldInfo("dv", "Doppler radial velocity", 13, 7500000, -7500000, 16, 1);
    private static final ChillFieldInfo[] types = new ChillFieldInfo[]{
            V,
    };

    public static void main(final String[] args) throws IOException {
        //for (String arg : args) System.out.println(arg);
        //load(new Contreargs[0], new CacheMainLRU());
    }

    public static void load(final ControlMessage command, final CacheMain cache) throws IOException {
        String path = FileFunctions.stripFileName(command.getDir()) + "/" + FileFunctions.stripFileName(command.getFile());
        NetcdfFile ncFile = NetcdfFile.open(path);
        Dimension radial = ncFile.hasUnlimitedDimension() ?
                ncFile.getUnlimitedDimension() : ncFile.getRootGroup().findDimension("profile");
        Dimension gate = ncFile.getRootGroup().findDimension("range");

        for (ChillFieldInfo info : types) {
            Moment type = Moment.values()[info.fieldNumber];
            ChillMomentFieldScale scale = new ChillMomentFieldScale(info, type.ACCELERATOR, type.UNITS, 1000000, 0, 0);
            sm.putScale(scale);
            cache.addRay(command, ChillDefines.META_TYPE, scale);
        }

        Array baseTime = ncFile.findVariable("base_time").read();
        Array time = ncFile.findVariable("time_offset").read();
        int base = baseTime.getInt(baseTime.getIndex().set(0));
        Array range = ncFile.findVariable("range").read();

        Array[] data = new Array[types.length];
        for (int typeI = 0; typeI < types.length; ++typeI) {
            Variable var = ncFile.findVariable(types[typeI].fieldName);
            if (var == null) continue;
            data[typeI] = var.read();
        }

        ChillHSKHeader hskH = new ChillHSKHeader();
        hskH.radarId = ncFile.findGlobalAttribute("Aircraft").getStringValue();
        hskH.antMode = 2;
        hskH.angleScale = 0x7fffffff;
        hskH.gateWidth = (int) ((range.getFloat(range.getIndex().set(1)) - range.getFloat(range.getIndex().set(0))) * 1e3);
        cache.addRay(command, ChillDefines.META_TYPE, hskH);
        for (int radialI = 0; radialI < radial.getLength(); ++radialI) {
            ChillDataHeader dataH = new ChillDataHeader();
            Index i1 = time.getIndex().set(radialI);
            dataH.availableData = -1; //ALL types
            dataH.startAz = dataH.endAz = 0;
            dataH.startEl = dataH.endEl = (int) (90.0 / 360.0 * hskH.angleScale);
            dataH.numGates = gate.getLength();
            dataH.startRange = (int) (range.getFloat(range.getIndex().set(0)) * 1e3);
            double offset = time.getDouble(i1);
            dataH.dataTime = (int) (base + offset);
            dataH.fractionalSecs = (int) ((offset - (int) offset) * 1e6);
            cache.addRay(command, ChillDefines.META_TYPE, dataH);
            for (int typeI = 0; typeI < types.length; ++typeI) {
                if (data[typeI] == null) continue;
                Index i2 = data[typeI].getIndex().set0(radialI);
                double[] typeData = new double[dataH.numGates];
                for (int gateI = 0; gateI < dataH.numGates; ++gateI) {
                    typeData[gateI] = data[typeI].getDouble(i2.set1(gateI));
                }
                cache.addRay(command, types[typeI].fieldName, new ChillGenRay(hskH, dataH, types[typeI].fieldName, typeData));
                //System.out.println("cached " + types[typeI].fieldName + " ray");
                Thread.yield();
            }
        }

        for (String type : sm.getTypes()) {
            cache.setCompleteFlag(command, type);
            System.out.println("marked " + type + " complete; cached " + cache.getNumberOfRays(command, type) + " rays");
        }
        cache.setCompleteFlag(command, ChillDefines.META_TYPE);
    }
}
