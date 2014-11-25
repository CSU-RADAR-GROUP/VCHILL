package edu.colostate.vchill;

import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.chill.ChillMomentFieldScale;

/**
 * Utility methods for calculating NCP_PLUS
 *
 * @author Jochen Deyke
 * @version 2006-03-15
 */
public class NcpPlusUtil {
    public static final ChillMomentFieldScale scale = new ChillMomentFieldScale(ChillFieldInfo.NCP_PLUS, java.awt.event.KeyEvent.VK_U, "", 255, 1, 0);

    /**
     * access to configuration information
     */
    private static final Config config = Config.getInstance();

    /**
     * private default constructor prevents instantiation
     */
    private NcpPlusUtil() {
    }

    public static double[] calculateNCP_PLUS(final double[] ncp, final double[] prevZdr, final double[] currZdr, final double[] nextZdr) {
        double[] ncpPlus = new double[ncp.length];

        final int offset = config.getClutterWindowSize(); //size of window to look at
        for (int i = 0; i < ncp.length; ++i) { //Standard deviation of ZDR filter
            double total = 0;
            int count = 0; //the actual number of values averaged
            double[] grid = new double[offset * 3];
            for (int ii = i - offset / 2; ii < i + offset / 2 + 1; ++ii) {
                if (prevZdr != null && -1 < ii && ii < prevZdr.length) {
                    if (!Double.isNaN(prevZdr[ii])) total += grid[count++] = prevZdr[ii];
                }
                if (currZdr != null && -1 < ii && ii < currZdr.length) {
                    if (!Double.isNaN(currZdr[ii])) total += grid[count++] = currZdr[ii];
                }
                if (nextZdr != null && -1 < ii && ii < nextZdr.length) {
                    if (!Double.isNaN(nextZdr[ii])) total += grid[count++] = nextZdr[ii];
                }
            }

            double avg = total / count;
            total = 0;
            for (int ii = 0; ii < count; ++ii) total += Math.pow(grid[ii] - avg, 2);

            // If the difference in ZDR values is too high, the NCP is 0.
            ncpPlus[i] = Math.sqrt(total / count) > config.getClutterThreshold() ? Double.NaN : ncp[i];
        }

        double[] tmp = new double[ncpPlus.length];
        for (int i = 1; i < ncpPlus.length - 1; ++i) { //despeckle filter
            if (Double.isNaN(ncpPlus[i])) {
                tmp[i] = ncpPlus[i];
                continue;
            } //already considered bad
            tmp[i] = ncpPlus[i - 1] < 0.25 && ncpPlus[i + 1] < 0.25 ? Double.NaN : ncpPlus[i]; //bad on both sides => bad
        }
        ncpPlus = tmp;

        return ncpPlus;
    }
}
