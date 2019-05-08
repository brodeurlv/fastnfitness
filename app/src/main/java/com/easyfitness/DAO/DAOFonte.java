package com.easyfitness.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DateGraphData;
import com.easyfitness.utils.DateConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DAOFonte extends DAORecord {

    public static final int SUM_FCT = 0;
    public static final int MAX1_FCT = 1;
    public static final int MAX5_FCT = 2;
    public static final int NBSERIE_FCT = 3;

    private static final String TABLE_ARCHI = KEY + "," + DATE + "," + EXERCISE + "," + SERIE + "," + REPETITION + "," + WEIGHT + "," + UNIT + "," + PROFIL_KEY + "," + NOTES + "," + MACHINE_KEY + "," + TIME;

    public DAOFonte(Context context) {
        super(context);
    }

    /**
     * @param pDate    Date
     * @param pMachine Machine name
     *                 Le Record a ajouter a la base
     */
    public long addBodyBuildingRecord(Date pDate, String pMachine, int pSerie, int pRepetition, float pPoids, Profile pProfile, int pUnit, String pNote, String pTime) {
        return addRecord(pDate, pMachine, DAOMachine.TYPE_FONTE, pSerie, pRepetition, pPoids, pProfile, pUnit, pNote, pTime, 0, 0);
    }

    // Getting single value
    public Fonte getBodyBuildingRecord(long id) {
        String selectQuery = "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME + " WHERE " + KEY + "=" + id;
        List<Fonte> valueList;

        valueList = getRecordsList(selectQuery);
        if (valueList.isEmpty())
            return null;
        else
            return valueList.get(0);
    }

    // Getting All Records
    private List<Fonte> getRecordsList(String pRequest) {
        List<Fonte> valueList = new ArrayList<>();
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
                    date = dateFormat.parse(mCursor.getString(mCursor.getColumnIndex(DAOFonte.DATE)));
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
                    machine_key = lDAOMachine.addMachine(mCursor.getString(mCursor.getColumnIndex(DAOFonte.EXERCISE)), "", DAOMachine.TYPE_FONTE, "", false);
                } else {
                    machine_key = mCursor.getLong(mCursor.getColumnIndex(DAOFonte.MACHINE_KEY));
                }

                Fonte value = new Fonte(date, mCursor.getString(mCursor.getColumnIndex(DAOFonte.EXERCISE)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOFonte.SERIE)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOFonte.REPETITION)),
                    mCursor.getFloat(mCursor.getColumnIndex(DAOFonte.WEIGHT)),
                    lProfile,
                    mCursor.getInt(mCursor.getColumnIndex(DAOFonte.UNIT)),
                    mCursor.getString(mCursor.getColumnIndex(DAOFonte.NOTES)),
                    machine_key,
                    mCursor.getString(mCursor.getColumnIndex(DAOFonte.TIME)));

                value.setId(mCursor.getLong(mCursor.getColumnIndex(DAOFonte.KEY)));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        // return value list
        return valueList;
    }

    // Getting All Records
    public List<Fonte> getAllBodyBuildingRecords() {
        // Select All Query
        String selectQuery = "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME
            + " WHERE " + TYPE + "=" + DAOMachine.TYPE_FONTE
            + " ORDER BY " + DATE + " DESC," + KEY + " DESC";

        // return value list
        return getRecordsList(selectQuery);
    }

    // Getting All Records
    public List<Fonte> getAllBodyBuildingRecordsByProfileArray(Profile pProfile) {
        return getAllBodyBuildingRecordsByProfileArray(pProfile, -1);
    }

    private List<Fonte> getAllBodyBuildingRecordsByProfileArray(Profile pProfile, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;


        // Select All Query
        String selectQuery = "SELECT " + TABLE_ARCHI + " FROM " + TABLE_NAME
            + " WHERE " + PROFIL_KEY + "=" + pProfile.getId()
            + " AND " + TYPE + "=" + DAOMachine.TYPE_FONTE
            + " ORDER BY " + DATE + " DESC," + KEY + " DESC" + mTop;

        // Return value list
        return getRecordsList(selectQuery);
    }

    // Getting Function records
    public List<DateGraphData> getBodyBuildingFunctionRecords(Profile pProfile, String pMachine,
                                                              int pFunction) {

        String selectQuery = null;

        // TODO attention aux units de poids. Elles ne sont pas encore prise en compte ici.
        if (pFunction == DAOFonte.SUM_FCT) {
            selectQuery = "SELECT SUM(" + SERIE + "*" + REPETITION + "*"
                + WEIGHT + "), " + DATE + " FROM " + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DAOFonte.MAX5_FCT) {
            selectQuery = "SELECT MAX(" + WEIGHT + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + REPETITION + ">=5"
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DAOFonte.MAX1_FCT) {
            selectQuery = "SELECT MAX(" + WEIGHT + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + REPETITION + ">=1"
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DAOFonte.NBSERIE_FCT) {
            selectQuery = "SELECT count(" + KEY + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        }

        // Formation de tableau de valeur
        List<DateGraphData> valueList = new ArrayList<>();
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

                DateGraphData value = new DateGraphData(DateConverter.nbDays(date.getTime()), mCursor.getDouble(0));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
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
        String selectQuery = "SELECT " + SERIE + ", " + WEIGHT + ", " + REPETITION + " FROM " + TABLE_NAME
            + " WHERE " + DATE + "=\"" + lDate + "\" AND " + MACHINE_KEY + "=" + machine_key;
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                float value = mCursor.getInt(0) * mCursor.getFloat(1) * mCursor.getInt(2);
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
        String selectQuery = "SELECT " + SERIE + ", " + WEIGHT + ", " + REPETITION + " FROM " + TABLE_NAME
            + " WHERE " + DATE + "=\"" + lDate + "\"";
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                float value = mCursor.getInt(0) * mCursor.getFloat(1) * mCursor.getInt(2);
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
    public int updateRecord(Fonte m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        value.put(DAOFonte.DATE, dateFormat.format(m.getDate()));
        value.put(DAOFonte.EXERCISE, m.getExercise());
        value.put(DAOFonte.MACHINE_KEY, m.getExerciseKey());
        value.put(DAOFonte.SERIE, m.getSerie());
        value.put(DAOFonte.REPETITION, m.getRepetition());
        value.put(DAOFonte.WEIGHT, m.getPoids());
        value.put(DAOFonte.UNIT, m.getUnit());
        value.put(DAOFonte.NOTES, m.getNote());
        value.put(DAOFonte.PROFIL_KEY, m.getProfilKey());
        value.put(DAOFonte.TIME, m.getTime());
        value.put(DAOFonte.TYPE, DAOMachine.TYPE_FONTE);

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
            addBodyBuildingRecord(date, machine, i * 2, 10 + i, poids * i, mProfile, 0, "", "12:34:56");
        }

        date = new Date();
        poids = 12;

        for (int i = 1; i <= 5; i++) {
            String machine = "Dev Couche";
            date.setDate(date.getDay() + i * 10);
            addBodyBuildingRecord(date, machine, i * 2, 10 + i, poids * i, mProfile, 0, "", "12:34:56");
        }
    }

}
