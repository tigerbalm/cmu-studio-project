package com.lge.notyet.driver.business;

import com.lge.notyet.channels.MakeReservationRequestChannel;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.driver.manager.SessionManager;
import com.lge.notyet.driver.util.Log;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;


public class MakeReservationTask implements Callable<Void> {

    private static final String LOG_TAG = "MakeReservationTask";

    private final int mFacilityId;
    private final long mRequestTime;
    private final ITaskDoneCallback mTaskDoneCallback;

    private MakeReservationTask(int facilityId, long timeStamp, ITaskDoneCallback taskDoneCallback) {
        mFacilityId = facilityId;
        mRequestTime = timeStamp;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();

        MakeReservationRequestChannel rc = ncm.createReservationChannel(mFacilityId);
        rc.addObserver(mMakeReservationResult);
        rc.addTimeoutObserver(mMakeReservationTimeout);

        boolean ret = rc.request(MakeReservationRequestChannel.createRequestMessage(SessionManager.getInstance().getKey(), mRequestTime));
        if (mTaskDoneCallback != null && !ret) {
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }

        return null;
    }

    // Business Logic here, we have no time :(
    private final IOnResponse mMakeReservationResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            try {
                Log.logv(LOG_TAG, "mMakeReservationResult Result=" + message.getMessage());
                if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);

            } catch (Exception e) {
                e.printStackTrace();
                if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
            }
        }
    };

    private final IOnTimeout mMakeReservationTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            Log.logd(LOG_TAG, "Failed to send Message=" + message);
            if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(int facilityId, long timeStamp, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new MakeReservationTask(facilityId, timeStamp, taskDoneCallback));
    }
}
