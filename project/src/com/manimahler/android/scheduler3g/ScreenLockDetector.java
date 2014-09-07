package com.manimahler.android.scheduler3g;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;


/**
 * Using heuristics to find out whether the user is absent or not
 * (i.e. the device is locked and the next time the user comes back
 * the user-present action will be broadcast
 * 
 * 
 * Behavior this heuristic relies on (should be verified witch each release)
 * 
 * Type of screen lock     | KeyguardManager.isKeyguardSecure() | is locked (KeyguardManager.inKeyguardRestrictedInputMode())
 * --------------------------------------------------------------------------------------------------------------------------
 * Swipe                   | false                              | false (until lock timeout) / true (afterwards)
 * user-present broadcast: |                                      NO                         / YES
 * 
 * None                    | false                              | false
 * user-present broadcast: |                                    | YES (always when screen is turned on but only at API level 17+)
 * 
 * Other (PIN, PW, Pattern)| true                               | false (until lock timeout) / true (afterwards)
 * user-present broadcast: |                                    | NO                         / YES
 */ 
public class ScreenLockDetector {
	
	private static final String TAG = ScreenLockDetector.class.getSimpleName();
	
	private static final String LOCK_TYPE = "LOCK_TYPE";
	private static final String SCREEN_OFF_SINCE = "SCREEN_OFF_SINCE";
	private static final String LAST_USER_PRESENT = "LAST_USER_PRESENT";
	
	public static int LOCK_TYPE_DONTKNOW = 0;
	public static int LOCK_TYPE_NONE = 1;
	public static int LOCK_TYPE_SWIPE = 2;
	public static int LOCK_TYPE_PROPERLOCK = 3;
	
	private int _lastKnownLockType;
	
	private long _lastTimeUserPresent;
	private long _screenOffSince;
	
	SharedPreferences _preferences;
	
	public ScreenLockDetector(Context context) {
		
		// read last known value
		_preferences = PersistenceUtils.getScreenLockDetectorPrefs(context);
		
		_lastKnownLockType = _preferences.getInt(LOCK_TYPE, LOCK_TYPE_DONTKNOW);
		_screenOffSince = _preferences.getLong(SCREEN_OFF_SINCE, -1);
		_lastTimeUserPresent = _preferences.getLong(LAST_USER_PRESENT, -1);
	}
	
