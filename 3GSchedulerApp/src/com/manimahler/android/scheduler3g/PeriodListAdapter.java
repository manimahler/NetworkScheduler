package com.manimahler.android.scheduler3g;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PeriodListAdapter extends ArrayAdapter<EnabledPeriod> {
	private final Context context;
	private final ArrayList<EnabledPeriod> values;

	boolean _enabled;

	public PeriodListAdapter(Context context, ArrayList<EnabledPeriod> list) {
		super(context, R.layout.enabled_period, list);
		this.context = context;
		this.values = list;

		_enabled = true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.enabled_period, parent, false);

		EnabledPeriod period = values.get(position);

		View button = rowView.findViewById(R.id.buttonOn);
		if (period.is_scheduleStart() && period.is_scheduleStop()
				&& period.is_active()) {

			// change to setBackground once support for < SDK v16 is dropped.
			button.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.led_red));
		} else {
			button.setBackgroundColor(context.getResources().getColor(
					R.color.transparent));
		}

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
		boolean intervalWifi = period.is_wifi()
				&& period.is_intervalConnectWifi();
		ImageView wifiImgView = (ImageView) rowView
				.findViewById(R.id.imageViewWifi);
		tintViewIcon(wifiImgView, R.drawable.ic_action_wifi, !period.is_wifi(),
				intervalWifi);

		boolean intervalMob = period.is_mobileData()
				&& period.is_intervalConnectMobData();
		ImageView mobileDataView = (ImageView) rowView
				.findViewById(R.id.imageViewMobileData);
		tintViewIcon(mobileDataView, R.drawable.ic_action_mobile_data,
				!period.is_mobileData(), intervalMob);

		ImageView btView = (ImageView) rowView
				.findViewById(R.id.imageViewBluetooth);
		tintViewIcon(btView, R.drawable.ic_action_bluetooth1,
				!period.is_bluetooth(), false);

		ImageView volView = (ImageView) rowView
				.findViewById(R.id.imageViewVolume);
		tintViewIcon(volView, R.drawable.ic_action_volume_up,
				!period.is_volume(), false);

		if (!_enabled) {
			ViewUtils.setControlsEnabled(_enabled, (ViewGroup) rowView);
		}
		return rowView;
	}

	private void tintViewIcon(ImageView imageView, int iconResourceId,
			boolean tintIt, boolean intervals) {

		Drawable icon = ViewUtils.getTintedIcon(context, tintIt,
				R.color.button_unchecked, iconResourceId);
		//
		// int tint = context.getResources().getColor(R.color.button_unchecked);
		//
		// // re-reading the icon from the resource seems to be the only way to
		// // avoid tiniting
		// // ALL the images in the other rowViews!
		// // see http://www.curious-creature.org/2009/05/02/drawable-mutations/
		// Drawable icon = context.getResources().getDrawable(iconResourceId);
		//
		// if (tintIt) {
		// icon.mutate().setColorFilter(tint, Mode.MULTIPLY);
		// } else {
		// icon.mutate().clearColorFilter();
		// }

		if (!intervals) {
			imageView.setImageDrawable(icon);
		} else {

			Drawable[] layers = new Drawable[2];
			layers[0] = icon;
			layers[1] = context.getResources()
					.getDrawable(R.drawable.intervals);
			LayerDrawable layerDrawable = new LayerDrawable(layers);
			imageView.setImageDrawable(layerDrawable);
		}

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

			// itemToUpdate.set_intervalConnect(item.is_intervalConnect());

			itemToUpdate.set_startTimeMillis(item.get_startTimeMillis());
			itemToUpdate.set_endTimeMillis(item.get_endTimeMillis());
			itemToUpdate.set_weekDays(item.get_weekDays());

			itemToUpdate.set_mobileData(item.is_mobileData());
			itemToUpdate.set_wifi(item.is_wifi());
			itemToUpdate.set_bluetooth(item.is_bluetooth());
			itemToUpdate.set_volume(item.is_volume());

			itemToUpdate.set_intervalConnectWifi(item.is_intervalConnectWifi());
			itemToUpdate.set_intervalConnectMobData(item
					.is_intervalConnectMobData());

			try {
				itemToUpdate.set_active(itemToUpdate.isActiveNow());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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

	public void moveUp(int originalPosition) {
		EnabledPeriod period = values.get(originalPosition);

		values.remove(originalPosition);

		values.add(originalPosition - 1, period);

		notifyDataSetChanged();
	}

	public void moveDown(int originalPosition) {
		EnabledPeriod period = values.get(originalPosition);

		values.remove(originalPosition);

		values.add(originalPosition + 1, period);

		notifyDataSetChanged();
	}

	public void setItemsEnabled(boolean enabled) {
		_enabled = enabled;
	}
}
