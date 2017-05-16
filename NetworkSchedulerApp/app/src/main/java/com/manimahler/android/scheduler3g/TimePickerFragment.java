package com.manimahler.android.scheduler3g;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements
		TimePickerDialog.OnTimeSetListener {

	private int _hourOfDay;
	private int _minute;

	private boolean cancelDialog = false;

	public TimePickerFragment(int hourOfDay, int minute) {
		_hourOfDay = hourOfDay;
		_minute = minute;
	}

	public TimePickerFragment(long timeInMilliSeconds) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMilliSeconds);

		_hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		_minute = calendar.get(Calendar.MINUTE);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current time as the default values for the picker
		// final Calendar c = Calendar.getInstance();
		// int hour = c.get(Calendar.HOUR_OF_DAY);
		// int minute = c.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, _hourOfDay, _minute,
				DateFormat.is24HourFormat(getActivity()));
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		cancelDialog = true;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (!cancelDialog) {

			// work-around for onTimeSet being called always, even when
			// cancelling the dialog
			onTimeSetAndDone(_hourOfDay, _minute);
		}
	}

	public void onTimeSetAndDone(int hourOfDay, int minute) {
		// to override
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// Do something with the time chosen by the user
		_hourOfDay = hourOfDay;
		_minute = minute;
	}
}