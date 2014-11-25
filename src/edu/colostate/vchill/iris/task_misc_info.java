package edu.colostate.vchill.iris;

import java.nio.ByteBuffer;

/**
 * Class to represent task_misc_info structure
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 *         Completed
 */
public class task_misc_info {
    private int wavelength;
    private String TR_Serial;
    private int tr_power;
    private int Flags;
    private int polarization;
    private int truncation_height;
    private int comment_bytes;
    private long H_beamwidth;
    private long V_beamwidth;
    private long[] custom_storage = new long[10]; //Not used yet

    private int BeginPosition;
    private byte[] TempBuf;

    public task_misc_info(ByteBuffer in_buf) {
        BeginPosition = in_buf.position();

        wavelength = in_buf.getInt();
        TempBuf = new byte[16];
        in_buf.get(TempBuf);
        try {
            TR_Serial = new String(TempBuf, "UTF-8");
        } catch (Exception e) {
            System.err.println("Exception:" + e);
        }

        tr_power = in_buf.getInt();
        Flags = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        polarization = UtilityClass.UINT2_to_SINT(in_buf.getShort());
        truncation_height = in_buf.getInt();
        in_buf.position(in_buf.position() + 18 + 12);
        comment_bytes = in_buf.getShort();
        H_beamwidth = UtilityClass.UINT4_to_long(in_buf.getInt());
        V_beamwidth = UtilityClass.UINT4_to_long(in_buf.getInt());


    }

    /**
     * @return the wavelength
     */
    public int getWavelength() {
        return wavelength;
    }

    /**
     * @param wavelength the wavelength to set
     */
    public void setWavelength(int wavelength) {
        this.wavelength = wavelength;
    }

    /**
     * @return the TR_Serial
     */
    public String getTR_Serial() {
        return TR_Serial;
    }

    /**
     * @param TR_Serial the TR_Serial to set
     */
    public void setTR_Serial(String TR_Serial) {
        this.TR_Serial = TR_Serial;
    }

    /**
     * @return the tr_power
     */
    public int getTr_power() {
        return tr_power;
    }

    /**
     * @param tr_power the tr_power to set
     */
    public void setTr_power(int tr_power) {
        this.tr_power = tr_power;
    }

    /**
     * @return the Flags
     */
    public int getFlags() {
        return Flags;
    }

    /**
     * @param Flags the Flags to set
     */
    public void setFlags(int Flags) {
        this.Flags = Flags;
    }

    /**
     * @return the polarization
     */
    public int getPolarization() {
        return polarization;
    }

    /**
     * @param polarization the polarization to set
     */
    public void setPolarization(int polarization) {
        this.polarization = polarization;
    }

    /**
     * @return the truncation_height
     */
    public int getTruncation_height() {
        return truncation_height;
    }

    /**
     * @param truncation_height the truncation_height to set
     */
    public void setTruncation_height(int truncation_height) {
        this.truncation_height = truncation_height;
    }

    /**
     * @return the comment_bytes
     */
    public int getComment_bytes() {
        return comment_bytes;
    }

    /**
     * @param comment_bytes the comment_bytes to set
     */
    public void setComment_bytes(int comment_bytes) {
        this.comment_bytes = comment_bytes;
    }

    /**
     * @return the H_beamwidth
     */
    public long getH_beamwidth() {
        return H_beamwidth;
    }

    /**
     * @param H_beamwidth the H_beamwidth to set
     */
    public void setH_beamwidth(long H_beamwidth) {
        this.H_beamwidth = H_beamwidth;
    }

    /**
     * @return the V_beamwidth
     */
    public long getV_beamwidth() {
        return V_beamwidth;
    }

    /**
     * @param V_beamwidth the V_beamwidth to set
     */
    public void setV_beamwidth(long V_beamwidth) {
        this.V_beamwidth = V_beamwidth;
    }

    /**
     * @return the custom_storage
     */
    public long[] getCustom_storage() {
        return custom_storage;
    }

    /**
     * @param custom_storage the custom_storage to set
     */
    public void setCustom_storage(long[] custom_storage) {
        this.custom_storage = custom_storage;
    }

    /**
     * @return the BeginPosition
     */
    public int getBeginPosition() {
        return BeginPosition;
    }

    /**
     * @param BeginPosition the BeginPosition to set
     */
    public void setBeginPosition(int BeginPosition) {
        this.BeginPosition = BeginPosition;
    }

}
