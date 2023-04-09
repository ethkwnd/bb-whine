import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
                    public void onClick(View v) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_RECORD_AUDIO_PERMISSION);

            } else {
                if (!isRecording) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        }
    });
}

private void startRecording() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    String currentTime = dateFormat.format(new Date());
    audioFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/AudioRecorder_" + currentTime + ".3gp";

    mediaRecorder = new MediaRecorder();
    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    mediaRecorder.setOutputFile(audioFilePath);

    try {
        mediaRecorder.prepare();
        mediaRecorder.start();
        isRecording = true;
        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private void stopRecording() {
    if (isRecording) {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
    }
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
        }
    }
}

@Override
protected void onStop() {
    super.onStop();
    if (mediaRecorder != null) {
        stopRecording();
    }
}

