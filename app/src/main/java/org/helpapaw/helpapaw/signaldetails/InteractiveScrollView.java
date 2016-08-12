package org.helpapaw.helpapaw.signaldetails;

/**
 * Created by iliyan on 8/12/16
 */

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ScrollView;

public class InteractiveScrollView extends ScrollView {

    OnBottomReachedListener listener;
    private boolean isAtTheBottom;

    public InteractiveScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        isAtTheBottom = false;
    }

    public InteractiveScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isAtTheBottom = false;
    }

    public InteractiveScrollView(Context context) {
        super(context);
        isAtTheBottom = false;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        View view = getChildAt(getChildCount() - 1);
        int diff = (view.getBottom() - (getHeight() + getScrollY()));

        if (listener != null) {
            if (diff == 0 && !isAtTheBottom) {
                listener.onBottomReached(true);
                isAtTheBottom = true;
            } else if (diff > getPixels(TypedValue.COMPLEX_UNIT_DIP, 10) && isAtTheBottom) {
                listener.onBottomReached(false);
                isAtTheBottom = false;
            }
        }

        super.onScrollChanged(l, t, oldl, oldt);
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
        listener = onBottomReachedListener;
    }

    public int getPixels(int unit, float size) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) TypedValue.applyDimension(unit, size, metrics);
    }

    public boolean canScroll() {
        View child = getChildAt(0);
        if (child != null) {
            int childHeight = child.getHeight();
            return getHeight() < childHeight;
        }
        return false;
    }

    /**
     * Event listener.
     */
    public interface OnBottomReachedListener {
        void onBottomReached(boolean isBottomReached);
    }

}
