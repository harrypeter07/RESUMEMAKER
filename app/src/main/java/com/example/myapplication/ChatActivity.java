package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private List<ChatMessage> messages;
    private ChatAdapter adapter;
    private AIChatService aiChatService;
    private LinearProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("AI Resume Assistant");

        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        progressIndicator = findViewById(R.id.progressIndicator);

        messages = new ArrayList<>();
        adapter = new ChatAdapter(messages);
        aiChatService = new AIChatService(this);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Add welcome message
        messages.add(new ChatMessage(
            "Hello! I'm your AI resume assistant. I can help you create, format, and improve your resume. What would you like help with?",
            false
        ));
        adapter.notifyItemInserted(0);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    messageInput.setText("");
                }
            }
        });
    }

    private void sendMessage(String message) {
        // Add user message
        messages.add(new ChatMessage(message, true));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
        
        // Show progress indicator
        progressIndicator.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);
        messageInput.setEnabled(false);

        // Get AI response
        aiChatService.sendMessage(message, new AIChatService.ChatCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> {
                    progressIndicator.setVisibility(View.GONE);
                    sendButton.setEnabled(true);
                    messageInput.setEnabled(true);

                    messages.add(new ChatMessage(response, false));
                    adapter.notifyItemInserted(messages.size() - 1);
                    recyclerView.scrollToPosition(messages.size() - 1);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressIndicator.setVisibility(View.GONE);
                    sendButton.setEnabled(true);
                    messageInput.setEnabled(true);
                    Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (aiChatService != null) {
            aiChatService.shutdown();
        }
    }
} 