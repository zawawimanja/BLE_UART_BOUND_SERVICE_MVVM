package com.codingwithmitch.boundserviceexample1.view;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
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
import com.codingwithmitch.boundserviceexample1.data.DataManager;
import com.codingwithmitch.boundserviceexample1.service.MyService;
import com.codingwithmitch.boundserviceexample1.viewmodel.MainActivityViewModel;
import com.codingwithmitch.boundserviceexample1.viewmodel.MainViewModelFactory;

public class Main2Activity extends AppCompatActivity {

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
        setContentView(R.layout.activity_main2);
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


        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend=(Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();



//        // Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.sendText);
                String message = editText.getText().toString();

                mViewModel.setTXValueViewModel(message);
            }
        });


        mViewModel = createViewModel();;
        setObservers();





//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                mViewModel.setRXValueViewModel();
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

    }


    public void start(View view){
        mViewModel.setIsConnected();
        mViewModel.setIsServiceDiscover();
        mViewModel.setRXValueViewModel(mService.getRXValue());



    }

    private MainActivityViewModel createViewModel() {
        MainViewModelFactory factory = new MainViewModelFactory(DataManager.getInstance().getService());
        return ViewModelProviders.of(this, factory).get(MainActivityViewModel.class);
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




        mViewModel.getRXValueViewModel().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String text) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(mViewModel.getRXValueViewModel()!=null){
                            if(mViewModel.getBinder().getValue() != null){ // meaning the service is bound


                                mTextView1.setText(text);

                            }

                        }

                    }
                });

            }
        });


        mViewModel.getTXValueViewModel().observe(this, new Observer<byte[]>() {
            @Override
            public void onChanged(@Nullable final byte[] text) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(mViewModel.getTXValueViewModel()!=null){
                            if(mViewModel.getBinder().getValue() != null){ // meaning the service is bound

                                mService.writeRXCharacteristic(text);

                            }

                        }

                    }
                });

            }
        });


        mViewModel.getConnected().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean aBoolean) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(mViewModel.getConnected()!=null){
                            if(mViewModel.getBinder().getValue() != null){ // meaning the service is bound


                            }
                        }
                    }
                });

                if(aBoolean){
                    Toast.makeText(Main2Activity.this, "Already Connected", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Disconnect");
                }
                else{
                    Toast.makeText(Main2Activity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "connect");
                }
            }
        });



        mViewModel.getServiceDiscover().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean aBoolean) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(mViewModel.getServiceDiscover()!=null){
                            if(mViewModel.getBinder().getValue() != null){ // meaning the service is bound

                                mService.enableTXNotification();

                            }
                        }
                    }
                });
            }
        });

        mViewModel.getDeviceNotSupport().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean aBoolean) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(mViewModel.getDeviceNotSupport()!=null){
                            if(mViewModel.getBinder().getValue() != null){ // meaning the service is bound



                            }
                        }
                    }
                });

                if(aBoolean){
                    Toast.makeText(Main2Activity.this, "Not Support", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Disconnect");
                }
                else{
                    Toast.makeText(Main2Activity.this, "Support", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "connect");
                }
            }
        });






    }


    public void go1(View view){

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

        super.onBackPressed();
    }



}














