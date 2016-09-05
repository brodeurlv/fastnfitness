package com.easyfitness.DAO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DAOWeight extends DAOBase {
		
    // Contacts table name
	public static final String TABLE_NAME = "EFweight";
	
	  public static final String KEY = "_id";
	  public static final String POIDS = "poids";
	  public static final String DATE = "date";
	  public static final String PROFIL_KEY = "profil_id";
	  
	  public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " DATE, " + POIDS + " REAL , " + PROFIL_KEY + " INTEGER);";

	  public static final String TABLE_DROP =  "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
	  private Profil mProfil = null;	  
	  private Cursor mCursor = null;
	  
	public DAOWeight(Context context) {
		super(context);
	}
	
	public void setProfil (Profil pProfil)
	{
		mProfil = pProfil;
	}

	  /**
	   * @param pDate date of the weight measure
	   * @param pWeight weight
	   * @param pProfil profil associated with the measure
	   */
	  public void addWeight(Date pDate, float pWeight, Profil pProfil) {
		  SQLiteDatabase db = this.getWritableDatabase();
		  //TODO : verifier qu'on ne met pas deux poids sur la meme journee.
		  
		  ContentValues value = new ContentValues();
		  
		  SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
		  
		  value.put(DAOWeight.DATE, dateFormat.format(pDate));
		  value.put(DAOWeight.POIDS, pWeight);
		  value.put(DAOWeight.PROFIL_KEY, pProfil.getId());
		  
		  db.insert(DAOWeight.TABLE_NAME, null, value);
		  db.close(); // Closing database connection
	  }

	// Getting single value
	  private Weight getMeasure(long id) {
	        SQLiteDatabase db = this.getReadableDatabase();
	        
	        mCursor = null;
	        mCursor = db.query(TABLE_NAME, 
	        		new String[] { KEY, DATE, POIDS, PROFIL_KEY}, 
	        		KEY + "=?",
	                new String[] { String.valueOf(id) },
	                null, null, null, null);
	        if (mCursor != null)
	        	mCursor.moveToFirst();
	        
	        Date date;
			try {
				date = new SimpleDateFormat(DAOUtils.DATE_FORMAT).parse(mCursor.getString(1));
			} catch (ParseException e) {
				e.printStackTrace();
				date = new Date();
			}
	 
	        Weight value = new Weight(mCursor.getLong(0),
	        		date,
	        		mCursor.getFloat(2),
	        		mCursor.getLong(3)	                
	                );
	        
	        db.close();
	        
	        // return value
	        return value;
	    }
	    
	 // Getting All Measures
	    private List<Weight> getMeasuresList(String pRequest) {
	        List<Weight> valueList = new ArrayList<Weight>();
	        // Select All Query
	        String selectQuery = pRequest;
	 
	        SQLiteDatabase db = this.getReadableDatabase();
	        mCursor = null;
	        mCursor = db.rawQuery(selectQuery, null);
	 
	        // looping through all rows and adding to list
	        if (mCursor.moveToFirst()) {
	            do {
	    	        Date date;
	    			try {
	    				date = new SimpleDateFormat(DAOUtils.DATE_FORMAT).parse(mCursor.getString(1));
	    			} catch (ParseException e) {
	    				e.printStackTrace();
	    				date = new Date();
	    			}
	    			
	    	        Weight value = new Weight(mCursor.getLong(0),
	    	        		date,
	    	        		mCursor.getFloat(2),
	    	        		mCursor.getLong(3)	                
	    	                );
	    	        
	                // Adding value to list
	                valueList.add(value);
	            } while (mCursor.moveToNext());
	        }
	 
	        // return value list
	        return valueList;
	    }
	    
	    public Cursor GetCursor()
	    {
	    	return mCursor;
	    }
	     
	    // Getting All Measures
	    public List<Weight> getWeightList(Profil pProfil) {
	        // Select All Query
	        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfil.getId() + " GROUP BY " + DATE + " ORDER BY date(" + DATE + ") DESC";
	 
	        // return value list
	        return getMeasuresList(selectQuery);
	    }
	    
	    // Updating single value
	    public int updateMeasure(Weight m) {
	        SQLiteDatabase db = this.getWritableDatabase();
	 
	        ContentValues value = new ContentValues();
			  value.put(DAOWeight.DATE, m.getDate().toString());
			  value.put(DAOWeight.POIDS, m.getWeight());
			  value.put(DAOWeight.PROFIL_KEY, m.getProfil());
	 
	        // updating row
	        return db.update(TABLE_NAME, value, KEY + " = ?",
	                new String[] { String.valueOf(m.getId()) });
	    }
	 
	    // Deleting single Measure
	    public void deleteMeasure(Weight m) {
	    	deleteMeasure(m.getId());
	    }
	    
	    // Deleting single Measure
	    public void deleteMeasure(long id) {
	        SQLiteDatabase db = this.getWritableDatabase();
	        db.delete(TABLE_NAME, KEY + " = ?",
	                new String[] { String.valueOf(id) });
	    }
	 
	    // Getting Profils Count
	    public int getCount() {
	        String countQuery = "SELECT  * FROM " + TABLE_NAME;
	        open();
	        SQLiteDatabase db = this.getReadableDatabase();
	        Cursor cursor = db.rawQuery(countQuery, null);
	        	
	        int value = cursor.getCount();
	        cursor.close();
	        close();
	        
	        // return count
	        return value;
	    }
	    
	    public void populate() {
			Date date = new Date();
			int poids = 10;
			
			for (int i = 1; i<=5 ;i++ )
			{
				date.setTime(date.getTime()+i*1000*60*60*24*2);
				addWeight(date, Float.valueOf(i), mProfil);
			}
		}
	}


