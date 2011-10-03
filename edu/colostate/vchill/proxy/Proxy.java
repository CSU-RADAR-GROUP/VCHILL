package edu.colostate.vchill.proxy;

import edu.colostate.vchill.HdrUtil;
import edu.colostate.vchill.KdpUtil;
import edu.colostate.vchill.NcpPlusUtil;
import edu.colostate.vchill.RainUtil;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.chill.ChillFieldInfo;
import java.io.IOException;

/**
 * Abstract driver class for a proxy server.  Listens to socket requests and
 * creates new threads to handle them.
 *
 * @author Alexander Deyke
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public abstract class Proxy
{
    protected static final ScaleManager sm = ScaleManager.getInstance();

    /** shared cache */
    protected CacheMain cache;

    /** archive server to connect to */
    protected String serverName = "radar.chill.colostate.edu";
    protected int serverPort = 2510;

    /** port to listen on */
    protected int listenPort = 2510;

    /** idle timeout, defaults to 1 hour */
    protected long timeout = 3600000;

    /** size of cache */
    protected int cacheSize = ChillFieldInfo.types.length * 10;
    
    /** should the server calculate hybrid data types or not */
    protected boolean calcflag = true;

    /** password for shutting down the proxy */
    protected String password = "secret";

    /** should output be in gui rather than stdout/err? */
    protected boolean guiflag = false;

    /**
     * Puts the current thread to sleep for <code>millis</code> milliseconds.
     * This just calls Thread.sleep, but includes an empty catch block for the
     * possible InterruptedException.
     *
     * @param millis the number of milliseconds to sleep for
     */
    public static void sleep (final int millis)
    {
        try { Thread.sleep(millis); }
        catch (InterruptedException ie) {}
    }

    protected void addCalculatedTypeScales ()
    {
        if (calcflag) {
System.out.println("adding calculated data types to scale manager");
            sm.putScale(HdrUtil.scale);
            sm.putScale(NcpPlusUtil.scale);
            sm.putScale(KdpUtil.scale);
            sm.putScale(RainUtil.scale);
for (String type : sm.getTypes()) System.out.println(" - " + type);
System.out.println("done adding calculated data types to scale manager");
        }
    }

    public CacheMain getCache   () { return this.cache;      }
    public String getServerName () { return this.serverName; }
    public int getServerPort    () { return this.serverPort; }
    public long getTimeout      () { return this.timeout;    }
    public boolean getCalcFlag  () { return this.calcflag;   }

    public abstract void run () throws IOException;
}
