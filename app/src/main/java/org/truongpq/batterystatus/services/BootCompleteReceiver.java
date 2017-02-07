package org.truongpq.batterystatus.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.truongpq.batterystatus.Tools;

public class BootCompleteReceiver extends BroadcastReceiver {
    public BootCompleteReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Tools.isServiceRunning(context, BatteryService.class)) {
            context.startService(new Intent(context, BatteryService.class));
        }
    }
}
