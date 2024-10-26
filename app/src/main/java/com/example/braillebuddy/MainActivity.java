package com.example.braillebuddy;  // Replace with your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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
        EditText editView;

        private static final int RECOGNIZER_RESULT = 1;


        ArrayList<String> matches;

        private ExecutorService executorService = Executors.newSingleThreadExecutor();



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);


            speechButton = findViewById(R.id.imageView);
            editView =  findViewById(R.id.editText);


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
                editView.setText(matches.get(0).toString());

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



}