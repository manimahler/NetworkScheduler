package com.manimahler.android.scheduler3g;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * center_vertical Gravity added
 * 
 * @author
 */
public class FlowLayout extends ViewGroup {

	private final List<Integer> mLineHeights = new ArrayList<Integer>();

	public static class LayoutParams extends LinearLayout.LayoutParams {

		public final int horizontalSpacing;
		public final int verticalSpacing;

		/**
		 * @param horizontalSpacing
		 *            Pixels between items, horizontally
		 * @param verticalSpacing
		 *            Pixels between items, vertically
		 */
		public LayoutParams(final int horizontalSpacing,
				final int verticalSpacing) {
			super(0, 0);
			this.horizontalSpacing = horizontalSpacing;
			this.verticalSpacing = verticalSpacing;
		}
	}

	public FlowLayout(final Context context) {
		super(context);
	}

	public FlowLayout(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec,
			final int heightMeasureSpec) {
		assert (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED);

		final int maxWidth = MeasureSpec.getSize(widthMeasureSpec)
				- getPaddingLeft() - getPaddingRight();
		int width = 0;
		int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop()
				- getPaddingBottom();
		final int count = getChildCount();
		int currentLineHeight = 0;
		int currentHeight = 0;
		mLineHeights.clear();

		int xpos = getPaddingLeft();
		int ypos = getPaddingTop();

		int childHeightMeasureSpec;
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
			childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
					MeasureSpec.AT_MOST);
		} else {
			childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				final LayoutParams lp = (LayoutParams) child.getLayoutParams();
				child.measure(MeasureSpec.makeMeasureSpec(maxWidth,
						MeasureSpec.AT_MOST), childHeightMeasureSpec);
				final int childw = child.getMeasuredWidth();
				currentHeight = Math.max(currentLineHeight,
						child.getMeasuredHeight() + lp.verticalSpacing);

				if (xpos + childw > maxWidth) {
					xpos = getPaddingLeft();
					ypos += currentLineHeight;
					mLineHeights.add(currentLineHeight);
					currentLineHeight = currentHeight;
				} else {
					width = Math.max(xpos + childw, width);
					currentLineHeight = currentHeight;
				}

				xpos += childw + lp.horizontalSpacing;
			}
		}
		mLineHeights.add(currentHeight);

		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
			height = ypos + currentLineHeight;
		} else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
			if (ypos + currentLineHeight < height) {
				height = ypos + currentLineHeight;
			}
		}

		if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
			width = maxWidth;
		}

		setMeasuredDimension(width, height);
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(1, 1); // default of 1px spacing
	}

	@Override
	protected boolean checkLayoutParams(final ViewGroup.LayoutParams p) {
		if (p instanceof LayoutParams) {
			return true;
		}
		return false;
	}

	@Override
	protected void onLayout(final boolean changed, final int l, final int t,
			final int r, final int b) {
		final int count = getChildCount();
		final int width = r - l;
		int xpos = getPaddingLeft();
		int ypos = getPaddingTop();
		int currentLine = 0;

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			// Height of current line
			int currentHeight = mLineHeights.get(currentLine);
			if (child.getVisibility() != GONE) {
				final int childw = child.getMeasuredWidth();
				final int childh = child.getMeasuredHeight();
				final LayoutParams lp = (LayoutParams) child.getLayoutParams();

				if (xpos + childw > width) {
					// New line
					xpos = getPaddingLeft();
					ypos += currentHeight;
					currentHeight = mLineHeights.get(++currentLine);
				}

				// Add yoffset to top and bottom position if center_vertical ||
				// center
				int yoffset = 0;
				if (lp.gravity == Gravity.CENTER_VERTICAL
						|| lp.gravity == Gravity.CENTER) {
					// Average of difference in height
					yoffset = (currentHeight - childh) / 2;
				}
				child.layout(xpos, ypos + yoffset, xpos + childw, ypos + childh
						+ yoffset);
				xpos += childw + lp.horizontalSpacing;
			}
		}
	}
}