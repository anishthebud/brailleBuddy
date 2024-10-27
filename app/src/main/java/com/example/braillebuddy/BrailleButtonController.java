package com.example.braillebuddy;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.widget.Button;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.VibrationEffect;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.concurrent.TimeUnit;


public class BrailleButtonController {
    private static final long VIBRATION_DURATION = 50; // milliseconds
    private static final long PATTERN_DELAY = 100; // delay between dots
    private static final float SCALE_FACTOR = 1.2f; // how much the button grows
    private static final int ANIMATION_DURATION = 200; // milliseconds

    private double speedMult = 1.0;

    private final Context context;
    private final Vibrator vibrator;
    private final Handler handler;
    private boolean isPlaying = false;

    public BrailleButtonController(Context context) {
        this.context = context;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.handler = new Handler(Looper.getMainLooper());
    }


    public void playPattern(String sentence) throws InterruptedException {
        if (isPlaying) {
            stop();
        }
        isPlaying = true;

        // Convert pattern to binary representation (1 for dot, 0 for no dot)
        boolean[][] braillePatterns = convertToBraillePatterns(sentence);

        // Play each character's pattern
        for (int charIndex = 0; charIndex < braillePatterns.length && isPlaying; charIndex++) {
            playCharacter(braillePatterns[charIndex]);
            Thread.sleep(1000);
        }
    }

    private void playCharacter(boolean[] dots) throws InterruptedException {
        System.out.println(dots.length);
        boolean isSpace = true;
        for (boolean dot: dots) {
            if (dot) {
                isSpace = false;
                break;
            }
        }
        for (int i = 0; i < 6 && isPlaying; i++) {
            if (isSpace) {
                vibrator.vibrate((long) (speedMult*100));
            } else {
                if (dots[i]) {
                    vibrator.vibrate((long)(speedMult*200));
                } else {
                    vibrator.vibrate((long)(speedMult*50));
                }
                Thread.sleep((long) (speedMult*350));
            }
        }
    }

    private boolean[][] convertToBraillePatterns(String text) {
        // Simple conversion - each character gets 6 dots
        boolean[][] patterns = new boolean[text.length()][6];

        for (int i = 0; i < text.length(); i++) {
            char c = Character.toLowerCase(text.charAt(i));
            switch (c) {
                case 'a':
                    patterns[i] = new boolean[]{true, false, false, false, false, false};
                    break;
                case 'b':
                    patterns[i] = new boolean[]{true, false, true, false, false, false};
                    break;
                case 'c':
                    patterns[i] = new boolean[]{true, true, false, false, false, false};
                    break;
                case 'd':
                    patterns[i] = new boolean[]{true, true, false, true, false, false};
                    break;
                case 'e':
                    patterns[i] = new boolean[]{true, false, false, true, false, false};
                    break;
                case 'f':
                    patterns[i] = new boolean[]{true, true, true, false, false, false};
                    break;
                case 'g':
                    patterns[i] = new boolean[]{true, true, true, true, false, false};
                    break;
                case 'h':
                    patterns[i] = new boolean[]{true, false, true, true, false, false};
                    break;
                case 'i':
                    patterns[i] = new boolean[]{false, true, true, false, false, false};
                    break;
                case 'j':
                    patterns[i] = new boolean[]{false, true, true, true, false, false};
                    break;
                case 'k':
                    patterns[i] = new boolean[]{true, false, false, false, true, false};
                    break;
                case 'l':
                    patterns[i] = new boolean[]{true, false, true, false, true, false};
                    break;
                case 'm':
                    patterns[i] = new boolean[]{true, true, false, false, true, false};
                    break;
                case 'n':
                    patterns[i] = new boolean[]{true, true, false, true, true, false};
                    break;
                case 'o':
                    patterns[i] = new boolean[]{true, false, false, true, true, false};
                    break;
                case 'p':
                    patterns[i] = new boolean[]{true, true, true, false, true, false};
                    break;
                case 'q':
                    patterns[i] = new boolean[]{true, true, true, true, true, false};
                    break;
                case 'r':
                    patterns[i] = new boolean[]{true, false, true, true, true, false};
                    break;
                case 's':
                    patterns[i] = new boolean[]{false, true, true, false, true, false};
                    break;
                case 't':
                    patterns[i] = new boolean[]{false, true, true, true, true, false};
                    break;
                case 'u':
                    patterns[i] = new boolean[]{true, false, false, false, true, true};
                    break;
                case 'v':
                    patterns[i] = new boolean[]{true, false, true, false, true, true};
                    break;
                case 'w':
                    patterns[i] = new boolean[]{false, true, true, true, false, true};
                    break;
                case 'x':
                    patterns[i] = new boolean[]{true, true, false, false, true, true};
                    break;
                case 'y':
                    patterns[i] = new boolean[]{true, true, false, true, true, true};
                    break;
                case 'z':
                    patterns[i] = new boolean[]{true, false, false, true, true, true};
                    break;
                case ' ':
                    patterns[i] = new boolean[]{false, false, false, false, false, false}; // Space character
                    break;
                // Add more patterns for numbers, punctuation, and special characters if needed.
                default:
                    patterns[i] = new boolean[]{false, false, false, false, false, false}; // Default empty pattern
            }
        }
        return patterns;
    }


    public void stop() {
        isPlaying = false;
        handler.removeCallbacksAndMessages(null);
        // Reset all buttons to original scale
    }

}