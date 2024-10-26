package com.example.braillebuddy;  // Replace with your actual package name

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private BrailleButtonController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.stop();
        }
    }
}