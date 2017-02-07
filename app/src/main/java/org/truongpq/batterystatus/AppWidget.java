package org.truongpq.batterystatus;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.RemoteViews;

import org.truongpq.batterystatus.services.BatteryService;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    boolean bound = false;
    private BatteryService batteryService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bound = true;
            BatteryService.LocalBinder binder = (BatteryService.LocalBinder) service;
            batteryService = binder.getService();

            batteryService.update();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            batteryService = null;
            bound = false;
        }
    };

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
//        views.setTextViewText(R.id.tv_status, Tools.getBatteryState(status));

        Intent launchMain = new Intent(context, MainActivity.class);
        PendingIntent pendingMainIntent = PendingIntent.getActivity(context, 0, launchMain, 0);
        views.setOnClickPendingIntent(R.id.root, pendingMainIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        if (!Tools.isServiceRunning(context, BatteryService.class)) {
            context.startService(new Intent(context, BatteryService.class));
        }

        context.getApplicationContext().bindService(new Intent(context, BatteryService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        // Unbind from the service
        if (bound && batteryService != null) {
            context.unbindService(serviceConnection);
            bound = false;
        }
    }
}

