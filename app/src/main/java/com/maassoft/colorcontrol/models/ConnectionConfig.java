package com.maassoft.colorcontrol.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class ConnectionConfig implements Parcelable {
    private String defaultConnectionType;
    private int connectionTimeout;
    private int retryCount;
    private boolean autoReconnect;
    private boolean wakeOnLanEnabled;
    private int wakeOnLanPort;
    private boolean useEncryption;
    private String encryptionKey;
    private boolean logConnections;
    private int maxConnectionAttempts;
    private int keepAliveInterval;
    private boolean validateCertificates;
    
    public ConnectionConfig() {
        // Default configuration
        this.defaultConnectionType = "AUTO";
        this.connectionTimeout = 10000; // 10 seconds
        this.retryCount = 3;
        this.autoReconnect = true;
        this.wakeOnLanEnabled = true;
        this.wakeOnLanPort = 9;
        this.useEncryption = false;
        this.encryptionKey = "";
        this.logConnections = true;
        this.maxConnectionAttempts = 5;
        this.keepAliveInterval = 30000; // 30 seconds
        this.validateCertificates = true;
    }
    
    protected ConnectionConfig(Parcel in) {
        defaultConnectionType = in.readString();
        connectionTimeout = in.readInt();
        retryCount = in.readInt();
        autoReconnect = in.readByte() != 0;
        wakeOnLanEnabled = in.readByte() != 0;
        wakeOnLanPort = in.readInt();
        useEncryption = in.readByte() != 0;
        encryptionKey = in.readString();
        logConnections = in.readByte() != 0;
        maxConnectionAttempts = in.readInt();
        keepAliveInterval = in.readInt();
        validateCertificates = in.readByte() != 0;
    }
    
    public static final Creator<ConnectionConfig> CREATOR = new Creator<ConnectionConfig>() {
        @Override
        public ConnectionConfig createFromParcel(Parcel in) {
            return new ConnectionConfig(in);
        }
        
        @Override
        public ConnectionConfig[] newArray(int size) {
            return new ConnectionConfig[size];
        }
    };
    
    // Getters and Setters
    public String getDefaultConnectionType() {
        return defaultConnectionType;
    }
    
    public void setDefaultConnectionType(String defaultConnectionType) {
        this.defaultConnectionType = defaultConnectionType;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = Math.max(1000, connectionTimeout); // Minimum 1 second
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(int retryCount) {
        this.retryCount = Math.max(0, retryCount);
    }
    
    public boolean isAutoReconnect() {
        return autoReconnect;
    }
    
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }
    
    public boolean isWakeOnLanEnabled() {
        return wakeOnLanEnabled;
    }
    
    public void setWakeOnLanEnabled(boolean wakeOnLanEnabled) {
        this.wakeOnLanEnabled = wakeOnLanEnabled;
    }
    
    public int getWakeOnLanPort() {
        return wakeOnLanPort;
    }
    
    public void setWakeOnLanPort(int wakeOnLanPort) {
        this.wakeOnLanPort = Math.max(1, Math.min(65535, wakeOnLanPort));
    }
    
    public boolean isUseEncryption() {
        return useEncryption;
    }
    
    public void setUseEncryption(boolean useEncryption) {
        this.useEncryption = useEncryption;
    }
    
    public String getEncryptionKey() {
        return encryptionKey;
    }
    
    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
    
    public boolean isLogConnections() {
        return logConnections;
    }
    
    public void setLogConnections(boolean logConnections) {
        this.logConnections = logConnections;
    }
    
    public int getMaxConnectionAttempts() {
        return maxConnectionAttempts;
    }
    
    public void setMaxConnectionAttempts(int maxConnectionAttempts) {
        this.maxConnectionAttempts = Math.max(1, maxConnectionAttempts);
    }
    
    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }
    
    public void setKeepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = Math.max(5000, keepAliveInterval); // Minimum 5 seconds
    }
    
    public boolean isValidateCertificates() {
        return validateCertificates;
    }
    
    public void setValidateCertificates(boolean validateCertificates) {
        this.validateCertificates = validateCertificates;
    }
    
    // Utility methods
    public boolean isValid() {
        return connectionTimeout > 0 && 
               retryCount >= 0 && 
               maxConnectionAttempts > 0 && 
               keepAliveInterval > 0;
    }
    
    public long getTotalTimeout() {
        return (long) connectionTimeout * (retryCount + 1);
    }
    
    public String getConnectionTypeDisplayName() {
        if (defaultConnectionType == null) return "Auto";
        
        switch (defaultConnectionType.toUpperCase()) {
            case "WIFI": return "WiFi";
            case "BLUETOOTH": return "Bluetooth";
            case "USB": return "USB";
            case "AUTO": return "Auto";
            default: return defaultConnectionType;
        }
    }
    
    // Factory methods for different scenarios
    public static ConnectionConfig createFastConfig() {
        ConnectionConfig config = new ConnectionConfig();
        config.setConnectionTimeout(5000);
        config.setRetryCount(2);
        config.setKeepAliveInterval(15000);
        return config;
    }
    
    public static ConnectionConfig createReliableConfig() {
        ConnectionConfig config = new ConnectionConfig();
        config.setConnectionTimeout(15000);
        config.setRetryCount(5);
        config.setMaxConnectionAttempts(10);
        config.setKeepAliveInterval(60000);
        return config;
    }
    
    public static ConnectionConfig createBatterySaverConfig() {
        ConnectionConfig config = new ConnectionConfig();
        config.setAutoReconnect(false);
        config.setWakeOnLanEnabled(false);
        config.setKeepAliveInterval(120000); // 2 minutes
        config.setLogConnections(false);
        return config;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "ConnectionConfig{" +
                "defaultConnectionType='" + defaultConnectionType + '\'' +
                ", connectionTimeout=" + connectionTimeout +
                ", retryCount=" + retryCount +
                ", autoReconnect=" + autoReconnect +
                ", wakeOnLanEnabled=" + wakeOnLanEnabled +
                ", wakeOnLanPort=" + wakeOnLanPort +
                ", useEncryption=" + useEncryption +
                ", logConnections=" + logConnections +
                ", maxConnectionAttempts=" + maxConnectionAttempts +
                ", keepAliveInterval=" + keepAliveInterval +
                ", validateCertificates=" + validateCertificates +
                '}';
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(defaultConnectionType);
        dest.writeInt(connectionTimeout);
        dest.writeInt(retryCount);
        dest.writeByte((byte) (autoReconnect ? 1 : 0));
        dest.writeByte((byte) (wakeOnLanEnabled ? 1 : 0));
        dest.writeInt(wakeOnLanPort);
        dest.writeByte((byte) (useEncryption ? 1 : 0));
        dest.writeString(encryptionKey);
        dest.writeByte((byte) (logConnections ? 1 : 0));
        dest.writeInt(maxConnectionAttempts);
        dest.writeInt(keepAliveInterval);
        dest.writeByte((byte) (validateCertificates ? 1 : 0));
    }
}