package com.example.androidchat;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity {
    private final int NORMAL_CLOSURE_STATUS = 1000;

    private EditText messageInput;
    private MessageAdapter messageAdapter;
    private ListView messagesListView;
    private MemberData localMemberData;
    private MemberData remoteMemberData;
    private MemberData systemMemberData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageAdapter = new MessageAdapter(this);
        localMemberData = new MemberData("Local", String.format("#%06X", Color.GREEN & 0x00FFFFFF));
        remoteMemberData = new MemberData("Remote", String.format("#%06X", Color.BLUE & 0x00FFFFFF));
        systemMemberData = new MemberData("System", String.format("#%06X", Color.GRAY & 0x00FFFFFF));

        messageInput = findViewById(R.id.inputText);
        messagesListView = findViewById(R.id.messages_view);
        messagesListView.setAdapter(messageAdapter);
    }

    public void sendMessage(View view) {
        final String messageText = messageInput.getText().toString();
        if (messageText.length() < 1) return;

        final String url = "ws://echo.websocket.org";

        final OkHttpClient client = new OkHttpClient.Builder().readTimeout(3, TimeUnit.SECONDS).build();
        final Request request = new Request.Builder().url(url).build();

        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Message message = new Message(messageText, localMemberData, true);
                webSocket.send(messageText);
                output(message);
                webSocket.close(NORMAL_CLOSURE_STATUS, null);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Message message = new Message(text, remoteMemberData, false);
                output(message);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Message message = new Message("Closing connection with code " + code, systemMemberData, false);
                output(message);
                webSocket.close(NORMAL_CLOSURE_STATUS, null);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                Message message = new Message("Error " + t.getMessage(), systemMemberData, false);
                output(message);
            }
        });
        client.dispatcher().executorService().shutdown();
    }

    private void output(final Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.add(message);
                messagesListView.setSelection(messagesListView.getCount() - 1);
            }
        });
    }
}
