package com.manimahler.android.scheduler3g;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DelayStopBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try
		{
			int periodId = intent.getExtras().getInt(
					context.getString(R.string.period_id), -4);
			
			Log.d("DelayStopBroadcastReceiver", "Received delay broadcast for id " + periodId);
			
			// check if really needed:
			NotificationManager notificationManager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			
			notificationManager.cancel(periodId);
			
			NetworkScheduler scheduler = new NetworkScheduler();
			
			scheduler.cancelSwitchOff(context, "OFF");
			
			scheduler.scheduleSwitchOff(context, 36, "OFF_DELAYED", periodId);	
		}
		catch (Exception e)
		{
			Log.e("DelayStopBroadcastReceiver", "Error delaying switch off", e);
		}


		
	}

}
