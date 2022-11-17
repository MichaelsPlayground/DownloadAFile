package de.androidcrypto.downloadafile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    /**
     * sample files from https://file-examples.com
     * txt: https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_txt.txt?alt=media&token=cdd9d344-a104-4636-ba86-e013e9fb9be6
     * https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_100kb.jpg?alt=media&token=ca709aad-cdd4-43d2-b404-171363737b64
     * 100kb jpg: https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_100kb.jpg?alt=media&token=adbf8437-09c3-451e-82f0-f59badf1778e
     * 1000kb jpg: https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_1mb.jpg?alt=media&token=2649d345-2da7-4c8f-b3f6-e193137fcb66
     * https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_2500kb.jpg?alt=media&token=2d1cfdfb-9b6d-4019-9433-7762c7793f42
     * 2500kb jpg: https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_2500kb.jpg?alt=media&token=6d3f4784-c7fc-4920-a498-288d54a1d3ea
     */

    public static String jpg100 = "https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_100kb.jpg?alt=media&token=ca709aad-cdd4-43d2-b404-171363737b64";
    public static String jpg2500 = "https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_2500kb.jpg?alt=media&token=2d1cfdfb-9b6d-4019-9433-7762c7793f42";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // the error happens while doing network operations on MainThread
        // solution: https://stackoverflow.com/questions/25093546/android-os-networkonmainthreadexception-at-android-os-strictmodeandroidblockgua
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Button useDownloadmanager = findViewById(R.id.btnDownloadmanager);
        useDownloadmanager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadmanagerActivity.class);
                startActivity(intent);
            }
        });

        Button useOkhttp = findViewById(R.id.btnOkhttp);
        useOkhttp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, OkhttpActivity.class);
                startActivity(intent);
            }
        });

        Button useStream = findViewById(R.id.btnStream);
        useStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, StreamActivity.class);
                startActivity(intent);
            }
        });

        Button useHttpUrlConnection = findViewById(R.id.btnHttpUrlConnection);
        useHttpUrlConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HttpUrlConnectionActivity.class);
                startActivity(intent);
            }
        });
    }
}