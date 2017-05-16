package com.manimahler.android.scheduler3g;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {

	public static void setControlsEnabled(Context context, 
			boolean enable, ViewGroup vg, boolean includeClickable) {
		for (int i = 0; i < vg.getChildCount(); i++) {
			
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				vg.setBackgroundColor(context.getResources().getColor(R.color.grey));
			}
			
			View child = vg.getChildAt(i);
			if (child instanceof ViewGroup) {
				setControlsEnabled(context, enable, (ViewGroup) child, includeClickable);
			} else {
				float alpha = 1;

				if (!enable) {
					alpha = 0.3f;
				}

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					setAlpha(child, alpha);
				}
				
				if (includeClickable) {
					child.setClickable(enable);
					child.setFocusable(enable);
				}
			}
		}
	}
	
	public static Drawable getTintedIcon(Context context, boolean tint,
			int colorResId, int iconResId) {
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
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void setAlpha(View view, float alpha) {
		view.setAlpha(alpha);
	}
}
