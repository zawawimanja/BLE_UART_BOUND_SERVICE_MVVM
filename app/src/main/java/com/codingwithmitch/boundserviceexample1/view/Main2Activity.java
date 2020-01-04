package com.codingwithmitch.boundserviceexample1.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import com.codingwithmitch.boundserviceexample1.R;
import com.codingwithmitch.boundserviceexample1.service.MyService;
import com.codingwithmitch.boundserviceexample1.viewmodel.MainActivityViewModel;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

public class Main2Activity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int UART_PROFILE_DISCONNECTED = 21;

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
    Button connection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mProgressBar = findViewById(R.id.progresss_bar);
        mTextView = findViewById(R.id.text_view);
        mButton = findViewById(R.id.toggle_updates);
        mTextView1 = findViewById(R.id.text_view1);
        connection=findViewById(R.id.connection);



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


        //nak enable auto real time
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                toggleUpdates1();
            }
        }, 1000);

        new CountDownTimer(5000, 100) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {

            }
        }.start();


    }


    public void stop(View view){

        mViewModel.setRX(false);
    }





    private void toggleUpdates1(){
        if(mService != null){

            if(mService.getRXValue()!=null){

//                mViewModel.setRXValueViewModel(mService.getRXValue());
               mViewModel.setRX(true);
              //mViewModel.setConnection(true);
               // mViewModel.setIsConnected();
               mViewModel.setIsConnected(mService.getConnectionService());
                mViewModel.setIsServiceDiscover(mService.getServiceDiscover());
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
                    mService = (MyService) myBinder.getService();
                }
            }
        });


//
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

                                mTextView1.setText(progress);

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
                    Log.i(TAG,"Continue");
                    handler.postDelayed(runnable, 100);

                    if(mService.getRXValue()==null){

                        handler.removeCallbacks(runnable);
                    }

                }
                else{

                }
            }
        });




        mViewModel.getConnection().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean aBoolean) {
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(mViewModel.getConnection().getValue()){
                            if(mViewModel.getBinder().getValue() != null){ // meaning the service is bound

                                if(mService.getConnectionService()==null){
                                    mViewModel.setConnection(false);
                                }

                                String progress = mService.getConnectionService();
                                Log.i(TAG, "ConnectionState"+progress);

                                connection.setText(progress);
                                // showMessage(progress);


                            }
                            handler.postDelayed(this, 100);
                        }
                        else{


                            handler.removeCallbacks(this);

                        }
                    }
                };

                //  control what the button shows
                if(aBoolean){
                    //

                    handler.postDelayed(runnable, 100);

                }
                else{

                }
            }
        });


        mViewModel.getIsServiceDiscover().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean aBoolean) {
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(mViewModel.getIsServiceDiscover().getValue()){
                            if(mViewModel.getBinder().getValue() != null){ // meaning the service is bound




                             int progress = mService.getServiceDiscover();
                              Log.i(TAG, "ServiceState"+progress);
//
//                                connection.setText(progress);
//                                // showMessage(progress);


                            }
                            handler.postDelayed(this, 100);
                        }
                        else{


                            handler.removeCallbacks(this);

                        }
                    }
                };

                //  control what the button shows
                if(aBoolean){
                    mService.enableTXNotification();

                    handler.postDelayed(runnable, 100);

                }
                else{

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



}














