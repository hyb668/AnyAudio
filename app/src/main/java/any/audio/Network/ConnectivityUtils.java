package any.audio.Network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * Created by Ankit on 8/5/2016.
 */
public class ConnectivityUtils {

    private static Context context;
    private static ConnectivityUtils mInstance;

    public ConnectivityUtils(Context context) {
        ConnectivityUtils.context = context;
    }

    public static ConnectivityUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ConnectivityUtils(context);
        }
        return mInstance;
    }

    public static boolean isConnectedToNet() {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo mobileData = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        final android.net.NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mobileData.isConnected()) {
            return true;
        } else if (wifi.isConnected()) {
            return true;
        }
        return false;
    }

}