package com.androidlab.admin;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView connectionStatus;
    private TextView audioStatus;
    private TextView videoStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(28, 28, 28, 28);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("AndroidLab Admin");
        title.setTextSize(26);

        connectionStatus = new TextView(this);
        connectionStatus.setText("Client: Disconnected");
        connectionStatus.setTextSize(18);

        videoStatus = new TextView(this);
        videoStatus.setText("Camera: Not sharing");
        videoStatus.setTextSize(17);

        audioStatus = new TextView(this);
        audioStatus.setText("Microphone: Not sharing");
        audioStatus.setTextSize(17);

        TextView preview = new TextView(this);
        preview.setText("\n\nVideo preview\n\nWaiting for an authorized session...");
        preview.setTextSize(18);
        preview.setGravity(Gravity.CENTER);

        Button connectButton = new Button(this);
        connectButton.setText("Connect to Client");
        connectButton.setOnClickListener(v ->
            connectionStatus.setText("Client: Waiting for connection"));

        Button audioButton = new Button(this);
        audioButton.setText("Audio session status");
        audioButton.setOnClickListener(v ->
            audioStatus.setText("Microphone: Waiting for sharing"));

        Button stopButton = new Button(this);
        stopButton.setText("End session");
        stopButton.setOnClickListener(v -> {
            connectionStatus.setText("Client: Disconnected");
            videoStatus.setText("Camera: Not sharing");
            audioStatus.setText("Microphone: Not sharing");
            preview.setText("\n\nVideo preview\n\nSession ended.");
        });

        layout.addView(title);
        layout.addView(connectionStatus);
        layout.addView(videoStatus);
        layout.addView(audioStatus);
        layout.addView(preview,
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 400));
        layout.addView(connectButton);
        layout.addView(audioButton);
        layout.addView(stopButton);

        setContentView(layout);
    }
}
