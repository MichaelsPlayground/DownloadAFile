package de.androidcrypto.downloadafile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class DownloadmanagerActivity extends AppCompatActivity implements View.OnClickListener {

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
    ProgressBar progressBar;
    private static final int REQUEST_PERMISSION_WRITE_BYTE_EXTERNAL_STORAGE = 104;
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

        progressBar = findViewById(R.id.pbDownloadmanager);
        url = findViewById(R.id.etDownloadmanagerUrl);
        filename = findViewById(R.id.etDownloadmanagerFilename);
        urlLarge = findViewById(R.id.etDownloadmanagerUrlLarge);
        filenameLarge = findViewById(R.id.etDownloadmanagerFilenameLarge);

        query = findViewById(R.id.query);
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryStatus();
            }
        });
        start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startDownload();
            }
        });
        view = findViewById(R.id.view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                verifyPermissionsWriteByte();
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
                verifyPermissionsWriteByte();
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

    private void downloadUsingDownloadmanager(String downloadUrl, Uri uriFileStorage) {
        Log.i(TAG, "startDownload");
        Uri uri = Uri.parse(downloadUrl);

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .mkdirs();

        DownloadManager.Request req = new DownloadManager.Request(uri);

        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle("Demo")
                .setDescription("Something useful. No, really.")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        downloadFilename);
        lastDownload = mgr.enqueue(req);

        //v.setEnabled(false);
        //query.setEnabled(true);
        queryStatus();
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

    private void downloadUsingDownloadmanager2(String downloadUrl, Uri uri) {
        /*
        try {

            URL url = new URL(downloadUrl);
            BufferedInputDownloadmanager bis = new BufferedInputDownloadmanager(url.openDownloadmanager());
            OutputDownloadmanager fis = contextSave.getContentResolver().openOutputDownloadmanager(uri);
            //FileOutputDownloadmanager fis = new FileOutputDownloadmanager(file);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = bis.read(buffer, 0, 1024)) != -1) {
                fis.write(buffer, 0, count);
            }
            fis.close();
            bis.close();
            progressBar.setVisibility(View.INVISIBLE);


        } catch (IOException e) {
            e.printStackTrace();
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(),
                    "Failure: " + e.toString(),
                    Toast.LENGTH_SHORT).show();
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_WRITE_BYTE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writeByteToExternalSharedStorage();
            } else {
                Toast.makeText(this, "Grant Storage Permission is Required to use this function.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void verifyPermissionsWriteByte() {
        Log.i(TAG, "verifyPermissionsWriteByte");
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "verifyPermissionsWriteByte permissions GRANTED");
            writeByteToExternalSharedStorage();
        } else {
            Log.i(TAG, "verifyPermissionsWriteByte permissions NOT GRANTED");
            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_PERMISSION_WRITE_BYTE_EXTERNAL_STORAGE);
        }
    }

    private void writeByteToExternalSharedStorage() {
        Log.i(TAG, "writeByteToExternalSharedStorage");
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        //boolean pickerInitialUri = false;
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, filename.getText().toString());
        // get filename from edittext

        String filenameIntent = downloadFilename;
        // sanity check
        if (filenameIntent.equals("")) {
            filename.setText("no filename to save");
            return;
        }
        intent.putExtra(Intent.EXTRA_TITLE, filenameIntent);
        byteFileWriterActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> byteFileWriterActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains a URI for the document or directory that
                        // the user selected.
                        Uri uri = null;
                        if (resultData != null) {
                            uri = resultData.getData();
                            // Perform operations on the document using its URI.
                            try {
                                downloadUsingDownloadmanager(downloadUrl, uri);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),
                                        "Failure: " + e.toString(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });

    @Override
    public void onClick(View v) {

    }
}