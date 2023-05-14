package org.impulsewellness.biobeatslite.backend.filters;

import org.impulsewellness.litebest.backend.EMGData;

public class SmoothingFilter extends Filter {
    private int slidingWindowSize;
    private double[][] emgDataWindows;
    public SmoothingFilter(int slidingWindowSize, Filter next){
        super(next);
        this.slidingWindowSize = slidingWindowSize;
        this.emgDataWindows = new double[EMGData.getNumChannels()][slidingWindowSize];
    }
    @Override
    EMGData filterData(EMGData data) {
        double[][] emgDataCurrent = data.getData2d();
        double[][] emgDataFiltered = new double[EMGData.getNumChannels()][EMGData.getDataLength()];
        for (int channelIndex=0; channelIndex<EMGData.getNumChannels(); channelIndex++){
            // "slide" for every point in the packet
            for(int sampleIndex=0; sampleIndex<EMGData.getDataLength(); sampleIndex++){
                update(channelIndex, emgDataCurrent[channelIndex][sampleIndex]);
                emgDataFiltered[channelIndex][sampleIndex] =  average(emgDataWindows[channelIndex]);
            }
        }
        data.setData(emgDataFiltered);
        return data;
    }

    // TODO: change this to operate more like the matlab smoothing function
    // smooth(3) with windows size of 3 means
    // smooth
    private short average(double[] slidingWindow) {
        short calculatedAverage = 0;
        for (int i=0; i<slidingWindow.length; i++) {
            calculatedAverage += slidingWindow[i];
        }
        return (short)(calculatedAverage/(double) slidingWindow.length);
    }

    private void update(int channelIndex, double value) {
        for (int i=0; i<emgDataWindows.length-1; i++) {
            emgDataWindows[channelIndex][i]= emgDataWindows[channelIndex][i+1];
        }
        emgDataWindows[channelIndex][slidingWindowSize-1] = value;
    }
}
