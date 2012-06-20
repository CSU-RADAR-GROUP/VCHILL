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


    public structure_header getSt_header() {
      return st_header;
    }


    public void setSt_header(structure_header st_header) {
      this.st_header = st_header;
    }


    public int getProd_type() {
      return prod_type;
    }


    public void setProd_type(int prod_type) {
      this.prod_type = prod_type;
    }


    public int getScheduling_code() {
      return scheduling_code;
    }


    public void setScheduling_code(int scheduling_code) {
      this.scheduling_code = scheduling_code;
    }


    public int getSec_between_runs() {
      return sec_between_runs;
    }


    public void setSec_between_runs(int sec_between_runs) {
      this.sec_between_runs = sec_between_runs;
    }


    public ymds_time getGenerated_time() {
      return generated_time;
    }


    public void setGenerated_time(ymds_time generated_time) {
      this.generated_time = generated_time;
    }


    public ymds_time getTime_ingest_sweep() {
      return time_ingest_sweep;
    }


    public void setTime_ingest_sweep(ymds_time time_ingest_sweep) {
      this.time_ingest_sweep = time_ingest_sweep;
    }


    public ymds_time getTime_ingest_file() {
      return time_ingest_file;
    }


    public void setTime_ingest_file(ymds_time time_ingest_file) {
      this.time_ingest_file = time_ingest_file;
    }


    public String getProduct_config_file() {
      return product_config_file;
    }


    public void setProduct_config_file(String product_config_file) {
      this.product_config_file = product_config_file;
    }


    public String getTask_name() {
      return task_name;
    }


    public void setTask_name(String task_name) {
      this.task_name = task_name;
    }


    public int getFlag() {
      return flag;
    }


    public void setFlag(int flag) {
      this.flag = flag;
    }


    public int getX_scale() {
      return x_scale;
    }


    public void setX_scale(int x_scale) {
      this.x_scale = x_scale;
    }


    public int getY_scale() {
      return y_scale;
    }


    public void setY_scale(int y_scale) {
      this.y_scale = y_scale;
    }


    public int getZ_scale() {
      return z_scale;
    }


    public void setZ_scale(int z_scale) {
      this.z_scale = z_scale;
    }


    public int getX_dir_size() {
      return x_dir_size;
    }


    public void setX_dir_size(int x_dir_size) {
      this.x_dir_size = x_dir_size;
    }


    public int getY_dir_size() {
      return y_dir_size;
    }


    public void setY_dir_size(int y_dir_size) {
      this.y_dir_size = y_dir_size;
    }


    public int getZ_dir_size() {
      return z_dir_size;
    }


    public void setZ_dir_size(int z_dir_size) {
      this.z_dir_size = z_dir_size;
    }


    public int getX_loc_of_radar() {
      return x_loc_of_radar;
    }


    public void setX_loc_of_radar(int x_loc_of_radar) {
      this.x_loc_of_radar = x_loc_of_radar;
    }


    public int getY_loc_of_radar() {
      return y_loc_of_radar;
    }


    public void setY_loc_of_radar(int y_loc_of_radar) {
      this.y_loc_of_radar = y_loc_of_radar;
    }


    public int getZ_loc_of_radar() {
      return z_loc_of_radar;
    }


    public void setZ_loc_of_radar(int z_loc_of_radar) {
      this.z_loc_of_radar = z_loc_of_radar;
    }


    public int getMax_range() {
      return max_range;
    }


    public void setMax_range(int max_range) {
      this.max_range = max_range;
    }


    public int getData_type_generated() {
      return data_type_generated;
    }


    public void setData_type_generated(int data_type_generated) {
      this.data_type_generated = data_type_generated;
    }


    public String getName_of_projection() {
      return name_of_projection;
    }


    public void setName_of_projection(String name_of_projection) {
      this.name_of_projection = name_of_projection;
    }


    public int getType_of_input() {
      return type_of_input;
    }


    public void setType_of_input(int type_of_input) {
      this.type_of_input = type_of_input;
    }


    public short getProjection_type() {
      return projection_type;
    }


    public void setProjection_type(short projection_type) {
      this.projection_type = projection_type;
    }


    public short getSmoother() {
      return smoother;
    }


    public void setSmoother(short smoother) {
      this.smoother = smoother;
    }


    public short getTimes_run() {
      return times_run;
    }


    public void setTimes_run(short times_run) {
      this.times_run = times_run;
    }


    public int getZr_constant() {
      return zr_constant;
    }


    public void setZr_constant(int zr_constant) {
      this.zr_constant = zr_constant;
    }


    public int getZr_exponent() {
      return zr_exponent;
    }


    public void setZr_exponent(int zr_exponent) {
      this.zr_exponent = zr_exponent;
    }


    public short getX_dir() {
      return x_dir;
    }


    public void setX_dir(short x_dir) {
      this.x_dir = x_dir;
    }


    public short getY_dir() {
      return y_dir;
    }


    public void setY_dir(short y_dir) {
      this.y_dir = y_dir;
    }


    public product_specific_info getPsi() {
      return psi;
    }


    public void setPsi(product_specific_info psi) {
      this.psi = psi;
    }


    public String getList_minor_task_suffixes() {
      return list_minor_task_suffixes;
    }


    public void setList_minor_task_suffixes(String list_minor_task_suffixes) {
      this.list_minor_task_suffixes = list_minor_task_suffixes;
    }


    public byte[] getTempBuf() {
      return tempBuf;
    }


    public void setTempBuf(byte[] tempBuf) {
      this.tempBuf = tempBuf;
    }


   


    public byte getTempByte() {
      return TempByte;
    }


    public void setTempByte(byte tempByte) {
      TempByte = tempByte;
    }

}
