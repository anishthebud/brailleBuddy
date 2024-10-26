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

        // Initialize the controller
        controller = new BrailleButtonController(this);

        // Example: Add a button to start the pattern
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            try {
                controller.playPattern("hello i am anish budida");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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