package edu.colostate.vchill.iris;

import java.io.DataInputStream;
import java.nio.ByteBuffer;




/**
 * Class representation of product_configuration structure
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class product_configuration {
    //TODO Finish the full list
    private structure_header st_header;
    private int prod_type;
    private int scheduling_code;
    private int sec_between_runs;
    private ymds_time generated_time;
    private ymds_time time_ingest_sweep;
    private ymds_time time_ingest_file;
    private String product_config_file;
    private String task_name;
    private int flag;
    private int x_scale;
    private int y_scale;
    private int z_scale;
    private int x_dir_size;
    private int y_dir_size;
    private int z_dir_size;
    private int x_loc_of_radar;
    private int y_loc_of_radar;
    private int z_loc_of_radar;
    private int max_range;
    
    private int data_type_generated;
    private String name_of_projection;
    private int type_of_input;
    private short projection_type;
    
    private short smoother;
    private short times_run;
    private int zr_constant;
    private int zr_exponent;
    private short x_dir;
    private short y_dir;
    private product_specific_info psi;
    String list_minor_task_suffixes;
    
    
    
    private byte[] tempBuf = new byte[12];
    private byte[] TempBuf;
    private byte TempByte;
 
    
    public product_configuration(ByteBuffer in_buf){
        //For now to skip data we will just use DataInputString.skipbytes.
        st_header = new structure_header(in_buf);
        try{
        prod_type = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        scheduling_code = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        sec_between_runs = in_buf.getInt();
        generated_time = new ymds_time(in_buf);
        time_ingest_sweep = new ymds_time(in_buf);
        time_ingest_file = new ymds_time(in_buf);

        in_buf.position(in_buf.position()+6);
        
        in_buf.get(tempBuf);
        product_config_file = new String(tempBuf, "UTF-8");
        in_buf.get(tempBuf); //Task Name
        task_name = new String(tempBuf, "UTF-8");
        System.out.println("Task Name:"+task_name);
        flag = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        x_scale = in_buf.getInt();
        y_scale = in_buf.getInt();
        z_scale = in_buf.getInt();
        x_dir_size = in_buf.getInt();
        y_dir_size = in_buf.getInt();
        z_dir_size = in_buf.getInt();       
        x_loc_of_radar = in_buf.getInt();
        y_loc_of_radar = in_buf.getInt();
        z_loc_of_radar = in_buf.getInt();
        max_range = in_buf.getInt();

        in_buf.position(in_buf.position()+2);
        
        data_type_generated = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        TempBuf = new byte[12];
        in_buf.get(TempBuf);
        name_of_projection=new String(TempBuf,"UTF-8");
        type_of_input = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        TempByte = in_buf.get();
        projection_type = TempByte;
        
        in_buf.position(in_buf.position()+1);
        
        smoother = in_buf.getShort();
        times_run = in_buf.getShort();
        zr_constant = in_buf.getInt();
        zr_exponent = in_buf.getInt();
        x_dir = in_buf.getShort();
        y_dir = in_buf.getShort();
        psi = new product_specific_info(in_buf);
        TempBuf = new byte[16];
        in_buf.get(TempBuf);
        list_minor_task_suffixes = new String(TempBuf, "UTF-8");
        in_buf.position(in_buf.position()+12);
        in_buf.position(in_buf.position()+48);
        }catch(Exception e){
            System.err.println("Exception: "+e);
        }
        
                
                
        
    }

}
