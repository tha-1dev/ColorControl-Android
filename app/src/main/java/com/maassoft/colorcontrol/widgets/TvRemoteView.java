package com.maassoft.colorcontrol.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.maassoft.colorcontrol.R;
import com.maassoft.colorcontrol.utils.AnimationUtils;

public class TvRemoteView extends LinearLayout {
    
    private RemoteButtonClickListener buttonClickListener;
    
    // Remote buttons
    private CosmicButton btnPower;
    private CosmicButton btnVolumeUp;
    private CosmicButton btnVolumeDown;
    private CosmicButton btnMute;
    private CosmicButton btnChannelUp;
    private CosmicButton btnChannelDown;
    private CosmicButton btnHome;
    private CosmicButton btnBack;
    private CosmicButton btnMenu;
    private CosmicButton btnSource;
    private CosmicButton btnUp;
    private CosmicButton btnDown;
    private CosmicButton btnLeft;
    private CosmicButton btnRight;
    private CosmicButton btnOk;
    
    // Number pad buttons
    private CosmicButton[] numberButtons = new CosmicButton[10];
    
    public interface RemoteButtonClickListener {
        void onRemoteButtonClick(String buttonCommand);
        void onNumberPadClick(int number);
    }
    
    public TvRemoteView(Context context) {
        super(context);
        init(context);
    }
    
