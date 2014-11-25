package edu.colostate.vchill;

import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.chill.ChillMomentFieldScale;

/**
 * Utility methods for calculating HDR
 *
 * @author Jochen Deyke
 * @version 2007-01-26
 */
public class HdrUtil {
    public static final ChillMomentFieldScale scale = new ChillMomentFieldScale(ChillFieldInfo.HDR, java.awt.event.KeyEvent.VK_H, "db", 107, 170, -1564);

    /**
     * private default constructor prevents instantiation
     */
    private HdrUtil() {
    }

    public static double[] calculateHDR(final double[] dataZ, final double[] dataZDR) {
        double[] dataHDR = new double[dataZ.length];

        // Do the calculations for each gate.
        for (int i = 0; i < dataHDR.length; ++i) {
            if (Double.isNaN(dataZ[i]) || Double.isNaN(dataZDR[i])) dataHDR[i] = Double.NaN; //missing data
            else dataHDR[i] = dataZ[i] - ((dataZDR[i] > 0) ? ((dataZDR[i] > 1.74) ? 60 : (19 * dataZDR[i] + 27)) : 27);
        }

        return dataHDR;
    }
}
