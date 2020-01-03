package com.codingwithmitch.boundserviceexample1.view;

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

import com.codingwithmitch.boundserviceexample1.service.MyService;
import com.codingwithmitch.boundserviceexample1.R;
import com.codingwithmitch.boundserviceexample1.viewmodel.MainActivityViewModel;

import java.text.DateFormat;
import java.util.Date;

import static com.codingwithmitch.boundserviceexample1.service.MyService.ACTION_GATT_CONNECTED;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    private Handler mHandler;
    TextView mRemoteRssiVal,mTextView;
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
    private TextView mTextView2,mTextView1;
    private Button mButton;
    String deviceAddress;


    // Vars
//    private MyService mService;
    private MainActivityViewModel mViewModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mProgressBar = findViewById(R.id.progresss_bar);
        mTextView = findViewById(R.id.text_view);
        mButton = findViewById(R.id.toggle_updates);
        mTextView1 = findViewById(R.id.text_view1);
        mTextView2 = findViewById(R.id.text_view2);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend=(Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();


        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {

                    if (btnConnectDisconnect.getText().equals("Connect")){

                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);

                    } else {
                        //Disconnect button pressed
                        if (mDevice!=null)
                        {
                            mService.disconnect();

                        }
                    }

                }
            }
        });

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

//using handler not work real time for main but for other activites work for real time
//
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                if(mService.getRXValue()!=null){
//                    mViewModel.setRXValueViewModel(mService.getRXValue());
//                }
//            }
//        }, 1000);
//
//
//        //
//
//        new CountDownTimer(5000, 100) {
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            public void onFinish() {
//
//            }
//        }.start();

//        if(mService.getRXValue()!=null){
//        //    mViewModel.addToTeamA(mService.getRXValue());
//        }else{
//
//        }

        mService.getName();





        mButton.setOnClickListener(view -> {

        });


    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((MyService.MyBinder) rawBinder).getService();
            Log.i(TAG, "onServiceConnected mService= " + mService);
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


    int count=0;

    private void setObservers(){
        mViewModel.getBinder().observe(this, new Observer<MyService.MyBinder>() {
            @Override
            public void onChanged(@Nullable MyService.MyBinder myBinder) {
                if(myBinder == null){
                    Log.i(TAG, "onChanged: unbound from service");
                }
                else{
                    Log.i(TAG, "onChanged: bound to service.");
                    mService = myBinder.getService();
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
                                if(progress.equals("X")){
                                    Log.i(TAG, "ProgressRX"+progress);
                                    mTextView1.setText("a");
                                }else{
                                    mTextView1.setText(" ");
                                }
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
                                mTextView2.setText(aBoolean);

                            }

                        }

                    }
                });





            }
        });




//        mViewModel.getConnection().observe(this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(@Nullable final Boolean aBoolean) {
//                final Handler handler = new Handler();
//                final Runnable runnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        if(mViewModel.getConnection().getValue()){
//                            if(mViewModel.getBinder().getValue() != null){ // meaning the service is bound
//
//                                if(mService.getConnectionService()==null){
//                                    mViewModel.setConnection(false);
//                                }
//
//                                String progress = mService.getConnectionService();
//                                Log.i(TAG, "ConnectionState"+progress);
//
//
//                                showMessage(progress);
//                                count++;
//
//                            }
//                            handler.postDelayed(this, 100);
//                        }
//                        else{
//
//
//                                handler.removeCallbacks(this);
//
//                        }
//                    }
//                };
//
//               //  control what the button shows
//                if(aBoolean){
//                //    mButton.setText("Pause");
//
//                    edtMessage.setEnabled(true);
//                    btnSend.setEnabled(true);
//                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
//                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                    listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
//                    btnConnectDisconnect.setText("Disconnect");
//                    edtMessage.setEnabled(true);
//                    btnSend.setEnabled(true);
//
//                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                    mState = UART_PROFILE_CONNECTED;
//
//                    mService.enableTXNotification();
//
//                    handler.postDelayed(runnable, 100);
//
//                }
//                else{
//
//                }
//            }
//        });


   //     mViewModel.getScoreTeamA().observe(this, score -> mTextView2.setText(score));


        mViewModel.getScoreTeamA().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                mTextView1.setText(s);
            }
        });


    }


    public void go1(View view){

        //hardcode success sent when button click & not real time
       //   mViewModel.addToTeamA("t");


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

     LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    ////////////////////// all 3 method for onresume

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(MyService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(MyService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(MyService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(MyService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }


    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.i(TAG, "UART_CONNECT_MSG1");
                        btnConnectDisconnect.setText("Disconnect");
                        edtMessage.setEnabled(true);
                        btnSend.setEnabled(true);
                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                        listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            //*********************//
            if (action.equals(MyService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.i(TAG, "UART_DISCONNECT_MSG1");
                        btnConnectDisconnect.setText("Connect");
                        edtMessage.setEnabled(false);
                        btnSend.setEnabled(false);
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();

                    }
                });
            }


            //*********************//
            if (action.equals(MyService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }


            //*********************//rx
            if (action.equals(MyService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(MyService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            listAdapter.add("["+currentDateTimeString+"] RX: "+text);
                            //reason realtime updated
                            mViewModel.setRXValueViewModel(mService.getRXValue());
                            messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }


            //*********************//
            if (action.equals(MyService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    public void go(View view){
        Intent dash = new Intent(getApplicationContext(), TestActivity.class);
        startActivity(dash);

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                     deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    mService.connect(deviceAddress);


                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        // super.onBackPressed();
    }



}














