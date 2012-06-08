/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;
/**
 * Class representation of the ymds_time structure from Iris Programmers Manual
 * @author Joseph Hardin
 * Completed
 */
public class ymds_time {
    
    private int seconds;
    private int milliseconds;
    private short year;
    private short month;
    private short day;


    
public ymds_time(ByteBuffer in_buf)
{
    try{
        seconds=in_buf.getInt();
        milliseconds=UtilityClass.UINT2_to_SINT(in_buf.getShort());
        year = in_buf.getShort();
        month = in_buf.getShort();
        day = in_buf.getShort();
            
        }catch(Exception e){
            System.err.println("Exception: "+e);
        }
}

    /**
     * @return the seconds
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * @param seconds the seconds to set
     */
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    /**
     * @return the milliseconds
     */
    public int getMilliseconds() {
        return milliseconds;
    }

    /**
     * @param milliseconds the milliseconds to set
     */
    public void setMilliseconds(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    /**
     * @return the year
     */
    public short getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(short year) {
        this.year = year;
    }

    /**
     * @return the month
     */
    public short getMonth() {
        return month;
    }

    /**
     * @param month the month to set
     */
    public void setMonth(short month) {
        this.month = month;
    }

    /**
     * @return the day
     */
    public short getDay() {
        return day;
    }

    /**
     * @param day the day to set
     */
    public void setDay(short day) {
        this.day = day;
    }

    
    
}
