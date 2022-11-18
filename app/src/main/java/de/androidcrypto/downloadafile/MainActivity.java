package de.androidcrypto.downloadafile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * sample files from https://file-examples.com
     * txt: https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_txt.txt?alt=media&token=cdd9d344-a104-4636-ba86-e013e9fb9be6
     * https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_100kb.jpg?alt=media&token=ca709aad-cdd4-43d2-b404-171363737b64
     * 100kb jpg: https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_100kb.jpg?alt=media&token=adbf8437-09c3-451e-82f0-f59badf1778e
     * 1000kb jpg: https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_1mb.jpg?alt=media&token=2649d345-2da7-4c8f-b3f6-e193137fcb66
     * https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_2500kb.jpg?alt=media&token=2d1cfdfb-9b6d-4019-9433-7762c7793f42
     * 2500kb jpg: https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_2500kb.jpg?alt=media&token=6d3f4784-c7fc-4920-a498-288d54a1d3ea
     */

    Button useOkhttp;
    private View mLayout;

    public static String jpg100 = "https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_100kb.jpg?alt=media&token=ca709aad-cdd4-43d2-b404-171363737b64";
    public static String jpg2500 = "https://firebasestorage.googleapis.com/v0/b/fir-playground-1856e.appspot.com/o/samplefiles%2Fsample_jpg_2500kb.jpg?alt=media&token=2d1cfdfb-9b6d-4019-9433-7762c7793f42";

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 301;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // the error happens while doing network operations on MainThread
        // solution: https://stackoverflow.com/questions/25093546/android-os-networkonmainthreadexception-at-android-os-strictmodeandroidblockgua
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mLayout = findViewById(R.id.main_layout);
        useOkhttp = findViewById(R.id.btnOkhttp);

        /*
        boolean preCheck = PermissionsUtilsOld.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        System.out.println("*** PreCheck: " + preCheck);

        boolean check = PermissionsUtilsOld.checkAndRequestPermissions(this);
        System.out.println("*** Permission check: " + check);
*/

        showPermissionsPreview();
/*
        autoRequestAllPermissions();

        String requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int checkVal = checkCallingOrSelfPermission(requiredPermission);
        boolean permissionGranted = false;
        if (checkVal==PackageManager.PERMISSION_GRANTED) permissionGranted = true;
        System.out.println("*** permissionGranted: " + permissionGranted);
*/

        Button useDownloadmanager = findViewById(R.id.btnDownloadmanager);
        useDownloadmanager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadmanagerActivity.class);
                startActivity(intent);
            }
        });


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

    private void enableButtons() {
        useOkhttp.setEnabled(true);
    }


    /**
     * PERMISSION section
     * code is taken from the official Android permission-samples
     * https://github.com/android/permissions-samples
     */

    private void showPermissionsPreview() {
        // Check if the storage permission has been granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start storage preview
            Snackbar.make(mLayout,
                    "storage_permission_available",
                    Snackbar.LENGTH_SHORT).show();
            enableButtons();
        } else {
            // Permission is missing and must be requested.
            requestStoragePermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            // Request for storage permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start storage preview Activity.
                Snackbar.make(mLayout, "storage_permission_granted",
                                Snackbar.LENGTH_SHORT)
                        .show();
                enableButtons();
            } else {
                // Permission request was denied.
                /*
                Snackbar.make(mLayout, "storage_permission_denied",
                                Snackbar.LENGTH_SHORT)
                        .show();
*/

                // new code starts the system settings dialog for this app after an information is shown
                StringBuilder info = new StringBuilder();
                info.append("The app is downloading a file and wants to store this file in the download folder.\n");
                info.append("\nWithout the permission the app is not allowed to do so - please grant the permissions or the app will stop.\n");
                info.append("\nAfter you granted the permission you need to restart your app to run as expected\n");
                info.append("\nDo you grant the permissions ?");
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        //set icon
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        //set title
                        .setTitle("Why does the app need permissions ?")
                        //set message
                        .setMessage(info.toString())
                        //set positive button
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // call the system settings dialog
                                Intent dialogIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                dialogIntent.setData(Uri.parse("package:" + getPackageName()));
                                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(dialogIntent);
                            }
                        })
                        //set negative button
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .show();
                alertDialog.show();
                /*
                Snackbar.make(mLayout, "storage_access_required",
                        Snackbar.LENGTH_INDEFINITE).setAction("ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Request the permission
                        //Intent dialogIntent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                        Intent dialogIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        dialogIntent.setData(Uri.parse("package:" + getPackageName()));
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(dialogIntent);
                    }
                }).show();
                */
            }
        }
    }

    /**
     * Requests the {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestStoragePermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            Snackbar.make(mLayout, "storage_access_required",
                    Snackbar.LENGTH_INDEFINITE).setAction("ok", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }).show();

        } else {
            Snackbar.make(mLayout, "storage_unavailable", Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * END of PERMISSION section
     */


    // other way
    private void autoRequestAllPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (info == null) {
            return;
        }
        String[] permissions = info.requestedPermissions;
        boolean remained = false;
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remained = true;
            }
        }
        if (remained) {
            requestPermissions(permissions, 0);
        }
    }
}