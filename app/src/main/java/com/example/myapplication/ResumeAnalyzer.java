package com.example.myapplication;

import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ResumeAnalyzer {
    private static final String TAG = "ResumeAnalyzer";
    private static final String API_KEY = "AIzaSyAzmkkBxUZ86V3RZEt7sra_Pyi-rn6BqOE"; // Replace with your Gemini API key
    private final Executor executor = MoreExecutors.directExecutor(); // Use MoreExecutors.directExecutor() for simplicity

    // Interface for callback
    public interface AnalysisCallback {
        void onAnalysisComplete(String analysisResult);
    }

    public void analyzeResume(String resumeText, AnalysisCallback callback) {
        try {
            // Set up the Gemini model
            GenerativeModelFutures model = GenerativeModelFutures.from(
                    new GenerativeModel("gemini-2.0-flash-001", API_KEY)
            );

            // Create the prompt for resume analysis
            String prompt = "Analyze the following resume and provide constructive feedback along with the person information " +
                    "on how to improve it. Include suggestions on structure, content, formatting, " +
                    "and keywords. Focus on making it more effective for job applications.\n\n" +
                    "RESUME TEXT:\n" + resumeText;

            Content content = new Content.Builder()
                    .addText(prompt)
                    .build();

            // Make the API call
            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

            // Handle the response
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String analysisResult = result.getText();
                    Log.d(TAG, "Analysis complete: " + analysisResult);
                    callback.onAnalysisComplete(analysisResult);
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "Error analyzing resume", t);
                    callback.onAnalysisComplete("Error analyzing resume: " + t.getMessage());
                }
            }, executor);
        } catch (Exception e) {
            Log.e(TAG, "Exception in analyzeResume", e);
            callback.onAnalysisComplete("Error: " + e.getMessage());
        }
    }
}