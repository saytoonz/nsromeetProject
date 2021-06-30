package com.sayt.godslove.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Broadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, BG_Service.class);
        context.startService(service);

    }
}
