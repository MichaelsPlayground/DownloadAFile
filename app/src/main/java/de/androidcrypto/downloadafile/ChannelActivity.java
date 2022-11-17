package de.androidcrypto.downloadafile;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ChannelActivity extends AppCompatActivity {

    private static final String TAG = "ChannelActivity";
    /**
     * https://github.com/WebJournal/journaldev/blob/master/CoreJavaProjects/CoreJavaExamples/src/com/journaldev/files/DownloadFileFromURL.java
     */

    EditText url;
    EditText filename;
    EditText urlLarge;
    EditText filenameLarge;
    String downloadUrl;
    String downloadFilename;
    Uri downloadUri;
    Dialog dialog;
    int downloadedSize = 0;
    int totalSize = 0;
    TextView progressValue;
    ProgressBar progressBar;
    private static final int REQUEST_PERMISSION_WRITE_BYTE_EXTERNAL_STORAGE = 104;
    Context contextSave; // needed for write & read a file from uri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        progressBar = findViewById(R.id.pbChannel);
        progressValue = findViewById(R.id.tvChannelProgress);
        url = findViewById(R.id.etChannelUrl);
        filename = findViewById(R.id.etChannelFilename);
        urlLarge = findViewById(R.id.etChannelUrlLarge);
        filenameLarge = findViewById(R.id.etChannelFilenameLarge);

        Button run = findViewById(R.id.btnChannelRun);
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

        Button runLarge = findViewById(R.id.btnChannelLargeRun);
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

    private void downloadUsingChannel(String downloadUrl, Uri uri) {
        showProgress(downloadUrl);
        new Thread(new Runnable() {
            public void run() {
                downloadFile();
            }
        }).start();
    }

    void downloadFile(){
        Log.i(TAG, "downloadFile");
        downloadedSize = 0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection urlConnection = (HttpURLConnection)
                    url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            //connect
            urlConnection.connect();

            //set the path where we want to save the file
            //File SDCardRoot = Environment.getExternalStorageDirectory();
            //create a new file, to save the downloaded file
            //File file = new File(SDCardRoot,"downloaded_file.png");
            Log.i(TAG, "downloadUri: " + downloadUri);
            OutputStream fileOutput = contextSave.getContentResolver().openOutputStream(downloadUri);

            //Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //this is the total size of the file which we are downloading
            totalSize = urlConnection.getContentLength();

            runOnUiThread(new Runnable() {
                public void run() {
                    progressBar.setProgress(0);
                    progressBar.setMax(totalSize);
                }
            });

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            Log.i(TAG, "bufferLength: " + bufferLength);
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                Log.i(TAG, "bufferLength: " + bufferLength);
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                // update the progressbar //
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressBar.setProgress(downloadedSize);
                        float per = ((float)downloadedSize/totalSize) *
                                100;
                        progressValue.setText("Downloaded " + downloadedSize +
                                "KB / " + totalSize + "KB (" + (int)per + "%)" );
                    }
                });
            }
            // close the output stream when complete //
            fileOutput.close();
            runOnUiThread(new Runnable() {
                public void run() {
                    // pb.dismiss(); // if you want close it..
                }
            });

        } catch (final MalformedURLException e) {
            showError("Error : MalformedURLException " + e);
            e.printStackTrace();
        } catch (final IOException e) {
            showError("Error : IOException " + e);
            e.printStackTrace();
        }
        catch (final Exception e) {
            showError("Error : Please check your internet connection " +
                    e);
        }
    }

    void showError(final String err){
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(ChannelActivity.this, err,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    void showProgress(String file_path){
        //dialog = new Dialog(ChannelActivity.this);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.setContentView(R.layout.myprogressdialog);
        //dialog.setTitle("Download Progress");

        //TextView text = (TextView) dialog.findViewById(R.id.tv1);
        //text.setText("Downloading file from ... " + file_path);
        //progressValue = (TextView) dialog.findViewById(R.id.cur_pg_tv);
        progressValue.setText("Starting download...");
        //dialog.show();

        progressBar.setProgress(0);
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
                                downloadUri = uri;
                                downloadUsingChannel(downloadUrl, uri);
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