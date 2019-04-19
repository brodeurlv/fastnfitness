package com.easyfitness.bodymeasures;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.easyfitness.BtnClickListener;
import com.easyfitness.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static android.text.format.DateFormat.getDateFormat;

public class BodyMeasureCursorAdapter extends CursorAdapter {

    BtnClickListener mDeleteClickListener = null;
    private LayoutInflater mInflater;
    private Context mContext = null;

    public BodyMeasureCursorAdapter(Context context, Cursor c, int flags, BtnClickListener mD) {
        super(context, c, flags);
        mContext = context;
        mDeleteClickListener = mD;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView t0 = view.findViewById(R.id.LIST_BODYMEASURE_ID);
        t0.setText(cursor.getString(0));

        TextView t1 = view.findViewById(R.id.LIST_BODYMEASURE_DATE);
        Date date;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = dateFormat.parse(cursor.getString(1));

            //SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
            //dateFormat2.setTimeZone(TimeZone.getTimeZone("GMT"));
            //t1.setText(DateFormat.getDateInstance().format(date));
            DateFormat dateFormat3 = getDateFormat(mContext.getApplicationContext());
            dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"));
            t1.setText(dateFormat3.format(date));
        } catch (ParseException e) {
            t1.setText("");
            e.printStackTrace();
        }

        TextView t2 = view.findViewById(R.id.LIST_BODYMEASURE_WEIGHT);
        t2.setText(cursor.getString(3));

        CardView cdView = view.findViewById(R.id.CARDVIEW);

        int mFirstColorOdd = 0;
        if (cursor.getPosition() % 2 == mFirstColorOdd) {
            cdView.setBackgroundColor(context.getResources().getColor(R.color.background_even));
        } else {
            cdView.setBackgroundColor(context.getResources().getColor(R.color.background));
        }

        ImageView deletImg = view.findViewById(R.id.deleteButton);
        deletImg.setTag(cursor.getLong(0));
        deletImg.setOnClickListener(v -> {
            if (mDeleteClickListener != null)
                mDeleteClickListener.onBtnClick((long) v.getTag());
        });

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.bodymeasure_row, parent, false);
    }

}
