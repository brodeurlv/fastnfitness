package com.easyfitness.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DAORecord extends DAOBase {

    // Contacts table name
    public static final String TABLE_NAME = "EFfontes";

    public static final String KEY = "_id";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String EXERCISE = "machine";
    public static final String PROFIL_KEY = "profil_id";
    public static final String MACHINE_KEY = "machine_id";
    public static final String NOTES = "notes";
    public static final String TYPE = "type";

    // Specific to BodyBuilding
    public static final String SERIE = "serie";
    public static final String REPETITION = "repetition";
    public static final String WEIGHT = "poids";
    public static final String UNIT = "unit"; // 0:kg 1:lbs

    // Specific to Cardio
    public static final String DISTANCE = "distance";
    public static final String DURATION = "duration";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
            + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE
            + " DATE, " + EXERCISE + " TEXT, " + SERIE + " INTEGER, "
            + REPETITION + " INTEGER, " + WEIGHT + " REAL, " + PROFIL_KEY
            + " INTEGER, " + UNIT + " INTEGER, " + NOTES + " TEXT, " + MACHINE_KEY
            + " INTEGER," + TIME + " TEXT," + DISTANCE + " REAL, " + DURATION + " TEXT, " + TYPE + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS "
            + TABLE_NAME + ";";

    protected Profile mProfile = null;
    protected Cursor mCursor = null;
    protected Context mContext;

    public DAORecord(Context context) {
        super(context);
        mContext = context;
    }

    public void setProfile(Profile pProfile) {
        mProfile = pProfile;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    // Getting Count
    public int getCount() {
        String countQuery = "SELECT " + KEY + " FROM " + TABLE_NAME;
        open();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int value = cursor.getCount();
        cursor.close();
        close();

        // return count
        return value;
    }

    /**
     * @param pDate    Date
     * @param pMachine Machine name
     * @return id of the added record, -1 if error
     */
    public long addRecord(Date pDate, String pMachine, int pType, int pSerie, int pRepetition, float pPoids, Profile pProfile, int pUnit, String pNote, String pTime, float pDistance, long pDuration) {
        ContentValues value = new ContentValues();
        long new_id = -1;
        long machine_key = -1;

        //Test is Machine exists. If not create it.
        DAOMachine lDAOMachine = new DAOMachine(mContext);
        if (!lDAOMachine.machineExists(pMachine)) {
            machine_key = lDAOMachine.addMachine(pMachine, "", pType, "");
        } else {
            machine_key = lDAOMachine.getMachine(pMachine).getId();
        }

        value.put(DAORecord.DATE, DateConverter.dateToDBDateStr(pDate));
        value.put(DAORecord.EXERCISE, pMachine);
        value.put(DAORecord.SERIE, pSerie);
        value.put(DAORecord.REPETITION, pRepetition);
        value.put(DAORecord.WEIGHT, pPoids);
        value.put(DAORecord.PROFIL_KEY, pProfile.getId());
        value.put(DAORecord.UNIT, pUnit);
        value.put(DAORecord.NOTES, pNote);
        value.put(DAORecord.MACHINE_KEY, machine_key);
        value.put(DAORecord.TIME, pTime);
        value.put(DAORecord.DISTANCE, pDistance);
        value.put(DAORecord.DURATION, pDuration);
        value.put(DAORecord.TYPE, pType);

        SQLiteDatabase db = open();
        new_id = db.insert(DAORecord.TABLE_NAME, null, value);
        close();

        return new_id;
    }

    // Deleting single Record
    public void deleteRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Getting single value
    public Record getRecord(long id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY + "=" + id;

        mCursor = getRecordsListCursor(selectQuery);
        if (mCursor.moveToFirst()) {
            //Get Date
            Date date;
            try {
                SimpleDateFormat dateformat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
                dateformat.setTimeZone(TimeZone.getTimeZone("GMT"));
                date = dateformat.parse(mCursor.getString(mCursor.getColumnIndex(DAOFonte.DATE)));
            } catch (ParseException e) {
                e.printStackTrace();
                date = new Date();
            }

            //Get Profile
            DAOProfil lDAOProfil = new DAOProfil(mContext);
            Profile lProfile = lDAOProfil.getProfil(mCursor.getLong(mCursor.getColumnIndex(DAOFonte.PROFIL_KEY)));

            long machine_key = -1;

            //Test is Machine exists. If not create it.
            DAOMachine lDAOMachine = new DAOMachine(mContext);
            if (mCursor.getString(mCursor.getColumnIndex(DAOFonte.MACHINE_KEY)) == null) {
                machine_key = lDAOMachine.addMachine(mCursor.getString(mCursor.getColumnIndex(DAOFonte.EXERCISE)), "", DAOMachine.TYPE_FONTE, "");
            } else {
                machine_key = mCursor.getLong(mCursor.getColumnIndex(DAOFonte.MACHINE_KEY));
            }

            Record value = new Record(date,
                    mCursor.getString(mCursor.getColumnIndex(DAORecord.EXERCISE)),
                    lProfile,
                    machine_key,
                    mCursor.getString(mCursor.getColumnIndex(DAORecord.TIME)),
                    mCursor.getInt(mCursor.getColumnIndex(DAORecord.TYPE)));

            value.setId(mCursor.getLong(mCursor.getColumnIndex(DAORecord.KEY)));
            return value;
        } else {
            return null;
        }
    }

    // Get all record for one Machine
    public Cursor getAllRecordByMachines(Profile pProfile, String pMachines) {
        return getAllRecordByMachines(pProfile, pMachines, -1);
    }

    public Cursor getAllRecordByMachines(Profile pProfile, String pMachines, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachines + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop;

        // return value list
        return getRecordsListCursor(selectQuery);
    }

    // Getting All Records
    public Cursor getAllRecordsByProfile(Profile pProfile) {
        return getAllRecordsByProfile(pProfile, -1);
    }

    /**
     * @param pProfile   record associated to one profile
     * @param pNbRecords max number of records requested
     * @return pNbRecords number of records for a specified pProfile
     */
    public Cursor getAllRecordsByProfile(Profile pProfile, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + PROFIL_KEY + "=" + pProfile.getId() +
                " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop;

        // Return value list
        return getRecordsListCursor(selectQuery);
    }

    // Getting All Records
    private Cursor getRecordsListCursor(String pRequest) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Select All Query
        String selectQuery = pRequest;

        // return value list
        return db.rawQuery(selectQuery, null);
    }

    // Getting All Machines
    public List<String> getAllMachinesStrList(Profile pProfile) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        String selectQuery = "";
        if (pProfile == null) {
            selectQuery = "SELECT DISTINCT " + MACHINE_KEY + " FROM "
                    + TABLE_NAME + " ORDER BY " + EXERCISE + " ASC";
        } else {
            selectQuery = "SELECT DISTINCT " + EXERCISE + " FROM "
                    + TABLE_NAME + "  WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY " + EXERCISE + " ASC";
        }
        mCursor = db.rawQuery(selectQuery, null);

        int size = mCursor.getCount();

        List<String> valueList = new ArrayList<String>(size);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                valueList.add(mCursor.getString(0));
                i++;
            } while (mCursor.moveToNext());
        }
        close();
        // return value list
        return valueList;
    }

    // Getting All Machines
    public String[] getAllMachines(Profile pProfile) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;

        // Select All Machines
        String selectQuery = "SELECT DISTINCT " + EXERCISE + " FROM "
                + TABLE_NAME + "  WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY " + EXERCISE + " ASC";
        mCursor = db.rawQuery(selectQuery, null);

        int size = mCursor.getCount();

        String[] valueList = new String[size];

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                String value = mCursor.getString(0);
                valueList[i] = value;
                i++;
            } while (mCursor.moveToNext());
        }
        close();
        // return value list
        return valueList;
    }

    // Getting All Machines
    public String[] getAllMachines() {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;

        // Select All Machines
        String selectQuery = "SELECT DISTINCT  " + EXERCISE + " FROM "
                + TABLE_NAME + " ORDER BY " + EXERCISE + " ASC";
        mCursor = db.rawQuery(selectQuery, null);

        int size = mCursor.getCount();

        String[] valueList = new String[size];

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                String value = mCursor.getString(0);
                valueList[i] = value;
                i++;
            } while (mCursor.moveToNext());
        }
        close();
        // return value list
        return valueList;
    }

    // Getting All Dates
    public List<String> getAllDatesList(Profile pProfile) {

        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;

        // Select All Machines
        String selectQuery = "SELECT DISTINCT " + DATE + " FROM " + TABLE_NAME;
        if (pProfile != null)
            selectQuery += " WHERE " + PROFIL_KEY + "=" + pProfile.getId(); // pProfile should never be null but depending on how the activity is resuming it happen. to be fixed
        selectQuery += " ORDER BY " + DATE + " DESC";

        mCursor = db.rawQuery(selectQuery, null);
        int size = mCursor.getCount();

        List<String> valueList = new ArrayList<String>(size);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                int i = 0;
                //String date;
                //date = mCursor.getString(0);
                // Change Date format
                //date = date.substring(0, 3) + "-" + date.substring(5, 6) + "-"

                Date date;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    date = dateFormat.parse(mCursor.getString(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = new Date();
                }

                DateFormat dateFormat3 = android.text.format.DateFormat.getDateFormat(mContext.getApplicationContext());
                dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"));
                valueList.add(dateFormat3.format(date));
                i++;
            } while (mCursor.moveToNext());
        }

        close();

        // return value list
        return valueList;
    }

    // Getting Filtered records
    public Cursor getFilteredRecords(Profile pProfile, String pMachine, String pDate) {

        boolean lfilterMachine = true;
        boolean lfilterDate = true;
        String selectQuery = null;

        if (pMachine == null || pMachine.isEmpty() || pMachine.equals(mContext.getResources().getText(R.string.all).toString())) {
            lfilterMachine = false;
        }

        if (pDate == null || pDate.isEmpty() || pDate.equals(mContext.getResources().getText(R.string.all).toString())) {
            lfilterDate = false;
        }

        if (lfilterMachine && lfilterDate) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine
                    + "\" AND " + DATE + "=\"" + pDate
                    + "\" AND " + PROFIL_KEY + "=" + pProfile.getId()
                    + " ORDER BY " + DATE + " DESC," + KEY + " DESC";
        } else if (!lfilterMachine && lfilterDate) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + DATE + "=\"" + pDate
                    + "\" AND " + PROFIL_KEY + "=" + pProfile.getId()
                    + " ORDER BY " + DATE + " DESC," + KEY + " DESC";
        } else if (lfilterMachine && !lfilterDate) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine
                    + "\" AND " + PROFIL_KEY + "=" + pProfile.getId()
                    + " ORDER BY " + DATE + " DESC," + KEY + " DESC";
        } else if (!lfilterMachine && !lfilterDate) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + PROFIL_KEY + "=" + pProfile.getId()
                    + " ORDER BY " + DATE + " DESC," + KEY + " DESC";
        }

        // return value list
        return getRecordsListCursor(selectQuery);
    }

    /**
     * @return the last record for a profile p
     */
    public Record getLastRecord(Profile pProfile) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        Record lReturn = null;

        // Select All Machines
        /*String selectQuery = "SELECT " + KEY + " FROM " + TABLE_NAME
                + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " AND " + DATE + "=(SELECT MAX(" + DATE + ") FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + ");";
        ;*/

        String selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                + " WHERE " + PROFIL_KEY + "=" + pProfile.getId();
        ;
        mCursor = db.rawQuery(selectQuery, null);

        // looping through only the first rows.
        if (mCursor.moveToFirst()) {
            try {
                long value = mCursor.getLong(0);
                lReturn = getRecord(value);
            } catch (NumberFormatException e) {
                lReturn = null; // Return une valeur
            }
        }

        close();

        // return value list
        return lReturn;
    }

    /**
     * @return the last record for a profile p
     */
    public Record getLastExerciseRecord(long machineID) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        Record lReturn = null;

        // Select All Machines
        /*String selectQuery = "SELECT " + KEY + " FROM " + TABLE_NAME
                + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " AND " + DATE + "=(SELECT MAX(" + DATE + ") FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + ");";
        ;*/

        String selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                + " WHERE " + MACHINE_KEY + "=" + machineID;
        ;
        mCursor = db.rawQuery(selectQuery, null);

        // looping through only the first rows.
        if (mCursor.moveToFirst()) {
            try {
                long value = mCursor.getLong(0);
                lReturn = this.getRecord(value);
            } catch (NumberFormatException e) {
                lReturn = null; // Return une valeur
            }
        }

        close();

        // return value list
        return lReturn;
    }

    /**
     * @return the last record for a profile p
     */
    public Record getLastExerciseRecord(long machineID, Profile p) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        Record lReturn = null;

        String selectQuery;
        if (p == null) {
            selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                    + " WHERE " + MACHINE_KEY + "=" + machineID;
        } else {
            selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                    + " WHERE " + MACHINE_KEY + "=" + machineID + " AND " + PROFIL_KEY + "=" + p.getId();
        }
        mCursor = db.rawQuery(selectQuery, null);

        // looping through only the first rows.
        if (mCursor.moveToFirst()) {
            try {
                long value = mCursor.getLong(0);
                lReturn = this.getRecord(value);
            } catch (NumberFormatException e) {
                lReturn = null; // Return une valeur
            }
        }

        close();

        // return value list
        return lReturn;
    }

    // Get all record for one Machine
    public List<Record> getAllRecordByMachinesArray(Profile pProfile, String pMachines) {
        return getAllRecordByMachinesArray(pProfile, pMachines, -1);
    }

    public List<Record> getAllRecordByMachinesArray(Profile pProfile, String pMachines, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachines + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop;

        // return value list
        return getRecordsList(selectQuery);
    }

    // Getting All Records
    private List<Record> getRecordsList(String pRequest) {
        List<Record> valueList = new ArrayList<Record>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Select All Query
        String selectQuery = pRequest;

        mCursor = null;
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst() && mCursor.getCount() > 0) {
            do {
                //Get Date
                Date date;
                try {
                    SimpleDateFormat dateformat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
                    dateformat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    date = dateformat.parse(mCursor.getString(mCursor.getColumnIndex(DAOFonte.DATE)));
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = new Date();
                }

                //Get Profile
                DAOProfil lDAOProfil = new DAOProfil(mContext);
                Profile lProfile = lDAOProfil.getProfil(mCursor.getLong(mCursor.getColumnIndex(DAOFonte.PROFIL_KEY)));

                long machine_key = -1;

                //Test is Machine exists. If not create it.
                DAOMachine lDAOMachine = new DAOMachine(mContext);
                if (mCursor.getString(mCursor.getColumnIndex(DAOFonte.MACHINE_KEY)) == null) {
                    machine_key = lDAOMachine.addMachine(mCursor.getString(mCursor.getColumnIndex(DAOFonte.EXERCISE)), "", mCursor.getInt(mCursor.getColumnIndex(DAOFonte.TYPE)), "");
                } else {
                    machine_key = mCursor.getLong(mCursor.getColumnIndex(DAOFonte.MACHINE_KEY));
                }

                Record value = new Record(date,
                        mCursor.getString(mCursor.getColumnIndex(DAOFonte.EXERCISE)),
                        lProfile,
                        machine_key,
                        mCursor.getString(mCursor.getColumnIndex(DAOFonte.TIME)),
                        mCursor.getInt(mCursor.getColumnIndex(DAOFonte.TYPE)));

                value.setId(mCursor.getLong(mCursor.getColumnIndex(DAOFonte.KEY)));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        // return value list
        return valueList;
    }

    // Updating single value
    public int updateRecord(Record m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        value.put(DAOFonte.DATE, dateFormat.format(m.getDate()));
        value.put(DAOFonte.EXERCISE, m.getExercise());
        value.put(DAOFonte.PROFIL_KEY, m.getProfilKey());
        value.put(DAOFonte.TIME, m.getTime());
        value.put(DAOFonte.TYPE, m.getType());

        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
                new String[]{String.valueOf(m.getId())});
    }

    public void closeCursor() {
        if (mCursor != null) mCursor.close();
    }

    public void closeAll() {
        if (mCursor != null) mCursor.close();
        close();
    }
}
