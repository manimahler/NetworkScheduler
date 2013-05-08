package com.manimahler.android.scheduler3g;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class StartStopBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Bundle bundle = intent.getExtras();
		
		boolean on = bundle.getBoolean("Action3gOn");
		
		try {
			toggleMobileData(context, on);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			Toast.makeText(context, "Error changing 3g setting", Toast.LENGTH_SHORT).show();
		}
	}
	
	/// found on the Internet and adapted slightly
	private void toggleMobileData(Context context, boolean enable) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
	    final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    final Class<?> conmanClass = Class.forName(conman.getClass().getName());
	    final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
	    iConnectivityManagerField.setAccessible(true);
	    final Object iConnectivityManager = iConnectivityManagerField.get(conman);
	    final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
		final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		makeDataEnableToast(context, enable, telephonyManager);
		
	    setMobileDataEnabledMethod.setAccessible(true);
	    setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);
	}
	
	private void makeDataEnableToast(Context context, boolean enable,
			TelephonyManager telephonyManager) {
		if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED)
		{
			if (enable)
			{
				Toast.makeText(context, "3G Mobile Data Scheduler: Data access over mobile network is alredy enabled", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(context, "3G Mobile Data Scheduler: Switching off data access over mobile network", Toast.LENGTH_LONG).show();
			}
		}
		else if (telephonyManager.getDataState() == TelephonyManager.DATA_DISCONNECTED)
		{
			if (enable)
			{
				Toast.makeText(context, "3G Mobile Data Scheduler: Switching on data access over mobile network", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(context, "3G Mobile Data Scheduler: Data access over mobile network is alredy disabled", Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			Toast.makeText(context, "3G Mobile Data Scheduler: Unexpected state of mobile data. Please report.", Toast.LENGTH_LONG).show();
		}
	}
}
