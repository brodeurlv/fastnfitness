package com.easyfitness.graph;

import java.util.ArrayList;
import java.util.List;

import com.easyfitness.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

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
        mChart.getAxisLeft().setStartAtZero(false);
        mChart.getAxisRight().setStartAtZero(false);
	}
	
	public void draw(ArrayList<String> xVals,	ArrayList<Entry> yVals) {
		mChart.clear();

		LineDataSet set1 = new LineDataSet(yVals, mChartName);
        set1.setLineWidth(4f);
        set1.setCircleSize(6f);
        set1.setFillAlpha(65);

		List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
		dataSets.add(set1); // add the datasets

		// Create a data object with the datasets
		LineData data = new LineData(xVals, dataSets);

		// Set data
        mChart.setData(data);
        mChart.invalidate();
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
                    mChart.setVisibleXRangeMaximum(7); // allow 20 values to be displayed at once on the x-axis, not more
                    mChart.moveViewToX(mChart.getData().getXValCount() - 7); // set the left edge of the chart to x-index 10
                }
                break;
            case ZOOM_MONTH:
                mChart.fitScreen();
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum(30); // allow 20 values to be displayed at once on the x-axis, not more
                    mChart.moveViewToX(mChart.getData().getXValCount() - 30); // set the left edge of the chart to x-index 10
                }
                break;
            case ZOOM_YEAR:
                mChart.fitScreen();
                if (mChart.getData() != null) {
                     mChart.setVisibleXRangeMaximum(365); // allow 20 values to be displayed at once on the x-axis, not more
                    mChart.moveViewToX(mChart.getData().getXValCount() - 365); // set the left edge of the chart to x-index 10
                }
                break;
        }

        // refresh
        mChart.invalidate();
    }
	
	

}
