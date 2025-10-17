package com.maassoft.colorcontrol.controllers;

import android.util.Log;
import okhttp3.*;
import java.util.concurrent.TimeUnit;

public class SonyController {
    private static final String TAG = "SonyController";
    private static final int SONY_PORT = 10000;
    
    private OkHttpClient httpClient;
    private String currentIp;

    public SonyController() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();
    }

    public boolean connect(String ip) {
        this.currentIp = ip;
        return testConnection();
    }

    private boolean testConnection() {
        try {
            String testUrl = "http://" + currentIp + ":" + SONY_PORT + "/sony/system";
            String jsonPayload = "{\"method\":\"getPowerStatus\",\"params\":[],\"id\":1,\"version\":\"1.0\"}";
            
            RequestBody body = RequestBody.create(
                jsonPayload, 
                MediaType.parse("application/json")
            );
            
            Request request = new Request.Builder()
                .url(testUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
            
            Response response = httpClient.newCall(request).execute();
            return response.isSuccessful();
            
        } catch (Exception e) {
            Log.e(TAG, "Connection test failed: " + e.getMessage());
            return false;
        }
    }

    public boolean sendCommand(String command) {
        if (currentIp == null) {
            Log.e(TAG, "No IP address set");
            return false;
        }

        try {
            String url = "http://" + currentIp + ":" + SONY_PORT + "/sony/IRCC";
            String xmlPayload = buildCommandXml(command);
            
            RequestBody body = RequestBody.create(
                xmlPayload, 
                MediaType.parse("application/xml")
            );
            
            Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/xml")
                .addHeader("SOAPAction", "\"urn:schemas-sony-com:service:IRCC:1#X_SendIRCC\"")
                .build();
            
            Response response = httpClient.newCall(request).execute();
            boolean success = response.isSuccessful();
            
            if (success) {
                Log.d(TAG, "Sony command sent successfully: " + command);
            } else {
                Log.e(TAG, "Sony command failed: " + response.code());
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Error sending Sony command: " + e.getMessage());
            return false;
        }
    }

    private String buildCommandXml(String command) {
        return "<?xml version=\"1.0\"?>" +
               "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
               "<s:Body>" +
               "<u:X_SendIRCC xmlns:u=\"urn:schemas-sony-com:service:IRCC:1\">" +
               "<IRCCCode>" + command + "</IRCCCode>" +
               "</u:X_SendIRCC>" +
               "</s:Body>" +
               "</s:Envelope>";
    }

    public boolean wakeUp() {
        return sendCommand(SonyCommand.POWER);
    }

    public static class SonyCommand {
        public static final String POWER = "AAAAAQAAAAEAAAAVAw==";
        public static final String VOLUME_UP = "AAAAAQAAAAEAAAASAw==";
        public static final String VOLUME_DOWN = "AAAAAQAAAAEAAAATAw==";
        public static final String MUTE = "AAAAAQAAAAEAAAAUAw==";
        public static final String HOME = "AAAAAQAAAAEAAABgAw==";
        public static final String BACK = "AAAAAgAAAJcAAAAjAw==";
        public static final String UP = "AAAAAQAAAAEAAAB0Aw==";
        public static final String DOWN = "AAAAAQAAAAEAAAB1Aw==";
        public static final String LEFT = "AAAAAQAAAAEAAAA0Aw==";
        public static final String RIGHT = "AAAAAQAAAAEAAAAzAw==";
        public static final String ENTER = "AAAAAQAAAAEAAAALAw==";
    }
}