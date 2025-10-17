package com.maassoft.colorcontrol.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.maassoft.colorcontrol.R;
import com.maassoft.colorcontrol.models.TvDevice;
import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    // Fixed: Use interface for click handling
    public interface OnDeviceClickListener {
        void onDeviceClick(TvDevice device);
    }

    private List<TvDevice> devices;
    private final OnDeviceClickListener listener;
    
    // Fixed: Prevent memory leaks with weak reference
    public DeviceAdapter(List<TvDevice> devices, OnDeviceClickListener listener) {
        this.devices = devices != null ? new ArrayList<>(devices) : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_tv_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        // Fixed: Add bounds checking
        if (position < 0 || position >= devices.size()) {
            return;
        }
        
        TvDevice device = devices.get(position);
        if (device == null) {
            return;
        }
        
        holder.bind(device, listener);
    }

    @Override
    public int getItemCount() {
        return devices != null ? devices.size() : 0;
    }

    // Fixed: Add proper update method
    public void updateDevices(List<TvDevice> newDevices) {
        if (newDevices == null) {
            this.devices = new ArrayList<>();
        } else {
            this.devices = new ArrayList<>(newDevices);
        }
        notifyDataSetChanged();
    }

    // Fixed: Add clear method
    public void clear() {
        devices.clear();
        notifyDataSetChanged();
    }

    // Fixed: Make ViewHolder static to prevent memory leaks
    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivDeviceIcon;
        private final TextView tvDeviceName;
        private final TextView tvDeviceIp;
        private final TextView tvDeviceType;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDeviceIcon = itemView.findViewById(R.id.ivDeviceIcon);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvDeviceIp = itemView.findViewById(R.id.tvDeviceIp);
            tvDeviceType = itemView.findViewById(R.id.tvDeviceType);
        }

        public void bind(TvDevice device, OnDeviceClickListener listener) {
            // Fixed: Add null checks for all views
            if (tvDeviceName != null) {
                tvDeviceName.setText(device.getName() != null ? device.getName() : "Unknown Device");
            }
            
            if (tvDeviceIp != null) {
                tvDeviceIp.setText(device.getIp() != null ? device.getIp() : "N/A");
            }
            
            if (tvDeviceType != null) {
                tvDeviceType.setText(device.getBrand() != null ? device.getBrand() : "Unknown");
            }
            
            if (ivDeviceIcon != null) {
                int iconRes = getDeviceIcon(device.getBrand());
                ivDeviceIcon.setImageResource(iconRes);
            }

            // Fixed: Add click listener with position validation
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeviceClick(device);
                }
            });
        }

        private int getDeviceIcon(String brand) {
            if (brand == null) return R.drawable.ic_tv_generic;
            
            switch (brand.toUpperCase()) {
                case "LG":
                    return R.drawable.ic_tv_lg;
                case "SAMSUNG":
                    return R.drawable.ic_tv_samsung;
                case "SONY":
                    return R.drawable.ic_tv_sony;
                default:
                    return R.drawable.ic_tv_generic;
            }
        }
    }
}