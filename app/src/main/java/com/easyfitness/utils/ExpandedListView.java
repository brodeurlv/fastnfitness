package com.easyfitness.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

public class ExpandedListView extends ListView {
    private final int oldCount = 0;
    private ViewGroup.LayoutParams params;

    public ExpandedListView(Context context) {
        super(context);
    }

    public ExpandedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSpec = heightMeasureSpec;

        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        }

        super.onMeasure(widthMeasureSpec, heightSpec);
    }

    /*
    @Override
    protected void onDraw(Canvas canvas) {
        if (getCount() != oldCount && getCount() != 0) {
            int height = getChildAt(0).getHeight() + 1;
            oldCount = getCount();
            params = getLayoutParams();
            params.height = getCount() * height;
            setLayoutParams(params);
            if (getCount() != oldCount) {
                params = getLayoutParams();
                oldCount = getCount();
                int totalHeight = 0;
                for (int i = 0; i < getCount(); i++) {
                    this.measure(0, 0);
                    totalHeight += getMeasuredHeight();
                }

                params = getLayoutParams();
                params.height = totalHeight + (getDividerHeight() * (getCount() - 1));
                setLayoutParams(params);
            }

            super.onDraw(canvas);
        }

        super.onDraw(canvas);
    }*/

}
