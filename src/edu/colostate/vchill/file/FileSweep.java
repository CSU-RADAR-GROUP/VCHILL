package edu.colostate.vchill.file;

import java.util.LinkedList;

/**
 * Each instance of this class represents one complete Sweep
 * (all Rays) in the data file
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2010-08-30
 */
public class FileSweep {
    /**
     * Keep a reference to the header to update when adding/removing types
     */
    public FileSKUHeader skuH;

    /**
     * Sweepwide scan settings
     */
    public FileParameterData paramD;

    /**
     * Scaling information
     */
    public FileFieldScalingInfo[/*type*/] fieldScalings;

    /**
     * contains Ray objects
     */
    public LinkedList<FileRay> data;

    /**
     * sole constructor
     */
    public FileSweep() {
        this.data = new LinkedList<FileRay>();
    }
}
