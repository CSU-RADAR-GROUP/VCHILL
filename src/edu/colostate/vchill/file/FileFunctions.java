package edu.colostate.vchill.file;

import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.colostate.vchill.iris.SigmetProductRaw;
import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.chill.ChillDataHeader;
import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.chill.ChillGenRay;
import edu.colostate.vchill.chill.ChillHSKHeader;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.iris.IrisRawFile;
import edu.colostate.vchill.netcdf.CASANetCDFFile;
import edu.colostate.vchill.netcdf.NCARNetCDFFile;
import edu.colostate.vchill.netcdf.WCRNetCDFFile;

/**
 * Class that has functions that return directory names and file names
 * 
 * @author Brian Eriksson
 * @author Jochen Deyke
 * @author jpont
 * @created June 14, 2003
 * @version 2010-09-02
 */
public final class FileFunctions {
	private static final ScaleManager sm = ScaleManager.getInstance();
	private static final NumberFormat nf = new DecimalFormat("00");

	/** Information about various possible fields. */
	public enum Moment {
		R0HH(2, "R0HH", null, null, -1, null), R0VV(2, "R0VV", null, null, -1,
				null), R0HV(2, "R0HV", null, null, -1, null), R0VH(2, "R0VH",
				null, null, -1, null), R0HVc(4, "R0HVc", null, null, -1, null), R0VHc(
				4, "R0VHc", null, null, -1, null), R1HV(4, "R1HV", null, null,
				-1, null), R1VH(4, "R1VH", null, null, -1, null), R1VV(4,
				"R1VV", null, null, -1, null), R1HH(4, "R1HH", null, null, -1,
				null), R2HH(4, "R2HH", null, null, -1, null), R2VV(4, "R2VV",
				null, null, -1, null), Z(1, "Z", "Reflectivity", "dBz",
				KeyEvent.VK_Z, "Reflectivity"), V(1, "V", "Velocity", "m/s",
				KeyEvent.VK_V, "Velocity"), W(1, "W", "Spec. width", "m/s",
				KeyEvent.VK_W, "SpectralWidth"), NCP(1, "NCP",
				"Normalized Coherent Power", null, KeyEvent.VK_N,
				"NormalizedCoherentPower"), // fix netcdf
		ZDR(1, "ZDR", "Differential Reflectivity", "dB", KeyEvent.VK_D,
				"DifferentialReflectivity"), LDRH(1, "LDRH",
				"Linear Depolarization Ratio H", "db", KeyEvent.VK_H,
				"LinearDepolRatioH"), // fix netcdf
		LDRV(1, "LDRV", "Linear Depolarization Ratio V", "db", KeyEvent.VK_L,
				"LinearDepolRatioV"), // fix netcdf
		PHIDP(1, "PhiDP", "Differential Phase", "deg", KeyEvent.VK_P,
				"DifferentialPhase"), // "\u03a6DP"
		RHOHV(1, "RhoHV", "Correlation Coefficient", null, KeyEvent.VK_R,
				"CrossPolCorrelation"), TIME_SERIES(8, "T-Ser", null, null, -1,
				null), KDP(1, "KDP", "Specific Differential Phase", "deg/km",
				KeyEvent.VK_K, "SpecificPhase"), NCP_PLUS(1, "NCPp",
				"NCP + s.deviation of Zdr filter", null, KeyEvent.VK_U,
				"EnhancedNormalizedCoherentPower"), // fix netcdf
		HDR(1, "HDR", "Aydin Hail", "dB", KeyEvent.VK_H, "AydinHail"), // fix
																		// netcdf
		RCOMP(1, "Rcomp", "Rain rate", "mm/h", KeyEvent.VK_C,
				"CompositeRainRate"); // fix netcdf

		/** number of bytes per gate */
		public final int BYTE_SIZE;

		/** unique identifier; used on menus etc */
		public final String CODE;

		/** descriptive String */
		public final String DESCRIPTION;

		/**
		 * units (will be added to description in parentheses if non-
		 * <code>null</code>)
		 */
		public final String UNITS;

		/** KeyEvent constant to be used in creating shortcuts */
		public final int ACCELERATOR;

		/** Variable name when field is stored as NetCDF */
		public final String NETCDF;

