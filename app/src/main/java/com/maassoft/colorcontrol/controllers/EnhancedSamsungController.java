package com.maassoft.colorcontrol.controllers;

import android.util.Log;
import okhttp3.*;
import java.util.concurrent.TimeUnit;

public class EnhancedSamsungController {
    private static final String TAG = "EnhancedSamsungController";
    private static final int SAMSUNG_PORT = 8001;
    private static final int SAMSUNG_WAKE_PORT = 8002;
    
    private OkHttpClient httpClient;
    private String currentIp;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    
    public EnhancedSamsungController() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS))
            .addInterceptor(new RetryInterceptor())
            .build();
    }
    
    public boolean connect(String ip) {
        this.currentIp = ip;
        return validateConnection() && testConnection();
    }
    
    private boolean validateConnection() {
        if (currentIp == null || currentIp.isEmpty()) {
            Log.e(TAG, "Invalid IP address");
            return false;
        }
        
        if (!currentIp.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
            Log.e(TAG, "Invalid IP address format: " + currentIp);
            return false;
        }
        
        return true;
    }
    
    private boolean testConnection() {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                String testUrl = "http://" + currentIp + ":" + SAMSUNG_PORT + "/api/v2/";
                Request request = new Request.Builder()
                    .url(testUrl)
                    .head()
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        Log.i(TAG, "Samsung TV connection test successful");
                        retryCount = 0;
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Samsung TV connection test attempt " + (i + 1) + " failed: " + e.getMessage());
                
                if (i < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(1000 * (i + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        Log.e(TAG, "All Samsung TV connection attempts failed");
        return false;
    }
    
    public boolean sendCommand(String command) {
        if (!validateConnection()) {
            return false;
        }
        
        // Samsung TVs use different protocols - try both methods
        boolean method1 = sendXmlCommand(command);
        if (method1) {
            return true;
        }
        
        // Fall back to JSON method
        return sendJsonCommand(command);
    }
    
    private boolean sendXmlCommand(String command) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                String url = "http://" + currentIp + ":" + SAMSUNG_PORT + "/api/v2/";
                String xmlPayload = buildXmlCommand(command);
                
                RequestBody body = RequestBody.create(
                    xmlPayload, 
                    MediaType.parse("application/xml")
                );
                
                Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/xml")
                    .addHeader("Connection", "close")
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Samsung XML command sent successfully: " + command);
                        retryCount = 0;
                        return true;
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error sending Samsung XML command on attempt " + (attempt + 1) + ": " + e.getMessage());
                
                if (attempt < MAX_RETRIES - 1) {
                    try {
                        long backoffTime = 1000 * (long) Math.pow(2, attempt);
                        Thread.sleep(backoffTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean sendJsonCommand(String command) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                String url = "http://" + currentIp + ":" + SAMSUNG_PORT + "/api/v2/channels/samsung.remote.control";
                String jsonPayload = buildJsonCommand(command);
                
                RequestBody body = RequestBody.create(
                    jsonPayload, 
                    MediaType.parse("application/json")
                );
                
                Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Connection", "close")
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Samsung JSON command sent successfully: " + command);
                        retryCount = 0;
                        return true;
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error sending Samsung JSON command on attempt " + (attempt + 1) + ": " + e.getMessage());
                
                if (attempt < MAX_RETRIES - 1) {
                    try {
                        long backoffTime = 1000 * (long) Math.pow(2, attempt);
                        Thread.sleep(backoffTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        retryCount++;
        Log.e(TAG, "All Samsung command sending attempts failed");
        return false;
    }
    
    public boolean wakeUp() {
        if (!validateConnection()) {
            return false;
        }
        
        // Try multiple wake methods for Samsung TV
        Log.i(TAG, "Attempting to wake up Samsung TV");
        
        // Method 1: Send power command
        if (sendCommand(SamsungCommand.POWER)) {
            Log.i(TAG, "Samsung TV wake-up successful via power command");
            return true;
        }
        
        // Method 2: Send multiple key presses
        for (int i = 0; i < 3; i++) {
            if (sendCommand(SamsungCommand.HOME)) {
                Log.i(TAG, "Samsung TV wake-up successful via home command");
                return true;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Method 3: Try different command formats
        if (sendKeyPress("KEY_POWER")) {
            Log.i(TAG, "Samsung TV wake-up successful via key press");
            return true;
        }
        
        Log.e(TAG, "All Samsung TV wake-up attempts failed");
        return false;
    }
    
    private boolean sendKeyPress(String key) {
        try {
            String url = "http://" + currentIp + ":" + SAMSUNG_PORT + "/api/v2/channels/samsung.remote.control";
            String payload = "{\"method\":\"ms.remote.control\",\"params\":{\"Cmd\":\"Click\",\"DataOfCmd\":\"" + key + "\",\"Option\":\"false\",\"TypeOfRemote\":\"SendRemoteKey\"}}";
            
            RequestBody body = RequestBody.create(
                payload, 
                MediaType.parse("application/json")
            );
            
            Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
            
            Response response = httpClient.newCall(request).execute();
            return response.isSuccessful();
            
        } catch (Exception e) {
            Log.e(TAG, "Error sending key press: " + e.getMessage());
            return false;
        }
    }
    
    public void disconnect() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
        currentIp = null;
        retryCount = 0;
    }
    
    public void cleanup() {
        disconnect();
    }
    
    private String buildXmlCommand(String command) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
               "<remote>" +
               "<command>" + command + "</command>" +
               "</remote>";
    }
    
    private String buildJsonCommand(String command) {
        return "{\"method\":\"ms.remote.control\",\"params\":{\"Cmd\":\"Click\",\"DataOfCmd\":\"" + command + "\",\"Option\":\"false\",\"TypeOfRemote\":\"SendRemoteKey\"}}";
    }
    
    // Samsung TV Command Codes
    public static class SamsungCommand {
        // Basic Commands
        public static final String POWER = "KEY_POWER";
        public static final String HOME = "KEY_HOME";
        public static final String MENU = "KEY_MENU";
        public static final String BACK = "KEY_RETURN";
        public static final String OK = "KEY_ENTER";
        
        // Volume Controls
        public static final String VOLUME_UP = "KEY_VOLUP";
        public static final String VOLUME_DOWN = "KEY_VOLDOWN";
        public static final String MUTE = "KEY_MUTE";
        
        // Channel Controls
        public static final String CHANNEL_UP = "KEY_CHUP";
        public static final String CHANNEL_DOWN = "KEY_CHDOWN";
        
        // Navigation
        public static final String UP = "KEY_UP";
        public static final String DOWN = "KEY_DOWN";
        public static final String LEFT = "KEY_LEFT";
        public static final String RIGHT = "KEY_RIGHT";
        
        // Numbers
        public static final String NUM_0 = "KEY_0";
        public static final String NUM_1 = "KEY_1";
        public static final String NUM_2 = "KEY_2";
        public static final String NUM_3 = "KEY_3";
        public static final String NUM_4 = "KEY_4";
        public static final String NUM_5 = "KEY_5";
        public static final String NUM_6 = "KEY_6";
        public static final String NUM_7 = "KEY_7";
        public static final String NUM_8 = "KEY_8";
        public static final String NUM_9 = "KEY_9";
        
        // Color Buttons
        public static final String RED = "KEY_RED";
        public static final String GREEN = "KEY_GREEN";
        public static final String YELLOW = "KEY_YELLOW";
        public static final String BLUE = "KEY_BLUE";
        
        // Additional Controls
        public static final String SOURCE = "KEY_SOURCE";
        public static final String INFO = "KEY_INFO";
        public static final String EXIT = "KEY_EXIT";
        public static final String GUIDE = "KEY_GUIDE";
        public static final String SUBTITLE = "KEY_SUBTITLE";
        public static final String RECORD = "KEY_RECORD";
        public static final String PLAY = "KEY_PLAY";
        public static final String PAUSE = "KEY_PAUSE";
        public static final String STOP = "KEY_STOP";
        public static final String REWIND = "KEY_REWIND";
        public static final String FAST_FORWARD = "KEY_FF";
    }
    
    private static class RetryInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = null;
            IOException exception = null;
            
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    response = chain.proceed(request);
                    if (response.isSuccessful()) {
                        return response;
                    }
                } catch (IOException e) {
                    exception = e;
                    if (i < MAX_RETRIES - 1) {
                        try {
                            Thread.sleep(1000 * (i + 1));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new IOException("Interrupted during retry", ie);
                        }
                    }
                }
            }
            
            if (exception != null) {
                throw exception;
            }
            
            return response != null ? response : 
                new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(500)
                    .message("All retry attempts failed")
                    .build();
        }
    }
    
    // Utility methods
    public boolean isConnected() {
        return currentIp != null;
    }
    
    public String getCurrentIp() {
        return currentIp;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public void resetRetryCount() {
        retryCount = 0;
    }
}