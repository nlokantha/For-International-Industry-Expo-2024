package com.example.e_payment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_payment.databinding.ActivityCartBinding;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class CartActivity extends BaseActivity {
    ActivityCartBinding binding;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice mDevice;
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mSocket;
    private OutputStream mOutputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
        }

        binding.buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
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
                binding.textViewSubTotal.setText("$" + coupon);
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
        if (result.getContents() != null && result.getContents().equals("C3 Shopping Store")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            LayoutInflater inflater = this.getLayoutInflater();
            View alertDialog = inflater.inflate(R.layout.custom_complete_dialog, null);
            builder.setView(alertDialog);

            AlertDialog alert = builder.create();
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alert.show();

            AppCompatButton buttonClose = alertDialog.findViewById(R.id.buttonClose);
            TextView textView = alertDialog.findViewById(R.id.textViewResult);
            textView.setText(binding.textViewTotal.getText().toString());
            buttonClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    alert.dismiss();
                }
            });
        } else {
            Toast.makeText(this, "Error!!!", Toast.LENGTH_SHORT).show();
        }
    });

    public void ConnectWithDevice(BluetoothDevice device) {
        mDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();
            if (mSocket.isConnected()){
                Toast.makeText(this, "Connected !!!!", Toast.LENGTH_SHORT).show();
                mOutputStream = mSocket.getOutputStream();
            }else {
                Toast.makeText(this, "Not Connect !!!!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void SendToBoard(String msg){
        try {
            if (mOutputStream != null) {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
                Toast.makeText(this, "Data sent over Bluetooth", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth output stream not available", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}