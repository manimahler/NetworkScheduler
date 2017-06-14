package com.manimahler.android.scheduler3g;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.security.InvalidParameterException;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements
        TimePickerDialog.OnTimeSetListener {

    // Parent fragment must implement this interface
    public interface TimePickerDialogListener {
        void onStartTimePicked(int hourOfDay, int minute);

        void onEndTimePicked(int hourOfDay, int minute);
    }

    private static final String HOUR = "HOUR";
    private static final String MINUTE = "MINUTE";
    private static final String TYPE = "TYPE_START";
    public static final byte TYPE_START = 1;
    public static final byte TYPE_END = 2;

    private int _hourOfDay;
    private int _minute;
    private byte _type;

    private boolean timeSet = false;
    private boolean cancelDialog = false;

    // Factory method
    public static TimePickerFragment newInstance(long timeInMilliSeconds, byte type) {

        TimePickerFragment f = new TimePickerFragment();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliSeconds);

        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Supply input as argument.
        Bundle args = new Bundle();

        args.putInt(HOUR, hourOfDay);
        args.putInt(MINUTE, minute);
        args.putByte(TYPE, type);

        f.setArguments(args);

        return f;
    }

    public TimePickerFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int hour, minute;

        Bundle args = getArguments();
        if (args != null) {
            hour = args.getInt(HOUR, 0);
            minute = args.getInt(MINUTE, 0);
            _type = args.getByte(TYPE);
        } else {

            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // This is required for some android versions (4.4, others?)
        cancelDialog = true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (cancelDialog) {
            return;
        }

        if (timeSet) {

            // work-around for onTimeSet being called always, even when
            // cancelling the dialog
            onTimeSetAndDone(_hourOfDay, _minute);
        }

        timeSet = false;
    }

    public void onTimeSetAndDone(int hourOfDay, int minute) {

        // Get the 'parent' fragment through the activity
        TimePickerDialogListener listener =
                (TimePickerDialogListener) getActivity().getSupportFragmentManager().findFragmentByTag("fragment_schedule_period");

        if (listener == null) {
            return;
        }

        if (_type == TYPE_START) {
            listener.onStartTimePicked(hourOfDay, minute);
        } else if (_type == TYPE_END) {
            listener.onEndTimePicked(hourOfDay, minute);
        } else {
            throw new InvalidParameterException("_type has no valid value");
        }

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        _hourOfDay = hourOfDay;
        _minute = minute;

        timeSet = true;
    }
}