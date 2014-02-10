package com.manimahler.android.scheduler3g;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class EnabledPeriod {

	private static final String PERIOD_ID = "ID";
	private static final String NAME = "NAME";
	private static final String END_TIME = "EndTime";
	private static final String START_TIME = "StartTime";
	private static final String SCHEDULING_ENABLED = "SchedulingEnabled";
	private static final String SCHEDULE_START = "ScheduleStart";
	private static final String SCHEDULE_STOP = "ScheduleStop";

	private static final String BLUETOOTH = "BLUETOOTH";
	private static final String WIFI = "WIFI";
	private static final String MOBILE_DATA = "MOBILE_DATA";

	private static final String WEEK_DAYS = "WeekDays";

	private int _id;
	private String _name;
	private boolean _schedulingEnabled;
	private long _startTimeMillis;
	private long _endTimeMillis;
	private boolean _scheduleStart;
	private boolean _scheduleStop;

	private boolean _mobileData;
	private boolean _wifi;
	private boolean _bluetooth;

	private boolean[] _weekDays;

	public EnabledPeriod(Bundle bundle) {

		_id = bundle.getInt(PERIOD_ID);

		_name = bundle.getString(NAME);

		_schedulingEnabled = bundle.getBoolean(SCHEDULING_ENABLED, true);

		_scheduleStart = bundle.getBoolean(SCHEDULE_START, true);
		_scheduleStop = bundle.getBoolean(SCHEDULE_STOP, true);
		_startTimeMillis = bundle.getLong(START_TIME, 0);
		_endTimeMillis = bundle.getLong(END_TIME, 0);

		_mobileData = bundle.getBoolean(MOBILE_DATA, true);
		_wifi = bundle.getBoolean(WIFI, false);
		_bluetooth = bundle.getBoolean(BLUETOOTH, false);

		_weekDays = bundle.getBooleanArray(WEEK_DAYS);

		if (_weekDays == null) {
			_weekDays = new boolean[7];
		}
	}

	public EnabledPeriod(SharedPreferences preferences, String qualifier) {
		_id = preferences.getInt(PERIOD_ID + qualifier, -1);

		_name = preferences.getString(NAME + qualifier, "");

		_schedulingEnabled = preferences.getBoolean(SCHEDULING_ENABLED
				+ qualifier, true);
		_scheduleStart = preferences.getBoolean(SCHEDULE_START + qualifier, true);
		_scheduleStop = preferences.getBoolean(SCHEDULE_STOP + qualifier, true);

		_startTimeMillis = preferences.getLong(START_TIME + qualifier, 0);
		_endTimeMillis = preferences.getLong(END_TIME + qualifier, 0);

		_mobileData = preferences.getBoolean(MOBILE_DATA + qualifier, true);
		_wifi = preferences.getBoolean(WIFI + qualifier, false);
		_bluetooth = preferences.getBoolean(BLUETOOTH + qualifier, false);

		_weekDays = new boolean[7];

		for (int i = 0; i < 7; i++) {
			_weekDays[i] = preferences.getBoolean(WEEK_DAYS + qualifier + "_"
					+ i, false);
		}
	}

	// public EnabledPeriod(SharedPreferences preferences)
	// {
	// _schedulingEnabled = preferences.getBoolean(SCHEDULING_ENABLED, false);
	//
	// _startTimeMillis = preferences.getLong(START_TIME, 0);
	// _endTimeMillis = preferences.getLong(END_TIME, 0);
	// }

	public EnabledPeriod(boolean schedulingEnabled, long startTimeMillis,
			long endTimeMillis, boolean[] weekDays) {
		_id = -1;

		_name = "";

		_scheduleStart = true;
		_scheduleStop = true;
		
		_schedulingEnabled = schedulingEnabled;
		_startTimeMillis = startTimeMillis;
		_endTimeMillis = endTimeMillis;

		_weekDays = weekDays;

		_mobileData = true;
		_wifi = true;
		_bluetooth = true;
	}

	public void set_id(int id) {
		this._id = id;
	}

	public int get_id() {
		return _id;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}
	
	public boolean is_scheduleStart() {
		return _scheduleStart;
	}

	public void set_scheduleStart(boolean _scheduleStart) {
		this._scheduleStart = _scheduleStart;
	}

	public boolean is_scheduleStop() {
		return _scheduleStop;
	}

	public void set_scheduleStop(boolean _scheduleStop) {
		this._scheduleStop = _scheduleStop;
	}

	public boolean is_schedulingEnabled() {
		return _schedulingEnabled;
	}

	public void set_schedulingEnabled(boolean schedulingEnabled) {
		this._schedulingEnabled = schedulingEnabled;
	}

	public long get_startTimeMillis() {
		return _startTimeMillis;
	}

	public void set_startTimeMillis(long startTimeMillis) {
		this._startTimeMillis = startTimeMillis;
	}

	public long get_endTimeMillis() {
		return _endTimeMillis;
	}

	public void set_endTimeMillis(long endTimeMillis) {
		this._endTimeMillis = endTimeMillis;
	}

	public boolean[] get_weekDays() {
		return _weekDays;
	}

	public void set_weekDays(boolean[] weekDays) {
		this._weekDays = weekDays;
	}

	public boolean is_mobileData() {
		return _mobileData;
	}

	public void set_mobileData(boolean _mobileData) {
		this._mobileData = _mobileData;
	}

	public boolean is_wifi() {
		return _wifi;
	}

	public void set_wifi(boolean _wifi) {
		this._wifi = _wifi;
	}

	public boolean is_bluetooth() {
		return _bluetooth;
	}

	public void set_bluetooth(boolean _bluetooth) {
		this._bluetooth = _bluetooth;
	}

	public void saveToPreferences(SharedPreferences.Editor editor,
			String qualifier) {
		editor.putInt(PERIOD_ID + qualifier, _id);
		editor.putString(NAME + qualifier, _name);
		
		editor.putBoolean(SCHEDULE_START + qualifier, _scheduleStart);
		editor.putBoolean(SCHEDULE_STOP + qualifier, _scheduleStop);
		
		editor.putBoolean(SCHEDULING_ENABLED + qualifier, _schedulingEnabled);
		editor.putLong(START_TIME + qualifier, _startTimeMillis);
		editor.putLong(END_TIME + qualifier, _endTimeMillis);

		Log.d("saveToPreferences", "EnabledPeriod: Saving network settings...");

		editor.putBoolean(MOBILE_DATA + qualifier, _mobileData);
		editor.putBoolean(WIFI + qualifier, _wifi);
		editor.putBoolean(BLUETOOTH + qualifier, _bluetooth);

		Log.d("saveToPreferences", "EnabledPeriod: Saving week days...");
		for (int i = 0; i < 7; i++) {
			editor.putBoolean(WEEK_DAYS + qualifier + "_" + i, _weekDays[i]);
		}

	}

	public void saveToBundle(Bundle bundle) {
		bundle.putInt(PERIOD_ID, _id);
		bundle.putString(NAME, _name);
		
		bundle.putBoolean(SCHEDULE_START, _scheduleStart);
		bundle.putBoolean(SCHEDULE_STOP, _scheduleStop);
		
		bundle.putLong(START_TIME, _startTimeMillis);
		bundle.putLong(END_TIME, _endTimeMillis);
		bundle.putBoolean(SCHEDULING_ENABLED, _schedulingEnabled);
		bundle.putLong(START_TIME, _startTimeMillis);
		bundle.putLong(END_TIME, _endTimeMillis);

		bundle.putBoolean(MOBILE_DATA, _mobileData);
		bundle.putBoolean(WIFI, _wifi);
		bundle.putBoolean(BLUETOOTH, _bluetooth);

		bundle.putBooleanArray(WEEK_DAYS, _weekDays);

	}
}
