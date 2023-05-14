package org.impulsewellness.biobeatslite.backend.threads;

import static org.impulsewellness.biobeatslite.backend.Constants.EMG_MSG;
import static org.impulsewellness.biobeatslite.backend.Constants.HEADER_MSG;
import static org.impulsewellness.biobeatslite.backend.Constants.NEO_MSG;
import static org.impulsewellness.biobeatslite.backend.Constants.EMG_DATA_KEY;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.impulsewellness.biobeatslite.backend.Constants;
import org.impulsewellness.biobeatslite.backend.EMGData;
import org.impulsewellness.biobeatslite.backend.EMGViewModel;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RecodingHandlerThread extends HandlerThread {
    private static final String TAG = "RecordingHandlerThread";

    public static final String SESSION_NAME_KEY = "org.impulsewellness.litebest.recordingthread.SESSION_NAME_KEY";
    public static final String RECORDING_TYPE_KEY = "org.impulsewellness.litebest.recordingthread.RECORDING_TYPE_KEY";
    public static final String HARDWARE_ID_KEY = "org.impulsewellness.litebest.recordingthread.HARDWARE_ID_KEY";
    public static final String BODY_SCORE_KEY = "org.impulsewellness.litebest.recordingthread.BODY_SCORE_KEY";
    public static final String GAME_SCORE_KEY = "org.impulsewellness.litebest.recordingthread.GAME_SCORE_KEY";
    public static final String NUMBER_CONTRACTIONS_KEY = "org.impulsewellness.litebest.recordingthread.NUMBER_CONTRACTIONS_KEY";

    private RecordingHandler recordingHandler;
    private WeakReference<EMGViewModel> emgViewModelWeakReference;

    // file constants
    private static final String NEO_FILE_NAME = "neo.fst";
    private static final String FILE_PATH = "/data/data/org.impulsewellness.litebest/files/";
    private static final String SESSION_FILE_ENDING = ".data";


    // used by UI thread to send message to this recording thread's message queue
    public void sendEmgData(EMGData data){
        // package message
        Bundle bundle = new Bundle();
        bundle.putDoubleArray(EMG_DATA_KEY, data.getDataFlat());
        Message msg = new Message();
        msg.what = EMG_MSG;
        msg.setData(bundle);
        // send to FilterHandler
        recordingHandler.sendMessage(msg);
    }

    public void sendHeaderData(String sessionName, String recordingType, String hardwareID) {
        Bundle bundle = new Bundle();
        bundle.putString(SESSION_NAME_KEY, sessionName);
        bundle.putString(RECORDING_TYPE_KEY, recordingType);
        bundle.putString(HARDWARE_ID_KEY, hardwareID);
        Message msg = new Message();
        msg.what = HEADER_MSG;
        msg.setData(bundle);

        recordingHandler.sendMessage(msg);
    }

    /**
     * Used by the UI thread to send "trend" data to be saved after final score calculations.
     * @param wellnessScore
     * @param gameScore
     * @param numContractions
     */
    public void sendNeoData(Double wellnessScore, Double gameScore, Double numContractions) {
        // bundle needed data for neo file
        Bundle bundle = new Bundle();
        bundle.putDouble(BODY_SCORE_KEY, wellnessScore);
        bundle.putDouble(GAME_SCORE_KEY, gameScore);
        bundle.putDouble(NUMBER_CONTRACTIONS_KEY, numContractions);
        Message msg =  new Message();
        msg.what = NEO_MSG;
        msg.setData(bundle);

        recordingHandler.sendMessage(msg);
    }

    public RecodingHandlerThread(WeakReference<EMGViewModel> emgViewModelWeakReference) {
        super(TAG);
        this.emgViewModelWeakReference = emgViewModelWeakReference;
    }

    protected void onLooperPrepared() {
        super.onLooperPrepared();
        recordingHandler = new RecordingHandler(getLooper());
    }

    private class RecordingHandler extends Handler {
        private FileWriter sessionFileWriter;
        private FileWriter neoFileWriter;
        private String sessionFileName;

        public RecordingHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle msgBundle = msg.getData();
            switch (msg.what){
                case HEADER_MSG:
                    String sessionName = msgBundle.getString(SESSION_NAME_KEY);
                    String recordingType = msgBundle.getString(RECORDING_TYPE_KEY);
                    String hardwareID = msgBundle.getString(HARDWARE_ID_KEY);
                    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMATTER_FORMAT));
                    sessionFileName = time + "-" + sessionName;
                    try {
                        sessionFileWriter = new FileWriter(FILE_PATH + sessionFileName + SESSION_FILE_ENDING);
                        sessionFileWriter.write("Name: " + sessionName +
                                "\nRecordingType: " + recordingType +
                                "\nHardwareID: " + hardwareID + "\n---\n");
                    } catch(IOException ioe) {
                        Log.v(TAG, "Received HEADER_MSG. set-up sessionFileWriter failed.");
                        ioe.getStackTrace();
                    }

                    break;
                case EMG_MSG:
                    EMGData emgData = new EMGData();
                    double[] data = msgBundle.getDoubleArray(EMG_DATA_KEY);
                    emgData.setData(data);

                    try {
                        sessionFileWriter.write(emgData.dataToString());
                    } catch(IOException ioe) {
                        ioe.getStackTrace();
                    }
                    break;
                case NEO_MSG:
                    /*
                        NEO FILE STRUCT
                        Session File Name,  GameScore,  BodyScore, NumContractions \n
                     */
                    double wellnessScore = msgBundle.getDouble(BODY_SCORE_KEY);
                    double gameScore = msgBundle.getDouble(GAME_SCORE_KEY);
                    double numContractions = msgBundle.getDouble(NUMBER_CONTRACTIONS_KEY);

                    try {
                        sessionFileWriter.close();
                        neoFileWriter = new FileWriter(FILE_PATH + NEO_FILE_NAME, true);
                        neoFileWriter.write(sessionFileName + ", " + wellnessScore + ", " + gameScore + ", " + numContractions + " \n");
                        neoFileWriter.close();
                    } catch (IOException ioe) {
                        ioe.getStackTrace();
                    }
                    break;
                default:
                    Log.e(TAG, "Handler received unknown/null message type");
            }
        }
    }
}