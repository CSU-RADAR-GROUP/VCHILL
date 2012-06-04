package edu.colostate.vchill.file;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.HdrUtil;
import edu.colostate.vchill.KdpUtil;
import edu.colostate.vchill.NcpPlusUtil;
import edu.colostate.vchill.RainUtil;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.ProgressMonitorInputStream;
import edu.colostate.vchill.ProgressMonitorOutputStream;
import edu.colostate.vchill.ViewUtil;
import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.file.FileFunctions.Moment;
import edu.colostate.vchill.gui.GUIUtil;
import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 * An object for loading, altering, and then saving CHILL format
 * data files. This program is currently broken and needs to be fixed.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-09-01
 */
public class FileAlterer
{
    private static final ScaleManager sm = ScaleManager.getInstance();

    /** Sole constructor */
    private FileAlterer () {}

    /**
     * Loads a file into memory, adds additional fields to it, and saves it back to disk
     *
     * @param in the File to read from
     * @param out the File to write to
     * @param win the main window (used to add a progress bar) 
     */
    public static void alter (final File in, final File out, final JFrame win) throws Throwable
    {
        final JProgressBar progressBar = GUIUtil.addProgressBar(win);
        final DataInputStream input = new DataInputStream(new ProgressMonitorInputStream(new FileInputStream(in), progressBar));
        final DataOutputStream output = new DataOutputStream(new ProgressMonitorOutputStream(new FileOutputStream(out), progressBar));

        EventQueue.invokeLater(new Runnable() { public void run () {
            progressBar.setMaximum((int)in.length() * 2);
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
            sweeps = load(file, input);

            EventQueue.invokeLater(new Runnable() { public void run () {
                progressBar.setIndeterminate(true);
            }});

            System.out.println("        Adding NCP+");
            final int addedNCPp = addNCPPlus(sweeps);
            if (addedNCPp > 0 ) EventQueue.invokeLater(new Runnable() { public void run () {
                progressBar.setMaximum(progressBar.getMaximum() + addedNCPp); }});
            else System.out.println("            ....already present");

            System.out.println("        Adding HDR");
            final int addedHDR = addHDR(sweeps);
            if (addedHDR > 0 ) EventQueue.invokeLater(new Runnable() { public void run () {
                progressBar.setMaximum(progressBar.getMaximum() + addedHDR); }});
            else System.out.println("            ....already present");

            System.out.println("        Adding KDP");
            final int addedKDP = addKDP(sweeps);
            if (addedKDP > 0 ) EventQueue.invokeLater(new Runnable() { public void run () {
                progressBar.setMaximum(progressBar.getMaximum() + addedKDP); }});
            else System.out.println("            ....already present");

            System.out.println("        Adding RCOMP");
            final int addedRCOMP = addRCOMP(sweeps);
            if (addedRCOMP > 0 ) EventQueue.invokeLater(new Runnable() { public void run () {
                progressBar.setMaximum(progressBar.getMaximum() + addedRCOMP); }});
            else System.out.println("            ....already present");

            EventQueue.invokeLater(new Runnable() { public void run () {
                progressBar.setIndeterminate(false);
            }});

            System.out.println("    Writing to file " + out);
            save(file, output);
        ++sweepnum; }}

        input.close();
        output.flush();
        output.close();
        System.out.println("All done!");
    }

    /**
     * Loads a sweep from a CHILL format data file on disk into memory
     *
     * @param file LinkedList to add data to
     * @param input the DataInputStream (must be buffered) to read from
     * @return LinkedList of FileSweep objects referencing same data as <code>file</code>
     */
    public static LinkedList<FileSweep> load (final LinkedList<Object> file, final DataInputStream input) throws Throwable
    {
        LinkedList<FileSweep> sweeps = new LinkedList<FileSweep>();

        FileSKUHeader skuH = null;
        FileSweep currSweep = null;
        FileParameterData paramD = null;
        FileFieldScalingInfo[] fieldScalings = null;
        boolean sweepDone = false;

        while (input.available() > 0) { //each packet
            input.mark(FileSKUHeader.BYTE_SIZE);
            skuH = new FileSKUHeader();
            skuH.inputData(input);
            file.add(skuH);

            switch (skuH.id) {
                case ChillDefines.GATE_DATA_PACKET_CODE: //ray data
                    FileRay currRay = new FileRay();
                    FileDataHeader dataH = new FileDataHeader(skuH.length);
                    dataH.inputData(input);
                    file.add(currRay.dataH = dataH);
                    if (dataH.size_of_data == 0) break;

                    //actual data
                    byte[][] data = new byte[Moment.values().length][];
                    if (paramD.data_field_by_field == 1) { //contiguous data
                        { int m = 0; for (Moment moment : Moment.values()) { //each data type
                            if (((1l << m) & paramD.field_flag) != 0) { //present
                                data[m] = new byte[paramD.ngates * moment.BYTE_SIZE];
                                input.readFully(data[m]);
                            }
                        ++m; }}
                    } else { //interleaved data
                        { int m = 0; for (Moment moment : Moment.values()) {
                            if (((1l << m) & paramD.field_flag) != 0) { //present
                                data[m] = new byte[paramD.ngates * moment.BYTE_SIZE];
                            }
                        ++m; }}
                        for (int g = 0; g < currSweep.paramD.ngates; ++g) {
                            { int m = 0; for (Moment moment : Moment.values()) {
                                if (((1l << m) & paramD.field_flag) != 0) { //present
                                    for (int b = 0; b < moment.BYTE_SIZE; ++b) {
                                        data[m][moment.BYTE_SIZE * g + b] = input.readByte();
                                    }
                                }
                            ++m; }}
                        }
                    }
                    currRay.skuH = skuH;
                    currRay.data = data;
                    currSweep.data.add(currRay);
                    file.add(data);
                    break;
                case ChillDefines.GATE_PARAMS_PACKET_CODE: //parameters / new sweep
                    //limit to one sweep at a time to decrease memory use
                    if (sweepDone) { //we already have a sweep done
                        input.reset(); //rewind file
                        file.removeLast(); //remove extra SKU header
                        return sweeps;
                    } else sweepDone = true;

                    currSweep = new FileSweep();
                    currSweep.skuH = skuH;
                    sweeps.add(currSweep);
                    paramD = new FileParameterData();
                    paramD.inputData(input);
					sm.setAvailable( paramD.field_flag );
                    file.add(currSweep.paramD = paramD);

					//put the available moments in the scale manager
					for (Moment moment : Moment.values()) {
						if (((1l << moment.ordinal()) & paramD.field_flag) > 0 && moment.BYTE_SIZE == 1) {
							ChillMomentFieldScale scale = sm.getScale(moment.ordinal());
							if( scale == null )
							{
								ChillFieldInfo fieldInfo = FileFunctions.types[moment.ordinal()];
								scale = new ChillMomentFieldScale( fieldInfo, moment.ACCELERATOR, moment.UNITS, 100000, 1, 0 );
								sm.putScale( scale );
							}
						}
					}

                    //scaling info
                    fieldScalings = new FileFieldScalingInfo[Moment.values().length];
                    for (int i = 0; i < Moment.values().length; ++i) {
                        if ((paramD.field_flag & (1l << i)) == 0) continue;
                        fieldScalings[i] = new FileFieldScalingInfo();
                        fieldScalings[i].inputData(input);
                        ChillMomentFieldScale scale = sm.getScale(i);
                        if (scale != null) {
                            scale.factor = fieldScalings[i].factor;
                            scale.scale = fieldScalings[i].scale;
                            scale.bias = fieldScalings[i].bias;
                        }
                    }
                    file.add(currSweep.fieldScalings = fieldScalings);
                    break;
                default: throw new Error("Bad packet id code: " + skuH.id);
            }

            //trailing size marker
            int size = input.readInt();
            assert size == skuH.length : "\n  Bad length: was " + size + " instead of " + skuH.length;
        }
        return sweeps;
    }

    /**
     * Saves one or more sweeps from memory into a CHILL format data on disk
     *
     * @param file List of objects to write out 
     * @param output the DataOutput to write to
     */
    public static void save (final List<Object> file, final DataOutput output) throws Throwable
    {
        FileSKUHeader skuH = null;
        FileParameterData paramD = null;
        FileFieldScalingInfo[] fieldScalings = null;

        
        for (final Iterator iter = file.iterator(); iter.hasNext();) { //each packet
            skuH = (FileSKUHeader)iter.next();
            skuH.outputData(output);

            switch (skuH.id) {
                case ChillDefines.GATE_DATA_PACKET_CODE: //ray data
                    FileDataHeader dataH = (FileDataHeader)iter.next();
                    dataH.outputData(output);

                    //actual data
                    byte[][] data = (byte[][])iter.next();
                    if (paramD.data_field_by_field == 1) { //contiguous data
                        for (int m = 0; m < Moment.values().length; ++m) { //each data type
                            if (((1l << m) & paramD.field_flag) != 0) output.write(data[m]); //write if present
                        }
                    } else { //interleaved data
                        for (int g = 0; g < paramD.ngates; ++g) {
                            { int m = 0; for (Moment moment : Moment.values()) {
                                if (((1l << m) & paramD.field_flag) != 0) { //present
                                    for (int b = 0; b < moment.BYTE_SIZE; ++b) {
                                        output.write(data[m][moment.BYTE_SIZE * g + b]);
                                    }
                                }
                            ++m; }}
                        }
                    }
                    break;
                case ChillDefines.GATE_PARAMS_PACKET_CODE: //parameters / new sweep
                    paramD = (FileParameterData)iter.next();
                    paramD.outputData(output);

                    //scaling info
                    fieldScalings = (FileFieldScalingInfo[])iter.next();
                    for (int i = 0; i < Moment.values().length; ++i) {
                        if ((paramD.field_flag & (1l << i)) != 0) fieldScalings[i].outputData(output);
                    }
                    break;
                default: throw new Error("Bad packet id code: " + skuH.id);
            }

            //trailing size marker
            output.writeInt(skuH.length);
        }
    }

    /**
     * Adds the NCP+ field to one or more sweeps (if it is not already present)
     *
     * @param sweeps a Collection of FileSweep objects to process
     * @return the number of bytes added to the file 
     */
    public static int addNCPPlus (final Collection<FileSweep> sweeps) throws Throwable
    {
        int addedBytes = 0;
        double[] prevZDR = null;
        double[] currZDR = null;
        double[] nextZDR;

        for (final FileSweep currSweep : sweeps) { //each sweep
            if ((currSweep.paramD.field_flag & (1l << Moment.NCP_PLUS.ordinal())) != 0) continue; //NCP+ already present
            if ((currSweep.paramD.field_flag & (1l << Moment.ZDR.ordinal())) == 0) continue; //need Zdr to calculate NCP+

            currSweep.paramD.field_flag |= (1l << Moment.NCP_PLUS.ordinal());
            ++currSweep.paramD.nfields;
            ++currSweep.paramD.nfields_current;
            addedBytes += FileFieldScalingInfo.BYTE_SIZE;
            currSweep.skuH.length += FileFieldScalingInfo.BYTE_SIZE;
            currSweep.paramD.sweep_bytes += FileFieldScalingInfo.BYTE_SIZE;
            currSweep.fieldScalings[Moment.NCP_PLUS.ordinal()] = currSweep.fieldScalings[Moment.NCP.ordinal()];

            for (final ListIterator rayIter = currSweep.data.listIterator(); rayIter.hasNext();) { //each ray
                FileRay curr = (FileRay)rayIter.next();
                addedBytes += (Moment.NCP_PLUS.BYTE_SIZE * currSweep.paramD.ngates);
                currSweep.paramD.sweep_bytes += (Moment.NCP_PLUS.BYTE_SIZE * currSweep.paramD.ngates);
                curr.skuH.length += (Moment.NCP_PLUS.BYTE_SIZE * currSweep.paramD.ngates);

                if (rayIter.hasNext()) {
                    FileRay next = (FileRay)rayIter.next();
                    nextZDR = ViewUtil.getValues(next.data[Moment.ZDR.ordinal()], Moment.ZDR.CODE);
                    rayIter.previous(); //back up so curr is correct next time
                } else {
                    nextZDR = null;
                }

                //process here
                double[] ncp = ViewUtil.getValues(curr.data[Moment.NCP.ordinal()], Moment.NCP.CODE);
                if (currZDR == null) currZDR = ViewUtil.getValues(curr.data[Moment.ZDR.ordinal()], Moment.ZDR.CODE);

                curr.data[Moment.NCP_PLUS.ordinal()] = ViewUtil.getBytes(NcpPlusUtil.calculateNCP_PLUS(ncp, prevZDR, currZDR, nextZDR), Moment.NCP_PLUS.CODE);

                prevZDR = currZDR; //for next iteration
                currZDR = nextZDR;
            }
        }
        return addedBytes;
    }

    /**
     * Adds the HDR field to one or more sweeps (if it is not already present)
     *
     * @param sweeps a Collection of FileSweep objects to process
     * @return the number of bytes added to the file 
     */
    public static int addHDR (final Collection<FileSweep> sweeps) throws Throwable
    {
        int addedBytes = 0;
        for (final FileSweep currSweep : sweeps) { //each sweep
            if ((currSweep.paramD.field_flag & (1l << Moment.HDR.ordinal())) != 0) continue; //already present
            if ((currSweep.paramD.field_flag & (1l << Moment.Z.ordinal())) == 0) continue; //needed to calculate
            if ((currSweep.paramD.field_flag & (1l << Moment.ZDR.ordinal())) == 0) continue; //needed to calculate

            currSweep.paramD.field_flag |= (1l << Moment.HDR.ordinal());
            ++currSweep.paramD.nfields;
            ++currSweep.paramD.nfields_current;
            addedBytes += FileFieldScalingInfo.BYTE_SIZE;
            currSweep.skuH.length += FileFieldScalingInfo.BYTE_SIZE;
            currSweep.paramD.sweep_bytes += FileFieldScalingInfo.BYTE_SIZE;
            currSweep.fieldScalings[Moment.HDR.ordinal()] = new FileFieldScalingInfo();
            currSweep.fieldScalings[Moment.HDR.ordinal()].factor = currSweep.fieldScalings[Moment.Z.ordinal()].factor;
            currSweep.fieldScalings[Moment.HDR.ordinal()].scale = (int)(ChillDefines.HDR_FACTOR * currSweep.fieldScalings[Moment.HDR.ordinal()].factor);
            currSweep.fieldScalings[Moment.HDR.ordinal()].bias = (int)(ChillDefines.HDR_OFFSET * currSweep.fieldScalings[Moment.HDR.ordinal()].factor);
            
            for (final FileRay curr : currSweep.data) { //each ray
                addedBytes += (Moment.NCP_PLUS.BYTE_SIZE * currSweep.paramD.ngates);
                currSweep.paramD.sweep_bytes += (Moment.NCP_PLUS.BYTE_SIZE * currSweep.paramD.ngates);
                curr.skuH.length += (Moment.NCP_PLUS.BYTE_SIZE * currSweep.paramD.ngates);

                //process here
                double[] z = ViewUtil.getValues(curr.data[Moment.Z.ordinal()], Moment.Z.CODE);
                double[] zdr = ViewUtil.getValues(curr.data[Moment.ZDR.ordinal()], Moment.ZDR.CODE);
                curr.data[Moment.HDR.ordinal()] = ViewUtil.getBytes(HdrUtil.calculateHDR(z, zdr), Moment.HDR.CODE);
            }
        }
        return addedBytes;
    }

    /**
     * Adds the KDP field to one or more sweeps (if it is not already present)
     *
     * @param sweeps a Collection of FileSweep objects to process
     * @return the number of bytes added to the file 
     */
    public static int addKDP (final Collection<FileSweep> sweeps) throws Throwable
    {
        int addedBytes = 0;
        for (final FileSweep currSweep : sweeps) { //each sweep
            if ((currSweep.paramD.field_flag & (1l << Moment.KDP.ordinal())) != 0) continue; //already present
            if ((currSweep.paramD.field_flag & (1l << Moment.Z.ordinal())) == 0) continue; //needed to calculate
            if ((currSweep.paramD.field_flag & (1l << Moment.PHIDP.ordinal())) == 0) continue; //needed to calculate
            if ((currSweep.paramD.field_flag & (1l << Moment.RHOHV.ordinal())) == 0) continue; //needed to calculate

            currSweep.paramD.field_flag |= (1l << Moment.KDP.ordinal());
            ++currSweep.paramD.nfields;
            ++currSweep.paramD.nfields_current;
            addedBytes += FileFieldScalingInfo.BYTE_SIZE;
            currSweep.skuH.length += FileFieldScalingInfo.BYTE_SIZE;
            currSweep.paramD.sweep_bytes += FileFieldScalingInfo.BYTE_SIZE;
            currSweep.fieldScalings[Moment.KDP.ordinal()] = new FileFieldScalingInfo();
            currSweep.fieldScalings[Moment.KDP.ordinal()].factor = 255;
            currSweep.fieldScalings[Moment.KDP.ordinal()].scale = 20;
            currSweep.fieldScalings[Moment.KDP.ordinal()].bias = -64 * currSweep.fieldScalings[Moment.KDP.ordinal()].scale;
            
            for (final FileRay curr : currSweep.data) { //each ray
                addedBytes += (Moment.KDP.BYTE_SIZE * currSweep.paramD.ngates);
                currSweep.paramD.sweep_bytes += (Moment.KDP.BYTE_SIZE * currSweep.paramD.ngates);
                curr.skuH.length += (Moment.KDP.BYTE_SIZE * currSweep.paramD.ngates);

                //process here
                double[] phi = ViewUtil.getValues(curr.data[Moment.PHIDP.ordinal()], Moment.PHIDP.CODE);
                double[] dbz = ViewUtil.getValues(curr.data[Moment.Z.ordinal()], Moment.Z.CODE);
                double[] rho = ViewUtil.getValues(curr.data[Moment.RHOHV.ordinal()], Moment.RHOHV.CODE);
                curr.data[Moment.KDP.ordinal()] = ViewUtil.getBytes(KdpUtil.calculateKDP(phi, dbz, rho,
                        currSweep.paramD.start_range * 1e-6, currSweep.paramD.gate_spacing * 1e-6), Moment.KDP.CODE);
            }
        }
        return addedBytes;
    }

    /**
     * Adds the RCOMP field to one or more sweeps (if it is not already present)
     *
     * @param sweeps a Collection of FileSweep objects to process
     * @return the number of bytes added to the file 
     */
    public static int addRCOMP (final Collection<FileSweep> sweeps) throws Throwable
    {
        int addedBytes = 0;
        for (final FileSweep currSweep : sweeps) { //each sweep
            if ((currSweep.paramD.field_flag & (1l << Moment.RCOMP.ordinal())) != 0) continue; //already present
            if ((currSweep.paramD.field_flag & (1l << Moment.KDP.ordinal())) == 0) continue; //needed to calculate
            if ((currSweep.paramD.field_flag & (1l << Moment.Z.ordinal())) == 0) continue; //needed to calculate
            if ((currSweep.paramD.field_flag & (1l << Moment.ZDR.ordinal())) == 0) continue; //needed to calculate

            currSweep.paramD.field_flag |= (1l << Moment.RCOMP.ordinal());
            ++currSweep.paramD.nfields;
            ++currSweep.paramD.nfields_current;
            addedBytes += FileFieldScalingInfo.BYTE_SIZE;
            currSweep.skuH.length += FileFieldScalingInfo.BYTE_SIZE;
            currSweep.paramD.sweep_bytes += FileFieldScalingInfo.BYTE_SIZE;
            currSweep.fieldScalings[Moment.RCOMP.ordinal()] = new FileFieldScalingInfo();
            currSweep.fieldScalings[Moment.RCOMP.ordinal()].factor = 1;
            currSweep.fieldScalings[Moment.RCOMP.ordinal()].scale = 1;
            currSweep.fieldScalings[Moment.RCOMP.ordinal()].bias = 0;

            for (final FileRay curr : currSweep.data) { //each ray
                addedBytes += (Moment.RCOMP.BYTE_SIZE * currSweep.paramD.ngates);
                currSweep.paramD.sweep_bytes += (Moment.RCOMP.BYTE_SIZE * currSweep.paramD.ngates);
                curr.skuH.length += (Moment.RCOMP.BYTE_SIZE * currSweep.paramD.ngates);

                //process here
                double[] kdp = ViewUtil.getValues(curr.data[Moment.KDP.ordinal()], Moment.KDP.CODE);
                double[] dbz = ViewUtil.getValues(curr.data[Moment.Z.ordinal()], Moment.Z.CODE);
                double[] zdr = ViewUtil.getValues(curr.data[Moment.ZDR.ordinal()], Moment.ZDR.CODE);

                byte[] rain = ViewUtil.getBytes(RainUtil.calculateCompositeRain(kdp, dbz, zdr), Moment.RCOMP.CODE);
                curr.data[Moment.RCOMP.ordinal()] = rain;
            }
        }
        return addedBytes;
    }

    public static void main (final String[] args) throws Throwable
    {
        File in, out;
        JFrame top = GUIUtil.startGUI("Java VCHILL Data File Modifier");
        System.out.println("This program will add the fields NCP+, KDP, HDR, and RCOMP to a CHILL format data file.");

        if (args.length < 2) {
            //get input file
            System.out.println("Please choose a file to add the calculated fields to.");
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(top);
            if (returnVal != JFileChooser.APPROVE_OPTION) System.exit(1);
            in = chooser.getSelectedFile();

            //get output file
            System.out.println("Please choose a file to save as.  Any contents will be overwritten.  Do *not* select the same file for input and output.");
            chooser = new JFileChooser();
            returnVal = chooser.showSaveDialog(top);
            if (returnVal != JFileChooser.APPROVE_OPTION) System.exit(1);
            out = chooser.getSelectedFile();
        } else {
            in  = new File(args[0]);
            out = new File(args[1]);
        }
        Thread.sleep(250);
        if (in.equals(out)) throw new IllegalArgumentException("Cannot read from and write to the same file");
        alter(in, out, top);
        if (args.length == 2) System.exit(0);
    }
}
