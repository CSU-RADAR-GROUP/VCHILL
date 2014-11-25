package edu.colostate.vchill;

import edu.colostate.vchill.chill.ChillMomentFieldScale;

import java.text.NumberFormat;

/**
 * Utility methods for displaying information in the View module
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-02
 */
public final class ViewUtil {
    private static final NumberFormat nf = Config.getInstance().getNumberFormat();
    private static final ScaleManager sm = ScaleManager.getInstance();
    private static final LocationManager lm = LocationManager.getInstance();

    /* Constants */
    private static final double K_E = 4.0 / 3.0;

    /**
     * The types of ellipsoids available.
     * <p/>
     * These are used to choose one of several available ellipsoid models
     * of the earth. The ellipsoid used depends on the data source. GPS
     * data makes use of the WGS-84 earth-centric model. If no specific
     * model is to be used, use the DEFAULT_ELLIPSOID constant, which
     * currently selects the WGS-84 model.
     */
    private enum Ellipsoid {
        /**
         * World Geodetic System, 1984 update -- used by GPS
         */
        WGS_1984(6378137, 298.257223563),
        /**
         * Clarke Ellipsoid (Nories)
         */
        CLARKE_1880(6378249.145, 293.465),
        /**
         * World Geodetic System, 1972 update
         */
        WGS_1972(6378153, 298.26),
        /**
         * Australian National Speroid, 1966
         */
        ANS_1966(6378160, 298.25),
        /**
         * 1980 Geodetic Reference System ellipsoid
         */
        GRS_1980(6378137, 298.257222101),;

        /**
         * Semimajor axis
         */
        public final double a;

        /**
         * Reciprocal of Flattening factor
         */
        public final double f;

        Ellipsoid(final double a, final double f) {
            this.a = a;
            this.f = f;
        }
    }

    private static final Ellipsoid DEFAULT_ELLIPSOID = Ellipsoid.WGS_1984;

    /**
     * Earth model to use for height calculation.
     * <p/>
     * The Flat model assumes the earth is flat, and uses a simple
     * trigonometric solution to find the height.
     * The curved model assumes the earth is spherical, which is
     * valid over the typical ranges at which radar targets can be
     * seen
     */
    private enum EarthModel {
        FLAT,
        CURVED,
    }

    private static double meridionalDist(final double lat, final double e, final double a) {
        double e2 = e * e;
        double e4 = e2 * e2;
        double e6 = e2 * e4;

        double v1 = 1.0 - e2 / 4.0 - 3.0 * e4 / 64.0 - 5.0 * e6 / 256.0;
        double v2 = 3.0 / 8.0 * (e2 - e4 / 4.0 + 15.0 * e6 / 128.0);
        double v3 = 15.0 / 256.0 * (e4 + 3.0 * e6 / 4.0);

        return a * (v1 * lat - v2 * Math.sin(2.0 * lat) + v3 * Math.sin(4.0 * lat));
    }

    private static double meridionalPart(final double lat, final double e) {
        double temp = e * Math.sin(lat);
        return Math.log(Math.tan(Math.PI / 4.0 + lat / 2.0)) - 0.5 * e * Math.log((1 + temp) / (1 - temp));
    }

    /**
     * Integrate along path to find approximate target latitude
     */
    private static double invSolution(final double ref_lat_r,
                                      final double e, final double a,
                                      final double range, final double azimuth_r) {
        double m_from = meridionalDist(ref_lat_r, e, a);
        double m_guess = m_from + range * Math.cos(azimuth_r);
        double lat = ref_lat_r + Math.PI * (m_guess - m_from) / (1852 * 60.0 * 180.0);
        for (int iteration = 0; iteration < 10; ++iteration) {
            double m_partial = meridionalDist(lat, e, a);
            lat = lat + Math.PI * (m_guess - m_partial) / (1852 * 60.0 * 180.0);
        }
        return lat;
    }

