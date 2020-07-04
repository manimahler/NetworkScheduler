package com.manimahler.android.scheduler3g;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
		final View rowView = inflater.inflate(R.layout.enabled_period, parent, false);

		final ScheduledPeriod period = values.get(position);

		View skipBtn = rowView.findViewById(R.id.buttonSkip);

		if (period.is_skipped()) {

			// change to setBackground once support for < SDK v16 is dropped.
			skipBtn.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.ic_action_next));
		} else {
			skipBtn.setBackgroundColor(context.getResources().getColor(
					R.color.transparent));
		}

		View periodOnLedBtn = rowView.findViewById(R.id.buttonOn);
		if (period.is_active()) {

			// change to setBackground once support for < SDK v16 is dropped.
			periodOnLedBtn.setBackgroundDrawable(context.getResources().getDrawable(
					R.drawable.led_red));
		} else {
			periodOnLedBtn.setBackgroundColor(context.getResources().getColor(
					R.color.transparent));
		}

		View showPeriodBtn = rowView.findViewById(R.id.buttonEdit);

		showPeriodBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showPeriodDetails(period);
			}
		});

		View showContextMenuBtn = rowView.findViewById(R.id.buttonContextMenu);

		showContextMenuBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rowView.showContextMenu();
			}
		});

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

		TextView nextDayView = (TextView) rowView
				.findViewById(R.id.text_start_nextday);

		setPeriodItemTimes(period, startView, stopView, onView, offView,
				nextDayView);

		// week days
		TextView weekDayView = (TextView) rowView.findViewById(R.id.bottomLine);

		String weekdayText = DateTimeUtils.getWeekdaysText(context,
				period.get_weekDays(), context.getString(R.string.everyday),
				context.getString(R.string.never));

		Log.d(TAG, "Week text: " + weekdayText);

		weekDayView.setText(weekdayText);

		// Wi-Fi - tinting the icons
		boolean intervalWifi = period.is_wifi()
				&& period.is_intervalConnectWifi();
		ImageView wifiImgView = (ImageView) rowView
				.findViewById(R.id.imageViewWifi);
		tintViewIcon(wifiImgView, R.drawable.ic_action_wifi, !period.is_wifi(),
				intervalWifi, period.is_overrideIntervalWifi(),
				!period.is_enableRadios());

		// Mobile Data: only below lollipop or if rooted:
		ImageView mobileDataView = (ImageView) rowView
				.findViewById(R.id.imageViewMobileData);
		
		if (ConnectionUtils.canToggleMobileData(context)) {
			boolean intervalMob = period.is_mobileData()
					&& period.is_intervalConnectMobData();
			
			tintViewIcon(mobileDataView, R.drawable.ic_action_mobile_data,
					!period.is_mobileData(), intervalMob,
					period.is_overrideIntervalMob(), !period.is_enableRadios());
		}
		else {
			LinearLayout topLine = (LinearLayout) rowView.findViewById(R.id.topLine);
			topLine.removeView(mobileDataView);
		}
		
		// Bluetooth
		ImageView btView = (ImageView) rowView
				.findViewById(R.id.imageViewBluetooth);
		boolean intervalBt = period.is_bluetooth()
				&& period.is_intervalConnectBluetooth();
		tintViewIcon(btView, R.drawable.ic_action_bluetooth1,
				!period.is_bluetooth(), intervalBt, false, !period.is_enableRadios());

		// Ringer sound
		ImageView volView = (ImageView) rowView
				.findViewById(R.id.imageViewVolume);
		tintVolumeIcon(volView, !period.is_volume(),
				period.is_vibrateWhenSilent() && period.is_volume(),
				!period.is_enableRadios());

		if (!_enabled) {
			ViewUtils.setControlsEnabled(context, _enabled,
					(ViewGroup) rowView, true);
		}

		if (!period.is_schedulingEnabled()) {
			ViewUtils.setControlsEnabled(context, false, (ViewGroup) rowView,
					false);
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

	public void showPeriodDetails(ScheduledPeriod item) {
		FragmentManager fm = ((FragmentActivity)context).getSupportFragmentManager();
		SchedulePeriodFragment schedulePeriodFragment = SchedulePeriodFragment.newInstance(item);

		schedulePeriodFragment.show(fm, "fragment_schedule_period");
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

	private void tintVolumeIcon(ImageView imageView, boolean tintIt,
			boolean vibrate, boolean strikeThrough) {

		int iconResourceId = R.drawable.ic_action_volume_up;

		Drawable icon = ViewUtils.getTintedIcon(context, tintIt,
				R.color.button_unchecked, iconResourceId);

		ArrayList<Drawable> iconList = new ArrayList<Drawable>(3);

		iconList.add(icon);

		if (vibrate) {
			Drawable vibrateDrawable = context.getResources().getDrawable(
					R.drawable.ic_action_vibrate_mini);

			//int insetLeft = (int) Math.round(icon.getIntrinsicWidth() * 0.6);
			//int insetTop = (int) Math.round(icon.getIntrinsicHeight() * 0.6);
			//int insetBottom = 0;
			//InsetDrawable smallVibrator = new InsetDrawable(vibrateDrawable,
			//		insetLeft, insetTop, 0, insetBottom);

			iconList.add(vibrateDrawable);
		}

		if (!tintIt && strikeThrough) {
			Drawable strike = context.getResources().getDrawable(
					R.drawable.ic_strikethrough);

			iconList.add(strike);
		}

		Drawable[] layers = iconList.toArray(new Drawable[iconList.size()]);
		LayerDrawable layerDrawable = new LayerDrawable(layers);

		imageView.setImageDrawable(layerDrawable);
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
		LayerDrawable layerDrawable = new LayerDrawable(layers);

		imageView.setImageDrawable(layerDrawable);
	}

	private void setPeriodItemTimes(ScheduledPeriod period, TextView startView,
			TextView stopView, TextView onView, TextView offView,
			TextView nextDayText) {

		int tint = R.color.weak_grey_transparent;

		int green = context.getResources().getColor(R.color.on_green);
		int red = context.getResources().getColor(R.color.off_red);

		String startTime = DateTimeUtils.getHourMinuteText(this.context,
				period.get_startTimeMillis());
		String stopTime = DateTimeUtils.getHourMinuteText(this.context,
				period.get_endTimeMillis());

		if (period.is_scheduleStart() && period.is_scheduleStop()) {
			if (period.is_enableRadios()) {
				// both on (normal)
				startView.setText(startTime);
				stopView.setText(stopTime);
				// stopView.setTextColor(tint);
				// offView.setTextColor(tint);
				onView.setTextColor(green);
				offView.setTextColor(red);
			} else {
				// both on (swap start and end time)
				startView.setText(stopTime);
				stopView.setText(startTime);

				// startView.setTextColor(tint);
				// onView.setTextColor(tint);
				onView.setTextColor(red);
				offView.setTextColor(green);

				// and swap the on-off texts
				onView.setText(R.string.off);
				offView.setText(R.string.on);
			}

			if (period.deactivationOnNextDay()) {
				nextDayText.setText(context.getString(R.string.next_day));
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
			if (period.is_schedulingEnabled()) {
				period.set_active(period.isActiveNow());
			}
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
		itemToUpdate.set_enableRadios(fromItem.is_enableRadios());
		itemToUpdate.set_weekDays(fromItem.get_weekDays());

		itemToUpdate.set_mobileData(fromItem.is_mobileData());
		itemToUpdate.set_wifi(fromItem.is_wifi());
		itemToUpdate.set_bluetooth(fromItem.is_bluetooth());
		itemToUpdate.set_volume(fromItem.is_volume());

		itemToUpdate.set_intervalConnectWifi(fromItem.is_intervalConnectWifi());
		itemToUpdate.set_intervalConnectMobData(fromItem
				.is_intervalConnectMobData());
		
		itemToUpdate.set_intervalConnectBluetooth(fromItem
				.is_intervalConnectBluetooth());

		itemToUpdate.set_vibrateWhenSilent(fromItem.is_vibrateWhenSilent());

		itemToUpdate.set_skipped(fromItem.is_skipped());

		itemToUpdate.set_schedulingEnabled(fromItem.is_schedulingEnabled());

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
