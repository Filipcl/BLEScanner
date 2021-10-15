package com.example.myapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "MainActivity";

   BluetoothAdapter mBluetoothAdapter;
    Button btnDiscoverable;
    Button btnDiscover;
    public ArrayList<BluetoothDevice> mDevices = new ArrayList<>();
    DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    /*private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    BluetoothGatt mBluetoothGatt;
    String address = "C2:1C:80:AE:F2:A0";

     */

    private final BroadcastReceiver mBrodcastReciver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "OnReceiver STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBrodcastRecivies1 STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBrodcastRecivies1 STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBrodcastRecivies1 STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBrodcastReciver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBrodcastReciver2 discoverability enable");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBrodcastRecivies2 discoverability disable,  able to make connection with paired device");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBrodcastRecivies2 discoverability disable,  not able to make connection\"");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBrodcastReciver2 Connrcting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBrodcastReciver2 connected.");
                        break;
                }
            }
        }
    };


    private final BroadcastReceiver mBrodcastReciver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

                // Sjekker om vi finner sensoren!
                if(device.getAddress().equals("C2:1C:80:AE:F2:A0")){
                    Log.d(TAG,"HEI; JEG FANT SENSOREN!!!");
                }
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };
    private final BroadcastReceiver mBrodcastReciver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Case1: if alredy bonded
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BOND_BONDED");
                }
                //Case2: creating a bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "BOND_BONDING");
                }
                //Case3: if bond is broken
                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "BOND_NONE");
                }
            }
        }
    };

    @Override
   protected void onDestroy(){
        Log.d(TAG, "OnDestroy called");
       super.onDestroy();
        unregisterReceiver(mBrodcastReciver1);
        unregisterReceiver(mBrodcastReciver2);
        unregisterReceiver(mBrodcastReciver3);
        unregisterReceiver(mBrodcastReciver4);
   }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnOnOff = (Button) findViewById(R.id.btnONOFF);
        btnDiscoverable = (Button) findViewById(R.id.btnDiscoverable);
        btnDiscover = (Button) findViewById(R.id.btnDiscover);

        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        mDevices = new ArrayList<>();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBrodcastReciver4, filter);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        lvNewDevices.setOnItemClickListener(MainActivity.this);


        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth");
                enableDisableBT();
            }

        });
        btnDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth");
                btnDiscoverablen();
            }

        });
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: discover bluetooth devices");
                discoverNewDevices();
            }

        });
    }

    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT : Does not have BT capability");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "Enable BT");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBrodcastReciver1, BTIntent);
        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "disable BT");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBrodcastReciver1, BTIntent);
        }
    }

    public void btnDiscoverablen() {
        Log.d(TAG, "Making device discoverable for 300 seconds");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBrodcastReciver2, intentFilter);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void discoverNewDevices(){
        Log.d(TAG, "Discovering new devices");
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: canceling discovery");

            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBrodcastReciver3, discoverDevicesIntent);

        }
        if(!mBluetoothAdapter.isDiscovering()){
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBrodcastReciver3, discoverDevicesIntent);
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    //Her vil jeg da hente ut dataen fra sensoren!!!!
    //Jeg trenger da å sette opp en UART kommunikasjon for å få dette til

    @Override
    public void onItemClick(AdapterView<?> adapterView , View view , int i , long l) {
        mBluetoothAdapter.cancelDiscovery();
        Log.d(TAG, "You clicked on a device");
        String deviceName = mDevices.get(i).getName();
        String deviceAddress = mDevices.get(i).getAddress();

        Log.d(TAG,"onClick device: devicename = " + deviceName);
        Log.d(TAG,"onClick device: deviceAddress = " + deviceAddress);
        Log.d(TAG, "onClick device: type = " + mDevices.get(i).getType());
        Log.d(TAG, "onClick device: fetchUUID =" + mDevices.get(i).fetchUuidsWithSdp());


        //mDevices.get(i).connectGatt(this, true, gattCallback);


        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG,"Trying to pair with " + deviceName);
            mDevices.get(i).createBond();
        }
    }

}