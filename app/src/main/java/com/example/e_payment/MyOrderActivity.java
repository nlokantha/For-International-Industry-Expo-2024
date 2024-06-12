package com.example.e_payment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.e_payment.Adapter.DeviceAdapter;
import com.example.e_payment.databinding.ActivityMyOrderBinding;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MyOrderActivity extends BaseActivity implements DeviceAdapter.OnDeviceSelectedListener {
    ActivityMyOrderBinding binding;
    List<BluetoothDevice> deviceList = new ArrayList<>();
    DeviceAdapter adapter = new DeviceAdapter(deviceList, this);
    private static final int REQUEST_BLUETOOTH_PERMISSION = 100;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice mDevice;
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mSocket;
    private OutputStream mOutputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        checkPermissionsAndStart();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void checkPermissionsAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_ADVERTISE},
                        REQUEST_BLUETOOTH_PERMISSION);
            } else {
                getPairedDevices();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN},
                        REQUEST_BLUETOOTH_PERMISSION);
            } else {
                getPairedDevices();
            }
        } else {
            getPairedDevices();
        }
    }

    @SuppressLint("MissingPermission")
    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        deviceList.addAll(pairedDevices);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                getPairedDevices();
            } else {
                Toast.makeText(MyOrderActivity.this, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void ConnectWithDevice(BluetoothDevice device) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());
                    mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
                    bluetoothAdapter.cancelDiscovery();
                    mSocket.connect();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mSocket.isConnected()) {
                                Toast.makeText(MyOrderActivity.this, "Connected !!!!", Toast.LENGTH_SHORT).show();
                                try {
                                    mOutputStream = mSocket.getOutputStream();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(MyOrderActivity.this, "Not Connected !!!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyOrderActivity.this, "Connection Failed !!!!", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDeviceSelected(BluetoothDevice device) {
        this.mDevice = device;
        Intent intent = new Intent(MyOrderActivity.this, CartActivity.class);
        intent.putExtra("device",device.getAddress());
        startActivity(intent);
//        ConnectWithDevice(device);
    }
}
