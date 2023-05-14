package org.impulsewellness.biobeatslite.backend.filters;

import static org.impulsewellness.biobeatslite.backend.Constants.EMG_DATA_KEY;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import org.impulsewellness.biobeatslite.backend.EMGData;
import org.impulsewellness.biobeatslite.backend.EMGViewModel;

import java.lang.ref.WeakReference;

public class FilteringHandlerThread extends HandlerThread {

    private static final String TAG = "FilteringHandlerThread";

    private FilterHandler filterHandler;
    private WeakReference<EMGViewModel> emgViewModelWeakReference;

    // Filters to be used
    //private DownSamplingFilter downsFilter = new DownSamplingFilter(4, null);
    //private HilbertTransformFilter hilbertFilter = new HilbertTransformFilter(2000, null);
    private SmoothingFilter smoothingFilter = new SmoothingFilter(3, null);
    //private PowerFilter powerFilter = new PowerFilter(2, smoothingFilter);
    private RectifyFilter rectifyFilter = new RectifyFilter(smoothingFilter);
    //private NotchFilter notchFilter = new NotchFilter(2, 59, 61, 9000, rectifyFilter);
    private HighPassFilter highPassFilter = new HighPassFilter(2, 1, 1000, rectifyFilter);

    // stat stuff
    private StatisticsFilter statisticsFilter = new StatisticsFilter(null);

    public FilteringHandlerThread(EMGViewModel model) {
        super(TAG);
        emgViewModelWeakReference = new WeakReference<EMGViewModel>(model);
        emgViewModelWeakReference.get().addStatSource(statisticsFilter.getMaxAmpsLiveData(), statisticsFilter.getContractionTimesLiveData());
    }

    // Get a reference to worker thread's handler after looper is prepared
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        filterHandler = new FilterHandler(getLooper());
    }

    // used by UI thread to send message to this filtering thread's message queue
    public void sendEmgData(EMGData data){
        // package message
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putDoubleArray(EMG_DATA_KEY, data.getDataFlat());
        msg.setData(bundle);
        // send to FilterHandler
        filterHandler.sendMessage(msg);
    }

    // this class is used to define
    private class FilterHandler extends Handler {
        public FilterHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // unpack msg, un-filtered data
            EMGData emgData = new EMGData();
            emgData.setData(msg.getData().getDoubleArray(EMG_DATA_KEY));
            // do the filtering
            highPassFilter.queFilterData(emgData);
            emgData = smoothingFilter.getFilteredData();
            // compute live stat stuff
            statisticsFilter.queFilterData(emgData);
            // pack filtered data into message
            Bundle bundle = new Bundle();
            bundle.putDoubleArray(EMG_DATA_KEY, emgData.getDataFlat());
            Message newMsg = new Message();
            newMsg.setData(bundle);
            // send data back to view model
            emgViewModelWeakReference.get().updateFilteredLiveData(newMsg);
        }
    }
}
