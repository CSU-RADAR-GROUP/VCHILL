package edu.colostate.vchill.chill;

/**
 * Test type, used when performing calibration cycles
 * @author Jochen Deyke
 * @version 2007-05-03
 */
public enum TestType
{
    /** No test is currently taking place */
    NONE,

    /** CW calibration is taking place */
    CW_CAL,

    /** Fixed sun-pointing is taking place */
    SOLAR_CAL_FIXED,

    /** Scanning across the face of the sun */
    SOLAR_CAL_SCAN,

    /** Noise source is connected to the H channel */
    NOISE_SOURCE_H,

    /** Noise source is connected to the V channel */
    NOISE_SOURCE_V,

    /** Antenna is pointing at blue-sky */
    BLUESKY,

    /** Not a real test mode, indicates to compute nodes to save cal params */
    SAVEPARAMS,
}
