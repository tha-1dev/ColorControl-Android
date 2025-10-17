package com.maassoft.colorcontrol.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.maassoft.colorcontrol.models.TvDevice;
import com.maassoft.colorcontrol.utils.PermissionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class BluetoothDiscovery {
    private static final String TAG = "BluetoothDiscovery";
    
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> discoveredDevices;
    private List<TvDevice> discoveredTvDevices;
    private DiscoveryListener discoveryListener;
    private boolean isDiscovering = false;
    
    // TV device patterns
    private static final String[] TV_DEVICE_PATTERNS = {
        "LG", "Samsung", "Sony", "Panasonic", "TCL", "Hisense", 
        "Sharp", "Toshiba", "Philips", "Vizio", "TV", "Television",
        "SmartTV", "WebOS", "BRAVIA", "QLED", "OLED", "VIERA"
    };
    
    public interface DiscoveryListener {
        void onDiscoveryStarted();
        void onDiscoveryFinished();
        void onDeviceFound(BluetoothDevice device);
        void onTvDeviceFound(TvDevice tvDevice);
        void onDiscoveryError(String error);
    }
    
    public BluetoothDiscovery(Context context) {
        this.context = context;
        this.discoveredDevices = new CopyOnWriteArrayList<>();
        this.discoveredTvDevices = new CopyOnWriteArrayList<>();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        registerDiscoveryReceiver();
    }
    
    private void registerDiscoveryReceiver() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            
            context.registerReceiver(discoveryReceiver, filter);
        } catch (Exception e) {
            Log.e(TAG, "Error registering discovery receiver: " + e.getMessage());
        }
    }
    
    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Bluetooth discovery started");
                isDiscovering = true;
                clearDiscoveryResults();
                
                if (discoveryListener != null) {
                    discoveryListener.onDiscoveryStarted();
                }
                
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    handleDiscoveredDevice(device);
                }
                
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Bluetooth discovery finished");
                isDiscovering = false;
                
                if (discoveryListener != null) {
                    discoveryListener.onDiscoveryFinished();
                }
            }
        }
    };
    
    private void handleDiscoveredDevice(BluetoothDevice device) {
        // Avoid duplicates
        if (isDeviceAlreadyDiscovered(device)) {
            return;
        }
        
        discoveredDevices.add(device);
        
        if (discoveryListener != null) {
            discoveryListener.onDeviceFound(device);
        }
        
        // Check if it's a TV device
        if (isTvDevice(device)) {
            TvDevice tvDevice = createTvDeviceFromBluetooth(device);
            discoveredTvDevices.add(tvDevice);
            
            if (discoveryListener != null) {
                discoveryListener.onTvDeviceFound(tvDevice);
            }
            
            Log.i(TAG, "TV device discovered: " + device.getName() + " [" + device.getAddress() + "]");
        }
    }
    
    private boolean isDeviceAlreadyDiscovered(BluetoothDevice device) {
        for (BluetoothDevice discovered : discoveredDevices) {
            if (discovered.getAddress().equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isTvDevice(BluetoothDevice device) {
        String deviceName = device.getName();
        if (deviceName == null) return false;
        
        String upperName = deviceName.toUpperCase();
        for (String pattern : TV_DEVICE_PATTERNS) {
            if (upperName.contains(pattern.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
    
    private TvDevice createTvDeviceFromBluetooth(BluetoothDevice device) {
        String deviceName = device.getName();
        String brand = detectTvBrand(deviceName);
        String macAddress = device.getAddress();
        
        TvDevice tvDevice = new TvDevice(deviceName, "", brand);
        tvDevice.setMacAddress(macAddress);
        tvDevice.setConnectionType("BLUETOOTH");
        
        // Set additional information if available
        try {
            // Bluetooth device class might give hints about device type
            int deviceClass = device.getBluetoothClass().getDeviceClass();
            tvDevice.setModel("Bluetooth Class: " + deviceClass);
        } catch (Exception e) {
            // Ignore if we can't get device class
        }
        
        return tvDevice;
    }
    
    private String detectTvBrand(String deviceName) {
        if (deviceName == null) return "Unknown";
        
        String upperName = deviceName.toUpperCase();
        
        if (upperName.contains("LG")) return "LG";
        if (upperName.contains("SAMSUNG")) return "Samsung";
        if (upperName.contains("SONY")) return "Sony";
        if (upperName.contains("BRAVIA")) return "Sony";
        if (upperName.contains("PANASONIC")) return "Panasonic";
        if (upperName.contains("VIERA")) return "Panasonic";
        if (upperName.contains("TCL")) return "TCL";
        if (upperName.contains("HISENSE")) return "Hisense";
        if (upperName.contains("SHARP")) return "Sharp";
        if (upperName.contains("TOSHIBA")) return "Toshiba";
        if (upperName.contains("PHILIPS")) return "Philips";
        if (upperName.contains("VIZIO")) return "Vizio";
        
        return "Generic TV";
    }
    
    public void startDiscovery() {
        if (!hasBluetoothPermissions()) {
            Log.e(TAG, "Bluetooth permissions not granted");
            if (discoveryListener != null) {
                discoveryListener.onDiscoveryError("Bluetooth permissions required");
            }
            return;
        }
        
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported");
            if (discoveryListener != null) {
                discoveryListener.onDiscoveryError("Bluetooth not supported");
            }
            return;
        }
        
        if (!bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not enabled");
            if (discoveryListener != null) {
                discoveryListener.onDiscoveryError("Bluetooth not enabled");
            }
            return;
        }
        
        if (isDiscovering) {
            Log.w(TAG, "Discovery already in progress");
            return;
        }
        
        // Cancel any ongoing discovery
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        
        // Start new discovery
        boolean started = bluetoothAdapter.startDiscovery();
        if (started) {
            Log.d(TAG, "Bluetooth discovery initiated");
        } else {
            Log.e(TAG, "Failed to start Bluetooth discovery");
            if (discoveryListener != null) {
                discoveryListener.onDiscoveryError("Failed to start discovery");
            }
        }
    }
    
    public void stopDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "Bluetooth discovery stopped");
        }
        isDiscovering = false;
    }
    
    public List<TvDevice> getPairedTvDevices() {
        List<TvDevice> pairedTvDevices = new ArrayList<>();
        
        if (bluetoothAdapter == null) return pairedTvDevices;
        
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (isTvDevice(device)) {
                TvDevice tvDevice = createTvDeviceFromBluetooth(device);
                pairedTvDevices.add(tvDevice);
            }
        }
        
        return pairedTvDevices;
    }
    
    public List<TvDevice> getDiscoveredTvDevices() {
        return new ArrayList<>(discoveredTvDevices);
    }
    
    public List<BluetoothDevice> getDiscoveredDevices() {
        return new ArrayList<>(discoveredDevices);
    }
    
    public void clearDiscoveryResults() {
        discoveredDevices.clear();
        discoveredTvDevices.clear();
    }
    
    private boolean hasBluetoothPermissions() {
        return PermissionManager.hasRequiredPermissions(context);
    }
    
    public void setDiscoveryListener(DiscoveryListener listener) {
        this.discoveryListener = listener;
    }
    
    public boolean isDiscovering() {
        return isDiscovering;
    }
    
    public void cleanup() {
        try {
            stopDiscovery();
            context.unregisterReceiver(discoveryReceiver);
        } catch (Exception e) {
            // Receiver might not be registered
        }
    }
    
    // Utility methods for device filtering
    public List<TvDevice> filterTvDevicesByBrand(String brand) {
        List<TvDevice> filtered = new ArrayList<>();
        for (TvDevice device : discoveredTvDevices) {
            if (device.getBrand().equalsIgnoreCase(brand)) {
                filtered.add(device);
            }
        }
        return filtered;
    }
    
    public List<TvDevice> filterTvDevicesByName(String namePattern) {
        List<TvDevice> filtered = new ArrayList<>();
        for (TvDevice device : discoveredTvDevices) {
            if (device.getName().toLowerCase().contains(namePattern.toLowerCase())) {
                filtered.add(device);
            }
        }
        return filtered;
    }
    
    // Method to get device signal strength (RSSI) if available
    public int getDeviceSignalStrength(BluetoothDevice device) {
        // Note: RSSI is not directly available through standard Android API
        // This would require custom implementation or manufacturer-specific APIs
        return -1; // Unknown signal strength
    }
    
    // Method to estimate connection quality
    public String getConnectionQuality(BluetoothDevice device) {
        // This is a simplified estimation
        // Real implementation would use RSSI and other factors
        return "GOOD"; // GOOD, FAIR, POOR
    }
}