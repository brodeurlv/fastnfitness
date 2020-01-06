package com.easyfitness.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.GraphData;
import com.easyfitness.utils.DateConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DAOStatic extends DAORecord {

    public static final int MAX_FCT = 1;
    public static final int NBSERIE_FCT = 2;

    private static final String TABLE_ARCHI = KEY + "," + DATE + "," + EXERCISE + "," + SERIE + "," + SECONDS + "," + WEIGHT + "," + UNIT + "," + PROFIL_KEY + "," + NOTES + "," + MACHINE_KEY + "," + TIME;

    public DAOStatic(Context context) {
        super(context);
    }

    /**
     * @param pDate    Date
     * @param pMachine Machine name
     *                 Le Record a ajouter a la base
     */
    public long addStaticRecord(Date pDate, String pMachine, int pSerie, int pSeconds, float pPoids, Profile pProfile, int pUnit, String pNote, String pTime) {
        return addRecord(pDate, pMachine, DAOMachine.TYPE_STATIC, pSerie, 0, pPoids, pProfile, pUnit, pNote, pTime, 0, 0, pSeconds, 0);
    }

    public void addStaticList(List<StaticExercise> staticExerciseList) {
        for (StaticExercise staticExercise: staticExerciseList) {
            addRecord(staticExercise.mDate, staticExercise.getExercise(), DAOMachine.TYPE_CARDIO, staticExercise.getSerie(), 0, staticExercise.getPoids(), staticExercise.getProfil(), 0, "", staticExercise.getTime(), 0, 0, staticExercise.getSecond(), 0);
        }
    }

    // Getting single value
    public StaticExercise getStaticRecord(long id) {
        String selectQuery = "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME + " WHERE " + KEY + "=" + id;
        List<StaticExercise> valueList;

        valueList = getRecordsList(selectQuery);
        if (valueList.isEmpty())
            return null;
        else
            return valueList.get(0);
    }

    // Getting All Records
    private List<StaticExercise> getRecordsList(String pRequest) {
        List<StaticExercise> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Select All Query

        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst() && mCursor.getCount() > 0) {
            do {
                //Get Date
                Date date;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    date = dateFormat.parse(mCursor.getString(mCursor.getColumnIndex(DAOStatic.DATE)));
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = new Date();
                }

                //Get Profile
                DAOProfil lDAOProfil = new DAOProfil(mContext);
                Profile lProfile = lDAOProfil.getProfil(mCursor.getLong(mCursor.getColumnIndex(DAOStatic.PROFIL_KEY)));

                long machine_key = -1;

                //Test is Machine exists. If not create it.
                DAOMachine lDAOMachine = new DAOMachine(mContext);
                if (mCursor.getString(mCursor.getColumnIndex(DAOStatic.MACHINE_KEY)) == null) {
                    machine_key = lDAOMachine.addMachine(mCursor.getString(mCursor.getColumnIndex(DAOStatic.EXERCISE)), "", DAOMachine.TYPE_STATIC, "", false, "");
                } else {
                    machine_key = mCursor.getLong(mCursor.getColumnIndex(DAOStatic.MACHINE_KEY));
                }

                StaticExercise value = new StaticExercise(date, mCursor.getString(mCursor.getColumnIndex(DAOStatic.EXERCISE)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOStatic.SERIE)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOStatic.SECONDS)),
                    mCursor.getFloat(mCursor.getColumnIndex(DAOStatic.WEIGHT)),
                    lProfile,
                    mCursor.getInt(mCursor.getColumnIndex(DAOStatic.UNIT)),
                    machine_key,
                    mCursor.getString(mCursor.getColumnIndex(DAOStatic.TIME)));

                value.setId(mCursor.getLong(mCursor.getColumnIndex(DAOStatic.KEY)));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        // return value list
        return valueList;
    }

    // Getting All Records
    public List<StaticExercise> getAllStaticRecords() {
        // Select All Query
        String selectQuery = "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME
            + " WHERE " + TYPE + "=" + DAOMachine.TYPE_STATIC
            + " ORDER BY " + DATE + " DESC," + KEY + " DESC";

        // return value list
        return getRecordsList(selectQuery);
    }

    // Getting All Records
    public List<StaticExercise> getAllStaticRecordsByProfileArray(Profile pProfile) {
        return getAllStaticRecordsByProfileArray(pProfile, -1);
    }

    private List<StaticExercise> getAllStaticRecordsByProfileArray(Profile pProfile, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;


        // Select All Query
        String selectQuery = "SELECT " + TABLE_ARCHI + " FROM " + TABLE_NAME
            + " WHERE " + PROFIL_KEY + "=" + pProfile.getId()
            + " AND " + TYPE + "=" + DAOMachine.TYPE_STATIC
            + " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop;

        // Return value list
        return getRecordsList(selectQuery);
    }

    // Getting Function records
    public List<GraphData> getStaticFunctionRecords(Profile pProfile, String pMachine,
                                                    int pFunction) {

        String selectQuery = null;

        // TODO attention aux units de poids. Elles ne sont pas encore prise en compte ici.
        if (pFunction == DAOStatic.MAX_FCT) {
            selectQuery = "SELECT MAX(" + WEIGHT + ") , " + SECONDS + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + SECONDS
                + " ORDER BY " + SECONDS + " ASC";
        } else if (pFunction == DAOStatic.NBSERIE_FCT) {
            selectQuery = "SELECT count(" + KEY + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        }

        // Formation de tableau de valeur
        List<GraphData> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.rawQuery(selectQuery, null);

        double i = 0;

        // looping through all rows and adding to list
        if (pFunction == DAOStatic.NBSERIE_FCT) {
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

                    GraphData value = new GraphData(DateConverter.nbDays(date.getTime()), mCursor.getDouble(0));

                    // Adding value to list
                    valueList.add(value);
                } while (mCursor.moveToNext());
            }
        } else if (pFunction == DAOStatic.MAX_FCT) {
            if (mCursor.moveToFirst()) {
                do {
                    GraphData value = new GraphData(mCursor.getDouble(1), mCursor.getDouble(0));
                    valueList.add(value);
                } while (mCursor.moveToNext());
            }

        }

        // return value list
        return valueList;
    }

    /**
     * @return the number of series for this machine for this day
     */
    public int getNbSeries(Date pDate, String pMachine) {


        int lReturn = 0;

        //Test is Machine exists. If not create it.
        DAOMachine lDAOMachine = new DAOMachine(mContext);
        long machine_key = lDAOMachine.getMachine(pMachine).getId();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String lDate = dateFormat.format(pDate);

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;

        // Select All Machines
        String selectQuery = "SELECT SUM(" + SERIE + ") FROM " + TABLE_NAME
            + " WHERE " + DATE + "=\"" + lDate + "\" AND " + MACHINE_KEY + "=" + machine_key;
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        mCursor.moveToFirst();
        try {
            lReturn = mCursor.getInt(0);
        } catch (NumberFormatException e) {
            //Date date = new Date();
            lReturn = 0; // Return une valeur
        }

        close();

        // return value
        return lReturn;
    }

    /**
     * @return the total weight for this machine for this day
     */
    public float getTotalWeightMachine(Date pDate, String pMachine) {

        float lReturn = 0;

        //Test is Machine exists. If not create it.
        DAOMachine lDAOMachine = new DAOMachine(mContext);
        long machine_key = lDAOMachine.getMachine(pMachine).getId();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String lDate = dateFormat.format(pDate);

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        // Select All Machines
        String selectQuery = "SELECT " + SERIE + ", " + WEIGHT + " FROM " + TABLE_NAME
            + " WHERE " + DATE + "=\"" + lDate + "\" AND " + MACHINE_KEY + "=" + machine_key;
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                float value = mCursor.getInt(0) * mCursor.getFloat(1) ;
                lReturn += value;
                i++;
            } while (mCursor.moveToNext());
        }
        close();

        // return value
        return lReturn;
    }


    /**
     * @return the total weight for this day
     */
    public float getTotalWeightSession(Date pDate) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        float lReturn = 0;

        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String lDate = dateFormat.format(pDate);

        // Select All Machines
        String selectQuery = "SELECT " + SERIE + ", " + WEIGHT  + " FROM " + TABLE_NAME
            + " WHERE " + DATE + "=\"" + lDate + "\"";
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                float value = mCursor.getInt(0) * mCursor.getFloat(1) ;
                lReturn += value;
                i++;
            } while (mCursor.moveToNext());
        }
        close();

        // return value
        return lReturn;
    }

    /**
     * @return Max weight for a profile p and a machine m
     */
    public Weight getMax(Profile p, Machine m) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        Weight w = null;

        // Select All Machines
        String selectQuery = "SELECT MAX(" + WEIGHT + "), " + UNIT + " FROM " + TABLE_NAME
            + " WHERE " + PROFIL_KEY + "=" + p.getId() + " AND " + MACHINE_KEY + "=" + m.getId();
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                w = new Weight(mCursor.getFloat(0), mCursor.getInt(1));
            } while (mCursor.moveToNext());
        }
        close();

        // return value
        return w;
    }

    /**
     * @return Min weight for a profile p and a machine m
     */
    public Weight getMin(Profile p, Machine m) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        Weight w = null;

        // Select All Machines
        String selectQuery = "SELECT MIN(" + WEIGHT + "), " + UNIT + " FROM " + TABLE_NAME
            + " WHERE " + PROFIL_KEY + "=" + p.getId() + " AND " + MACHINE_KEY + "=" + m.getId();
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                w = new Weight(mCursor.getFloat(0), mCursor.getInt(1));
            } while (mCursor.moveToNext());
        }
        close();

        // return value
        return w;
    }

    // Updating single value
    public int updateRecord(StaticExercise m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        value.put(DAOStatic.DATE, dateFormat.format(m.getDate()));
        value.put(DAOStatic.EXERCISE, m.getExercise());
        value.put(DAOStatic.MACHINE_KEY, m.getExerciseKey());
        value.put(DAOStatic.SERIE, m.getSerie());
        value.put(DAOStatic.SECONDS, m.getSecond());
        value.put(DAOStatic.WEIGHT, m.getPoids());
        value.put(DAOStatic.UNIT, m.getUnit());
        value.put(DAOStatic.NOTES, m.getNote());
        value.put(DAOStatic.PROFIL_KEY, m.getProfilKey());
        value.put(DAOStatic.TIME, m.getTime());
        value.put(DAOStatic.TYPE, DAOMachine.TYPE_STATIC);

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
            String machine = "Biceps";
            date.setDate(date.getDay() + i * 10);
            addStaticRecord(date, machine, i * 2, 10 + i, poids * i, mProfile, 0, "", "12:34:56");
        }

        date = new Date();
        poids = 12;

        for (int i = 1; i <= 5; i++) {
            String machine = "Dev Couche";
            date.setDate(date.getDay() + i * 10);
            addStaticRecord(date, machine, i * 2, 10 + i, poids * i, mProfile, 0, "", "12:34:56");
        }
    }

}
