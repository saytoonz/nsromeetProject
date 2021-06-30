package com.nsromapa.nsromeet.services;

import android.content.Context;
import android.content.Intent;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, BackgroundService.class);
        context.startService(i);
    }
}
