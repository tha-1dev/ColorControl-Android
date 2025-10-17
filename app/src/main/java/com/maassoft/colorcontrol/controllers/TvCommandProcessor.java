package com.maassoft.colorcontrol.controllers;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class TvCommandProcessor {
    private static final String TAG = "TvCommandProcessor";
    
    private EnhancedLgController lgController;
    private EnhancedSamsungController samsungController;
    private SonyController sonyController;
    
    private String currentBrand;
    private String currentIp;

    public TvCommandProcessor() {
        this.lgController = new EnhancedLgController();
        this.samsungController = new EnhancedSamsungController();
        this.sonyController = new SonyController();
    }

    public boolean connect(String ip, String brand) {
        this.currentIp = ip;
        this.currentBrand = brand;
        
        switch (brand.toUpperCase()) {
            case "LG":
                return lgController.connect(ip);
            case "SAMSUNG":
                return samsungController.connect(ip);
            case "SONY":
                return sonyController.connect(ip);
            default:
                Log.e(TAG, "Unsupported TV brand: " + brand);
                return false;
        }
    }

    public boolean sendCommand(String commandType) {
        if (currentIp == null || currentBrand == null) {
            Log.e(TAG, "Not connected to any TV");
            return false;
        }

        String command = getCommandCode(currentBrand, commandType);
        if (command == null) {
            Log.e(TAG, "Unknown command: " + commandType + " for brand: " + currentBrand);
            return false;
        }

        switch (currentBrand.toUpperCase()) {
            case "LG":
                return lgController.sendCommand(command);
            case "SAMSUNG":
                return samsungController.sendCommand(command);
            case "SONY":
                return sonyController.sendCommand(command);
            default:
                return false;
        }
    }

    public boolean wakeUp() {
        if (currentIp == null || currentBrand == null) {
            return false;
        }

        switch (currentBrand.toUpperCase()) {
            case "LG":
                return lgController.wakeUp();
            case "SAMSUNG":
                return samsungController.wakeUp();
            case "SONY":
                return sonyController.wakeUp();
            default:
                return false;
        }
    }

    private String getCommandCode(String brand, String commandType) {
        Map<String, String> commands = new HashMap<>();
        
        switch (brand.toUpperCase()) {
            case "LG":
                commands.put("POWER", EnhancedLgController.LgCommand.POWER_ON);
                commands.put("VOLUME_UP", EnhancedLgController.LgCommand.VOLUME_UP);
                commands.put("VOLUME_DOWN", EnhancedLgController.LgCommand.VOLUME_DOWN);
                commands.put("MUTE", EnhancedLgController.LgCommand.MUTE);
                commands.put("HOME", EnhancedLgController.LgCommand.HOME);
                commands.put("BACK", EnhancedLgController.LgCommand.BACK);
                break;
                
            case "SAMSUNG":
                commands.put("POWER", EnhancedSamsungController.SamsungCommand.POWER);
                commands.put("VOLUME_UP", EnhancedSamsungController.SamsungCommand.VOLUME_UP);
                commands.put("VOLUME_DOWN", EnhancedSamsungController.SamsungCommand.VOLUME_DOWN);
                commands.put("MUTE", EnhancedSamsungController.SamsungCommand.MUTE);
                commands.put("HOME", EnhancedSamsungController.SamsungCommand.HOME);
                commands.put("BACK", EnhancedSamsungController.SamsungCommand.BACK);
                break;
                
            case "SONY":
                commands.put("POWER", SonyController.SonyCommand.POWER);
                commands.put("VOLUME_UP", SonyController.SonyCommand.VOLUME_UP);
                commands.put("VOLUME_DOWN", SonyController.SonyCommand.VOLUME_DOWN);
                commands.put("MUTE", SonyController.SonyCommand.MUTE);
                commands.put("HOME", SonyController.SonyCommand.HOME);
                commands.put("BACK", SonyController.SonyCommand.BACK);
                break;
        }
        
        return commands.get(commandType);
    }

    public void disconnect() {
        currentIp = null;
        currentBrand = null;
    }
}