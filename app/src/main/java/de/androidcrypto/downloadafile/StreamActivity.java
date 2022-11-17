package de.androidcrypto.downloadafile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

public class StreamActivity extends AppCompatActivity {

    private static final String TAG = "StreamActivity";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        progressBar = findViewById(R.id.pbStream);
        url = findViewById(R.id.etStreamUrl);
        filename = findViewById(R.id.etStreamFilename);
        urlLarge = findViewById(R.id.etStreamUrlLarge);
        filenameLarge = findViewById(R.id.etStreamFilenameLarge);

        Button run = findViewById(R.id.btnStreamRun);
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

        Button runLarge = findViewById(R.id.btnStreamLargeRun);
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

    private void downloadUsingStream(String downloadUrl, Uri uri) {
        try {
            URL url = new URL(downloadUrl);
            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            OutputStream fis = contextSave.getContentResolver().openOutputStream(uri);
            //FileOutputStream fis = new FileOutputStream(file);
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
        }
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
                                downloadUsingStream(downloadUrl, uri);
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
}