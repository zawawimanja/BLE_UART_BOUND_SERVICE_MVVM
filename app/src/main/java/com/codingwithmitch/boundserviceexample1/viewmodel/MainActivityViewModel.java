package com.codingwithmitch.boundserviceexample1.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.codingwithmitch.boundserviceexample1.service.MyService;

import java.io.UnsupportedEncodingException;

import static com.codingwithmitch.boundserviceexample1.service.MyService.ACTION_GATT_CONNECTED;
import static com.codingwithmitch.boundserviceexample1.service.MyService.ACTION_GATT_DISCONNECTED;
import static com.codingwithmitch.boundserviceexample1.service.MyService.DEVICE_DOES_NOT_SUPPORT_UART;

public class MainActivityViewModel extends ViewModel {

    private static final String TAG = "MainActivityViewModel";


    private MutableLiveData<MyService.MyBinder> mBinder = new MutableLiveData<>();
    private MutableLiveData<Boolean> mRX = new MutableLiveData<>();
    private MutableLiveData<Boolean> mConnection = new MutableLiveData<>();

    MyService myService=new MyService();



    private final MutableLiveData<Boolean> mDeviceNotSupport = new MutableLiveData<Boolean>();

    private final MutableLiveData<byte[]> mTXValue = new MutableLiveData<byte[]>();


    private final MutableLiveData<String> mRXValue = new MutableLiveData<>();

    // Flag to determine if the device is connected
    private final MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();


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

    public MainActivityViewModel(MyService myService) {
    }


    public LiveData<MyService.MyBinder> getBinder(){
        return mBinder;
    }

    public ServiceConnection getServiceConnection(){
        return serviceConnection;
    }

    public LiveData<Boolean> getServiceDiscover() {
        return mIsConnected;
    }

    public LiveData<Boolean> getConnected() {
        return mIsConnected;
    }

    public LiveData<String> getRXValueViewModel() {
        return mRXValue;
    }

    public LiveData<byte[]> getTXValueViewModel() {
        return mTXValue;
    }

    public LiveData<Boolean> getDeviceNotSupport() {
        return mDeviceNotSupport;
    }

    public void setDeviceSupported(String text){


//            if(myService.getDeviceNotSupport()=="Not Support"){
//                mDeviceNotSupport.postValue(true);
//            }

        if(text==DEVICE_DOES_NOT_SUPPORT_UART){
            mDeviceNotSupport.postValue(true);
        }

    }


    public void setIsConnected(){

        if(myService.getConnectionService()!=null){

            if(myService.getConnectionService()==ACTION_GATT_CONNECTED){
                mIsConnected.postValue(true);
            }
            if(myService.getConnectionService()==ACTION_GATT_DISCONNECTED){
                mIsConnected.postValue(false);
            }

        }

    }


    public void setIsServiceDiscover(){

                if( myService.getServiceDiscover()==0){
                 mIsConnected.postValue(true);
        }


    }






//    public void setRXValueViewModel(){
//
//        if(myService.getRXValue()!=null){
//
//            mRXValue.postValue(myService.getRXValue());
//        }
//
//    }




    public void setRXValueViewModel(String text){

        mRXValue.postValue(text);
    }




    public void setTXValueViewModel(String text){

        byte[] value;
        try {
            //send data to service
            value = text.getBytes("UTF-8");

            mTXValue.postValue(value);

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }



}
















