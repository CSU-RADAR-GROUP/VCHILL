package edu.colostate.vchill.file;

import edu.colostate.vchill.ChillDefines;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The scaling information (factor, scale, bias) of a field.
 *
 * @author  Justin Carlson
 * @author  Brian Eriksson
 * @author  Jochen Deyke
 * @author  jpont
 * @created June 17, 2003
 * @version 2010-08-30
 */
public class FileFieldScalingInfo
{
    public static final int BYTE_SIZE = 3 * ChillDefines.INT_BYTE_SIZE;

    /**
     * scale and bias values are multiplied by this
     * factor before being stored
     */
    public int factor;
    
    /** gain of the data */                         
    public int scale;
    
    /** offset of zero value */
    public int bias;
    
    /**
     * Reads Field parameters header from the inputted CHILL file
     *
     * @param in DataInput object containing CHILL file
     */
    public void inputData (final DataInput in)
    {
        try {
            this.factor = in.readInt();
            this.scale = in.readInt();
            this.bias = in.readInt();
        } catch (IOException e) {
            System.out.println("IO message = " + e.getMessage());
            System.out.println("IO trace = ");
            e.printStackTrace();
        }
    }

    /**
     * Writes Field parameters header to the outputted CHILL file
     *
     * @param out DataOutput object containing CHILL file
     */
    public void outputData (final DataOutput out)
    {
        try {
            out.writeInt(this.factor);
            out.writeInt(this.scale);
            out.writeInt(this.bias);
        } catch (IOException e) {
            System.out.println("IO message = " + e.getMessage());
            System.out.println("IO trace = ");
            e.printStackTrace();
        }
    }

    @Override public String toString ()
    {
        return " factor: " + this.factor + "\n" +
               " scale : " + this.scale  + "\n" +
               " bias  : " + this.bias   + "\n";
    }
}