		Moment(final int byteSize, final String code, final String description,
				final String units, final int accelerator, final String netcdf) {
			this.BYTE_SIZE = byteSize;
			this.CODE = code;
			this.UNITS = units;
			this.DESCRIPTION = description
					+ (units == null ? "" : " (" + units + ")");
			this.ACCELERATOR = accelerator;
			this.NETCDF = netcdf;
		}

		@Override
		public String toString() {
			return this.CODE;
		}

		public static Moment translate(final String type) {
			for (Moment m : Moment.values()) {
				if (m.CODE.equalsIgnoreCase(type))
					return m;
			}
			// throw new IllegalArgumentException(type +
			// " is not a recognized moment code");
			return null;
		}
	}

	public static final ChillFieldInfo R0HH = new ChillFieldInfo(
			Moment.R0HH.CODE, Moment.R0HH.DESCRIPTION, Moment.R0HH.ordinal(),
			12800000, -12800000, 0, 0);
	public static final ChillFieldInfo R0VV = new ChillFieldInfo(
			Moment.R0VV.CODE, Moment.R0VV.DESCRIPTION, Moment.R0VV.ordinal(),
			12800000, -12800000, 0, 0);
	public static final ChillFieldInfo R0HV = new ChillFieldInfo(
			Moment.R0HV.CODE, Moment.R0HV.DESCRIPTION, Moment.R0HV.ordinal(),
			12800000, -12800000, 0, 0);
	public static final ChillFieldInfo R0VH = new ChillFieldInfo(
			Moment.R0VH.CODE, Moment.R0VH.DESCRIPTION, Moment.R0VH.ordinal(),
			12800000, -12800000, 0, 0);
	public static final ChillFieldInfo R0HVc = new ChillFieldInfo(
			Moment.R0HVc.CODE, Moment.R0HVc.DESCRIPTION,
			Moment.R0HVc.ordinal(), 12800000, -12800000, 0, 0);
	public static final ChillFieldInfo R0VHc = new ChillFieldInfo(
			Moment.R0VHc.CODE, Moment.R0VHc.DESCRIPTION,
			Moment.R0VHc.ordinal(), 12800000, -12800000, 0, 0);
	public static final ChillFieldInfo R1HV = new ChillFieldInfo(
			Moment.R1HV.CODE, Moment.R1HV.DESCRIPTION, Moment.R1HV.ordinal(),
			12800000, -12800000, 0, 0);
	public static final ChillFieldInfo R1VH = new ChillFieldInfo(
			Moment.R1VH.CODE, Moment.R1VH.DESCRIPTION, Moment.R1VH.ordinal(),
			12800000, -12800000, 0, 0);
	public static final ChillFieldInfo R1VV = new ChillFieldInfo(
			Moment.R1VV.CODE, Moment.R1VV.DESCRIPTION, Moment.R1VV.ordinal(),
			12800000, -12800000, 0, 0);
	public static final ChillFieldInfo R1HH = new ChillFieldInfo(
			Moment.R1HH.CODE, Moment.R1HH.DESCRIPTION, Moment.R1HH.ordinal(),
			12800000, -12800000, 0, 0);
	public static final ChillFieldInfo R2HH = new ChillFieldInfo(
			Moment.R2HH.CODE, Moment.R2HH.DESCRIPTION, Moment.R2HH.ordinal(),
			12800000, -12800000, 0, 0);
	public static final ChillFieldInfo R2VV = new ChillFieldInfo(
			Moment.R2VV.CODE, Moment.R2VV.DESCRIPTION, Moment.R2VV.ordinal(),
			12800000, -12800000, 0, 0);
	public static final ChillFieldInfo Z = new ChillFieldInfo(Moment.Z.CODE,
			Moment.Z.DESCRIPTION, 12, 9600000, -3200000, 0, 0);
	public static final ChillFieldInfo V = new ChillFieldInfo(Moment.V.CODE,
			Moment.V.DESCRIPTION, 13, 5500000, -5500000, 16, 1);
	public static final ChillFieldInfo W = new ChillFieldInfo(Moment.W.CODE,
			Moment.W.DESCRIPTION, 14, 5500000, -5500000, 0, 2);
	public static final ChillFieldInfo NCP = new ChillFieldInfo(
			Moment.NCP.CODE, Moment.NCP.DESCRIPTION, 15, 1000000, 0, 0, 3);
	public static final ChillFieldInfo ZDR = new ChillFieldInfo(
			Moment.ZDR.CODE, Moment.ZDR.DESCRIPTION, 16, 9030899, -3010299, 0,
			4);
	public static final ChillFieldInfo LDRH = new ChillFieldInfo(
			Moment.LDRH.CODE, Moment.LDRH.DESCRIPTION, 17, 0, -480000, 0, 5);
	public static final ChillFieldInfo LDRV = new ChillFieldInfo(
			Moment.LDRV.CODE, Moment.LDRV.DESCRIPTION, 18, 0, -480000, 0, 5);
	public static final ChillFieldInfo PHIDP = new ChillFieldInfo(
			Moment.PHIDP.CODE, Moment.PHIDP.DESCRIPTION, 19, 18000000,
			-18000000, 0, 6);
	public static final ChillFieldInfo RHOHV = new ChillFieldInfo(
			Moment.RHOHV.CODE, Moment.RHOHV.DESCRIPTION, 20, 1000000, 300000,
			0, 7);
	public static final ChillFieldInfo TIME_SERIES = new ChillFieldInfo(
			Moment.TIME_SERIES.CODE, Moment.TIME_SERIES.DESCRIPTION,
			Moment.TIME_SERIES.ordinal(), 12800000, -12800000, 0, 0);
	public static final ChillFieldInfo KDP = new ChillFieldInfo(
			Moment.KDP.CODE, Moment.KDP.DESCRIPTION, Moment.KDP.ordinal(),
			12800000, -12800000, 0, 0);
	public static final ChillFieldInfo NCP_PLUS = new ChillFieldInfo(
			Moment.NCP_PLUS.CODE, Moment.NCP_PLUS.DESCRIPTION,
			Moment.NCP_PLUS.ordinal(), 12800000, -12800000, 0, 0);
	public static final ChillFieldInfo HDR = new ChillFieldInfo(
			Moment.HDR.CODE, Moment.HDR.DESCRIPTION, Moment.HDR.ordinal(),
			12800000, -12800000, 0, 0);
	public static final ChillFieldInfo RCOMP = new ChillFieldInfo(
			Moment.RCOMP.CODE, Moment.RCOMP.DESCRIPTION,
			Moment.RCOMP.ordinal(), 12800000, -12800000, 0, 0);
	public static final ChillFieldInfo[] types = new ChillFieldInfo[] { R0HH,
			R0VV, R0HV, R0VH, R0HVc, R0VHc, R1HV, R1VH, R1VV, R1HH, R2HH, R2VV,
			Z, V, W, NCP, ZDR, LDRH, LDRV, PHIDP, RHOHV, TIME_SERIES, KDP,
			NCP_PLUS, HDR, RCOMP };

