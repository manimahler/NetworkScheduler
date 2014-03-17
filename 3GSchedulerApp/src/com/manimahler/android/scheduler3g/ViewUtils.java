package com.manimahler.android.scheduler3g;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {

	public static void setControlsEnabled(boolean enable, ViewGroup vg) {
		for (int i = 0; i < vg.getChildCount(); i++) {
			View child = vg.getChildAt(i);
			if (child instanceof ViewGroup) {
				setControlsEnabled(enable, (ViewGroup) child);
			} else {

				child.setClickable(enable);

				float alpha = 1;

				if (!enable) {
					alpha = 0.3f;
				}
				child.setAlpha(alpha);

				child.setFocusable(enable);

			}
		}
	}
	
	public static Drawable getTintedIcon(Context context, boolean tint, int colorResId, int iconResId)
	{
		int tintColor = context.getResources().getColor(colorResId);

		// re-reading the icon from the resource seems to be the only way to
		// avoid tinting ALL the images in the other rowViews!
		// see http://www.curious-creature.org/2009/05/02/drawable-mutations/
		Drawable icon = context.getResources().getDrawable(iconResId);

		if (tint) {
			icon.mutate().setColorFilter(tintColor, Mode.MULTIPLY);
		} else {
			icon.mutate().clearColorFilter();
		}
		
		return icon;
	}
}
