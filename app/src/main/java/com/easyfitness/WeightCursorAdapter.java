package com.easyfitness;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.easyfitness.DAO.DAOUtils;
import com.easyfitness.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class WeightCursorAdapter extends CursorAdapter {
	 
	 private LayoutInflater mInflater;
	 
	 public WeightCursorAdapter(Context context, Cursor c, int flags) {
	  super(context, c, flags);
	  mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 }
	 
	 @Override
	 public void bindView(View view, Context context, Cursor cursor) {
	 
	  if(cursor.getPosition()%2==1) {
	   view.setBackgroundColor(context.getResources().getColor(R.color.background_odd));
	  }
	  else {
	   view.setBackgroundColor(context.getResources().getColor(R.color.background_even));
	  }
	 
	  TextView t1 = view.findViewById(R.id.DATE_CELL);
	  Date date;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			date = dateFormat.parse(cursor.getString(1));

			DateFormat dateFormat2 = android.text.format.DateFormat.getDateFormat(context.getApplicationContext());
			dateFormat2.setTimeZone(TimeZone.getTimeZone("GMT"));
			t1.setText(dateFormat2.format(date));
		} catch (ParseException e) {
			t1.setText("");
			e.printStackTrace();
		}


      TextView t2 = view.findViewById(R.id.MACHINE_CELL);
      t2.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2))));
      
      TextView t3 = view.findViewById(R.id.SERIE_CELL);
      t3.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(3))));
      
      TextView t4 = view.findViewById(R.id.REPETITION_CELL);
      t4.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(4))));
      
      TextView t5 = view.findViewById(R.id.POIDS_CELL);
      t5.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(5))));      
	 
	 }
	 
	 @Override
	 public View newView(Context context, Cursor cursor, ViewGroup parent) {
		 return mInflater.inflate(R.layout.row_fonte, parent, false);
	 }
	 
	}