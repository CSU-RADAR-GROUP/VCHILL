package edu.colostate.vchill.proxy;

import java.io.IOException;

import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.HdrUtil;
import edu.colostate.vchill.KdpUtil;
import edu.colostate.vchill.NcpPlusUtil;
import edu.colostate.vchill.RainUtil;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.chill.ChillGenRay;

/**
 * Performs calculations of hybrid data types.
 *
 * @author Alexander Deyke
 * @author Jochen Deyke
 * @version 2007-09-28
 */
class ProxyCalculationThread implements Runnable
{
    /** command to process */
    private ControlMessage command;

    /** shared cache */
    private CacheMain cache;

    public ProxyCalculationThread (final ControlMessage command, final CacheMain cache)
    {
        this.command = command;
        this.cache = cache;
    }

    public void start ()
    {
        new Thread(this, "ProxyCalculationThread").start();
    }

    /**
     * Creates and starts a thread for each type of calculation.
     */
    public void run ()
    {
        new Thread(new Runnable () { public void run () {
            try { calculateKDP(); }
            catch (IOException ioe) { ioe.printStackTrace(); }
        }}, "calculateKDP").start();
        new Thread(new Runnable () { public void run () {
            try { calculateHDR(); }
            catch (IOException ioe) { ioe.printStackTrace(); }
        }}, "calculateHDR").start();
        new Thread(new Runnable () { public void run () {
            try { calculateNCP_PLUS(); }
            catch (IOException ioe) { ioe.printStackTrace(); }
        }}, "calculateNCP_PLUS").start();
        new Thread(new Runnable () { public void run () {
            try { calculateRCOMP(); }
            catch (IOException ioe) { ioe.printStackTrace(); }
        }}, "calculateRCOMP").start();
    }

    /**
     * Calculates HDR using Z and ZDR.
     */
    public void calculateHDR () throws IOException
    {
        int currRayNumber = 0;
        while (true) {
            ChillGenRay currZ = (ChillGenRay)(cache.getDataWait(this.command, ChillFieldInfo.Z.fieldName, currRayNumber));
            if (currZ == null) break; //done
            ChillGenRay currZDR = (ChillGenRay)(cache.getDataWait(this.command, ChillFieldInfo.ZDR.fieldName, currRayNumber));
            if (currZDR == null) break; //done

            double[] dataZ = currZ.getData();
            double[] dataZDR = currZDR.getData();

            double[] dataHDR = HdrUtil.calculateHDR(dataZ, dataZDR);

            //store results
            cache.addRay(this.command, ChillFieldInfo.HDR.fieldName, new ChillGenRay(currZ.getHSKHeader(), currZ.getDataHeader(), ChillFieldInfo.HDR.fieldName, dataHDR));
            ++currRayNumber;
            Thread.yield();
        }
        System.out.println("ProxyCalculationThread: Marking Hdr completed; cached " + cache.getNumberOfRays(this.command, ChillFieldInfo.HDR.fieldName) + " rays");
        cache.setCompleteFlag(this.command, ChillFieldInfo.HDR.fieldName);
    }

    /**
     * Calculates NCP+ using NCP and ZDR.
     */
    public void calculateNCP_PLUS () throws IOException
    {
        ChillGenRay prevZDR = null;
        ChillGenRay currZDR = null;
        ChillGenRay nextZDR = null;
        ChillGenRay currNCP = null;

        int currRayNumber = 0;
        while (true) {
            currZDR = (ChillGenRay)(cache.getDataWait(this.command, ChillFieldInfo.ZDR.fieldName, currRayNumber));
            if (currZDR == null) break; //done
            nextZDR = (ChillGenRay)(cache.getDataWait(this.command, ChillFieldInfo.ZDR.fieldName, currRayNumber + 1));
            currNCP = (ChillGenRay)(cache.getDataWait(this.command, ChillFieldInfo.NCP.fieldName, currRayNumber));
            if (currNCP == null) break; //done

            double[] dataNCP = currNCP.getData();
            double[] dataprevZDR = prevZDR == null ? null : prevZDR.getData();
            double[] datacurrZDR = currZDR.getData();
            double[] datanextZDR = nextZDR == null ? null : nextZDR.getData();

            double[] dataNCP_PLUS = NcpPlusUtil.calculateNCP_PLUS(dataNCP, dataprevZDR, datacurrZDR, datanextZDR);

            cache.addRay(this.command, ChillFieldInfo.NCP_PLUS.fieldName, new ChillGenRay(currNCP.getHSKHeader(), currNCP.getDataHeader(), ChillFieldInfo.NCP_PLUS.fieldName, dataNCP_PLUS));
            prevZDR = currZDR;
            ++currRayNumber;
            Thread.yield();
        }
        System.out.println("ProxyCalculationThread: Marking NCP+ completed; cached " + cache.getNumberOfRays(this.command, ChillFieldInfo.NCP_PLUS.fieldName) + " rays");
        cache.setCompleteFlag(this.command, ChillFieldInfo.NCP_PLUS.fieldName);
    }

