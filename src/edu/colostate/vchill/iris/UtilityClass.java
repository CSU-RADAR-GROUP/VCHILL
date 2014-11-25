/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

/**
 * Class to contain miscellaneous utility functions, mostly unit conversions
 *
 * @author Joseph Hardin
 */
public class UtilityClass {

    public static int RECORD_SIZE = 6144;


    public static int UINT2_to_SINT(short input) {
        int b = input & 0xffff;
        return b;
    }

    public static long UINT4_to_long(int input) {
        long b = input & 0xffffffffL;
        return b;
    }

    public static short convert_code_word(short codeword) {
        return (short) (32767 + codeword + 1);
    }

    public static double BIN2_to_double(short input) {

        return ((double) UtilityClass.UINT2_to_SINT(input) / (90 * 2 ^ 16));
    }

    public static double BIN4_to_double(int input) {
        return ((double) UtilityClass.UINT4_to_long(input) * 360) / (2 ^ 32);
    }

    public static int cw_to_size(short cw) {
        return 0;
    }


}
