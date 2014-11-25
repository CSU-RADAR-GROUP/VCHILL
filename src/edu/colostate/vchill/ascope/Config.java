package edu.colostate.vchill.ascope;

class Config {
    private static final Config me = new Config();

    private volatile boolean drawClickRange = true;
    private volatile double clickRange;
    private volatile double clickY;

    private Config() {
    }

    static Config getInstance() {
        return me;
    }

    public synchronized void setDrawClickRangeEnabled(final boolean drawClickRange) {
        this.drawClickRange = drawClickRange;
    }

    public synchronized boolean isDrawClickRangeEnabled() {
        return this.drawClickRange;
    }

    public synchronized void setClickRange(final double clickRange) {
        this.clickRange = clickRange;
    }

    public synchronized double getClickRange() {
        return this.clickRange;
    }

    public synchronized void setClickY(final double clickY) {
        this.clickY = clickY;
    }

    public synchronized double getClickY() {
        return this.clickY;
    }
}
