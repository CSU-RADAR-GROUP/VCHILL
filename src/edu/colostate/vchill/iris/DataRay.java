/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Class that contains actual ray data
 * 
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class DataRay {

	private double angle;
	private RayHeader rayheader;
	private String DataType;
	private double[] data;
	private int number_of_bins;
	private byte[] bytearray;
	private int dtype;

	public DataRay() {
	}

	public void setByteArray(byte[] inbyte) {
		this.bytearray = inbyte;
	}

	public void setRangeBins(int numbins) {
		number_of_bins = numbins;
		data = new double[numbins];
	}

	/**
	 * @return the rayheader
	 */
	public RayHeader getRayheader() {
		return rayheader;
	}

	/**
	 * @param rayheader
	 *            the rayheader to set
	 */
	public void setRayheader(RayHeader rayheader) {
		this.rayheader = rayheader;
	}

	/**
	 * @return the DataType
	 */
	public String getDataType() {
		return DataType;
	}

	/**
	 * @param DataType
	 *            the DataType to set
	 */
	public void setDataType(String DataType) {
		this.DataType = DataType;
	}

	/**
	 * @return the data
	 */
	public double[] getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(double data_in, int i) {
		this.data[i] = data_in;
	}

	/**
	 * @return the number_of_bins
	 */
	public int getNumber_of_bins() {
		return number_of_bins;
	}

	/**
	 * @param number_of_bins
	 *            the number_of_bins to set
	 */
	public void setNumber_of_bins(int number_of_bins) {
		this.number_of_bins = number_of_bins;
	}

	public void setBulkData(short[] inbuf) {
		for (int i = 0; i < inbuf.length; i++) {
			this.data[i] = UtilityClass.UINT2_to_SINT(inbuf[i]);
		}

		// Now lets translate the various data types.

	}

	/**
	 * @return the dtype
	 */
	public int getDtype() {
		return dtype;
	}

	/**
	 * @param dtype
	 *            the dtype to set
	 */
	public void setDtype(int dtype) {
		this.dtype = dtype;
	}

	public void translateData() {

		switch (this.dtype) {
		case 0:// Extended Headers
				// We just leave this for now.
			break;

		case 1:
		case 2:
			// 1 bit Power/Reflectivity
			for (int i = 0; i < this.data.length; i++) {
				if (data[i] != 255) {
					data[i] = (data[i] - 64) / 2;
				} else if (data[i] == 255) {
					data[i] = 255;
				}
			}
			break;

		// case 3://Velocity

		// case 4://Spectrum Width

		case 5: // ZDR (1 byte)
			for (int i = 0; i < this.data.length; i++) {
				if (this.data[i] == 0) {
					this.data[i] = Double.NaN;
				} else {
					data[i] = (data[i] - 128) / 16;
					
				}
			}
			break;
		// case 7: //Corrected Reflectivity

		case 8: // 2 Bit Power
		case 9: // 2 bit Reflectivity
			for (int i = 0; i < this.data.length; i++) {
				if ((this.data[i] == 0) || (this.data[i] == 65535)) {
				  this.data[i] = Double.NaN;
				} else {
					data[i] = (data[i] - 32768) / 100;
				}
				//System.out.println("Reflectivity or Powr Value of"+data[i]);
			}

			break;

		case 10: // 2 Byte velocity
			for (int i = 0; i < this.data.length; i++) {
				if ((this.data[i] == 0) || (this.data[i] == 65535)) {
				  this.data[i] = Double.NaN;
				} else {
					data[i] = (data[i] - 32768) / 100;
				}
			}

			break;

		case 11: // 2 Byte width
			for (int i = 0; i < this.data.length; i++) {
				if ((this.data[i] == 0) || (this.data[i] == 65535)) {
				  this.data[i] = Double.NaN;
				
				} else {
					data[i] = data[i] / 100;
				}
			}

			break;
			
		case 12: // 2 Byte ZDR
			for (int i = 0; i < this.data.length; i++) {
				if ((this.data[i] == 0) || (this.data[i] == 65535)) {
					this.data[i]= Double.NaN;
				} else {
					data[i] = (data[i] - 32768) / 100;

				}
			}
			break;

		// case 13: //Rainfall rate

		// case 14: //KDP 1 byte(See sigmet for the formula)

		case 15: // KDP 2 byte

			for (int i = 0; i < this.data.length; i++) {
				if ((this.data[i] == 0) || (this.data[i] == 65535)) {
				  this.data[i] = Double.NaN;
				} else {
					data[i] = (data[i] - 32768) / 100;
				}
			}

			break;

		case 16: // PhiDP 1 byte
			for (int i = 0; i < this.data.length; i++) {
				if ((data[i] == 255) || data[i] == 0) {
					this.data[i]=Double.NaN;
				} else {
					data[i] = (180 * (data[i] - 1) / 254) % 180;
				}
			}
			break;

		// case 17: //Corrected Velocity 1 byte

		case 19: // RhoHV 1 byte
			for (int i = 0; i < this.data.length; i++) {
				if ((data[i] == 0) || (data[i] == 255)) {
					data[i] = Math.sqrt((data[i] - 2) / 253);
				}
			}
			break;
		case 20: // RhoHV 2 byte
			for (int i = 0; i < this.data.length; i++) {
				if ((data[i] == 255) || data[i] == 0) {
					this.data[i]=Double.NaN;
				} else {
					data[i] = (data[i] - 1) / 65533;
				}
			}
			
			break;

		case 21: //DBZC2
			for (int i = 0; i < this.data.length; i++) {
				if ((this.data[i] == 0) || (this.data[i] == 65535)) {
					continue;
				} else {
					data[i] = (data[i] - 32768) / 100;
				}
			}

			break;
			
		case 23: // SQI(2Byte)
			for (int i = 0; i < this.data.length; i++) {
				if ((data[i] == 255) || data[i] == 0) {
					continue;
				} else {
					data[i] = (data[i] - 1) / 65533;
				}
			}
			break;

		case 24: // PhiDP(2 byte)
			for (int i = 0; i < this.data.length; i++) {
				if ((data[i] == 255) || data[i] == 0) {
					continue;
				} else {
					data[i] = (360 * (data[i] - 1) / 65534) % 360;
				}
			}
			break;

		case 58:
			for (int i = 0; i < this.data.length; i++) {
				if ((this.data[i] == 0) || (this.data[i] == 65535)) {
					continue;
				} else {
					data[i] = (data[i] - 32768) / 100;

				}
			}
			break;
			
			
		default:
		//	System.err.println("Unhandled Product Type" + this.dtype);
			break;
		}

	}
}
