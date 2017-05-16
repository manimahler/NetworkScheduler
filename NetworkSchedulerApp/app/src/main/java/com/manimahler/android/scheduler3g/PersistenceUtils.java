package com.manimahler.android.scheduler3g;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PersistenceUtils {

	private static final String TAG = PersistenceUtils.class.getSimpleName();

	private static final String ARRAY_SIZE = "ARRAY_SIZE";
	private static final String PREF_VERSION = "VERSION";

	public static SharedPreferences getSchedulesPreferences(Context context) {
		return context.getSharedPreferences("SCHEDULER_PREFS",
				Context.MODE_PRIVATE);
	}
	

	public static void saveToPreferences(SharedPreferences preferences,
			ArrayList<ScheduledPeriod> periods) throws Exception {
		// Rough idea: EnabledPeriod could implement some interface
		// 'IKeyValuePersistable'
		// that has a Save(Writer writer) method. A Writer has the common
		// put<Type> methods
		// and there are two subclasses, one that would adapt to
		// SharedPrefs.Editor and one for Bundles

		// For the moment:

		try {

			SharedPreferences.Editor editor = preferences.edit();

			// TODO: get rid of this:
			editor.clear();

			int version = 0;
			editor.putInt(PREF_VERSION, version);

			int size = periods.size();
			editor.putInt(ARRAY_SIZE, size);

			for (int i = 0; i < size; i++) {

				Log.d(TAG, "Saving period : " + i);
				ScheduledPeriod period = periods.get(i);

				period.saveToPreferences(editor, Integer.toString(i));
			}

			// Commit to storage
			editor.commit();

		} catch (Exception ex) {
			Log.e(TAG, "Error saving settings: " + ex.toString());

			throw ex;
		}
	}

	public static void saveToPreferences(SharedPreferences preferences,
			ScheduledPeriod period) throws Exception {
		try {

			// Brute force. TODO: Sqlite
			ArrayList<ScheduledPeriod> savedPeriods = readFromPreferences(preferences);

			int idxToReplace = -1;
			for (int i = 0; i < savedPeriods.size(); i++) {
				if (savedPeriods.get(i).get_id() == period.get_id()) {
					idxToReplace = i;
				}
			}

			if (idxToReplace < 0) {
				throw new Exception("Error storing enabled-period");
			}

			savedPeriods.set(idxToReplace, period);

			saveToPreferences(preferences, savedPeriods);

		} catch (Exception ex) {
			Log.e(TAG, "Error saving settings: " + ex.toString());

			throw ex;
		}
	}

	public static SchedulerSettings readSettings(Context context) {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		// initialize defaults, in case the prefs screen was never opened
		PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

		SchedulerSettings result = new SchedulerSettings(sharedPrefs, context);
		
		// static cache in user log
		UserLog.set_loggingEnabled(result.is_globalOn() && result.is_loggingEnabled(), context);
		
		return result;
	}

	public static ArrayList<ScheduledPeriod> readFromPreferences(
			SharedPreferences preferences) {
		preferences.getInt(PREF_VERSION, 0);

		// assertion...
		int size = preferences.getInt(ARRAY_SIZE, 0);

		ArrayList<ScheduledPeriod> result = new ArrayList<ScheduledPeriod>(size);

		for (int i = 0; i < size; i++) {
			result.add(new ScheduledPeriod(preferences, Integer.toString(i)));
		}

		return result;
	}

	public static ScheduledPeriod getPeriod(SharedPreferences preferences,
			int periodId) {
		ArrayList<ScheduledPeriod> periods = PersistenceUtils
				.readFromPreferences(preferences);

		for (ScheduledPeriod enabledPeriod : periods) {
			if (periodId == enabledPeriod.get_id()) {
				return enabledPeriod;
			}
		}

		Log.d(TAG, "Unable to find enabled period " + periodId);
		return null;
	}

	public static void saveGlobalOnState(Context context, boolean value) {

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		SharedPreferences.Editor editor = sharedPrefs.edit();

		String GLOBAL_ON = "pref_key_global_on";

		editor.putBoolean(GLOBAL_ON, value);

		editor.commit();
	}
	
	public static void saveBluetoothState(Context context, boolean isInUse) {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		
		UserLog.log(context, "Detected bluetooth usage change. Is in use: " + isInUse);
		
		SharedPreferences.Editor editor = sharedPrefs.edit();
		
		editor.putBoolean(SchedulerSettings.BLUETOOTH_IN_USE, isInUse);
		
		editor.commit();
	}

}
