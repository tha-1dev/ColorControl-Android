package com.maassoft.colorcontrol.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    // Fixed: Organize permissions by feature
    private static final String[] NETWORK_PERMISSIONS = {
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    };
    
    private static final String[] BLUETOOTH_PERMISSIONS;
    
    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BLUETOOTH_PERMISSIONS = new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            BLUETOOTH_PERMISSIONS = new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
    }
    
    private static final String[] LOCATION_PERMISSIONS = {
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static boolean checkAndRequestPermissions(Activity activity) {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // Check all required permissions
        permissionsToRequest.addAll(getMissingPermissions(activity, NETWORK_PERMISSIONS));
        permissionsToRequest.addAll(getMissingPermissions(activity, BLUETOOTH_PERMISSIONS));
        permissionsToRequest.addAll(getMissingPermissions(activity, LOCATION_PERMISSIONS));
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toArray(new String[0]),
                PERMISSION_REQUEST_CODE
            );
            return false;
        }
        
        return true;
    }
    
    public static boolean hasRequiredPermissions(Context context) {
        return getMissingPermissions(context, NETWORK_PERMISSIONS).isEmpty() &&
               getMissingPermissions(context, BLUETOOTH_PERMISSIONS).isEmpty() &&
               getMissingPermissions(context, LOCATION_PERMISSIONS).isEmpty();
    }
    
    private static List<String> getMissingPermissions(Context context, String[] permissions) {
        List<String> missingPermissions = new ArrayList<>();
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        
        return missingPermissions;
    }
    
    public static boolean handlePermissionResult(int requestCode, String[] permissions, 
                                               int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return false;
        }
        
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                // Check if this is a critical permission
                if (isCriticalPermission(permissions[i])) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private static boolean isCriticalPermission(String permission) {
        return permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
               permission.equals(Manifest.permission.BLUETOOTH_CONNECT);
    }
    
    public static String getPermissionRationaleMessage(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return "ต้องการเข้าถึงตำแหน่งเพื่อค้นหาอุปกรณ์บลูทูธ";
            case Manifest.permission.BLUETOOTH_CONNECT:
                return "ต้องการสิทธิ์บลูทูธเพื่อเชื่อมต่อกับทีวี";
            default:
                return "จำเป็นต้องได้รับสิทธิ์นี้เพื่อการทำงานของแอป";
        }
    }
}