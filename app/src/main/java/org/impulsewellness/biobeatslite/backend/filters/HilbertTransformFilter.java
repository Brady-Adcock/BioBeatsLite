package org.impulsewellness.litebest.backend.filters;

import com.github.psambit9791.jdsp.transform.Hilbert;

import org.impulsewellness.litebest.backend.EMGData;

public class HilbertTransformFilter extends Filter{

    // TODO : make constructor and field which specifies window size
    private int slidingWindowSize;
    private double[][] emgDataWindows;

    public HilbertTransformFilter(int slidingWindowSize, Filter next) {
        super(next);
        this.slidingWindowSize = slidingWindowSize;
        this.emgDataWindows = new double[EMGData.getNumChannels()][slidingWindowSize];
    }

    @Override
    EMGData filterData(EMGData data) {
        // add data to window
        // do transform
        // return only values from
        updateSlidingWindows(data.getData2d());
        double[][] emgDataCurrent  = data.getData2d();
        double[][] emgDataFiltered = new double[EMGData.getNumChannels()][slidingWindowSize];
        for(int channelIndex=0; channelIndex<EMGData.getNumChannels(); channelIndex++) {
            Hilbert hilbert = new Hilbert(emgDataWindows[channelIndex]);
            hilbert.transform();
            emgDataFiltered[channelIndex] = hilbert.getAmplitudeEnvelope();
            for (int sampleIndex = 0; sampleIndex < EMGData.getDataLength(); sampleIndex++) {
                emgDataCurrent[channelIndex][sampleIndex] = emgDataFiltered[channelIndex][slidingWindowSize-EMGData.getDataLength()+sampleIndex];
            }
        }
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
