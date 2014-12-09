package edu.colostate.vchill.color;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ColorMapTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testInstantiation_interpolation() throws Exception {
        ColorMap cmap = new ColorMap(true);
        assertTrue(cmap.get_interpolation_value());

        cmap = new ColorMap(false);
        assertFalse(cmap.get_interpolation_value());
    }

}