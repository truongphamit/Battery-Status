package org.truongpq.batterystatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.BinderThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;

import org.truongpq.batterystatus.services.BatteryService;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.tv_level)
    TextView tv_level;

    @BindView(R.id.tv_status)
    TextView tv_status;

    @BindView(R.id.tv_plugger)
    TextView tv_plugger;

    @BindView(R.id.tv_health)
    TextView tv_health;

    @BindView(R.id.tv_model)
    TextView tv_model;

    @BindView(R.id.tv_build)
    TextView tv_build;

    @BindView(R.id.tv_version)
    TextView tv_version;

    @BindView(R.id.tv_tech)
    TextView tv_tech;

    @BindView(R.id.tv_temp)
    TextView tv_temp;

    @BindView(R.id.tv_vol)
    TextView tv_vol;

    @BindView(R.id.tv_Capacity)
    TextView tv_Capacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NativeExpressAdView adView = (NativeExpressAdView)findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder()
                .build();
        adView.loadAd(request);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tools.openBatteryUsagePage(MainActivity.this);
            }
        });

        if (!Tools.isServiceRunning(this, BatteryService.class)) {
            startService(new Intent(this, BatteryService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_rate_us) {
            Tools.openRateUs(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void init() {
        this.registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
            String technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

            double maxMa = Tools.getBatteryCapacity(MainActivity.this);
            tv_level.setText(level + "%");
            tv_Capacity.setText((int) maxMa + " mA");
            tv_status.setText(Tools.getBatteryState(status));
            tv_plugger.setText(Tools.getHowCharging(plugged));
            tv_health.setText(Tools.getHealth(health));
            tv_model.setText(Tools.getDeviceName());
            tv_build.setText(Tools.getOSBuildNumber());
            tv_version.setText(Build.VERSION.RELEASE);
            tv_tech.setText(technology);
            tv_temp.setText(temperature / 10.0 + " \u2103");
            tv_vol.setText(voltage / 1000.0 + " V");
        }
    };

    @Override
    public void onBackPressed() {
        Tools.showConfirmDialog(this, getString(R.string.confirm_title), getString(R.string.confirm_message), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Tools.openRateUs(MainActivity.this);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(batteryInfoReceiver);
    }
}
