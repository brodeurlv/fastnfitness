package com.easyfitness.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DateGraphData;
import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DAOCardio extends DAORecord {

    public static final int DISTANCE_FCT = 0;
    public static final int DURATION_FCT = 1;
    public static final int SPEED_FCT = 2;
    public static final int MAXDURATION_FCT = 3;
    public static final int MAXDISTANCE_FCT = 4;
    public static final int NBSERIE_FCT = 5;

    private static final String OLD_TABLE_NAME = "EFcardio";

    private static final String TABLE_ARCHI = KEY + "," + DATE + "," + EXERCISE + "," + DISTANCE + "," + DURATION + "," + PROFIL_KEY + "," + TIME;

    public DAOCardio(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * @param pDate
     * @param pTime
     * @param pMachine
     * @param pDistance
     * @param pDuration
     * @param pProfile
     * @return
     */
    public long addCardioRecord(Date pDate, String pTime, String pMachine, float pDistance, long pDuration, Profile pProfile) {
        return addRecord(pDate, pMachine, DAOMachine.TYPE_CARDIO, 0, 0, 0, pProfile, 0, "", pTime, pDistance, pDuration);
    }

    // Getting single value
    public Cardio getRecord(long id) {
        String selectQuery = "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME
            + " WHERE " + KEY + "=" + id;
        List<Cardio> valueList;

        valueList = getRecordsList(selectQuery);
        if (valueList.isEmpty())
            return null;
        else
            return valueList.get(0);
    }

    // Getting All Records
    private List<Cardio> getRecordsList(String pRequest) {
        List<Cardio> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Select All Query

        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                //Get Date
                Date date;
                try {
                    date = new SimpleDateFormat(DAOUtils.DATE_FORMAT).parse(mCursor.getString(mCursor.getColumnIndex(DAOCardio.DATE)));
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = new Date();
                }

                //Get Profile
                DAOProfil lDAOProfil = new DAOProfil(mContext);
                Profile lProfile = lDAOProfil.getProfil(mCursor.getLong(mCursor.getColumnIndex(DAOCardio.PROFIL_KEY)));

                Cardio value = new Cardio(date,
                    mCursor.getString(mCursor.getColumnIndex(DAOCardio.EXERCISE)),
                    mCursor.getFloat(mCursor.getColumnIndex(DAOCardio.DISTANCE)),
                    mCursor.getLong(mCursor.getColumnIndex(DAOCardio.DURATION)),
                    lProfile);

                value.setId(Long.parseLong(mCursor.getString(mCursor.getColumnIndex(DAOCardio.KEY))));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        // return value list
        return valueList;
    }

    // Getting All Records
    public List<Cardio> getAllRecords() {
        // Select All Query
        String selectQuery = "SELECT " + TABLE_ARCHI + " FROM " + TABLE_NAME
            + " ORDER BY " + KEY + " DESC";

        // return value list
        return getRecordsList(selectQuery);
    }

    // Getting All Records
    public List<Cardio> getAllCardioRecordsByProfile(Profile pProfile) {
        // Select All Query
        String selectQuery = "SELECT " + TABLE_ARCHI + " FROM " + TABLE_NAME
            + " WHERE " + PROFIL_KEY + "=" + pProfile.getId()
            + " AND " + TYPE + "=" + DAOMachine.TYPE_CARDIO
            + " ORDER BY " + KEY + " DESC";

        // return value list
        return getRecordsList(selectQuery);
    }

    // Getting Top 10 Records
    public List<Cardio> getTop10Records(Profile pProfile) {
        // Select All Query
        String selectQuery = "SELECT TOP 10 * FROM " + TABLE_NAME
            + " WHERE " + PROFIL_KEY + "=" + pProfile.getId()
            + " AND " + TYPE + "=" + DAOMachine.TYPE_CARDIO
            + " ORDER BY " + KEY + " DESC";

        // return value list
        return getRecordsList(selectQuery);
    }

    // Getting Function records
    public List<DateGraphData> getFunctionRecords(Profile pProfile, String pMachine,
                                                  int pFunction) {

        boolean lfilterMachine = true;
        boolean lfilterFunction = true;
        String selectQuery = null;

        if (pMachine == null || pMachine.isEmpty() || pMachine.equals(mContext.getResources().getText(R.string.all).toString())) {
            lfilterMachine = false;
        }

        if (pFunction == DAOCardio.DISTANCE_FCT) {
            selectQuery = "SELECT SUM(" + DISTANCE + "), " + DATE + " FROM " + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DAOCardio.DURATION_FCT) {
            selectQuery = "SELECT SUM(" + DURATION + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DAOCardio.SPEED_FCT) {
            selectQuery = "SELECT SUM(" + DISTANCE + ") / SUM(" + DURATION + ")," + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DAOCardio.MAXDISTANCE_FCT) {
            selectQuery = "SELECT MAX(" + DISTANCE + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        }
        // case "MEAN" : selectQuery = "SELECT SUM("+ SERIE + "*" + REPETITION +
        // "*" + WEIGHT +") FROM " + TABLE_NAME + " WHERE " + EXERCISE + "=\"" +
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
                    SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    date = dateFormat.parse(mCursor.getString(1));
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = new Date();
                }

                DateGraphData value = new DateGraphData(DateConverter.nbDays(date.getTime()),
                    mCursor.getDouble(0));

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
        String selectQuery = "SELECT DISTINCT  " + EXERCISE + " FROM " + TABLE_NAME
            + " WHERE " + PROFIL_KEY + "=" + pProfile.getId()
            + " AND " + TYPE + "=" + DAOMachine.TYPE_CARDIO
            + " ORDER BY " + EXERCISE + " COLLATE NOCASE ASC";
        mCursor = db.rawQuery(selectQuery, null);

        int size = mCursor.getCount();

        String[] valueList = new String[size];

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                String value = mCursor.getString(mCursor.getColumnIndex(DAOCardio.EXERCISE));
                valueList[i] = value;
                i++;
            } while (mCursor.moveToNext());
        }
        close();
        // return value list
        return valueList;
    }

    // Get all record for one Exercise
    public List<Cardio> getAllCardioRecordByMachines(Profile pProfile, String pExercise) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME
            + " WHERE " + EXERCISE + "=\"" + pExercise + "\""
            + " AND " + PROFIL_KEY + "=" + pProfile.getId()
            + " ORDER BY " + KEY + " DESC";

        // return value list
        return getRecordsList(selectQuery);
    }

    // Updating single value
    public int updateRecord(Profile pProfile, Cardio m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(DAOCardio.DATE, m.getDate().toString());
        value.put(DAOCardio.EXERCISE, m.getExercise());
        value.put(DAOCardio.MACHINE_KEY, m.getExerciseKey());
        value.put(DAOCardio.DISTANCE, m.getDistance());
        value.put(DAOCardio.DURATION, m.getDuration());
        value.put(DAOCardio.PROFIL_KEY, pProfile.getId());

        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }

    public void populate() {
        // DBORecord(long id, Date pDate, String pMachine, int pSerie, int
        // pRepetition, int pPoids)
        Date date = new Date();
        int poids = 10;

        for (int i = 1; i <= 5; i++) {
            String machine = "Tapis";
            date.setDate(date.getDay() + i * 10);
            addCardioRecord(date, "00:00", machine, (float) i * 20, 120000 * i, mProfile);
        }

        date = new Date();
        poids = 12;

        for (int i = 1; i <= 5; i++) {
            String machine = "Rameur";
            date.setDate(date.getDay() + i * 10);
            addCardioRecord(date, "00:00", machine, 0, 120000 * i * 3, mProfile);
        }
    }

}
