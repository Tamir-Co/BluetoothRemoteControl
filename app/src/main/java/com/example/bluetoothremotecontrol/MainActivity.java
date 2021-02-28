package com.example.bluetoothremotecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private SeekBar seekBar_speed, seekBar_direction;
    private static int speed = 0, direction = 0;
    private int left_forward_1, right_forward_3;
    private int left_backward_2, right_backward_4;
    private ProgressDialog progress;

    String address = null;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent it = getIntent();
        address = it.getStringExtra(FirstActivity.EXTRA_ADDRESS);

        seekBar_speed = (SeekBar) findViewById(R.id.seekBar_speed);
        seekBar_direction = (SeekBar) findViewById(R.id.seekBar_direction);


        //direction = seekBar_direction.getProgress();
        //speed = seekBar_speed.getProgress();
        //Toast.makeText(MainActivity.this, "speed: "+ (-speed), Toast.LENGTH_LONG).show();

        //Toast.makeText(MainActivity.this, "speed: " + speed + "\ndirection: " + direction, Toast.LENGTH_LONG).show();

        seekBar_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                speed = seekBar_speed.getProgress();
                send_info();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                speed = seekBar_speed.getProgress();
                send_info();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //seekBar_speed.setProgress(512);
            }
        });

        seekBar_direction.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                direction = seekBar_direction.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                direction = seekBar_direction.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //seekBar_direction.setProgress(512);
            }
        });

    }

    private void send_info() {
        speed = speed - 1023;
        if (speed >= 0) {    // moving forward
            left_forward_1 = (speed / 1023) * (direction);
            right_forward_3 = (speed / 1023) * (1023 - direction);
            left_backward_2 = 0;
            right_backward_4 = 0;
        }
        else {    // moving backward
            left_backward_2 = -(speed / 1023) * (direction);
            right_backward_4 = -(speed / 1023) * (1023 - direction);
            left_forward_1 = 0;
            right_forward_3 = 0;
        }
        String msg = "";
        msg += left_forward_1 + ' ' + left_backward_2 + ' ' + right_forward_3 + ' ' + right_backward_4 + ' ';

        try {
            btSocket.getOutputStream().write(msg.getBytes());
        } catch (IOException e) {
            //e.printStackTrace();
            Toast.makeText(this, "can't send info.\ncheck your BT connection.", Toast.LENGTH_LONG).show();
        }
    }


    // =============================================================================================================================================
/*

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // get the mobile bluetooth device
                    BluetoothDevice dispositivo = bluetoothAdapter.getRemoteDevice(address); // connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID); // create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect(); // start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false; // if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) // after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                //msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                Toast.makeText(MainActivity.this, "Connection Failed. Is it a SPP Bluetooth? Try again.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                //msg("Connected.");
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }


*/


}