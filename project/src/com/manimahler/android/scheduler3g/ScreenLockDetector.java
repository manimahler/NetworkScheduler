package com.manimahler.android.scheduler3g;

import java.lang.reflect.Method;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
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
	
	public ScreenLockDetector() {	
	}

	private boolean isLocked(Context context) {
		KeyguardManager kgMgr = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		
		// NOTE: isKeyguardLocked() was only added at API level 16, but this seems to work ok:
		boolean isLocked = kgMgr.inKeyguardRestrictedInputMode();
		return isLocked;
	}

	/**
	 * Returns whether the user is absent in a way that the next time the screen
	 * is turned on (if no screen lock is set up) or unlocked a
	 * USER_PRESENT_ACTION is broadcast.
	 */
	public boolean isUserAbsent(Context context) {

		boolean isDeviceLocked = isLocked(context);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			// there was no 'None' option for screen lock
			return isDeviceLocked;
		}

		if (isLockScreenDisabled(context)) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
				// android 3.0 - 4.1: we have a problem with 'None' because
				// user_present is never broadcast!
				UserLog.log(TAG, context,
						"No screen lock on android 3.0 - 4.1: User-presence will not be detected! Please switch to 'Swipe'");
			}
			return !isScreenOn(context);
		} else {
			return isDeviceLocked;
		}

	}
	
	private boolean isScreenOn(Context context) {
		PowerManager powerManager = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		return powerManager.isScreenOn();
	}
		
	private boolean isLockScreenDisabled(Context context)
	{
	    String LOCKSCREEN_UTILS = "com.android.internal.widget.LockPatternUtils";

	    try
	    { 
	        Class<?> lockUtilsClass = Class.forName(LOCKSCREEN_UTILS);
	        // "this" is a Context, in my case an Activity
	        Object lockUtils = lockUtilsClass.getConstructor(Context.class).newInstance(context);

	        Method method = lockUtilsClass.getMethod("isLockScreenDisabled");

	        boolean isDisabled = Boolean.valueOf(String.valueOf(method.invoke(lockUtils)));

	        return isDisabled;
	    }
	    catch (Exception e)
	    {
	        Log.e("reflectInternalUtils", "ex:"+e);
	    }

	    return false;
	}

}
