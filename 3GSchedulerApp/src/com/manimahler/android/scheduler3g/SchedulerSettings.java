package com.manimahler.android.scheduler3g;

import android.content.SharedPreferences;
import android.os.Bundle;

public class SchedulerSettings {
	private static final String VIBRATE = "Vibrate";
	private static final String WARN_DEACTIVATION = "WarnOnDeactivation";
	private static final String CONNECT_INTERVAL = "ConnectInterval";
	
	private boolean _vibrate;
	private boolean _warnOnDeactivation;
	private int _connectInterval; 
	

	public SchedulerSettings(SharedPreferences preferences)
	{
		_vibrate = preferences.getBoolean(VIBRATE, false);
		_warnOnDeactivation = preferences.getBoolean(WARN_DEACTIVATION, true);
		_connectInterval = preferences.getInt(CONNECT_INTERVAL, 15);
	}
	
	public void saveToPreferences(SharedPreferences preferences)
	{
		SharedPreferences.Editor editor = preferences.edit();

		editor.putBoolean(VIBRATE, _vibrate);
		editor.putBoolean(WARN_DEACTIVATION, _warnOnDeactivation);
		editor.putInt(CONNECT_INTERVAL, _connectInterval);
		
		// Commit to storage
		editor.commit();
	}

	public boolean is_vibrate() {
		return _vibrate;
	}

	public void set_vibrate(boolean _vibrate) {
		this._vibrate = _vibrate;
	}

	public boolean is_warnOnDeactivation() {
		return _warnOnDeactivation;
	}

	public void set_warnOnDeactivation(boolean _warnOnDeactivation) {
		this._warnOnDeactivation = _warnOnDeactivation;
	}

	public int get_connectInterval() {
		return _connectInterval;
	}

	public void set_connectInterval(int _connectInterval) {
		this._connectInterval = _connectInterval;
	}

	

}
