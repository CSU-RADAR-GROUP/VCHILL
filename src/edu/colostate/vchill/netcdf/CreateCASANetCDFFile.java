package edu.colostate.vchill.netcdf;

import edu.colostate.vchill.ProgressMonitorInputStream;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.file.FileAlterer;
import edu.colostate.vchill.file.FileFieldScalingInfo;
import edu.colostate.vchill.file.FileFunctions;
import edu.colostate.vchill.file.FileFunctions.Moment;
import edu.colostate.vchill.file.FileRay;
import edu.colostate.vchill.file.FileSweep;
import edu.colostate.vchill.gui.GUIUtil;
import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TimeZone;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

/**
 * Class to translate CHILL DRX files to CASA NetCDF
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class CreateCASANetCDFFile
{
    private static final ScaleManager sm = ScaleManager.getInstance();
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'-'HHmmss");
    static {
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        for (ChillFieldInfo info : ChillFieldInfo.types) {
            Moment type = Moment.values()[info.fieldNumber];
            sm.putScale(new ChillMomentFieldScale(info, type.ACCELERATOR, type.UNITS, 0, 0, 0));
        }
    }

    /**
     * Translates a sweep from CHILL DRX format to a CASA NetCDF file
     * @param sweep the sweep to translate
     * @param dir the directory to write to
     */
    public static void createNetCDFFile (final FileSweep sweep, final String dir) throws InvalidRangeException, IOException
    {
        final String radarname = sweep.paramD.getRadarName();
        final String scanmode = sweep.paramD.getScanMode();
        final String starttime = df.format(sweep.data.get(0).dataH.time * 1000l);
        final String filename = dir + "/" + radarname + starttime + ".nc";
        System.out.println("    Writing to file " + filename);

        final Collection<String> availableMoments = FileFunctions.getAvailableMoments(sweep.paramD);

        int numRays = sweep.data.size();
        double[] azimuth = new double[numRays];
        double[] elevation = new double[numRays];
        double[] gateWidth = new double[numRays];
        double[] startRange = new double[numRays];
        int[] time = new int[numRays];
        int[] timenSec = new int[numRays];
        double[] txFrequency = new double[numRays];
        double[] txLength = new double[numRays];
        double[] txPower = new double[numRays];
        byte[][][] data = new byte[availableMoments.size()][numRays][];

        { int r = 0; for (FileRay ray : sweep.data) {
            azimuth[r] = ray.dataH.azimuth * 1e-6;
            elevation[r] = ray.dataH.elevation * 1e-6;
            gateWidth[r] = sweep.paramD.gate_spacing;
            startRange[r] = sweep.paramD.start_range;
            time[r] = ray.dataH.time;
            //timenSec stays blank; nanosecond precision not available
            txFrequency[r] = sweep.paramD.prf * 1e-3f;
            txLength[r] = sweep.paramD.pulse_width;
            txPower[r] = (sweep.paramD.txmit_power_H + sweep.paramD.txmit_power_V) / 200.0;
            { int m = 0; for (String moment : availableMoments) {
                data[m][r] = ray.data[sm.getScale(moment).fieldNumber];
            ++m; }}
        ++r; }}

        NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(filename, false);

        Dimension gate = ncfile.addDimension("Gate", sweep.paramD.ngates);
        Dimension radial = ncfile.addDimension("Radial", numRays);
        radial.setUnlimited(true);

        ncfile.addVariable("Nyquist_Velocity", DataType.FLOAT, new Dimension[0]);
        ncfile.addVariableAttribute("Nyquist_Velocity", "units", "meters/second");

        ncfile.addVariable("Wavelength", DataType.FLOAT, new Dimension[0]);
        ncfile.addVariableAttribute("Wavelength", "units", "meters");

        //----------------------------------------------------------------------

        ncfile.addVariable("Azimuth", DataType.DOUBLE, new Dimension[] {radial});
        ncfile.addVariableAttribute("Azimuth", "Units", "Degrees");

        ncfile.addVariable("Elevation", DataType.DOUBLE, new Dimension[] {radial});
        ncfile.addVariableAttribute("Elevation", "Units", "Degrees");

        ncfile.addVariable("GateWidth", DataType.DOUBLE, new Dimension[] {radial});
        ncfile.addVariableAttribute("GateWidth", "Units", "Millimeters");

        ncfile.addVariable("StartRange", DataType.DOUBLE, new Dimension[] {radial});
        ncfile.addVariableAttribute("StartRange", "Units", "Millimeters");

        ncfile.addVariable("Time", DataType.INT, new Dimension[] {radial});
        ncfile.addVariableAttribute("Time", "Units", "Seconds");

        ncfile.addVariable("TimenSec", DataType.INT, new Dimension[] {radial});
        ncfile.addVariableAttribute("Time", "Units", "NanoSeconds");

        ncfile.addVariable("TxFrequency", DataType.DOUBLE, new Dimension[] {radial});
        ncfile.addVariableAttribute("TxFrequency", "Units", "Hertz");

        ncfile.addVariable("TxLength", DataType.DOUBLE, new Dimension[] {radial});
        ncfile.addVariableAttribute("TxLength", "Units", "Seconds");

        ncfile.addVariable("TxPower", DataType.DOUBLE, new Dimension[] {radial});
        ncfile.addVariableAttribute("TxPower", "Units", "dBm");

        //data
        for (String moment : availableMoments) {
            ncfile.addVariable(moment, DataType.FLOAT, new Dimension[] {radial, gate});
            ChillMomentFieldScale scale = sm.getScale(moment);
            ncfile.addVariableAttribute(moment, "Units", scale.units == null ? "" : scale.units);
            ncfile.addVariableAttribute(moment, "MaxValue", scale.maxValue);
            ncfile.addVariableAttribute(moment, "MinValue", scale.minValue);
            ncfile.addVariableAttribute(moment, "Factor", scale.factor);
            ncfile.addVariableAttribute(moment, "Scale", scale.scale);
            ncfile.addVariableAttribute(moment, "Bias", scale.bias);
        }

        ncfile.addGlobalAttribute("RadarName", radarname);
        ncfile.addGlobalAttribute("Latitude", sweep.paramD.latitude * 1e-6);
        ncfile.addGlobalAttribute("Longitude", sweep.paramD.longitude * 1e-6);
        ncfile.addGlobalAttribute("Height", sweep.paramD.altitude * 1.0);
        ncfile.addGlobalAttribute("NumGates", sweep.paramD.ngates);
        ncfile.addGlobalAttribute("AntennaBeamwidth", sweep.paramD.beam_width * 1e-6);
        ncfile.addGlobalAttribute("Scan_Mode", scanmode); //PPI/RHI
        ncfile.addGlobalAttribute("Start_Time", starttime);

        ncfile.create();

        ncfile.write("Nyquist_Velocity", factory((sweep.paramD.wavelength * 1e-6f / 2) * sweep.paramD.prf * 1e-3f));
        ncfile.write("Wavelength", factory(sweep.paramD.wavelength * 1e-6f));
        //----------------------------------------------------------------------
        ncfile.write("Azimuth", Array.factory(azimuth));
        ncfile.write("Elevation", Array.factory(elevation));
        ncfile.write("GateWidth", Array.factory(gateWidth));
        ncfile.write("StartRange", Array.factory(startRange));
        ncfile.write("Time", Array.factory(time));
        ncfile.write("TimenSec", Array.factory(timenSec));
        ncfile.write("TxFrequency", Array.factory(txFrequency));
        ncfile.write("TxLength", Array.factory(txLength));
        ncfile.write("TxPower", Array.factory(txPower));
        { int i = 0; for (String moment : availableMoments) {
            ncfile.write(moment, Array.factory(decode(data[i], sweep.fieldScalings[sm.getScale(moment).fieldNumber])));
        ++i; }}

        ncfile.close();
    }

    private static ArrayDouble factory (final double number)
    {
        ArrayDouble.D0 array = new ArrayDouble.D0();
        array.set(number);
        return array;
    }

    /**
     * Translates a file from CHILL DRX format to a series of CASA NetCDF files - one per sweep
     * @param in the file to read from
     * @param out the directory to write to
     */
    private static void processFile (final File in, final String out, final JFrame win) throws Throwable
    {
        final JProgressBar progressBar = GUIUtil.addProgressBar(win);
        DataInputStream input = new DataInputStream(new ProgressMonitorInputStream(new FileInputStream(in), progressBar));
        EventQueue.invokeLater(new Runnable() { public void run () {
            progressBar.setMaximum((int)in.length());
            progressBar.setStringPainted(true);
        }});

        /**
         * The actual data file, as a series of objects.
         * The first is always a FileSKUHeader which describes what the next object
         * will be, followed by that object.  The Integer size trailer that would come
         * in the actual data file after each described object is discarded for easier
         * updating/adding of types.  This pattern (SKU Header, described object)
         * repeats indefinitely.
         */
        LinkedList<Object> file;

        /**
         * Only the data portion, as a series of FileSweep objects, for easier manipulation.
         * References the same data arrays as <code>file</code>.
         */
        LinkedList<FileSweep> sweeps;

        { int sweepnum = 0; while (input.available() > 0) { //each sweep
            file = new LinkedList<Object>();

            System.out.println("Sweep " + sweepnum + ":");

            System.out.println("    Reading from file " + in);
            sweeps = FileAlterer.load(file, input);

            EventQueue.invokeLater(new Runnable() { public void run () {
                progressBar.setIndeterminate(true);
            }});

            for (FileSweep sweep : sweeps) createNetCDFFile(sweep, out);

            EventQueue.invokeLater(new Runnable() { public void run () {
                progressBar.setIndeterminate(false);
            }});
        ++sweepnum; }}

        input.close();
        System.out.println("All done!");
    }

    public static void main (final String[] args) throws Throwable
    {
        processFile(new File(args[0]), args.length > 1 ? args[1] : "..", GUIUtil.startGUI("Java VCHILL Data File Translator"));
        System.exit(0);
    }

    /**
     * Uncompresses compressed radar data based on the provided scaling information
     * @param data the data to uncompress
     * @param fieldScaling the scaling information to use
     * @return the uncompressed data
     */
    static float[][] decode (final byte[][] data, final FileFieldScalingInfo fieldScaling)
    {
        float[][] result = new float[data.length][];
        for (int i = 0; i < data.length; ++i) {
            result[i] = new float[data[i].length];
            for (int j = 0; j < data[i].length; ++j) {
                if (data[i][j] == 0) result[i][j] = Float.NaN;
                else result[i][j] = ((data[i][j] & 0xff) * fieldScaling.scale + fieldScaling.bias) / (float)fieldScaling.factor;
            }
        }
        return result;
    }
}
