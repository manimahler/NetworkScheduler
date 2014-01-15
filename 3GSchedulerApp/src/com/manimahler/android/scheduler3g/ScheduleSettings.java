package com.manimahler.android.scheduler3g;

import android.content.SharedPreferences;

public class ScheduleSettings {
	private static final String END_TIME = "EndTime";
	private static final String START_TIME = "StartTime";
	private static final String SCHEDULING_ENABLED = "SchedulingEnabled";
	
	
	public boolean _schedulingEnabled;
	public long _startTimeMillis;
	public long _endTimeMillis;

	public boolean is_schedulingEnabled() {
		return _schedulingEnabled;
	}

	public void set_schedulingEnabled(boolean _schedulingEnabled) {
		this._schedulingEnabled = _schedulingEnabled;
	}

	public long get_startTimeMillis() {
		return _startTimeMillis;
	}

	public void set_startTimeMillis(long _startTimeMillis) {
		this._startTimeMillis = _startTimeMillis;
	}

	public long get_endTimeMillis() {
		return _endTimeMillis;
	}

	public void set_endTimeMillis(long _endTimeMillis) {
		this._endTimeMillis = _endTimeMillis;
	}

	public ScheduleSettings(SharedPreferences preferences)
	{
		_schedulingEnabled = preferences.getBoolean(SCHEDULING_ENABLED, false);
		
		_startTimeMillis = preferences.getLong(START_TIME, 0);
		_endTimeMillis = preferences.getLong(END_TIME, 0);
	}
	
	public ScheduleSettings(boolean schedulingEnabled, long startTimeMillis, long endTimeMillis)
	{
		_schedulingEnabled = schedulingEnabled;
		_startTimeMillis = startTimeMillis;
		_endTimeMillis = endTimeMillis;
	}
	
	public void saveToPreferences(SharedPreferences preferences)
	{
		SharedPreferences.Editor editor = preferences.edit();

		editor.putBoolean(SCHEDULING_ENABLED, _schedulingEnabled);
		editor.putLong(START_TIME, _startTimeMillis);
		editor.putLong(END_TIME, _endTimeMillis);

		// Commit to storage
		editor.commit();
	}

}
