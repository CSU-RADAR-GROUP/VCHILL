package edu.colostate.vchill.chill;

import edu.colostate.vchill.ChillDefines;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Processor control/status information
 *
 * @author Jochen Deyke
 * @version 2007-05-21
 */
public class ChillProcessorInfo extends ChillHeader {
    /**
     * size (in bytes) of this header (including ChillHeaderHeader, but not including extraData)
     */
    public static final int BYTE_SIZE = ChillHeaderHeader.BYTE_SIZE +
            4 * ChillDefines.INT_BYTE_SIZE + //enums
            3 * ChillDefines.INT_BYTE_SIZE + //unsigned
            ChillDefines.FLOAT_BYTE_SIZE +
            4 * ChillDefines.FLOAT_BYTE_SIZE +
            ChillDefines.INT_BYTE_SIZE + //unsigned
            5 * ChillDefines.FLOAT_BYTE_SIZE;

    /**
     * Processing mode used by moment calculator.
     * 1 &lt;&lt; ordinal to set/test processing_mode
     */
    enum ProcMode {
        INDEXEDBEAM,
        LONGINT,
        DUALPRT,
        PHASECODE,
    }

    /**
     * Transmitter polarization mode
     */
    PolarizationMode polarization_mode;
    /**
     * Signal Processing mode
     */
    int processing_mode; //bitmask
    /**
     * Transmitter pulse type
     */
    PulseType pulse_type;
    /**
     * Radar calibration/test type
     */
    TestType test_type;

    /**
     * Number of cycles integrated to give one ray
     */
    public /*unsigned*/ int integration_cycle_pulses;
    /**
     * Clutter filter used by the processor
     */
    public /*unsigned*/ int clutter_filter_number;
    /**
     * Number of range gates to average
     */
    public /*unsigned*/ int range_gate_averaging;
    /**
     * Beamwidth in degrees, over which to integrate in indexed beam mode
     */
    public float indexed_beam_width_d;

    /**
     * Gate spacing (in meters), does not include effect of range avergaing
     */
    public float gate_spacing_m;
    /**
     * PRT in microseconds
     */
    public float prt_usec;
    /**
     * Range to start processing
     */
    public float range_start_km;
    /**
     * Range to stop processing
     */
    public float range_stop_km;

    /**
     * Number of gates for digitizer to acquire
     */
    public /*unsigned*/ int max_gate;
    /**
     * Power at signal generator output in dBm, when test set is commanded to output 0dBm
     */
    public float test_power_dbm;
    /**
     * Reserved
     */
    public float unused1;
    /**
     * Reserved
     */
    public float unused2;
    /**
     * Range at which test pulse is located
     */
    public float test_pulse_range_km;
    /**
     * Length of test pulse
     */
    public float test_pulse_length_usec;

    public ChillProcessorInfo() {
        super(new ChillHeaderHeader(ChillDefines.GEN_MOM_DATA, BYTE_SIZE));
        super.extraData = new byte[0];
    }

    /**
     * Constructs a header by reading initial values from a DataInput.
     *
     * @param in the DataInput to read initialization values from
     */
    public ChillProcessorInfo(final DataInput in, final ChillHeaderHeader header) throws IOException {
        super(header);
        assert header.recordType == ChillDefines.HSK_ID_PROCESSOR_INFO;
        assert header.headerLength - ChillProcessorInfo.BYTE_SIZE >= 0;
        this.polarization_mode = PolarizationMode.values()[in.readInt()];
        this.processing_mode = in.readInt();
        this.pulse_type = PulseType.values()[in.readInt()];
        this.test_type = TestType.values()[in.readInt()];
        this.integration_cycle_pulses = in.readInt();
        this.clutter_filter_number = in.readInt();
        this.range_gate_averaging = in.readInt();
        this.indexed_beam_width_d = in.readFloat();
        this.gate_spacing_m = in.readFloat();
        this.prt_usec = in.readFloat();
        this.range_start_km = in.readFloat();
        this.range_stop_km = in.readFloat();
        this.max_gate = in.readInt();
        this.test_power_dbm = in.readFloat();
        this.unused1 = in.readFloat();
        this.unused2 = in.readFloat();
        this.test_pulse_range_km = in.readFloat();
        this.test_pulse_length_usec = in.readFloat();
        in.readFully(super.extraData = new byte[header.headerLength - ChillProcessorInfo.BYTE_SIZE]);
    }

    /**
     * Writes this header to a DataOut
     *
     * @param out the DataOutput to write values to
     */
    public void write(final DataOutput out) throws IOException {
        assert header.recordType == ChillDefines.HSK_ID_PROCESSOR_INFO;
        assert header.headerLength == ChillProcessorInfo.BYTE_SIZE + extraData.length;
        super.header.write(out);
        out.writeInt(this.polarization_mode.ordinal());
        out.writeInt(this.processing_mode);
        out.writeInt(this.pulse_type.ordinal());
        out.writeInt(this.test_type.ordinal());
        out.writeInt(this.integration_cycle_pulses);
        out.writeInt(this.clutter_filter_number);
        out.writeInt(this.range_gate_averaging);
        out.writeFloat(this.indexed_beam_width_d);
        out.writeFloat(this.gate_spacing_m);
        out.writeFloat(this.prt_usec);
        out.writeFloat(this.range_start_km);
        out.writeFloat(this.range_stop_km);
        out.writeInt(this.max_gate);
        out.writeFloat(this.test_power_dbm);
        out.writeFloat(this.unused1);
        out.writeFloat(this.unused2);
        out.writeFloat(this.test_pulse_range_km);
        out.writeFloat(this.test_pulse_length_usec);
        out.write(this.extraData);
    }
}