    /**
     * Convert from lat/long to range/heading
     *
     * @param lat_d      Destination point latitude (in degrees)
     * @param long_d     Destination point longitude (in degrees)
     * @param ref_lat_d  Reference point latitude (in degrees)
     * @param ref_long_d Reference point longitude (in degrees)
     * @param ellipsoid  The reference ellipsoid to use when making computations.
     *                   Can be set to DEFAULT_ELLIPSOID to use the default (WGS-84)
     * @return [range (in m), azimuth (in degrees)] from reference to destination
     */
    private static double[] latlongToRtheta(
            double lat_d, double long_d,
            double ref_lat_d, double ref_long_d,
            Ellipsoid ellipsoid) {
        double d_lat, d_long;
        double course;
        double distance;
        double delta_mdist;
        double delta_mpart;

        double lat_r = Math.toRadians(lat_d);
        double long_r = Math.toRadians(long_d);
        double ref_lat_r = Math.toRadians(ref_lat_d);
        double ref_long_r = Math.toRadians(ref_long_d);

        double flattening = 1 / ellipsoid.f;
        double eccentricity = Math.sqrt(2.0 * flattening - flattening * flattening);
        double major_axis = ellipsoid.a;

        if (Math.signum(ref_long_r) == Math.signum(long_r)) {
            d_long = long_r - ref_long_r;
        } else {
            d_long = 2.0 * Math.PI - Math.abs(long_r) - Math.abs(ref_long_r);
            if ((long_r - ref_long_r) >= 0.0) {
                d_long = -d_long;
            }
        }
        d_lat = lat_r - ref_lat_r;

        if (lat_r == ref_lat_r) { /* Special case of the same latitude */
            course = (d_long > 0.0) ? 90.0 : 270.0;
            double temp = eccentricity * Math.sin(lat_r);
            distance = major_axis * d_long * Math.cos(lat_r) / Math.sqrt(1 - temp * temp);
        } else if (long_r == ref_long_r) { /* Special case of the same longitude */
            delta_mdist = meridionalDist(lat_r, eccentricity, major_axis) -
                    meridionalDist(ref_lat_r, eccentricity, major_axis);
            distance = Math.abs(delta_mdist * 60.0);
            course = (d_lat > 0.0) ? 0.0 : 180.0;
        } else {
            delta_mpart = meridionalPart(lat_r, eccentricity) -
                    meridionalPart(ref_lat_r, eccentricity);
            delta_mdist = meridionalDist(lat_r, eccentricity, major_axis) -
                    meridionalDist(ref_lat_r, eccentricity, major_axis);
            course = Math.atan2(d_long, delta_mpart);
            if (course < 0.0) course += (2.0 * Math.PI);
            distance = delta_mdist / Math.cos(course);
        }

        return new double[]{distance, Math.toDegrees(course)};
    }

    /**
     * Convert from range/heading to lat/long
     *
     * @param rng_m      Range (in m) from reference pt to destination is stored here
     * @param azimuth_d  Azimuth (in degrees) from ref. pt to destination stored here
     * @param ref_lat_d  Reference point latitude
     * @param ref_long_d Reference point longitude
     * @param ellipsoid  The reference ellipsoid to use when making computations.
     *                   Can be set to DEFAULT_ELLIPSOID to use the default (WGS-84)
     * @return [lat, long] (in degrees)
     */
    private static double[] rthetaToLatlong(
            double rng_m, double azimuth_d,
            double ref_lat_d, double ref_long_d,
            Ellipsoid ellipsoid) {
        double lat_r, long_r;

        double flattening = 1 / ellipsoid.f;
        double eccentricity = Math.sqrt(2.0 * flattening - flattening * flattening);
        double major_axis = ellipsoid.a;

        double azimuth_r = Math.toRadians(azimuth_d);
        double ref_lat_r = Math.toRadians(ref_lat_d);
        double ref_long_r = Math.toRadians(ref_long_d);
        
        /* Special case various cases which would cause tan and atan to blow up */
        if ((azimuth_r == 0.0) || (azimuth_r == Math.PI)) {
            lat_r = invSolution(ref_lat_r, eccentricity, major_axis, rng_m, azimuth_r);
            long_r = ref_long_r;
        } else if ((azimuth_r == Math.PI / 2.0) || (azimuth_r == (3.0 * Math.PI / 2.0))) {
            double temp = eccentricity * Math.sin(ref_lat_r);
            lat_r = ref_lat_r;
            long_r = ref_long_r + ((azimuth_r == Math.PI / 2.0) ? rng_m : -rng_m) * Math.sqrt(1 - temp * temp) /
                    (major_axis * Math.cos(ref_lat_r));
        } else {
            lat_r = invSolution(ref_lat_r, eccentricity, major_axis, rng_m, azimuth_r);
            double delta_mpart = meridionalPart(lat_r, eccentricity) -
                    meridionalPart(ref_lat_r, eccentricity);
            long_r = ref_long_r + delta_mpart * Math.tan(azimuth_r);
            if (azimuth_r < Math.PI) {
                long_r = ref_long_r + Math.abs(delta_mpart * Math.tan(azimuth_r));
            } else {
                long_r = ref_long_r - Math.abs(delta_mpart * Math.tan(azimuth_r));
            }
        }

        if (long_r > Math.PI) long_r -= (2.0 * Math.PI);
        if (long_r < -Math.PI) long_r += (2.0 * Math.PI);

        return new double[]{Math.toDegrees(lat_r), Math.toDegrees(long_r)};
    }

