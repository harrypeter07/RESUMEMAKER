package com.example.myapplication;

import android.content.Context;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.Content;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class AIChatService {
    private static final String API_KEY = "AIzaSyAzmkkBxUZ86V3RZEt7sra_Pyi-rn6BqOE";
    private final GenerativeModel model;
    private static final String SYSTEM_PROMPT = 
        "You are REMAA , made by Four members team Hassan , Harshal , Harshvardhan and Harshad (Resume Enhancement and Management AI Assistant). " +
        "You help users create, format, and improve their resumes. " +
        "You provide professional, concise advice and can assist with specific sections of a resume. " +
        "Keep responses focused and practical. " +
        "If you don't know something, admit it and suggest alternatives.";

    public AIChatService(Context context) {
        try {
            model = new GenerativeModel(
                "gemini-2.0-flash-001",
                API_KEY
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Gemini model: " + e.getMessage());
        }
    }

    public interface ChatCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public void sendMessage(String message, ChatCallback callback) {
        if (message == null || message.trim().isEmpty()) {
            callback.onError("Message cannot be empty");
            return;
        }

        try {
            String fullPrompt = SYSTEM_PROMPT + "\nUser: " + message;
            
            model.generateContent(
                fullPrompt,
                new Continuation<GenerateContentResponse>() {
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(Object result) {
                        try {
                            if (result instanceof GenerateContentResponse) {
                                GenerateContentResponse response = (GenerateContentResponse) result;
                                String text = response.getText();
                                if (text != null && !text.isEmpty()) {
                                    callback.onResponse(text);
                                } else {
                                    callback.onError("Empty response received from AI");
                                }
                            } else if (result instanceof Throwable) {
                                Throwable error = (Throwable) result;
                                callback.onError("AI Error: " + error.getMessage());
                            } else {
                                callback.onError("Failed to generate response: Unknown response type");
                            }
                        } catch (Exception e) {
                            callback.onError("Error processing response: " + e.getMessage());
                        }
                    }
                }
            );
        } catch (Exception e) {
            callback.onError("Error: " + e.getMessage());
        }
    }
}