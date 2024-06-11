package com.example.e_payment.Adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_payment.R;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    List<BluetoothDevice> mDevice;
    private OnDeviceSelectedListener listener;
    public interface OnDeviceSelectedListener{
        void onDeviceSelected(BluetoothDevice device);
    }

    public DeviceAdapter(List<BluetoothDevice> mDevice, OnDeviceSelectedListener listener) {
        this.mDevice = mDevice;
        this.listener = listener;
    }

    public DeviceAdapter(List<BluetoothDevice> mDevice) {
        this.mDevice = mDevice;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.devicelist_bluetooth, parent, false);
        return new DeviceViewHolder(itemView);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
            BluetoothDevice device = mDevice.get(position);
            holder.textViewDeviceName.setText(device.getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDeviceSelected(device);
                }
            });

    }

    @Override
    public int getItemCount() {
        return mDevice.size();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder{
        TextView textViewDeviceName;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDeviceName = itemView.findViewById(R.id.textViewDeviceName);
        }
    }

}
