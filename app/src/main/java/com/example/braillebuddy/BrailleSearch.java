package com.example.braillebuddy;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.foundation.content.MediaType;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import android.os.Vibrator;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrailleSearch extends AppCompatActivity {
    private boolean isPlaying = false;
    private static final String OPENAI_API_KEY = "sk-a_yKKSH8BZYSMSULkdmUDLGJ-h805z0DfA1v6hPjCPT3BlbkFJ0CyEijiIzISocoPkuhiLWnnGyxNtzwpm0AIjGPGH4A";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;
    private String gptResponse = ""; // String to store GPT-4's response
    private boolean[] brailleDots = new boolean[6];
    private TextView outputTextView;
    private TextView submittedCharactersTextView;
    private StringBuilder submittedCharacters = new StringBuilder();
    private static final int MIN_DISTANCE = 150;
    float x1, x2, y1, y2;
    Intent anotherActivity;

    private Map<String, Character> brailleToCharMap;
    private Vibrator vibrator;
    private BrailleButtonController controller;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_search);

        // Initialize OkHttpClient for API calls
        client = new OkHttpClient();

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        controller = new BrailleButtonController(this);

        initBrailleToCharMap();
        setupButtonListeners();

        outputTextView = findViewById(R.id.outputTextView);
        submittedCharactersTextView = findViewById(R.id.submittedCharactersTextView);

        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> {
            try {
                submitCharacter();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Button backspaceButton = findViewById(R.id.backspaceButton);
        backspaceButton.setOnClickListener(v -> removeCharacter());

        // Modify the search button (previously speak button)
        Button searchButton = findViewById(R.id.speakButton);
        searchButton.setText("Search with GPT-4");
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = submittedCharacters.toString();
                System.out.println(searchQuery);
                if (!searchQuery.isEmpty()) {
                    sendToOpenAIAPI(searchQuery);
                } else {
                    Toast.makeText(BrailleSearch.this, "Please enter text to search", Toast.LENGTH_SHORT).show();
                }
                System.out.println(gptResponse);
            }
        });
    }

    private void sendToOpenAIAPI(String query) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("model", "gpt-4o");
            requestBody.put("max_tokens", 100);
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "user").put("content", query));
            messages.put(new JSONObject().put("role", "system").put("content", "You are acting like a search engine that returns accurate info based on what the user wants. In a concise manner."));
            requestBody.put("messages", messages);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(BrailleSearch.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    System.out.println("Response Data:");
                    System.out.println(responseData);
                    JSONObject jsonResponse = new JSONObject(responseData);
                    gptResponse = jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    // Clear the submitted characters after successful search
                    runOnUiThread(() -> {
                        Log.d("GPT-4 Response", gptResponse);
                        System.out.println(gptResponse);
                        outputTextView.setText(gptResponse); // Update UI with response
                        executorService.submit(() -> {
                            submittedCharacters.setLength(0);
                            if (controller != null) {
                                controller.stop();
                            }
                            try {
                                submittedCharacters.setLength(0);
                                submittedCharactersTextView.setText(submittedCharacters);
                                controller.playPattern(gptResponse);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(BrailleSearch.this, "Error processing response", Toast.LENGTH_LONG).show());
                }
            }
        });
    }
    protected void onDestroy() {

        super.onDestroy();
        if (controller != null) {
            controller.stop();
        }

        if (executorService != null) {
            executorService.shutdownNow(); // Shutdown the executor service
        }
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
                if (controller != null) {
                    controller.stop();
                }
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
        outputTextView.setText("");
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
        brailleToCharMap.put("000000", ' ');
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
        if (submittedCharacters.length() - 1 >= 0) {
            submittedCharacters.deleteCharAt(submittedCharacters.length() - 1);
            vibrator.vibrate(300);
        } else {
            vibrator.vibrate(500);
        }
        String outputText = submittedCharacters.toString();
        submittedCharactersTextView.setText(outputText);
    }

    private void resetButtons() {
        for (int i = 0; i < brailleDots.length; i++) {
            brailleDots[i] = false; // Reset dot states
            String check = brailleDots[i]  ? "true" : "false";
            Button button = findViewById(getResources().getIdentifier("button_dot" + (i + 1), "id", getPackageName()));
            updateButtonAppearance(button, false); // Update button appearance
        }
        outputTextView.setText(""); // Clear output text
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
                    if (controller != null) {
                        controller.stop();
                    }
                } else if (deltaX < MIN_DISTANCE*-1) {
                    Log.d("SWIPE", "left swipe");
                    anotherActivity = new Intent(this, MainActivity.class);
                    startActivity(anotherActivity);
                    if (controller != null) {
                        controller.stop();
                    }
                }
        }
        return super.onTouchEvent(event);
    }

}