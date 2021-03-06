package com.lge.notyet.attendant.business;

import com.lge.notyet.channels.LoginRequestChannel;
import com.lge.notyet.attendant.manager.NetworkConnectionManager;
import com.lge.notyet.attendant.util.Log;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class LoginTask implements Callable<Void> {

    private static final String LOG_TAG = "LoginTask";

    private final String mUserEmailAddress;
    private final String mPassWord;
    private final ITaskDoneCallback mTaskDoneCallback;

    private LoginTask(String userEmailAddress, String passWord, ITaskDoneCallback taskDoneCallback) {
        mUserEmailAddress = userEmailAddress;
        mPassWord = passWord;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();

        LoginRequestChannel lc = ncm.createLoginChannel();
        lc.addObserver(mLoginResult);
        lc.addTimeoutObserver(mLoginTimeout);

        boolean ret = lc.request(LoginRequestChannel.createRequestMessage(mUserEmailAddress, mPassWord));
        if (mTaskDoneCallback != null && !ret) {
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
        return null;
    }

    // Business Logic here, we have no time :(
    private final IOnResponse mLoginResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            try {
                Log.logv(LOG_TAG, "mLoginResult Result=" + message.getMessage());
                mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);

            } catch (Exception e) {
                e.printStackTrace();
                if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
            }
        }
    };

    private final IOnTimeout mLoginTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            Log.logd(LOG_TAG, "Failed to send Message=" + message);
            if (mTaskDoneCallback != null) mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String userEmailAddress, String passWord, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new LoginTask(userEmailAddress, passWord, taskDoneCallback));
    }
}
