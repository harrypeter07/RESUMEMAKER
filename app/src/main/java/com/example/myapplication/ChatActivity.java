package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private AIChatService aiChatService;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        try {
            // Initialize views
            recyclerView = findViewById(R.id.recyclerView);
            messageInput = findViewById(R.id.messageInput);
            sendButton = findViewById(R.id.sendButton);
            progressBar = findViewById(R.id.progressBar);

            if (recyclerView == null || messageInput == null || sendButton == null || progressBar == null) {
                throw new IllegalStateException("Required views not found in layout");
            }

            // Set up RecyclerView
            chatMessages = new ArrayList<>();
            chatAdapter = new ChatAdapter(chatMessages);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(chatAdapter);

            // Initialize AI service
            try {
                aiChatService = new AIChatService(this);
            } catch (Exception e) {
                showError("Failed to initialize AI service: " + e.getMessage());
                finish();
                return;
            }

            // Add welcome message
            chatMessages.add(new ChatMessage(
                "Hello! I'm REMAA, your Resume Enhancement and Management AI Assistant. " +
                "I can help you create, format, and improve your resume. What would you like help with?",
                false
            ));
            chatAdapter.notifyItemInserted(0);

            // Set up send button
            sendButton.setOnClickListener(v -> sendMessage());

        } catch (Exception e) {
            showError("Failed to initialize chat: " + e.getMessage());
            finish();
        }
    }

    private void sendMessage() {
        try {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) {
                return;
            }

            // Add user message
            chatMessages.add(new ChatMessage(message, true));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            recyclerView.scrollToPosition(chatMessages.size() - 1);

            // Clear input and show progress
            messageInput.setText("");
            progressBar.setVisibility(View.VISIBLE);
            sendButton.setEnabled(false);
            messageInput.setEnabled(false);

            // Get AI response
            aiChatService.sendMessage(message, new AIChatService.ChatCallback() {
                @Override
                public void onResponse(String response) {
                    runOnUiThread(() -> {
                        try {
                            progressBar.setVisibility(View.GONE);
                            sendButton.setEnabled(true);
                            messageInput.setEnabled(true);

                            chatMessages.add(new ChatMessage(response, false));
                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                            recyclerView.scrollToPosition(chatMessages.size() - 1);
                        } catch (Exception e) {
                            showError("Error displaying response: " + e.getMessage());
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        try {
                            progressBar.setVisibility(View.GONE);
                            sendButton.setEnabled(true);
                            messageInput.setEnabled(true);

                            if (error.contains("API key")) {
                                showError("API key not configured. Please set up your Gemini API key in AIChatService.java");
                            } else {
                                showError("Error: " + error);
                            }
                        } catch (Exception e) {
                            showError("Error handling error response: " + e.getMessage());
                        }
                    });
                }
            });
        } catch (Exception e) {
            showError("Error sending message: " + e.getMessage());
        }
    }

    private void showError(String message) {
        try {
            if (!isFinishing() && recyclerView != null) {
                Snackbar snackbar = Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG);
                snackbar.setAction("OK", v -> snackbar.dismiss());
                snackbar.show();
            }
        } catch (Exception e) {
            // If even showing the error fails, log it
            e.printStackTrace();
        }
    }
} 