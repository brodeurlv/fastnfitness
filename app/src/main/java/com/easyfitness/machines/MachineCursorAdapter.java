package com.easyfitness.machines;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easyfitness.R;
import com.easyfitness.utils.ImageUtil;

public class MachineCursorAdapter extends CursorAdapter {
	 
	 private LayoutInflater mInflater;
	 private Context mContext = null;
	 
	 public MachineCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 }
	 
	 @Override
	 public void bindView(View view, Context context, Cursor cursor) {
		 		 
		  TextView t0 = (TextView) view.findViewById(R.id.LIST_MACHINE_ID); 
	      t0.setText(cursor.getString(0));
			 
		  TextView t1 = (TextView) view.findViewById(R.id.LIST_MACHINE_NAME);
		  t1.setText(cursor.getString(1));
	
	      TextView t2 = (TextView) view.findViewById(R.id.LIST_MACHINE_SHORT_DESCRIPTION);
	      t2.setText(cursor.getString(2));
	      
	      ImageView i0 = (ImageView) view.findViewById(R.id.LIST_MACHINE_PHOTO);
	      String lPath = cursor.getString(5);
	      if( lPath != null && !lPath.isEmpty() ) {
	    	  try {
				lPath = lPath.substring(0, lPath.lastIndexOf('.')) + "_TH.jpg";
				ImageUtil.setThumb(i0, lPath);
			} catch (Exception e) {
				i0.setImageResource(R.drawable.ic_machine);
				e.printStackTrace();
			}
	      } else {
	    	  i0.setImageResource(R.drawable.ic_machine);
	      }
      
	 }

	 @Override
	 public View newView(Context context, Cursor cursor, ViewGroup parent) {
		 return mInflater.inflate(R.layout.machinelist_row, parent, false);
		 
	 }	 
	 
	}