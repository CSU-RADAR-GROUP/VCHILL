package edu.colostate.vchill.plot;

import edu.colostate.vchill.data.Ray;
import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

/**
 * This is the plotting method for Time vs Height plots
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-06-26
 */
class ViewPlotMethodTH extends ViewPlotMethod
{
    private static final int BLOCKSIZE = 1; //used to be zoom based
    protected int prevX;

    public ViewPlotMethodTH (final String type)
    {
        super(type);
        this.prevX =  0 - BLOCKSIZE;
        this.Mappable = false;
    }

    @Override protected double getStartAngle (final Ray currRay) { return 0; }
    @Override protected double getEndAngle (final Ray currRay) { return 0; }

    @Override protected int getX (final Angle angle, final double offset)
    {
        //won't work - should only set prevX on last gate of ray
        return prevX  = (prevX + BLOCKSIZE) % this.width;
    }

    @Override protected int getY (final Angle angle, final double offset, final int xPos)
    {
        //won't work - don't have neccessary info
        return 0;
    }

	/**
	 * Gets the desired ray number from the specified
	 * x, y location. It returns -1 if it can't find the ray.
	 */
	@Override public int getRayNumFromXY (int x, int y)
	{
		return x / BLOCKSIZE;
	}

	/**
	 * The maximum number of rays displayable. It returns
	 * -1 if it can't determine the number of rays.
	 */
	@Override public int getMaxDisplayableRays ()
	{
		return this.width / BLOCKSIZE;
	}

    @Override public void plotData (final Ray prevRay, final Ray currRay, final Ray nextRay, final Ray threshRay, final Graphics g)
    {
        if( currRay == null ) {
            throw new IllegalArgumentException( "Error: PlotMethodTH plotData(): null for data" );
        }

        radarAzimuth = currRay.getStartAzimuth();
        radarElevation = currRay.getStartElevation();
        metersPerGate = currRay.getGateWidth() * 1e3;
        numGates = currRay.getNumberOfGates();
        setTime(currRay.getDate());
        startRange = currRay.getStartRange() * 1e-6; //mm -> km

        double plotStepSize = config.getPlotRange() * BLOCKSIZE / (this.height * currRay.getGateWidth());

        int x  = (prevX + BLOCKSIZE) % this.width;
        prevX = x;

        double prevValue = Double.NaN;

		int pixelOffset = 0;
        double gateOffset = startRange / (metersPerGate / 1000.0F);
		if( gateOffset < 0 )
		{
			gateOffset = Math.abs( gateOffset );
		}
		else
		{
			gateOffset = 0;
			pixelOffset = getPixelsFromKm(startRange); //km -> px
		}

        g.setColor(Color.BLACK);
        g.fillRect(x, 0, BLOCKSIZE, this.height);
        List<Color> colors = getColors();
        ViewPlotDataFilter filter = new ViewPlotDataFilter(prevRay, currRay, nextRay, threshRay, this.type);
        for (int i = 0; i < this.height; ++i) {
            int y = this.height - (i + pixelOffset);
            int k = (int)(i * plotStepSize + gateOffset);
            if (k >= numGates) break;

            //apply filters
            double filteredValue = filter.applyFilters(k, plotStepSize, getElevationInKm(0 /*not used*/, y), prevValue);
            Color colorValue = getColorValue(filteredValue, colors);
            prevValue = filteredValue;

            g.setColor(colorValue);
            g.drawLine(x, y, x + BLOCKSIZE, y);
        } //end for
    }

    @Override public void plotGrid (final Graphics g)
    {
        int currentDistance = 0;
        int height;

        while (currentDistance <= config.getPlotRange()) {
            height = this.height - getPixelsFromKm(currentDistance);
            g.drawLine(0, height, this.width, height);
            g.drawString(String.valueOf(currentDistance),
                    2,
                    height + 11);
            currentDistance += config.getGridSpacing();
        }
    }

    @Override protected int getPixelsFromKm (final double km)
    {
        return (int)((BLOCKSIZE * km * this.height) / config.getPlotRange());
    }

    @Override protected double getKmFromPixels (final int numPixels)
    {
        return (config.getPlotRange() * numPixels) / (BLOCKSIZE  * this.height);
    }

    @Override public void plotClickPoint (final Graphics g)
    {
        g.setColor(Color.WHITE);
        int height = this.height - getPixelsFromKm(clickRng);
        g.drawLine(0, height, this.width, height);
    }

    //used for getting display value for click point
    @Override public double getClickAz () { return radarAzimuth; }
    @Override public double getClickEl () { return radarElevation; }

    @Override public int getOriginX () { return 0; }
    @Override public int getOriginY () { return this.height; }

    @Override public double getRangeInKm (final int x, final int y) {
        return getKmFromPixels(this.height - y); }

    @Override public String getPlotMode () {
        return "MAN"; }

    @Override public double getKmEast (final int x, final int y) {
        return 0; }
    @Override public double getKmNorth (final int x, final int y) {
        return 0; }

    @Override public int getPixelsX (final double kmEast, final double kmNorth) {
        return 0; }
    @Override public int getPixelsY (final double kmEast, final double kmNorth) {
        return 0; }

    @Override public double getAzimuthDegrees (final int x, final int y)
	{
		Object[] rayInfo = vc.getRay( this, this.type, x, y );
		if( rayInfo == null )
			return radarAzimuth;
		else
			return ((Ray) rayInfo[0]).getStartAzimuth();
	}
    @Override public double getElevationDegrees (final int x, final int y)
	{
        Object[] rayInfo = vc.getRay( this, this.type, x, y );
		if( rayInfo == null )
			return radarElevation;
		else
			return ((Ray) rayInfo[0]).getStartElevation();
	}

    @Override public double getElevationInKm (final int x, final int y) {
        return getKmFromPixels(this.height - y); }

    @Override public void setNewPlot ()
    {
        super.setNewPlot();
        this.prevX =  0 - BLOCKSIZE;
    }
}
