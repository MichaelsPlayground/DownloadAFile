package de.androidcrypto.downloadafile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DownloadmanagerActivity extends AppCompatActivity {

    DownloadManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloadmanager);

        Button run = findViewById(R.id.btnDownloadmanagerRun);
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText url = findViewById(R.id.etDownloadmanagerUrl);
                manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(url.getText().toString());
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                long reference = manager.enqueue(request);
            }
        });
    }
}