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

public class DAOProgram  extends DAORecord{

    public static final int SUM_FCT = 0;
    public static final int MAX1_FCT = 1;
    public static final int MAX5_FCT = 2;
    public static final int NBSERIE_FCT = 3;

    private static final String TABLE_ARCHI = KEY + "," + SECONDS + "," + EXERCISE + "," + SERIE + "," + REPETITION + "," + WEIGHT + "," + UNIT + "," + PROFIL_KEY + "," + NOTES + "," + MACHINE_KEY;// + "," + TIME;

    public DAOProgram(Context context) {
        super(context);
    }

    /**
     * @param pMachine Machine name
     *                 Le Record a ajouter a la base
     */
    public long addProgramRecord(int restSecond, String pMachine, int pSerie, int pRepetition, float pPoids, Profile pProfile, int pUnit, String pNote) {
        return this.addRecord(pMachine, DAOMachine.TYPE_FONTE, pSerie, pRepetition, pPoids, pProfile, pUnit, pNote, 0, 0, restSecond, 0);
    }

    public long addRecord(String pMachine, int pType, int pSerie, int pRepetition, float pPoids, Profile pProfile, int pUnit, String pNote, float pDistance, long pDuration, int pSeconds, int distance_unit ) {
        ContentValues value = new ContentValues();
        long new_id = -1;
        long machine_key = -1;

        //Test is Machine exists. If not create it.
        DAOMachine lDAOMachine = new DAOMachine(mContext);
        if (!lDAOMachine.machineExists(pMachine)) {
            machine_key = lDAOMachine.addMachine(pMachine, "", pType, "", false, "");
        } else {
            machine_key = lDAOMachine.getMachine(pMachine).getId();
        }

        value.put(DAOExerciseInProgram.EXERCISE, pMachine);
        value.put(DAOExerciseInProgram.SERIE, pSerie);
        value.put(DAOExerciseInProgram.REPETITION, pRepetition);
        value.put(DAOExerciseInProgram.WEIGHT, pPoids);
        value.put(DAOExerciseInProgram.PROFIL_KEY, pProfile.getId());
        value.put(DAOExerciseInProgram.UNIT, pUnit);
        value.put(DAOExerciseInProgram.NOTES, pNote);
        value.put(DAOExerciseInProgram.MACHINE_KEY, machine_key);
        value.put(DAOExerciseInProgram.DISTANCE, pDistance);
        value.put(DAOExerciseInProgram.DURATION, pDuration);
        value.put(DAOExerciseInProgram.TYPE, pType);
        value.put(DAOExerciseInProgram.SECONDS, pSeconds);
        value.put(DAOExerciseInProgram.DISTANCE_UNIT, distance_unit);

        SQLiteDatabase db = open();
        new_id = db.insert(DAOExerciseInProgram.TABLE_NAME, null, value);
        close();

        return new_id;
    }
    /**
     * @param programList List of Program records
     */
    public void addProgramList(List<Program> programList) {
        for (Program program: programList) {
            addRecord(program.mDate, program.mExercise, DAOMachine.TYPE_FONTE, program.getSerie(), program.getRepetition(), program.getPoids(), program.mProfile, program.getUnit(), program.getNote(), program.mTime, 0, 0, 0, 0);
        }
    }

    // Getting single value
    public Program getProgramRecord(long id) {
        String selectQuery = "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME + " WHERE " + KEY + "=" + id;
        List<Program> valueList;

        valueList = getRecordsList(selectQuery);
        if (valueList.isEmpty())
            return null;
        else
            return valueList.get(0);
    }

    // Getting All Records
    private List<Program> getRecordsList(String pRequest) {
        List<Program> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Select All Query

        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst() && mCursor.getCount() > 0) {
            do {
                //Get Date
//                Date date;
//                try {
//                    SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
//                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//                    date = dateFormat.parse(mCursor.getString(mCursor.getColumnIndex(DAOProgram.DATE)));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                    date = new Date();
//                }

                //Get Profile
                DAOProfil lDAOProfil = new DAOProfil(mContext);
                Profile lProfile = lDAOProfil.getProfil(mCursor.getLong(mCursor.getColumnIndex(DAOProgram.PROFIL_KEY)));

                long machine_key = -1;

                //Test is Machine exists. If not create it.
                DAOMachine lDAOMachine = new DAOMachine(mContext);
                if (mCursor.getString(mCursor.getColumnIndex(DAOProgram.MACHINE_KEY)) == null) {
                    machine_key = lDAOMachine.addMachine(mCursor.getString(mCursor.getColumnIndex(DAOProgram.EXERCISE)), "", DAOMachine.TYPE_FONTE, "", false, "");
                } else {
                    machine_key = mCursor.getLong(mCursor.getColumnIndex(DAOProgram.MACHINE_KEY));
                }
                int defaultRestSec=60;
                Program value = new Program(defaultRestSec, mCursor.getString(mCursor.getColumnIndex(DAOProgram.EXERCISE)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOProgram.SERIE)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOProgram.REPETITION)),
                    mCursor.getFloat(mCursor.getColumnIndex(DAOProgram.WEIGHT)),
                    lProfile,
                    mCursor.getInt(mCursor.getColumnIndex(DAOProgram.UNIT)),
                    mCursor.getString(mCursor.getColumnIndex(DAOProgram.NOTES)),
                    machine_key,
                    mCursor.getString(mCursor.getColumnIndex(DAOProgram.TIME)));

