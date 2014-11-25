/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;


/**
 * Class to represent task_end_info structure.
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class task_end_info {

    private int BeginPosition;
    private byte[] TempBuf;

    private short task_major_number;
    private short task_minor_number;
    private String task_config_file;
    private String task_description;
    private int tasks_in_hybrid;
    private int task_state;
    private ymds_time data_time_of_task;

    public task_end_info(ByteBuffer in_buf) {
        BeginPosition = in_buf.position();

        task_major_number = in_buf.getShort();
        task_minor_number = in_buf.getShort();
        TempBuf = new byte[12];
        in_buf.get(TempBuf);
        try {
            task_config_file = new String(TempBuf, "UTF-8");
            TempBuf = new byte[80];
            in_buf.get(TempBuf);
            task_description = new String(TempBuf, "UTF-8");
        } catch (Exception e) {
            System.err.println("Exception:" + e);
        }
        tasks_in_hybrid = in_buf.getInt();
        task_state = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        in_buf.position(in_buf.position() + 2);
        data_time_of_task = new ymds_time(in_buf);


    }

    /**
     * @return the task_major_number
     */
    public short getTask_major_number() {
        return task_major_number;
    }

    /**
     * @param task_major_number the task_major_number to set
     */
    public void setTask_major_number(short task_major_number) {
        this.task_major_number = task_major_number;
    }

    /**
     * @return the task_minor_number
     */
    public short getTask_minor_number() {
        return task_minor_number;
    }

    /**
     * @param task_minor_number the task_minor_number to set
     */
    public void setTask_minor_number(short task_minor_number) {
        this.task_minor_number = task_minor_number;
    }

    /**
     * @return the task_config_file
     */
    public String getTask_config_file() {
        return task_config_file;
    }

    /**
     * @param task_config_file the task_config_file to set
     */
    public void setTask_config_file(String task_config_file) {
        this.task_config_file = task_config_file;
    }

    /**
     * @return the task_description
     */
    public String getTask_description() {
        return task_description;
    }

    /**
     * @param task_description the task_description to set
     */
    public void setTask_description(String task_description) {
        this.task_description = task_description;
    }

    /**
     * @return the tasks_in_hybrid
     */
    public int getTasks_in_hybrid() {
        return tasks_in_hybrid;
    }

    /**
     * @param tasks_in_hybrid the tasks_in_hybrid to set
     */
    public void setTasks_in_hybrid(int tasks_in_hybrid) {
        this.tasks_in_hybrid = tasks_in_hybrid;
    }

    /**
     * @return the task_state
     */
    public int getTask_state() {
        return task_state;
    }

    /**
     * @param task_state the task_state to set
     */
    public void setTask_state(int task_state) {
        this.task_state = task_state;
    }

    /**
     * @return the data_time_of_task
     */
    public ymds_time getData_time_of_task() {
        return data_time_of_task;
    }

    /**
     * @param data_time_of_task the data_time_of_task to set
     */
    public void setData_time_of_task(ymds_time data_time_of_task) {
        this.data_time_of_task = data_time_of_task;
    }

}
