package com.easyfitness.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.TimeZone;

import static android.text.format.DateFormat.getDateFormat;

public class DateGraphMarkerView extends MarkerView {

    private final TextView tvContent;
    private final TextView tvDate;
    private final DecimalFormat mFormat = new DecimalFormat("#.##");
    /**
     * Screen width in pixels.
     */
    private final int uiScreenWidth;
    private Chart lineChart = null;
    private MPPointF mOffset;

    public DateGraphMarkerView(Context context, int layoutResource, Chart chart) {
        super(context, layoutResource);

        // find your layout components
        tvContent = findViewById(R.id.tvContent);
        tvDate = findViewById(R.id.tvDate);
        uiScreenWidth = getResources().getDisplayMetrics().widthPixels;
        lineChart = chart;
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        DateFormat dateFormat3 = getDateFormat(getContext().getApplicationContext());
        dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"));
        tvDate.setText(dateFormat3.format(new Date((long) DateConverter.nbMilliseconds(e.getX()))));
        tvContent.setText(mFormat.format(e.getY()));

        // this will perform necessary layouting
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {

        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2.0f), -getHeight());
        }

        return mOffset;
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        // take offsets into consideration
        int lineChartWidth = 0;
        int lineChartHeight = 0;
        float offsetX = getOffset().getX();
        float offsetY = getOffset().getY();

        float width = getWidth();
        float height = getHeight();

        if (lineChart != null) {
            lineChartWidth = lineChart.getWidth();
            lineChartHeight = lineChart.getHeight();
        }

        //Si ca deborde sur les cotés
        if (posX + offsetX < 0) {
            offsetX = -posX;
        } else if (posX + width + offsetX > lineChartWidth) {
            offsetX = lineChartWidth - posX - width;
        }
        posX += offsetX;

        // Si ca deborde en haut ou en bas
        if (posY + offsetY < 0) {
            posY = posY + 20;
        } else if (posY + height + offsetY > lineChartHeight) {
            posY += lineChartHeight - posY - height;
        } else {
            posY += offsetY;
        }

        // translate to the correct position and draw
        canvas.translate(posX, posY);
        draw(canvas);
        canvas.translate(-posX, -posY);
    }
}
