package com.manimahler.android.scheduler3g;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.util.Log;

public class PersistenceUtils {

	private static final String ARRAY_SIZE = "ARRAY_SIZE";
	private static final String PREF_VERSION = "VERSION";
	
	private static final String VIBRATE = "Vibrate";
	private static final String WARN_DEACTIVATION = "WarnOnDeactivation";
	private static final String CONNECT_INTERVAL = "ConnectInterval";

	public static void saveToPreferences(SharedPreferences preferences,
			ArrayList<EnabledPeriod> periods, SchedulerSettings settings) {
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

				Log.d("saveToPreferences", "Saving period : " + i);
				EnabledPeriod period = periods.get(i);

				period.saveToPreferences(editor, Integer.toString(i));
			}
			
			putSettings(settings, editor);

			// Commit to storage
			editor.commit();

		} catch (Exception ex) {
			Log.e("saveToPreferences",
					"Error saving settings: " + ex.toString());

			// throw ex;
		}
	}
	
	public static void putSettings(SchedulerSettings settings, SharedPreferences.Editor editor)
	{
		editor.putBoolean(VIBRATE, settings.is_vibrate());
		editor.putBoolean(WARN_DEACTIVATION, settings.is_warnOnDeactivation());
		editor.putInt(CONNECT_INTERVAL, settings.get_connectInterval());
		
	}

	public static ArrayList<EnabledPeriod> readFromPreferences(
			SharedPreferences preferences) {
		preferences.getInt(PREF_VERSION, 0);

		// assertion...

		int size = preferences.getInt(ARRAY_SIZE, 0);

		ArrayList<EnabledPeriod> result = new ArrayList<EnabledPeriod>(size);

		for (int i = 0; i < size; i++) {
			result.add(new EnabledPeriod(preferences, Integer.toString(i)));
		}

		return result;
	}

	public static EnabledPeriod getPeriod(SharedPreferences preferences,
			int periodId) {
		ArrayList<EnabledPeriod> enabledPeriods = PersistenceUtils
				.readFromPreferences(preferences);

		for (EnabledPeriod enabledPeriod : enabledPeriods) {
			if (periodId == enabledPeriod.get_id()) {
				return enabledPeriod;
			}
		}
		return null;
	}
}
