package com.example.bluetoothremotecontrol;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class FirstActivity extends AppCompatActivity {

    private Button btn_goto_remote, btn_turn_on_off;
    private static Boolean is_connected = false;
    private SharedPreferences shared_prefs;
    private static final String SHARED_PREFS = "Shared_Prefs_BT_Remote_Control";

    private static final int REQUEST_ENABLE_BT = 0;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = null;
    public static final String EXTRA_ADDRESS = "device_address";
    public static final String MY_DEVICE_NAME = "AirPods";  // TODO  HC-05 Tamir's iPad Mini AirPods LAPTOP-BM35FHI5

    //private ProgressDialog progress;
    //static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is unavailable on your phone", Toast.LENGTH_LONG).show();
        } // else { } // Bluetooth is available


        shared_prefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        address = shared_prefs.getString("address", null);


        btn_goto_remote = (Button) findViewById(R.id.btn_goto_remote);
        if (!is_connected)
            btn_goto_remote.setEnabled(false);
        btn_goto_remote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(FirstActivity.this, MainActivity.class);
                it.putExtra(EXTRA_ADDRESS, address); // this address will be received at MainActivity (class) Activity
                startActivity(it);
                finish();
            }
        });


        btn_turn_on_off = (Button) findViewById(R.id.btn_turn_on_off);
        btn_turn_on_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_connected) {
                    //if (bluetoothAdapter.isEnabled()) { // BT is on
                    Toast.makeText(FirstActivity.this, "disconnecting...", Toast.LENGTH_SHORT).show();

                    if (btSocket != null) { // If the btSocket is busy
                        try {
                            btSocket.close();
                            bluetoothAdapter.disable();
                            btn_turn_on_off.setText(R.string.connect_to_Bluetooth);  // "connect to Bluetooth"
                            is_connected = false;
                            btn_goto_remote.setEnabled(false);
                        } catch (IOException e) {
                            //e.printStackTrace();
                            Toast.makeText(FirstActivity.this, "can't disconnect", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                //} else { // if is not connected
                //if (bluetoothAdapter.isEnabled()) { // BT is on
                //}
                else { // BT is not connected
                    if (bluetoothAdapter.isEnabled()) // if BT is on
                        scanPairedDevices(111);
                    else { // if BT is off
                        Intent it = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(it, REQUEST_ENABLE_BT); // Requesting permission to turn on the BT
                        //btn_turn_on_off.setText("connect to bluetooth");
                    }
                }
                //}
            }
        });

        /*if (bluetoothAdapter.isEnabled() && address != null) { // if BT is on and paired with HC-05
            is_connected = true;
            btn_turn_on_off.setText("disconnect from HC-05");
            btn_goto_remote.setEnabled(true);
            //Intent it = new Intent(FirstActivity.this, MainActivity.class);
            //startActivity(it);
            //finish();
        }*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {  // BT is on
                    Toast.makeText(this, "BT is on", Toast.LENGTH_SHORT).show();
                    scanPairedDevices(999);
                } else
                    Toast.makeText(this, "no permission for BT", Toast.LENGTH_SHORT).show();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanPairedDevices(int a) {
        Toast.makeText(FirstActivity.this, "connecting..." + a, Toast.LENGTH_LONG).show();
        // TODO HC-05 , 1234
        Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
        String names = "";
        BluetoothDevice device_HC05 = null;
        if (deviceSet.size() > 0)
            for (BluetoothDevice device : deviceSet) {
                names += device.getName() + "\n";
                if (device.getName().equals(MY_DEVICE_NAME)) {
                    device_HC05 = device;
                    //is_connected = true;
                    //btn_goto_remote.setEnabled(true);
                    //btn_turn_on_off.setText("disconnect from HC-05");
                    address = device.getAddress();
                    SharedPreferences.Editor editor = shared_prefs.edit();
                    editor.putString("address", address);
                    editor.apply();

                    connect_to_paired_device(device);
                    break;
                    //Intent it = new Intent(FirstActivity.this, MainActivity.class);
                    //it.putExtra(EXTRA_ADDRESS, address); // this will be received at MainActivity (class) Activity
                    //startActivity(it);
                }
            }
        Toast.makeText(this, "All pairs:\n" + names, Toast.LENGTH_SHORT).show();

        if (device_HC05 == null) {  // if HC-05 isn't paired then scan
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {  // checks for BT Permissions for API 21+
                int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
                if (permissionCheck != 0)
                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);  // Any number
            }
            bluetoothAdapter.startDiscovery();
            Toast.makeText(this, "startDiscovery", Toast.LENGTH_SHORT).show();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {  // finds new BT devices
            String action = intent.getAction();
            String names = "";
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    names += device.getName() + "\n";
                    if (device.getName().equals(MY_DEVICE_NAME)) {
                        unregisterReceiver(receiver);
                        bluetoothAdapter.cancelDiscovery();
                        device.createBond();
                        Toast.makeText(FirstActivity.this, "new bond: " + device.getName(), Toast.LENGTH_SHORT).show(); // TODO check & delete
                        //is_connected = true;
                        //btn_goto_remote.setEnabled(true);
                        //btn_turn_on_off.setText("disconnect from HC-05");
                        address = device.getAddress();
                        SharedPreferences.Editor editor = shared_prefs.edit();
                        editor.putString("address", address);
                        editor.apply();

                        connect_to_paired_device(device);
                    }
                }
            }

            Toast.makeText(FirstActivity.this, "new pair:\n" + names, Toast.LENGTH_LONG).show(); // TODO check & delete
        }
    };


    private void connect_to_paired_device(BluetoothDevice device) {
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {  // if the device is bonded
            try {
                Toast.makeText(FirstActivity.this, "1. Trying to connect to " + device.getName(), Toast.LENGTH_SHORT).show();
                btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            try {
                Toast.makeText(FirstActivity.this, "2. Trying to connect to " + device.getName(), Toast.LENGTH_SHORT).show();
                btSocket.connect();
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        is_connected = true;
                        btn_goto_remote.setEnabled(true);
                        btn_turn_on_off.setText(R.string.disconnect_from_HC_05);  // "disconnect from HC-05"
                    }
                }, 1500);
            } catch (IOException e) {
                try {
                    btSocket.close();
                    Toast.makeText(FirstActivity.this, "Cannot connect", Toast.LENGTH_SHORT).show();
                } catch (IOException e1) {
                    Toast.makeText(FirstActivity.this, "Socket not closed", Toast.LENGTH_SHORT).show();
                    e1.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        bluetoothAdapter.cancelDiscovery();
    }
}