    /** Calculates KDP from PhiDP, using NCP_PLUS, RHO_HV and ZDR as filters */
    public void calculateKDP () throws IOException
    {
        int currRayNumber = 0;
        while (true) {
            ChillGenRay rayPHIDP = (ChillGenRay)(cache.getDataWait(this.command, ChillFieldInfo.PHIDP.fieldName, currRayNumber));
            if (rayPHIDP == null) break; //done
            ChillGenRay rayZ = (ChillGenRay)(cache.getDataWait(this.command, ChillFieldInfo.Z.fieldName, currRayNumber));
            if (rayZ == null) break; //done
            ChillGenRay rayRHOHV = (ChillGenRay)(cache.getDataWait(this.command, ChillFieldInfo.RHOHV.fieldName, currRayNumber));
            if (rayRHOHV == null) break; //done

            //these arrays contain raw/compressed data (0-255)
            double[] dataPHIDP = rayPHIDP.getData();
            double[] dataZ = rayZ.getData();
            double[] dataRHOHV = rayRHOHV.getData();
            double[] dataKDP = KdpUtil.calculateKDP(dataPHIDP, dataZ, dataRHOHV, rayPHIDP.getStartRange() * 1e-6, rayPHIDP.getGateWidth());

            //store result
            cache.addRay(this.command, ChillFieldInfo.KDP.fieldName, new ChillGenRay(rayPHIDP.getHSKHeader(), rayPHIDP.getDataHeader(), ChillFieldInfo.KDP.fieldName, dataKDP));
            ++currRayNumber;
            Thread.yield();
        }
        System.out.println("ProxyCalculationThread: Marking KDP completed; cached " + cache.getNumberOfRays(this.command, ChillFieldInfo.KDP.fieldName) + " rays");
        cache.setCompleteFlag(this.command, ChillFieldInfo.KDP.fieldName);
    }

    /**
     * Calculates RCOMP using KDP.
     */
    public void calculateRCOMP () throws IOException
    {
        //ControlMessage command = this.command.setType(ChillFieldInfo.RCOMP.fieldName);

        int currRayNumber = 0;
        while (true) {
            ChillGenRay rayKDP = (ChillGenRay)(cache.getDataWait(this.command, ChillFieldInfo.KDP.fieldName, currRayNumber));
            if (rayKDP == null) break; //done
            ChillGenRay rayZ = (ChillGenRay)(cache.getDataWait(this.command, ChillFieldInfo.Z.fieldName, currRayNumber));
            if (rayZ == null) break; //done
            ChillGenRay rayZdr = (ChillGenRay)(cache.getDataWait(this.command, ChillFieldInfo.ZDR.fieldName, currRayNumber));
            if (rayZdr == null) break; //done

            double[] dataKDP = rayKDP.getData();
            double[] dataZDR = rayZdr.getData();
            double[] dataZ = rayZ.getData();
            double[] rain = RainUtil.calculateCompositeRain(dataKDP, dataZ, dataZDR);

            //store result
            cache.addRay(this.command, ChillFieldInfo.RCOMP.fieldName, new ChillGenRay(rayKDP.getHSKHeader(), rayKDP.getDataHeader(), ChillFieldInfo.RCOMP.fieldName, rain));
            ++currRayNumber;
            Thread.yield();
        }
        System.out.println("ProxyCalculationThread: Marking RCOMP completed; cached " + cache.getNumberOfRays(this.command, ChillFieldInfo.RCOMP.fieldName) + " rays");
        cache.setCompleteFlag(this.command, ChillFieldInfo.RCOMP.fieldName);
    }
}
