package com.maassoft.colorcontrol.network;

import android.content.Context;
import android.util.Log;
import com.maassoft.colorcontrol.models.TvDevice;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TvDiscovery {
    private static final String TAG = "TvDiscovery";
    
    private Context context;
    private ExecutorService discoveryExecutor;
    private List<Future<?>> discoveryTasks;
    
    // Common TV ports
    private static final int[] TV_PORTS = {8080, 8001, 8002, 7676, 9999, 10000};
    
    // TV brand patterns for hostname detection
    private static final String[] LG_PATTERNS = {"lg", "webos"};
    private static final String[] SAMSUNG_PATTERNS = {"samsung", "smarttv"};
    private static final String[] SONY_PATTERNS = {"sony", "bravia"};
    
    public interface DiscoveryListener {
        void onDiscoveryStarted();
        void onDiscoveryFinished();
        void onDeviceFound(TvDevice device);
        void onDiscoveryError(String error);
    }
    
    public TvDiscovery(Context context) {
        this.context = context;
        this.discoveryExecutor = Executors.newFixedThreadPool(10);
        this.discoveryTasks = new ArrayList<>();
    }
    
    public List<TvDevice> discoverTvs() {
        Log.i(TAG, "Starting TV discovery...");
        List<TvDevice> discoveredDevices = new ArrayList<>();
        
        try {
            // Get local network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                // Skip loopback and inactive interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;
                
                // Get interface addresses
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                
                for (InetAddress address : Collections.list(addresses)) {
                    // Only consider IPv4 addresses
                    if (address.getHostAddress().contains(":")) continue;
                    
                    String subnet = getSubnet(address.getHostAddress());
                    if (subnet != null) {
                        Log.d(TAG, "Scanning subnet: " + subnet);
                        scanSubnet(subnet, discoveredDevices);
                    }
                }
            }
            
        } catch (SocketException e) {
            Log.e(TAG, "Error during network discovery: " + e.getMessage());
        }
        
        Log.i(TAG, "TV discovery completed. Found " + discoveredDevices.size() + " devices");
        return discoveredDevices;
    }
    
    public void discoverTvsAsync(DiscoveryListener listener) {
        if (listener != null) {
            listener.onDiscoveryStarted();
        }
        
        discoveryExecutor.execute(() -> {
            try {
                List<TvDevice> devices = discoverTvs();
                
                if (listener != null) {
                    for (TvDevice device : devices) {
                        listener.onDeviceFound(device);
                    }
                    listener.onDiscoveryFinished();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Async discovery error: " + e.getMessage());
                if (listener != null) {
                    listener.onDiscoveryError(e.getMessage());
                }
            }
        });
    }
    
    private void scanSubnet(String subnet, List<TvDevice> discoveredDevices) {
        List<Future<?>> tasks = new ArrayList<>();
        
        // Scan IP range 1-254
        for (int i = 1; i <= 254; i++) {
            final String ip = subnet + i;
            Future<?> task = discoveryExecutor.submit(() -> checkIpForTv(ip, discoveredDevices));
            tasks.add(task);
        }
        
        // Wait for all tasks to complete
        for (Future<?> task : tasks) {
            try {
                task.get();
            } catch (Exception e) {
                Log.w(TAG, "Task execution error: " + e.getMessage());
            }
        }
    }
    
    private void checkIpForTv(String ip, List<TvDevice> discoveredDevices) {
        Log.d(TAG, "Checking IP: " + ip);
        
        // First, try to get hostname
        String hostname = getHostname(ip);
        String brand = detectTvBrand(hostname);
        
        // If hostname suggests it's a TV, or if we don't have hostname, check ports
        if (brand != null || hostname == null) {
            for (int port : TV_PORTS) {
                if (isPortOpen(ip, port)) {
                    TvDevice device = createTvDevice(ip, port, brand, hostname);
                    synchronized (discoveredDevices) {
                        discoveredDevices.add(device);
                    }
                    Log.i(TAG, "Found TV: " + device.getName() + " at " + ip + ":" + port);
                    break; // Found TV, no need to check other ports
                }
            }
        }
    }
    
    private String getHostname(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.getHostName();
        } catch (Exception e) {
            return null;
        }
    }
    
    private String detectTvBrand(String hostname) {
        if (hostname == null) return null;
        
        String lowerHostname = hostname.toLowerCase();
        
        for (String pattern : LG_PATTERNS) {
            if (lowerHostname.contains(pattern)) return "LG";
        }
        
        for (String pattern : SAMSUNG_PATTERNS) {
            if (lowerHostname.contains(pattern)) return "Samsung";
        }
        
        for (String pattern : SONY_PATTERNS) {
            if (lowerHostname.contains(pattern)) return "Sony";
        }
        
        return null;
    }
    
    private boolean isPortOpen(String ip, int port) {
        try {
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress(ip, port), 1000);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private TvDevice createTvDevice(String ip, int port, String brand, String hostname) {
        String deviceName;
        
        if (brand != null) {
            deviceName = brand + " TV";
        } else if (hostname != null) {
            deviceName = hostname;
        } else {
            deviceName = "TV at " + ip;
        }
        
        if (brand == null) {
            brand = detectBrandFromPort(port);
        }
        
        TvDevice device = new TvDevice(deviceName, ip, brand);
        device.setPort(port);
        
        // Set connection type based on port
        if (port == 8080) {
            device.setConnectionType("LG WebOS");
        } else if (port == 8001) {
            device.setConnectionType("Samsung Tizen");
        } else if (port == 10000) {
            device.setConnectionType("Sony Bravia");
        } else {
            device.setConnectionType("Network");
        }
        
        return device;
    }
    
    private String detectBrandFromPort(int port) {
        switch (port) {
            case 8080: return "LG";
            case 8001: return "Samsung";
            case 10000: return "Sony";
            default: return "Generic";
        }
    }
    
    private String getSubnet(String ipAddress) {
        if (ipAddress == null) return null;
        
        String[] parts = ipAddress.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + "." + parts[2] + ".";
        }
        return null;
    }
    
    public List<TvDevice> discoverTvsInRange(String startIp, String endIp) {
        Log.i(TAG, "Scanning IP range: " + startIp + " - " + endIp);
        List<TvDevice> discoveredDevices = new ArrayList<>();
        
        try {
            int start = Integer.parseInt(startIp.split("\\.")[3]);
            int end = Integer.parseInt(endIp.split("\\.")[3]);
            String base = getSubnet(startIp);
            
            for (int i = start; i <= end; i++) {
                String ip = base + i;
                checkIpForTv(ip, discoveredDevices);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error scanning IP range: " + e.getMessage());
        }
        
        return discoveredDevices;
    }
    
    public void stopDiscovery() {
        for (Future<?> task : discoveryTasks) {
            if (!task.isDone()) {
                task.cancel(true);
            }
        }
        discoveryTasks.clear();
    }
    
    public void cleanup() {
        stopDiscovery();
        if (discoveryExecutor != null && !discoveryExecutor.isShutdown()) {
            discoveryExecutor.shutdown();
        }
    }
    
    // Utility methods for specific TV brands
    public List<TvDevice> discoverLgTvs() {
        return discoverTvsByPort(8080, "LG");
    }
    
    public List<TvDevice> discoverSamsungTvs() {
        return discoverTvsByPort(8001, "Samsung");
    }
    
    public List<TvDevice> discoverSonyTvs() {
        return discoverTvsByPort(10000, "Sony");
    }
    
    private List<TvDevice> discoverTvsByPort(int port, String brand) {
        List<TvDevice> allDevices = discoverTvs();
        List<TvDevice> filteredDevices = new ArrayList<>();
        
        for (TvDevice device : allDevices) {
            if (device.getPort() == port || device.getBrand().equalsIgnoreCase(brand)) {
                filteredDevices.add(device);
            }
        }
        
        return filteredDevices;
    }
}