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
import android.os.IBinder;
import android.os.Messenger;
import android.service.media.MediaBrowserService;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.List;

import static android.util.Log.e;
import static android.util.Log.i;
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
    String currentTrackName;
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    Messenger mMessenger;

    /**
     * Command to the service to display a message
     */


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
        // TODO Send token
        Intent tokenIntent = new Intent();
        tokenIntent.putExtra("sessionToken", mediaSessionToken);
        tokenIntent.setType("tokenType");
        String tokenType = tokenIntent.getType();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints)
    {
        return null;
    }

    protected void setNotification(String songTitle)
    {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        // Open activity when clicking on notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        // Get audio URI and Attributes for channel
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Now playing: " + songTitle) //TODO set string
                .setContentText(songTitle)
                .setSmallIcon(R.drawable.ic_home_black_24dp)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        startForeground(1, notification); // Notification Identifier >0; for updating notification

    }

    /***
     * Play audio file
     * @param uri Audio file path
     */
    protected void initPlayer(Uri uri)
    {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.setVolume(50, 50);
        } catch (Exception exception) {
            e("startPlayer", exception.getMessage());
        }
    }

    public void startPlayer()
    {
        if (!mediaPlayer.isPlaying()) {
            setNotification(currentTrackName);
            mediaPlayer.start();
        }
    }

    public void stopPlayer()
    {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void onDestroy()
    {
        mediaSession.release(); // clean up the session and notify any controllers
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Uri inputUri = intent.getParcelableExtra("inputUri");
        currentTrackName = inputUri.getPath();
        initPlayer(inputUri);

        i("onBind", "");
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        mMessenger = new Messenger(new IncomingHandler(this));
        return mMessenger.getBinder();
    }


    /**
     * @param intent Intent that was used to bind to this service
     * @return false - No onRebind(Intent) method later called when new clients bind to it
     */
    @Override
    public boolean onUnbind(Intent intent)
    {
        mediaPlayer.release();
        return false;
    }
}