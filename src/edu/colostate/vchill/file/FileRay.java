package edu.colostate.vchill.file;

/**
 * Each instance of this class represents one Ray of one Sweep
 * (all data types) in the data file
 *
 * @author Jochen Deyke
 * @version 2005-04-07
 */
public class FileRay
{
    /** Keep a reference to the header to update when adding/removing types */
    public FileSKUHeader skuH;

    /** Angles etc stored here */
    public FileDataHeader dataH;

    /** The actual gate data (ragged array of arrays) */
    public byte[/*type*/][/*gate as byte(s)*/] data;
}
