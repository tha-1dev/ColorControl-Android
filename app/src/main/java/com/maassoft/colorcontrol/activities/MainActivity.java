package com.maassoft.colorcontrol.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maassoft.colorcontrol.R;
import com.maassoft.colorcontrol.adapters.DeviceAdapter;
import com.maassoft.colorcontrol.models.TvDevice;
import com.maassoft.colorcontrol.utils.PermissionManager;
import com.maassoft.colorcontrol.utils.SharedPrefsManager;
import com.maassoft.colorcontrol.viewmodels.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private SharedPrefsManager prefsManager;
    private DeviceAdapter deviceAdapter;
    private RecyclerView rvDevices;
    
    // Fixed: Add weak reference to prevent memory leaks
    private TextView tvConnectionStatus;
    private View scanningProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fixed: Check permissions before initialization
        if (!PermissionManager.checkAndRequestPermissions(this)) {
            return;
        }

        initViews();
        initViewModel();
        setupRecyclerView();
        checkFirstLaunch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fixed: Refresh connection state when returning to app
        refreshConnectionState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fixed: Clean up to prevent memory leaks
        if (viewModel != null) {
            viewModel.cleanup();
        }
        if (deviceAdapter != null) {
            deviceAdapter.clear();
        }
    }

    private void initViews() {
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);
        scanningProgress = findViewById(R.id.scanningProgress);
        rvDevices = findViewById(R.id.rvDevices);
        
        Button btnQuickConnect = findViewById(R.id.btnQuickConnect);
        Button btnManualSetup = findViewById(R.id.btnManualSetup);
        Button btnScanDevices = findViewById(R.id.btnScanDevices);
        Button btnSettings = findViewById(R.id.btnSettings);

        // Fixed: Add null checks for views
        if (btnQuickConnect != null) {
            btnQuickConnect.setOnClickListener(v -> quickConnect());
        }
        if (btnManualSetup != null) {
            btnManualSetup.setOnClickListener(v -> manualSetup());
        }
        if (btnScanDevices != null) {
            btnScanDevices.setOnClickListener(v -> scanDevices());
        }
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> openSettings());
        }
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        prefsManager = new SharedPrefsManager(this);

        // Fixed: Use proper lifecycle-aware observables
        viewModel.getConnectionState().observe(this, state -> {
            if (tvConnectionStatus != null) {
                updateConnectionStatus(state);
            }
        });

        viewModel.getDiscoveredDevices().observe(this, devices -> {
            if (deviceAdapter != null) {
                updateDeviceList(devices);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                showErrorToast(error);
            }
        });
    }

    private void setupRecyclerView() {
        // Fixed: Initialize with empty list to avoid NPE
        deviceAdapter = new DeviceAdapter(new ArrayList<>(), this::onDeviceSelected);
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.setAdapter(deviceAdapter);
    }

    private void checkFirstLaunch() {
        if (prefsManager.isFirstLaunch()) {
            // Fixed: Use Intent flags to prevent multiple instances
            Intent intent = new Intent(this, SetupWizardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            prefsManager.setFirstLaunch(false);
        }
    }

    private void quickConnect() {
        // Fixed: Check network connectivity first
        if (!isNetworkAvailable()) {
            showNetworkError();
            return;
        }

        TvDevice lastDevice = prefsManager.getLastConnectedDevice();
        if (lastDevice != null) {
            connectToDevice(lastDevice);
        } else {
            manualSetup();
        }
    }

    private void manualSetup() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void scanDevices() {
        // Fixed: Check permissions before scanning
        if (!PermissionManager.hasRequiredPermissions(this)) {
            PermissionManager.checkAndRequestPermissions(this);
            return;
        }

        if (!isNetworkAvailable()) {
            showNetworkError();
            return;
        }

        viewModel.startDeviceDiscovery();
        showScanningProgress();
    }

    private void connectToDevice(TvDevice device) {
        // Fixed: Validate device before connecting
        if (device == null || !device.isValid()) {
            showErrorToast("อุปกรณ์ไม่ถูกต้อง");
            return;
        }

        Intent intent = new Intent(this, ControlActivity.class);
        intent.putExtra("tv_device", device);
        startActivity(intent);
    }

    private void onDeviceSelected(TvDevice device) {
        connectToDevice(device);
    }

    private void updateConnectionStatus(String state) {
        if (tvConnectionStatus == null) return;

        tvConnectionStatus.setText(state);
        
        switch (state) {
            case "CONNECTED":
                tvConnectionStatus.setTextColor(getColor(R.color.connected_green));
                break;
            case "CONNECTING":
                tvConnectionStatus.setTextColor(getColor(R.color.connecting_orange));
                break;
            case "DISCONNECTED":
                tvConnectionStatus.setTextColor(getColor(R.color.disconnected_red));
                break;
            default:
                tvConnectionStatus.setTextColor(getColor(R.color.gray_500));
        }
    }

    private void updateDeviceList(List<TvDevice> devices) {
        hideScanningProgress();
        
        if (devices == null || devices.isEmpty()) {
            showNoDevicesFound();
        } else {
            deviceAdapter.updateDevices(devices);
            showDevicesList();
        }
    }

    private void showScanningProgress() {
        if (scanningProgress != null) {
            scanningProgress.setVisibility(View.VISIBLE);
        }
    }

    private void hideScanningProgress() {
        if (scanningProgress != null) {
            scanningProgress.setVisibility(View.GONE);
        }
    }

    private void showNoDevicesFound() {
        TextView tvNoDevices = findViewById(R.id.tvNoDevices);
        if (tvNoDevices != null) {
            tvNoDevices.setVisibility(View.VISIBLE);
        }
        if (rvDevices != null) {
            rvDevices.setVisibility(View.GONE);
        }
    }

    private void showDevicesList() {
        TextView tvNoDevices = findViewById(R.id.tvNoDevices);
        if (tvNoDevices != null) {
            tvNoDevices.setVisibility(View.GONE);
        }
        if (rvDevices != null) {
            rvDevices.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void showNetworkError() {
        showErrorToast("กรุณาตรวจสอบการเชื่อมต่ออินเทอร์เน็ต");
    }

    private void showErrorToast(String message) {
        // Fixed: Use runOnUiThread to avoid thread issues
        runOnUiThread(() -> 
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        );
    }

    private void refreshConnectionState() {
        if (viewModel != null) {
            viewModel.refreshConnectionState();
        }
    }

    // Fixed: Handle configuration changes
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save important state
        if (viewModel != null) {
            outState.putString("connection_state", viewModel.getConnectionState().getValue());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore state
        String savedState = savedInstanceState.getString("connection_state");
        if (savedState != null && tvConnectionStatus != null) {
            updateConnectionStatus(savedState);
        }
    }
}