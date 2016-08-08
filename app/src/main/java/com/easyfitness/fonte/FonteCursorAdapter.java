package com.easyfitness.fonte;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.easyfitness.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class FonteCursorAdapter extends CursorAdapter {
	 
	 private LayoutInflater mInflater;
	 private int mFirstColorOdd = 0;
	 private Context mContext = null;
	 
	 public FonteCursorAdapter(Context context, Cursor c, int flags) {
	  super(context, c, flags);
	  mContext = context;
	  mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 }
	 
	 @Override
	 public void bindView(View view, Context context, Cursor cursor) {
		 
	  if(cursor.getPosition()%2==mFirstColorOdd) {		  
	   view.setBackgroundColor(context.getResources().getColor(R.color.background_odd));
	  }
	  else {
	   view.setBackgroundColor(context.getResources().getColor(R.color.background_even));
	  }
	 
	  TextView t1 = (TextView) view.findViewById(R.id.DATE_CELL);
	  Date date;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			date = dateFormat.parse(cursor.getString(1));
			
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
			//dateFormat2.setTimeZone(TimeZone.getTimeZone("GMT"));
			t1.setText(dateFormat2.format(date));
		} catch (ParseException e) {
			t1.setText("");
			e.printStackTrace();
		}


      TextView t2 = (TextView) view.findViewById(R.id.MACHINE_CELL);
      //t2.setText(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(2))));
      t2.setText(cursor.getString(2));
      
      TextView t3 = (TextView) view.findViewById(R.id.SERIE_CELL);
      t3.setText(cursor.getString(3));
      
      TextView t4 = (TextView) view.findViewById(R.id.REPETITION_CELL);
      t4.setText(cursor.getString(4));
      
      TextView t5 = (TextView) view.findViewById(R.id.POIDS_CELL);
      String poids = mContext.getString(R.string.KgUnitLabel);
      if (cursor.getString(6).equals("1")) poids= mContext.getString(R.string.LbsUnitLabel);
      
      t5.setText(cursor.getString(5)+poids);
            
	 
	 }
	 
	 @Override
	 public View newView(Context context, Cursor cursor, ViewGroup parent) {
		 return mInflater.inflate(R.layout.row_fonte, parent, false);
	 }	 
	 
	 /*
	  * @pColor : si 1 alors affiche la couleur Odd en premier. Sinon, a couleur Even.
	  */
	 public void setFirstColorOdd(int pColor) {
		 mFirstColorOdd = pColor;		 
	 }
	}