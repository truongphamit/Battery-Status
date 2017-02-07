package org.truongpq.batterystatus;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by truongpq on 1/18/17.
 */

public class Tools {
    // Max Mah
    public static double getBatteryCapacity(Context context) {
        Object mPowerProfile_ = null;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            double batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
            return batteryCapacity;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static String getBatteryState(int status) {
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                return "Charging";
            case BatteryManager.BATTERY_STATUS_FULL:
                return "Full";
            default:
                return "Discharging";
        }
    }

    public static String getHowCharging(int chargePlug) {
        switch (chargePlug) {
            case BatteryManager.BATTERY_PLUGGED_USB:
                return "USB charger";
            case BatteryManager.BATTERY_PLUGGED_AC:
                return "AC charger";
            default:
                return "On Battery";
        }
    }

    public static String getHealth(int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_COLD:
                return "Cold";
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return "Dead";
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return "Good";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return "Over Voltage";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return "Overheat";
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return "Unspecified Failure";
            default:
                return "Unknown";
        }
    }

    /**
     * Returns the consumer friendly device name
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    public static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

//        String phrase = "";
        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
//                phrase += Character.toUpperCase(c);
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
//            phrase += c;
            phrase.append(c);
        }

        return phrase.toString();
    }

    public static String getOSBuildNumber() {
        String osBuildNumber = Build.FINGERPRINT;  //"google/shamu/shamu:5.1.1/LMY48Y/2364368:user/release-keys”
        final String forwardSlash = "/";
        String osReleaseVersion = Build.VERSION.RELEASE + forwardSlash;
        try {
            osBuildNumber = osBuildNumber.substring(osBuildNumber.indexOf(osReleaseVersion));  //"5.1.1/LMY48Y/2364368:user/release-keys”
            osBuildNumber = osBuildNumber.replace(osReleaseVersion, "");  //"LMY48Y/2364368:user/release-keys”
            osBuildNumber = osBuildNumber.substring(0, osBuildNumber.indexOf(forwardSlash)); //"LMY48Y"
        } catch (Exception e) {
            Log.e("getOSBuildNumber", "Exception while parsing - " + e.getMessage());
        }

        return osBuildNumber;
    }

    public static void openBatteryUsagePage(Context ctx) {
        Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
        ResolveInfo resolveInfo = ctx.getPackageManager().resolveActivity(powerUsageIntent, 0);
        // check that the Battery app exists on this device
        if (resolveInfo != null) {
            ctx.startActivity(powerUsageIntent);
        }
    }

    public static void openRateUs(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + context.getPackageName()));
        context.startActivity(intent);
    }

    public static void showConfirmDialog(Context c,String title, String message, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(c);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(R.string.no_thanks, positiveListener);
        alertDialogBuilder.setNegativeButton(R.string.rate_now, negativeListener);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
