package com.androidlab.client;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.hardware.Camera;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.AudioFormat;

public class MainActivity extends Activity {

    private static final int PERMISSION_CODE = 100;
    private TextView status;
    private SurfaceView preview;
    private Camera camera;
    private AudioRecord audioRecord;
    private Thread audioThread;
    private volatile boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(24, 24, 24, 24);

        TextView title = new TextView(this);
        title.setText("AndroidLab Client");
        title.setTextSize(25);

        status = new TextView(this);
        status.setText("آماده به کار");
        status.setTextSize(17);

        preview = new SurfaceView(this);
        layout.addView(title);
        layout.addView(status);
        layout.addView(preview,
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 450));

        Button permissionButton = new Button(this);
        permissionButton.setText("درخواست مجوزها");
        permissionButton.setOnClickListener(v -> requestPermissions());
        layout.addView(permissionButton);

        Button cameraButton = new Button(this);
        cameraButton.setText("شروع دوربین");
        cameraButton.setOnClickListener(v -> startCamera());
        layout.addView(cameraButton);

        Button micButton = new Button(this);
        micButton.setText("شروع آزمایش میکروفون");
        micButton.setOnClickListener(v -> startMic());
        layout.addView(micButton);

        Button stopButton = new Button(this);
        stopButton.setText("توقف میکروفون");
        stopButton.setOnClickListener(v -> stopMic());
        layout.addView(stopButton);

        setContentView(layout);
    }

    private void requestPermissions() {
        requestPermissions(new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        }, PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);

        if (requestCode == PERMISSION_CODE) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                status.setText("مجوز دوربین و میکروفون داده شد.");
            } else {
                status.setText("هر دو مجوز لازم هستند.");
            }
        }
    }

    private void startCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }

        try {
            if (camera != null) return;

            camera = Camera.open();
            camera.setPreviewDisplay(preview.getHolder());
            camera.startPreview();
            status.setText("دوربین فعال است.");
        } catch (Exception e) {
            status.setText("خطای دوربین: " + e.getMessage());
            if (camera != null) {
                camera.release();
                camera = null;
            }
        }
    }

    private void startMic() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }

        if (recording) return;

        int sampleRate = 16000;
        int bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        );

        if (bufferSize <= 0) {
            status.setText("خطا در آماده‌سازی میکروفون.");
            return;
        }

        audioRecord = new AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        );

        audioRecord.startRecording();
        recording = true;
        status.setText("میکروفون فعال است.");

        audioThread = new Thread(() -> {
            short[] buffer = new short[bufferSize];

            while (recording) {
                int count = audioRecord.read(buffer, 0, buffer.length);
                if (count > 0) {
                    long total = 0;
                    for (int i = 0; i < count; i++) {
                        total += Math.abs((int) buffer[i]);
                    }

                    final int level = (int) (total / count);
                    runOnUiThread(() -> {
                        if (recording) {
                            status.setText("میکروفون فعال است | سطح صدا: " + level);
                        }
                    });
                }
            }
        });

        audioThread.start();
    }

    private void stopMic() {
        recording = false;

        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }

        status.setText("میکروفون متوقف شد.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMic();

        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
