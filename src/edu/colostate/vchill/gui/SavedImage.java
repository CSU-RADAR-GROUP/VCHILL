package edu.colostate.vchill.gui;

/**
 * Each instance of this class represents one saved image
 * for storing in the cyclic image buffer.  In addition to
 * the actual image, some information about the origin of
 * the image is stored as well.
 *
 * @author Jochen Deyke
 * @version 2004-12-09
 */
public final class SavedImage<E>
{
    private final E image;
    private final String description;

    public SavedImage (final E image, final String description)
    {
        this.image = image;
        this.description = description;
    }

    public E getImage ()
    {
        return this.image;
    }

    public String getDescription ()
    {
        return this.description;
    }
}
