package edu.colostate.vchill.socket;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ChillDefines.Channel;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * CHILL format data structure for storing archive control packets
 * (as expected by the archive server)
 *
 * @author Jochen Deyke
 * @author Alexander Deyke
 * @version 2007-09-10
 */
public final class SocketArchCtl
{
    private static final int IN_FILE_LENGTH = 100;

    public static final int BYTE_SIZE = //116
        ChillDefines.INT_BYTE_SIZE +
        ChillDefines.SHORT_BYTE_SIZE * 4 +
        ChillDefines.INT_BYTE_SIZE +
        IN_FILE_LENGTH;

    /** What do we want from the server? */
    public enum Command {
        /** not used */
        unknown,

        /** not used by Java VCHILL */
        RAY_MODE,

        /** request data for a sweep from a file */
        SWEEP_MODE,

        /** not used by Java VCHILL */
        VOLUME_MODE,

        /** not used by Java VCHILL */
        NOSTOP_MODE,

        /** get details on a file */
        STATUS_REQ,

        /** ask server to stop sending current sweep */
        HALT_COMMAND,

        /** not used by Java VCHILL */
        SWEEP_SEEK,

        /** list directories, files, both, or bookmarks available*/
        DIRECTORY_REQ,

        /** initial connection to the server */
        CONNECT_REQ,

        /** close session with server */
        DISCONNECT_REQ,
    }

    /** Subtypes for Command.DIRECTORY_REQ requests */
    public enum DirType {
        /** not used */
        unknown,

        /** no longer used - now using CONTENTS instead */
        DIRECTORIES,

        /** no longer used - now using CONTENTS instead */
        FILES,

        /** no longer used - now using xml based bookmarks from webserver instead */
        BOOKMARKS,

        /** generic for files and subdirs */
        CONTENTS,
    }
    
    /** type of request */
    private final Command archMode;

    /** starting sweep number: 0=start from current */
    private final short startSweep;

    /** num rays to read */
    private final short rayStep;

    /** sweep filter (not used?) */
    private final short sweepLow, sweepHigh;

    /** added delay between reads (always 0) */
    private final int extraDelay;

    /** ascii filename, length = 100 */
    private final byte inFile[];

    /**
     * Sole constructor
     *
     * @param archMode the type of request
     * @param startSweep the starting sweep number, 0 = start from current
     * @param rayStep the number of rays to read
     * @param sweepLow sweep filter (not currently used)
     * @param sweepHigh sweep filter (not currently used)
     * @param extraDelay added delay between reads (always 0)
     * @param inFile filename, maximum 99 characters (plus terminating 0)
     */
    public SocketArchCtl (
            final Command archMode,
            final short   startSweep,
            final short   rayStep,
            final short   sweepLow,
            final short   sweepHigh,
            final int     extraDelay,
            final String  inFile)
    {
        this.archMode   = archMode;
        assert archMode != Command.unknown;
        this.startSweep = startSweep;
        this.rayStep    = rayStep;
        this.sweepLow   = sweepLow;
        this.sweepHigh  = sweepHigh;
        this.extraDelay = extraDelay;
        this.inFile = new byte[IN_FILE_LENGTH];
        try { //translate String to byte[]
            byte[] tmp = inFile.getBytes("US-ASCII");
            if( tmp.length >= IN_FILE_LENGTH ) {
                throw new Error( "inFile too long" );
            }
            for (int i = 0; i < IN_FILE_LENGTH; ++i) {
                this.inFile[i] = ((i < tmp.length) ? tmp[i] : 0);
            }
        } catch (UnsupportedEncodingException uee) { throw new Error(uee); }
    }

    /**
     * @param in the DataInput to load values from
     */
    public SocketArchCtl (final DataInput in)
    { try {
        this.archMode   = Command.values()[in.readInt()];
        this.startSweep = in.readShort();
        this.rayStep    = in.readShort();
        this.sweepLow   = in.readShort();
        this.sweepHigh  = in.readShort();
        this.extraDelay = in.readInt();
        in.readFully(this.inFile = new byte[IN_FILE_LENGTH]);
    } catch (IOException ioe) { throw new Error(ioe); }}

