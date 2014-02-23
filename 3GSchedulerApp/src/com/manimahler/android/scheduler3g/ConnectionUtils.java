package com.manimahler.android.scheduler3g;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ConnectionUtils {
	
	public static void toggleNetworkState(Context context,
			EnabledPeriod enabledPeriod, boolean enable)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {

		if (enabledPeriod.is_mobileData()) {
			toggleMobileData(context, enable);
		}

		if (enabledPeriod.is_wifi()) {
			toggleWifi(context, enable);
		}

		if (enabledPeriod.is_bluetooth()) {
			toggleBluetooth(context, enable);
		}
	}
	

	public static boolean isBluetoothOn() {
		
		boolean isBluetoothEnabled = false;
		
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		if (adapter != null) {
			if (adapter.getState() == BluetoothAdapter.STATE_ON) {
				
				Log.d("ConnectionUtils", "bluetooth state: " + adapter.getState());

				isBluetoothEnabled = true;
				// TODO: differentiate between STATE_ON and STATE_CONNECTED,
				// etc. -> settings to force off despite connected
				// if (sensors.length() > 0) {
				// sensors += ", ";
				// }
				//
				// sensors += context.getString(R.string.bluetooth);
			}
		}
		
		return isBluetoothEnabled;
	}
	
	public static boolean isWifiOn(Context context) {
		
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		boolean wifiEnabled = wifiManager.isWifiEnabled();

		return wifiEnabled;
	}
	
	public static boolean isWifiConnected(Context context)
	{
	ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

	return (mWifi.isConnected()) ;
	}
	
	public static boolean isMobileDataOn(Context context) {
		
		boolean mobileDataEnabled = false;
		
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
			mobileDataEnabled = true;
			// sensors = context.getString(R.string.mobile_data);
		}
		
		return mobileDataEnabled;
	}

	public static void toggleWifi(Context context, boolean enable) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		
		Log.d("ConnectionUtils", "Switching WIFI ON status to " + enable);
		
		wifiManager.setWifiEnabled(enable);
	}

	public static void toggleBluetooth(Context context, boolean enable) {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		if (adapter != null) {
			if (adapter.getState() == BluetoothAdapter.STATE_ON) {

				if (enable) {
					Log.d("Bluetooth", "Bluetooth already enabled");
				} else {
					Log.d("ConnectionUtils", "Switching BT ON status to " + enable);
					adapter.disable();
				}

			} else if (adapter.getState() == BluetoothAdapter.STATE_OFF) {

				if (enable) {
					Log.d("ConnectionUtils", "Switching BT ON status to " + enable);
					adapter.enable();
				} else {
					Log.d("Bluetooth", "Bluetooth already disabled");
				}
			} else {
				// State.INTERMEDIATE_STATE;
			}
		}
	}

	// found on the Internet and adapted slightly
	public static void toggleMobileData(Context context, boolean enable)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {

		final ConnectivityManager conman = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final Class<?> conmanClass = Class.forName(conman.getClass().getName());
		final Field iConnectivityManagerField = conmanClass
				.getDeclaredField("mService");
		iConnectivityManagerField.setAccessible(true);
		final Object iConnectivityManager = iConnectivityManagerField
				.get(conman);
		final Class<?> iConnectivityManagerClass = Class
				.forName(iConnectivityManager.getClass().getName());
		final Method setMobileDataEnabledMethod = iConnectivityManagerClass
				.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);

		Log.d("ConnectionUtils",
				"Switching mobile data ON status to " + enable);

		setMobileDataEnabledMethod.setAccessible(true);
		setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);
	}
}
