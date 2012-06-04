package edu.colostate.vchill.map;

/**
 * Each instance of this class represents one instruction used for rendering
 * the vector graphics map display.
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class MapInstruction
{
	/** The available shapes that a map instruction can be. */
    public enum Shape {
        /** The start point of a line */
        START_LINE,
        /** The end point of a line - may also be the start point of an additional line */
        CONT_LINE,
        /** Reserved for future use */
        unknown2,
        /** Reserved for future use */
        unknown3,
        /** A single point of interest - the comment may be used as a label */
        POINT,
        /** A circle - treat x as radius and PREVIOUS instruction's x,y as center */
        CIRCLE,
    }

    private final double x;
    private final double y;
    private final Shape type;
    private final String comment;

    public MapInstruction (final double x, final double y, final Shape type, final String comment)
    {
        this.x = x;
        this.y = y;
        this.type = type;
        this.comment = comment;
    }

    public double getX ()
    {
        return this.x;
    }

    public double getY ()
    {
        return this.y;
    }

    public Shape getType ()
    {
        return this.type;
    }

    public String getComment ()
    {
        return this.comment;
    }

    @Override public String toString ()
    {
        return this.x + " " + this.y + " " + this.type + " " + this.comment;
    }
}
