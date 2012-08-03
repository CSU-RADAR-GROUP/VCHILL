package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ChillDefines.ColorType;
import edu.colostate.vchill.socket.SocketUtil;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Field description / scaling information header
 *
 * @author Jochen Deyke
 * @version 2007-10-29
 */
public class ChillMomentFieldScale extends ChillHeader implements Comparable<ChillMomentFieldScale>
{
    /** Maximum length (in bytes) of UTF-8 coded string */
    public static final int fieldNameLength = 32;
    public static final int fieldDescriptionLength = 128;
    public static final int unitsLength = 32;

    /** size (in bytes) of this header (including ChillHeaderHeader, but not including extraData) */
    public static final int BYTE_SIZE = ChillHeaderHeader.BYTE_SIZE +
        fieldNameLength + fieldDescriptionLength + ChillDefines.INT_BYTE_SIZE + unitsLength +
        6 * ChillDefines.INT_BYTE_SIZE + 2 * ChillDefines.SHORT_BYTE_SIZE;

    /** field name in UTF-8 coded string */
    public String fieldName;

    public String fieldDescription;

    public int keyboardAccelerator;

    public String units;

    /** identifies this field's bit number in the requested_data word.
        0 = bit 0 (lsb) = value of 1 in long long requested_data word */
    public int fieldNumber;

    /** factor, scale and bias provide scaling info for this field.
        actual field value =  ((value in data array)*scale + bias)/factor  */
    public int factor;
    public int scale;
    public int bias;    

    /** maximum field value (scaled by factor) */
    public int maxValue;

    /** minimum field value (scaled by factor) */
    public int minValue;

    /** bit 0 -> if 1 -> 8 bit signed if 0 -> 8 bit unsigned 
        bits 1-3 reserved 
        bit 4 -> if set indicates field may be unfolded 
        (currently CHILL uses only unsigned 8 bit data. This is offset
        binary rather than 2's complement.  The 'bias' word is used to
        locate the 0 for fields with positive and negative values) */
    public short dataWordCoding;

    /** This is used in selecting from available color tables, and as an indicator for special processing.
        e.g. vel fields can be unfolded by the display, and NCP fields may be used in thresholding */
    public ColorType colorMapType;

    /** current values used for display */
    private volatile double currentMax;
    private volatile double currentMin;

    public ChillMomentFieldScale (final ChillFieldInfo info, final int keyboardAccelerator, final String units, final int factor, final int scale, final int bias)
    {
        super(new ChillHeaderHeader(ChillDefines.FIELD_SCALE_DATA, BYTE_SIZE));
        extraData = new byte[0];
        this.fieldName = info.fieldName;
        this.fieldDescription = info.longFieldName;
        this.keyboardAccelerator = keyboardAccelerator;
        this.units = units;
        this.fieldNumber = info.fieldNumber;
        this.factor = factor;
        this.scale = scale;
        this.bias = bias;
        this.maxValue = info.maxValue;
        this.minValue = info.minValue;
        this.currentMax = this.maxValue / (double)this.factor;
        this.currentMin = this.minValue / (double)this.factor;
        this.dataWordCoding = info.dataWordCoding;
        this.colorMapType = ColorType.values()[info.colorMapType];
    }

    /**
     * Constructs a header by reading initial values from a DataInput.
     *
     * @param in the DataInput to read initialization values from
     */
    public ChillMomentFieldScale (final DataInput in, final ChillHeaderHeader header) throws IOException
    {
        super(header);
        assert header.recordType == ChillDefines.FIELD_SCALE_DATA;
        this.fieldName = SocketUtil.readString(in, fieldNameLength);
        this.fieldDescription = SocketUtil.readString(in, fieldDescriptionLength);
        this.keyboardAccelerator = in.readInt();
        this.units = SocketUtil.readString(in, unitsLength);
        this.fieldNumber = in.readInt();
        assert this.fieldNumber < ChillDefines.MAX_NUM_TYPES;
        this.factor = in.readInt();
        this.scale = in.readInt();
        this.bias = in.readInt();
        this.maxValue = in.readInt();
        this.minValue = in.readInt();
        this.dataWordCoding = in.readShort();
        short color = in.readShort();
        if (color >= ColorType.values().length) color = 0; //default to reflectivity on unknown color type
        this.colorMapType = ColorType.values()[color];
        this.currentMax = this.maxValue / (double)this.factor;
        this.currentMin = this.minValue / (double)this.factor;
        assert header.headerLength - ChillMomentFieldScale.BYTE_SIZE >= 0;
        in.readFully(this.extraData = new byte[header.headerLength - ChillMomentFieldScale.BYTE_SIZE]);
    }

    /**
     * Writes this header to a DataOut
     *
     * @param out the DataOutput to write values to
     */
    public void write (final DataOutput out) throws IOException
    {
        assert header.headerLength == ChillMomentFieldScale.BYTE_SIZE + extraData.length;
        super.header.write(out);
        SocketUtil.writeString(this.fieldName, out, fieldNameLength);
        SocketUtil.writeString(this.fieldDescription, out, fieldDescriptionLength);
        out.writeInt(this.keyboardAccelerator);
        SocketUtil.writeString(this.units, out, unitsLength);
        assert this.fieldNumber < ChillDefines.MAX_NUM_TYPES;
        out.writeInt(this.fieldNumber);
        out.writeInt(this.factor);
        out.writeInt(this.scale);
        out.writeInt(this.bias);
        out.writeInt(this.maxValue);
        out.writeInt(this.minValue);
        out.writeShort(this.dataWordCoding);
        out.writeShort((short)(this.colorMapType.ordinal()));
        out.write(this.extraData);
    }

    public double getValue (final int byteValue)
    {
        if (byteValue == 0) return Double.NaN;
        return (byteValue * (double)this.scale + this.bias) / this.factor;
    }

    public int getHash (final double doubleValue)
    {
        if (Double.isNaN(doubleValue)) return 0;
        return (int)((doubleValue * this.factor - this.bias) / this.scale);
    }

    public double getMax () {
        return this.currentMax;
    }

    public void setMax (final double newMax)
    {
        this.currentMax = newMax; //Math.min(newMax, this.maxValue / (double)this.factor);
    }

    public double getMin () {
        return this.currentMin;
    }

    public void setMin (final double newMin)
    {
        this.currentMin = newMin; //Math.max(newMin, this.minValue / (double)this.factor);
    }

    public boolean isUnfoldable ()
    {
        return ((1 << 4) & this.dataWordCoding) > 0;
    }

    public boolean isGradientable ()
    {
        return this.colorMapType == ColorType.Z;
    }

    public boolean shouldLowerBoundBeClipped ()
    {
        return this.colorMapType == ColorType.Z;
    }

    public int compareTo (final ChillMomentFieldScale other)
    {
        return this.fieldNumber - other.fieldNumber;
    }

    public int hashCode ()
    {
        return this.fieldNumber;
    }

    public boolean equals (Object other)
    {
        if (!(other instanceof ChillMomentFieldScale)) return false;
        return this.fieldNumber == ((ChillMomentFieldScale)other).fieldNumber;
    }

    public String toString ()
    {
        return this.fieldName;
    }
}
