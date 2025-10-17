package com.maassoft.colorcontrol.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.maassoft.colorcontrol.R;
import com.maassoft.colorcontrol.models.TvDevice;
import com.maassoft.colorcontrol.services.TvControlService;
import com.maassoft.colorcontrol.utils.AnimationUtils;
import com.maassoft.colorcontrol.widgets.TvRemoteView;

public class ControlActivity extends AppCompatActivity 
    implements TvRemoteView.RemoteButtonClickListener,
               TvControlService.ServiceCallback {
    
    private TvDevice tvDevice;
    private String connectionType;
    
    private TextView tvDeviceName;
    private TextView tvConnectionStatus;
    private TvRemoteView tvRemoteView;
    
    private TvControlService tvControlService;
    private boolean isServiceBound = false;
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TvControlService.TvControlBinder binder = (TvControlService.TvControlBinder) service;
            tvControlService = binder.getService();
            tvControlService.setServiceCallback(ControlActivity.this);
            isServiceBound = true;
            
            // Connect to TV device
            if (tvDevice != null) {
                tvControlService.connectToDevice(tvDevice);
            }
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            tvControlService = null;
            isServiceBound = false;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        
        // Get TV device from intent
        tvDevice = getIntent().getParcelableExtra("tv_device");
        connectionType = getIntent().getStringExtra("connection_type");
        
        setupToolbar();
        setupViews();
        setupRemoteView();
        startControlService();
    }
    
    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ควบคุมทีวี");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupViews() {
        tvDeviceName = findViewById(R.id.tvDeviceName);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        tvRemoteView = findViewById(R.id.tvRemoteView);
        
        if (tvDevice != null) {
            tvDeviceName.setText(tvDevice.getDisplayName());
        }
        
        // Setup wake TV button
        findViewById(R.id.btnWakeTv).setOnClickListener(v -> {
            AnimationUtils.animateButtonPress(v);
            wakeUpTv();
        });
        
        // Setup disconnect button
        findViewById(R.id.btnDisconnect).setOnClickListener(v -> {
            AnimationUtils.animateButtonPress(v);
            disconnect();
        });
    }
    
    private void setupRemoteView() {
        tvRemoteView.setRemoteButtonClickListener(this);
        tvRemoteView.setConnectedState(false); // Initially disconnected
    }
    
    private void startControlService() {
        Intent intent = new Intent(this, TvControlService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void wakeUpTv() {
        if (tvControlService != null) {
            tvControlService.wakeUpTv();
            AnimationUtils.pulseAnimation(findViewById(R.id.btnWakeTv));
        }
    }
    
    private void disconnect() {
        if (tvControlService != null) {
            tvControlService.disconnect();
        }
        finish();
    }
    
    // TvRemoteView.RemoteButtonClickListener implementation
    @Override
    public void onRemoteButtonClick(String buttonCommand) {
        if (tvControlService != null && isServiceBound) {
            tvControlService.sendCommand(buttonCommand);
            tvRemoteView.highlightButton(buttonCommand);
        }
    }
    
    @Override
    public void onNumberPadClick(int number) {
        // Handle number pad input
        String command = "NUM_" + number;
        onRemoteButtonClick(command);
    }
    
    // TvControlService.ServiceCallback implementation
    @Override
    public void onConnectionStateChanged(boolean connected) {
        runOnUiThread(() -> {
            tvConnectionStatus.setText(connected ? "เชื่อมต่อแล้ว" : "ไม่ได้เชื่อมต่อ");
            tvConnectionStatus.setTextColor(getColor(
                connected ? R.color.connected_green : R.color.disconnected_red
            ));
            
            tvRemoteView.setConnectedState(connected);
            
            if (connected) {
                AnimationUtils.fadeIn(findViewById(R.id.connectionStatusCard), 500);
            }
        });
    }
    
    @Override
    public void onCommandSent(String command, boolean success) {
        runOnUiThread(() -> {
            if (success) {
                // Show success feedback
                AnimationUtils.pulseAnimation(tvRemoteView);
            } else {
                // Show error feedback
                AnimationUtils.shakeAnimation(tvRemoteView);
            }
        });
    }
    
    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> {
            tvConnectionStatus.setText("ข้อผิดพลาด: " + errorMessage);
            tvConnectionStatus.setTextColor(getColor(R.color.error_red));
            AnimationUtils.shakeAnimation(findViewById(R.id.connectionStatusCard));
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Unbind service
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}