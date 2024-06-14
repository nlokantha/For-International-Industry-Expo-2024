package com.example.e_payment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_payment.Adapter.DeviceAdapter;
import com.example.e_payment.databinding.ActivityCartBinding;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CartActivity extends BaseActivity {
    ActivityCartBinding binding;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice mDevice;
    private static final String TAG = "demo";

    String bluetooth,shopName;
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mSocket;
    private OutputStream mOutputStream;

    private static final int REQUEST_BLUETOOTH_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
        }

        if (getIntent() != null && getIntent().getStringExtra("device") != null){
            bluetooth = getIntent().getStringExtra("device");
//            Toast.makeText(this, bluetooth, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate: "+bluetooth);
//            ConnectWithDevice(getIntent().getStringExtra("device"));
        }
        binding.buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
            }
        });

        binding.buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String total = binding.editTextCoupon.getText().toString();
                if (!total.isEmpty()){
                    SendToBoard(total);
                }
                binding.textViewShopName.setText("N/A");
                binding.textViewTotal.setText("$0");
                binding.textViewSubtotal.setText("$0");
                binding.textView10.setText("$0");
                binding.textView12.setText("$0");
                try {
                    if (mSocket != null) {
                        mSocket.close();
                    }
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }



            }
        });
        binding.imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String coupon = binding.editTextCoupon.getText().toString();
                binding.textViewSubtotal.setText("$" + coupon);
                binding.textViewTotal.setText("$" + coupon);
            }
        });
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
            String inputString = result.getContents().toString();
            String macAddress = extractMACAddress(inputString);
        Log.d(TAG, "fgfgfg:"+macAddress);
            ConnectWithDevice(macAddress);
            binding.textViewShopName.setText(inputString);

//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setCancelable(false);
//            LayoutInflater inflater = this.getLayoutInflater();
//            View alertDialog = inflater.inflate(R.layout.custom_complete_dialog, null);
//            builder.setView(alertDialog);
//
//            AlertDialog alert = builder.create();
//            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            alert.show();
//
//            AppCompatButton buttonClose = alertDialog.findViewById(R.id.buttonClose);
//            TextView textView = alertDialog.findViewById(R.id.textViewResult);
//            textView.setText(binding.textViewTotal.getText().toString());
//            buttonClose.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    alert.dismiss();
//                }
//            });
    });

//    public void ConnectWithDevice(String device) {
//        mDevice = bluetoothAdapter.getRemoteDevice(device);
//        try {
//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//            mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
//            mSocket.connect();
//            if (mSocket.isConnected()){
//                Toast.makeText(this, "Connected !!!!", Toast.LENGTH_SHORT).show();
//                mOutputStream = mSocket.getOutputStream();
//            }else {
//                Toast.makeText(this, "Not Connect !!!!", Toast.LENGTH_SHORT).show();
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
    @SuppressLint("MissingPermission")
    public void ConnectWithDevice(String device) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDevice = bluetoothAdapter.getRemoteDevice(device);
                    mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
                    bluetoothAdapter.cancelDiscovery();
                    mSocket.connect();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mSocket.isConnected()) {
                                Toast.makeText(CartActivity.this, "Connected !!!!", Toast.LENGTH_SHORT).show();
                                try {
                                    mOutputStream = mSocket.getOutputStream();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(CartActivity.this, "Not Connected !!!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CartActivity.this, "Connection Failed !!!!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    try {
                        if (mSocket != null) {
                            mSocket.close();
                        }
                    } catch (IOException closeException) {
                        closeException.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void SendToBoard(String msg){
        try {
            if (mOutputStream != null) {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
//                Toast.makeText(this, "Data sent over Bluetooth", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth output stream not available", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String extractMACAddress(String input) {
        Pattern pattern = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }



}