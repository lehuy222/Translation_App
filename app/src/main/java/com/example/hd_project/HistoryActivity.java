package com.example.hd_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    DatabaseHelper db;
    ArrayAdapter<Translation> adapter;
    Spinner sourceLanguageSpinner, targetLanguageSpinner;
    Button searchButton, backToMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = new DatabaseHelper(this);
        ListView historyListView = findViewById(R.id.historyListView);
        sourceLanguageSpinner = findViewById(R.id.sourceLanguageSpinner);
        targetLanguageSpinner = findViewById(R.id.targetLanguageSpinner);

        searchButton = findViewById(R.id.searchButton);
        backToMenuButton = findViewById(R.id.backToMenuButton);

        initSpinner(sourceLanguageSpinner, db.getUniqueLanguages(true)); // true for source languages
        initSpinner(targetLanguageSpinner, db.getUniqueLanguages(false)); // false for target languages

        // Generate 10 random translation history entries
        List<Translation> translations = db.getAllTranslations();

        // Set up the ArrayAdapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, translations);
        historyListView.setAdapter(adapter);

        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(HistoryActivity.this, TranslationDetailsActivity.class);
                // Send item name or ID to the detail view
                intent.putExtra("ID", translations.get(position).getId());
                startActivity(intent);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterTranslations();
            }
        });

        backToMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent new_intent = new Intent(HistoryActivity.this, MainActivity2.class);
                startActivity(new_intent);
            }
        });
    }

    private void initSpinner(Spinner spinner, List<String> items) {
        items.add(0, "None"); // Add "None" as the first item
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(spinnerAdapter);
    }

    private void filterTranslations() {
        String sourceLang = sourceLanguageSpinner.getSelectedItem().toString();
        String targetLang = targetLanguageSpinner.getSelectedItem().toString();
        List<Translation> filteredTranslations = db.getTranslationsByLanguages(
                sourceLang.equals("None") ? null : sourceLang,
                targetLang.equals("None") ? null : targetLang
        );
        adapter.clear();
        adapter.addAll(filteredTranslations);
        adapter.notifyDataSetChanged();
    }
}