package com.example.music_player;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String CHANNEL_ID = "exampleServiceChannel";
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage.
     * If the app does not has permission then the user will be prompted to grant permissions.
     */
    public void verifyStoragePermissions() {
        // Check if we have write permission
        //int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

       // if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
        ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
        );
        //}
    }

    /**
     * App Entry point
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger logger = new Logger();
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
    }

    /**
     * On Start Button click, play the song
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
                    audioExtra = handleSend(intent).getPath(); // Handle single image being sent
                }
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                if (type.startsWith("audio/")) {
                    handleSendMultiple(intent); // Handle multiple images being sent
                }
            }
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData(); // Handle single image being sent
            audioExtra = uri.getPath(); // Handle single image being sent
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