package com.maassoft.colorcontrol.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.maassoft.colorcontrol.models.TvDevice;
import com.maassoft.colorcontrol.network.TvDiscovery;
import com.maassoft.colorcontrol.utils.SharedPrefsManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {
    private MutableLiveData<String> connectionState = new MutableLiveData<>("DISCONNECTED");
    private MutableLiveData<List<TvDevice>> discoveredDevices = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    private SharedPrefsManager prefsManager;
    private ExecutorService executorService;
    private TvDiscovery tvDiscovery;

    public MainViewModel(Application application) {
        super(application);
        prefsManager = new SharedPrefsManager(application);
        executorService = Executors.newSingleThreadExecutor();
        tvDiscovery = new TvDiscovery(application);
        
        // Load previously connected devices
        loadSavedDevices();
    }

    public void startDeviceDiscovery() {
        executorService.execute(() -> {
            try {
                List<TvDevice> devices = tvDiscovery.discoverTvs();
                discoveredDevices.postValue(devices);
            } catch (Exception e) {
                errorMessage.postValue("การค้นหาอุปกรณ์ล้มเหลว: " + e.getMessage());
            }
        });
    }

    public void refreshConnectionState() {
        // Implement connection state refresh logic
        connectionState.postValue("DISCONNECTED");
    }

    private void loadSavedDevices() {
        // Load saved devices from SharedPreferences
        // This is a placeholder implementation
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // Getters for LiveData
    public MutableLiveData<String> getConnectionState() {
        return connectionState;
    }

    public MutableLiveData<List<TvDevice>> getDiscoveredDevices() {
        return discoveredDevices;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cleanup();
    }
}