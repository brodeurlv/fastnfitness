package com.easyfitness.DAO.record;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.Weight;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.RecordType;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.graph.GraphData;
import com.easyfitness.utils.DateConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAOStatic extends DAORecord {

    public static final int MAX_FCT = 1;
    public static final int NBSERIE_FCT = 2;
    public static final int MAX_LENGTH = 3;

    private static final String TABLE_ARCHI = KEY + "," + DATE + "," + EXERCISE + "," + SETS + "," + SECONDS + "," + WEIGHT + "," + WEIGHT_UNIT + "," + PROFILE_KEY + "," + NOTES + "," + EXERCISE_KEY + "," + TIME;


    public DAOStatic(Context context) {
        super(context);
    }

    /**
     * @param pDate      Date
     * @param pMachine   Machine name
     * @param pProfileId Profile ID
     */
    public long addStaticRecordToFreeWorkout(Date pDate, String pMachine, int pSerie, int pSeconds, float pPoids, long pProfileId, WeightUnit pUnit, String pNote) {
        return addRecordToFreeWorkout(pDate, pMachine, ExerciseType.ISOMETRIC, pSerie, 0, pPoids, pUnit, pSeconds, 0, DistanceUnit.KM, 0, pNote, pProfileId);
    }

    public long addStaticTemplateToProgram(long pTemplateId, Date pDate, String pExerciseName, int pSets, int pSeconds, float pWeight, WeightUnit pWeightUnit, int restTime, int templateOrder) {
        return addTemplateToProgram(pDate, pExerciseName, ExerciseType.ISOMETRIC, pSets, 0, pWeight,
                pWeightUnit, pSeconds, 0, DistanceUnit.KM, 0, "", pTemplateId, restTime, templateOrder);
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
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + SECONDS
                    + " ORDER BY " + SECONDS + " ASC";
        } else if (pFunction == DAOStatic.MAX_LENGTH) {
            selectQuery = "SELECT MAX(" + SECONDS + ") , " + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == DAOStatic.NBSERIE_FCT) {
            selectQuery = "SELECT count(" + KEY + ") , " + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        } else {
            return null;
        }

        // Formation de tableau de valeur
        List<GraphData> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.rawQuery(selectQuery, null);

        double i = 0;

        // looping through all rows and adding to list
        if (pFunction == DAOStatic.NBSERIE_FCT || pFunction == DAOStatic.MAX_LENGTH) {
            if (mCursor.moveToFirst()) {
                do {

                    Date date = DateConverter.DBDateStrToDate(mCursor.getString(1));
                    GraphData value = new GraphData(DateConverter.nbDays(date), mCursor.getDouble(0));

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
    public int getSets(Date pDate, String pMachine, Profile pProfile) {
        int lReturn = 0;

        //Test is Machine exists. If not create it.
        DAOMachine lDAOMachine = new DAOMachine(mContext);
        long machine_key = lDAOMachine.getMachine(pMachine).getId();

        String lDate = DateConverter.dateToDBDateStr(pDate);

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;

        // Select All Machines
        String selectQuery = "SELECT SUM(" + SETS + ") FROM " + TABLE_NAME
                + " WHERE " + LOCAL_DATE + "=\"" + lDate + "\" AND " + EXERCISE_KEY + "=" + machine_key
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();
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
    public float getTotalWeightMachine(Date pDate, String pMachine, Profile pProfile) {
        if (pProfile == null) return 0;
        float lReturn = 0;

        //Test is Machine exists. If not create it.
        DAOMachine lDAOMachine = new DAOMachine(mContext);
        long machine_key = lDAOMachine.getMachine(pMachine).getId();

        String lDate = DateConverter.dateToDBDateStr(pDate);

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        // Select All Machines
        String selectQuery = "SELECT " + SETS + ", " + WEIGHT + " FROM " + TABLE_NAME
                + " WHERE " + LOCAL_DATE + "=\"" + lDate + "\" AND " + EXERCISE_KEY + "=" + machine_key
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                float value = mCursor.getInt(0) * mCursor.getFloat(1);
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
    public float getTotalWeightSession(Date pDate, Profile pProfile) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        float lReturn = 0;

        String lDate = DateConverter.dateToDBDateStr(pDate);

        // Select All Machines
        String selectQuery = "SELECT " + SETS + ", " + WEIGHT + " FROM " + TABLE_NAME
                + " WHERE " + LOCAL_DATE + "=\"" + lDate + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            int i = 0;
            do {
                float value = mCursor.getInt(0) * mCursor.getFloat(1);
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
        String selectQuery = "SELECT MAX(" + WEIGHT + "), " + WEIGHT_UNIT + " FROM " + TABLE_NAME
                + " WHERE " + PROFILE_KEY + "=" + p.getId() + " AND " + EXERCISE_KEY + "=" + m.getId()
                + " AND ( " + TEMPLATE_RECORD_STATUS + "<" + ProgramRecordStatus.SUCCESS.ordinal()
                + " OR "+ TEMPLATE_RECORD_STATUS + "=" + ProgramRecordStatus.NONE.ordinal() + ")"
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            w = new Weight(mCursor.getFloat(0), WeightUnit.fromInteger(mCursor.getInt(1)));
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
        String selectQuery = "SELECT MIN(" + WEIGHT + "), " + WEIGHT_UNIT + " FROM " + TABLE_NAME
                + " WHERE " + PROFILE_KEY + "=" + p.getId() + " AND " + EXERCISE_KEY + "=" + m.getId()
                + " AND ( " + TEMPLATE_RECORD_STATUS + "<" + ProgramRecordStatus.SUCCESS.ordinal()
                + " OR "+ TEMPLATE_RECORD_STATUS + "=" + ProgramRecordStatus.NONE.ordinal() + ")"
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            w = new Weight(mCursor.getFloat(0), WeightUnit.fromInteger(mCursor.getInt(1)));
        }
        close();

        // return value
        return w;
    }
}
