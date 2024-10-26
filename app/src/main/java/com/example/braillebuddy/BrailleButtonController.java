package com.example.braillebuddy;

import android.content.Context;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;

public class BrailleButtonController {
    private static final long VIBRATION_DURATION = 50; // milliseconds
    private static final long PATTERN_DELAY = 500; // delay between dots
    private static final float SCALE_FACTOR = 1.2f; // how much the button grows
    private static final int ANIMATION_DURATION = 200; // milliseconds

    private final Context context;
    private final Button[] buttons;
    private final Vibrator vibrator;
    private final Handler handler;
    private boolean isPlaying = false;

    public BrailleButtonController(Context context, Button... buttons) {
        this.context = context;
        this.buttons = buttons;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void playPattern(String pattern) {
        if (isPlaying) {
            stop();
        }
        isPlaying = true;

        // Convert pattern to binary representation (1 for dot, 0 for no dot)
        boolean[][] braillePatterns = convertToBraillePatterns(pattern);

        // Play each character's pattern
        for (int charIndex = 0; charIndex < braillePatterns.length && isPlaying; charIndex++) {
            final int finalCharIndex = charIndex;
            handler.postDelayed(() -> {
                if (isPlaying) {
                    playCharacter(braillePatterns[finalCharIndex]);
                }
            }, charIndex * (PATTERN_DELAY * 8)); // Allow time for each character
        }
    }

    private void playCharacter(boolean[] dots) {
        for (int i = 0; i < dots.length && i < buttons.length && isPlaying; i++) {
            if (dots[i]) {
                final int buttonIndex = i;
                handler.postDelayed(() -> {
                    if (isPlaying) {
                        animateAndVibrateButton(buttons[buttonIndex]);
                    }
                }, i * PATTERN_DELAY);
            }
        }
    }

    private void animateAndVibrateButton(Button button) {
        // Scale animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, SCALE_FACTOR);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, SCALE_FACTOR);

        // Create animation set
        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleX, scaleY);
        scaleUp.setDuration(ANIMATION_DURATION);
        scaleUp.setInterpolator(new AccelerateDecelerateInterpolator());

        // Scale down
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", SCALE_FACTOR, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", SCALE_FACTOR, 1f);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);
        scaleDown.setDuration(ANIMATION_DURATION);
        scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());

        // Play animations in sequence
        AnimatorSet fullSequence = new AnimatorSet();
        fullSequence.playSequentially(scaleUp, scaleDown);
        fullSequence.start();

        int durationFactor = 1;

        // Vibrate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION*durationFactor, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(VIBRATION_DURATION);
        }
    }

    private boolean[][] convertToBraillePatterns(String text) {
        // Simple conversion - each character gets 6 dots
        boolean[][] patterns = new boolean[text.length()][6];

        for (int i = 0; i < text.length(); i++) {
            char c = Character.toLowerCase(text.charAt(i));
            // Example pattern - you can expand this based on actual Braille patterns
            switch (c) {
                case 'a':
                    patterns[i] = new boolean[]{true, false, false, false, false, false};
                    break;
                case 'b':
                    patterns[i] = new boolean[]{true, false, true, false, false, false};
                    break;
                // Add more character patterns as needed
                default:
                    patterns[i] = new boolean[]{false, false, false, false, false, false};
            }
        }
        return patterns;
    }

    public void stop() {
        isPlaying = false;
        handler.removeCallbacksAndMessages(null);
        // Reset all buttons to original scale
        for (Button button : buttons) {
            button.setScaleX(1f);
            button.setScaleY(1f);
        }
    }
}