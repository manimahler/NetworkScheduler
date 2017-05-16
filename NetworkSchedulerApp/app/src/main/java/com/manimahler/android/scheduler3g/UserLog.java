package com.manimahler.android.scheduler3g;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.util.Log;

public class UserLog {

	private static final String TAG = UserLog.class.getSimpleName();
	
	private static boolean _loggingEnabled; 

	public static void log(Context context, String text, Exception exception) {
		
		String exceptionText = getErrorStack(exception);
	
		log(context, text + " " + exceptionText);
	}
	
	public static void log(Context context, String text) {
		log(null, context, text);
	}
	
	public static void log(String tag, Context context, String text) {

		if (tag != null) {
			Log.d(tag, text);
		}
		
		if (! _loggingEnabled) {
			return;
		}
		
		try {
			File logFile = initializeLogfile(context);
			
			DateFormat dateFormat = DateFormat.getDateTimeInstance();
			
			String dateString = dateFormat.format(new Date());

			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(String.format("%s: %s", dateString, text));
			buf.newLine();
			buf.flush();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	public static boolean is_loggingEnabled() {
		return _loggingEnabled;
	}

	public static void set_loggingEnabled(boolean loggingEnabled, Context context) {
		
		boolean valueChanged = _loggingEnabled != loggingEnabled;
		
		if (valueChanged && ! loggingEnabled){
			Log.d(TAG, "Logging is currently disabled");
		}
		
		_loggingEnabled = loggingEnabled;
		
		if (valueChanged && loggingEnabled) {
			log(context, String.format("Logging is currently enabled using file %s", getLogPath(context)));
		}
	}
	
	public static void logEnabledChanged(Context context) {
		
		if (_loggingEnabled) {
			log(context, "Disabled logging, stopping log...");
		}
		else
		{
			log(context, "Logging enabled, starting log...");
		}
	}
	
	public static File getLogFile(Context context) {
		File logFile = new File(context.getExternalFilesDir(null),
				"NetworkScheduler.log");
		
		return logFile;
	}

	private static File initializeLogfile(Context context) {

		File logFile = null;

		try {
			logFile = getLogFile(context);

			if (logFile.createNewFile()) {
				Log.d(TAG, "Created logfile " + logFile);
			} else {
				Log.d(TAG, "Using existing logfile: " + logFile);
			}

		} catch (IOException e) {
			// Unable to create file, likely because external storage is
			// not currently mounted.
			Log.w(TAG, "Error writing " + logFile, e);
		}
		return logFile;
	}
	
	private static String getErrorStack(Exception e) {
	    CharArrayWriter cw = new CharArrayWriter();
	    PrintWriter w = new PrintWriter(cw);
	    e.printStackTrace(w);
	    w.close();
	    return cw.toString();
	}
	
	private static String getLogPath(Context context) {
		File logFile = getLogFile(context);
		
		String result;
		if (logFile.exists()) {
			result = logFile.getAbsolutePath();
		} else {
			result = null;
		}
		
		return result;
	}
}
