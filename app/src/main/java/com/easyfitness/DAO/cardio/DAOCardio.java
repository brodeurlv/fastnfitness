package com.easyfitness.DAO.cardio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.Profile;
import com.easyfitness.DateGraphData;
import com.easyfitness.DAO.DAOBase;
import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.DAOUtils;
import com.easyfitness.R;

public class DAOCardio extends DAOBase {

	// Contacts table name
	public static final String TABLE_NAME = "EFcardio";

	public static final String KEY = "_id";
	public static final String DATE = "date";
	public static final String EXERCICE = "exercice";
	public static final String DISTANCE = "distance";
	public static final String DURATION = "duration";
	public static final String PROFIL_KEY = "profil_id";
	public static final String NOTES = "notes";
	public static final String DISTANCE_UNIT = "distance_unit";
	public static final String VITESSE = "vitesse";	

	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
			+ DATE + " DATE, " 
			+ EXERCICE + " TEXT, " 
			+ DISTANCE + " FLOAT, " 
			+ DURATION + " INTEGER, " 
			+ PROFIL_KEY + " INTEGER, "
			+ NOTES + " TEXT, " 
			+ DISTANCE_UNIT + " TEXT, " 
			+ VITESSE + " FLOAT);";
	
	public static final String TABLE_DROP = "DROP TABLE IF EXISTS "
			+ TABLE_NAME + ";";
	
	private Profile mProfile = null;
	private Cursor mCursor = null;
	private Context mContext = null;

	public DAOCardio(Context context) {
			super(context);
			mContext = context;
	}
	
	public void setProfil (Profile pProfile)
	{
		mProfile = pProfile;
	}
	
	/**
	 * @param m
	 *            le Record a ajouter a la base
	 */
	public void addRecord(Cardio m) {
		SQLiteDatabase db = open();
		ContentValues value = new ContentValues();

		SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);

		value.put(DAOCardio.DATE, dateFormat.format(m.getDate()));
		value.put(DAOCardio.EXERCICE, m.getExercice());
		value.put(DAOCardio.DISTANCE, m.getDistance());
		value.put(DAOCardio.DURATION, m.getDuration());
		value.put(DAOCardio.PROFIL_KEY, m.getProfil().getId());