    /**
     * @param data the byte[] to load values from
     * @param offset the index of the first byte to read
     */
    public SocketArchCtl (final byte[] data, int offset)
    {
        if( data.length < offset + this.BYTE_SIZE - 1 ) {
            throw new IllegalArgumentException( "not enough data in input array" );
        }
        this.archMode   = Command.values()[SocketUtil.readInt(data, offset)];   offset += ChillDefines.INT_BYTE_SIZE;
        this.startSweep = SocketUtil.readShort(data, offset); offset += ChillDefines.SHORT_BYTE_SIZE;
        this.rayStep    = SocketUtil.readShort(data, offset); offset += ChillDefines.SHORT_BYTE_SIZE;
        this.sweepLow   = SocketUtil.readShort(data, offset); offset += ChillDefines.SHORT_BYTE_SIZE;
        this.sweepHigh  = SocketUtil.readShort(data, offset); offset += ChillDefines.SHORT_BYTE_SIZE;
        this.extraDelay = SocketUtil.readInt(data, offset);   offset += ChillDefines.INT_BYTE_SIZE;
        this.inFile = new byte[IN_FILE_LENGTH];
        for (int i = 0; i < IN_FILE_LENGTH; ++i) this.inFile[i] = data[offset++];
        assert offset == this.BYTE_SIZE : "number of bytes read != size";
    }

    public SocketArchCtl (final byte[] data)
    {
        this(data, 0);
    }

    /**
     * @param out the DataOutput to write values to
     */
    public void write (final DataOutput out)
    { try {
        out.writeInt(this.archMode.ordinal());
        out.writeShort(this.startSweep);
        out.writeShort(this.rayStep);
        out.writeShort(this.sweepLow);
        out.writeShort(this.sweepHigh);
        out.writeInt(this.extraDelay);
        out.write(this.inFile);
    } catch (IOException ioe) { throw new Error(ioe); }}

    /**
     * Translates this SocketArchCtl object to a byte[] for transmission over the network 
     *
     * @return byte[] representation of this SocketArchCtl object
     */
    public byte[] getBytes ()
    {
        final byte[] output = new byte[this.BYTE_SIZE];
        int offset = 0;
        offset += SocketUtil.writeInt(this.archMode.ordinal(), output, offset);
        offset += SocketUtil.writeShort(this.startSweep, output, offset);
        offset += SocketUtil.writeShort(this.rayStep, output, offset);
        offset += SocketUtil.writeShort(this.sweepLow, output, offset);
        offset += SocketUtil.writeShort(this.sweepHigh, output, offset);
        offset += SocketUtil.writeInt(this.extraDelay, output, offset);
        offset += SocketUtil.copyBytes(this.inFile, 0, output, offset, IN_FILE_LENGTH);
        assert offset == this.BYTE_SIZE : "Num bytes returned != size";
        return output;
    }

    /**
     * Translates this SocketArchCtl object, prefaced by a command code identifing this as the control channel, to a byte[] for transmission over the network 
     * @return byte[] representation of this SocketArchCtl object prefaced by ARCH_CTL_CHANNEL
     */
    public byte[] prepareFirstPacket ()
    {
        final byte[] output = new byte[this.BYTE_SIZE + ChillDefines.INT_BYTE_SIZE];
        int offset = 0;
        offset += SocketUtil.writeInt(Channel.ARCH_CTL.ordinal(), output, offset);
        offset += SocketUtil.writeInt(this.archMode.ordinal(), output, offset);
        offset += SocketUtil.writeShort(this.startSweep, output, offset);
        offset += SocketUtil.writeShort(this.rayStep, output, offset);
        offset += SocketUtil.writeShort(this.sweepLow, output, offset);
        offset += SocketUtil.writeShort(this.sweepHigh, output, offset);
        offset += SocketUtil.writeInt(this.extraDelay, output, offset);
        offset += SocketUtil.copyBytes(this.inFile, 0, output, offset, IN_FILE_LENGTH);
        assert offset == this.BYTE_SIZE + ChillDefines.INT_BYTE_SIZE : "Num bytes returned != size + ChillDefines.INT_BYTE_SIZE";
        return output;
    }
    
    public Command getArchMode   () { return archMode;   }
    public short   getStartSweep () { return startSweep; }
    public short   getRayStep    () { return rayStep;    }
    public short   getSweepLow   () { return sweepLow;   }
    public short   getSweepHigh  () { return sweepHigh;  }
    public int     getExtraDelay () { return extraDelay; }
    public String  getInFile     () { return new String(inFile).trim(); }
}
