package edu.colostate.vchill.plot;

import edu.colostate.vchill.Config;
import edu.colostate.vchill.EstimateParser;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.data.Ray;

/**
 * Applies filters to data values for plotting.
 * Usually, one ViewPlotDataFilter object is created per plot reuqest
 * (ie. per ray of a data type).
 *
 * @author Jochen Deyke
 * @author jpont
 * @version 2009-03-19
 */
public class ViewPlotDataFilter
{ 
    protected final static Config config = Config.getInstance();
    protected final static ScaleManager sm = ScaleManager.getInstance();
    
    private Ray prevRay;
    private Ray currRay;
    private Ray nextRay;
    private Ray threshRay;
    
    private double[] prevData;
    private double[] currData;
    private double[] nextData;
    private double[] threshData;
    
    private ChillMomentFieldScale scale;

	/* The gate number that the unfolding algorithm should start at. */
	private int unfoldStartGate;

	/* The last good velocity estimate. */
	private double lastGoodEstimate;
	/* The distance from the last good estimate. */
	private int distFromLastGoodEstimate;
    
    /** Creates a new instance of ViewPlotDataFilter */
    public ViewPlotDataFilter (final Ray prevRay, final Ray currRay, final Ray nextRay, final Ray threshRay, final String type)
    {
        this.prevRay = prevRay;
        this.currRay = currRay;
        this.nextRay = nextRay;
        this.threshRay = threshRay;
        
        prevData = prevRay != null ? prevRay.getData() : null;
        currData = currRay != null ? currRay.getData() : null;
        nextData = nextRay != null ? nextRay.getData() : null;
        threshData = threshRay != null ? threshRay.getData() : null;
        
        this.scale = sm.getScale(type);

		/* Determine the gate number that the unfolding algorithm should start at. */
		unfoldStartGate = (int) (config.getUnfoldStartRange() / currRay.getGateWidth());
		lastGoodEstimate = Double.NaN;
		distFromLastGoodEstimate = 0;
    }
    
    public double applyFilters (final double i, final double plotStepSize, final double elevation, final double prevValue)
    {
        if (currData == null || this.scale == null) return Double.NaN; //no data
        int k = (int)i;
        double currValue = currData[k]; //current data value
        if (Double.isNaN(currValue)) {
			distFromLastGoodEstimate++;
			return Double.NaN;
		} //missing data
        if (this.checkThresholdNotOK(k)) return Double.NaN; //threshold filter
        if (config.isNoiseReductionEnabled()) { //noise filter
			if (config.isThresholdEnabled()) {
				if (this.checkThresholdNotOK((int)(i - plotStepSize)) &&
					this.checkThresholdNotOK((int)(i + plotStepSize))) {
					return Double.NaN; //if both surrounding pixels are bad, so is this one
				}
			} else {
				int prevIndex = (int) (i - plotStepSize);
				int nextIndex = (int) (i + plotStepSize);
				if ((prevIndex < 0 || Double.isNaN(this.currData[prevIndex])) &&
					(nextIndex >= currData.length || Double.isNaN(this.currData[nextIndex]))) {
					return Double.NaN; //if both surrounding pixels are bad, so is this one
				}
			}
        }

        if (config.isUnfoldingEnabled() && scale.isUnfoldable() && k >= unfoldStartGate) { //velocity unfolding
            double vEst = config.isUnfoldingAutomatic() ? prevValue :
                EstimateParser.getInstance().getVEstimate(elevation);
			if( !Double.isNaN(vEst) ) {
				lastGoodEstimate = vEst;
				distFromLastGoodEstimate = 0;
			} else {
				if( distFromLastGoodEstimate < 5 ) { //don't let small groupings of NaNs affect unfolding
					vEst = lastGoodEstimate;
				} else {
					distFromLastGoodEstimate++;
					return currValue / 2;
				}
			}
            return this.getUnfoldedValue(currValue, vEst, currRay.getVelocityRange());
        } else if (scale.isGradientable() && config.isGradientEnabled()) { //Z as gradient
            return this.getGradientValue(k);
        }

        if (config.isSmoothingEnabled()) return this.getSmoothedValue(k);                      
        return currValue;
    }
    