	/** private default constructor prevents instantiation */
	private FileFunctions() {
	}

	/**
	 * Returns a list of sweeps in a file
	 * 
	 * @param dir
	 *            String describing the directory to look under
	 * @param file
	 *            String with the name of the file to open
	 * @return ArrayList of Strings of sweeps in <code>file</code>
	 */
	public static Collection<String> getSweepList(final String dir,
			final String file) {
		ArrayList<String> sweepList = new ArrayList<String>();
		String name = stripFileName(file); // trim off scan type

		if (isNetCDF(name)) {
			sweepList.add("Sweep 1");
			return sweepList;
		}
		if (isIRISRAW(name)){
		  SigmetProductRaw SPR_input=new SigmetProductRaw(stripFileName(dir)+"/" + name);
		  for(int i = 0; i<SPR_input.getSweeps();i++){
		  sweepList.add("Sweep "+i);
		  }
		  return sweepList;
		}

		DataInputStream fin = null;
		String newFileName = stripFileName(dir) + "/" + name;

		try {
			fin = new DataInputStream(new FileInputStream(newFileName)); // UNbuffered
																			// for
																			// performance
		} catch (IOException ioe) {
			System.err
					.println("FileFunctions: IOException in getSweepList: File not found?");
			ioe.printStackTrace();
			return sweepList;
		} // end catch

		FileParameterData parameterData = new FileParameterData();
		FileSKUHeader skuHeader = new FileSKUHeader();

		int count = 0;
		boolean readOK = skuHeader.inputData(fin);
		if (!readOK)
			return sweepList; // not a valid file -> empty list

		do { // ASSUMPTION : The first SKU header will always be a sweep header.
			int toSkip = 0;
			try {
				readOK = parameterData.inputData(fin);
				toSkip = parameterData.sweep_bytes - parameterData.byteSize(); // numbytes
																				// to
																				// next
																				// parameterheader
				skipBytes(fin, toSkip); // Skip to the next parameterheader
				if (readOK)
					sweepList.add("Sweep " + nf.format(++count));
			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.out.println("Tried to skip " + toSkip + " bytes");
			} // end catch
		} while (readOK); // end while
		return sweepList;
	} // end getSweepList

