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

		ArrayList<EnabledPeriod> enabledPeriods = PersistenceUtils
				.readFromPreferences(prefs);

		for (EnabledPeriod enabledPeriod : enabledPeriods) {

			if (enabledPeriod.useIntervalConnectMobileData()) {
				// just toggle on, it will be switched off automatically by the
				// interval alarm
				Log.d(TAG, "Switching on mobile data for active period "
						+ enabledPeriod.get_name());

				ConnectionUtils.toggleMobileData(context, true);
			}

			if (enabledPeriod.useIntervalConnectWifi()) {
				// just toggle on, it will be switched off automatically by the
				// interval alarm
				Log.d(TAG, "Switching on wifi for active period "
						+ enabledPeriod.get_name());

				ConnectionUtils.toggleWifi(context, true);
			}
		}

	}
}
