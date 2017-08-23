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

public class BodyMeasureCursorAdapter extends CursorAdapter {

	 private LayoutInflater mInflater;
	 private Context mContext = null;

	 public BodyMeasureCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 }
	 
	 @Override
	 public void bindView(View view, Context context, Cursor cursor) {
		  TextView t0 = (TextView) view.findViewById(R.id.LIST_BODYMEASURE_ID);
	      t0.setText(cursor.getString(0));
			 
		  TextView t1 = (TextView) view.findViewById(R.id.LIST_BODYPART);
		  t1.setText(cursor.getString(1));
	
	      TextView t2 = (TextView) view.findViewById(R.id.LIST_BODYMEASURE_WEIGHT);
	      t2.setText(cursor.getString(2));
	 }
	 
	 @Override
	 public View newView(Context context, Cursor cursor, ViewGroup parent) {
		 return mInflater.inflate(R.layout.bodymeasure_row, parent, false);
		 
	 }	 
	 
}