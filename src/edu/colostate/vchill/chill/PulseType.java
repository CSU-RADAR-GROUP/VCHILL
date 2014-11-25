package edu.colostate.vchill.chill;

/**
 * Pulse type used by transmitter
 *
 * @author Jochen Deyke
 * @version 2007-05-03
 */
public enum PulseType {
    /**
     * 1 microsecond rectangular pulse
     */
    rect_1us,

    /**
     * 200 nanosecond rectangular pulse
     */
    rect_200ns,

    /**
     * 1 microsecond gaussian-weighted pulse
     */
    gaussian_1us,
}
