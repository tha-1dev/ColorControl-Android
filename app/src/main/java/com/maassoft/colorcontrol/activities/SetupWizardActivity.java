package com.maassoft.colorcontrol.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.maassoft.colorcontrol.R;
import com.maassoft.colorcontrol.adapters.SetupWizardAdapter;
import com.maassoft.colorcontrol.utils.AnimationUtils;
import com.maassoft.colorcontrol.utils.SharedPrefsManager;
import java.util.Arrays;
import java.util.List;

public class SetupWizardActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Button btnNext;
    private Button btnPrevious;
    private Button btnFinish;
    private ProgressBar progressBar;

    private SetupWizardAdapter adapter;
    private SharedPrefsManager prefsManager;

    // Wizard pages
    private final List<Integer> wizardPages = Arrays.asList(
            R.layout.wizard_page_welcome,
            R.layout.wizard_page_permissions,
            R.layout.wizard_page_connection,
            R.layout.wizard_page_discovery,
            R.layout.wizard_page_complete
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_wizard);

        prefsManager = new SharedPrefsManager(this);
        setupViews();
        setupViewPager();
        setupButtons();
        updateProgress();
    }

    private void setupViews() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnFinish = findViewById(R.id.btnFinish);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupViewPager() {
        adapter = new SetupWizardAdapter(this, wizardPages);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false); // Disable swipe

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Empty tabs for indicator only
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateUI();
                updateProgress();
            }
        });
    }

    private void setupButtons() {
        btnNext.setOnClickListener(v -> {
            AnimationUtils.animateButtonPress(v);
            navigateToNextPage();
        });

        btnPrevious.setOnClickListener(v -> {
            AnimationUtils.animateButtonPress(v);
            navigateToPreviousPage();
        });

        btnFinish.setOnClickListener(v -> {
            AnimationUtils.animateButtonPress(v);
            completeSetup();
        });
    }

    private void navigateToNextPage() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < wizardPages.size() - 1) {
            viewPager.setCurrentItem(currentItem + 1, true);
        }
    }

    private void navigateToPreviousPage() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem > 0) {
            viewPager.setCurrentItem(currentItem - 1, true);
        }
    }

    private void updateUI() {
        int currentItem = viewPager.getCurrentItem();
        int totalItems = wizardPages.size();

        // Update button visibility
        btnPrevious.setVisibility(currentItem > 0 ? Button.VISIBLE : Button.INVISIBLE);
        btnNext.setVisibility(currentItem < totalItems - 1 ? Button.VISIBLE : Button.INVISIBLE);
        btnFinish.setVisibility(currentItem == totalItems - 1 ? Button.VISIBLE : Button.INVISIBLE);

        // Update button texts
        if (currentItem == totalItems - 2) {
            btnNext.setText("เสร็จสิ้น");
        } else {
            btnNext.setText("ต่อไป");
        }
    }

    private void updateProgress() {
        int currentItem = viewPager.getCurrentItem();
        int totalItems = wizardPages.size();
        int progress = (int) ((currentItem + 1) * 100.0 / totalItems);

        progressBar.setProgress(progress);

        // Animate progress bar
        AnimationUtils.pulseAnimation(progressBar);
    }

    private void completeSetup() {
        // Save setup completion
        prefsManager.setFirstLaunch(false);

        // Apply default settings
        prefsManager.setAutoConnectEnabled(true);
        prefsManager.setAutoWakeEnabled(true);
        prefsManager.setVibrationFeedbackEnabled(true);

        // Navigate to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // Add transition animation
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() > 0) {
            navigateToPreviousPage();
        } else {
            super.onBackPressed();
        }
    }
}