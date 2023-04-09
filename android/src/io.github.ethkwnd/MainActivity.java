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

import android.os.AsyncTask;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


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

        // Show the upload button
        Button uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setVisibility(View.VISIBLE);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wavFilePath = audioFilePath.replace(".3gp", ".wav");
                convertToWav(audioFilePath, wavFilePath);
                uploadFile(wavFilePath);
            }
    });
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



private void convertToWav(String inputPath, String outputPath) {
    int returnCode = FFmpeg.execute("-i " + inputPath + " -acodec pcm_s16le -ar 44100 -ac 1 " + outputPath);

    if (returnCode == RETURN_CODE_SUCCESS) {
        Log.i("MainActivity", "Audio conversion to WAV successful");
    } else {
        Log.e("MainActivity", "Audio conversion to WAV failed");
    }
}

private void uploadFile(String filePath) {
    new AsyncTask<String, Void, Boolean>() {
        @Override
        protected Boolean doInBackground(String... params) {
            String filePath = params[0];
            String urlString = "https://example.org/upload";
            HttpURLConnection connection = null;

            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "audio/wav");
                connection.connect();

                InputStream inputStream = new BufferedInputStream(new FileInputStream(filePath));
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    connection.getOutputStream().write(buffer, 0, bytesRead);
                }

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return true;
                } else {
                    return false;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }
    }.execute(filePath);
}
