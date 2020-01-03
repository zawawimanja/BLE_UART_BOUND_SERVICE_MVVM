package com.codingwithmitch.boundserviceexample1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.codingwithmitch.boundserviceexample1.service.MyService;

import static com.codingwithmitch.boundserviceexample1.service.MyService.ACTION_GATT_CONNECTED;
import static com.codingwithmitch.boundserviceexample1.service.MyService.ACTION_GATT_DISCONNECTED;
import static com.codingwithmitch.boundserviceexample1.service.MyService.ACTION_GATT_SERVICES_DISCOVERED;

public class MainActivityViewModel extends ViewModel {

    private static final String TAG = "MainActivityViewModel";
    private MutableLiveData<String> scoreTeamA;
    private MutableLiveData<Integer> scoreTeamB;
    private MutableLiveData<String> gameTitle;
    private MutableLiveData<String> teamAName;
    private MutableLiveData<String> teamBName;
    private LiveData<String> scoreStringTeamA;
    private LiveData<String> scoreStringTeamB;


    private MutableLiveData<MyService.MyBinder> mBinder = new MutableLiveData<>();
    private MutableLiveData<Boolean> mRX = new MutableLiveData<>();
    private MutableLiveData<Boolean> mConnection = new MutableLiveData<>();

    MyService myService=new MyService();

    // Connection states Connecting, Connected, Disconnecting, Disconnected etc.
    private final MutableLiveData<String> mRXValue = new MutableLiveData<>();

    // Flag to determine if the device is connected
    private final MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();

    // Flag to determine if the device has required services
    private final MutableLiveData<Boolean> mIsSupported = new MutableLiveData<>();

    // Flag to determine if the device is ready
    private final MutableLiveData<Void> mOnDeviceReady = new MutableLiveData<>();

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



    public MainActivityViewModel() {

        scoreTeamA = new MutableLiveData<>();
      //  scoreTeamA.setValue("awi");

    }
    public void addToTeamA(String amount) {

        scoreTeamA.postValue(amount);
    }

    public LiveData<String> getScoreTeamA() {
        return scoreTeamA;
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
        Log.i(TAG, "Get Connection");

  

        return mConnection;
    }


    public void setConnection(boolean text){
        Log.i(TAG, "CheckConnection");

        mConnection.postValue(text);
    }



    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }



  public void setIsConnected(){

       if( myService.getConnectionService()==ACTION_GATT_CONNECTED){
           mIsConnected.postValue(true);
       }
      if( myService.getConnectionService()==ACTION_GATT_DISCONNECTED){
          mIsConnected.postValue(false);
      }

  }


    public void setIsServiceDiscover(){

        if( myService.getConnectionService()==ACTION_GATT_SERVICES_DISCOVERED){
            mIsConnected.postValue(true);
        }


    }

    public void setRXValueViewModel(String text){

        mRXValue.postValue(text);
    }

    public LiveData<String> getRXValueViewModel() {
        return mRXValue;
    }




}
















