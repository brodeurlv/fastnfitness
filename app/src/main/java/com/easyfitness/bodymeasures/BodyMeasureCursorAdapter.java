package com.easyfitness.bodymeasures;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyfitness.R;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class BodyMeasureCursorAdapter extends CursorAdapter {

	 private LayoutInflater mInflater;
	 private Context mContext = null;
    private int mFirstColorOdd = 0;

	 public BodyMeasureCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 }
	 
	 @Override
	 public void bindView(View view, Context context, Cursor cursor) {
		  TextView t0 = (TextView) view.findViewById(R.id.LIST_BODYMEASURE_ID);
	      t0.setText(cursor.getString(0));

		 TextView t1 = (TextView) view.findViewById(R.id.LIST_BODYMEASURE_DATE);
			 Date date;
			 try {
				 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				 dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				 date = dateFormat.parse(cursor.getString(1));

				 //SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
				 //dateFormat2.setTimeZone(TimeZone.getTimeZone("GMT"));
				 //t1.setText(DateFormat.getDateInstance().format(date));
				 DateFormat dateFormat3 = android.text.format.DateFormat.getDateFormat(mContext.getApplicationContext());
				 t1.setText(dateFormat3.format(date));
			 } catch (ParseException e) {
				 t1.setText("");
				 e.printStackTrace();
			 }
	
	      TextView t2 = (TextView) view.findViewById(R.id.LIST_BODYMEASURE_WEIGHT);
	      t2.setText(cursor.getString(3));

         if(cursor.getPosition()%2==mFirstColorOdd) {
             view.setBackgroundColor(context.getResources().getColor(R.color.background_odd));
         }
         else {
             view.setBackgroundColor(context.getResources().getColor(R.color.background_even));
         }
	 }
	 
	 @Override
	 public View newView(Context context, Cursor cursor, ViewGroup parent) {
		 return mInflater.inflate(R.layout.bodymeasure_row, parent, false);
	 }	 
	 
}