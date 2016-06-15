package com.lge.notyet.driver.business;

import com.lge.notyet.channels.ModifyAccountRequestChannel;
import com.lge.notyet.channels.SignUpRequestChannel;
import com.lge.notyet.driver.manager.NetworkConnectionManager;
import com.lge.notyet.driver.ui.ITaskDoneCallback;
import com.lge.notyet.lib.comm.*;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by beney.kim on 2016-06-16.
 */
public class ModifyAccountTask implements Callable<Void>  {

    private String mSessionKey;
    private String mUserEmailAddress;
    private String mPassWord;
    private String mCreditCardNumber;
    private String mCreditCardExpireDate;
    private String mCreditCardCvc;
    private ITaskDoneCallback mTaskDoneCallback;

    public ModifyAccountTask(String sessionKey, String userEmailAddress, String passWord, String creditCardNumber, String creditCardExpireDate, String creditCardCvc, ITaskDoneCallback taskDoneCallback) {
        mSessionKey = sessionKey;
        mUserEmailAddress = userEmailAddress;
        mPassWord = passWord;
        mCreditCardNumber = creditCardNumber;
        mCreditCardExpireDate = creditCardExpireDate;
        mCreditCardCvc = creditCardCvc;
        mTaskDoneCallback = taskDoneCallback;
    }

    @Override
    public Void call() throws Exception {

        NetworkConnectionManager ncm = NetworkConnectionManager.getInstance();
        ncm.open();

        ModifyAccountRequestChannel sc = ncm.createModifyAccountRequestChannel();
        sc.addObserver(mModifyAccountResult);
        sc.addTimeoutObserver(mModifyAccountTimeout);

        sc.request(sc.createRequestMessage(mSessionKey, mUserEmailAddress, mPassWord, mCreditCardNumber, mCreditCardExpireDate, mCreditCardCvc));
        return null;
    }

    // Business Logic here, we have no time :(
    private IOnResponse mModifyAccountResult = new IOnResponse() {

        @Override
        public void onResponse(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {

            // Need to parse
            // ReservationResponseMessage result = (ReservationResponseMessage) message;
            System.out.println("mModifyAccountResult Result=" + message.getMessage());
            mTaskDoneCallback.onDone(ITaskDoneCallback.SUCCESS, message);
        }
    };

    private IOnTimeout mModifyAccountTimeout = new IOnTimeout() {

        @Override
        public void onTimeout(NetworkChannel networkChannel, NetworkMessage message) {
            System.out.println("Failed to send Message=" + message);
            mTaskDoneCallback.onDone(ITaskDoneCallback.FAIL, null);
        }
    };

    public static FutureTask<Void> getTask(String sessionKey, String userEmailAddress, String passWord, String creditCardNumber, String creditCardExpireDate, String creditCardCvc, ITaskDoneCallback taskDoneCallback) {
        return new FutureTask<>(new ModifyAccountTask(sessionKey, userEmailAddress, passWord, creditCardNumber, creditCardExpireDate, creditCardCvc, taskDoneCallback));
    }
}