	/**
	 * List valid files and subirectoried given a directory name
	 * 
	 * @param dir
	 *            the directory to list
	 * @return Collection containing Strings for each subdirectory and valid
	 *         file
	 */
	public static Collection<String> getDirectory(final String dir) {
		List<String> names = new ArrayList<String>();
		File[] files = new File(stripFileName(dir)).listFiles();

		for (File file : files) {
			String name = getDecoratedName(file);
			if (name != null)
				names.add(name);
		}

		Collections.sort(names);
		return names;
	}

	protected static String getDecoratedName(final File file) {
		if (file.isDirectory()) {
			try {
				return URLEncoder.encode(file.getAbsolutePath(), "UTF-8")
						+ " DIR";
			} catch (UnsupportedEncodingException uee) {
				return file.getAbsolutePath() + " DIR";
			}
		} else if (file.isFile()) {
			String name = file.getName();
			if (isCHILL(name)) {
				FileParameterData parameterData = new FileParameterData();
				FileSKUHeader skuHeader = new FileSKUHeader();
				try {
					DataInputStream fin = new DataInputStream(
							new BufferedInputStream(new FileInputStream(file)));
					if (!skuHeader.inputData(fin))
						return null; // failed to load sku header
					if (!parameterData.inputData(fin))
						return null; // failed to load parameter data
					fin.close();
				} catch (IOException IO) {
					System.err.println("Error opening "
							+ file.getAbsolutePath());
					return null;
				}

				try {
					return URLEncoder.encode(name, "UTF-8") + " "
							+ parameterData.getScanMode();
				} catch (UnsupportedEncodingException uee) {
					return name + " " + parameterData.getScanMode();
				}
			} else if (isNetCDF(name)) {
				try {
					return URLEncoder.encode(name, "UTF-8") + " NetCDF";
				} catch (UnsupportedEncodingException uee) {
					return name + " NetCDF";
				}
			} else if (isIRISRAW(name)) {
				try {
					return URLEncoder.encode(name, "UTF-8") + " IRISRaw";
				} catch (UnsupportedEncodingException uee) {
					return name + "IRISRaw";
				}
			}
		}

		return null; // not a valid file
	}

	public static ChillHSKHeader getChillHSKHeader(
			final FileParameterData paramD) {
		ChillHSKHeader hskH = new ChillHSKHeader();
		hskH.radarId = paramD.getRadarName();
		hskH.radarLatitude = paramD.latitude;
		hskH.radarLongitude = paramD.longitude;
		hskH.radarAltitudeMsl = paramD.altitude * 1000;
		hskH.antMode = paramD.chill_scan_mode;
		hskH.nyquistVel = (int) (paramD.wavelength * 1e6 / 2 * paramD.prf);
		hskH.gateWidth = paramD.gate_spacing;
		hskH.pulses = paramD.samples_per_beam;
		hskH.polarizationMode = paramD.processor_mode;
		hskH.tiltNum = paramD.tilt_num;
		// saveTilt not set
		hskH.angleScale = 0x7fffffff;
		hskH.sweepStartTime = paramD.volume_start_time;
		return hskH;
	}

	public static ChillDataHeader getChillDataHeader(
			final FileParameterData paramD, final FileDataHeader fDataH,
			final ChillHSKHeader cHskH) {
		int angleScale = (cHskH != null) ? cHskH.angleScale : 0x7fffffff;
		ChillDataHeader cDataH = new ChillDataHeader();
		cDataH.startAz = cDataH.endAz = (int) ((fDataH.azimuth * 1e-6) / 360 * angleScale);
		cDataH.startEl = cDataH.endEl = (int) ((fDataH.elevation * 1e-6) / 360 * angleScale);
		cDataH.numGates = paramD.ngates;
		cDataH.startRange = paramD.start_range;
		cDataH.dataTime = fDataH.time & 0xffffffff;
		// fractionalSecs not set;
		return cDataH;
	}

