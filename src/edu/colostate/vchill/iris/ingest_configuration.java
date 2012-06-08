/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;


/**
 * Class to represent C struct ingest_configuration
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class ingest_configuration {
 
    private byte[] tempBuf;
    
    private String name_of_file;
    private short num_extant;
    
    private int size_of_all_files;
    private ymds_time vol_scan_start_time;
    
    private short bytes_in_ray_headers;
    private short bytes_in_ext_ray_headers;
    private short num_task_config;
    private short playback_version;
    
    private String iris_version;
    private String hardware_name_of_site;
    private short local_timezone_west_GMT;
    private String name_of_site;
    private short recorded_minutes_west_GMT;
    private long latitude;
    private long longitude;
    private short height_of_ground;
    private short height_of_radar;
    private int resolution;
    private int index_of_first_ray;
    private int num_rays_in_sweep;
    private int num_bytes_in_gparam;
    private int altitude_radar_cm;
    private int[] velocity_of_radar = new int[3];
    private int[] antenna_offset_from_INU = new int[3];
    private long fault_status;
    private short height_melting_layer;
    
    private String local_timezone;
    private long flags;
    private String config_name;
    
    
    
    
    private int BeginPosition;
    private byte[] TempBuf;
    private byte TempByte;
    
    
    public ingest_configuration(ByteBuffer in_buf)
    {   
        try{
            BeginPosition=in_buf.position();
            
            tempBuf = new byte[80];
            in_buf.get(tempBuf);//Name of File on disk
            name_of_file = new String(tempBuf,"UTF-8");
            num_extant=in_buf.getShort();
            
            in_buf.position(in_buf.position()+2);
            
            size_of_all_files=in_buf.getInt();
            vol_scan_start_time = new ymds_time(in_buf);
            
            in_buf.position(in_buf.position()+12);
            
            bytes_in_ray_headers = in_buf.getShort();
            bytes_in_ext_ray_headers = in_buf.getShort();
            num_task_config=in_buf.getShort();
            playback_version=in_buf.getShort();
            in_buf.position(in_buf.position()+4);
            
            TempBuf = new byte[8];
            in_buf.get(TempBuf);
            iris_version = new String(TempBuf, "UTF-8");
            TempBuf = new byte[16];
            in_buf.get(TempBuf);
            hardware_name_of_site = new String(TempBuf, "UTF-8");
            local_timezone_west_GMT = in_buf.getShort();
            TempBuf = new byte[16];
            in_buf.get(TempBuf);
            name_of_site = new String(TempBuf, "UTF-8");
            recorded_minutes_west_GMT = in_buf.getShort();
            latitude = UtilityClass.UINT4_to_long(in_buf.getInt());
            longitude = UtilityClass.UINT4_to_long(in_buf.getInt());
            height_of_ground = in_buf.getShort();
            height_of_radar = in_buf.getShort();
            resolution = UtilityClass.UINT2_to_SINT(in_buf.getShort());
            index_of_first_ray = UtilityClass.UINT2_to_SINT(in_buf.getShort());
            num_rays_in_sweep= UtilityClass.UINT2_to_SINT(in_buf.getShort());
            num_bytes_in_gparam = in_buf.getShort();
            altitude_radar_cm = in_buf.getInt();
            for(int i=0; i<3; i++)
            {
                velocity_of_radar[i] = in_buf.getInt();
            }
            for(int i=0;i<3;i++)
            {
                antenna_offset_from_INU[i]=in_buf.getInt();
            }
            fault_status=UtilityClass.UINT4_to_long(in_buf.getInt());
            height_melting_layer = in_buf.getShort();
            in_buf.position(in_buf.position()+2);
            TempBuf = new byte[8];
            in_buf.get(TempBuf);
            local_timezone = new String(TempBuf, "UTF-8");
            flags = UtilityClass.UINT4_to_long(in_buf.getInt());
            TempBuf = new byte[16];
            in_buf.get(TempBuf);
            config_name = new String(TempBuf, "UTF-8");
            in_buf.position(in_buf.position()+228);
 
        
        }catch(Exception e){
            System.err.println("Exception:"+e);
        }
    }
    
    
}
