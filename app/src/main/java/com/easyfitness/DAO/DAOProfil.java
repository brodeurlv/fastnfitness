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

public class DAOProfil extends DAOBase {
	
    // Contacts table name
	public static final String TABLE_NAME = "EFprofil";
	
	  public static final String KEY = "_id";
	  public static final String NAME = "name";
	  public static final String CREATIONDATE = "creationdate";
	  
	  public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CREATIONDATE + " DATE, " + NAME + " TEXT);";

	  public static final String TABLE_DROP =  "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
	  
	  private Cursor mCursor = null;
	  
	  //DAOFonte mDAOFonte = null;
	  

	public DAOProfil(Context context) {
		super(context);
	}
	
	  /**
	   * @param m DBOProfil Profil a ajouter a la base
	   */
	  public void addProfil(Profil m) {
		  // Check if profil already exists		  
		  Profil check = getProfil(m.getName());
		  if (check != null) return;
		  
		  SQLiteDatabase db = this.getWritableDatabase();
		  
		  ContentValues value = new ContentValues();
		  
		  SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
		  
		  value.put(DAOProfil.CREATIONDATE, dateFormat.format(m.getDate()));
		  value.put(DAOProfil.NAME, m.getName());
		  
		  db.insert(DAOProfil.TABLE_NAME, null, value);
		  
		  close();
	  }
	  
	  /**
	   * @param pName String Nom du profil a ajouter a la base
	   */
	  public void addProfil(String pName) {
		  // Check if profil already exists		  
		  Profil check = getProfil(pName);
		  if (check != null) return;
		  
		  SQLiteDatabase db = this.getWritableDatabase();
		  
		  ContentValues value = new ContentValues();
		  
		  SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
		  
		  value.put(DAOProfil.CREATIONDATE, dateFormat.format(new Date()));
		  value.put(DAOProfil.NAME, pName);
		  
		  db.insert(DAOProfil.TABLE_NAME, null, value);
		  
		  close();		  
	  }	  

	  /**
	   * @param id long id of the Profil
	   */
	  public Profil getProfil(long id) {
	        SQLiteDatabase db = this.getReadableDatabase();
			if (mCursor!=null) mCursor.close();
	        mCursor = null;
	        mCursor = db.query(TABLE_NAME, 
	        		new String[] { KEY, CREATIONDATE, NAME }, 
	        		KEY + "=?",
	                new String[] { String.valueOf(id) },
	                null, null, null, null);
	        if (mCursor != null && mCursor.getCount()>0) {
	        	mCursor.moveToFirst();
	        
	        Date date;
			try {
				date = new SimpleDateFormat(DAOUtils.DATE_FORMAT).parse(mCursor.getString(1));
			} catch (ParseException e) {
				e.printStackTrace();
				date = new Date();
			}
	 
	        Profil value = new Profil(mCursor.getLong(0),
	        		date,
	        		mCursor.getString(2)
	                );
	        mCursor.close();
			close();
	        
	        // return value
	        return value;
	        }
	        else
	        {
		        mCursor.close();
				close();
	        	return null;
	        }
	       
	    }
	  
	  /**
	   * @param name String name of the Profil
	   */
	  public Profil getProfil(String name) {
	        SQLiteDatabase db = this.getReadableDatabase();
			if (mCursor!=null) mCursor.close();
	        mCursor = null;
	        mCursor = db.query(TABLE_NAME, 
	        		new String[] { KEY, CREATIONDATE, NAME }, 
	        		NAME + "=?",
	        		new String[] { name },
	                null, null, null, null);
	        if (mCursor != null && mCursor.getCount()>0) {
	        	mCursor.moveToFirst();
	        
	        Date date;
			try {
				date = new SimpleDateFormat(DAOUtils.DATE_FORMAT).parse(mCursor.getString(1));
			} catch (ParseException e) {
				e.printStackTrace();
				date = new Date();
			}
	 
	        Profil value = new Profil(mCursor.getLong(0),
	        		date,
	        		mCursor.getString(2)
	                );
	        
	        mCursor.close();
			close();
	        
	        // return value
	        return value;
	        }
	        else
	        {
				close();
	        	return null;
	        }

	    }
	    
