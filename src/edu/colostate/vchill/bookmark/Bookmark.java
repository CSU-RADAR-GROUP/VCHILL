package edu.colostate.vchill.bookmark;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ControlMessage;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Information needed to recall a particular sweep, along with the associated commentary.
 * Some numbers are stored as <code>String</code>s to allow errorchecking to be moved to a more convenient place.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-07-07
 */
public class Bookmark
{
	/** Server:port */
    public String url;

    /** As passed to socket */
    public String dir;

    /** As passed to socket */
    public String file;

    /** As passed to socket */
    public String sweep;

    /** "PPI" or "RHI" or "MAN" */
    public String scan_type;

    /**
     * A <code>Scale</code> instance contains the scaling information for one field/data type
     */
    public static class Scale
    {
        /** If true, ignore <code>minval</code> and <code>maxval</code> and instead use values from a fixed set */
        public boolean autoscale;

        /** The minimum value to show when displaying the data */
        public String minval;

        /** The maximum value to show when displaying the data */
        public String maxval;

        public Scale () {}
        public Scale (final Scale other)
        {
            this.autoscale = other.autoscale;
            this.minval = other.minval;
            this.maxval = other.maxval;
        }
    }

    /** Scaling information for the various data types */
    public final Map<String, Scale> scale;

    //shared among all ppi/rhi images:
    /** New center of image (in km from radar) */
    public double x, y;

    /** Screen radius (km) - used to enlarge data */
    public double range;

    /** Distance between range rings (km) */
    public String ring;

    /** Maximum height (km) to plot to */
    public String rhi_height;

    /** Multiline description of bookmarked phenomenon, or URL of a web page containing the description */
    public String comment;

    /**
     * Creates a new (empty) <code>Bookmark</code> - fields will need to be set manually
     */
    public Bookmark ()
    {
        this.scale = new LinkedHashMap<String, Scale>(ChillDefines.MAX_NUM_TYPES);
        this.comment = "";
        this.dir = "";
        this.file = "";
        this.range = 0.0;
        this.rhi_height = "25";
        this.ring = "";
        this.scan_type = "";
        this.sweep = "";
        this.url = "";
        this.x = 0.0;
        this.y = 0.0;
    }

    /**
     * Copy an existing <code>Bookmark</code> into a new one
     */
    public Bookmark (final Bookmark other)
    {
        this();
        this.url = other.url;
        this.dir = other.dir;
        this.file = other.file;
        this.sweep = other.sweep;
        this.scan_type = other.scan_type;
        this.scale.putAll(other.scale);
        this.x = other.x;
        this.y = other.y;
        this.range = other.range;
        this.ring = other.ring;
        this.rhi_height = other.rhi_height;
        this.comment = other.comment;
    }

    /**
     * @return the <code>ControlMessage</code> needed to display this <code>Bookmark</code>
     */
    public ControlMessage getControlMessage ()
    {
        return new ControlMessage(this.url, this.dir, this.file, this.sweep);
    }

	@Override public String toString ()
    {
        return this.url +
            ControlMessage.separator + this.dir +
            ControlMessage.separator + this.file +
            ControlMessage.separator + this.sweep + " " + this.scan_type;
    }
}
