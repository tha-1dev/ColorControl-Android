package com.maassoft.colorcontrol.network;

import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WakeOnLan {
    private static final String TAG = "WakeOnLan";
    private static final int WOL_PORT = 9;

    public static boolean sendWakeOnLan(String macAddress, String ipAddress) {
        if (macAddress == null || macAddress.isEmpty()) {
            Log.e(TAG, "MAC address is null or empty");
            return false;
        }

        try {
            byte[] macBytes = getMacBytes(macAddress);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            
            // Create magic packet
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }
            
            // Send packet
            InetAddress address = InetAddress.getByName(getBroadcastAddress(ipAddress));
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, WOL_PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
            
            Log.i(TAG, "Wake-on-LAN packet sent to " + macAddress);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to send Wake-on-LAN: " + e.getMessage());
            return false;
        }
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-|\\.)");
        
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address format");
        }
        
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address");
        }
        
        return bytes;
    }

    private static String getBroadcastAddress(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + "." + parts[2] + ".255";
        }
        return "255.255.255.255";
    }
}