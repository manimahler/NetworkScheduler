package com.manimahler.android.scheduler3g;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

// TODO: turn into a PreferenceFragment once gingerbread support is dropped...
public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = SettingsActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}

		Preference buttonShowLog = (Preference) findPreference(this
				.getString(R.string.pref_key_logging_show));
		buttonShowLog
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						openLogFile(arg0.getContext());
						return true;
					}
				});

		Preference buttonDeleteLog = (Preference) findPreference(this
				.getString(R.string.pref_key_logging_delete));
		buttonDeleteLog
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						deleteLogFile(arg0.getContext());
						updatePrefAppearance(arg0);
						return true;
					}
				});
	}

	private void openLogFile(Context context) {
		Intent intent = new Intent();

		intent.setAction(android.content.Intent.ACTION_VIEW);

		File file = UserLog.getLogFile(context);

		intent.setDataAndType(Uri.fromFile(file), "text/*");

		startActivity(intent);

	}

	private void deleteLogFile(Context context) {

		File file = UserLog.getLogFile(context);

		try {
			if (file.exists()) {
				file.delete();

				Toast.makeText(context,
						context.getString(R.string.log_file_deleted),
						Toast.LENGTH_SHORT).show();
			}

		} catch (Exception e) {
			// Unable to create file, likely because external storage is
			// not currently mounted.
			Log.w(TAG, "Error deleting log file", e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		updatePrefAppearance(findPreference(key));
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceCategory) {
			PreferenceCategory pCat = (PreferenceCategory) p;
			for (int i = 0; i < pCat.getPreferenceCount(); i++) {
				initSummary(pCat.getPreference(i));
			}
		} else {
			updatePrefAppearance(p);
		}
	}

	private void updatePrefAppearance(Preference p) {
		// The idea to set the summary was taken from
		// http://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android-preference-in-the-preference-su
		// However this does not work if the summary in the strings resource
		// contains a format (%s)
		// which is replaced in here. Because the next time p.getSummary won't
		// contain the %s parameter
		// and the current value does not get inserted! There does not seem to
		// be a simple solution, so resort to hack:

		if (p.getKey().equals(
				this.getString(R.string.pref_key_connect_interval))) {
			ensurePreferenceLarger0(p);

			updatePreferenceTitle(p, R.string.pref_title_connect_interval);
			updatePreferenceSummary(p, R.string.pref_summary_connect_interval);
		}

		if (p.getKey().equals(
				this.getString(R.string.pref_key_connect_duration))) {
			ensurePreferenceLarger0(p);

			updatePreferenceTitle(p, R.string.pref_title_connect_duration);
			updatePreferenceSummary(p, R.string.pref_summary_connect_duration);
		}

		if (p.getKey().equals(this.getString(R.string.pref_key_delay_min))) {
			ensurePreferenceLarger0(p);

			updatePreferenceTitle(p, R.string.pref_title_delay_min);
			updatePreferenceSummary(p, R.string.pref_summary_delay_min);
		}

		if (p.getKey().equals(this.getString(R.string.pref_key_logging_show))) {

			File logFile = UserLog.getLogFile(p.getContext());

			p.setEnabled(logFile.exists());
		}

		if (p.getKey().equals(this.getString(R.string.pref_key_logging_delete))) {

			File logFile = UserLog.getLogFile(p.getContext());

			double kilobytes = 0;
			if (logFile.exists()) {
				double bytes = logFile.length();
				kilobytes = (bytes / 1024);
			} else {
				p.setEnabled(false);
			}

			String summaryFormat = this
					.getString(R.string.pref_summary_logging_delete);

			DecimalFormat df = new DecimalFormat("#0.0");
			p.setSummary(String.format(summaryFormat, df.format(kilobytes)));
		}
	}

	private void ensurePreferenceLarger0(Preference p) {
		EditTextPreference editTextPref = (EditTextPreference) p;
		String currentText = editTextPref.getText();
		int currentValue = 0;
		try {
			currentValue = Integer.parseInt(currentText);
		} catch (Exception e) {
			// caught intentionally
		}

		if (currentValue == 0) {
			// minimum value:
			editTextPref.setText("1");
		}
	}

	private void updatePreferenceSummary(Preference p, int summaryResId) {

		EditTextPreference editTextPref = (EditTextPreference) p;

		String summaryFormat = this.getString(summaryResId);

		p.setSummary(String.format(summaryFormat, editTextPref.getText()));
	}

	private void updatePreferenceTitle(Preference p, int titleResId) {
		EditTextPreference editTextPref = (EditTextPreference) p;

		String titleFormat = this.getString(titleResId);

		p.setTitle(String.format(titleFormat, editTextPref.getText()));
	}
}