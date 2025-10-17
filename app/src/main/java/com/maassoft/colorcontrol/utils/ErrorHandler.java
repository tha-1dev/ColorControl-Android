package com.maassoft.colorcontrol.utils;

import android.util.Log;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLHandshakeException;

public class ErrorHandler {
    private static final String TAG = "ErrorHandler";
    
    public static String handleNetworkError(Throwable error) {
        if (error instanceof SocketTimeoutException) {
            Log.w(TAG, "Network timeout occurred");
            return "การเชื่อมต่อหมดเวลา กรุณาลองใหม่อีกครั้ง";
        } else if (error instanceof ConnectException) {
            Log.w(TAG, "Connection refused");
            return "ไม่สามารถเชื่อมต่อกับทีวีได้ กรุณาตรวจสอบการตั้งค่า";
        } else if (error instanceof UnknownHostException) {
            Log.w(TAG, "Unknown host");
            return "ไม่พบทีวีในเครือข่าย กรุณาตรวจสอบ IP Address";
        } else if (error instanceof SSLHandshakeException) {
            Log.w(TAG, "SSL handshake failed");
            return "การเชื่อมต่อปลอดภัยล้มเหลว";
        } else if (error instanceof IOException) {
            Log.w(TAG, "Network IO error: " + error.getMessage());
            return "ข้อผิดพลาดในการเชื่อมต่อเครือข่าย";
        } else {
            Log.e(TAG, "Unexpected error: " + error.getMessage(), error);
            return "เกิดข้อผิดพลาดที่ไม่คาดคิด";
        }
    }
    
    public static boolean isRecoverableError(Throwable error) {
        return error instanceof SocketTimeoutException ||
               error instanceof IOException;
    }
    
    public static void logError(String context, Throwable error) {
        Log.e(TAG, "Error in " + context + ": " + error.getMessage(), error);
    }
}