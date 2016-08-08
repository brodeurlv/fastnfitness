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
				setThumb(i0, lPath);
			} catch (Exception e) {
				i0.setImageResource(R.drawable.ic_machine);
				e.printStackTrace();
			}
	      } else {
	    	  i0.setImageResource(R.drawable.ic_machine);
	      }
      
	 }
	 
	 private void setThumb(ImageView mImageView, String pPath) {
		    try {
		    	if (pPath == null) return;
		    	File f = new File(pPath);
		    	if(!f.exists() || f.isDirectory()) return; // TODO Si le fichier n'existe pas, pourrait verifier si le fichier source existe et creer la miniature.
		    	
				// Get the dimensions of the View
				float targetW = 128;//mImageView.getWidth();
				//float targetH = mImageView.getHeight();

				// Get the dimensions of the bitmap
				BitmapFactory.Options bmOptions = new BitmapFactory.Options();
				bmOptions.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(pPath, bmOptions);
				float photoW = bmOptions.outWidth;
				float photoH = bmOptions.outHeight;

				// Determine how much to scale down the image
				int scaleFactor = (int)(photoW/targetW); //Math.min(photoW/targetW, photoH/targetH);

				// Decode the image file into a Bitmap sized to fill the View
				bmOptions.inJustDecodeBounds = false;
				bmOptions.inSampleSize = scaleFactor;
				bmOptions.inPurgeable = true;

				Bitmap bitmap = BitmapFactory.decodeFile(pPath, bmOptions);
				mImageView.setImageBitmap(bitmap);
				mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	 
	 @Override
	 public View newView(Context context, Cursor cursor, ViewGroup parent) {
		 return mInflater.inflate(R.layout.machinelist_row, parent, false);
		 
	 }	 
	 
	}