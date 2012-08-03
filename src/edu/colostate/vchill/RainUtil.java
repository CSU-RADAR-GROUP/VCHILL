package edu.colostate.vchill;

import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.chill.ChillMomentFieldScale;

/**
 * Utility methods for calculating rain rate
 *
 * @author Jochen Deyke
 * @version 2006-03-15
 */
public class RainUtil
{
    public static final ChillMomentFieldScale scale = new ChillMomentFieldScale(ChillFieldInfo.RCOMP, java.awt.event.KeyEvent.VK_C, "mm/h", 1, 1, 0);

    /** private default constructor prevents instantiation */
    private RainUtil () {}

    public static double[] calculateRK40 (final double[] dataKDP)
    {
        double[] dataRK40 = new double[dataKDP.length];

        for (int gate = 0; gate < dataKDP.length; ++gate) {
            dataRK40[gate] = 40.5 * Math.pow(dataKDP[gate], 0.85);
        }

        return dataRK40;
    }

    public static double[] calculateRK50 (final double[] dataKDP)
    {
        double[] dataRK50 = new double[dataKDP.length];

        for (int gate = 0; gate < dataKDP.length; ++gate) {
            dataRK50[gate] = 50.7 * Math.pow(dataKDP[gate], 0.85);
        }

        return dataRK50;
    }

    public static double[] calculateRKZdr (final double[] dataKDP, final double[] dataZDR)
    {
        double[] dataRKZdr = new double[dataKDP.length];

        for (int gate = 0; gate < dataKDP.length; ++gate) {
            dataRKZdr[gate] = 90.8 * Math.pow(dataKDP[gate], 0.93) * Math.pow(10, -0.169 * dataZDR[gate]);
        }

        return dataRKZdr;
    }

    public static double[] calculateRZZdr (final double[] dataZ, final double[] dataZDR)
    {
        double[] dataRZZdr = new double[dataZ.length];

        for (int gate = 0; gate < dataZ.length; ++gate) {
            double gateZ = Math.pow(10, dataZ[gate] / 10); // -> linear
            dataRZZdr[gate] = 6.7e-3 * Math.pow(gateZ, 0.927) * Math.pow(10, -0.3433 * dataZDR[gate]);
        }

        return dataRZZdr;
    }

    public static double[] calculateRZ (final double[] dataZ)
    {
        double[] dataRZ = new double[dataZ.length];

        for (int gate = 0; gate < dataZ.length; ++gate) {
            double gateZ = dataZ[gate];
            if (gateZ > 53) gateZ = 53; //threshold
            gateZ = Math.pow(10, gateZ / 10); // -> linear
            dataRZ[gate] = Math.pow(gateZ / 300, 1 / 1.4);
        }

        return dataRZ;
    }

    public static double[] calculateCompositeRain (final double[] kdp, final double[] dbz, final double[] zdr)
    {
        double[] rk40 = RainUtil.calculateRK40(kdp);
        double[] rk50 = RainUtil.calculateRK50(kdp);
        double[] rkzdr = RainUtil.calculateRKZdr(kdp, zdr);
        double[] rzzdr = RainUtil.calculateRZZdr(dbz, zdr);
        double[] rz = RainUtil.calculateRZ(dbz);

        double[] rain = new double[kdp.length];
        for (int i = 0; i < rain.length; ++i) {
            double kdpThresh = 0;

            double sum = kdp[i] > kdpThresh ? rk40[i] + rk50[i] + rkzdr[i] : 0;
            int count = kdp[i] > kdpThresh ? 3 : 0;
            sum += rzzdr[i] + rz[i];
            count += 2;
            double avg = sum / count;
            double stdDev = Math.sqrt((
                kdp[i] > kdpThresh ? (
                    Math.pow(rk40[i] - avg, 2) +
                    Math.pow(rk50[i] - avg, 2) +
                    Math.pow(rkzdr[i] - avg, 2)
                ) : 0 +
                Math.pow(rzzdr[i] - avg, 2) +
                Math.pow(rz[i] - avg, 2)) / count);
            sum = 0; count = 0;
            if (kdp[i] > kdpThresh) {
                if (Math.abs(rk40[i]  - avg) < 1.5 * stdDev) { sum += rk40[i];  ++count; }
                if (Math.abs(rk50[i]  - avg) < 1.5 * stdDev) { sum += rk50[i];  ++count; }
                if (Math.abs(rkzdr[i] - avg) < 1.5 * stdDev) { sum += rkzdr[i]; ++count; }
            }
            if (Math.abs(rzzdr[i] - avg) < 1.5 * stdDev) { sum += rzzdr[i]; ++count; }
            if (Math.abs(rz[i]    - avg) < 1.5 * stdDev) { sum += rz[i];    ++count; }
            //if (i % 100 == 0) System.out.println("count = " + count);
            
            rain[i] = count > 0 ? sum / count : 0;
        }
        return rain;
    }
}
