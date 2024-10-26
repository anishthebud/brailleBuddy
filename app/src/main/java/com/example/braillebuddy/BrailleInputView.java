package com.example.braillebuddy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;

public class BrailleInputView extends View {
    private static final int MAX_FINGERS = 6;
    private SparseArray<PointF> activeFingers;
    private boolean[] dots;
    private OnBrailleInputListener listener;

    // Interface for listening to Braille input events
    public interface OnBrailleInputListener {
        void onCharacterInput(char character);
    }

    public BrailleInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activeFingers = new SparseArray<>();
        dots = new boolean[6];
        setBackgroundColor(Color.LTGRAY);
    }

    public void setOnBrailleInputListener(OnBrailleInputListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (activeFingers.size() < MAX_FINGERS) {
                    PointF point = new PointF(event.getX(pointerIndex), event.getY(pointerIndex));
                    activeFingers.put(pointerId, point);
                    updateDotPositions();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (activeFingers.indexOfKey(pointerId) >= 0) {
                    int dotIndex = getDotIndex(activeFingers.get(pointerId));
                    if (dotIndex >= 0) {
                        dots[dotIndex] = true;
                        checkAndSendCharacter();
                    }
                    activeFingers.remove(pointerId);
                }

                // If all fingers are removed, send space
                if (activeFingers.size() == 0) {
                    if (listener != null) {
                        listener.onCharacterInput(' ');
                    }
                    clearDots();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int id = event.getPointerId(i);
                    if (activeFingers.indexOfKey(id) >= 0) {
                        activeFingers.get(id).set(event.getX(i), event.getY(i));
                    }
                }
                updateDotPositions();
                break;
        }

        invalidate();
        return true;
    }

    private void updateDotPositions() {
        // Divide view into 6 regions (2x3 grid)
        float cellWidth = getWidth() / 2f;
        float cellHeight = getHeight() / 3f;

        for (int i = 0; i < activeFingers.size(); i++) {
            PointF point = activeFingers.valueAt(i);
            int col = (int) (point.x / cellWidth);
            int row = (int) (point.y / cellHeight);
            int dotIndex = row * 2 + col;

            if (dotIndex >= 0 && dotIndex < 6) {
                dots[dotIndex] = false; // Reset dot when finger is active
            }
        }
    }

    private int getDotIndex(PointF point) {
        float cellWidth = getWidth() / 2f;
        float cellHeight = getHeight() / 3f;
        int col = (int) (point.x / cellWidth);
        int row = (int) (point.y / cellHeight);
        return (col >= 0 && col < 2 && row >= 0 && row < 3) ? row * 2 + col : -1;
    }

    private void checkAndSendCharacter() {
        if (listener != null) {
            char character = dotsToCharacter();
            if (character != 0) {
                listener.onCharacterInput(character);
            }
        }
    }

    private char dotsToCharacter() {
        // Basic Braille to character mapping
        // Dot positions: 0 1
        //               2 3
        //               4 5
        if (dots[0] && !dots[1] && !dots[2] && !dots[3] && !dots[4] && !dots[5]) return 'A';
        if (dots[0] && dots[2] && !dots[1] && !dots[3] && !dots[4] && !dots[5]) return 'B';
        if (dots[0] && dots[1] && !dots[2] && !dots[3] && !dots[4] && !dots[5]) return 'C';
        // Add more character mappings here
        return 0;
    }

    private void clearDots() {
        Arrays.fill(dots, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cellWidth = getWidth() / 2f;
        float cellHeight = getHeight() / 3f;
        float radius = Math.min(cellWidth, cellHeight) * 0.2f;

        // Draw dots
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        for (int i = 0; i < 6; i++) {
            int row = i / 2;
            int col = i % 2;
            float cx = col * cellWidth + cellWidth / 2;
            float cy = row * cellHeight + cellHeight / 2;

            // Draw active fingers in blue, released dots in green
            if (isDotOccupied(i)) {
                paint.setColor(Color.BLUE);
            } else if (dots[i]) {
                paint.setColor(Color.GREEN);
            } else {
                paint.setColor(Color.GRAY);
            }

            canvas.drawCircle(cx, cy, radius, paint);
        }
    }

    private boolean isDotOccupied(int dotIndex) {
        float cellWidth = getWidth() / 2f;
        float cellHeight = getHeight() / 3f;

        for (int i = 0; i < activeFingers.size(); i++) {
            PointF point = activeFingers.valueAt(i);
            int col = (int) (point.x / cellWidth);
            int row = (int) (point.y / cellHeight);
            if (row * 2 + col == dotIndex) {
                return true;
            }
        }
        return false;
    }
}