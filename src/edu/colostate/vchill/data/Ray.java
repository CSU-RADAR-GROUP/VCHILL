package edu.colostate.vchill.data;

/**
 * An abstract generic ray.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public abstract class Ray {
    /**
     * @return radar name/id
     */
    public abstract String getRadarId();

    /**
     * @return azimuth in degrees
     */
    public abstract double getStartAzimuth();

    /**
     * @return elevation in degrees
     */
    public abstract double getStartElevation();

    /**
     * @return azimuth in degrees
     */
    public abstract double getEndAzimuth();

    /**
     * @return elevation in degrees
     */
    public abstract double getEndElevation();

    /**
     * @return range to 1st gate in mm
     */
    public abstract double getStartRange();

    /**
     * @return range to last gate in mm
     */
    public abstract double getEndRange();

    /**
     * @return gate width in km
     */
    public abstract double getGateWidth();

    /**
     * @return gate data
     */
    public abstract double[] getData();

    /**
     * @return milliseconds since 1970
     */
    public abstract long getDate();

    /**
     * @return number of gates per ray; should be same as getData().length
     */
    public abstract int getNumberOfGates();

    /**
     * @return basic Nyquist interval in mm/sec
     */
    public abstract double getVelocityRange();

    /**
     * @return current sweep number
     */
    public abstract int getTiltNum();

    /**
     * @return operator selected sweep number to save
     */
    public abstract int getSaveTilt();

    /**
     * @return "PPI", "RHI", etc
     */
    public abstract String getMode();
}