		db.insert(DAOCardio.TABLE_NAME, null, value);
		close();
	}

	
	// Getting single value
	public Cardio getRecord(long id) {
		SQLiteDatabase db = open();
		mCursor = null;
		mCursor = db.query(TABLE_NAME, new String[] { KEY, DATE, EXERCICE,
				DISTANCE, DURATION, PROFIL_KEY }, KEY + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (mCursor != null)
			mCursor.moveToFirst();

		Date date;
		try {
			date = new SimpleDateFormat(DAOUtils.DATE_FORMAT).parse(mCursor
					.getString(1));
		} catch (ParseException e) {
			e.printStackTrace();
			date = new Date();
		}
		
		//Get Profile
		DAOProfil lDAOProfil = new DAOProfil(mContext);
		Profile lProfile = lDAOProfil.getProfil(mCursor.getString(5));

		Cardio value = new Cardio(date, 
				mCursor.getString(2), 
				mCursor.getFloat(3), 
				mCursor.getLong(4),
                lProfile);

		value.setId(mCursor.getLong(0));
		// return value
		close();
		return value;
	}

	// Getting All Records
	private List<Cardio> getRecordsList(String pRequest) {
		List<Cardio> valueList = new ArrayList<Cardio>();
		SQLiteDatabase db = this.getReadableDatabase();
		// Select All Query
		String selectQuery = pRequest;

		mCursor = null;
		mCursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (mCursor.moveToFirst()) {
			do {
				//Get Date
				Date date;
				try {
					date = new SimpleDateFormat(DAOUtils.DATE_FORMAT).parse(mCursor.getString(1));
				} catch (ParseException e) {
					e.printStackTrace();
					date = new Date();
				}
				
				//Get Profile
				DAOProfil lDAOProfil = new DAOProfil(mContext);
				Profile lProfile = lDAOProfil.getProfil(mCursor.getString(5));

				Cardio value = new Cardio(date, 
						mCursor.getString(2), 
						mCursor.getFloat(3), 
						mCursor.getLong(4),
                        lProfile);
				
				value.setId(Long.parseLong(mCursor.getString(0)));

				// Adding value to list
				valueList.add(value);
			} while (mCursor.moveToNext());
		}
		// return value list
		return valueList;
	}

	public Cursor GetCursor() {
		return mCursor;
	}

	// Getting All Records
	public List<Cardio> getAllRecords() {
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY "
				+ KEY + " DESC";

		// return value list
		return getRecordsList(selectQuery);
	}
	
	// Getting All Records
	public List<Cardio> getAllRecordsByProfil(Profile pProfile) {
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_NAME + 
				" WHERE " + PROFIL_KEY + "=" + pProfile.getId() +
				" ORDER BY " + KEY + " DESC";

		// return value list
		return getRecordsList(selectQuery);
	}

	// Getting Top 10 Records
	public List<Cardio> getTop10Records(Profile pProfile) {
		// Select All Query
		String selectQuery = "SELECT TOP 10 * FROM " + TABLE_NAME+ 
				" WHERE " + PROFIL_KEY + "=" + pProfile.getId()
				+ " ORDER BY " + KEY + " DESC";

		// return value list
		return getRecordsList(selectQuery);
	}

	// Getting Filtered records
	public List<Cardio> getFilteredRecords(Profile pProfile, String pMachine, String pDate) {

		boolean lfilterMachine = true;
		boolean lfilterDate = true;
		String selectQuery = null;

		if (pMachine == null || pMachine.isEmpty() || pMachine.equals(mContext.getResources().getText(R.string.all).toString()))
		{
			lfilterMachine = false;
		}

		if (pDate == null || pDate.isEmpty() || pDate.equals(mContext.getResources().getText(R.string.all).toString()))
		{
			lfilterDate = false;
		}

		if (lfilterMachine && lfilterDate) {
			selectQuery = "SELECT * FROM " + TABLE_NAME 
					+ " WHERE " + EXERCICE + "=\"" + pMachine 
					+ "\" AND " + DATE + "=\"" + pDate 
					+ "\" AND " + PROFIL_KEY + "=" + pProfile.getId()
					+ " ORDER BY " + KEY + " DESC";
		} else if (!lfilterMachine && lfilterDate) {
			selectQuery = "SELECT * FROM " + TABLE_NAME 
					+ " WHERE " + DATE + "=\"" + pDate 
					+ "\" AND " + PROFIL_KEY + "=" + pProfile.getId()
					+ " ORDER BY " + KEY + " DESC";
		} else if (lfilterMachine && !lfilterDate) {
			selectQuery = "SELECT * FROM " + TABLE_NAME 
					+ " WHERE " + EXERCICE	+ "=\"" + pMachine 
					+ "\" AND " + PROFIL_KEY + "=" + pProfile.getId()
					+ " ORDER BY " + KEY + " DESC";
		} else if (!lfilterMachine && !lfilterDate) {
			selectQuery = "SELECT * FROM " + TABLE_NAME 
					+ " WHERE " + PROFIL_KEY + "=" + pProfile.getId()
					+ " ORDER BY " + KEY
					+ " DESC";
		}

		// return value list
		return getRecordsList(selectQuery);
	}

	// Getting Function records
	public List<DateGraphData> getFunctionRecords(Profile pProfile, String pMachine,
                                                  String pFunction) {

		boolean lfilterMachine = true;
		boolean lfilterFunction = true;
		String selectQuery = null;

		if (pMachine == null || pMachine.isEmpty() || pMachine.equals(mContext.getResources().getText(R.string.all).toString()))
		{
			lfilterMachine = false;
		}

		if (pFunction == null || pFunction.isEmpty() || pFunction.equals(mContext.getResources().getText(R.string.all).toString()))
		{
			lfilterFunction = false;
		}

		if (pFunction.equals("SUM DISTANCE")) {
			selectQuery = "SELECT SUM(" + DISTANCE + "), " + DATE + " FROM " + TABLE_NAME 
					+ " WHERE "	+ EXERCICE + "=\"" + pMachine + "\"" 
					+ " AND " + PROFIL_KEY + "=" + pProfile.getId()
					+ " GROUP BY " + DATE
					+ " ORDER BY date(" + DATE + ") ASC";
		} else if (pFunction.equals("SUM DURATION")) {
			selectQuery = "SELECT MAX(" + DURATION + ") , " + DATE + " FROM "
					+ TABLE_NAME 
					+ " WHERE " + EXERCICE + "=\"" + pMachine + "\""
					+ " AND " + PROFIL_KEY + "=" + pProfile.getId()
					+ " GROUP BY " + DATE 
					+ " ORDER BY date(" + DATE	+ ") ASC";
		} else if (pFunction.equals("MAX DISTANCE")) {
			selectQuery = "SELECT SUM(" + DISTANCE + ") , " + DATE + " FROM "
					+ TABLE_NAME 
					+ " WHERE " + EXERCICE + "=\"" + pMachine + "\""
					+ " AND " + PROFIL_KEY + "=" + pProfile.getId()
					+ " GROUP BY " + DATE 
					+ " ORDER BY date(" + DATE	+ ") ASC";
		} else if (pFunction.equals("MAX DURATION")) {
			selectQuery = "SELECT MAX(" + DURATION + ") , " + DATE + " FROM "
					+ TABLE_NAME 
					+ " WHERE " + EXERCICE + "=\"" + pMachine + "\""
					+ " AND " + PROFIL_KEY + "=" + pProfile.getId()
					+ " GROUP BY " + DATE 
					+ " ORDER BY date(" + DATE	+ ") ASC";
		}
		// case "MEAN" : selectQuery = "SELECT SUM("+ SERIE + "*" + REPETITION +
		// "*" + POIDS +") FROM " + TABLE_NAME + " WHERE " + MACHINE + "=\"" +
		// pMachine + "\" AND " + DATE + "=\"" + pDate + "\" ORDER BY " + KEY +
		// " DESC";
		// break;

		// Formation de tableau de valeur
		List<DateGraphData> valueList = new ArrayList<DateGraphData>();
		SQLiteDatabase db = this.getReadableDatabase();
		mCursor = null;
		mCursor = db.rawQuery(selectQuery, null);

		double i = 0;

		// looping through all rows and adding to list
		if (mCursor.moveToFirst()) {
			do {
				Date date;
				try {
					date = new SimpleDateFormat(DAOUtils.DATE_FORMAT)
							.parse(mCursor.getString(1));
				} catch (ParseException e) {
					e.printStackTrace();
					date = new Date();
				}

				DateGraphData value = new DateGraphData(date.getTime(),
						Double.parseDouble(mCursor.getString(0)));

				// Adding value to list
				valueList.add(value);
			} while (mCursor.moveToNext());
		}

		// return value list
		return valueList;
	}

	// Getting All Machines
	public String[] getAllMachines(Profile pProfile) {
		SQLiteDatabase db = this.getReadableDatabase();
		mCursor = null;

		// Select All Machines
		String selectQuery = "SELECT DISTINCT  " + EXERCICE + " FROM "
				+ TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY " + EXERCICE + " ASC";
		mCursor = db.rawQuery(selectQuery, null);

		int size = mCursor.getCount();

		String[] valueList = new String[size];

		// looping through all rows and adding to list
		if (mCursor.moveToFirst()) {
			int i = 0;
			do {
				String value = new String(mCursor.getString(0));
				valueList[i] = value;
				i++;
			} while (mCursor.moveToNext());
		}
		close();
		// return value list
		return valueList;
	}

	// Getting All Dates
	public Date[] getAllDates(Profile pProfile) {
		
		SQLiteDatabase db = this.getReadableDatabase();
		if (mCursor!=null) mCursor.close();
		mCursor = null;

		// Select All Machines
		String selectQuery = "SELECT DISTINCT " + DATE + " FROM " + TABLE_NAME
				+ " WHERE " + PROFIL_KEY + "=" + pProfile.getId()
				+ " ORDER BY " + DATE + " ASC";
		mCursor = db.rawQuery(selectQuery, null);
		int size = mCursor.getCount();

		Date[] valueList = new Date[size];

		// looping through all rows and adding to list
		if (mCursor.moveToFirst()) {
			do {
				int i = 0;
				Date date;
				try {
					date = new SimpleDateFormat(DAOUtils.DATE_FORMAT)
							.parse(mCursor.getString(1));
				} catch (ParseException e) {
					e.printStackTrace();
					date = new Date();
				}
				valueList[i] = date;
				i++;
			} while (mCursor.moveToNext());
		}
		
		close();

		// return value list
		return valueList;
	}

	// Getting All Dates
	public String[] getAllDatesAsString(Profile pProfile) {

		SQLiteDatabase db = this.getReadableDatabase();
		if (mCursor!=null) mCursor.close();
		mCursor = null;

		// Select All Machines
		String selectQuery = "SELECT DISTINCT " + DATE + " FROM " + TABLE_NAME
				+ " WHERE " + PROFIL_KEY + "=" + pProfile.getId()
				+ " ORDER BY " + DATE + " ASC";
		mCursor = db.rawQuery(selectQuery, null);

		int size = mCursor.getCount();

		String[] valueList = new String[size];

		// looping through all rows and adding to list
		if (mCursor.moveToFirst()) {
			int i = 0;
			do {
				String value = new String(mCursor.getString(0));
				valueList[i] = value;
				i++;
			} while (mCursor.moveToNext());
		}
		
		close();

		// return value list
		return valueList;
	}

	// Get all record for one Machine
	public List<Cardio> getAllRecordByMachines(Profile pProfile, String pMachines) {
		// Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_NAME 
				+ " WHERE " + EXERCICE + "=\"" + pMachines + "\""
				+ " AND " + PROFIL_KEY + "=" + pProfile.getId()
				+ " ORDER BY " + KEY + " DESC";

		// return value list
		return getRecordsList(selectQuery);
	}

	// Get all record for one Date
	public List<Cardio> getAllRecordByDate(Profile pProfile, Date pDate) {
		// return value list
		return getAllRecordByDate(pProfile, pDate.toString());
	}

	// Get all record for one Date
	public List<Cardio> getAllRecordByDate(Profile pProfile, String pDate) {
		// Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_NAME 
				+ " WHERE " + DATE + "=\"" + pDate + "\"" 
				+ " AND " + PROFIL_KEY + "=" + pProfile.getId()
				+ " ORDER BY " + KEY + " DESC";

		// return value list
		return getRecordsList(selectQuery);
	}

	// Getting last record
	public Cardio getLastRecord(Profile pProfile) {

		SQLiteDatabase db = this.getReadableDatabase();
		mCursor = null;
		Cardio lReturn = null;

		// Select All Machines
		String selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME 
				+ " WHERE " + PROFIL_KEY + "=" + pProfile.getId();
		mCursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		mCursor.moveToFirst();
		try {
			long value = Long.parseLong(mCursor.getString(0));
			lReturn = this.getRecord(value);
		} catch (NumberFormatException e) {
			//Date date = new Date();
			lReturn = null; // Return une valeur
		}
		
		close();

		// return value list
		return lReturn;
	}

	// Updating single value
	public int updateRecord(Profile pProfile, Cardio m) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues value = new ContentValues();
		value.put(DAOCardio.DATE, m.getDate().toString());
		value.put(DAOCardio.EXERCICE, m.getExercice());
		value.put(DAOCardio.DISTANCE, m.getDistance());
		value.put(DAOCardio.DURATION, m.getDuration());
		value.put(DAOCardio.PROFIL_KEY, pProfile.getId());

		// updating row
		return db.update(TABLE_NAME, value, KEY + " = ?",
				new String[] { String.valueOf(m.getId()) });
	}

	// Deleting single Record
	public void deleteRecord(Cardio m) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, KEY + " = ?",
				new String[] { String.valueOf(m.getId()) });
		db.close();
	}

	// Deleting single Record
	public void deleteRecord(long id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, KEY + " = ?", new String[] { String.valueOf(id) });
		db.close();
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
		// DBORecord(long id, Date pDate, String pMachine, int pSerie, int
		// pRepetition, int pPoids)
		Date date = new Date();
		int poids = 10;

		for (int i = 1; i <= 5; i++) {
			String machine = "Tapis";
			date.setDate(date.getDay() + i * 10);
			addRecord(new Cardio(date, machine, (float)i * 20, 120000*i, mProfile));
		}

		date = new Date();
		poids = 12;

		for (int i = 1; i <= 5; i++) {
			String machine = "Rameur";
			date.setDate(date.getDay() + i * 10);
			addRecord(new Cardio(date, machine, 0, 120000*i*3, mProfile));
		}
	}

}
