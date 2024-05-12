package com.example.hd_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner sourceLanguageSpinner, destinationLanguageSpinner;
    private EditText inputText, imagePathEditText;
    private TextView translationOutput;
    private Button translateButton, historyFavoritesButton, extractTextButton, backToMenuButton;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Replace "your_layout_name" with the actual layout file name

        // Initialize the Spinners
        sourceLanguageSpinner = findViewById(R.id.sourceLanguageSpinner);
        destinationLanguageSpinner = findViewById(R.id.targetLanguageSpinner);

        // Initialize the EditText and TextView for input and output
        inputText = findViewById(R.id.inputText);
        translationOutput = findViewById(R.id.translationOutput);

        // Initialize the Buttons
        translateButton = findViewById(R.id.translateButton);
        historyFavoritesButton = findViewById(R.id.historyFavoritesButton);

        db = new DatabaseHelper(this);

        // Set up the spinner with dummy data
        setUpSpinner(sourceLanguageSpinner, new String[]{"English", "Spanish", "French", "Hindi", "Chinese"});
        setUpSpinner(destinationLanguageSpinner, new String[]{"Spanish", "English", "French", "Hindi", "Chinese"});

        imagePathEditText = findViewById(R.id.imagePath);
        extractTextButton = findViewById(R.id.extractTextButton);
        backToMenuButton = findViewById(R.id.backToMenuButton);

        extractTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadAndSendImage(imagePathEditText.getText().toString());
            }
        });

        // Set up the translate button click listener
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performTranslation();
            }
        });

        // Optionally, set up the history/favorites button
        historyFavoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sourceLanguage = sourceLanguageSpinner.getSelectedItem().toString();
                String destinationLanguage = destinationLanguageSpinner.getSelectedItem().toString();
                String input = inputText.getText().toString();

                // Simulate translation (replace this part with your actual translation logic)
                String output = translationOutput.getText().toString();

                // Get current datetime
                String currentDateTime = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                db.addTranslation(new Translation(sourceLanguage, destinationLanguage, input, output, currentDateTime));
            }
        });

        backToMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent new_intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(new_intent);
            }
        });
    }

    private void setUpSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
    }

    private void performTranslation() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // API URL
                    URL url = new URL("http://10.0.2.2:5000/translate");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    // Create JSON body
                    JSONObject jsonInput = new JSONObject();
                    jsonInput.put("source_language", sourceLanguageSpinner.getSelectedItem().toString());
                    jsonInput.put("des_language", destinationLanguageSpinner.getSelectedItem().toString());
                    jsonInput.put("input_data", inputText.getText().toString());

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonInput.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        final String output = new JSONObject(response.toString()).getString("message");

                        // Update the UI with the response
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                translationOutput.setText(output);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void downloadAndSendImage(String imageUrl) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);

                    // Now send the Bitmap to your OCR API
                    sendImageToApi(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void sendImageToApi(Bitmap bitmap) {
        Thread thread = new Thread(() -> {
            try {
                // Set up the connection
                URL url = new URL("http://10.0.2.2:5001/ocr");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Define boundary for multipart/form-data
                String boundary = "WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                // Compress bitmap to ByteArrayOutputStream
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageData = byteArrayOutputStream.toByteArray();

                // Write image data as multipart/form-data
                try (DataOutputStream request = new DataOutputStream(conn.getOutputStream())) {
                    request.writeBytes("--" + boundary + "\r\n");
                    request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n");
                    request.writeBytes("Content-Type: image/jpeg\r\n");
                    request.writeBytes("\r\n");
                    request.write(imageData);
                    request.writeBytes("\r\n");
                    request.writeBytes("--" + boundary + "--\r\n");
                    request.flush();
                }

                // Check response code
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Parse the JSON response to get the OCR result
                    String jsonString = result.toString();
                    JSONObject jsonObject = new JSONObject(jsonString);
                    String ocrResult = jsonObject.getString("ocr_result");

                    // Clean up the OCR result string
                    String cleanedResult = ocrResult.replace("\\n", " ")       // Replace \n with space
                            .replace("\\u2018", "'")   // Replace Unicode single quote with apostrophe
                            .replaceAll("\\\\u[0-9a-fA-F]{4}", "");  // Regex to remove any unicode characters

                    // Update the EditText in UI thread
                    runOnUiThread(() -> {
                        inputText.setText(cleanedResult);
                    });
                } else {
                    System.out.println("Response Code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }


}

