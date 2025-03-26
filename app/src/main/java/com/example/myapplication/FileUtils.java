// FileUtils.java
package com.example.myapplication;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FileUtils {
    private static final String TAG = "FileUtils";
    private static final Executor executor = Executors.newSingleThreadExecutor();

    // Interface for callback
    public interface TextExtractionCallback {
        void onTextExtracted(String text);
    }

    public static String getFileName(ContentResolver contentResolver, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file name", e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static void extractTextFromDocument(ContentResolver contentResolver, Uri fileUri,
                                               TextExtractionCallback callback) {
        executor.execute(() -> {
            String extractedText = "";

            try {
                InputStream inputStream = contentResolver.openInputStream(fileUri);
                if (inputStream == null) {
                    callback.onTextExtracted("");
                    return;
                }

                // Basic text extraction that works for text files and simple documents
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                extractedText = stringBuilder.toString();

                inputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "Error extracting text from document", e);
            }

            callback.onTextExtracted(extractedText);
        });
    }
}