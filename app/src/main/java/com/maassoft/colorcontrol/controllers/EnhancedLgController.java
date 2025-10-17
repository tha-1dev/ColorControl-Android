package com.maassoft.colorcontrol.controllers;

import android.util.Log;
import okhttp3.*;
import java.util.concurrent.TimeUnit;

public class EnhancedLgController {
    private static final String TAG = "EnhancedLgController";
    private static final int LG_PORT = 8080;
    private static final int LG_WAKE_PORT = 9999;
    
    private OkHttpClient httpClient;
    private String currentIp;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    
    public EnhancedLgController() {
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
                String testUrl = "http://" + currentIp + ":" + LG_PORT + "/roap/api/auth";
                Request request = new Request.Builder()
                    .url(testUrl)
                    .head()
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        Log.i(TAG, "LG TV connection test successful");
                        retryCount = 0;
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "LG TV connection test attempt " + (i + 1) + " failed: " + e.getMessage());
                
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
        
        Log.e(TAG, "All LG TV connection attempts failed");
        return false;
    }
    
    public boolean sendCommand(String command) {
        if (!validateConnection()) {
            return false;
        }
        
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                String url = "http://" + currentIp + ":" + LG_PORT + "/roap/api/command";
                String xmlPayload = buildCommandXml(command);
                
                RequestBody body = RequestBody.create(
                    xmlPayload, 
                    MediaType.parse("application/atom+xml")
                );
                
                Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/atom+xml")
                    .addHeader("Connection", "close")
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "LG command sent successfully: " + command);
                        retryCount = 0;
                        return true;
                    } else {
                        Log.w(TAG, "LG command failed with HTTP " + response.code());
                        
                        if (response.code() == 401) {
                            Log.e(TAG, "LG TV authentication required");
                            break;
                        }
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error sending LG command on attempt " + (attempt + 1) + ": " + e.getMessage());
                
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
        Log.e(TAG, "All LG command sending attempts failed");
        return false;
    }
    
    public boolean wakeUp() {
        if (!validateConnection()) {
            return false;
        }
        
        // Try multiple wake methods for LG TV
        Log.i(TAG, "Attempting to wake up LG TV");
        
        // Method 1: Send power command multiple times
        for (int i = 0; i < 3; i++) {
            if (sendCommand(LgCommand.POWER_ON)) {
                Log.i(TAG, "LG TV wake-up successful via power command");
                return true;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Method 2: Send home command
        if (sendCommand(LgCommand.HOME)) {
            Log.i(TAG, "LG TV wake-up successful via home command");
            return true;
        }
        
        // Method 3: Send OK command
        if (sendCommand(LgCommand.OK)) {
            Log.i(TAG, "LG TV wake-up successful via OK command");
            return true;
        }
        
        Log.e(TAG, "All LG TV wake-up attempts failed");
        return false;
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
    
    private String buildCommandXml(String command) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
               "<command>" +
               "<name>HandleKeyInput</name>" +
               "<value>" + command + "</value>" +
               "</command>";
    }
    
    // LG TV Command Codes
    public static class LgCommand {
        // Basic Commands
        public static final String POWER_ON = "1";
        public static final String POWER_OFF = "2";
        public static final String HOME = "33";
        public static final String MENU = "34";
        public static final String BACK = "35";
        public static final String OK = "40";
        
        // Volume Controls
        public static final String VOLUME_UP = "24";
        public static final String VOLUME_DOWN = "25";
        public static final String MUTE = "26";
        
        // Channel Controls
        public static final String CHANNEL_UP = "33";
        public static final String CHANNEL_DOWN = "34";
        
        // Navigation
        public static final String UP = "12";
        public static final String DOWN = "13";
        public static final String LEFT = "14";
        public static final String RIGHT = "15";
        
        // Numbers
        public static final String NUM_0 = "48";
        public static final String NUM_1 = "49";
        public static final String NUM_2 = "50";
        public static final String NUM_3 = "51";
        public static final String NUM_4 = "52";
        public static final String NUM_5 = "53";
        public static final String NUM_6 = "54";
        public static final String NUM_7 = "55";
        public static final String NUM_8 = "56";
        public static final String NUM_9 = "57";
        
        // Color Buttons
        public static final String RED = "116";
        public static final String GREEN = "117";
        public static final String YELLOW = "118";
        public static final String BLUE = "119";
        
        // Additional Controls
        public static final String INPUT = "47";
        public static final String INFO = "28";
        public static final String EXIT = "36";
        public static final String GUIDE = "58";
        public static final String SUBTITLE = "59";
        public static final String RECORD = "85";
        public static final String PLAY = "68";
        public static final String PAUSE = "69";
        public static final String STOP = "71";
        public static final String REWIND = "72";
        public static final String FAST_FORWARD = "73";
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