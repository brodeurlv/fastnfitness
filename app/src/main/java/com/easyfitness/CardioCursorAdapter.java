package com.easyfitness;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.easyfitness.DAO.DAOCardio;
import com.easyfitness.DAO.DAOUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static android.text.format.DateFormat.getDateFormat;

public class CardioCursorAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private Context mContext = null;

    public CardioCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        if (cursor.getPosition() % 2 == 1) {
            view.setBackgroundColor(context.getResources().getColor(R.color.background_odd));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.background_even));
        }

        TextView t1 = view.findViewById(R.id.DATE_CELL);
        Date date;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = dateFormat.parse(cursor.getString(cursor.getColumnIndex(DAOCardio.DATE)));

            DateFormat dateFormat3 = getDateFormat(mContext.getApplicationContext());
            dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"));
            t1.setText(dateFormat3.format(date));

        } catch (ParseException e) {
            t1.setText("");
            e.printStackTrace();
        }

        TextView t10 = view.findViewById(R.id.TIME_CELL);
        t10.setText(cursor.getString(cursor.getColumnIndex(DAOCardio.TIME)));

        TextView t2 = view.findViewById(R.id.MACHINE_CELL);
        t2.setText(cursor.getString(cursor.getColumnIndex(DAOCardio.EXERCISE)));

        TextView t3 = view.findViewById(R.id.SERIE_CELL);
        t3.setText(cursor.getString(cursor.getColumnIndex(DAOCardio.DISTANCE)));

        TextView t4 = view.findViewById(R.id.REPETITION_CELL);
        t4.setText(""); //cursor.getString(cursor.getColumnIndex(cursor.getColumnName(4))));

        TextView t5 = view.findViewById(R.id.POIDS_CELL);

        Date date2 = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        date2.setTime(Long.parseLong(cursor.getString(cursor.getColumnIndex(DAOCardio.DURATION))));
        t5.setText(dateFormat.format(date2));

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.row_fonte, parent, false);
    }

}
