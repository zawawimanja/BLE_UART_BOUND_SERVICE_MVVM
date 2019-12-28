package com.codingwithmitch.boundserviceexample1;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

public class Main3Activity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private MyService mService ;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect,btnSend;
    private EditText edtMessage;

    // UI Components
    private ProgressBar mProgressBar;
    private TextView mTextView,mTextView1;
    private Button mButton;
    String deviceAddress;

    // Vars
//    private MyService mService;
    private MainActivityViewModel mViewModel;
    private MainActivity mainActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mProgressBar = findViewById(R.id.progresss_bar);
        mTextView = findViewById(R.id.text_view);
        mButton = findViewById(R.id.toggle_updates);
        mTextView1 = findViewById(R.id.text_view1);



        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend=(Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();
        //toggleUpdates();




        // Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.sendText);
                String message = editText.getText().toString();
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });



        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        setObservers();


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleUpdates1();
            }
        });



    }

    private void toggleUpdates(){
        if(mService != null){
            if(mService.getProgress() == mService.getMaxValue()){
                mService.resetTask();
                mButton.setText("Start");

            }
            else{
                if(mService.getIsPaused()){
                    mService.unPausePretendLongRunningTask();
                    mViewModel.setIsProgressBarUpdating(true);
                }
                else{
                    mService.pausePretendLongRunningTask();
                    mViewModel.setIsProgressBarUpdating(false);
                }
            }

        }
    }




    private void toggleUpdates1(){
        if(mService != null){

            if(mService.getRXValue()!=null){

                mViewModel.setRX(true);
            }

            else{


                mViewModel.setRX(false);
            }



        }

    }


    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((MyService.MyBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };


    private void setObservers(){
        mViewModel.getBinder().observe(this, new Observer<MyService.MyBinder>() {
            @Override
            public void onChanged(@Nullable MyService.MyBinder myBinder) {
                if(myBinder == null){
                    Log.d(TAG, "onChanged: unbound from service");
                }
                else{
                    Log.d(TAG, "onChanged: bound to service.");
                    mService = myBinder.getService();
                }
            }
        });

        mViewModel.getIsProgressBarUpdating().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean aBoolean) {
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(mViewModel.getIsProgressBarUpdating().getValue()){
                            if(mViewModel.getBinder().getValue() != null){ // meaning the service is bound
                                if(mService.getProgress() == mService.getMaxValue()){
                                    mViewModel.setIsProgressBarUpdating(false);
                                }
                                mProgressBar.setProgress(mService.getProgress());
                                mProgressBar.setMax(mService.getMaxValue());
                                String progress =
                                        String.valueOf(100 * mService.getProgress() / mService.getMaxValue()) + "%";
                                Log.i(TAG, "ProgressMain"+progress);
                                mTextView.setText(progress);
                            }
                            handler.postDelayed(this, 100);
                        }
                        else{
                            handler.removeCallbacks(this);
                        }
                    }
                };

                // control what the button shows
                if(aBoolean){
                    mButton.setText("Pause");
                    handler.postDelayed(runnable, 100);

                }
                else{
                    if(mService.getProgress() == mService.getMaxValue()){
                        mButton.setText("Restart");
                    }
                    else{
                        mButton.setText("Start");
                    }
                }
            }
        });

        mViewModel.getRX().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean aBoolean) {
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(mViewModel.getRX().getValue()){
                            if(mViewModel.getBinder().getValue() != null){ // meaning the service is bound

                                if(mService.getRXValue()==null){
                                    mViewModel.setRX(false);
                                }

                                String progress = mService.getRXValue();
                                if(progress.equals("Y")){
                                    Log.i(TAG, "ProgressRX"+progress);
                                    mTextView1.setText("B");
                                }else{
                                    mTextView1.setText(" ");
                                }

                            }
                            handler.postDelayed(this, 100);
                        }
                        else{
                            handler.removeCallbacks(this);
                        }
                    }
                };

                // control what the button shows
                if(aBoolean){
                    mButton.setText("Pause");
                    handler.postDelayed(runnable, 100);

                }
                else{
                    if(mService.getProgress() == mService.getMaxValue()){
                        mButton.setText("Restart");
                    }
                    else{
                        mButton.setText("Start");
                    }
                }
            }
        });



    }


    @Override
    protected void onResume() {
        super.onResume();
        startService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mViewModel.getBinder() != null){
            unbindService(mViewModel.getServiceConnection());
        }
    }


    ///////////////////////////////////////////////////////////////////////////////

    private void startService(){
        Intent serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);

        bindService();
    }

    private void bindService(){
        Intent serviceBindIntent =  new Intent(this, MyService.class);
        bindService(serviceBindIntent, mViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, MyService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);


    }

    ////////////////////// all 3 method for onresume



    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    public void go(View view){


        Intent dash = new Intent(getApplicationContext(), Main3Activity.class);
        startActivity(dash);
//        mTextView1.setText(mService.getRXValue());
//        Log.i(TAG, "RXVALUE"+mService.getRXValue());
    }

    @Override
    public void onBackPressed() {


        super.onBackPressed();
    }





}














