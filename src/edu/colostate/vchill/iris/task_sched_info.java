/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 * Class to represent task_sched_info.
 * Completed
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class task_sched_info {

    private int start_time;
    private int stop_time;
    private int skip_time;
    private int last_run;
    private int time_used;
    private int relative_day;
    private int flag;


    task_sched_info(ByteBuffer in_buf) {
        start_time = in_buf.getInt();
        stop_time = in_buf.getInt();
        skip_time = in_buf.getInt();
        last_run = in_buf.getInt();
        time_used = in_buf.getInt();
        relative_day = in_buf.getInt();
        int flag = UtilityClass.UINT2_to_SINT(in_buf.getShort());

    }

    /**
     * @return the start_time
     */
    public int getStart_time() {
        return start_time;
    }

    /**
     * @param start_time the start_time to set
     */
    public void setStart_time(int start_time) {
        this.start_time = start_time;
    }

    /**
     * @return the stop_time
     */
    public int getStop_time() {
        return stop_time;
    }

    /**
     * @param stop_time the stop_time to set
     */
    public void setStop_time(int stop_time) {
        this.stop_time = stop_time;
    }

    /**
     * @return the skip_time
     */
    public int getSkip_time() {
        return skip_time;
    }

    /**
     * @param skip_time the skip_time to set
     */
    public void setSkip_time(int skip_time) {
        this.skip_time = skip_time;
    }

    /**
     * @return the last_run
     */
    public int getLast_run() {
        return last_run;
    }

    /**
     * @param last_run the last_run to set
     */
    public void setLast_run(int last_run) {
        this.last_run = last_run;
    }

    /**
     * @return the time_used
     */
    public int getTime_used() {
        return time_used;
    }

    /**
     * @param time_used the time_used to set
     */
    public void setTime_used(int time_used) {
        this.time_used = time_used;
    }

    /**
     * @return the relative_day
     */
    public int getRelative_day() {
        return relative_day;
    }

    /**
     * @param relative_day the relative_day to set
     */
    public void setRelative_day(int relative_day) {
        this.relative_day = relative_day;
    }

    /**
     * @return the flag
     */
    public int getFlag() {
        return flag;
    }

    /**
     * @param flag the flag to set
     */
    public void setFlag(int flag) {
        this.flag = flag;
    }

}
