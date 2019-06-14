package com.easyfitness.graph;

import android.content.Context;

import com.easyfitness.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BarGraph {

    private BarChart mChart = null;
    private String mChartName = null;
    private Context mContext = null;

    public BarGraph(Context context, BarChart chart, String name) {
        mChart = chart;
        mChartName = name;
        //mChart.setDoubleTapToZoomEnabled(true);
        mChart.setHorizontalScrollBarEnabled(true);
        mChart.setVerticalScrollBarEnabled(true);
        //mChart.setAutoScaleMinMaxEnabled(true);
        mChart.setDrawBorders(true);

        //IMarker marker = new BarGraphMarkerView(mChart.getContext(), R.layout.graph_markerview, mChart);
        //mChart.setMarker(marker);

        mContext = context;
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ColorTemplate.getHoloBlue());
        xAxis.setDrawAxisLine(false);
        //xAxis.setDrawGridLines(true);
        //xAxis.setCenterAxisLabels(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        /*xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("dd-MMM"); // HH:mm:ss

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                //long millis = TimeUnit.HOURS.toMillis((long) value);
                mFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date tmpDate = new Date((long) DateConverter.nbMilliseconds(value)); // Convert days in milliseconds
                return mFormat.format(tmpDate);
            }
        });*/

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity((float) 1);

        mChart.setFitBars(true);
        leftAxis.setAxisMinimum(0f);

        mChart.getAxisRight().setEnabled(false);
    }

    public void draw(List<BarEntry> entries, ArrayList<String> xAxisLabel) {
        mChart.clear();
        if (entries.isEmpty()) {
            return;
        }

        XAxis xAxis = this.mChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));


        Collections.sort(entries, new EntryXComparator());

        //Log.d("DEBUG", arrayToString(entries));

        BarDataSet set1 = new BarDataSet(entries, mChartName);
        set1.setColor(mContext.getResources().getColor(R.color.toolbar_background));

        // Create a data object with the datasets
        BarData data = new BarData(set1);

        data.setValueTextSize(12);
        data.setValueFormatter(new IValueFormatter() {
            private DecimalFormat mFormat = new DecimalFormat("#.## kg");

            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return mFormat.format(value);
            }
        });

        // Set data
        mChart.setData(data);

        mChart.getAxisLeft().setAxisMinimum(0f);

        mChart.invalidate();
        //mChart.animateY(500, Easing.EasingOption.EaseInBack);    //refresh graph

    }

    public BarChart getChart() {
        return mChart;
    }


}