	  	// Getting All Profils
	    public List<Profil> getProfilsList(String pRequest) {
	        List<Profil> valueList = new ArrayList<Profil>();
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
	    			
	    	        Profil value = new Profil(mCursor.getLong(0),
	    	        		date,
	    	        		mCursor.getString(2)
	    	                );
	    	        
	                // Adding value to list
	                valueList.add(value);
	            } while (mCursor.moveToNext());
	        }

			close();
	        // return value list
	        return valueList;
	    }
	    
	    public Cursor GetCursor()
	    {
	    	return mCursor;
	    }
	     
	    // Getting All Profils
	    public List<Profil> getAllProfils() {
	        // Select All Query
	        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + KEY + " DESC";
	 
	        // return value list
	        return getProfilsList(selectQuery);
	    }
	    
	    // Getting Top 10 Profils
	    public List<Profil> getTop10Profils() {
	        // Select All Query
	        String selectQuery = "SELECT TOP 10 * FROM " + TABLE_NAME + " ORDER BY " + KEY + " DESC";
	 
	        // return value list
	        return getProfilsList(selectQuery);
	    }
	    
	    // Getting All Machines
	    public String[] getAllProfil() {
	        
	        SQLiteDatabase db = this.getReadableDatabase();
	        mCursor = null;
	        
	        // Select All Machines
	        String selectQuery = "SELECT DISTINCT  " + NAME + " FROM " + TABLE_NAME  + " ORDER BY " + NAME + " ASC";
	 	        mCursor = db.rawQuery(selectQuery, null);
	 	        
	 	    int size = mCursor.getCount();
	 	        
	 	    String[] valueList = new String[size]; 
	 
	        // looping through all rows and adding to list
	        if (mCursor.moveToFirst()) {
	        	int i = 0;
	            do {
	    	        String value = new String(mCursor.getString(0));
	    	        valueList[i]=value;
	    	        i++;
	            } while (mCursor.moveToNext());
	        }

			close();
	 
	        // return value list
	        return valueList;
	    }
 
		 // Getting last record
	    public Profil getLastProfil() {
	
	        SQLiteDatabase db = this.getReadableDatabase();
	        mCursor = null;
	        
	        // Select All Machines
	        String selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME;
	 	    mCursor = db.rawQuery(selectQuery, null);
	 	    
	        // looping through all rows and adding to list
	        mCursor.moveToFirst();
	        long value = Long.parseLong(mCursor.getString(0));
	        
	        Profil prof = this.getProfil(value);
	        mCursor.close();
	        close();
	        
	        // return value list
	        return prof;
	    }
	 
	    // Updating single value
	    public int updateProfil(Profil m) {
	        SQLiteDatabase db = this.getWritableDatabase();
	 
	        ContentValues value = new ContentValues();
			  value.put(DAOProfil.CREATIONDATE, m.getDate().toString());
			  value.put(DAOProfil.NAME, m.getName());
	 
	        // updating row
	        return db.update(TABLE_NAME, value, KEY + " = ?",
	                new String[] { String.valueOf(m.getId()) });
	    }
	 
	    // Deleting single Profil
	    public void deleteProfil(Profil m) {
	    	deleteProfil(m.getId());
	    }
	    
	    // Deleting single Profil
	    public void deleteProfil(long id) {
	    	open();
	        
	        DAOWeight mWeightDb;
	        mWeightDb = new DAOWeight(null); // null car a ce moment le DatabaseHelper est cree depuis bien longtemps. 
			List<Weight> valueList = mWeightDb.getWeightList(getProfil(id));
			for (int i = 0; i<valueList.size();i++) {
				mWeightDb.deleteMeasure(valueList.get(i).getId());
			}
			
	        SQLiteDatabase db = this.getWritableDatabase();
	        db.delete(TABLE_NAME, KEY + " = ?",
	                new String[] { String.valueOf(id) });
	        
	        close();
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
	    
	    
	    /* DEBUG ONLY */
	    public void populate() {
			Date date = new Date();
			Profil m = new Profil(0, date, "Champignon");
			this.addProfil(m);
			m = new Profil(0, date, "Musclor");
			this.addProfil(m);
	    }
	}


