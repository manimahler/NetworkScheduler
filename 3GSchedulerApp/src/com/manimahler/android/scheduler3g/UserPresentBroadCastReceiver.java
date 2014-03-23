package com.manimahler.android.scheduler3g;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class UserPresentBroadCastReceiver extends BroadcastReceiver {

	private static final String TAG = "UserPresentBroadCastReceiver";

	private final String USER_PRESENT_ACTION = "android.intent.action.USER_PRESENT";

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			
			String action = intent.getAction();
			
			Log.d(TAG, "Receiving action " + action);
			
			if (action.equals(USER_PRESENT_ACTION)) {

				Log.d(TAG, "Receiving action USER PRESENT");

				StartSensorsInIntervalConnect(context);
			}
		} catch (Exception e) {
			Log.e(USER_PRESENT_ACTION, "Error handling user-present event", e);
		}
	}

	private void StartSensorsInIntervalConnect(Context context)
			throws Exception {
		NetworkScheduler alarmHandler = new NetworkScheduler();

		SharedPreferences prefs = alarmHandler.getSchedulesPreferences(context);

		ArrayList<ScheduledPeriod> periods = PersistenceUtils
				.readFromPreferences(prefs);
		
		boolean wifiOn = false;
		boolean mobDataOn = false;

		for (ScheduledPeriod enabledPeriod : periods) {

			if (enabledPeriod.isIntervalConnectingWifi()) {
				// just toggle on, it will be switched off automatically by the
				// interval alarm
				Log.d(TAG, "Switching on wifi for active period "
						+ enabledPeriod.get_name());

				wifiOn = true;
			}
			
			if (enabledPeriod.isIntervalConnectingMobileData()) {
				// just toggle on, it will be switched off automatically by the
				// interval alarm
				Log.d(TAG, "Switching on mobile data for active period "
						+ enabledPeriod.get_name());

				mobDataOn = true;
			}
		}
		
		if (wifiOn)
		{
			ConnectionUtils.toggleWifi(context, true);
			
			// NOTE: It would be possible to wait here too for a little while to avoid 
			// 		 connecting mobile data first but the user might not want to wait as long as it takes...
		}

		if (mobDataOn)
		{
			ConnectionUtils.toggleMobileData(context, true);
		}
	}
}
