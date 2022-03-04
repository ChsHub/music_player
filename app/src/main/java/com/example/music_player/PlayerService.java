package com.example.music_player;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.MediaSession.Token;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.media.MediaBrowserService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.List;

import static android.util.Log.e;
import static com.example.music_player.MainActivity.CHANNEL_ID;

/***
 * Service that runs the music player.
 * Should run in own thread since it's CPU heavy:
 * https://developer.android.com/reference/android/app/Service
 * https://developer.android.com/guide/topics/media-apps/media-apps-overview
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PlayerService extends MediaBrowserService
{
    MediaPlayer mediaPlayer;
    MediaSession mediaSession;

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowser.MediaItem>> result)
    {

    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        // https://developer.android.com/guide/topics/media-apps/working-with-a-media-session
        mediaSession = new MediaSession(getApplicationContext(), "MediaSession01");
        Token mediaSessionToken = mediaSession.getSessionToken();
        Intent tokenIntent = new Intent();
        tokenIntent.putExtra("sessionToken", mediaSessionToken);
        sendBroadcast(tokenIntent);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints)
    {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Uri inputUri = intent.getParcelableExtra("inputUri");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        // Open activity when clicking on notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Get sound URI and Attributes for channel

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Now playing: " + inputUri.getPath()) //TODO set string
                    .setContentText(inputUri.getPath())
                    .setSmallIcon(R.drawable.ic_home_black_24dp)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            startForeground(1, notification); // Notification Identifier >0; for updating notification
        }

        //        //do heavy work on a background thread
        //stopSelf();

        startPlayer(inputUri);

        return START_STICKY; // START_STICKY for explicit start and stop of Service https://developer.android.com/reference/android/app/Service#START_STICKY
    }


    /***
     * Play audio file
     * @param uri Audio file path
     */
    protected void startPlayer(Uri uri)
    {
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.setVolume(50, 50);
            mediaPlayer.start();
        } catch (Exception exception) {
            e("startPlayer", exception.getMessage());
        }
    }

    protected void stopPlayer()
    {
        mediaPlayer.stop();
    }

    public void onDestroy()
    {
        mediaSession.release(); // clean up the session and notify any controllers
    }
}