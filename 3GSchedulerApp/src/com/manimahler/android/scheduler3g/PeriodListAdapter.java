package com.manimahler.android.scheduler3g;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PeriodListAdapter extends ArrayAdapter<ScheduledPeriod> {

	private static final String TAG = PeriodListAdapter.class.getSimpleName();

	private final Context context;
	private ArrayList<ScheduledPeriod> values;

	boolean _enabled;

	public PeriodListAdapter(Context context, ArrayList<ScheduledPeriod> list) {
		super(context, R.layout.enabled_period, list);
		this.context = context;
		values = list;

		_enabled = true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.enabled_period, parent, false);

		ScheduledPeriod period = values.get(position);

		View button = rowView.findViewById(R.id.buttonOn);
		if (period.is_active()) {

			// change to setBackground once support for < SDK v16 is dropped.
			button.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.led_red));
		} else {
			button.setBackgroundColor(context.getResources().getColor(
					R.color.transparent));
		}

		View skip = rowView.findViewById(R.id.buttonSkip);

		if (period.is_skipped()) {

			// change to setBackground once support for < SDK v16 is dropped.
			skip.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.ic_action_next));
		} else {
			skip.setBackgroundColor(context.getResources().getColor(
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

		Log.d(TAG, "Week text: " + weekdayText);

		weekDayView.setText(weekdayText);

		// tinting the icons
		boolean intervalWifi = period.is_wifi()
				&& period.is_intervalConnectWifi();
		ImageView wifiImgView = (ImageView) rowView
				.findViewById(R.id.imageViewWifi);
		tintViewIcon(wifiImgView, R.drawable.ic_action_wifi, !period.is_wifi(),
				intervalWifi, period.is_overrideIntervalWifi(),
				!period.activeIsEnabled());

		boolean intervalMob = period.is_mobileData()
				&& period.is_intervalConnectMobData();
		ImageView mobileDataView = (ImageView) rowView
				.findViewById(R.id.imageViewMobileData);
		tintViewIcon(mobileDataView, R.drawable.ic_action_mobile_data,
				!period.is_mobileData(), intervalMob,
				period.is_overrideIntervalMob(), !period.activeIsEnabled());

		ImageView btView = (ImageView) rowView
				.findViewById(R.id.imageViewBluetooth);
		tintViewIcon(btView, R.drawable.ic_action_bluetooth1,
				!period.is_bluetooth(), false, false, !period.activeIsEnabled());

		ImageView volView = (ImageView) rowView
				.findViewById(R.id.imageViewVolume);
		tintViewIcon(volView, R.drawable.ic_action_volume_up,
				!period.is_volume(), false, false, !period.activeIsEnabled());

		if (!_enabled) {
			ViewUtils.setControlsEnabled(_enabled, (ViewGroup) rowView);
		}
		return rowView;
	}

	public void resetPeriods(ArrayList<ScheduledPeriod> list) {
		super.clear();

		// NOTE: addAll(list) requires API level 11
		// super.addAll(list);
		for (ScheduledPeriod scheduledPeriod : list) {
			super.add(scheduledPeriod);
		}

		notifyDataSetChanged();
	}

	public ArrayList<ScheduledPeriod> getPeriods() {
		return values;
	}

	public void updateItem(ScheduledPeriod item) {
		if (item.get_id() < 0) {
			item.set_id(this.getMaxId() + 1);
			this.add(item);

			updateActiveProperty(item);
		} else {
			ScheduledPeriod itemToUpdate = findItem(item.get_id());

			if (itemToUpdate == null) {
				Log.e(TAG, "Item to update not found");
			}

			copyProperties(itemToUpdate, item);

			updateActiveProperty(itemToUpdate);
		}
		notifyDataSetChanged();
	}

	public void removeAt(int position) {
		ScheduledPeriod itemToRemove = this.getItem(position);

		this.remove(itemToRemove);

		notifyDataSetChanged();
	}

	public void moveUp(int originalPosition) {
		ScheduledPeriod period = values.get(originalPosition);

		values.remove(originalPosition);

		values.add(originalPosition - 1, period);

		notifyDataSetChanged();
	}

	public void moveDown(int originalPosition) {
		ScheduledPeriod period = values.get(originalPosition);

		values.remove(originalPosition);

		values.add(originalPosition + 1, period);

		notifyDataSetChanged();
	}

	public void setItemsEnabled(boolean enabled) {
		_enabled = enabled;
	}

	private void tintViewIcon(ImageView imageView, int iconResourceId,
			boolean tintIt, boolean intervals, boolean overrideIntervals,
			boolean strikeThrough) {

		Drawable icon = ViewUtils.getTintedIcon(context, tintIt,
				R.color.button_unchecked, iconResourceId);

		ArrayList<Drawable> iconList = new ArrayList<Drawable>(3);

		iconList.add(icon);

		if (intervals) {
			Drawable intervalDrawable = ViewUtils.getTintedIcon(context,
					overrideIntervals, R.color.black, R.drawable.intervals);

			// if override intervals...

			iconList.add(intervalDrawable);
		}

		if (!tintIt && strikeThrough) {
			Drawable strike = context.getResources().getDrawable(
					R.drawable.ic_strikethrough);

			iconList.add(strike);
		}

		Drawable[] layers = iconList.toArray(new Drawable[iconList.size()]);
		// if (!intervals) {
		// imageView.setImageDrawable(icon);
		// } else {
		//
		//
		// layers[0] = icon;
		// layers[1] = context.getResources()
		// .getDrawable(R.drawable.intervals);
		// layers[2] =
		// context.getResources().getDrawable(R.drawable.strikethrough);
		//
		// LayerDrawable layerDrawable = new LayerDrawable(layers);
		// imageView.setImageDrawable(layerDrawable);
		// }

		LayerDrawable layerDrawable = new LayerDrawable(layers);
		// if (layerDrawable.getNumberOfLayers() == 3)
		// {
		// layerDrawable.setLayerInset(2, 0, 0, 0, 17);
		// }

		imageView.setImageDrawable(layerDrawable);
	}

	private void setPeriodItemTimes(ScheduledPeriod period, TextView startView,
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

	private void updateActiveProperty(ScheduledPeriod period) {
		try {
			period.set_active(period.isActiveNow());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void copyProperties(ScheduledPeriod itemToUpdate,
			ScheduledPeriod fromItem) {
		itemToUpdate.set_id(fromItem.get_id());
		itemToUpdate.set_name(fromItem.get_name());

		itemToUpdate.set_scheduleStart(fromItem.is_scheduleStart());
		itemToUpdate.set_scheduleStop(fromItem.is_scheduleStop());

		itemToUpdate.set_startTimeMillis(fromItem.get_startTimeMillis());
		itemToUpdate.set_endTimeMillis(fromItem.get_endTimeMillis());
		itemToUpdate.set_weekDays(fromItem.get_weekDays());

		itemToUpdate.set_mobileData(fromItem.is_mobileData());
		itemToUpdate.set_wifi(fromItem.is_wifi());
		itemToUpdate.set_bluetooth(fromItem.is_bluetooth());
		itemToUpdate.set_volume(fromItem.is_volume());

		itemToUpdate.set_intervalConnectWifi(fromItem.is_intervalConnectWifi());
		itemToUpdate.set_intervalConnectMobData(fromItem
				.is_intervalConnectMobData());

		itemToUpdate.set_vibrateWhenSilent(fromItem.is_vibrateWhenSilent());

		itemToUpdate.set_skipped(fromItem.is_skipped());

		// re-set the userOverride flag (the user just pressed ok to these
		// settings)!
	}

	private ScheduledPeriod findItem(int id) {

		for (int i = 0; i < this.getCount(); i++) {
			ScheduledPeriod current = this.getItem(i);
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
}
