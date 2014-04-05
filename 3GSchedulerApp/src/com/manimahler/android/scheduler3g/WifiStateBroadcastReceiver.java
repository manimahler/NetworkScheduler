package com.manimahler.android.scheduler3g;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiStateBroadcastReceiver extends BroadcastReceiver {

	int ACTION_DISABLED = WifiManager.WIFI_STATE_DISABLED;
	
	// TODO: probably not a good idea because this does not work for
	//		 mobile data!
	
	public static AtomicBoolean ChangingWifiState = new AtomicBoolean(); 

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Log.d("NetworkStateBroadcastReceiver",
					"Received network change action " + intent.getAction());
			
			int currentState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,	-1);
			
			if (currentState != WifiManager.WIFI_STATE_ENABLED
					&& currentState != WifiManager.WIFI_STATE_DISABLED) {
				Log.d("NetworkStateBroadcastReceiver",
				"Ignoring wifi state " + currentState);
				return;
			}
			
			if (ChangingWifiState.get()) {
				Log.d("NetworkStateBroadcastReceiver",
						"Wifi change caused by Network Scheduler. Doing nothing. State " + currentState);
				ChangingWifiState.set(false);
				return;
			}

			Log.d("NetworkStateBroadcastReceiver", "Wifi state considered changed by user!");

			if (currentState == WifiManager.WIFI_STATE_ENABLED
					|| currentState == WifiManager.WIFI_STATE_DISABLED) {
				// user action, notify the period to stop toggling
				setManualOverride(context);
			}

		} catch (Exception e) {
			Log.e("NetworkStateBroadcastReceiver",
					"Error handling network state change", e);
		}
	}

	private void setManualOverride(Context context) {
		NetworkScheduler networkScheduler = new NetworkScheduler();

		SharedPreferences schedulesPreferences = PersistenceUtils.getSchedulesPreferences(context);

		ArrayList<ScheduledPeriod> periods = PersistenceUtils
				.readFromPreferences(schedulesPreferences);
		
		SchedulerSettings settings = PersistenceUtils.readSettings(context);

		for (ScheduledPeriod period : periods) {
			if (period.isIntervalConnectingWifi()) {
				period.set_overrideIntervalWifi(true);
				PersistenceUtils
						.saveToPreferences(schedulesPreferences, period);
				
				networkScheduler.setupIntervalConnect(context, settings);
			}
		}
	}

}
