package com.easyfitness.graph;

import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Graph {
	
	private LineChart mChart = null;
	private String mChartName = null;

	public enum zoomType {ZOOM_ALL, ZOOM_YEAR, ZOOM_MONTH, ZOOM_WEEK}

	public Graph(LineChart chart, String name) {
		mChart = chart;
		mChartName = name;
		mChart.setDoubleTapToZoomEnabled(true);
        mChart.setHorizontalScrollBarEnabled(true);
        mChart.setVerticalScrollBarEnabled(true);
        mChart.setAutoScaleMinMaxEnabled(true);


        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setTypeface(mTfLight);
        //xAxis.setTextSize(10f);
        //xAxis.setTextColor(Color.);
        xAxis.setTextColor(ColorTemplate.getHoloBlue());
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        //xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1000 * 60 * 60 * 24); // ?
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("dd-MMM");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                //long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date((long) value));
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        //leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        //leftAxis.setAxisMinimum(0f);
        //leftAxis.setAxisMaximum(170f);
        //leftAxis.setYOffset(0);
        //leftAxis.setTextColor(Color.rgb(255, 192, 56));
        leftAxis.resetAxisMinimum();

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        //mChart.getAxisLeft().setAxisMinValue(0f);
        //mChart.getAxisRight().setAxisMinValue(0f);
    }

    public void draw(ArrayList<Entry> entries) {
        //mChart.clear();

        Collections.sort(entries, new EntryXComparator());

        //log.d("DEBUG", arrayToString(entries));

        LineDataSet set1 = new LineDataSet(entries, mChartName);
        set1.setLineWidth(3f);
        set1.setCircleRadius(4f);
        set1.setFillAlpha(150);

		/*List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets*/

		// Create a data object with the datasets
        LineData data = new LineData(set1);

		// Set data
        mChart.setData(data);
        //mChart.animateX(500, Easing.EasingOption.EaseInOutBack);    //refresh graph

        mChart.invalidate();
        //mChart.resetViewPortOffsets();
    }

    private String arrayToString(ArrayList<Entry> entries) {
        String output = "";
        String delimiter = "\n"; // Can be new line \n tab \t etc...
        for (int i = 0; i < entries.size(); i++) {
            output = output + entries.get(i).getY() + " / " + entries.get(i).getX() + delimiter;
        }

        return output;
    }

    public LineChart getLineChart() {
		return mChart;
	}

    public void setZoom(zoomType z) {
        switch (z) {
            case ZOOM_ALL:
                mChart.fitScreen();
                break;
            case ZOOM_WEEK:
                mChart.fitScreen();
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 7 * 60 * 60 * 24 * 1000); // allow 20 values to be displayed at once on the x-axis, not more
                    mChart.moveViewToX(mChart.getData().getXMax() + (1 - 7) * 60 * 60 * 24 * 1000); // set the left edge of the chart to x-index 10
                }
                break;
            case ZOOM_MONTH:
                mChart.fitScreen();
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 30 * 60 * 60 * 24 * 1000); // allow 30 values to be displayed at once on the x-axis, not more
                    mChart.moveViewToX(mChart.getData().getXMax() + (float) (1 - 30) * 60 * 60 * 24 * 1000); // set the left edge of the chart to x-index 10
                }
                break;
            case ZOOM_YEAR:
                mChart.fitScreen();
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 365 * 60 * 60 * 24 * 1000); // allow 365 values to be displayed at once on the x-axis, not more
                    mChart.moveViewToX(mChart.getData().getXMax() + (float) (1 - 365) * 60 * 60 * 24 * 1000); // set the left edge of the chart to x-index 10
                }
                break;
        }

        // refresh
        mChart.invalidate();
    }
	
	

}
