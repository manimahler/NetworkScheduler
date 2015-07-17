package com.manimahler.android.scheduler3g;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Assert;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ConnectionUtils {

	private static final String TAG = ConnectionUtils.class.getSimpleName();

	private static Boolean _canToggleMobileData = null;

	public static void toggleNetworkState(Context context,
			ScheduledPeriod enabledPeriod, boolean enable) {

		if (enabledPeriod.is_mobileData()) {
			toggleMobileData(context, enable);
		}

		if (enabledPeriod.is_wifi()) {
			toggleWifi(context, enable);
		}

		if (enabledPeriod.is_bluetooth()) {
			toggleBluetooth(context, enable);
		}

		if (enabledPeriod.is_volume()) {
			toggleVolume(context, enable, enabledPeriod.is_vibrateWhenSilent());
		}
	}

	public static void toggleVolume(Context context, boolean enable,
			boolean vibrateWhenSilent) {
		AudioManager audiomanager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);

		if (enable) {
			Log.d(TAG, "Setting ringer mode to normal");
			UserLog.log(context, "Setting ringer mode to normal");
			audiomanager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		} else if (vibrateWhenSilent) {
			Log.d(TAG, "Setting ringer mode to vibrate");
			UserLog.log(context, "Setting ringer mode to vibrate");
			audiomanager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		} else {
			Log.d(TAG, "Setting ringer mode to silent");
			UserLog.log(context, "Setting ringer mode to silent");
			audiomanager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		}
	}

	public static boolean isVolumeOn(Context context) {
		AudioManager audiomanager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);

		return audiomanager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;

	}

	public static boolean isRingerModeNormal(Context context) {
		AudioManager audiomanager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);

		return audiomanager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
	}

	public static boolean isBluetoothOn() {

		boolean isBluetoothEnabled = false;

		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		if (adapter != null) {
			if (adapter.getState() == BluetoothAdapter.STATE_ON) {

				Log.d(TAG, "bluetooth state: " + adapter.getState());

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

	public static boolean isWifiConnected(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		return (mWifi.isConnected());
	}

	public static boolean isTethering(Context context) {

		boolean isWifiAPenabled = false;
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		Method[] wmMethods = wifi.getClass().getDeclaredMethods();
		for (Method method : wmMethods) {
			if (method.getName().equals("isWifiApEnabled")) {

				try {
					isWifiAPenabled = (Boolean) method.invoke(wifi);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return isWifiAPenabled;
	}

	public static boolean isMobileDataOn(Context context) {

		// From
		// http://stackoverflow.com/questions/12806709/android-how-to-tell-if-mobile-network-data-is-enabled-or-disabled-even-when
		boolean mobileDataEnabled = false; // Assume disabled
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			Class<?> cmClass = Class.forName(cm.getClass().getName());
			Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
			method.setAccessible(true); // Make the method callable
			// get the setting for "mobile data"
			mobileDataEnabled = (Boolean) method.invoke(cm);
		} catch (Exception e) {
			// Some problem accessible private API
			// TODO do whatever error handling you want here
			Log.e(TAG, "Error getting mobile data state");
			e.printStackTrace();
		}

		return mobileDataEnabled;

		// This does not return the correct answer when WiFi is connected
		// boolean mobileDataEnabled = false;
		//
		// TelephonyManager telephonyManager = (TelephonyManager) context
		// .getSystemService(Context.TELEPHONY_SERVICE);
		//
		// int dataState = telephonyManager.getDataState();
		//
		// Log.d("ConnectionUtils", "Data state is " + dataState);
		//
		// if (dataState == TelephonyManager.DATA_CONNECTED) {
		// mobileDataEnabled = true;
		// // sensors = context.getString(R.string.mobile_data);
		// }
		//
		// return mobileDataEnabled;
	}

	public static void toggleWifi(Context context, boolean enable) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		Log.d(TAG, "Switching WIFI ON status to " + enable);

		int wifiState = wifiManager.getWifiState();

		Log.d(TAG, "Current Wi-Fi state is " + wifiState);

		if (enable && !wifiManager.isWifiEnabled()) {
			UserLog.log(context, "Enabling Wi-Fi");
			wifiManager.setWifiEnabled(enable);
		}

		if (!enable && wifiManager.isWifiEnabled()) {
			UserLog.log(context, "Disabling Wi-Fi");
			wifiManager.setWifiEnabled(enable);
		}

		// This entire carefulness did not really resolve the KITKAT bug and
		// produced
		// issues on earlier versions (e.g. unable to disconnect on Gingerbread)
		//
		// if (enable && !wifiManager.isWifiEnabled()) {
		//
		// if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
		// setWifiEnabled(wifiManager, enable);
		// } else if (wifiState == WifiManager.WIFI_STATE_UNKNOWN) {
		// if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
		// // observed once on SGS3: Wifi stayed in in unknown state
		// // for quite a while (but toggling worked)
		// Log.w("ConnectionUtils",
		// "Wifi state is WIFI_STATE_UNKNOWN, trying to enable...");
		// setWifiEnabled(wifiManager, enable);
		// } else {
		// // take extra care not to trigger the Wifi-Bug on KitKat
		// Log.w("ConnectionUtils",
		// "Wifi state is WIFI_STATE_UNKNOWN, doing nothing...");
		// }
		// } else {
		// Log.d("ConnectionUtils",
		// "Wifi state is not disabled, not enabling!");
		// }
		// }
		//
		//
		// if (!enable && wifiManager.isWifiEnabled()) {
		// if (!isWifiConnected(context) || wifiManager.disconnect()) {
		// setWifiEnabled(wifiManager, enable);
		// } else {
		// Log.w("ConnectionUtils",
		// "Cannot disconnect from WiFi. Not switching off!");
		// }
		// } else if (! enable) {
		// Log.d("ConnectionUtils",
		// "Wifi state is not enabled, not disabling!");
		// }
	}

	//
	// private static void setWifiEnabled(WifiManager wifiManager, boolean
	// enable) {
	//
	//
	// Log.d("ConnectionUtils", "Setting Wifi state change flag");
	//
	// // NOTE: setWifiEnabled returns long before the broadcast from the system
	// // is received. So there is no point re-setting the flag in the finally
	// clause here.
	// WifiStateBroadcastReceiver.ChangingWifiState.set(true);
	// boolean success = wifiManager.setWifiEnabled(enable);
	//
	// Log.d("ConnectionUtils", "Wifi toggle success: " + success);
	//
	// }

	public static void toggleBluetooth(Context context, boolean enable) {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		if (adapter != null) {
			if (adapter.getState() == BluetoothAdapter.STATE_ON) {

				if (enable) {
					Log.d(TAG, "Bluetooth already enabled");
					UserLog.log(context, "Bluetooth already enabled");
				} else {
					Log.d(TAG, "Switching BT ON status to " + enable);
					UserLog.log(context, "Disabling Bluetooth");
					adapter.disable();
				}

			} else if (adapter.getState() == BluetoothAdapter.STATE_OFF) {

				if (enable) {
					Log.d(TAG, "Switching BT ON status to " + enable);
					UserLog.log(context, "Enabling Bluetooth");
					adapter.enable();
				} else {
					Log.d(TAG, "Bluetooth already disabled");
					UserLog.log(context, "Bluetooth already disabled");
				}
			} else {
				// State.INTERMEDIATE_STATE;
			}
		}
	}

	public static boolean canToggleMobileData(Context context) {

		if (_canToggleMobileData != null) {
			return _canToggleMobileData;
		}

		// Determine if mobile data is available
		boolean hasMobileDataSensor = hasMobileDataSensor(context);

		boolean result;

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			result = hasMobileDataSensor;
		} else {
			result = hasMobileDataSensor
					&& RootCommandExecuter.canRunRootCommands();
		}

		_canToggleMobileData = result;

		return result;
	}

	public static boolean hasMobileDataSensor(Context context) {

		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_TELEPHONY)) {
			return true;
		}

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = cm
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (networkInfo == null) {
			UserLog.log(context, "No mobile data sensor available");
			return false;
		}

		return true;
	}

	public static void toggleMobileData(Context context, boolean enable) {

		if (!canToggleMobileData(context)) {
			UserLog.log(context,
					"Cannot enable/disable mobile data due to lacking privileges (or no 3G sensor)");
			return;
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			toggleMobileDataPreLollipop(context, enable);
		} else {
			try {
				setMobileDataStateLollipop(context, enable);
			} catch (Exception ex) {
				Log.e(TAG, "Failed to toggle mobile data state as system app",
						ex);
				UserLog.log(context,
						"Failed to toggle mobile data state as system app: "
								+ ex.toString());
			}
		}

	}

	public static void toggleMobileDataPreLollipop(Context context,
			boolean enable) {

		Log.d(TAG, "Switching mobile data ON status to " + enable);

		try {
			final ConnectivityManager conman = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final Class<?> conmanClass = Class.forName(conman.getClass()
					.getName());
			final Field iConnectivityManagerField = conmanClass
					.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			final Object iConnectivityManager = iConnectivityManagerField
					.get(conman);
			final Class<?> iConnectivityManagerClass = Class
					.forName(iConnectivityManager.getClass().getName());
			final Method setMobileDataEnabledMethod = iConnectivityManagerClass
					.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);

			setMobileDataEnabledMethod.setAccessible(true);

			if (enable) {
				UserLog.log(context, "Enabling Mobile Data");
			} else {
				UserLog.log(context, "Disabling Mobile Data");
			}

			setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);

		} catch (Exception e) {
			Log.e(TAG, "Error switching mobile data ", e);
			UserLog.log(context, "Error changing mobile data state.");
		}
	}

	private static void setMobileDataStateLollipop(Context context,
			boolean mobileDataEnabled) {
		try {
			
			// Fix crash reported in developer console (Android 5.0) security exception, somehow not caught by the exception below
			if (RootCommandExecuter.canRunRootCommands())
			{
				setMobileDataStateLollipopCommandlineAsRoot(context, mobileDataEnabled);
			}
			else if (checkCanToggleMobileDataAsSystemAppPermission(context))
			{
				// it could have been made a system app...
				setMobileDataStateLollipopAsSystemApp(context, mobileDataEnabled);
			}
			else {
				UserLog.log(context, "Unable to toggle mobile data. Network Scheduler is not allowed to issue root commands and is no system app");
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error setting mobile data state.", ex);
			UserLog.log(context, "Error toggling mobile data", ex);
		}
	}
	
	private static boolean checkCanToggleMobileDataAsSystemAppPermission(Context context)
	{
	    String permission = "android.permission.MODIFY_PHONE_STATE";
	    
	    int result = context.checkCallingOrSelfPermission(permission);
	    
	    return (result == PackageManager.PERMISSION_GRANTED);         
	}

	private static void setMobileDataStateLollipopAsSystemApp(Context context,
			boolean mobileDataEnabled) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		TelephonyManager telephonyService = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		Method setMobileDataEnabledMethod = telephonyService.getClass()
				.getDeclaredMethod("setDataEnabled", boolean.class);

		Assert.assertNotNull("setMobileDataEnabledMethod not found",
				setMobileDataEnabledMethod);

		setMobileDataEnabledMethod.invoke(telephonyService,
				mobileDataEnabled);

		if (mobileDataEnabled) {
			UserLog.log(context, "Enabled Mobile Data as system app");
		} else {
			UserLog.log(context, "Disabled Mobile Data as system app");
		}
	}

	private static void setMobileDataStateLollipopCommandlineAsRoot(
			Context context, boolean enable) {
		try {
			StringBuilder command = new StringBuilder();
			command.append("su -c ");
			command.append("\"svc data ");

			command.append(enable ? "enable" : "disable");
			command.append("\"\n");

			Log.i(TAG, "Executing on the command line: " + command.toString());

			int returnValue = RootCommandExecuter.execute(command.toString());

			if (returnValue != 0) {
				UserLog.log(
						context,
						"Error setting mobile data state using root command line. Make sure root privilege is granted. Return value: "
								+ returnValue);
			} else {
				UserLog.log(context,
						"Toggled mobile data using root command line.");
			}
		} catch (Exception e) {
			Log.e(TAG, "Error changing mobile data on command line", e);
			UserLog.log(
					context,
					"Error changing mobile data on root command line. Make sure root privilege is granted.");
		}
	}

	//
	// public boolean getMobileDataState(Context context)
	// {
	// try
	// {
	// TelephonyManager telephonyService = (TelephonyManager)
	// context.getSystemService(Context.TELEPHONY_SERVICE);
	//
	// Method getMobileDataEnabledMethod =
	// telephonyService.getClass().getDeclaredMethod("getDataEnabled");
	//
	// if (null != getMobileDataEnabledMethod)
	// {
	// boolean mobileDataEnabled = (Boolean)
	// getMobileDataEnabledMethod.invoke(telephonyService);
	//
	// return mobileDataEnabled;
	// }
	// }
	// catch (Exception ex)
	// {
	// Log.e(TAG, "Error getting mobile data state", ex);
	// }
	//
	// return false;
	// }
	//

	//
	// private static void
	// setMobileDataStateLollipopCommandlineAsRootUsingServiceCallPhone(Context
	// context, boolean enable) {
	// try{
	//
	// //TEST
	//
	// StringBuilder command = new StringBuilder();
	// command.append("su -c ");
	// command.append("\"service call phone ");
	// command.append(getTransactionCode(context) + " ");
	// command.append("i32 ");
	// command.append(enable?"1":"0");
	// command.append("\"\n");
	//
	// Log.i(TAG, "Executing on the command line: " + command.toString());
	//
	// Process process = Runtime.getRuntime().exec(command.toString());
	//
	// int returnValue = process.waitFor();
	//
	// Log.i(TAG, "Command returned with: " + returnValue);
	//
	// if (returnValue != 0) {
	// UserLog.log(context,
	// "Error setting mobile data state using service call phone. Return value: "
	// + returnValue);
	// setMobileDataStateLollipopCommandlineAsRoot2(context, enable);
	// }
	// }catch(Exception e){
	// UserLog.log(context,
	// "Error setting mobile data state using service call phone.");
	// Log.e(TAG, "Error changing mobile data on command line", e);
	// }
	// }

	//
	// private static void
	// setMobileDataStateLollipopCommandlineAsRootUsingServiceCallPhone(Context
	// context, boolean enable) {
	// try{
	//
	// //TEST
	//
	// StringBuilder command = new StringBuilder();
	// command.append("su -c ");
	// command.append("\"service call phone ");
	// command.append(getTransactionCode(context) + " ");
	// command.append("i32 ");
	// command.append(enable?"1":"0");
	// command.append("\"\n");
	//
	// Log.i(TAG, "Executing on the command line: " + command.toString());
	//
	// Process process = Runtime.getRuntime().exec(command.toString());
	//
	// int returnValue = process.waitFor();
	//
	// Log.i(TAG, "Command returned with: " + returnValue);
	//
	// if (returnValue != 0) {
	// UserLog.log(context,
	// "Error setting mobile data state using service call phone. Return value: "
	// + returnValue);
	// setMobileDataStateLollipopCommandlineAsRoot2(context, enable);
	// }
	// }catch(Exception e){
	// UserLog.log(context,
	// "Error setting mobile data state using service call phone.");
	// Log.e(TAG, "Error changing mobile data on command line", e);
	// }
	// }
}
