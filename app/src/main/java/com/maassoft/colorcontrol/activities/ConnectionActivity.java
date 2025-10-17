package com.maassoft.colorcontrol.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.maassoft.colorcontrol.R;
import com.maassoft.colorcontrol.models.TvDevice;
import com.maassoft.colorcontrol.utils.AnimationUtils;

public class ConnectionActivity extends AppCompatActivity {
    
    private MaterialButtonToggleGroup connectionToggle;
    private AutoCompleteTextView autoCompleteTvBrand;
    private TextInputEditText etIpAddress;
    private TextInputEditText etMacAddress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        
        setupToolbar();
        setupViews();
        setupConnectionTypeToggle();
        setupBrandAutoComplete();
        setupConnectButton();
    }
    
    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("การเชื่อมต่อ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupViews() {
        connectionToggle = findViewById(R.id.connectionToggle);
        autoCompleteTvBrand = findViewById(R.id.autoCompleteTvBrand);
        etIpAddress = findViewById(R.id.etIpAddress);
        etMacAddress = findViewById(R.id.etMacAddress);
    }
    
    private void setupConnectionTypeToggle() {
        connectionToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                AnimationUtils.animateButtonPress(findViewById(checkedId));
                updateConnectionFields(checkedId);
            }
        });
        
        // Select Auto by default
        connectionToggle.check(R.id.btnAuto);
    }
    
    private void updateConnectionFields(int checkedId) {
        boolean showManualFields = checkedId != R.id.btnAuto;
        
        if (autoCompleteTvBrand != null) {
            autoCompleteTvBrand.setEnabled(showManualFields);
        }
        if (etIpAddress != null) {
            etIpAddress.setEnabled(showManualFields);
        }
        if (etMacAddress != null) {
            etMacAddress.setEnabled(showManualFields);
        }
        
        if (!showManualFields) {
            // Clear fields when Auto is selected
            if (autoCompleteTvBrand != null) autoCompleteTvBrand.setText("");
            if (etIpAddress != null) etIpAddress.setText("");
            if (etMacAddress != null) etMacAddress.setText("");
        }
    }
    
    private void setupBrandAutoComplete() {
        String[] tvBrands = getResources().getStringArray(R.array.tv_brands);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            tvBrands
        );
        autoCompleteTvBrand.setAdapter(adapter);
    }
    
    private void setupConnectButton() {
        findViewById(R.id.btnConnect).setOnClickListener(v -> {
            AnimationUtils.animateButtonPress(v);
            attemptConnection();
        });
    }
    
    private void attemptConnection() {
        String connectionType = getSelectedConnectionType();
        String brand = autoCompleteTvBrand.getText().toString().trim();
        String ipAddress = etIpAddress.getText().toString().trim();
        String macAddress = etMacAddress.getText().toString().trim();
        
        // Validate inputs for manual connection
        if (!connectionType.equals("AUTO")) {
            if (brand.isEmpty()) {
                autoCompleteTvBrand.setError("กรุณาเลือกยี่ห้อทีวี");
                return;
            }
            if (ipAddress.isEmpty()) {
                etIpAddress.setError("กรุณาระบุ IP Address");
                return;
            }
        }
        
        // Create TV device
        TvDevice tvDevice = createTvDevice(connectionType, brand, ipAddress, macAddress);
        
        // Navigate to control activity
        Intent intent = new Intent(this, ControlActivity.class);
        intent.putExtra("tv_device", tvDevice);
        intent.putExtra("connection_type", connectionType);
        startActivity(intent);
        
        AnimationUtils.slideInFromBottom(findViewById(R.id.btnConnect), 300);
    }
    
    private TvDevice createTvDevice(String connectionType, String brand, String ip, String mac) {
        String deviceName = brand + " TV";
        if (connectionType.equals("AUTO")) {
            deviceName = "TV Auto-Discovery";
            brand = "AUTO";
            ip = "AUTO";
        }
        
        TvDevice device = new TvDevice(deviceName, ip, brand);
        device.setMacAddress(mac);
        device.setConnectionType(connectionType);
        
        return device;
    }
    
    private String getSelectedConnectionType() {
        int checkedId = connectionToggle.getCheckedButtonId();
        
        if (checkedId == R.id.btnWifi) return "WIFI";
        if (checkedId == R.id.btnBluetooth) return "BLUETOOTH";
        if (checkedId == R.id.btnUsb) return "USB";
        return "AUTO"; // Default to Auto
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}