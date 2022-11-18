package de.androidcrypto.downloadafile;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DownloadmanagerActivity extends AppCompatActivity  {

    private static final String TAG = "DownloadmanagerActivity";
    /**
     * https://github.com/WebJournal/journaldev/blob/master/CoreJavaProjects/CoreJavaExamples/src/com/journaldev/files/DownloadFileFromURL.java
     */

    EditText url;
    EditText filename;
    EditText urlLarge;
    EditText filenameLarge;
    String downloadUrl;
    String downloadFilename;
    Context contextSave; // needed for write & read a file from uri

    private DownloadManager mgr = null;
    private long lastDownload = -1L;
    private Button query = null;
    private Button start = null;
    private Button view = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloadmanager);

        url = findViewById(R.id.etDownloadmanagerUrl);
        filename = findViewById(R.id.etDownloadmanagerFilename);
        urlLarge = findViewById(R.id.etDownloadmanagerUrlLarge);
        filenameLarge = findViewById(R.id.etDownloadmanagerFilenameLarge);

        url.setText(MainActivity.jpg100);
        urlLarge.setText(MainActivity.jpg2500);

        query = findViewById(R.id.query);
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryStatus();
            }
        });

        mgr = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);

        Button run = findViewById(R.id.btnDownloadmanagerRun);
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**
                 * flow: 1 check given runtime write permissions
                 * 2: fire an intent to get an uri
                 * 3: download file to uri
                 */
                downloadUrl = url.getText().toString();
                downloadFilename = filename.getText().toString();
                contextSave = view.getContext();
                downloadUsingDownloadmanager(downloadUrl, downloadFilename);
            }
        });

        Button runLarge = findViewById(R.id.btnDownloadmanagerLargeRun);
        runLarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**
                 * flow: 1 check given runtime write permissions
                 * 2: fire an intent to get an uri
                 * 3: download file to uri
                 */
                downloadUrl = urlLarge.getText().toString();
                downloadFilename = filenameLarge.getText().toString();
                contextSave = view.getContext();
                downloadUsingDownloadmanager(downloadUrl, downloadFilename);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        f.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        this.registerReceiver(onEvent, f);
    }

    @Override
    public void onPause() {
        this.unregisterReceiver(onEvent);

        super.onPause();
    }

    private void downloadUsingDownloadmanager(String downloadUrl, String downloadFilename) {
        Log.i(TAG, "startDownload");
        Uri uri = Uri.parse(downloadUrl);

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .mkdirs();

        DownloadManager.Request req = new DownloadManager.Request(uri);

        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(downloadFilename)
                .setDescription("Download is running...")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        downloadFilename);
        lastDownload = mgr.enqueue(req);
    }

    @SuppressLint("Range")
    private void queryStatus() {
        Cursor c = mgr.query(new DownloadManager.Query().setFilterById(lastDownload));

        if (c == null) {
            Toast.makeText(this, "download_not_found",
                    Toast.LENGTH_LONG).show();
        } else {
            c.moveToFirst();

            Log.d(getClass().getName(),
                    "COLUMN_ID: "
                            + c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)));
            Log.d(getClass().getName(),
                    "COLUMN_BYTES_DOWNLOADED_SO_FAR: "
                            + c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
            Log.d(getClass().getName(),
                    "COLUMN_LAST_MODIFIED_TIMESTAMP: "
                            + c.getLong(c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)));
            Log.d(getClass().getName(),
                    "COLUMN_LOCAL_URI: "
                            + c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
            Log.d(getClass().getName(),
                    "COLUMN_STATUS: "
                            + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)));
            Log.d(getClass().getName(),
                    "COLUMN_REASON: "
                            + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));

            Toast.makeText(this, statusMessage(c), Toast.LENGTH_LONG)
                    .show();

            c.close();
        }
    }

    private String statusMessage(Cursor c) {
        String msg = "???";
        @SuppressLint("Range") int columnStatus = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
        switch (columnStatus) {
            case DownloadManager.STATUS_FAILED:
                msg = "download_failed";
                break;

            case DownloadManager.STATUS_PAUSED:
                msg = "download_paused";
                break;

            case DownloadManager.STATUS_PENDING:
                msg = "download_pending";
                break;

            case DownloadManager.STATUS_RUNNING:
                msg = "download_in_progress";
                break;

            case DownloadManager.STATUS_SUCCESSFUL:
                msg = "download_complete";
                break;

            default:
                msg = "download_is_nowhere_in_sight";
                break;
        }

        return (msg);
    }

    private BroadcastReceiver onEvent = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent i) {
            if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(i.getAction())) {
                Toast.makeText(ctxt, "hi", Toast.LENGTH_LONG).show();
            } else {
                start.setEnabled(true);
            }
        }
    };
}