package com.manimahler.android.scheduler3g;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PeriodListAdapter extends ArrayAdapter<EnabledPeriod> {
	private final Context context;
	private final ArrayList<EnabledPeriod> values;

	public PeriodListAdapter(Context context, ArrayList<EnabledPeriod> list) {
		super(context, R.layout.enabled_period, list);
		this.context = context;
		this.values = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.enabled_period, parent, false);

		

		
		EnabledPeriod period = values.get(position);

		if (period.get_name() != null && !period.get_name().isEmpty()) {
			TextView name = (TextView) rowView.findViewById(R.id.TextViewName);
			name.setText(period.get_name());
		}

		TextView startView = (TextView) rowView
				.findViewById(R.id.textViewStartTime);
		TextView stopView = (TextView) rowView
				.findViewById(R.id.TextViewStopTime);

		TextView onView = (TextView) rowView.findViewById(R.id.textViewOn);
		TextView offView = (TextView) rowView.findViewById(R.id.textViewOff);

		setPeriodItemTimes(period, startView, stopView, onView, offView);

		// week days
		TextView weekDayView = (TextView) rowView.findViewById(R.id.bottomLine);

		String weekdayText = DateTimeUtils.getWeekdaysText(context,
				period.get_weekDays(), context.getString(R.string.everyday),
				context.getString(R.string.never));

		Log.d("PeriodListAdapter.getView", "Week text: " + weekdayText);

		weekDayView.setText(weekdayText);

		
		// tinting the icons
		int tint = rowView.getResources().getColor(R.color.button_unchecked);

		if (!period.is_wifi()) {
			ImageView icon = (ImageView) rowView
					.findViewById(R.id.imageViewWifi);
			icon.setColorFilter(tint);
		}

		if (!period.is_mobileData()) {
			ImageView icon = (ImageView) rowView
					.findViewById(R.id.imageViewMobileData);
			icon.setColorFilter(tint);
		}

		if (!period.is_bluetooth()) {
			ImageView icon = (ImageView) rowView
					.findViewById(R.id.imageViewBluetooth);
			icon.setColorFilter(tint);
		}

		return rowView;
	}

	private void setPeriodItemTimes(EnabledPeriod period, TextView startView,
			TextView stopView, TextView onView, TextView offView) {

		int tint = R.color.weak_grey_transparent;

		String startTime = DateTimeUtils.getHourMinuteText(this.context,
				period.get_startTimeMillis());
		String stopTime = DateTimeUtils.getHourMinuteText(this.context,
				period.get_endTimeMillis());

		if (period.is_scheduleStart() && period.is_scheduleStop()) {
			if (DateTimeUtils.isEarlierInTheDay(period.get_startTimeMillis(),
					period.get_endTimeMillis())) {
				// both on (normal)
				startView.setText(startTime);
				stopView.setText(stopTime);
				stopView.setTextColor(tint);
				offView.setTextColor(tint);
			} else {
				// both on (swap start and end time)
				startView.setText(stopTime);
				stopView.setText(startTime);

				startView.setTextColor(tint);
				onView.setTextColor(tint);

				// and swap the on-off texts
				onView.setText(R.string.off);
				offView.setText(R.string.on);
			}
		} else {
			if (period.is_scheduleStart()) {
				startView.setText(startTime);
			} else {
				startView.setText(null);
				onView.setText("no switching on");
			}

			if (period.is_scheduleStop()) {
				stopView.setText(stopTime);
				stopView.setTextColor(tint);
				offView.setTextColor(tint);
			} else {
				stopView.setText(null);
				offView.setText("no switching off");
				offView.setTextColor(tint);
			}
		}
	}

	public void updateItem(EnabledPeriod item) {
		if (item.get_id() < 0) {

			item.set_id(this.getMaxId() + 1);

			this.add(item);

			notifyDataSetChanged();
		} else {
			EnabledPeriod itemToUpdate = findItem(item.get_id());

			if (itemToUpdate == null) {
				Log.e("PeriodListAdapter.updateItem",
						"Item to update not found");
			}

			itemToUpdate.set_id(item.get_id());
			itemToUpdate.set_name(item.get_name());

			itemToUpdate.set_scheduleStart(item.is_scheduleStart());
			itemToUpdate.set_scheduleStop(item.is_scheduleStop());

			itemToUpdate.set_startTimeMillis(item.get_startTimeMillis());
			itemToUpdate.set_endTimeMillis(item.get_endTimeMillis());
			itemToUpdate.set_weekDays(item.get_weekDays());

			itemToUpdate.set_mobileData(item.is_mobileData());
			itemToUpdate.set_wifi(item.is_wifi());
			itemToUpdate.set_bluetooth(item.is_bluetooth());

			notifyDataSetChanged();
		}
	}

	private EnabledPeriod findItem(int id) {

		for (int i = 0; i < this.getCount(); i++) {
			EnabledPeriod current = this.getItem(i);
			if (current.get_id() == id) {
				return current;
			}
		}

		return null;
	}

	private int getMaxId() {
		int result = 0;
		for (int i = 0; i < this.getCount(); i++) {
			int currentId = this.getItem(i).get_id();
			if (currentId > result) {
				result = currentId;
			}
		}

		return result;
	}

	public void removeAt(int position) {
		EnabledPeriod itemToRemove = this.getItem(position);

		this.remove(itemToRemove);

		notifyDataSetChanged();
	}
	
	public void moveUp(int originalPosition)
	{
		EnabledPeriod period = values.get(originalPosition);
		
		values.remove(originalPosition);
		
		values.add(originalPosition - 1, period);
		
		notifyDataSetChanged();
	}
	
	public void moveDown(int originalPosition)
	{
		EnabledPeriod period = values.get(originalPosition);
		
		values.remove(originalPosition);
		
		values.add(originalPosition + 1, period);
		
		notifyDataSetChanged();
	}
	
	
	private int getAvailableScreenWitdh(Activity activity)
	{
		  
		WindowManager w = activity.getWindowManager();
		Display d = w.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);
		// since SDK_INT = 1;
		int widthPixels = metrics.widthPixels;
		int heightPixels = metrics.heightPixels;
		// includes window decorations (statusbar bar/menu bar)
		if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
		try {
		    widthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
		    heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
		} catch (Exception ignored) {
		}
		// includes window decorations (statusbar bar/menu bar)
		if (Build.VERSION.SDK_INT >= 17)
		try {
		    Point realSize = new Point();
		    Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
		    widthPixels = realSize.x;
		    heightPixels = realSize.y;
		} catch (Exception ignored) {
		}
		
		return widthPixels;
	}
	

}
