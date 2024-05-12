package com.example.hd_project;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class TranslationDetailsActivity extends Activity {

    private TextView tvTranslationId;
    private TextView tvSourceLanguage;
    private TextView tvDestinationLanguage;
    private EditText etSourceText;
    private EditText etTranslatedText;
    private TextView tvDateTime;
    DatabaseHelper db;
    Button deleteBt, shareButton, backToMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_details);

        // Initialize views
        tvTranslationId = findViewById(R.id.tvTranslationId);
        tvSourceLanguage = findViewById(R.id.tvSourceLanguage);
        tvDestinationLanguage = findViewById(R.id.tvDestinationLanguage);
        etSourceText = findViewById(R.id.etSourceText);
        etTranslatedText = findViewById(R.id.etTranslatedText);
        tvDateTime = findViewById(R.id.tvDateTime);

        db = new DatabaseHelper(this);
        int id = getIntent().getIntExtra("ID", -1);
        // Example data - typically you'd fetch this from a database or pass via intent
        Translation exampleTranslation = db.getTranslationById(id);

        // Fill the fields with data
        fillTranslationDetails(exampleTranslation);

        deleteBt = findViewById(R.id.deleteBt);
        shareButton = findViewById(R.id.shareButton);
        backToMenuButton = findViewById(R.id.backToMenuButton);

        deleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteTranslation(id);
                Intent new_intent = new Intent(TranslationDetailsActivity.this, HistoryActivity.class);
                startActivity(new_intent);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap screenshot = takeScreenshot();
                Uri imageUri = saveBitmap(screenshot);

                if (imageUri != null) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    sendIntent.setType("image/png");

                    Intent chooser = Intent.createChooser(sendIntent, "Share Screenshot");

                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    startActivity(chooser);

                } else {
                    Toast.makeText(TranslationDetailsActivity.this, "Failed to capture screenshot", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backToMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent new_intent = new Intent(TranslationDetailsActivity.this, HistoryActivity.class);
                startActivity(new_intent);
            }
        });
    }

    private void fillTranslationDetails(Translation translation) {
        tvTranslationId.setText("ID: " + translation.getId());
        tvSourceLanguage.setText("Source Language: " + translation.getSourceLanguage());
        tvDestinationLanguage.setText("Destination Language: " + translation.getDestinationLanguage());
        etSourceText.setText(translation.getInput());
        etTranslatedText.setText(translation.getOutput());
        tvDateTime.setText("Date and Time: " + translation.getDatetime());
    }

    private Bitmap takeScreenshot() {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private Uri saveBitmap(Bitmap bitmap) {
        File imagePath = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imagePath.mkdirs();
            File imageFile = new File(imagePath, "screenshot.png");

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            uri = FileProvider.getUriForFile(this, "com.example.hd_project.fileprovider", imageFile);
        } catch (Exception e) {
            Log.e("ShareError", "Error while saving bitmap", e);
        }
        return uri;
    }
}
