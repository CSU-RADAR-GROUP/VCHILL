/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Class that acts as a data stream for coded data.
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class DataDecoderBuffer {

    private byte[] buffer; // Main buffer, the rest can not be relied on.
    private byte[] t_buffer;
    private int bytesleft;
    private ByteBuffer in_stream;
    private short codeword;
    private byte[] z_buffer;
    private byte[] d_buffer;
    private ByteBuffer r_buf;
    private DataInputStream dis;

    public DataDecoderBuffer(DataInputStream dis, ByteBuffer start_buf) {
        bytesleft = 0;
        t_buffer = new byte[6144];
        in_stream = start_buf;
        this.dis = dis;


    }


    public void EnlargeBuffer() {

        byte tempbyte[] = new byte[12];

//            System.out.println("Enlarging Buffer");
//            System.out.println("Buffer Size:"+in_stream.remaining());
        //Just incase we read in another record.
        t_buffer = new byte[6132];
        try {
            dis.read(tempbyte);
            ByteBuffer tbuf = ByteBuffer.wrap(tempbyte);
            tbuf.order(ByteOrder.LITTLE_ENDIAN);
            raw_prod_bhdr bhdr = new raw_prod_bhdr(tbuf);
//                System.out.println("***Ray from bhdr:"+bhdr.getRay_number());
            dis.read(t_buffer);
            z_buffer = new byte[in_stream.remaining()];
            in_stream.get(z_buffer);

            d_buffer = new byte[6132 + z_buffer.length];
            System.arraycopy(z_buffer, 0, d_buffer, 0, z_buffer.length);
            System.arraycopy(t_buffer, 0, d_buffer, z_buffer.length, 6132);
            in_stream = ByteBuffer.wrap(d_buffer);
            in_stream.order(ByteOrder.LITTLE_ENDIAN);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Class to act as an interface to the buffered stream and provide on the fly decoding.
     *
     * @param numbytes
     * @return
     */
    public ByteBuffer getData(int numbytes) {


        while (bytesleft < numbytes) {
            if (in_stream.remaining() < 2) {
                this.EnlargeBuffer();
            }
//            System.out.println("Position:"+in_stream.position());
            codeword = in_stream.getShort();
//            System.out.println("CodeWord:"+codeword);
            if (codeword == 1) {
                System.err.println("**********Erroneous end of ray detected****************");
                break;
            } else if (codeword > 0) {
                t_buffer = buffer;
                z_buffer = new byte[2 * codeword];
                Arrays.fill(z_buffer, (byte) 0);
                buffer = new byte[bytesleft + 2 * codeword];
                if (bytesleft != 0) {
                    System.arraycopy(t_buffer, 0, buffer, 0, bytesleft);
                }
                System.arraycopy(z_buffer, 0, buffer, bytesleft, 2 * codeword);
                bytesleft += 2 * codeword;
            } else if (codeword < 0) {
                while (2 * UtilityClass.convert_code_word(codeword) > in_stream.remaining()) {
                    this.EnlargeBuffer();
                }
                d_buffer = new byte[2 * UtilityClass.convert_code_word(codeword)];
                in_stream.get(d_buffer);
                t_buffer = buffer;
                buffer = new byte[bytesleft + 2 * UtilityClass.convert_code_word(codeword)];
                if (bytesleft != 0) {
                    System.arraycopy(t_buffer, 0, buffer, 0, bytesleft);
                }
                System.arraycopy(d_buffer, 0, buffer, bytesleft, 2 * UtilityClass.convert_code_word(codeword));
                bytesleft += 2 * UtilityClass.convert_code_word(codeword);
            } else {
                System.out.println("Problem Encountered");
            }
        }

        if (bytesleft == numbytes) { //We hit the end of a record
            r_buf = ByteBuffer.wrap(buffer);
            r_buf.order(ByteOrder.LITTLE_ENDIAN);
            buffer = null;
            bytesleft = 0;
            return r_buf;
        }
        if (bytesleft > numbytes) {
            t_buffer = new byte[numbytes];
            d_buffer = new byte[bytesleft - numbytes];
            System.arraycopy(buffer, 0, t_buffer, 0, numbytes);
            System.arraycopy(buffer, numbytes, d_buffer, 0, bytesleft - numbytes);
            bytesleft -= numbytes;
            buffer = new byte[bytesleft];
            System.arraycopy(d_buffer, 0, buffer, 0, bytesleft);
            r_buf = ByteBuffer.wrap(t_buffer);
            r_buf.order(ByteOrder.LITTLE_ENDIAN);
            return r_buf;

        }


        return in_stream;
    }

    public boolean checkEndRay() {
        if (in_stream.remaining() == 0) {
            this.EnlargeBuffer();
        }

        short codeword = in_stream.getShort();
        if (codeword != 1) {
            System.err.println("Misaligned Ray Detected.");
            return false;
        } else {
//            System.out.println("Ray Alignment was correct");
            return true;
        }

    }

    public boolean nondestructivecheckEndRay() {
        if (in_stream.remaining() == 0) {
            this.EnlargeBuffer();
        }
        in_stream.mark();
        short codeword = in_stream.getShort();
        in_stream.reset();
        if (codeword == 1)
            return true;
        else
            return false;

    }


}