	public void userPresent(Context context) {
		
		setLastUserPresent(System.currentTimeMillis());
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			// user_present is never broadcast if screen lock is 'None':
			if (isKeyguardSecured(context)) {
				setKnownLockType(LOCK_TYPE_PROPERLOCK);
			} else {
				setKnownLockType(LOCK_TYPE_SWIPE);
			}
		}
	}

	public void setScreenIsOn(boolean isOn, Context context) {

		if (isOn) {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
					&& _screenOffSince > 0 && _lastTimeUserPresent > 0
					&& _lastTimeUserPresent < _screenOffSince) {

				// there was no user-present action when the screen came back
				// on:
				// must have been from non-locked sleep mode
				if (isKeyguardSecured(context)) {
					setKnownLockType(LOCK_TYPE_PROPERLOCK);
				} else {
					setKnownLockType(LOCK_TYPE_SWIPE);
				}
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
					&& Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1
					&& _lastTimeUserPresent > _screenOffSince) {
				// there was no user-present

			}

			setScrenOffSince(-9999);
		} else { // screen dark
			if (isLocked(context)) {
				if (isKeyguardSecured(context)) {
					setKnownLockType(LOCK_TYPE_PROPERLOCK);
				} else {
					setKnownLockType(LOCK_TYPE_SWIPE);
				}
			} else { // screen dark and not locked
				
				long timeSinceFirstOff = System.currentTimeMillis() - _screenOffSince;
				long timeout;
				
				// when we can rely on user_present:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
						&& _screenOffSince > 0) {
					// could be 'Swipe' before lock-time-out or 'None', but
					// there is a possible gap in this logic, if the device was
					// repeatedly switched back on before it was locked
					// (no user-present) -> incorrectly set to none

					timeout = 30 * 1000;

				} else { // user-present not reliable, use large time-out
					timeout = 2 * 3600 * 1000;
				}
				
				if (timeSinceFirstOff > timeout) {
					setKnownLockType(LOCK_TYPE_NONE);
				}
			}
		}

		if (_screenOffSince < 0) {
			// it was on in the mean while and we can re-start the
			// measurement
			setScrenOffSince(System.currentTimeMillis());
		}
	}
	
	private void setKnownLockType(int lockType) {
		if (_lastKnownLockType == lockType) {
			return;
		}
		
		_lastKnownLockType = lockType;
		
		Editor editor = _preferences.edit();
		editor.putInt(LOCK_TYPE, lockType);
		editor.commit();
	}
	
	private void setScrenOffSince(long timeInMillis) {
		
		_screenOffSince = timeInMillis;
				
		putLongIntoPrefs(SCREEN_OFF_SINCE, timeInMillis);
	}
	
	private void setLastUserPresent(long timeInMillis) {
		
		_lastTimeUserPresent = timeInMillis;
		
		putLongIntoPrefs(LAST_USER_PRESENT, timeInMillis);
	}

	private void putLongIntoPrefs(String key, long value) {
		Editor editor = _preferences.edit();
		editor.putLong(key, value);
		editor.commit();
	}
	
	
	public int getLockType(Context context) {
		
		boolean isLocked = isLocked(context);
		
		
//		int DELAY = 3000;
//		int lockTimeout = Settings.System.getInt(context.getContentResolver(), 
//				Settings.Secure.LOCK_SCREEN_LOCK_AFTER_TIMEOUT, DELAY);
		
		long lockTimeout = Settings.Secure.getLong(context.getContentResolver(), "lock_screen_lock_after_timeout", -1);
		
		boolean keyguardSecured = isKeyguardSecured(context);
		Log.d(TAG, "Lock timeout: " + lockTimeout + ". is locked: " + isLocked + ". isKeyguardSecured: " + keyguardSecured);
		
		int result;
		
		if (keyguardSecured) {
			// proper lock
			result = LOCK_TYPE_PROPERLOCK;
		}
		else if (isLocked){

			// locked despite isKeyguardSecure == false -> must be swipe
			result = LOCK_TYPE_SWIPE;

		} else {
			
			if (isScreenOn(context)) {
				// has screen come on without user-present action -> 
			}
			
			long timeSinceLastUserPresent = System.currentTimeMillis() - _lastTimeUserPresent;
			
			// if screen is dark and 
			if (! isScreenOn(context) && timeSinceLastUserPresent > lockTimeout) {
				// must be 'None'
				result = LOCK_TYPE_NONE;
			}
			else {
				
			}
			result = LOCK_TYPE_DONTKNOW;


		}
		
		return result;
	}

	private boolean isLocked(Context context) {
		KeyguardManager kgMgr = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		
		// NOTE: isKeyguardLocked() was only added at API level 16, but this seems to work ok:
		boolean isLocked = kgMgr.inKeyguardRestrictedInputMode();
		return isLocked;
	}
	
	/**
	 *  Returns whether the user is absent in a way that the next time
	 *  the screen is turned on (if no screen lock is set up) or unlocked
	 *  a USER_PRESENT_ACTION is brodcast. 
	 */
	// TODO
	@SuppressLint("NewApi")
	public boolean isUserAbsent(Context context) {
		
		KeyguardManager kgMgr = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);

		int currentLockType = getLockType(context);
		
		if (currentLockType == LOCK_TYPE_PROPERLOCK ||
				currentLockType == LOCK_TYPE_SWIPE) {
			return kgMgr.isKeyguardLocked();
		}

		
		boolean isDeviceLocked = kgMgr.inKeyguardRestrictedInputMode();
		
		// TEST:
		int defTimeOut = Settings.System.getInt(context.getContentResolver(), 
                Settings.System.SCREEN_OFF_TIMEOUT, -1);
		
		UserLog.log(TAG,  context, "screeen_off_time_out: " + defTimeOut);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			boolean keyguardIsSecure = kgMgr.isKeyguardSecure();
			
			boolean isLocked = kgMgr.isKeyguardLocked();
			
			UserLog.log(TAG,  context, "keyguardIsSecure: " + keyguardIsSecure + ". keyguardIsLocked: " + isLocked + ". isKeyguardRestrictedInputMode: " + isDeviceLocked);
		}
		
		// TODO
		return false;

	}
	
	private boolean isScreenOn(Context context) {
		PowerManager powerManager = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		return powerManager.isScreenOn();
	}
	
	private boolean isKeyguardSecured(Context context) {
		
		// TODO: starting with jelly bean (API level 16, use KeyguardManager.isKeyguardSecure()
		
		// adapted from http://stackoverflow.com/questions/7768879/check-whether-lock-was-enabled-or-not
		final String PASSWORD_TYPE_KEY = "lockscreen.password_type";
		
        ContentResolver contentResolver = context.getContentResolver();
        
		long mode = Settings.Secure.getLong(contentResolver, PASSWORD_TYPE_KEY, -1);
        
        Log.d(TAG, "Password type: " + mode);
        
        if (mode == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING)
        {
            if (android.provider.Settings.Secure.getInt(contentResolver, Settings.Secure.LOCK_PATTERN_ENABLED, 0) == 1)
            {
                return true;
            }
            else return false; // none or slider
        }
        else {
        	return true;
        }
	}
	
	

}
