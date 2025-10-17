package com.maassoft.colorcontrol.utils;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class AnimationUtils {
    
    public static void animateButtonPress(View view) {
        ScaleAnimation scaleDown = new ScaleAnimation(
            1.0f, 0.95f, 1.0f, 0.95f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleDown.setDuration(100);
        
        ScaleAnimation scaleUp = new ScaleAnimation(
            0.95f, 1.0f, 0.95f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleUp.setDuration(100);
        scaleUp.setStartOffset(100);
        
        scaleDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(scaleUp);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        view.startAnimation(scaleDown);
    }
    
    public static void fadeIn(View view, long duration) {
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(duration);
        animation.setInterpolator(new FastOutSlowInInterpolator());
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }
    
    public static void fadeOut(View view, long duration) {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(duration);
        animation.setInterpolator(new FastOutSlowInInterpolator());
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(animation);
    }
    
    public static void slideInFromBottom(View view, long duration) {
        TranslateAnimation animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(duration);
        animation.setInterpolator(new FastOutSlowInInterpolator());
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }
    
    public static void slideOutToBottom(View view, long duration) {
        TranslateAnimation animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f
        );
        animation.setDuration(duration);
        animation.setInterpolator(new FastOutSlowInInterpolator());
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(animation);
    }
    
    public static void pulseAnimation(View view) {
        ScaleAnimation scaleUp = new ScaleAnimation(
            1.0f, 1.1f, 1.0f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleUp.setDuration(200);
        
        ScaleAnimation scaleDown = new ScaleAnimation(
            1.1f, 1.0f, 1.1f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleDown.setDuration(200);
        scaleDown.setStartOffset(200);
        
        scaleUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(scaleDown);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        view.startAnimation(scaleUp);
    }
    
    public static void shakeAnimation(View view) {
        TranslateAnimation shake = new TranslateAnimation(
            -10, 10, 0, 0
        );
        shake.setDuration(50);
        shake.setRepeatCount(5);
        shake.setRepeatMode(Animation.REVERSE);
        view.startAnimation(shake);
    }
    
    public static void cosmicGlowAnimation(View view) {
        AnimationSet animationSet = new AnimationSet(true);
        
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.3f, 0.8f);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        
        ScaleAnimation scaleAnimation = new ScaleAnimation(
            1.0f, 1.05f, 1.0f, 1.05f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(1500);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);
        animationSet.setInterpolator(new FastOutSlowInInterpolator());
        
        view.startAnimation(animationSet);
    }
}