package edu.colostate.vchill;

import edu.colostate.vchill.ViewUtil;
import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.chill.ChillMomentFieldScale;

/**
 * Utility methods for calculating KDP
 *
 * @author Jochen Deyke
 * @version 2007-05-25
 */
public class KdpUtil
{
    public static final ChillMomentFieldScale scale = new ChillMomentFieldScale(ChillFieldInfo.KDP, java.awt.event.KeyEvent.VK_K, "deg/km", 255, 20, -1280);

    /** KDP threshold for bad data */
    public static final double snrThresh = 3.0;
    public static final double rhoHVThresh = 0.9; //c
    //public static final double rhoHVThresh = 0.1; //fortran
    public static final double stdDevThresh = 7.0; //c
    //public static final double stdDevThresh = 15.0; //fortran

    /** Number of "good" gates to enter a cell */
    public static final int goodNeeded = 10;

    /** Number of "bad" gates needed to leave a cell */
    public static final int badNeeded = 5;
    
    /** Number of gates to average for padding */
    public static final int numToAverage = 4;

    /** FIR3 filter */
    private static final double deltaThresh = 3;
    private static final double fir3gain = 1.044222;
    private static final double[] fir3coef = {
        1.625807356e-2, 2.230852545e-2, 2.896372364e-2,
        3.595993808e-2, 4.298744446e-2, 4.971005447e-2, 5.578764970e-2,
        6.089991897e-2, 6.476934523e-2, 6.718151185e-2, 6.80010000e-2,
        6.718151185e-2, 6.476934523e-2, 6.089991897e-2, 5.578764970e-2,
        4.971005447e-2, 4.298744446e-2, 3.595993808e-2, 2.896372364e-2,
        2.230852545e-2, 1.625807356e-2,
    };

    /** calculate kdp slope using +/- this many gates */
    public static final int windowSize = 10;

    /** private default constructor prevents instantiation */
    private KdpUtil () {}

    /**
     * @param startRange initial range offset in km
     * @param gateWidth width of gate in km
     */
    public static double[] calculateKDP (final double[] phi, final double[] dbz, final double[] rhohv, final double startRange, final double gateWidth)
    {
        //these arrays contain actual data values (eg dBz, deg, km)
        //phidp is initially extended to allow for FIR3 filter
        double[] phidp = new double[phi.length + 2*windowSize];
        double[] snr = new double[dbz.length];
        double[] range = new double[phidp.length]; //also extended
        final double[] kdp = new double[phi.length];

        //initialize double arrays from byte arrays
        for (int gate = 0; gate < range.length; ++gate) {
            range[gate] = startRange + (gate - windowSize)*gateWidth;
        }
        for (int gate = 0; gate < snr.length; ++gate) {
            phidp[gate+windowSize] = phi[gate];
            snr[gate] = dbz[gate] + 43.0 - (20.0 * Math.log10(range[gate + windowSize]));
        }

        //fill in phidp extensions:
        { //find the 1st and last 4 good gates; pad lead-in/-out with average
            Run first = findNextGoodRun(windowSize, phidp, snr, rhohv);
            if (first != null) for (int gate = first.start - numToAverage; gate < first.start; ++gate) {
                phidp[gate] = first.average;
            }

            Run last = findPreviousGoodRun (phidp.length - 1, phidp, snr, rhohv);
            if (last != null) for (int gate = last.end+1; gate < last.end+1+numToAverage; ++gate) {
                phidp[gate] = last.average;
            }

            //can't fill in if there is no good data
            if (first != null && last != null && first.start != last.start)
            { //fill in bad spots with weighted average of surrounding good data
                Run prev = first;
                Run curr = findNextGoodRun(prev.end, phidp, snr, rhohv);
                while (curr != null && curr.start != last.start) {
                    for (int gate = prev.end; gate < curr.start; ++gate) {
                        //weighted average of surrounding good run's averages
                        phidp[gate] = phidp[prev.end - 1] + ((range[gate] - range[prev.end - 1]) * (phidp[curr.start] - phidp[prev.end - 1]) / (range[curr.start] - range[prev.end - 1]));
                    }
                    //go to next good run
                    prev = curr;
                    curr = findNextGoodRun(prev.end, phidp, snr, rhohv);
                }
            }
        }

        //FIR3 filter
        for (int pass = 0; pass < 2; ++pass) {
            double[] filtered = new double[phidp.length];
            for (int gate = windowSize; gate < phidp.length-windowSize; ++gate) {
                double acc = 0;
                for (int term = 0; term < fir3coef.length; ++term) {
                    acc += (fir3coef[term] * phidp[gate - fir3coef.length / 2 + term]);
                }
                filtered[gate] = acc * fir3gain;
                //don't smooth if insufficient delta
                double delta = Math.abs(phidp[gate] - filtered[gate]);
                if (delta < deltaThresh) filtered[gate] = phidp[gate];
            }
            phidp = filtered;
        }
        
        //calculate derivative
        for (int gate = 0; gate < snr.length; ++gate) {
            if (isGateBad(snr, rhohv, phidp, gate)) continue;
            //calculate derivative / slope of best fit line
            kdp[gate] = linearLeastSquareEstimate(range, phidp, gate, gate + 2 * windowSize)[0] / 2.0;
        }

        return kdp;
    }

