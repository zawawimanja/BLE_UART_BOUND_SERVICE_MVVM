package com.codingwithmitch.boundserviceexample1.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.codingwithmitch.boundserviceexample1.service.MyService;

public class MainActivityViewModel extends AndroidViewModel {

    private static final String TAG = "MainActivityViewModel";

    private MutableLiveData<Boolean> mIsProgressBarUpdating = new MutableLiveData<>();
    private MutableLiveData<MyService.MyBinder> mBinder = new MutableLiveData<>();
    private MutableLiveData<Boolean> mRX = new MutableLiveData<>();
    private MutableLiveData<Boolean> mConnection = new MutableLiveData<>();

    private MutableLiveData<String> mRXData = new MutableLiveData<>();

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

    public MainActivityViewModel(@NonNull final Application application) {
        super(application);


    }


    public void setIsProgressBarUpdating(boolean isUpdating){
        mIsProgressBarUpdating.postValue(isUpdating);
    }



    public LiveData<Boolean> getRX(){
        Log.i(TAG, "Get RX");
        return mRX;
    }


    public void setRX(boolean text){
        Log.i(TAG, "CheckConnection");

        mRX.postValue(text);
    }


    public LiveData<Boolean> getConnection(){
        Log.i(TAG, "Get RX");
        return mConnection;
    }


    public void setConnection(boolean text){
        Log.i(TAG, "CheckConnection");

        mConnection.postValue(text);
    }










}
















