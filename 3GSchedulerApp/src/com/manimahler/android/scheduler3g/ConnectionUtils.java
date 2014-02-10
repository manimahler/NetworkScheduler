package com.manimahler.android.scheduler3g;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
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

	private static void toggleWifi(Context context, boolean enable) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(enable);

		// boolean wifiEnabled = wifiManager.isWifiEnabled();
	}

	private static void toggleBluetooth(Context context, boolean enable) {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		if (adapter != null) {
			if (adapter.getState() == BluetoothAdapter.STATE_ON) {

				if (enable) {
					Log.d("Bluetooth", "Bluetooth already enabled");
				} else {
					adapter.disable();
				}

			} else if (adapter.getState() == BluetoothAdapter.STATE_OFF) {

				if (enable) {
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
	private static void toggleMobileData(Context context, boolean enable)
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

		Log.d("StartStopBroadcastReceiver",
				"Switching mobile data ON status to " + enable);

		setMobileDataEnabledMethod.setAccessible(true);
		setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);
	}
}