	public static Collection<String> getAvailableMoments(
			final FileParameterData paramD) {
		ArrayList<String> list = new ArrayList<String>();
		for (Moment moment : Moment.values()) {
			if (((1l << moment.ordinal()) & paramD.field_flag) > 0
					&& moment.BYTE_SIZE == 1) {
				ChillMomentFieldScale scale = sm.getScale(moment.ordinal());
				if (scale == null) {
					ChillFieldInfo fieldInfo = types[moment.ordinal()];
					scale = new ChillMomentFieldScale(fieldInfo,
							moment.ACCELERATOR, moment.UNITS, 100000, 1, 0);
					sm.putScale(scale);
				}

				list.add(scale.fieldName);
			}
		}
		return list;
	}

	/**
	 * Loads a sweep (all data types) into the cache
	 */
	public static void load(FileConnection fileConn, ControlMessage command,
			final CacheMain cache) throws IOException {
		if (cache.getCompleteFlag(command, ChillDefines.META_TYPE)) {
			fileConn.setIsSweepDone(true);
			return;
		}
		String path = stripFileName(command.getDir()) + "/"
				+ stripFileName(command.getFile());
		sm.clear(); // clear the list of fields because the file will provide a
					// new list
    if (isNetCDF(path)) {
      try {
        if (command.getFile().startsWith("WCR")) {
          System.out.println("loading WCR netcdf");
          WCRNetCDFFile.load(command, cache);
        } else if (command.getFile().startsWith("ncswp_")) {
          System.out.println("loading NCAR netcdf");
          NCARNetCDFFile.load(command, cache);
        } else {
          System.out.println("loading CASA netcdf");
          CASANetCDFFile.load(command, cache);
        }
      } finally {
        fileConn.setIsSweepDone(true);
      }
      return;
    } else if (isIRISRAW(path)) {
      try {
        System.out.println("Loading Iris Raw data.");
        IrisRawFile.load(command, cache);
      } catch (Exception e) {
        System.err.println("Exception :" + e);
      } finally {
        fileConn.setIsSweepDone(true);
      }
      return;
    }

		System.out.println("loading CHILL data");

		File file = new File(path);
		DataInputStream input = new DataInputStream(new BufferedInputStream(
				new FileInputStream(file), 1024));
		// DataInputStream input = new DataInputStream(new
		// BufferedInputStream(new FileInputStream(file)));
		// DataInputStream input = new DataInputStream(new
		// FileInputStream(file));
		int sweep = Integer.parseInt(command.getSweep().split(" ")[1]);

		Collection<String> availableMoments = null;

		FileSKUHeader skuH = null;
		FileParameterData paramD = null;
		FileFieldScalingInfo[] fieldScalings = null;

		ChillHSKHeader hskH=null ;
		ChillDataHeader cDataH=null;

		int currSweepNum = 0;

		loop: while (input.available() > 0) { // each packet
			input.mark(FileSKUHeader.BYTE_SIZE);
			skuH = new FileSKUHeader();
			skuH.inputData(input);

			switch (skuH.id) {
			case ChillDefines.GATE_DATA_PACKET_CODE: // ray data
				FileDataHeader fDataH = new FileDataHeader(skuH.length);
				fDataH.inputData(input);
				cDataH = getChillDataHeader(paramD, fDataH, hskH);
				cache.addRay(command, ChillDefines.META_TYPE, cDataH);
				if (fDataH.size_of_data == 0)
					break;
				if (currSweepNum != sweep) {
					skipBytes(input, fDataH.size_of_data);
					break;
				}

        // actual data
        if (paramD.data_field_by_field == 1) { // contiguous data
          {
            int m = 0;
            for (Moment moment : Moment.values()) { // each data
              // type
              if (((1l << m) & paramD.field_flag) != 0) { // present
                if (moment.BYTE_SIZE == 1) {
                  byte[] data = new byte[paramD.ngates];
                  input.readFully(data);
                  cache.addRay(command, sm.getScale(moment.ordinal()).fieldName,
                    new ChillGenRay(hskH, cDataH, sm.getScale(moment.ordinal()).fieldName, data));
                  Thread.yield();
                } else {
                  skipBytes(input, paramD.ngates * moment.BYTE_SIZE);
                }
              }
              ++m;
            }
          }
				} else { // interleaved data
					byte[][] data = new byte[availableMoments.size()][paramD.ngates];
					for (int g = 0; g < paramD.ngates; ++g) {
						{
							int m = 0;
							for (Moment moment : Moment.values()) {
								if (((1l << moment.ordinal()) & paramD.field_flag) != 0) { // present
									if (moment.BYTE_SIZE == 1) {
										data[m++][g] = input.readByte();
									} else {
										skipBytes(input, moment.BYTE_SIZE);
									}
								}
							}
						}
					}
					{
						int m = 0;
						for (String moment : availableMoments) {
							cache.addRay(command, moment, new ChillGenRay(hskH,
									cDataH, moment, data[m]));
							++m;
						}
					}
					Thread.yield();
				}
				break;
			case ChillDefines.GATE_PARAMS_PACKET_CODE: // parameters / new sweep
				++currSweepNum;
				if (currSweepNum > sweep)
					break loop;

				paramD = new FileParameterData();
				paramD.inputData(input);
				sm.setAvailable(paramD.field_flag);
				hskH = getChillHSKHeader(paramD);
				cache.addRay(command, ChillDefines.META_TYPE, hskH);
				availableMoments = FileFunctions.getAvailableMoments(paramD);

				// scaling info
				fieldScalings = new FileFieldScalingInfo[Moment.values().length];
				for (int i = 0; i < Moment.values().length; ++i) {
					if ((paramD.field_flag & (1l << i)) == 0)
						continue;
					fieldScalings[i] = new FileFieldScalingInfo();
					fieldScalings[i].inputData(input);
					ChillMomentFieldScale scale = sm.getScale(i);
					if (scale != null) {
						scale.factor = fieldScalings[i].factor;
						scale.scale = fieldScalings[i].scale;
						scale.bias = fieldScalings[i].bias;
					}
				}
				break;
			default:
				fileConn.setIsSweepDone(true);
				throw new Error("Bad packet id code: " + skuH.id);
			}

			// trailing size marker
			int size = input.readInt();
			assert size == skuH.length : "\n  Bad length: was " + size
					+ " instead of " + skuH.length;
		} // end loop label while

		input.close();
		for (String moment : availableMoments) {
			cache.setCompleteFlag(command, moment);
		}
		cache.setCompleteFlag(command, ChillDefines.META_TYPE);
		fileConn.setIsSweepDone(true);
	}

