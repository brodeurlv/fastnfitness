package com.easyfitness.graph;

import android.content.Context;

import com.easyfitness.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;

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
        mChart.setHorizontalScrollBarEnabled(false);
        mChart.setVerticalScrollBarEnabled(false);
        mChart.setDrawBorders(true);
        mChart.setNoDataText(context.getString(R.string.no_chart_data_available));
        mChart.setExtraOffsets(0, 0, 0, 10);

        mContext = context;
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ColorTemplate.getHoloBlue());
        xAxis.setDrawAxisLine(true);
        xAxis.setTextSize(14);

        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity((float) 1);
        leftAxis.setTextSize(12);

        mChart.setFitBars(true);
        leftAxis.setAxisMinimum(0);

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

        BarDataSet set1 = new BarDataSet(entries, mChartName);
        set1.setColor(mContext.getResources().getColor(R.color.toolbar_background));

        // Create a data object with the datasets
        BarData data = new BarData(set1);

        data.setValueTextSize(12);
        data.setValueFormatter(new ValueFormatter() {
            private final DecimalFormat mFormat = new DecimalFormat("#.##");

            @Override
            public String getFormattedValue(float value) {
                return mFormat.format(value);
            }
        });

        // Set data
        mChart.setData(data);

        mChart.getAxisLeft().setAxisMinimum(0f);

        mChart.invalidate();
    }

    public BarChart getChart() {
        return mChart;
    }

    public void setGraphDescription(String description) {
        Description desc = new Description();
        desc.setText(description);
        desc.setTextSize(12);
        mChart.setDescription(desc);
    }


}
