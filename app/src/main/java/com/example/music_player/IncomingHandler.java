package com.example.music_player;

import android.os.Handler;
import android.os.Message;

/**
 * Handler of incoming messages from clients.
 */
class IncomingHandler extends Handler
{
    static final int MESSAGE_TOGGLE_PLAYER = 2;
    private final PlayerService playerService;

    public IncomingHandler(PlayerService context)
    {
        playerService = context;
    }

    @Override
    public void handleMessage(Message msg)
    {
        if (msg.what == MESSAGE_TOGGLE_PLAYER) {
            playerService.togglePlayer();
        } else {
            super.handleMessage(msg);
        }
    }
}