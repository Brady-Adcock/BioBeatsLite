package org.impulsewellness.litebest.backend.filters;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.impulsewellness.litebest.backend.EMGData;

import java.util.ArrayList;

public class StatisticsFilter extends Filter {
    private int timeElapsed; // milli seconds
    private MutableLiveData<ArrayList<Double>> maxAmpsLiveData;
    private MutableLiveData<ArrayList<Double>> contractionTimesLiveData;
    private ArrayList<Double> maxAmps;
    private ArrayList<Double> contractionTimes;
    private double oldTimeElapsed;
    private static final String TAG = "StatisticsFilter";

    /*
    TODO: fix these assumptions, use a percentage of Max Voluntary Contraction (MVC) to determine
    threshold for "contraction" rather than just guesstimating. MVC should be determined by calibration
     */
    private final static int CONTRACTION_THRESHOLD = 200;

    public StatisticsFilter(Filter next) {
        super(next);
        timeElapsed = 0;
        maxAmps = new ArrayList<>();
        contractionTimes = new ArrayList<>();
        maxAmpsLiveData = new MutableLiveData<>();
        contractionTimesLiveData = new MutableLiveData<>();
        oldTimeElapsed = 0;

        for (int i = 0; i < EMGData.getNumChannels(); i++) {
            maxAmps.add(0.0);
            contractionTimes.add(0.0);
        }


    }

    @Override
    protected EMGData filterData(EMGData data) {
        //Log.v("StatisticsFilter", "filterData() called");
        double[][] rec_data = data.getData2d();
        for(int i=0; i<EMGData.getNumChannels(); i++) {
            for(int j=0; j<EMGData.getDataLength(); j++){
                if (maxAmps.get(i) < rec_data[i][j]) {
                    maxAmps.set(i, rec_data[i][j]);
                }
                if (rec_data[i][j] > CONTRACTION_THRESHOLD) {
                    contractionTimes.set(i, contractionTimes.get(i) + (1000.0 / EMGData.getFrequency())); // 1000 ms in a sec
                }
            }
        }
        timeElapsed += EMGData.getDataLength() * (1000.0 / EMGData.getFrequency()); // 1000 ms in a sec
        // update maxes and contraction times every second
        if (timeElapsed - oldTimeElapsed > 50) {
            //Log.v("StatisticsFilter", maxAmps.toString());
            maxAmpsLiveData.postValue(maxAmps);
            contractionTimesLiveData.postValue(contractionTimes);
            oldTimeElapsed = timeElapsed;
        }
        return data;
    }

    // TODO:: fix this, should be observable like the maxAmpsLiveData and contractionTimesLiveData
    public int getTimeElapsed() {
        return timeElapsed;
    }

    public LiveData<ArrayList<Double>> getMaxAmpsLiveData() {
        return maxAmpsLiveData;
    }

    public LiveData<ArrayList<Double>> getContractionTimesLiveData() {
        return contractionTimesLiveData;
    }
}