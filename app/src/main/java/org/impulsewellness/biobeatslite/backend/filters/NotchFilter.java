package org.impulsewellness.litebest.backend.filters;

import android.util.Log;

import org.impulsewellness.litebest.backend.EMGData;

import com.github.psambit9791.jdsp.filter.Butterworth;

public class NotchFilter extends Filter {

    private int order;
    private int lowCutOff, highCutOff;
    private int slidingWindowSize;
    private double[][] emgDataWindows;
    private Butterworth butterworth;
    private static String Tag = "NotchFilter";

    public NotchFilter(int order, int lowCutOff, int highCutOff, int slidingWindowSize, Filter next) {
        super(next);
        this.order = order;
        this.lowCutOff = lowCutOff;
        this.highCutOff = highCutOff;
        // TODO :: ensure slidingWindowSize is greater than number of samples in a packet
        this.slidingWindowSize = slidingWindowSize;
        // sliding windows for each emg channel
        this.emgDataWindows = new double[EMGData.getNumChannels()][slidingWindowSize];
        butterworth = new Butterworth(EMGData.getFrequency());
    }

    @Override
    protected EMGData filterData(EMGData data) {
        double[][] emgDataCurrent = data.getData2d();
        double[][] emgDataFilteredWindows = new double[EMGData.getNumChannels()][slidingWindowSize];
        updateSlidingWindows(emgDataCurrent);
        // filter the sliding window
        for (int channelIndex = 0; channelIndex < EMGData.getNumChannels(); channelIndex++) {
            emgDataFilteredWindows[channelIndex] = butterworth.bandStopFilter(emgDataWindows[channelIndex],
                    order,
                    lowCutOff,
                    highCutOff);
            // get one emgData object worth of the filtered window
            for (int sampleIndex = 0; sampleIndex < EMGData.getDataLength(); sampleIndex++) {
                emgDataCurrent[channelIndex][sampleIndex] = emgDataFilteredWindows[channelIndex][slidingWindowSize-EMGData.getDataLength()+sampleIndex];
            }
        }
        // update the emg data object
        data.setData(emgDataCurrent);
        Log.v(Tag, data.dataToString());
        return data;
    }

    /**
     * Updates sliding window with incoming data.
     * @param emgDataCurrent incoming data.
     */
    private void updateSlidingWindows(double[][] emgDataCurrent) {
        // TODO :: ensure the sliding window size is greater than ble packet data length.
        for (int channelIndex = 0; channelIndex < EMGData.getNumChannels(); channelIndex++) {
            for( int sampleIndex = 0;
                 sampleIndex < slidingWindowSize - EMGData.getDataLength();
                 sampleIndex++) {
                // shift old emg data over
                double hold = emgDataWindows[channelIndex][sampleIndex+EMGData.getDataLength()];
                emgDataWindows[channelIndex][sampleIndex] = hold;
            }
            for (int sampleIndex = slidingWindowSize - EMGData.getDataLength();
                 sampleIndex < EMGData.getDataLength();
                 sampleIndex++) {
                // put current emg data into sliding window
                emgDataWindows[channelIndex][sampleIndex] = emgDataCurrent[channelIndex][sampleIndex-slidingWindowSize];
            }
        }
    }
}