    /**
     * Checks the threshold value at a given index.
     *
     * @param index desired index
     * @return true if the data is missing or bad, false if it is OK
     */
    public boolean checkThresholdNotOK (final int index)
    {
        if (config.isThresholdEnabled() && threshData != null) {
            if (-1 < index && index < threshData.length) {
                double threshVal = threshData[index];
                if (Double.isNaN(threshVal)) return true; //"missing" threshold value -> bad
                if (config.isThresholdAbsoluteValue()) threshVal = Math.abs(threshVal);
                double cutoff = config.getThresholdFilterCutoff();
                if (config.isThresholdGreaterThan()) return threshVal > cutoff;
                return threshVal < cutoff;
            }
        }
        return false;
    }

    /**
     * @param index index of current gate
     * @return The smoothed value
     */
    public double getSmoothedValue (final int index)
    {
        double translatedValue = Double.NaN;

        if (currData == null) throw new IllegalArgumentException("Null for curr");
        if (prevData != null && prevData.length != currData.length) prevData = null; //different length implies different sweep
        if (nextData != null && nextData.length != currData.length) nextData = null; //different length implies different sweep

        double maxJump = (scale.getMax() - scale.getMin()) / 12.0; //the maximum difference between current and surroundings to still smooth

        //Check to see if the gates all exist.  Will not at the beginning and end
        //rays.
        double sum = 0;
        int numpoints = 0;
        for (int i = -1; i < 2; ++i) {
            int ii = index + i;
            if (ii < 0 || ii >= currData.length) continue; //index out of range
            //the < test returns false for NaN, so no extra check is needed
            if (prevData != null && Math.abs(currData[index] - prevData[ii]) < maxJump) { //gate valid
                sum += prevData[ii];
                ++numpoints;
            }

            if (Math.abs(currData[index] - currData[ii]) < maxJump) { //gate valid
                sum += currData[ii];
                ++numpoints;
            }

            if (nextData != null && Math.abs(currData[index] - nextData[ii]) < maxJump) { //gate valid
                sum += nextData[ii];
                ++numpoints;
            }
        }
        if (numpoints > 0) translatedValue = sum / numpoints;

        return translatedValue;
    }

    protected double getUnfoldedValue (double currValue, final double vEst, final double nyquist)
    {
		if( Double.isNaN(vEst) )
			return currValue / 2;

		/* Make sure the signs are different, otherwise the velocity isn't folded. */
		if( Math.signum(currValue) == 1.0 && Math.signum(vEst) != -1.0 )
			return currValue / 2;
		else if( Math.signum(currValue) == -1.0 && Math.signum(vEst) != 1.0 )
			return currValue / 2;

        double foldThreshold = nyquist * (config.isUnfoldingAutomatic() ? 0.9 : 0.5);
		/* Double the vEst because it's halfed and the current value isn't. */
        if (Math.abs(currValue - (2*vEst)) > foldThreshold) {
            currValue += vEst > 0 ? (2*nyquist) : -(2*nyquist);
        }
        return currValue / 2;
    }

    protected double getGradientValue (final int index)
    {
        switch (config.getGradientType()) {
            case Azimuth: return this.getAzimuthGradientValue(index);
            case Range: return this.getRangeGradientValue(index);
            default: return currData[index];
        }
    }
    
    protected double getRangeGradientValue (final int index)
    {
        if (index - 1 < 0 || index + 1 >= currData.length) return Double.NaN; //goes off edge; can't calculate
        if (this.checkThresholdNotOK(index - 1)) return Double.NaN; //bad data; can't calculate
        if (this.checkThresholdNotOK(index + 1)) return Double.NaN; //bad data; can't calculate
        return (currData[index + 1] - currData[index - 1]) / (2 * currRay.getGateWidth());
    }
    
    protected double getAzimuthGradientValue (final int index)
    {
        if (prevRay == null || nextRay == null) return Double.NaN; //goes off edge; can't calculate
        return (prevRay.getStartAzimuth() > nextRay.getStartAzimuth() ?
                (prevRay.getData()[index] - nextRay.getData()[index]) :
                (nextRay.getData()[index] - prevRay.getData()[index])
            ) / (currRay.getGateWidth() * index * Math.abs(Math.toRadians(nextRay.getStartAzimuth() - prevRay.getStartAzimuth())));
    }
}
