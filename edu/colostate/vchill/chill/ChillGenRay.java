package edu.colostate.vchill.chill;

import edu.colostate.vchill.ViewUtil;
import edu.colostate.vchill.data.Ray;

/**
 * Data storage using the new more general protocol
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2007-11-29
 */
public class ChillGenRay extends Ray
{
    /** sweepwide header */
    public ChillHSKHeader hskH;

    /** raywide header */
    public ChillDataHeader dataH;

    /** name of datatype; used for scaling etc */
    public String type;

    /** the actual gate data; uncompressed */
    public double[] data;

    public ChillGenRay (final ChillHSKHeader hskH, final ChillDataHeader dataH, final String type, final double[] data)
    {
        this.hskH = hskH;
        this.dataH = dataH;
        this.type = type;
        this.data = data;
    }

    public ChillGenRay (final ChillHSKHeader hskH, final ChillDataHeader dataH, final String type, final byte[] data)
    {
        this.hskH = hskH;
        this.dataH = dataH;
        this.type = type;
        this.data = ViewUtil.getValues(data, type);
    }

    public String getRadarId () { return this.hskH.radarId; }
    public double getStartAzimuth () { return this.dataH.startAz * 360.0 / this.hskH.angleScale; }
    public double getStartElevation () { return this.dataH.startEl * 360.0 / this.hskH.angleScale; }
    public double getEndAzimuth () { return this.dataH.endAz * 360.0 / this.hskH.angleScale; }
    public double getEndElevation () { return this.dataH.endEl * 360.0 / this.hskH.angleScale; }
    public double getStartRange () { return this.dataH.startRange; }
    public double getEndRange () { return this.dataH.startRange + this.hskH.gateWidth * this.dataH.numGates; }
    public double getGateWidth () { return this.hskH.gateWidth * 1e-6; }
    public double[] getData () { return this.data; }
    public long getDate () { return this.dataH.dataTime * 1000l + this.dataH.fractionalSecs / 1000000; }
    public int getNumberOfGates () { return this.dataH.numGates; }
    public double getVelocityRange () { return this.hskH.nyquistVel / 1000.0; }
    public int getTiltNum () { return this.hskH.tiltNum; }
    public int getSaveTilt () { return this.hskH.saveTilt; }
    public String getMode () { return this.hskH.getMode(); }

    public ChillHSKHeader getHSKHeader () { return this.hskH; }
    public ChillDataHeader getDataHeader () { return this.dataH; }
    public String getType () { return this.type; }
}