	public static boolean isIRISRAW(String path) {
		int k = path.indexOf(".RAW");
		if (k == -1) {
			return false;
		} else {
            int j = path.indexOf(".uf");
            if(j ==-1){
			    System.out.println("This is an iris file");
			    return true;
            }else {
                return false;
            }
		}

	}

	public static void skipBytes(final DataInputStream input, long toSkip)
			throws IOException {
		while (toSkip > 0) {
			long skipped = input.skip(toSkip); // skip
			if (skipped < 1)
				throw new IOException("Skipped " + skipped
						+ " bytes instead of " + toSkip);
			toSkip -= skipped;
		}
	}

	public static String stripFileName(final String name) {
		try {
			return URLDecoder.decode(name.substring(0, name.lastIndexOf(" ")),
					"UTF-8");
		} catch (UnsupportedEncodingException uee) {
			return name.substring(0, name.lastIndexOf(" "));
		}
	}

	public static boolean isCHILL(final String name) {
		if (name.startsWith("CHL")) { // If CHILL file
			if (name.endsWith(".cdet") || name.endsWith(".cf")) {
				return false; // not data file
			} else {
				return true;
			}
		}

		return false;
	}

	public static boolean isNetCDF(String name) {
		if (name.endsWith(".gz"))
			name = name.substring(0, name.length() - 3); // strip compressedness
		if (name.endsWith(".netcdf"))
			return true;
		if (name.endsWith(".nc"))
			return true;
		return false;
	}
}
