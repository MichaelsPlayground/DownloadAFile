package de.androidcrypto.downloadafile;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkhttpActivity extends AppCompatActivity {

    private static final String TAG = "OkhttpActivity";
    /**
     * https://github.com/howtoprogram/Java-Examples/blob/master/okhttp-examples/src/main/java/com/howtoprogram/okhttp/download/DownloadServiceImplOkHttp.java
     * implementation("com.squareup.okhttp3:okhttp:4.10.0")
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
        setContentView(R.layout.activity_okhttp);

        progressBar = findViewById(R.id.pbOkhttp);
        url = findViewById(R.id.etOkhttpUrl);
        filename = findViewById(R.id.etOkhttpFilename);
        urlLarge = findViewById(R.id.etOkhttpUrlLarge);
        filenameLarge = findViewById(R.id.etOkhttpFilenameLarge);

        Button run = findViewById(R.id.btnOkhttpRun);
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

        Button runLarge = findViewById(R.id.btnOkhttpLargeRun);
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

    public void downloadFileAsync(final String downloadUrl, Uri uri) throws Exception {
        progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),
                        "Failure: " + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }

            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    progressBar.setVisibility(View.INVISIBLE);
                    throw new IOException("Failed to download file: " + response);
                }
                // we do need a uri to save the data, get it from an intent
                OutputStream fos = contextSave.getContentResolver().openOutputStream(uri);
                //FileOutputStream fos = new FileOutputStream(uri);
                fos.write(response.body().bytes());
                fos.close();
                Log.i(TAG, "SUCCESS - file written");
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
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
                                downloadFileAsync(downloadUrl, uri);
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