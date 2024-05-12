package com.example.hd_project;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Translations.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "translations";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SOURCE_LANGUAGE = "source_language";
    private static final String COLUMN_DESTINATION_LANGUAGE = "destination_language";
    private static final String COLUMN_INPUT = "input";
    private static final String COLUMN_OUTPUT = "output";
    private static final String COLUMN_DATETIME = "datetime";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SOURCE_LANGUAGE + " TEXT,"
                + COLUMN_DESTINATION_LANGUAGE + " TEXT,"
                + COLUMN_INPUT + " TEXT,"
                + COLUMN_OUTPUT + " TEXT,"
                + COLUMN_DATETIME + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addTranslation(Translation translation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SOURCE_LANGUAGE, translation.getSourceLanguage());
        values.put(COLUMN_DESTINATION_LANGUAGE, translation.getDestinationLanguage());
        values.put(COLUMN_INPUT, translation.getInput());
        values.put(COLUMN_OUTPUT, translation.getOutput());
        values.put(COLUMN_DATETIME, translation.getDatetime());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<Translation> getAllTranslations() {
        List<Translation> translationList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Translation translation = new Translation(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_SOURCE_LANGUAGE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DESTINATION_LANGUAGE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_INPUT)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_OUTPUT)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DATETIME)));
                translationList.add(translation);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return translationList;
    }

    public Translation getTranslationById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME, // The table to query
                new String[]{COLUMN_ID, COLUMN_SOURCE_LANGUAGE, COLUMN_DESTINATION_LANGUAGE, COLUMN_INPUT, COLUMN_OUTPUT, COLUMN_DATETIME}, // The columns to return
                COLUMN_ID + " = ?", // The columns for the WHERE clause
                new String[]{String.valueOf(id)}, // The values for the WHERE clause
                null, // group by
                null, // having
                null // order by
        );

        Translation translation = null;
        if (cursor != null && cursor.moveToFirst()) {
            translation = new Translation(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOURCE_LANGUAGE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESTINATION_LANGUAGE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INPUT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OUTPUT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATETIME))
            );
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return translation;
    }

    public int deleteTranslation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Perform the delete operation and return the number of rows affected
        int rowsAffected = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }

    public List<String> getUniqueLanguages(boolean isSource) {
        List<String> languages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String column = isSource ? COLUMN_SOURCE_LANGUAGE : COLUMN_DESTINATION_LANGUAGE;
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + column + " FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                languages.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return languages;
    }

    public List<Translation> getTranslationsByLanguages(String sourceLang, String targetLang) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "";
        List<String> selectionArgs = new ArrayList<>();

        if (sourceLang != null) {
            selection += COLUMN_SOURCE_LANGUAGE + " = ?";
            selectionArgs.add(sourceLang);
        }
        if (targetLang != null) {
            if (!selection.isEmpty()) {
                selection += " AND ";
            }
            selection += COLUMN_DESTINATION_LANGUAGE + " = ?";
            selectionArgs.add(targetLang);
        }

        if (selection.isEmpty()) {
            selection = "1"; // No filter, select all
        }

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + selection, selectionArgs.toArray(new String[0]));

        List<Translation> translationList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Translation translation = new Translation(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_SOURCE_LANGUAGE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DESTINATION_LANGUAGE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_INPUT)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_OUTPUT)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DATETIME))
                );
                translationList.add(translation);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return translationList;
    }


}
