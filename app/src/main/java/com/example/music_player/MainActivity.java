package com.example.music_player;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.session.MediaController;
import android.media.session.MediaSession.Token;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

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
    /**
     * Messenger for communicating with the service.
     */
    Messenger mService = null;

    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean bound;

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

        Uri notificationSound = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.notification2);
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
    @RequiresApi(api = Build.VERSION_CODES.O)
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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        createNotificationChannel();


        //mediaController = new MediaController(context, (Token) intent.getParcelableExtra("sessionToken")); TODO


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startService(View view)
    {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String audioExtra = "";
        Uri uri;
        Intent playerIntent = new Intent(this, PlayerService.class);


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
            uri = intent.getData();
            audioExtra = uri.getPath();
            playerIntent.putExtra("inputUri", uri);
        }

        // TODO handle different paths document/audio
        if (audioExtra.contains("document/raw:")) {
            audioExtra = audioExtra.replace("/document/raw:", "");
        }
        if (audioExtra.contains("/document/primary:")) {
            audioExtra = audioExtra.replace("/document/primary:", "");
        }
        // Start music service, with attached intent
        if (!audioExtra.equals("")) {

            playerIntent.putExtra("inputExtra", audioExtra); // Puts intent with audio path as extra
            // https://developer.android.com/guide/components/services?hl=en#LifecycleCallbacks
            if (bindService(playerIntent, mConnection, BIND_AUTO_CREATE)) {
                i("startService", "Service bound");
            }
        }
    }

    protected Uri handleSend(Intent intent)
    {
        return intent.getParcelableExtra(Intent.EXTRA_STREAM);
    }

    protected void handleSendMultiple(Intent intent)
    {
        ArrayList<Uri> audioURIs = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (audioURIs != null) {
            // TODO Update UI to reflect multiple files
        }
    }

    /**
     * TODO On Stop Button click, pause the song
     *
     * @param view Button view object
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopService(View view)
    {
        if (!bound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, PlayerService.MSG_SAY_HELLO, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}