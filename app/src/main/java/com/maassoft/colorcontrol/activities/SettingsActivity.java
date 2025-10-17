package com.maassoft.colorcontrol.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import com.maassoft.colorcontrol.R;
import com.maassoft.colorcontrol.utils.SharedPrefsManager;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar();
        setupSettingsFragment();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ตั้งค่า");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSettingsFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SharedPrefsManager prefsManager;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            prefsManager = new SharedPrefsManager(requireContext());
            setupPreferences();
        }

        private void setupPreferences() {
            // Connection Settings
            setupAutoConnectPreference();
            setupAutoWakePreference();

            // UI Settings
            setupDarkModePreference();
            setupVibrationPreference();
            setupSoundPreference();
            setupKeepScreenOnPreference();

            // Advanced Settings
            setupClearDataPreference();
            setupAboutPreference();
        }

        private void setupAutoConnectPreference() {
            SwitchPreferenceCompat autoConnectPref = findPreference("auto_connect");
            if (autoConnectPref != null) {
                autoConnectPref.setChecked(prefsManager.isAutoConnectEnabled());
                autoConnectPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = (Boolean) newValue;
                    prefsManager.setAutoConnectEnabled(enabled);
                    showToast(enabled ? "เปิดการเชื่อมต่ออัตโนมัติ" : "ปิดการเชื่อมต่ออัตโนมัติ");
                    return true;
                });
            }
        }

        private void setupAutoWakePreference() {
            SwitchPreferenceCompat autoWakePref = findPreference("auto_wake");
            if (autoWakePref != null) {
                autoWakePref.setChecked(prefsManager.isAutoWakeEnabled());
                autoWakePref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = (Boolean) newValue;
                    prefsManager.setAutoWakeEnabled(enabled);
                    showToast(enabled ? "เปิดการปลุกทีวีอัตโนมัติ" : "ปิดการปลุกทีวีอัตโนมัติ");
                    return true;
                });
            }
        }

        private void setupDarkModePreference() {
            SwitchPreferenceCompat darkModePref = findPreference("dark_mode");
            if (darkModePref != null) {
                darkModePref.setChecked(prefsManager.isDarkModeEnabled());
                darkModePref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = (Boolean) newValue;
                    prefsManager.setDarkModeEnabled(enabled);
                    
                    // Apply theme
                    if (enabled) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    
                    showToast(enabled ? "เปลี่ยนเป็นโหมดมืด" : "เปลี่ยนเป็นโหมดสว่าง");
                    return true;
                });
            }
        }

        private void setupVibrationPreference() {
            SwitchPreferenceCompat vibrationPref = findPreference("vibration_feedback");
            if (vibrationPref != null) {
                vibrationPref.setChecked(prefsManager.isVibrationFeedbackEnabled());
                vibrationPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = (Boolean) newValue;
                    prefsManager.setVibrationFeedbackEnabled(enabled);
                    showToast(enabled ? "เปิดการสั่นสะเทือน" : "ปิดการสั่นสะเทือน");
                    return true;
                });
            }
        }

        private void setupSoundPreference() {
            SwitchPreferenceCompat soundPref = findPreference("sound_feedback");
            if (soundPref != null) {
                soundPref.setChecked(prefsManager.isSoundFeedbackEnabled());
                soundPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = (Boolean) newValue;
                    prefsManager.setSoundFeedbackEnabled(enabled);
                    showToast(enabled ? "เปิดเสียงตอบรับ" : "ปิดเสียงตอบรับ");
                    return true;
                });
            }
        }

        private void setupKeepScreenOnPreference() {
            SwitchPreferenceCompat keepScreenOnPref = findPreference("keep_screen_on");
            if (keepScreenOnPref != null) {
                keepScreenOnPref.setChecked(prefsManager.isKeepScreenOnEnabled());
                keepScreenOnPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean enabled = (Boolean) newValue;
                    prefsManager.setKeepScreenOnEnabled(enabled);
                    
                    if (getActivity() != null) {
                        if (enabled) {
                            getActivity().getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        } else {
                            getActivity().getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                    }
                    
                    showToast(enabled ? "เปิดการคงสภาพหน้าจอ" : "ปิดการคงสภาพหน้าจอ");
                    return true;
                });
            }
        }

        private void setupClearDataPreference() {
            Preference clearDataPref = findPreference("clear_data");
            if (clearDataPref != null) {
                clearDataPref.setOnPreferenceClickListener(preference -> {
                    showClearDataDialog();
                    return true;
                });
            }
        }

        private void setupAboutPreference() {
            Preference aboutPref = findPreference("about");
            if (aboutPref != null) {
                aboutPref.setSummary("ColorControl Universe v1.0.0");
                aboutPref.setOnPreferenceClickListener(preference -> {
                    showAboutDialog();
                    return true;
                });
            }
        }

        private void showClearDataDialog() {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("ล้างข้อมูล")
                    .setMessage("คุณแน่ใจว่าต้องการล้างข้อมูลทั้งหมด? การกระทำนี้ไม่สามารถย้อนกลับได้")
                    .setPositiveButton("ล้างข้อมูล", (dialog, which) -> {
                        prefsManager.clearAllData();
                        showToast("ล้างข้อมูลเรียบร้อยแล้ว");
                    })
                    .setNegativeButton("ยกเลิก", null)
                    .show();
        }

        private void showAboutDialog() {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("เกี่ยวกับ")
                    .setMessage("ColorControl Universe\n\nเวอร์ชัน 1.0.0\n\nแอปพลิเคชันควบคุมทีวีอัจฉริยะที่รองรับการเชื่อมต่อผ่าน WiFi, Bluetooth และ USB")
                    .setPositiveButton("ตกลง", null)
                    .show();
        }

        private void showToast(String message) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}