    public TvRemoteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public TvRemoteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.widget_remote, this, true);
        
        findViews(view);
        setupClickListeners();
        setupAnimations();
    }
    
    private void findViews(View view) {
        // Basic controls
        btnPower = view.findViewById(R.id.btnPower);
        btnVolumeUp = view.findViewById(R.id.btnVolumeUp);
        btnVolumeDown = view.findViewById(R.id.btnVolumeDown);
        btnMute = view.findViewById(R.id.btnMute);
        btnChannelUp = view.findViewById(R.id.btnChannelUp);
        btnChannelDown = view.findViewById(R.id.btnChannelDown);
        btnHome = view.findViewById(R.id.btnHome);
        btnBack = view.findViewById(R.id.btnBack);
        btnMenu = view.findViewById(R.id.btnMenu);
        btnSource = view.findViewById(R.id.btnSource);
        
        // Navigation controls
        btnUp = view.findViewById(R.id.btnUp);
        btnDown = view.findViewById(R.id.btnDown);
        btnLeft = view.findViewById(R.id.btnLeft);
        btnRight = view.findViewById(R.id.btnRight);
        btnOk = view.findViewById(R.id.btnOk);
        
        // Number pad
        numberButtons[0] = view.findViewById(R.id.btnNum0);
        numberButtons[1] = view.findViewById(R.id.btnNum1);
        numberButtons[2] = view.findViewById(R.id.btnNum2);
        numberButtons[3] = view.findViewById(R.id.btnNum3);
        numberButtons[4] = view.findViewById(R.id.btnNum4);
        numberButtons[5] = view.findViewById(R.id.btnNum5);
        numberButtons[6] = view.findViewById(R.id.btnNum6);
        numberButtons[7] = view.findViewById(R.id.btnNum7);
        numberButtons[8] = view.findViewById(R.id.btnNum8);
        numberButtons[9] = view.findViewById(R.id.btnNum9);
    }
    
    private void setupClickListeners() {
        // Basic controls
        btnPower.setOnClickListener(v -> onButtonClick("POWER"));
        btnVolumeUp.setOnClickListener(v -> onButtonClick("VOLUME_UP"));
        btnVolumeDown.setOnClickListener(v -> onButtonClick("VOLUME_DOWN"));
        btnMute.setOnClickListener(v -> onButtonClick("MUTE"));
        btnChannelUp.setOnClickListener(v -> onButtonClick("CHANNEL_UP"));
        btnChannelDown.setOnClickListener(v -> onButtonClick("CHANNEL_DOWN"));
        btnHome.setOnClickListener(v -> onButtonClick("HOME"));
        btnBack.setOnClickListener(v -> onButtonClick("BACK"));
        btnMenu.setOnClickListener(v -> onButtonClick("MENU"));
        btnSource.setOnClickListener(v -> onButtonClick("SOURCE"));
        
        // Navigation controls
        btnUp.setOnClickListener(v -> onButtonClick("UP"));
        btnDown.setOnClickListener(v -> onButtonClick("DOWN"));
        btnLeft.setOnClickListener(v -> onButtonClick("LEFT"));
        btnRight.setOnClickListener(v -> onButtonClick("RIGHT"));
        btnOk.setOnClickListener(v -> onButtonClick("OK"));
        
        // Number pad
        for (int i = 0; i < numberButtons.length; i++) {
            final int number = i;
            numberButtons[i].setOnClickListener(v -> onNumberClick(number));
        }
        
        // Setup long press listeners for repeated actions
        setupLongPressListeners();
    }
    
    private void setupLongPressListeners() {
        // Volume up long press for continuous volume increase
        btnVolumeUp.setOnLongClickListener(v -> {
            startRepeatedAction("VOLUME_UP");
            return true;
        });
        
        // Volume down long press for continuous volume decrease
        btnVolumeDown.setOnLongClickListener(v -> {
            startRepeatedAction("VOLUME_DOWN");
            return true;
        });
        
        // Channel up long press for continuous channel change
        btnChannelUp.setOnLongClickListener(v -> {
            startRepeatedAction("CHANNEL_UP");
            return true;
        });
        
        // Channel down long press for continuous channel change
        btnChannelDown.setOnLongClickListener(v -> {
            startRepeatedAction("CHANNEL_DOWN");
            return true;
        });
        
        // Setup touch listeners to stop repeated actions
        setupTouchListeners();
    }
    
    private void setupTouchListeners() {
        View.OnTouchListener stopRepeatedActionListener = (v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                stopRepeatedAction();
            }
            return false;
        };
        
        btnVolumeUp.setOnTouchListener(stopRepeatedActionListener);
        btnVolumeDown.setOnTouchListener(stopRepeatedActionListener);
        btnChannelUp.setOnTouchListener(stopRepeatedActionListener);
        btnChannelDown.setOnTouchListener(stopRepeatedActionListener);
    }
    
    private void setupAnimations() {
        // Add click animations to all buttons
        View[] allButtons = {
            btnPower, btnVolumeUp, btnVolumeDown, btnMute,
            btnChannelUp, btnChannelDown, btnHome, btnBack,
            btnMenu, btnSource, btnUp, btnDown, btnLeft,
            btnRight, btnOk
        };
        
        for (View button : allButtons) {
            if (button != null) {
                button.setOnClickListener(v -> {
                    if (button instanceof CosmicButton) {
                        ((CosmicButton) button).animatePress();
                    } else {
                        AnimationUtils.animateButtonPress(button);
                    }
                });
            }
        }
        
        // Add animations to number buttons
        for (CosmicButton numberButton : numberButtons) {
            if (numberButton != null) {
                numberButton.setOnClickListener(v -> numberButton.animatePress());
            }
        }
    }
    
    private void onButtonClick(String command) {
        if (buttonClickListener != null) {
            buttonClickListener.onRemoteButtonClick(command);
        }
        
        // Provide haptic feedback if available
        performHapticFeedback();
    }
    
    private void onNumberClick(int number) {
        if (buttonClickListener != null) {
            buttonClickListener.onNumberPadClick(number);
        }
        
        // Provide haptic feedback if available
        performHapticFeedback();
    }
    
    private void performHapticFeedback() {
        if (isHapticFeedbackEnabled()) {
            performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
        }
    }
    
    private void startRepeatedAction(String command) {
        // Implement repeated action for long press
        // This would typically use a Handler to send repeated commands
        android.util.Log.d("TvRemoteView", "Starting repeated action: " + command);
    }
    
    private void stopRepeatedAction() {
        // Stop any ongoing repeated actions
        android.util.Log.d("TvRemoteView", "Stopping repeated action");
    }
    
    public void setRemoteButtonClickListener(RemoteButtonClickListener listener) {
        this.buttonClickListener = listener;
    }
    
    // Public methods to control remote state
    public void setConnectedState(boolean connected) {
        setEnabled(connected);
        
        // Visual feedback for connection state
        if (connected) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.5f);
        }
    }
    
    public void highlightButton(String buttonCommand) {
        // Implement button highlighting for visual feedback
        CosmicButton button = getButtonForCommand(buttonCommand);
        if (button != null) {
            button.pulse();
        }
    }
    
    private CosmicButton getButtonForCommand(String command) {
        switch (command) {
            case "POWER": return btnPower;
            case "VOLUME_UP": return btnVolumeUp;
            case "VOLUME_DOWN": return btnVolumeDown;
            case "MUTE": return btnMute;
            case "CHANNEL_UP": return btnChannelUp;
            case "CHANNEL_DOWN": return btnChannelDown;
            case "HOME": return btnHome;
            case "BACK": return btnBack;
            case "MENU": return btnMenu;
            case "SOURCE": return btnSource;
            case "UP": return btnUp;
            case "DOWN": return btnDown;
            case "LEFT": return btnLeft;
            case "RIGHT": return btnRight;
            case "OK": return btnOk;
            default: return null;
        }
    }
    
    // Customization methods
    public void setButtonColors(int primaryColor, int secondaryColor) {
        for (CosmicButton button : getAllCosmicButtons()) {
            if (button != null) {
                button.setCosmicColors(primaryColor, secondaryColor);
            }
        }
    }
    
    public void setButtonSize(int sizeDp) {
        // Implement dynamic button size adjustment
    }
    
    public void showNumberPad(boolean show) {
        // Implement number pad visibility toggle
    }
    
    private CosmicButton[] getAllCosmicButtons() {
        return new CosmicButton[] {
            btnPower, btnVolumeUp, btnVolumeDown, btnMute,
            btnChannelUp, btnChannelDown, btnHome, btnBack,
            btnMenu, btnSource, btnUp, btnDown, btnLeft,
            btnRight, btnOk,
            numberButtons[0], numberButtons[1], numberButtons[2],
            numberButtons[3], numberButtons[4], numberButtons[5],
            numberButtons[6], numberButtons[7], numberButtons[8],
            numberButtons[9]
        };
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        
        // Enable/disable all child buttons
        for (CosmicButton button : getAllCosmicButtons()) {
            if (button != null) {
                button.setEnabled(enabled);
            }
        }
    }
}