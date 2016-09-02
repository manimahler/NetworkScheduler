package com.manimahler.android.scheduler3g;

import java.lang.reflect.InvocationTargetException;
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
	
	public ScreenLockDetector() {}

	private boolean isLocked(Context context) {
		KeyguardManager kgMgr = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		
		// Added at API level 16:
//		boolean isKeyguardLocked = kgMgr.isKeyguardLocked();
//		Log.d(TAG, "isKeyguardLocked: " + isKeyguardLocked);
//		
		// Added at API level 22:
//		boolean isDeviceLocked = kgMgr.isDeviceLocked();
//		Log.d(TAG, "isDeviceLocked: " + isDeviceLocked);
		
		// NOTE: isKeyguardLocked() was only added at API level 16, but this seems to work ok:
		boolean isLocked = kgMgr.inKeyguardRestrictedInputMode();
		return isLocked;
	}

	/**
	 * Returns whether the user is absent in a way that the next time the screen
	 * is turned on (if no screen lock is set up) or the device is unlocked (including swipe) a
	 * USER_PRESENT_ACTION is broadcast.
	 */
	public boolean isUserAbsent(Context context) {
		
		boolean isDeviceLocked = isLocked(context);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			// there was no 'None' option for screen lock in the olden days
			return isDeviceLocked;
		}
				
		if (isLockScreenDisabled(context)) {
			
			// Lock Type 'None' (USER_PRESENT is broadcast when screen comes on)
			
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
				// android 3.0 - 4.1: we have a problem with 'None' because
				// user_present is never broadcast!
				UserLog.log(TAG, context,
						"No screen lock on android 3.0 - 4.1: User-presence will not be detected! Please switch to 'Swipe'");
			}
			
			return !isScreenOn(context);
		} else {
			// Lock Type 'Swipe' or proper lock  (USER_PRESENT is broadcast when device is unlocked)
			return isDeviceLocked;
		}
	}
	
	private boolean isScreenOn(Context context) {
		
		PowerManager powerManager = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		
		boolean result;
		
		if (Build.VERSION.SDK_INT >= 20) {
			result = powerManager.isInteractive();
		}
		else{
			result = powerManager.isScreenOn();
		}
		
		Log.d(TAG, "isScreenOn: " + result);
		
		return result;
	}
	
	private boolean isLockScreenDisabled(Context context)
	{
		// Starting with android 6.0 calling isLockScreenDisabled fails altogether because the
		// signature has changed. There is a new method isDeviceSecure which, however, does
		// not allow the differentiation between lock screen 'None' and 'Swipe.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			
			KeyguardManager keyguardMgr = (KeyguardManager) context
					.getSystemService(Context.KEYGUARD_SERVICE);
			
			Log.d(TAG, "Marshmallow is device secured: " + keyguardMgr.isDeviceSecure());
			
			// But luckily there is no 'Automatically lock x minutes after sleep' option when 
			// 'Swipe' is set which means that as soon as the screen is off, switching back on 
			// requires a swipe which results in a USER_PRESENT broadcast. 
			return !keyguardMgr.isDeviceSecure();
		}
		
	    String LOCKSCREEN_UTILS = "com.android.internal.widget.LockPatternUtils";

	    try 
	    {
	        Class<?> lockUtilsClass = Class.forName(LOCKSCREEN_UTILS);
	        
	        Object lockUtils = lockUtilsClass.getConstructor(Context.class).newInstance(context);
	        
	        Method method = lockUtilsClass.getMethod("isLockScreenDisabled");
	        
	        // Starting with android 5.x this fails with InvocationTargetException 
	        // (caused by SecurityException - MANAGE_USERS permission is required because
	        //  internally some additional logic was added to return false if one can switch between several users)
	        // if (Screen Lock is None) { 
	        //	 ... exception caused by getting all users (if user count)
	        // } else {
	        //	 return false;
	        // }
	        // -> therefore if no exception is thrown, we know the screen lock setting is
	        //    set to Swipe, Pattern, PIN/PW or something else other than 'None'
	        
	        boolean isDisabled;
	        try {
	        
	        	isDisabled = Boolean.valueOf(String.valueOf(method.invoke(lockUtils)));
	        }
	        catch (InvocationTargetException ex) {
	        	Log.w(TAG, "Expected exception with screen lock type equals 'None': " + ex);
	        	isDisabled = true;
	        }
	        return isDisabled;
	    }
	    catch (Exception e)
	    {
	        Log.e(TAG, "Error detecting whether screen lock is disabled: " + e);
	        
	        e.printStackTrace();
	    }

	    return false;
	}

}
