package com.manimahler.android.scheduler3g;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * Initially taken from https://gist.github.com/brunomateus/5617025,
 * because updating the summary does supposedly not work otherwise
 *
 */
public class ListPreferenceWithSummary extends ListPreference {

	public ListPreferenceWithSummary(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListPreferenceWithSummary(Context context) {
		super(context);
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		setSummary(value);
	}

	@Override
	public void setSummary(CharSequence summary) {
		super.setSummary(getEntry());
	}
}