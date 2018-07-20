package com.example.androidchat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {
    private final int NORMAL_CLOSURE_STATUS = 1000;

    private TextView outputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputTextView = findViewById(R.id.output_text_view);
    }

    public void start(View view) {
        final String message = "Test message";
        final String url = "ws://echo.websocket.org";
        output("Sending text: " + message);

        final OkHttpClient client = new OkHttpClient.Builder().readTimeout(3, TimeUnit.SECONDS).build();
        final Request request = new Request.Builder().url(url).build();

        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                webSocket.send("Test message");
                webSocket.close(NORMAL_CLOSURE_STATUS, null);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                output("Receiving text: " + text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                output("Receiving bytes: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                output("Closing connection with code: " + code);
                webSocket.close(NORMAL_CLOSURE_STATUS, null);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                output("Error: " + t.getMessage());
            }
        });
        client.dispatcher().executorService().shutdown();
    }

    public void clear(View view) {
        clearOutput();
    }

    private void output(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                outputTextView.append("\n\n" + text);
            }
        });
    }

    private void clearOutput() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                outputTextView.setText(null);
            }
        });
    }
}
