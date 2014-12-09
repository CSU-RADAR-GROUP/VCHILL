package edu.colostate.vchill;

/**
 * This class allows for a single place to specify the current version of
 * Java VCHILL, which can be used in the About box as well as in the socket
 * code etc.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2007-12-05
 */
public final class Version {
    public static final int majorRevision = 4;
    public static final int minorRevision = 17;
    public static final int microRevision = 0;
    public static final String buildDate = "2014.12.04";
    public static final String string = "Version " +
            majorRevision + "." + minorRevision + "." + microRevision;

    /**
     * Private default constructor prevents instantiation
     */
    private Version() {
    }
}
