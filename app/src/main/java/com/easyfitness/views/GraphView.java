package com.easyfitness.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.easyfitness.R;
import com.easyfitness.graph.DateGraphMarkerView;
import com.easyfitness.graph.ZoomType;
import com.easyfitness.utils.DateConverter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

public class GraphView extends ConstraintLayout {
    private View rootView;
    private AppCompatSpinner mZoomSpinner;

    private int mType;
    private LineChart mChart;
    private String mChartName;
    private String mName;
    private ZoomType mZoom = ZoomType.ZOOM_ALL;
    private final AdapterView.OnItemSelectedListener itemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            int pos = mZoomSpinner.getSelectedItemPosition();
            setZoom(ZoomType.fromInteger(pos));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    public GraphView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public GraphView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GraphView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        rootView = inflate(context, R.layout.graph_view, this);
        mZoomSpinner = rootView.findViewById(R.id.graphview_spinner);
        mChart = rootView.findViewById(R.id.graphview_linechart);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.GraphView,
                0, 0);

        mZoomSpinner.setOnItemSelectedListener(itemSelected);

        try {
            mType = a.getInteger(R.styleable.GraphView_graphType, 0);
            setType(mType);
            mChartName = a.getString(R.styleable.GraphView_name);
            setName(mChartName);
        } finally {
            a.recycle();
        }

        // Graph design
        mChart.setDoubleTapToZoomEnabled(true);
        mChart.setHorizontalScrollBarEnabled(true);
        mChart.setVerticalScrollBarEnabled(true);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.setDrawBorders(true);
        mChart.setNoDataText(context.getString(R.string.no_chart_data_available));
        mChart.setExtraOffsets(0, 0, 0, 10);

        IMarker marker = new DateGraphMarkerView(mChart.getContext(), R.layout.graph_markerview, mChart);
        mChart.setMarker(marker);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setEnabled(false);
        l.setTextSize(12);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ColorTemplate.getHoloBlue());
        xAxis.setTextSize(14);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1); // 1 jour
        xAxis.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd-MMM"); // HH:mm:ss

            @Override
            public String getFormattedValue(float value) {
                //long millis = TimeUnit.HOURS.toMillis((long) value);
                mFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date tmpDate = new Date((long) DateConverter.nbMilliseconds(value)); // Convert days in milliseconds
                return mFormat.format(tmpDate);
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(0.5f);
        leftAxis.setTextSize(12);
        leftAxis.resetAxisMinimum();

        mChart.getAxisRight().setEnabled(false);
    }

    public void setType(int value) {
        mType = value;
    }

    public void setZoom(ZoomType z) {
        mZoom = z;
        mChart.fitScreen();
        mChart.resetZoom();
        switch (z) {
            case ZOOM_ALL:
                break;
            case ZOOM_WEEK:
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 7); // allow 7 values to be displayed at once on the x-axis, not more
                    mChart.moveViewToX(mChart.getData().getXMax() - (float) 7); // set the left edge of the chart to x-index 10
                }
                break;
            case ZOOM_MONTH:
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 30); // allow 30 values to be displayed at once on the x-axis, not more
                    mChart.moveViewToX(mChart.getData().getXMax() - (float) 30); // set the left edge of the chart to x-index 10
                }
                break;
            case ZOOM_YEAR:
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 365); // allow 365 values to be displayed at once on the x-axis, not more
                    mChart.moveViewToX(mChart.getData().getXMax() - (float) 365); // set the left edge of the chart to x-index 10
                }
                break;
        }
    }

    public void clear() {
        mChart.clear();
    }

    public void draw(ArrayList<Entry> entries) {
        mChart.clear();
        if (entries.isEmpty()) {
            return;
        }

        Collections.sort(entries, new EntryXComparator());

        LineDataSet set1 = new LineDataSet(entries, mChartName);
        set1.setLineWidth(3f);
        set1.setCircleRadius(4f);
        set1.setDrawFilled(true);
        if (Utils.getSDKInt() >= 18) {
            // fill drawable only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_blue);
            set1.setFillDrawable(drawable);
        } else {
            set1.setFillColor(ColorTemplate.getHoloBlue());
        }
        set1.setFillAlpha(100);
        set1.setColor(getContext().getResources().getColor(R.color.toolbar_background));
        set1.setCircleColor(getContext().getResources().getColor(R.color.toolbar_background));

        // Create a data object with the datasets
        LineData data = new LineData(set1);
        data.setValueTextColor(getContext().getResources().getColor(R.color.cardview_title_color));
        data.setValueTextSize(12f);
        data.setValueFormatter(new DefaultValueFormatter(2));

        // Set data
        mChart.setData(data);
        //mChart.animateY(500, Easing.EasingOption.EaseInBack);    //refresh graph
        setZoom(mZoom); // Refresh zoom
    }

    public void setGraphDescription(String description) {
        Description desc = new Description();
        desc.setText(description);
        desc.setTextSize(12);
        mChart.setDescription(desc);
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}
