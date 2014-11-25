package edu.colostate.vchill;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for using user input to estimate velocity at a given height for use in unfolding.
 *
 * @author Jochen Deyke
 * @version 2007-05-24
 */
public final class EstimateParser {
    private static final EstimateParser ep = new EstimateParser();

    /**
     * Estimated velocities by height
     */
    private volatile List<Estimate> vEstimates;

    private EstimateParser() {
        this.parse(ConfigUtil.getString("Velocity Estimates",
                "2 -5\n" +
                        "4 -9\n" +
                        "6 -12\n" +
                        "8 -16\n" +
                        "10 -20\n" +
                        "12 -27\n" +
                        "14 -30\n" +
                        "500 0"));
    }

    public static EstimateParser getInstance() {
        return ep;
    }

    /**
     * Gets the estimated velocity at a given height.  If there is no estimate entered at the desired height, a weighted average of the surrounding heights is used.
     *
     * @param height desired height
     * @return velocity estimate for that height
     */
    public double getVEstimate(final double height) {
        Estimate prev = new Estimate(Double.MIN_VALUE, 0);
        for (Estimate est : this.vEstimates) {
            if (est.height > height) {
                return prev.velocity + ((height - prev.height) * (est.velocity - prev.velocity) / (est.height - prev.height));
            }
            prev = est;
        }
        return prev.velocity; //default
    }

    /**
     * Parses a list of height/estimate pairs from a multiline string
     *
     * @param estimates a multiline String containing space separated height/estimate pairs (one pair per line)
     */
    public void parse(final String estimates) {
        if (estimates == null) return;
        String[] lines = estimates.split("\n");
        List<Estimate> result = new LinkedList<Estimate>();
        for (int i = 0; i < lines.length; ++i) {
            String[] line = lines[i].split(" ");
            try {
                result.add(new Estimate(Double.parseDouble(line[0]), Double.parseDouble(line[1])));
            } catch (NumberFormatException nfe) {
            } catch (ArrayIndexOutOfBoundsException aioobe) {
            }
        }
        Collections.sort(result);
        this.vEstimates = result;
        ConfigUtil.put("Velocity Estimates", this.toString());
    }

    /**
     * Converts the current set of estimates to a multiline string
     *
     * @return a multiline String containing space separated height/estimate pairs (one pair per line)
     */
    public String toString() {
        StringBuilder buff = new StringBuilder();
        for (Estimate est : vEstimates) {
            buff.append(est.height).append(" ").append(est.velocity).append("\n");
        }
        return buff.toString();
    }

    /**
     * Class representing a height/velocity estimate pair
     */
    private static class Estimate implements Comparable<Estimate> {
        public final double height;
        public final double velocity;

        public Estimate(final double height, final double velocity) {
            this.height = height;
            this.velocity = velocity;
        }

        public int compareTo(final Estimate other) {
            return (int) (this.height - other.height);
        }
    }
}
