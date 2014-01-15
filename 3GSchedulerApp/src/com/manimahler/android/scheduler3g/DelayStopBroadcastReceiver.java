package com.manimahler.android.scheduler3g;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DelayStopBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d("DelayStopBroadcastReceiver", "Receiving delay broadcast");

		try
		{
			// check if really needed:
			NotificationManager notificationManager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			
			notificationManager.cancel(17);
			
			AlarmHandler ah = new AlarmHandler();
			
			ah.cancelSwitchOff(context, "OFF");
			
			ah.scheduleSwitchOff(context, 36, "OFF_DELAYED");	
		}
		catch (Exception e)
		{
			Log.e("DelayStopBroadcastReceiver", "Error delaying switch off", e);
		}


		
	}

}
