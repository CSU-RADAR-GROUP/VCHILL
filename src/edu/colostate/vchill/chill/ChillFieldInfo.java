package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;

/**
 * This class describes the data coming in over CHILL archive connections.
 * This info is necessary mainly for proxy calculations;
 * elsewhere, use the ChillMomentFieldScale blocks from the network.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2008-03-25
 */
public class ChillFieldInfo {
    /**
     * short field name 4 chars max
     */
    public final int fieldNameLength = 16;
    public final String fieldName;

    /**
     * long field name
     */
    public final int longFieldNameLength = 60;
    public final String longFieldName;

    /**
     * defines field order. this is also this field's bit number in the moment request mask
     */
    public final int fieldNumber;

    /**
     * maximum value (for full scale display)
     */
    public final int maxValue;

    /**
     * minumum value (for full scale display)
     */
    public final int minValue;

    /**
     * bit 0 [1->8 bit signed, 0->unsigned], bit 4 [1->velocity unfold this field]
     */
    public final short dataWordCoding;

    /**
     * what type of color map to use - see examples below
     */
    public final short colorMapType;

    public static final ChillFieldInfo Z = new ChillFieldInfo("dBZ", "Reflectivity", 12, 7500000, -1000000, 0, 0);
    public static final ChillFieldInfo V = new ChillFieldInfo("Vel", "Mean Velocity", 13, 5500000, -5500000, 16, 1);
    public static final ChillFieldInfo W = new ChillFieldInfo("Wid", "Spectral Width", 14, 5500000, -5500000, 0, 2);
    public static final ChillFieldInfo NCP = new ChillFieldInfo("NCP", "Normalized Coherent Power", 15, 1000000, 0, 0, 3);
    public static final ChillFieldInfo ZDR = new ChillFieldInfo("ZDR", "Differential Reflectivity", 16, 9030899, -3010299, 0, 4);
    public static final ChillFieldInfo LDRH = new ChillFieldInfo("LDRH", "Linear Depolarization H", 17, 0, -480000, 0, 5);
    public static final ChillFieldInfo LDRV = new ChillFieldInfo("LDRV", "Linear Depolarization V", 18, 0, -480000, 0, 5);
    public static final ChillFieldInfo PHIDP = new ChillFieldInfo("PHIDP", "Diferential Phase Shift", 19, 90000000, -90000000, 0, 6);
    public static final ChillFieldInfo RHOHV = new ChillFieldInfo("RhoHV", "HV Corelation at lag 0", 20, 1000000, 0, 0, 7);
    public static final int CALC_CUTOFF = 21;
    public static final ChillFieldInfo KDP = new ChillFieldInfo("KDP", "Specific Differential Phase", 22, 2550, -512, 0, 7); //fix scale
    public static final ChillFieldInfo NCP_PLUS = new ChillFieldInfo("NCPp", "NCP + s.deviation of Zdr filter", 23, 1000000, 0, 0, 3); //fix scale
    public static final ChillFieldInfo HDR = new ChillFieldInfo("HDR", "Aydin Hail", 24, 25000000, 0, 0, 0); //fix scale
    public static final ChillFieldInfo RCOMP = new ChillFieldInfo("RComp", "Rain Rate", 25, 25000000, 0, 0, 3); //fix scale
    public static final ChillFieldInfo VFilt = new ChillFieldInfo("VelFilt", "FilteredVelocity", 26, 5500000, -5500000, 16, 1);
    public static final ChillFieldInfo VFast = new ChillFieldInfo("VelFast", "VelocityFast", 27, 5500000, -5500000, 16, 1);
    public static final ChillFieldInfo VSlow = new ChillFieldInfo("VelSlow", "VelocitySlow", 28, 5500000, -5500000, 16, 1);
    public static final ChillFieldInfo[] types = {Z, V, W, NCP, ZDR, LDRH, LDRV, PHIDP, RHOHV, KDP, NCP_PLUS, HDR, RCOMP, VFilt, VFast, VSlow};

    public ChillFieldInfo(final String fieldName, final String longFieldName, final int fieldNumber, final int maxValue, final int minValue, final int dataWordCoding, final int colorMapType) {
        assert fieldName.length() < fieldNameLength;
        this.fieldName = fieldName;
        assert longFieldName.length() < longFieldNameLength;
        this.longFieldName = longFieldName;
        assert fieldNumber < ChillDefines.MAX_NUM_TYPES;
        this.fieldNumber = fieldNumber;
        assert maxValue > minValue;
        this.maxValue = maxValue;
        this.minValue = minValue;
        assert dataWordCoding <= Short.MAX_VALUE;
        this.dataWordCoding = (short) dataWordCoding;
        assert colorMapType <= Short.MAX_VALUE;
        this.colorMapType = (short) colorMapType;
    }
}
