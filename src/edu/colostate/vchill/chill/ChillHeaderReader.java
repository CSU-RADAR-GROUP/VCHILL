package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.ViewControl;
import edu.colostate.vchill.ViewControlThread;
import edu.colostate.vchill.cache.CacheMain;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * This class reads Chill headers into a cache.
 * If a data header is encountered, it calls back the data handler.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2008-08-25
 */
public abstract class ChillHeaderReader
{
    private static final ScaleManager sm = ScaleManager.getInstance();
    private static final ViewControlThread vct = ViewControl.getInstance().getViewControlThread();
    protected DataInputStream in;
    protected CacheMain cache;
    protected ChillHSKHeader hskH;

    /**
     * Constructs a ChillHeaderReader.
     * @param in the DataInputStream to read from
     * @param cache the CacheMain to save the headers to
     */
    public ChillHeaderReader (final DataInputStream in, final CacheMain cache)
    {
        this.in = in;
        this.cache = cache;
    }

    /**
	 * Read a header from the input stream into the cache.
	 * 
	 * @param command the ControlMessage to save the headers under
	 * @return was data read successful?  Will return false if non-data header read
	 */
    public boolean readHeader (final ControlMessage command) throws IOException
    {
        ChillHeaderHeader headerH = new ChillHeaderHeader(this.in);
//System.out.println("getting header 0x" + Integer.toHexString(headerH.recordType));
        switch (headerH.recordType) {
            case ChillDefines.GEN_MOM_DATA:
                ChillDataHeader dataH = new ChillDataHeader(this.in, headerH);
                sm.setAvailable(dataH.availableData);
                if (!readData(dataH, command)) return false; //error reading data - should not be possible
                cache.addRay(command, ChillDefines.META_TYPE, dataH); //add metadata AFTER ray data so plot can avoid Wait
                return true;
            case ChillDefines.BRIEF_HSK_DATA:
                this.hskH = new ChillHSKHeader(this.in, headerH);
                hskHupdated();
                cache.addRay(command, ChillDefines.META_TYPE, this.hskH);
                break;
            case ChillDefines.FIELD_SCALE_DATA:
                ChillMomentFieldScale scale = new ChillMomentFieldScale(this.in, headerH);
                cache.addRay(command, ChillDefines.META_TYPE, scale);
                sm.putScale(scale);
                break;
            case ChillDefines.TRACK_DATA:
                ChillTrackInfo track = new ChillTrackInfo(this.in, headerH);
		ChillHeaderReader.vct.handleAircraft( track );
                cache.addRay(command, ChillDefines.META_TYPE, track);
                break;
            case ChillDefines.OLD_EXT_TRACK_DATA:
                ChillOldExtTrackInfo oldexttrack = new ChillOldExtTrackInfo(this.in, headerH);
		ChillHeaderReader.vct.handleAircraft( oldexttrack );
                cache.addRay(command, ChillDefines.META_TYPE, oldexttrack);
                break;
            case ChillDefines.NEW_EXT_TRACK_DATA:
                ChillNewExtTrackInfo newexttrack = new ChillNewExtTrackInfo(this.in, headerH);
		ChillHeaderReader.vct.handleAircraft( newexttrack );
                cache.addRay(command, ChillDefines.META_TYPE, newexttrack);
                break;
            case ChillDefines.HSK_ID_RADAR_INFO:
                ChillRadarInfo radinf = new ChillRadarInfo(this.in, headerH);
                cache.addRay(command, ChillDefines.META_TYPE, radinf);
                break;
            case ChillDefines.HSK_ID_PROCESSOR_INFO:
                ChillProcessorInfo procinf = new ChillProcessorInfo(this.in, headerH);
                cache.addRay(command, ChillDefines.META_TYPE, procinf);
                break;
            case ChillDefines.HSK_ID_PWR_UPDATE:
                ChillPowerUpdate pwrup = new ChillPowerUpdate(this.in, headerH);
                cache.addRay(command, ChillDefines.META_TYPE, pwrup);
                break;
            case ChillDefines.HSK_ID_SCAN_SEG:
                ChillScanSeg scanseg = new ChillScanSeg(this.in, headerH);
                cache.addRay(command, ChillDefines.META_TYPE, scanseg);
                break;
            case ChillDefines.HSK_ID_END_NOTICE:
                ChillEndNotice endnotice = new ChillEndNotice(this.in, headerH);
                if (endnotice.isStart()) startNotice();
                if (endnotice.isEnd()) endNotice(command);
                cache.addRay(command, ChillDefines.META_TYPE, endnotice);
                break;
            case ChillDefines.HSK_ID_XMIT_INFO:
                ChillXmitInfo xmitinfo = new ChillXmitInfo(this.in, headerH);
                cache.addRay(command, ChillDefines.META_TYPE, xmitinfo);
                break;
            default:
                System.out.println("Don't know how to handle header of type 0x" + Integer.toHexString(headerH.recordType));
                ChillHeader generic = new ChillHeader(this.in, headerH);
                cache.addRay(command, ChillDefines.META_TYPE, generic);
                break;
        }
        return false;
    }

    /**
     * Called when a DataHeader is encountered
     * @return was data successfully read
     */
    public abstract boolean readData (final ChillDataHeader dataH, final ControlMessage metaCommand) throws IOException;
    /** Called when an EndNotice signaling the start of a sweep */
    public void startNotice () { }
    /** Called when an EndNotice signaling end of sweep or volume is encountered */
    public void endNotice (final ControlMessage metaCommand) { }
    /** Called when a HSKHeader is encountered */
    public void hskHupdated () { }
}
