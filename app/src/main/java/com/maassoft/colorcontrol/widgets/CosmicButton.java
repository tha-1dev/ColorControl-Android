package com.maassoft.colorcontrol.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.appcompat.widget.AppCompatButton;
import com.maassoft.colorcontrol.R;

public class CosmicButton extends AppCompatButton {
    
    private Paint backgroundPaint;
    private Paint borderPaint;
    private Paint glowPaint;
    private Path buttonPath;
    private RectF buttonRect;
    
    private int cosmicColor1;
    private int cosmicColor2;
    private int borderColor;
    private int glowColor;
    private float cornerRadius;
    private float borderWidth;
    private float glowRadius;
    private boolean isPressed = false;
    private boolean isEnabled = true;
    
    public CosmicButton(Context context) {
        super(context);
        init(context, null);
    }
    
    public CosmicButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public CosmicButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        // Default values
        cosmicColor1 = Color.parseColor("#4FC3F7");
        cosmicColor2 = Color.parseColor("#7E57C2");
        borderColor = Color.parseColor("#FFFFFF");
        glowColor = Color.parseColor("#804FC3F7");
        cornerRadius = 16f;
        borderWidth = 2f;
        glowRadius = 10f;
        
        // Read custom attributes
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CosmicButton);
            
            cosmicColor1 = a.getColor(R.styleable.CosmicButton_cosmicColor1, cosmicColor1);
            cosmicColor2 = a.getColor(R.styleable.CosmicButton_cosmicColor2, cosmicColor2);
            borderColor = a.getColor(R.styleable.CosmicButton_borderColor, borderColor);
            glowColor = a.getColor(R.styleable.CosmicButton_glowColor, glowColor);
            cornerRadius = a.getDimension(R.styleable.CosmicButton_cornerRadius, cornerRadius);
            borderWidth = a.getDimension(R.styleable.CosmicButton_borderWidth, borderWidth);
            glowRadius = a.getDimension(R.styleable.CosmicButton_glowRadius, glowRadius);
            
            a.recycle();
        }
        
        setupPaints();
        setupButton();
    }
    
    private void setupPaints() {
        // Background paint with gradient
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        // Border paint
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(borderColor);
        
        // Glow paint
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setColor(glowColor);
    }
    
    private void setupButton() {
        setBackground(null); // Remove default background
        setPadding(
            (int) (getPaddingLeft() + borderWidth + glowRadius),
            (int) (getPaddingTop() + borderWidth + glowRadius),
            (int) (getPaddingRight() + borderWidth + glowRadius),
            (int) (getPaddingBottom() + borderWidth + glowRadius)
        );
        
        buttonPath = new Path();
        buttonRect = new RectF();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Update gradient
        LinearGradient gradient = new LinearGradient(
            0, 0, w, h,
            cosmicColor1, cosmicColor2,
            Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(gradient);
        
        // Update button path and rect
        buttonRect.set(
            glowRadius + borderWidth,
            glowRadius + borderWidth,
            w - glowRadius - borderWidth,
            h - glowRadius - borderWidth
        );
        
        buttonPath.reset();
        buttonPath.addRoundRect(buttonRect, cornerRadius, cornerRadius, Path.Direction.CW);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (!isEnabled) {
            // Draw disabled state
            backgroundPaint.setAlpha(128);
            borderPaint.setAlpha(128);
        } else if (isPressed) {
            // Draw pressed state
            backgroundPaint.setAlpha(200);
            borderPaint.setAlpha(200);
        } else {
            // Draw normal state
            backgroundPaint.setAlpha(255);
            borderPaint.setAlpha(255);
        }
        
        // Draw glow effect
        if (isEnabled && !isPressed) {
            glowPaint.setAlpha(100);
            canvas.drawRoundRect(
                buttonRect.left - glowRadius,
                buttonRect.top - glowRadius,
                buttonRect.right + glowRadius,
                buttonRect.bottom + glowRadius,
                cornerRadius + glowRadius,
                cornerRadius + glowRadius,
                glowPaint
            );
        }
        
        // Draw background
        canvas.drawPath(buttonPath, backgroundPaint);
        
        // Draw border
        canvas.drawPath(buttonPath, borderPaint);
        
        // Draw text
        super.onDraw(canvas);
    }
    
    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (this.isPressed != pressed) {
            this.isPressed = pressed;
            invalidate();
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.isEnabled != enabled) {
            this.isEnabled = enabled;
            invalidate();
        }
    }
    
    // Custom methods for dynamic color changes
    public void setCosmicColors(int color1, int color2) {
        this.cosmicColor1 = color1;
        this.cosmicColor2 = color2;
        updateGradient();
        invalidate();
    }
    
    public void setBorderColor(int color) {
        this.borderColor = color;
        borderPaint.setColor(color);
        invalidate();
    }
    
    public void setGlowColor(int color) {
        this.glowColor = color;
        glowPaint.setColor(color);
        invalidate();
    }
    
    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        updateButtonPath();
        invalidate();
    }
    
    private void updateGradient() {
        if (getWidth() > 0 && getHeight() > 0) {
            LinearGradient gradient = new LinearGradient(
                0, 0, getWidth(), getHeight(),
                cosmicColor1, cosmicColor2,
                Shader.TileMode.CLAMP
            );
            backgroundPaint.setShader(gradient);
        }
    }
    
    private void updateButtonPath() {
        if (buttonRect != null && buttonPath != null) {
            buttonPath.reset();
            buttonPath.addRoundRect(buttonRect, cornerRadius, cornerRadius, Path.Direction.CW);
        }
    }
    
    // Animation methods
    public void animatePress() {
        setPressed(true);
        postDelayed(() -> setPressed(false), 150);
    }
    
    public void pulse() {
        // Implement pulse animation
        animate().scaleX(1.1f).scaleY(1.1f).setDuration(200)
                .withEndAction(() -> animate().scaleX(1f).scaleY(1f).setDuration(200).start())
                .start();
    }
}