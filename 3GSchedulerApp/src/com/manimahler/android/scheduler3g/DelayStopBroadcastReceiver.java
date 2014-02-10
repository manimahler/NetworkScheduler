package com.manimahler.android.scheduler3g;

import java.util.Date;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class DelayStopBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try
		{
			int periodId = intent.getExtras().getInt(
					context.getString(R.string.period_id), -4);
			
			String action = intent.getAction();
			Log.d("DelayStopBroadcastReceiver", "Received delay broadcast for action " + action + " and period id " + periodId);
			
			// check if really needed:
			NotificationManager notificationManager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			
			notificationManager.cancel(periodId);
			
			NetworkScheduler scheduler = new NetworkScheduler();
			
			scheduler.cancelSwitchOff(context, "OFF");
			
			if (action.equals("SKIP"))
			{
				Toast.makeText(context, "Switch off skipped today", Toast.LENGTH_LONG).show();
				//do nothing
				return;
			}
			
			// TODO
			int delayInSec = 36;
			
			scheduler.scheduleSwitchOff(context, delayInSec, "OFF_DELAYED", periodId);
			
			long delayTime = DateTimeUtils.getTimeFromNowInMillis(delayInSec);
			
			String timeFormat = DateFormat.getTimeFormat(context).format(new Date(delayTime));
			String toastText = String.format("Switch off delayed until %s", timeFormat);
			
			Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
		}
		catch (Exception e)
		{
			Log.e("DelayStopBroadcastReceiver", "Error delaying switch off", e);
		}


		
	}

}
