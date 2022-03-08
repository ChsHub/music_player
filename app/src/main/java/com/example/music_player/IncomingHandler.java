package com.example.music_player;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * Handler of incoming messages from clients.
 */
class IncomingHandler extends Handler
{
    static final int MESSAGE_START_PLAYER = 2;
    static final int MESSAGE_STOP_PLAYER = 3;
    private final PlayerService playerService;

    IncomingHandler(PlayerService context)
    {
        playerService = context;
    }

    @Override
    public void handleMessage(Message msg)
    {
        switch (msg.what) {
            case MESSAGE_START_PLAYER:
                playerService.startPlayer();
                break;
            case MESSAGE_STOP_PLAYER:
                playerService.stopPlayer();
                break;
            default:
                super.handleMessage(msg);
        }
    }
}