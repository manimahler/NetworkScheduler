package com.manimahler.android.scheduler3g;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

		TextView startView = (TextView) rowView
				.findViewById(R.id.textViewStartTime);
		startView.setText(DateTimeUtils.getHourMinuteText(this.context,
				period.get_startTimeMillis()));

		TextView stopView = (TextView) rowView
				.findViewById(R.id.TextViewStopTime);
		stopView.setText(DateTimeUtils.getHourMinuteText(this.context,
				period.get_endTimeMillis()));

		return rowView;
	}

	public void updateItem(EnabledPeriod item) {
		if (item.get_id() < 0) {
			
			item.set_id(this.getMaxId() + 1);
			
			this.add(item);

			notifyDataSetChanged();
		} else {
			EnabledPeriod itemToUpdate = findItem(item.get_id());

			if (itemToUpdate == null)
			{
				Log.e("PeriodListAdapter.updateItem", "Item to update not found");
			}
			
			itemToUpdate.set_id(item.get_id());
			itemToUpdate.set_startTimeMillis(item.get_startTimeMillis());
			itemToUpdate.set_endTimeMillis(item.get_endTimeMillis());
			itemToUpdate.set_weekDays(item.get_weekDays());
			
			itemToUpdate.set_mobileData(item.is_mobileData());
			itemToUpdate.set_wifi(item.is_wifi());
			itemToUpdate.set_bluetooth(item.is_bluetooth());

			notifyDataSetChanged();
		}
	}
	
	private EnabledPeriod findItem(int id)
	{
				
		for (int i = 0; i < this.getCount(); i++) {
			EnabledPeriod current = this.getItem(i);
			if (current.get_id() == id)
			{
				return current;
			}
		}
		
		return null;
	}
	
	private int getMaxId()
	{
		int result = 0;
		for (int i = 0; i < this.getCount(); i++) {
			int currentId = this.getItem(i).get_id();
			if (currentId > result)
			{
				result = currentId;
			}
		}
		
		return result;
	}
	//
	// public void addItem()
	// {
	// long start = DateTimeUtils.getNextTimeIn24hInMillis(6, 30);
	// long end = DateTimeUtils.getNextTimeIn24hInMillis(23, 30);
	//
	// EnabledPeriod newPeriod = new EnabledPeriod(true,start , end, new
	// boolean[7]);
	//
	// this.add(newPeriod);
	//
	// notifyDataSetChanged();
	// }

	public void removeAt(int position) {
		EnabledPeriod itemToRemove = this.getItem(position);
		
		this.remove(itemToRemove);
		
		notifyDataSetChanged();
	}
}
