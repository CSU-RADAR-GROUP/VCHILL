package edu.colostate.vchill.file;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: jhardin
 * Date: 7/31/13
 * Time: 1:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileFunctionsTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testIsIRISRAW() throws Exception {
    }

    @Test
    public void testIsNetCDF() throws Exception {
        assertTrue(FileFunctions.isNetCDF("test.nc"));
        assertFalse(FileFunctions.isNetCDF("test.raw"));
    }
}
