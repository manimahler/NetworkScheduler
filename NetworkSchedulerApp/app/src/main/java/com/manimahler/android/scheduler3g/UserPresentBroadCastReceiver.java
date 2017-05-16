package com.manimahler.android.scheduler3g;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UserPresentBroadCastReceiver extends BroadcastReceiver {

	private static final String TAG = UserPresentBroadCastReceiver.class
			.getSimpleName();

	private final String USER_PRESENT_ACTION = "android.intent.action.USER_PRESENT";

	@Override
	public void onReceive(Context context, Intent intent) {
		try {

			String action = intent.getAction();

			Log.d(TAG, "Receiving action " + action);

			if (action.equals(USER_PRESENT_ACTION)) {
				startSensorsInIntervalConnect(context);
			}
		} catch (Exception e) {
			Log.e(USER_PRESENT_ACTION, "Error handling user-present event", e);
		}
	}

	private void startSensorsInIntervalConnect(Context context)
			throws Exception {

		SchedulerSettings settings = PersistenceUtils.readSettings(context);

		if (settings.is_globalOn()) {
			
			NetworkScheduler scheduler = new NetworkScheduler();

			scheduler.intervalSwitchOnDueToUnlock(context, settings);
		}
	}
}