    /**
     * Linear Least Square Estimate subroutine to fit a linear equation for
     * (x[i], y[i]) (i=startIndex,...,endIndex), so that y[i] = slope * x[i] + intercept
     *
     * @param x range (km)
     * @param y PhiDP (deg)
     * @param startIndex the first index to process
     * @param endIndex the last index to process 
     * @return {slope, intercept} of fitted line
     */
    private static double[] linearLeastSquareEstimate (final double[] x, final double[] y, final int startIndex, final int endIndex)
    {
        if (startIndex < 0) throw new IllegalArgumentException("startIndex must be >= 0");
        int pastEnd = endIndex + 1;
        if (pastEnd > x.length) throw new IllegalArgumentException("endIndex must be < x.length");
        if (pastEnd > y.length) throw new IllegalArgumentException("endIndex must be < y.length");
        if (startIndex >= endIndex) throw new IllegalArgumentException("endIndex must be > startIndex");

        double sum_x = 0.0;
        double sum_y = 0.0;
        double sum_xy = 0.0;
        double sum_xx = 0.0;
        double sum_x_sum_x = 0.0;
        double total = pastEnd - startIndex;

        for (int i = startIndex; i < pastEnd; ++i) {
            sum_x += x[i];
            sum_y += y[i];
            sum_xy += x[i] * y[i];
            sum_xx += x[i] * x[i];
        }

        sum_x_sum_x = sum_x * sum_x;

        /* output desired items */
        double slope = (sum_x*sum_y - total*sum_xy)/(sum_x_sum_x - total*sum_xx);
        double intercept = (sum_y - slope * sum_x) / total;
        return new double[] {slope, intercept};
    }

    /**
     * Calculates the mean and standard deviation of an array of double
     *
     * @param inp floating point input array
     * @param startIndex the first index to average
     * @param pastEnd the index just past the last one to average
     * @return {mean, standardDeviation}
     */ 
    private static double[] meanAndStandardDeviation (final double[] inp, final int startIndex, final int pastEnd)
    {
        if (startIndex < 0) throw new IllegalArgumentException("startIndex must be >= 0");
        if (pastEnd > inp.length) throw new IllegalArgumentException("pastEnd must be <= inp.length");
        if (startIndex >= pastEnd) throw new IllegalArgumentException("pastEnd must be > startIndex");

        double sum_y = 0.0;
        double sum_ymean = 0.0;
        double total = pastEnd - startIndex;

        for (int i = startIndex; i < pastEnd; ++i) sum_y += inp[i];
        double local_mean = sum_y/total;
        for (int i = startIndex; i < pastEnd; ++i) {
            double y = inp[i] - local_mean;
            sum_ymean += (y*y);
        }

        double std_dev = Math.sqrt(sum_ymean/total);
        return new double[] {local_mean, std_dev};
    }

    /**
     * Checks whther a particular gate passes certain thresholds
     *
     * @param snr An array of snr values
     * @param rhohv An array of rhohv values (must be same size as snr)
     * @param phidp An array of phidp values (must be snr.length + 2*windowSize)
     * @return true if gate is considered bad, false if gate is good
     */
    private static boolean isGateBad (final double[] snr, final double[] rhohv, final double[] phidp, final int gate)
    {
        try {
            if (snr[gate] < snrThresh) return true; //noise
            if (rhohv[gate] < rhoHVThresh) return true; //too little RhoHV
            double stdDev = meanAndStandardDeviation(phidp, gate, gate + windowSize + 1)[1];
            if (stdDev > stdDevThresh) return true; //too much deviation in PhiDP
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return true; //over edge is also considered bad
        }
        return false;
    }

    /**
     * @param index the index to start searching from
     * @param phidp the array of PhiDP data (in degrees) to search
     * @return the next run of good data or <code>null</code> if no good run found
     */
    private static Run findNextGoodRun (final int index, final double[] phidp, final double[] snr, final double[] rhohv)
    { //find the 1st 4 good gates
        double total = 0;
        int start = index;
        int numGood = 0, numBad = 0;
        for (int gate = index; gate < phidp.length; ++gate) {
            if (isGateBad(snr, rhohv, phidp, gate)) {
                ++numBad;
                if (numBad > badNeeded) { //only reset if sufficient gates
                    if (numGood >= numToAverage) { //end of a run of good data
                        return new Run(start, gate - badNeeded, total / numGood);
                    }
                    numGood = 0;
                    total = 0;
                }
            } else { //good gate
                if (numGood == 0) start = gate; //first good gate this run
                total += phidp[gate];
                ++numGood;
                if (numGood > goodNeeded) { //only reset if sufficient gates
                    numBad = 0;
                }
            }
        }
        return null;
    }

    /**
     * @param index the index to start searching from
     * @param phidp the array of PhiDP data (in degrees) to search
     * @return the previous run of good data or <code>null</code> if no good run found
     */
    private static Run findPreviousGoodRun (final int index, final double[] phidp, final double[] snr, final double[] rhohv)
    {
        double total = 0;
        int end = index;
        int numGood = 0, numBad = 0;
        for (int gate = index; gate > 0; --gate) {
            if (isGateBad(snr, rhohv, phidp, gate)) {
                ++numBad;
                if (numBad > badNeeded) { //only reset if sufficient gates
                    if (numGood >= numToAverage) { //end of a run of good data
                        return new Run(gate + badNeeded, end, total / numGood);
                    }
                    numGood = 0;
                    total = 0;
                }
            } else { //good gate
                if (numGood == 0) end = gate; //first good gate this run
                total += phidp[gate];
                ++numGood;
                if (numGood > goodNeeded) { //only reset if sufficient gates
                    numBad = 0;
                }
            }
        }
        return null;
    }

    /**
     * A <code>Run</code> represents a contiguous run of good data
     */
    private static class Run
    {
        
        /** start of good data */
        public final int start;
        /** start of bad data after good run */
        public final int end;
        /** average of good data */
        public final double average;

        public Run (final int start, final int end, final double average)
        { this.start = start; this.end = end; this.average = average; }
    }
}
