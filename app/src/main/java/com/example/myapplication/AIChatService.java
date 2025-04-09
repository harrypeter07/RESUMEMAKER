package com.example.myapplication;

import android.content.Context;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import kotlin.coroutines.Continuation;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AIChatService {
    private static final String API_KEY = "YOUR_GEMINI_API_KEY"; // Replace with your API key
    private final GenerativeModel model;
    private final ExecutorService executor;
    private static final String SYSTEM_PROMPT = 
        "You are a helpful resume building assistant. You can help users create, format, " +
        "and improve their resumes. You provide professional, concise advice and can assist " +
        "with specific sections of a resume. Keep responses focused and practical.";

    public interface ChatCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public AIChatService(Context context) {
        GenerationConfig config = new GenerationConfig.Builder()
            .temperature(0.7f)
            .topK(40)
            .topP(0.95f)
            .maxOutputTokens(1000)
            .build();

        model = new GenerativeModel(
            "gemini-pro",
            API_KEY,
            config
        );

        executor = Executors.newSingleThreadExecutor();
    }

    public void sendMessage(String message, ChatCallback callback) {
        executor.execute(() -> {
            try {
                String fullPrompt = SYSTEM_PROMPT + "\nUser: " + message;
                GenerateContentResponse response = model.generateContent(fullPrompt);
                String aiResponse = response.getText();
                callback.onResponse(aiResponse);
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
} 