                value.setId(mCursor.getLong(mCursor.getColumnIndex(DAOProgram.KEY)));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        // return value list
        return valueList;
    }

    // Getting All Records
    public List<Program> getAllProgramRecords() {
        // Select All Query
        String selectQuery = "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME
            + " WHERE " + TYPE + "=" + DAOMachine.TYPE_FONTE
            + " ORDER BY " + DATE + " DESC," + KEY + " DESC";

        // return value list
        return getRecordsList(selectQuery);
    }

    // Getting All Records
    public List<Program> getAllProgramRecordsByProfileArray(Profile pProfile) {
        return getAllProgramRecordsByProfileArray(pProfile, -1);
    }

    private List<Program> getAllProgramRecordsByProfileArray(Profile pProfile, int pNbRecords) {
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
    public List<GraphData> getProgramFunctionRecords(Profile pProfile, String pMachine,
                                                          int pFunction) {

        String selectQuery = null;

        // TODO attention aux units de poids. Elles ne sont pas encore prise en compte ici.
        if (pFunction == DAOProgram.SUM_FCT) {
            selectQuery = "SELECT SUM(" + SERIE + "*" + REPETITION + "*"
                + WEIGHT + "), " + DATE + " FROM " + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DAOProgram.MAX5_FCT) {
            selectQuery = "SELECT MAX(" + WEIGHT + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + REPETITION + ">=5"
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DAOProgram.MAX1_FCT) {
            selectQuery = "SELECT MAX(" + WEIGHT + ") , " + DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + REPETITION + ">=1"
                + " AND " + PROFIL_KEY + "=" + pProfile.getId()
                + " GROUP BY " + DATE
                + " ORDER BY date(" + DATE + ") ASC";
        } else if (pFunction == DAOProgram.NBSERIE_FCT) {
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

        // return value list
        return valueList;
    }

//    /**
//     * @return the number of series for this machine for this day
//     */
//    public int getNbSeries(Date pDate, String pMachine) {
//
//
//        int lReturn = 0;
//
//        //Test is Machine exists. If not create it.
//        DAOMachine lDAOMachine = new DAOMachine(mContext);
//        long machine_key = lDAOMachine.getMachine(pMachine).getId();
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
//        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//        String lDate = dateFormat.format(pDate);
//
//        SQLiteDatabase db = this.getReadableDatabase();
//        mCursor = null;
//
//        // Select All Machines
//        String selectQuery = "SELECT SUM(" + SERIE + ") FROM " + TABLE_NAME
//            + " WHERE " + DATE + "=\"" + lDate + "\" AND " + MACHINE_KEY + "=" + machine_key;
//        mCursor = db.rawQuery(selectQuery, null);
//
//        // looping through all rows and adding to list
//        mCursor.moveToFirst();
//        try {
//            lReturn = mCursor.getInt(0);
//        } catch (NumberFormatException e) {
//            //Date date = new Date();
//            lReturn = 0; // Return une valeur
//        }
//
//        close();
//
//        // return value
//        return lReturn;
//    }

//    /**
//     * @return the total weight for this machine for this day
//     */
//    public float getTotalWeightMachine(Date pDate, String pMachine) {
//
//        float lReturn = 0;
//
//        //Test is Machine exists. If not create it.
//        DAOMachine lDAOMachine = new DAOMachine(mContext);
//        long machine_key = lDAOMachine.getMachine(pMachine).getId();
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
//        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//        String lDate = dateFormat.format(pDate);
//
//        SQLiteDatabase db = this.getReadableDatabase();
//        mCursor = null;
//        // Select All Machines
//        String selectQuery = "SELECT " + SERIE + ", " + WEIGHT + ", " + REPETITION + " FROM " + TABLE_NAME
//            + " WHERE " + DATE + "=\"" + lDate + "\" AND " + MACHINE_KEY + "=" + machine_key;
//        mCursor = db.rawQuery(selectQuery, null);
//
//        // looping through all rows and adding to list
//        if (mCursor.moveToFirst()) {
//            int i = 0;
//            do {
//                float value = mCursor.getInt(0) * mCursor.getFloat(1) * mCursor.getInt(2);
//                lReturn += value;
//                i++;
//            } while (mCursor.moveToNext());
//        }
//        close();
//
//        // return value
//        return lReturn;
//    }


//    /**
//     * @return the total weight for this day
//     */
//    public float getTotalWeightSession(Date pDate) {
//
//        SQLiteDatabase db = this.getReadableDatabase();
//        mCursor = null;
//        float lReturn = 0;
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
//        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//        String lDate = dateFormat.format(pDate);
//
//        // Select All Machines
//        String selectQuery = "SELECT " + SERIE + ", " + WEIGHT + ", " + REPETITION + " FROM " + TABLE_NAME
//            + " WHERE " + DATE + "=\"" + lDate + "\"";
//        mCursor = db.rawQuery(selectQuery, null);
//
//        // looping through all rows and adding to list
//        if (mCursor.moveToFirst()) {
//            int i = 0;
//            do {
//                float value = mCursor.getInt(0) * mCursor.getFloat(1) * mCursor.getInt(2);
//                lReturn += value;
//                i++;
//            } while (mCursor.moveToNext());
//        }
//        close();
//
//        // return value
//        return lReturn;
//    }

//    /**
//     * @return Max weight for a profile p and a machine m
//     */
//    public Weight getMax(Profile p, Machine m) {
//
//        SQLiteDatabase db = this.getReadableDatabase();
//        mCursor = null;
//        Weight w = null;
//
//        // Select All Machines
//        String selectQuery = "SELECT MAX(" + WEIGHT + "), " + UNIT + " FROM " + TABLE_NAME
//            + " WHERE " + PROFIL_KEY + "=" + p.getId() + " AND " + MACHINE_KEY + "=" + m.getId();
//        mCursor = db.rawQuery(selectQuery, null);
//
//        // looping through all rows and adding to list
//        if (mCursor.moveToFirst()) {
//            do {
//                w = new Weight(mCursor.getFloat(0), mCursor.getInt(1));
//            } while (mCursor.moveToNext());
//        }
//        close();
//
//        // return value
//        return w;
//    }
//
//    /**
//     * @return Min weight for a profile p and a machine m
//     */
//    public Weight getMin(Profile p, Machine m) {
//
//        SQLiteDatabase db = this.getReadableDatabase();
//        mCursor = null;
//        Weight w = null;
//
//        // Select All Machines
//        String selectQuery = "SELECT MIN(" + WEIGHT + "), " + UNIT + " FROM " + TABLE_NAME
//            + " WHERE " + PROFIL_KEY + "=" + p.getId() + " AND " + MACHINE_KEY + "=" + m.getId();
//        mCursor = db.rawQuery(selectQuery, null);
//
//        // looping through all rows and adding to list
//        if (mCursor.moveToFirst()) {
//            do {
//                w = new Weight(mCursor.getFloat(0), mCursor.getInt(1));
//            } while (mCursor.moveToNext());
//        }
//        close();
//
//        // return value
//        return w;
//    }

    // Updating single value
    public int updateRecord(Program m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

//        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
//        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        value.put(DAOProgram.SECONDS, m.getSecRest());
//        value.put(DAOProgram.DATE, dateFormat.format(m.getDate()));
        value.put(DAOProgram.EXERCISE, m.getExercise());
        value.put(DAOProgram.MACHINE_KEY, m.getExerciseKey());
        value.put(DAOProgram.SERIE, m.getSerie());
        value.put(DAOProgram.REPETITION, m.getRepetition());
        value.put(DAOProgram.WEIGHT, m.getPoids());
        value.put(DAOProgram.UNIT, m.getUnit());
        value.put(DAOProgram.NOTES, m.getNote());
        value.put(DAOProgram.PROFIL_KEY, m.getProfilKey());
        value.put(DAOProgram.TIME, m.getTime());
        value.put(DAOProgram.TYPE, DAOMachine.TYPE_FONTE);

        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }


//    public void populate() {
//        // DBORecord(long id, Date pDate, String pMachine, int pSerie, int
//        // pRepetition, int pPoids)
//        Date date = new Date();
//        int poids = 10;
//
//        for (int i = 1; i <= 5; i++) {
//            String machine = "Biceps";
//            date.setDate(date.getDay() + i * 10);
//            addProgramRecord(date, machine, i * 2, 10 + i, poids * i, mProfile, 0, "", "12:34:56");
//        }
//
//        date = new Date();
//        poids = 12;
//
//        for (int i = 1; i <= 5; i++) {
//            String machine = "Dev Couche";
//            date.setDate(date.getDay() + i * 10);
//            addProgramRecord(date, machine, i * 2, 10 + i, poids * i, mProfile, 0, "", "12:34:56");
//        }
//    }
}
