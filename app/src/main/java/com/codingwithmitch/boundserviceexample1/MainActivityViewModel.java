package com.codingwithmitch.boundserviceexample1;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class MainActivityViewModel extends ViewModel {
    Intent intent;
    private static final String TAG = "MainActivityViewModel";

    private MutableLiveData<Boolean> mIsProgressBarUpdating = new MutableLiveData<>();
    private MutableLiveData<MyService.MyBinder> mBinder = new MutableLiveData<>();
    private MutableLiveData<Boolean> mRX = new MutableLiveData<>();

    private MutableLiveData<Boolean>mIsConnected = new MutableLiveData<>();

    // Keeping this in here because it doesn't require a context
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            Log.d(TAG, "ServiceConnection: connected to service.");
            // We've bound to MyService, cast the IBinder and get MyBinder instance
            MyService.MyBinder binder = (MyService.MyBinder) iBinder;
            mBinder.postValue(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "ServiceConnection: disconnected from service.");
            mBinder.postValue(null);
        }
    };


    public ServiceConnection getServiceConnection(){
        return serviceConnection;
    }

    public LiveData<MyService.MyBinder> getBinder(){
        return mBinder;
    }


    public LiveData<Boolean> getIsProgressBarUpdating(){
        return mIsProgressBarUpdating;
    }


    public LiveData<Boolean> getIsConnected(){
        Log.i(TAG, "Get connection");
        return mIsConnected;
    }

    public LiveData<Boolean> getRX(){
        Log.i(TAG, "Get RX");
        return mRX;
    }


    public void setRX(boolean text){
        Log.i(TAG, "CheckConnection");

        mRX.postValue(text);
    }


    public void setIsConnected(boolean isConnected){
        Log.i(TAG, "CheckConnection");
        mIsConnected.postValue(isConnected);
    }


    public void setIsProgressBarUpdating(boolean isUpdating){
        mIsProgressBarUpdating.postValue(isUpdating);
    }







}
















