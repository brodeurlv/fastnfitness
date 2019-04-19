package com.easyfitness;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ProfilWeightCursorAdapter extends CursorAdapter {

    BtnClickListener mDeleteClickListener = null;
    private LayoutInflater mInflater;
    private int mFirstColorOdd = 0;
    private Context mContext = null;

    public ProfilWeightCursorAdapter(Context context, Cursor c, int flags, BtnClickListener deleteFunction) {
        super(context, c, flags);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mDeleteClickListener = deleteFunction;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        CardView cdView = view.findViewById(R.id.CARDVIEW);


        if (cursor.getPosition() % 2 == mFirstColorOdd) {
            cdView.setBackgroundColor(context.getResources().getColor(R.color.background_even));
        } else {
            cdView.setBackgroundColor(context.getResources().getColor(R.color.background));
        }

        TextView t1 = view.findViewById(R.id.DATE_CELL);
        Date date;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = dateFormat.parse(cursor.getString(1));

            //SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
            //dateFormat2.setTimeZone(TimeZone.getTimeZone("GMT"));
            //t1.setText(dateFormat2.format(date));
            DateFormat dateFormat3 = android.text.format.DateFormat.getDateFormat(mContext.getApplicationContext());
            dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"));
            t1.setText(dateFormat3.format(date));
        } catch (ParseException e) {
            t1.setText("");
            e.printStackTrace();
        }

        TextView t2 = view.findViewById(R.id.WEIGHT_CELL);
        //t2.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2))));
        t2.setText(cursor.getString(2));

        ImageView deletImg = view.findViewById(R.id.deleteButton);
        deletImg.setTag(cursor.getLong(0));
        deletImg.setOnClickListener(v -> {
            if (mDeleteClickListener != null)
                mDeleteClickListener.onBtnClick((long) v.getTag());
        });

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.row_profilweight, parent, false);
    }

    /*
     * @pColor : si 1 alors affiche la couleur Odd en premier. Sinon, a couleur Even.
     */
    public void setFirstColorOdd(int pColor) {
        mFirstColorOdd = pColor;
    }
}
