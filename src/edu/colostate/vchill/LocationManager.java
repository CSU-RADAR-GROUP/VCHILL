package edu.colostate.vchill;

/**
 * Utility class to keep track of current radar location
 *
 * @author Jochen Deyke
 * @version 2006-05-25
 */
public class LocationManager
{
    private static final LocationManager lm = new LocationManager();

    private volatile double radarLatitude = ChillDefines.CHILL_LATITUDE;
    private volatile double radarLongitude = ChillDefines.CHILL_LONGITUDE;

    private LocationManager () {}

    public static final LocationManager getInstance () {
        return lm; }

    public synchronized void setLatitude (final double radarLatitude) {
        this.radarLatitude = radarLatitude; }
    public double getLatitude () {
        return this.radarLatitude; }

    public synchronized void setLongitude (final double radarLongitude) {
        this.radarLongitude = radarLongitude; }
    public double getLongitude () {
        return this.radarLongitude; }
}
