package com.example.music_player;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import static android.util.Log.i;

public class PlayerConnection implements ServiceConnection
{
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        i("onServiceConnected", "CONNECTED");
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {

    }
}