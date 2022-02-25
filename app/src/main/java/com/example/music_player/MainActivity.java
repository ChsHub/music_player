package com.example.music_player;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.util.Log.e;

public class MainActivity extends AppCompatActivity {
    public static final String CHANNEL_ID = "exampleServiceChannel";
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static File getCacheFile(Context context) {
        String path = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification2).toString();
        File cacheFile = new File(context.getCacheDir(), path);
        try {
            InputStream inputStream = context.getAssets().open(path);
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            e("MainActivity", "FILE NOT FOUND");
        }
        return cacheFile;
    }

    /**
     * Checks if the app has permission to write to device storage.
     * If the app does not has permission then the user will be prompted to grant permissions.
     */
    public void verifyStoragePermissions() {
        ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
        );
        //}
    }

    /**
     * App Entry point
     *
     * @param savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Logger logger = new Logger();
        BottomNavigationView navView = findViewById(R.id.nav_view);

        verifyStoragePermissions();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        //Create Notification Channel
        Uri notificationSound = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification2);
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Example Service Channel", NotificationManager.IMPORTANCE_HIGH);
        AudioAttributes audioAttributes = serviceChannel.getAudioAttributes();

        serviceChannel.setSound(notificationSound, audioAttributes); // Can only be set before createNotificationChannel
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(serviceChannel);
    }

    /**
     * On Start Button click, play the song
     *
     * @param view
     */
    public void startService(View view) {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String audioExtra = "";


        if (type != null) {
            if (Intent.ACTION_SEND.equals(action)) {
                if (type.startsWith("audio/")) {
                    audioExtra = handleSend(intent).getPath(); // Handle single audio being sent
                }
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                if (type.startsWith("audio/")) {
                    handleSendMultiple(intent); // Handle multiple audio being sent
                }
            }
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            audioExtra = uri.getPath();
        }

        // TODO handle different paths document/audio
        if (audioExtra.contains("document/raw:")) {
            audioExtra = audioExtra.replace("/document/raw:", "");
        }
        if (!audioExtra.equals("")) {
            Intent serviceIntent = new Intent(this, PlayerService.class);
            serviceIntent.putExtra("inputExtra", audioExtra); // Puts intent with audio path as extra
            ContextCompat.startForegroundService(this, serviceIntent);
        }
    }

    Uri handleSend(Intent intent) {
        return intent.getParcelableExtra(Intent.EXTRA_STREAM);
    }

    void handleSendMultiple(Intent intent) {
        ArrayList<Uri> audioURIs = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (audioURIs != null) {
            // TODO Update UI to reflect multiple images being shared
        }
    }

    public void stopService(View v) {
        Intent serviceIntent = new Intent(this, PlayerService.class);
        stopService(serviceIntent);
    }
}