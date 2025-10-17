package com.maassoft.colorcontrol.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.maassoft.colorcontrol.models.TvDevice;
import com.maassoft.colorcontrol.utils.PermissionManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdvancedBluetoothConnection {
    private static final String TAG = "AdvancedBluetoothConnection";
    
    // Bluetooth UUIDs for different TV brands
    private static final UUID UUID_LG = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID UUID_SAMSUNG = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID UUID_SONY = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID UUID_PANASONIC = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice connectedDevice;
    private ConnectedThread connectedThread;
    private BluetoothConnectionListener connectionListener;
    
    private ExecutorService bluetoothExecutor;
    private boolean isConnecting = false;
    private boolean isConnected = false;
    
    // TV brand patterns for auto-detection
    private static final String[] LG_PATTERNS = {"LG", "WebOS", "OLED"};
    private static final String[] SAMSUNG_PATTERNS = {"Samsung", "SmartTV", "QLED"};
    private static final String[] SONY_PATTERNS = {"Sony", "BRAVIA", "XBR"};
    private static final String[] PANASONIC_PATTERNS = {"Panasonic", "VIERA"};
    
    public interface BluetoothConnectionListener {
        void onBluetoothConnected(BluetoothDevice device);
        void onBluetoothDisconnected();
        void onBluetoothConnectionFailed(String error);
        void onBluetoothDataReceived(byte[] data);
        void onBluetoothDeviceFound(BluetoothDevice device);
    }
    
    public AdvancedBluetoothConnection(Context context) {
        this.context = context;
        this.bluetoothExecutor = Executors.newSingleThreadExecutor();
        initializeBluetooth();
    }
    
    private void initializeBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported on this device");
            return;
        }
        
        if (!bluetoothAdapter.isEnabled()) {
            Log.w(TAG, "Bluetooth is not enabled");
        }
        
        registerBluetoothReceiver();
    }
    
    private void registerBluetoothReceiver() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            
            context.registerReceiver(bluetoothReceiver, filter);
        } catch (Exception e) {
            Log.e(TAG, "Error registering Bluetooth receiver: " + e.getMessage());
        }
    }
    
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && isTvDevice(device)) {
                    Log.d(TAG, "Found potential TV device: " + device.getName() + " - " + device.getAddress());
                    
                    if (connectionListener != null) {
                        connectionListener.onBluetoothDeviceFound(device);
                    }
                }
                
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Bluetooth discovery finished");
                
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.equals(connectedDevice)) {
                    Log.i(TAG, "Bluetooth connection established: " + device.getName());
                    isConnected = true;
                    isConnecting = false;
                    
                    if (connectionListener != null) {
                        connectionListener.onBluetoothConnected(device);
                    }
                }
                
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.equals(connectedDevice)) {
                    Log.w(TAG, "Bluetooth connection lost: " + device.getName());
                    isConnected = false;
                    cleanupConnection();
                    
                    if (connectionListener != null) {
                        connectionListener.onBluetoothDisconnected();
                    }
                }
            }
        }
    };
    
    public boolean connectToTvBluetooth(TvDevice tvDevice) {
        if (!hasBluetoothPermissions()) {
            Log.e(TAG, "Bluetooth permissions not granted");
            return false;
        }
        
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not available or disabled");
            return false;
        }
        
        if (isConnecting || isConnected) {
            Log.w(TAG, "Already connecting or connected");
            return false;
        }
        
        isConnecting = true;
        
        bluetoothExecutor.execute(() -> {
            try {
                // First, try to find the device in paired devices
                BluetoothDevice device = findBluetoothDevice(tvDevice);
                
                if (device == null) {
                    Log.e(TAG, "Bluetooth device not found: " + tvDevice.getName());
                    notifyConnectionFailed("Device not found");
                    return;
                }
                
                // Connect to the device
                boolean connected = connectToDevice(device, tvDevice.getBrand());
                
                if (connected) {
                    connectedDevice = device;
                    Log.i(TAG, "Successfully connected to TV via Bluetooth: " + device.getName());
                } else {
                    notifyConnectionFailed("Connection failed");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error connecting to Bluetooth device: " + e.getMessage());
                notifyConnectionFailed("Connection error: " + e.getMessage());
            } finally {
                isConnecting = false;
            }
        });
        
        return true;
    }
    
    private BluetoothDevice findBluetoothDevice(TvDevice tvDevice) {
        // Check paired devices first
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (matchesTvDevice(device, tvDevice)) {
                Log.d(TAG, "Found TV in paired devices: " + device.getName());
                return device;
            }
        }
        
        // If not found in paired devices, start discovery
        Log.d(TAG, "Device not paired, starting discovery...");
        startBluetoothDiscovery();
        
        // In a real implementation, you would wait for discovery results
        // For now, return null and let discovery callback handle it
        return null;
    }
    
    private boolean matchesTvDevice(BluetoothDevice device, TvDevice tvDevice) {
        String deviceName = device.getName();
        if (deviceName == null) return false;
        
        // Match by name pattern
        if (tvDevice.getName() != null && deviceName.contains(tvDevice.getName())) {
            return true;
        }
        
        // Match by brand
        String brand = tvDevice.getBrand();
        if (brand != null) {
            switch (brand.toUpperCase()) {
                case "LG":
                    return containsAny(deviceName, LG_PATTERNS);
                case "SAMSUNG":
                    return containsAny(deviceName, SAMSUNG_PATTERNS);
                case "SONY":
                    return containsAny(deviceName, SONY_PATTERNS);
                case "PANASONIC":
                    return containsAny(deviceName, PANASONIC_PATTERNS);
            }
        }
        
        return false;
    }
    
    private boolean containsAny(String text, String[] patterns) {
        if (text == null) return false;
        for (String pattern : patterns) {
            if (text.toUpperCase().contains(pattern.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean connectToDevice(BluetoothDevice device, String brand) {
        try {
            UUID uuid = getUuidForBrand(brand);
            
            // Method 1: Standard connection
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                
                // Start data communication thread
                startCommunicationThread();
                return true;
                
            } catch (IOException e) {
                Log.w(TAG, "Standard connection failed, trying fallback: " + e.getMessage());
                
                // Method 2: Fallback connection using reflection
                return tryFallbackConnection(device);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "All connection methods failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean tryFallbackConnection(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createRfcommSocket", int.class);
            bluetoothSocket = (BluetoothSocket) method.invoke(device, 1);
            bluetoothSocket.connect();
            
            startCommunicationThread();
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Fallback connection failed: " + e.getMessage());
            return false;
        }
    }
    
    private UUID getUuidForBrand(String brand) {
        if (brand == null) return UUID_LG;
        
        switch (brand.toUpperCase()) {
            case "SAMSUNG":
                return UUID_SAMSUNG;
            case "SONY":
                return UUID_SONY;
            case "PANASONIC":
                return UUID_PANASONIC;
            case "LG":
            default:
                return UUID_LG;
        }
    }
    
    private void startCommunicationThread() {
        if (bluetoothSocket == null) return;
        
        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();
    }
    
    public boolean sendCommand(String command) {
        if (!isConnected || connectedThread == null) {
            Log.w(TAG, "Not connected, cannot send command");
            return false;
        }
        
        try {
            // Convert command to bytes (protocol specific)
            byte[] commandBytes = encodeCommand(command);
            connectedThread.write(commandBytes);
            Log.d(TAG, "Bluetooth command sent: " + command);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error sending Bluetooth command: " + e.getMessage());
            return false;
        }
    }
    
    private byte[] encodeCommand(String command) {
        // TV brand specific command encoding
        // This is a simplified implementation
        // Real implementation would use proper protocols for each brand
        
        String encodedCommand;
        switch (command.toUpperCase()) {
            case "POWER":
                encodedCommand = "PWR1\n";
                break;
            case "VOLUME_UP":
                encodedCommand = "VOLU\n";
                break;
            case "VOLUME_DOWN":
                encodedCommand = "VOLD\n";
                break;
            case "MUTE":
                encodedCommand = "MUTE1\n";
                break;
            case "CHANNEL_UP":
                encodedCommand = "CHNU\n";
                break;
            case "CHANNEL_DOWN":
                encodedCommand = "CHND\n";
                break;
            case "HOME":
                encodedCommand = "HOME1\n";
                break;
            case "BACK":
                encodedCommand = "BACK1\n";
                break;
            default:
                encodedCommand = command + "\n";
        }
        
        return encodedCommand.getBytes();
    }
    
    public void startBluetoothDiscovery() {
        if (!hasBluetoothPermissions()) {
            Log.e(TAG, "Bluetooth permissions not granted for discovery");
            return;
        }
        
        if (bluetoothAdapter == null) return;
        
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        
        boolean started = bluetoothAdapter.startDiscovery();
        if (started) {
            Log.d(TAG, "Bluetooth discovery started");
        } else {
            Log.e(TAG, "Failed to start Bluetooth discovery");
        }
    }
    
    public void stopBluetoothDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "Bluetooth discovery stopped");
        }
    }
    
    public void disconnect() {
        bluetoothExecutor.execute(() -> {
            cleanupConnection();
            isConnected = false;
            isConnecting = false;
            
            if (connectionListener != null) {
                connectionListener.onBluetoothDisconnected();
            }
        });
    }
    
    private void cleanupConnection() {
        try {
            if (connectedThread != null) {
                connectedThread.cancel();
                connectedThread = null;
            }
            
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
            
            connectedDevice = null;
            
        } catch (IOException e) {
            Log.e(TAG, "Error cleaning up Bluetooth connection: " + e.getMessage());
        }
    }
    
    private boolean isTvDevice(BluetoothDevice device) {
        String deviceName = device.getName();
        if (deviceName == null) return false;
        
        // Check if device name matches TV patterns
        return containsAny(deviceName, LG_PATTERNS) ||
               containsAny(deviceName, SAMSUNG_PATTERNS) ||
               containsAny(deviceName, SONY_PATTERNS) ||
               containsAny(deviceName, PANASONIC_PATTERNS) ||
               deviceName.toUpperCase().contains("TV") ||
               deviceName.toUpperCase().contains("TELEVISION");
    }
    
    private boolean hasBluetoothPermissions() {
        return PermissionManager.hasRequiredPermissions(context);
    }
    
    private void notifyConnectionFailed(String error) {
        if (connectionListener != null) {
            connectionListener.onBluetoothConnectionFailed(error);
        }
    }
    
    public void setConnectionListener(BluetoothConnectionListener listener) {
        this.connectionListener = listener;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public boolean isConnecting() {
        return isConnecting;
    }
    
    public BluetoothDevice getConnectedDevice() {
        return connectedDevice;
    }
    
    public void cleanup() {
        try {
            disconnect();
            stopBluetoothDiscovery();
            
            if (bluetoothExecutor != null && !bluetoothExecutor.isShutdown()) {
                bluetoothExecutor.shutdown();
            }
            
            try {
                context.unregisterReceiver(bluetoothReceiver);
            } catch (Exception e) {
                // Receiver might not be registered
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error during Bluetooth cleanup: " + e.getMessage());
        }
    }
    
    // Connected thread for handling data communication
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private volatile boolean running = true;
        
        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error getting Bluetooth streams: " + e.getMessage());
            }
            
            inputStream = tmpIn;
            outputStream = tmpOut;
        }
        
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            
            while (running) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        byte[] receivedData = new byte[bytes];
                        System.arraycopy(buffer, 0, receivedData, 0, bytes);
                        
                        // Notify about received data
                        if (connectionListener != null) {
                            connectionListener.onBluetoothDataReceived(receivedData);
                        }
                        
                        Log.d(TAG, "Received Bluetooth data: " + new String(receivedData));
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Bluetooth connection lost: " + e.getMessage());
                    break;
                }
            }
        }
        
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                outputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error writing to Bluetooth output stream: " + e.getMessage());
            }
        }
        
        public void cancel() {
            running = false;
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Bluetooth connection: " + e.getMessage());
            }
        }
    }
}