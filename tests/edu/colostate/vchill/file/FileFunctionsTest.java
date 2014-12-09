package edu.colostate.vchill.file;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileFunctionsTest {

    @Before
    public void setUp() throws Exception {

    }


    @Test
    public void testIsCFRadial() throws Exception {
        assertTrue(FileFunctions.isCFRadial("test.cf"));
        assertTrue(FileFunctions.isCFRadial("testnc.cf"));
        assertTrue(FileFunctions.isNetCDF("test.nc.gz"));


        assertFalse(FileFunctions.isCFRadial("test.nc"));
        assertFalse(FileFunctions.isCFRadial("test.cf.nc"));
        assertFalse(FileFunctions.isCFRadial("test.RAW"));
        assertFalse(FileFunctions.isCFRadial("test.CHL"));
        assertFalse(FileFunctions.isCFRadial("test.chl"));
    }

    @Test
    public void testIsIRISRAW() throws Exception {
        assertTrue(FileFunctions.isIRISRAW("test.RAW1231"));

        assertFalse(FileFunctions.isIRISRAW("test.cf"));
        assertFalse(FileFunctions.isIRISRAW("test.CHL"));
        assertFalse(FileFunctions.isIRISRAW("test.chl"));
    }

    @Test
    public void testIsCHILL() throws Exception {
        assertTrue(FileFunctions.isCHILL("CHLtest.chl"));
        assertTrue(FileFunctions.isCHILL("CHLtest.CHL"));


        assertFalse(FileFunctions.isCHILL("test.cf"));
        assertFalse(FileFunctions.isCHILL("test.RAW"));

        assertFalse(FileFunctions.isCHILL("test.cdet"));
    }

    @Test
    public void testIsNetCDF() throws Exception {
        assertTrue(FileFunctions.isNetCDF("test.nc"));
        assertTrue(FileFunctions.isNetCDF("test.netcdf"));

        assertFalse(FileFunctions.isNetCDF("test.cf"));
        assertFalse(FileFunctions.isNetCDF("test.RAW"));
        assertFalse(FileFunctions.isNetCDF("test.CHL"));
        assertFalse(FileFunctions.isNetCDF("test.chl"));

    }
}