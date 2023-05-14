package org.impulsewellness.litebest.backend.filters;

import com.github.psambit9791.jdsp.filter.Butterworth;

import org.impulsewellness.litebest.backend.EMGData;

public class HighPassFilter extends Filter {

    private int order;
    private int cutOffFrequency;
    private int slidingWindowSize;
    private double[][] emgDataWindows;
    private Butterworth butterworth;

    public HighPassFilter(int order, int cutOffFrequency, int slidingWindowSize, Filter next) {
        super(next);
        this.order = order;
        this.cutOffFrequency = cutOffFrequency;
        // TODO :: ensure slidingWindowSize is greater than number of samples in a packet
        this.slidingWindowSize = slidingWindowSize;
        // sliding windows for each emg channel
        this.emgDataWindows = new double[EMGData.getNumChannels()][slidingWindowSize];
        butterworth = new Butterworth(EMGData.getFrequency());
    }

    @Override
    protected EMGData filterData(EMGData data) {
        double[][] emgDataCurrent = data.getData2d();
        double[][] emgDataFiltered = new double[EMGData.getNumChannels()][slidingWindowSize];
        updateSlidingWindows(emgDataCurrent);
        // filter the sliding window
        for (int channelIndex = 0; channelIndex < EMGData.getNumChannels(); channelIndex++) {
            emgDataFiltered[channelIndex] = butterworth.highPassFilter(emgDataWindows[channelIndex],
                                                                             order,
                                                                             cutOffFrequency);
            // get one emgData object worth of the filtered data
            for (int sampleIndex = 0; sampleIndex < EMGData.getDataLength(); sampleIndex++) {
                emgDataCurrent[channelIndex][sampleIndex] = emgDataFiltered[channelIndex][slidingWindowSize-EMGData.getDataLength()+sampleIndex];
            }
        }
        // update the emg data object
        data.setData(emgDataCurrent);
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
                emgDataWindows[channelIndex][sampleIndex] = emgDataWindows[channelIndex][sampleIndex+EMGData.getDataLength()];
            }
            for (int sampleIndex = slidingWindowSize - EMGData.getDataLength();
                 sampleIndex < slidingWindowSize;
                 sampleIndex++) {
                // put current emg data into sliding window
                emgDataWindows[channelIndex][sampleIndex] = emgDataCurrent[channelIndex][sampleIndex-(slidingWindowSize - EMGData.getDataLength())];
            }
        }
    }
}
