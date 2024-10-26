package com.example.braillebuddy;  // Replace with your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private BrailleButtonController controller;
    ImageView speechButton;
    EditText editView;

    private static final int RECOGNIZER_RESULT = 1;

    ArrayList<String> matches;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speechButton = findViewById(R.id.imageView);
        editView =  findViewById(R.id.editText);

        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to Text");
                startActivityForResult(speechIntent, RECOGNIZER_RESULT);
            }
        });


        // Initialize the buttons
        Button[] buttons = new Button[1];
        buttons[0] = findViewById(R.id.button1);
        /**
        buttons[1] = findViewById(R.id.button2);
        buttons[2] = findViewById(R.id.button3);
        buttons[3] = findViewById(R.id.button4);
        buttons[4] = findViewById(R.id.button5);
        buttons[5] = findViewById(R.id.button6);
         **/

        // Initialize the controller
        controller = new BrailleButtonController(this, buttons);

        // Example: Add a button to start the pattern
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            controller.playPattern("ab");
        });

    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == RECOGNIZER_RESULT && resultCode == RESULT_OK) {

            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            editView.setText(matches.get(0).toString());

        }
        super.onActivityResult(requestCode, resultCode, data);


    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (controller != null) {
            controller.stop();
        }
    }



}