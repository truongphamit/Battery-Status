package org.truongpq.batterystatus.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import org.truongpq.batterystatus.AppWidget;
import org.truongpq.batterystatus.MainActivity;
import org.truongpq.batterystatus.R;
import org.truongpq.batterystatus.Tools;

public class BatteryService extends Service {
    public static final int NOTIFICATION_ID = 1;

    private final IBinder mBinder = new LocalBinder();

    public BatteryService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryInfoReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryInfoReceiver);
        removeNotification();
    }

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
            views.setTextViewText(R.id.tv_status, Tools.getBatteryState(status));
            views.setTextViewText(R.id.tv_health, Tools.getHealth(health));
            views.setTextViewText(R.id.tv_level, level + "%");
            views.setTextViewText(R.id.tv_temp, temperature / 10.0 + " \u2103");
            views.setTextViewText(R.id.tv_vol, voltage / 1000.0 + " V");
            AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, AppWidget.class), views);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (preferences.getBoolean("key_notification", false)) {
                createNotification(NOTIFICATION_ID, R.drawable.ic_battery, Tools.getBatteryState(status) + ": " + level + "%", temperature / 10.0 + " \u2103" + "    " + voltage / 1000.0 + " V");
            } else {
                removeNotification();
            }
        }
    };

    private void createNotification(int nId, int iconRes, String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(iconRes)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setOngoing(true)
                .setContentIntent(pIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(nId, mBuilder.build());
    }

    private void removeNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    public void update() {
        unregisterReceiver(batteryInfoReceiver);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryInfoReceiver, intentFilter);
    }

    public class LocalBinder extends Binder {
        public BatteryService getService() {
            return BatteryService.this;
        }
    }
}