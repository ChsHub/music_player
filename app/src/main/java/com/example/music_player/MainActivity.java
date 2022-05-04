package com.example.music_player;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.session.MediaController;
import android.media.session.MediaSession.Token;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.util.Log.i;

public class MainActivity extends AppCompatActivity
{
    public static final String CHANNEL_ID = "exampleServiceChannel";
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public MediaController mediaController;
    //   MediaBrowser mediaBrowser; // Communicates with MediaBrowserService TODO
    //

    Messenger mService = null; // Messenger for communicating with the service.
    boolean bound; // Flag indicating whether we have called bind on the service.

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            bound = true;
        }

        public void onServiceDisconnected(ComponentName className)
        {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            bound = false;
        }
    };

    /**
     * Checks if the app has permission to write to device storage.
     * If the app does not has permission then the user will be prompted to grant permissions.
     */
    public void verifyStoragePermissions()
    {
        ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
        );
    }

    /**
     * Notification Channel for PlayerService
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void createNotificationChannel()
    {

        Uri notificationSound = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification);
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Example Service Channel", NotificationManager.IMPORTANCE_HIGH);
        AudioAttributes audioAttributes = serviceChannel.getAudioAttributes();

        serviceChannel.setSound(notificationSound, audioAttributes); // Can only be set before createNotificationChannel
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(serviceChannel);
    }

    /**
     * App Entry point
     *
     * @param savedInstanceState if the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        createNotificationChannel();
        initDirectoryList();

        //mediaController = new MediaController(context, (Token) intent.getParcelableExtra("sessionToken")); TODO
    }

    protected void initDirectoryList()
    {
        RecyclerView recyclerView = findViewById(R.id.track_list);
        List<TrackItem> trackList = generateTrackList(30);
        recyclerView.setAdapter(new TrackListAdapter(trackList));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // Optimized performance, if Recycler size is fixed
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        if (hasFocus) {
            Intent intent = getIntent();
            Intent playerIntent = new Intent(this, PlayerService.class);
            Uri uri = null;

            switch (intent.getAction()) {
                case Intent.ACTION_SEND:
                    uri = handleSend(intent); // Handle single audio being sent
                    break;
                case Intent.ACTION_SEND_MULTIPLE:
                    uri = handleSendMultiple(intent);
                    break;
                case Intent.ACTION_VIEW:
                    uri = intent.getData();
                    break;
            }
            if (uri != null) {
                // Start music service, with attached intent
                // https://developer.android.com/guide/components/services?hl=en#LifecycleCallbacks
                playerIntent.putExtra(PlayerService.URI_EXTRA, uri);
                if (bindService(playerIntent, mConnection, BIND_AUTO_CREATE)) {
                    i("startService", "Service bound");
                }
            }
        }
    }


    private void connectPlayerService(Token token)
    {
        mediaController = new MediaController(this, token);
        //mediaBrowser = new MediaBrowser(this, PlayerService.class,); TODO
    }

    /**
     * On Start Button click, play the song
     *
     * @param view Button view object
     */
    public void togglePlayback(View view)
    {
        sendPlayerServiceMessage(IncomingHandler.MESSAGE_TOGGLE_PLAYER);
    }

    protected Uri handleSend(Intent intent)
    {
        return intent.getParcelableExtra(Intent.EXTRA_STREAM);
    }

    protected Uri handleSendMultiple(Intent intent)
    {
        ArrayList<Uri> audioURIs = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (audioURIs != null) {
            // TODO Update UI to reflect multiple files
        }
        return null;
    }

    protected void sendPlayerServiceMessage(int message)
    {
        if (!bound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, message, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate test data
     * TODO replace with directory navigation
     */
    protected List<TrackItem> generateTrackList(int size)
    {
        String path = Environment.getExternalStorageDirectory().toString() + "/"; // TODO fix file listing
        AssetManager mgr = getAssets();
        try {

            String[] fileList = mgr.list(path);
            Log.e("FILES", String.valueOf(fileList.length));

            for (String s : fileList) {
                Log.e("FILE:", path + "/" + s);
            }

        } catch (IOException e) {
            Log.v("List error:", "can't list" + path);
        }

        ArrayList<TrackItem> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new TrackItem(R.drawable.ic_play_black_24dp, "Track " + Integer.valueOf(i).toString(), "Artist"));
        }
        return list;
    }
}