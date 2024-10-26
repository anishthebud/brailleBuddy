package com.example.braillebuddy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;

public class BrailletoSpeech extends AppCompatActivity {
    private static final int ROWS = 3;
    private static final int COLS = 2;
    private TextView outputText;
    private boolean[][] dotMatrix = new boolean[COLS][ROWS];
    private StringBuilder brailleText = new StringBuilder();
    private int activeFingers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a full-screen touch area and output text view
        RelativeLayout layout = new RelativeLayout(this);
        TouchView touchView = new TouchView(this);
        outputText = new TextView(this);

        // Configure the output text view
        outputText.setTextSize(24);
        outputText.setPadding(20, 20, 20, 20);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        // Add views to layout
        layout.addView(touchView);
        layout.addView(outputText, params);
        setContentView(layout);
    }

    private class TouchView extends View {
        private float cellWidth;
        private float cellHeight;
        private Paint paint;

        public TouchView(Context context) {
            super(context);
            paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            cellWidth = w / (float) COLS;
            cellHeight = (h * 0.7f) / (float) ROWS; // Use 70% of height for touch area
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Draw grid lines
            paint.setColor(Color.GRAY);
            paint.setStrokeWidth(2);
            for (int i = 1; i < COLS; i++) {
                canvas.drawLine(i * cellWidth, 0, i * cellWidth, ROWS * cellHeight, paint);
            }
            for (int i = 1; i < ROWS; i++) {
                canvas.drawLine(0, i * cellHeight, COLS * cellWidth, i * cellHeight, paint);
            }

            // Draw active dots
            paint.setColor(Color.BLUE);
            float radius = Math.min(cellWidth, cellHeight) * 0.2f;
            for (int i = 0; i < COLS; i++) {
                for (int j = 0; j < ROWS; j++) {
                    if (dotMatrix[i][j]) {
                        canvas.drawCircle(
                                i * cellWidth + cellWidth/2,
                                j * cellHeight + cellHeight/2,
                                radius,
                                paint
                        );
                    }
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int pointerCount = event.getPointerCount();

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_MOVE:
                    // Clear the matrix
                    for (int i = 0; i < COLS; i++) {
                        Arrays.fill(dotMatrix[i], false);
                    }

                    // Mark active touch points
                    for (int i = 0; i < pointerCount; i++) {
                        int col = (int) (event.getX(i) / cellWidth);
                        int row = (int) (event.getY(i) / cellHeight);

                        if (col >= 0 && col < COLS && row >= 0 && row < ROWS) {
                            dotMatrix[col][row] = true;
                        }
                    }
                    activeFingers = pointerCount;
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    if (event.getPointerCount() <= 1) {
                        // Convert current dot pattern to Braille character
                        String brailleChar = convertToBraille();
                        brailleText.append(brailleChar);
                        outputText.setText(brailleText.toString());

                        // Clear the matrix
                        for (int i = 0; i < COLS; i++) {
                            Arrays.fill(dotMatrix[i], false);
                        }
                        activeFingers = 0;
                    }
                    break;
            }

            invalidate();
            return true;
        }

        private String convertToBraille() {
            if (activeFingers == 0) return " ";

            // Convert dot matrix to Braille pattern
            if (!dotMatrix[0][0] && dotMatrix[0][1] && dotMatrix[0][2] && dotMatrix[1][0] && dotMatrix[1][1] && dotMatrix[1][2]) return "a";
            if (!dotMatrix[0][0] && !dotMatrix[0][1] && dotMatrix[0][2] && dotMatrix[1][0] && dotMatrix[1][1] && dotMatrix[1][2]) return "b";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && dotMatrix[0][2] && !dotMatrix[1][0] && dotMatrix[1][1] && dotMatrix[1][2]) return "c";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && dotMatrix[0][2] && !dotMatrix[1][0] && !dotMatrix[1][1] && dotMatrix[1][2]) return "d";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && dotMatrix[0][2] && dotMatrix[1][0] && !dotMatrix[1][1] && dotMatrix[1][2]) return "e";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && dotMatrix[0][2] && !dotMatrix[1][0] && dotMatrix[1][1] && !dotMatrix[1][2]) return "f";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && dotMatrix[0][2] && !dotMatrix[1][0] && !dotMatrix[1][1] && !dotMatrix[1][2]) return "g";
            if (!dotMatrix[0][0] && !dotMatrix[0][1] && dotMatrix[0][2] && !dotMatrix[1][0] && dotMatrix[1][1] && dotMatrix[1][2]) return "h";
            if (dotMatrix[0][0] && !dotMatrix[0][1] && dotMatrix[0][2] && !dotMatrix[1][0] && dotMatrix[1][1] && dotMatrix[1][2]) return "i";
            if (dotMatrix[0][0] && !dotMatrix[0][1] && dotMatrix[0][2] && !dotMatrix[1][0] && !dotMatrix[1][1] && dotMatrix[1][2]) return "j";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && !dotMatrix[0][2] && dotMatrix[1][0] && dotMatrix[1][1] && dotMatrix[1][2]) return "k";
            if (!dotMatrix[0][0] && !dotMatrix[0][1] && !dotMatrix[0][2] && dotMatrix[1][0] && dotMatrix[1][1] && dotMatrix[1][2]) return "l";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && !dotMatrix[0][2] && !dotMatrix[1][0] && dotMatrix[1][1] && dotMatrix[1][2]) return "m";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && !dotMatrix[0][2] && !dotMatrix[1][0] && !dotMatrix[1][1] && dotMatrix[1][2]) return "n";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && !dotMatrix[0][2] && dotMatrix[1][0] && !dotMatrix[1][1] && dotMatrix[1][2]) return "o";
            if (!dotMatrix[0][0] && !dotMatrix[0][1] && !dotMatrix[0][2] && !dotMatrix[1][0] && dotMatrix[1][1] && dotMatrix[1][2]) return "p";
            if (!dotMatrix[0][0] && !dotMatrix[0][1] && !dotMatrix[0][2] && !dotMatrix[1][0] && !dotMatrix[1][1] && dotMatrix[1][2]) return "q";
            if (!dotMatrix[0][0] && !dotMatrix[0][1] && !dotMatrix[0][2] && dotMatrix[1][0] && !dotMatrix[1][1] && dotMatrix[1][2]) return "r";
            if (dotMatrix[0][0] && !dotMatrix[0][1] && !dotMatrix[0][2] && !dotMatrix[1][0] && dotMatrix[1][1] && dotMatrix[1][2]) return "s";
            if (dotMatrix[0][0] && !dotMatrix[0][1] && !dotMatrix[0][2] && !dotMatrix[1][0] && !dotMatrix[1][1] && dotMatrix[1][2]) return "t";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && !dotMatrix[0][2] && dotMatrix[1][0] && dotMatrix[1][1] && !dotMatrix[1][2]) return "u";
            if (!dotMatrix[0][0] && !dotMatrix[0][1] && !dotMatrix[0][2] && dotMatrix[1][0] && dotMatrix[1][1] && !dotMatrix[1][2]) return "v";
            if (dotMatrix[0][0] && !dotMatrix[0][1] && dotMatrix[0][2] && !dotMatrix[1][0] && !dotMatrix[1][1] && !dotMatrix[1][2]) return "w";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && !dotMatrix[0][2] && !dotMatrix[1][0] && dotMatrix[1][1] && !dotMatrix[1][2]) return "x";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && !dotMatrix[0][2] && !dotMatrix[1][0] && !dotMatrix[1][1] && !dotMatrix[1][2]) return "y";
            if (!dotMatrix[0][0] && dotMatrix[0][1] && !dotMatrix[0][2] && dotMatrix[1][0] && !dotMatrix[1][1] && !dotMatrix[1][2]) return "z";

            return "?"; // Unknown pattern
        }
    }
}