    /**
     * Convert from target slant range/elevation to height above ground
     * and distance along ground
     *
     * @param earthModel Earth model to use when performing calculations
     * @param ellipsoid  The reference ellipsoid to use when making computations.
     *                   Can be set to DEFAULT_ELLIPSOID to use the default (WGS-84)
     * @param range      Slant range to target, in meters
     * @param elevation  Elevation to target, in degrees
     * @return [height above ground, distance along ground] in meters
     */
    private static double[] getHeight(final EarthModel earthModel, final Ellipsoid ellipsoid, final double range, final double elevation) {
        double earth_radius = ellipsoid.a;
        double elevation_r = Math.toRadians(elevation);

        switch (earthModel) {
            case FLAT:
                return new double[]{range * Math.tan(elevation_r), range * Math.cos(elevation_r)};
            case CURVED:
                double height = Math.sqrt(range * range + K_E * K_E * earth_radius * earth_radius + 2.0 * range * K_E * earth_radius * Math.sin(elevation_r)) - K_E * earth_radius;
                return new double[]{height, K_E * earth_radius * Math.asin(range * Math.cos(elevation_r) / (K_E * earth_radius + height))};
        }

        return null;
    }

    /**
     * Converts km from radar to range/heading
     *
     * @param x km east of the radar
     * @param y km north of the radar
     * @return [range (in m), azimuth (in degrees)]
     */
    public static double[] kmToRtheta(final double x, final double y) {
        double range = 1e3 * Math.sqrt(x * x + y * y);
        double azimuth = 90 - Math.toDegrees(Math.PI / 2 - Math.atan2(x, y));
        if (azimuth < 0) azimuth += 360;
        return new double[]{range, azimuth};
    }

    /**
     * Converts range/heading to km from the radar
     *
     * @param range   in m
     * @param azimuth in degrees
     * @param isMap   specifies if this conversion is for a map
     * @return [x , y] in km east, north of the radar
     */
    public static double[] rthetaToKm(final double range, final double azimuth, final boolean isMap) {
        if (isMap) {
            double radians = Math.toRadians(90 - azimuth);
            if (radians < 0) radians += 2 * Math.PI;
            double x = range * 1e-3 * Math.cos(radians);
            double y = range * 1e-3 * Math.sin(radians);
            return new double[]{x, y};
        } else
            return new double[]{range * Math.cos(Math.toRadians(azimuth)), range * Math.sin(Math.toRadians(azimuth))};
    }

    /**
     * Private default constructor prevents instantiation
     */
    private ViewUtil() {
    }

    /**
     * Format a double into a string
     *
     * @param value the value to format
     * @return the formatted string version of <code>value</code>
     */
    public static String format(final double value) {
        return nf.format(value);
    }

