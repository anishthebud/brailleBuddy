package com.example.braillebuddy;


import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.os.Vibrator;

public class BrailleMappingActivity extends AppCompatActivity {
    TextToSpeech textToSpeech;
    private boolean[] brailleDots = new boolean[6]; // Stores dot states (on/off)
    private TextView outputTextView; // Display the character output
    private TextView submittedCharactersTextView; // Display submitted characters
    private StringBuilder submittedCharacters = new StringBuilder(); // Store submitted characters

    // Map for Braille dot patterns to characters
    private Map<String, Character> brailleToCharMap;
    private Vibrator vibrator;
    private BrailleButtonController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_keyboard);

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        controller = new BrailleButtonController(this);

        // Initialize Braille map
        initBrailleToCharMap();

        // Set up button listeners to toggle dot states
        setupButtonListeners();

        // Initialize the output TextView to show characters
        outputTextView = findViewById(R.id.outputTextView);
        submittedCharactersTextView = findViewById(R.id.submittedCharactersTextView);

        // Set up submit button
        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> {
            try {
                submitCharacter();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // Set up backspace button
        Button backspaceButton = findViewById(R.id.backspaceButton);
        backspaceButton.setOnClickListener(v -> {
            removeCharacter();
        });

        Button speakButton = findViewById(R.id.speakButton);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(submittedCharacters.toString().toString(),TextToSpeech.QUEUE_FLUSH,null);
            }
        });
    }

    private void setupButtonListeners() {
        Button[] buttons = new Button[] {
                findViewById(R.id.button_dot1),
                findViewById(R.id.button_dot2),
                findViewById(R.id.button_dot3),
                findViewById(R.id.button_dot4),
                findViewById(R.id.button_dot5),
                findViewById(R.id.button_dot6)
        };

        for (int i = 0; i < buttons.length; i++) {
            final int index = i;
            buttons[i].setOnClickListener(v -> {
                // Toggle dot state
                brailleDots[index] = !brailleDots[index];
                // Update button appearance based on new state
                updateButtonAppearance(buttons[index], brailleDots[index]);
                // Display character based on current button states
                displayBrailleCharacter();
            });
        }
    }

    private void updateButtonAppearance(Button button, boolean isOn) {
        int color = isOn ? android.R.color.holo_blue_light : android.R.color.darker_gray;
        button.setBackgroundTintList(getResources().getColorStateList(color));
    }

    private void displayBrailleCharacter() {
        Character brailleChar = getCharacterFromDots(brailleDots);
        if (brailleChar != null) {
            outputTextView.setText(String.valueOf(brailleChar));
        } else {
            outputTextView.setText("No match");
        }
    }

    private Character getCharacterFromDots(boolean[] dots) {
        // Convert boolean array to a string key for matching
        StringBuilder keyBuilder = new StringBuilder();
        for (boolean dot : dots) {
            keyBuilder.append(dot ? '1' : '0');
        }
        String key = keyBuilder.toString();
        return brailleToCharMap.getOrDefault(key, null);
    }

    private void initBrailleToCharMap() {
        brailleToCharMap = new HashMap<>();
        brailleToCharMap.put("100000", 'a');
        brailleToCharMap.put("101000", 'b');
        brailleToCharMap.put("110000", 'c');
        brailleToCharMap.put("110100", 'd');
        brailleToCharMap.put("100100", 'e');
        brailleToCharMap.put("111000", 'f');
        brailleToCharMap.put("111100", 'g');
        brailleToCharMap.put("101100", 'h');
        brailleToCharMap.put("011000", 'i');
        brailleToCharMap.put("011100", 'j');
        brailleToCharMap.put("100010", 'k');
        brailleToCharMap.put("101010", 'l');
        brailleToCharMap.put("110010", 'm');
        brailleToCharMap.put("110110", 'n');
        brailleToCharMap.put("100110", 'o');
        brailleToCharMap.put("111010", 'p');
        brailleToCharMap.put("111110", 'q');
        brailleToCharMap.put("101110", 'r');
        brailleToCharMap.put("011010", 's');
        brailleToCharMap.put("011110", 't');
        brailleToCharMap.put("100011", 'u');
        brailleToCharMap.put("101011", 'v');
        brailleToCharMap.put("011101", 'w');
        brailleToCharMap.put("110011", 'x');
        brailleToCharMap.put("110111", 'y');
        brailleToCharMap.put("100111", 'z');
        brailleToCharMap.put("111111", ' ');
        // Add more mappings as needed
    }

    private void submitCharacter() throws InterruptedException {
        Character currentChar = getCharacterFromDots(brailleDots);
        if (currentChar != null) {
            submittedCharacters.append(currentChar);
            submittedCharactersTextView.setText(submittedCharacters.toString());
            controller.playPattern(currentChar.toString());
            vibrator.vibrate(10);
            resetButtons();
        } else {
            resetButtons();
            vibrator.vibrate(1000);
        }
    }

    private void removeCharacter() {
        vibrator.vibrate(500);
        submittedCharacters.deleteCharAt(submittedCharacters.length() - 1);
        submittedCharactersTextView.setText(submittedCharacters.toString());
    }

    private void resetButtons() {
        Log.d("CALL", "reset butttons");
        for (int i = 0; i < brailleDots.length; i++) {
            brailleDots[i] = false; // Reset dot states
            String check = brailleDots[i]  ? "true" : "false";
            Log.d("STATUS OF DOT", check);
            Button button = findViewById(getResources().getIdentifier("button_dot" + (i + 1), "id", getPackageName()));
            updateButtonAppearance(button, false); // Update button appearance
        }
        outputTextView.setText(""); // Clear output text
    }
}