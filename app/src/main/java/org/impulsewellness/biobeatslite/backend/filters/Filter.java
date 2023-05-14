package org.impulsewellness.litebest.backend.filters;

import android.util.Log;

import org.impulsewellness.litebest.backend.EMGData;

import java.util.ArrayList;

abstract class Filter implements FilterInterface {
    private Filter next;

    protected int inputNumChannels;
    protected int outputNumChannels;

    protected int inputDataLength;
    protected int outputDataLength;

    protected int inputFrequency;
    protected int outputFrequency;

    private EMGData filteredData;

    public Filter() {
        outputNumChannels = inputNumChannels;
        outputDataLength = inputDataLength;
        outputFrequency = inputFrequency;
        inputNumChannels = EMGData.getNumChannels();
        inputDataLength = EMGData.getDataLength();
        inputFrequency = EMGData.getFrequency();
        filteredData = new EMGData();
        next = null;
    }

    public Filter(Filter filter) {
        if (filter == null) {
            inputNumChannels = EMGData.getNumChannels();
            inputDataLength = EMGData.getDataLength();
            inputFrequency = EMGData.getFrequency();
        } else {
            inputNumChannels = filter.outputNumChannels;
            inputDataLength = filter.outputDataLength;
            inputFrequency = filter.outputFrequency;
        }
        outputNumChannels = inputNumChannels;
        outputDataLength = inputDataLength;
        outputFrequency = inputFrequency;
        filteredData = new EMGData();

        next = filter;
    }

    abstract EMGData filterData(EMGData data);

    public EMGData getFilteredData() {
        return filteredData;
    }

    public void queFilterData(EMGData data) {
        // filter the data with this filter
        filteredData = filterData(data);
        // send filtered data to next filter in pipeline
        if (next != null) {
            next.queFilterData(filteredData);
        }
    }
}