    /**
     * Calculates the elevation above ground level from a given curved elevation
     *
     * @param height the distance (km) above the radar
     * @param range the distance (km) from the radar
     * @return the distance (km) above ground level
    public static double getElevationAngle (final double height, final double range)
    {
    return 180 + (Math.atan(Math.sin(Math.PI - range / a)/(a / (height + a) + Math.cos(Math.PI - range / a))) - Math.PI / 2)/(Math.PI / 180.0);
    }
     */

    /**
     * Calculates the elevation taking into account the Earth's curve
     *
     * @param el    the eleavation angle
     * @param range the distance (km) from the radar
     * @return the distance (km) above the radar
     */
    public static double getKmElevation(final double el, final double range) {
        return getHeight(EarthModel.CURVED, DEFAULT_ELLIPSOID, range * 1000, el)[0] / 1000;
    }

    /**
     * Converts lat/long to km from the radar
     *
     * @param lon   longitude of target (in degrees)
     * @param lat   latitude of target (in degrees)
     * @param isMap specifies if this conversion is for a map
     * @return [x, y] in km east, north of location given by LocationManager
     */
    public static double[] getKm(final double lon, final double lat, final boolean isMap) {
        double[] rtheta = latlongToRtheta(lat, lon, lm.getLatitude(), lm.getLongitude(), DEFAULT_ELLIPSOID);
        double[] km = rthetaToKm(rtheta[0], rtheta[1], isMap);
        return km;
    }

    /**
     * Converts km north/east of the radar into degrees latitude/longitude
     *
     * @param x km east of the radar
     * @param y km north of the radar
     * @return longitude, latitude in degrees
     */
    public static double[] getDegrees(final double x, final double y) {
        return getDegrees(x, y, lm.getLongitude(), lm.getLatitude());
    }

    /**
     * Converts km north/east of the specified center into degrees latitude/longitude
     *
     * @param x               km east of the radar
     * @param y               km north of the radar
     * @param centerLongitude in degrees
     * @param centerLatitude  in degrees
     * @return longitude, latitude in degrees
     */
    public static double[] getDegrees(final double x, final double y, final double centerLongitude, final double centerLatitude) {
        double[] rtheta = kmToRtheta(x, y);
        double[] degrees = rthetaToLatlong(rtheta[0], rtheta[1], centerLatitude, centerLongitude, DEFAULT_ELLIPSOID);
        return new double[]{degrees[1], degrees[0]};
    }

    /**
     * Converts km distances to kft
     *
     * @param km the distance to convert
     * @return the distance in kft
     */
    public static double getKFtFromKm(final double km) {
        return km * 3 / 0.9144;
    }

    /**
     * Converts a compressed byte value to
     * an uncompressed double-precision floating point equivalent
     *
     * @param byteValue the compressed value - valid range is 0-255 inclusive
     * @param type      the name of the data type
     * @return the uncompressed value
     */
    public static double getValue(final int byteValue, final String type) {
        return sm.getScale(type).getValue(byteValue);
    }

    /**
     * Converts an uncompressed double-precision floating point value to
     * a compressed byte equivalent
     *
     * @param doubleValue the uncompressed value
     * @param type        the name of the data type
     * @return the compressed value - valid range is 0-255 inclusive
     */
    public static int getHash(final double doubleValue, final String type) {
        int hash = sm.getScale(type).getHash(doubleValue);
        return hash;
    }

    /**
     * Converts an array of compressed byte values to
     * an array of uncompressed double-precision floating point equivalents
     *
     * @param bytes the compressed values
     * @param type  the name of the data type
     * @return the uncompressed values
     */
    public static double[] getValues(final byte[] bytes, final String type) {
        ChillMomentFieldScale scale = sm.getScale(type);
        double[] values = new double[bytes.length];
        for (int i = 0; i < bytes.length; ++i) values[i] = scale.getValue(bytes[i] & 0xff);
        return values;
    }

    /**
     * Converts an array of uncompressed double-precision floating point values to
     * an array of compressed byte equivalents
     *
     * @param values the uncompressed values
     * @param type   the name of the data type
     * @return the compressed values
     */
    public static byte[] getBytes(final double[] values, final String type) {
        ChillMomentFieldScale scale = sm.getScale(type);
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; ++i) bytes[i] = (byte) scale.getHash(values[i]);
        return bytes;
    }
}
