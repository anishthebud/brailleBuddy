package com.example.braillebuddy;  // Replace with your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;


    public class MainActivity extends AppCompatActivity {
        private BrailleButtonController controller;
        ImageView speechButton;
        TextView textView;

        private static final int RECOGNIZER_RESULT = 1;
        private static final int MIN_DISTANCE = 150;
        float x1, x2, y1, y2;
        Intent anotherActivity;


        ArrayList<String> matches;

        private ExecutorService executorService = Executors.newSingleThreadExecutor();



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);


            speechButton = findViewById(R.id.imageView);
            textView =  findViewById(R.id.editText);


            speechButton.setOnClickListener(v -> {
                if (controller != null) {
                    controller.stop();
                }
                Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to Text");
                startActivityForResult(speechIntent, RECOGNIZER_RESULT);

            });

        }



        @Override

        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

            if (requestCode == RECOGNIZER_RESULT && resultCode == RESULT_OK) {

                matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                textView.setText(matches.get(0).toString());

                controller = new BrailleButtonController(this);

                executorService.submit(() -> {
                    try {
                        controller.playPattern(matches.get(0));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

            }
            super.onActivityResult(requestCode, resultCode, data);
        }


        @Override
        protected void onDestroy() {

            super.onDestroy();
            if (controller != null) {
                controller.stop();
            }

            if (executorService != null) {
                executorService.shutdownNow(); // Shutdown the executor service
            }
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    y1 = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    x2 = event.getX();
                    y2 = event.getY();
                    float deltaX = x2 - x1;
                    float deltaY = y2 - y1;
                    if (deltaX > MIN_DISTANCE) {
                        Log.d("SWIPE", "right swipe");
                        anotherActivity = new Intent(this, BrailleMappingActivity.class);
                        startActivity(anotherActivity);
                        try {
                            controller.playPattern("K");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    } else if (deltaX < MIN_DISTANCE*-1) {
                        Log.d("SWIPE", "left swipe");
                        anotherActivity = new Intent(this, MainActivity.class);
                        startActivity(anotherActivity);
                        try {
                            controller.playPattern("C");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (deltaY > MIN_DISTANCE) {
                        Log.d("SWIPE", "down swipe");
                        try {
                            controller.playPattern(matches.get(0));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            }
            return super.onTouchEvent(event);
        }
}