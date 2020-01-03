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





//        // Handle Send button
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EditText editText = (EditText) findViewById(R.id.sendText);
//                String message = editText.getText().toString();
//                byte[] value;
//                try {
//                    //send data to service
//                    value = message.getBytes("UTF-8");
//                    mService.writeRXCharacteristic(value);
//                    //Update the log with time stamp
//                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
//                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                    edtMessage.setText("");
//                } catch (UnsupportedEncodingException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//            }
//        });



        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        setObservers();


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mViewModel.setRXValueViewModel(mService.getRXValue());
            }
        }, 1000);


        //

        new CountDownTimer(5000, 100) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {

            }
        }.start();

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


        mViewModel.getRXValueViewModel().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String aBoolean) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(mViewModel.getRXValueViewModel()!=null){
                            if(mViewModel.getBinder().getValue() != null){ // meaning the service is bound



                                String progress = mService.getRXValue();
                                if(progress.equals("X")){
                                    Log.i(TAG, "ProgressRX"+progress);
                                    mTextView1.setText("a");
                                }else{
                                    mTextView1.setText(" ");
                                }
                                mTextView1.setText(aBoolean);

                            }

                        }

                    }
                });
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
     bindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mViewModel.getBinder() != null){
            unbindService(mViewModel.getServiceConnection());
        }
    }


    ///////////////////////////////////////////////////////////////////////////////

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














