package com.example.music_player;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.core.app.NotificationCompat;

import java.io.File;

import static android.util.Log.e;
import static com.example.music_player.MainActivity.CHANNEL_ID;

/***
 * Service that runs the music player.
 * Should run in own thread since it's CPU heavy:
 * https://developer.android.com/reference/android/app/Service
 */
public class PlayerService extends MessengerService
{
    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            mValue = msg.arg1;
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String input = intent.getStringExtra("inputExtra");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        // Open activity when clicking on notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Get sound URI and Attributes for channel

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Now playing: " + input) //TODO set string
                    .setContentText(input)
                    .setSmallIcon(R.drawable.ic_home_black_24dp)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            startForeground(1, notification); // Notification Identifier >0; for updating notification
        }

        //do heavy work on a background thread
        //stopSelf();

        startPlayer(input);

        return START_STICKY; // START_STICKY for explicit start and stop of Service https://developer.android.com/reference/android/app/Service#START_STICKY
    }

    @Override
    public void showNotification()
    {

    }

    /***
     * Play audio file
     * @param path Audio file path
     */
    protected void startPlayer(String path)
    {
        // Creating file is needed for MediaPlayer
        File file = new File(path);
        if (!file.exists()) {
            e("StartPlayer", "File not found");
            return;
        }

        Uri uri = Uri.fromFile(file);
        final MediaPlayer player = MediaPlayer.create(this, uri);

        if (player == null) {
            e("StartPlayer", "Player is null");
            return;
        }
        //player.prepareAsync();
        player.setVolume(50, 50);
        player.start();
    }
}