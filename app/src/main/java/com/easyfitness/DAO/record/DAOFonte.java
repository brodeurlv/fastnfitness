package com.easyfitness.DAO.record;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.Weight;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.RecordType;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.graph.GraphData;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.enums.ProgramRecordStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAOFonte extends DAORecord {

    public static final int SUM_FCT = 0;
    public static final int MAX1_FCT = 1;
    public static final int MAX5_FCT = 2;
    public static final int NBSERIE_FCT = 3;
    public static final int TOTAL_REP_FCT = 4;
    public static final int MAX_REP_FCT = 5;
    public static final int ONEREPMAX_FCT = 6;

    private static final String TABLE_ARCHI = KEY + "," + DATE + "," + EXERCISE + "," + SETS + "," + REPS + "," + WEIGHT + "," + WEIGHT_UNIT + "," + PROFILE_KEY + "," + NOTES + "," + EXERCISE_KEY + "," + TIME;

    public DAOFonte(Context context) {
        super(context);
    }

    /**
     * @param pDate       Date
     * @param pExercise   Machine name
     * @param pWeightUnit
     * @param pProfileId
     */
    public long addStrengthRecordToFreeWorkout(Date pDate, String pExercise, int pSets, int pReps, float pWeight, WeightUnit pWeightUnit, String pNote, long pProfileId) {
        return addRecordToFreeWorkout(pDate, pExercise, ExerciseType.STRENGTH, pSets, pReps, pWeight, pWeightUnit, 0, 0, DistanceUnit.KM, 0, pNote, pProfileId);
    }

    public long addStrengthTemplateToProgram(long pProgramId, Date pDate, String pExerciseName, int pSets, int pReps, float pWeight, WeightUnit pWeightUnit, int restTime, int templateOrder) {
        return addTemplateToProgram(pDate, pExerciseName, ExerciseType.STRENGTH, pSets, pReps, pWeight,
                pWeightUnit, 0 , 0, DistanceUnit.KM, 0, "", pProgramId,
                restTime, templateOrder);
    }

    /**
     * @param fonteList List of Fonte records
     */
    public void addBodyBuildingList(List<Record> fonteList) {
        addList(fonteList);
    }

    // Getting Function records
    public List<GraphData> getBodyBuildingFunctionRecords(Profile pProfile, String pMachine,
                                                          int pFunction) {

        String selectQuery = null;

        // TODO Weight unit are not considered here yet.
        if (pFunction == DAOFonte.SUM_FCT) {
            selectQuery = "SELECT SUM(" + SETS + "*" + REPS + "*"
                    + WEIGHT + "), " + LOCAL_DATE + " FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == DAOFonte.MAX5_FCT) {
            selectQuery = "SELECT MAX(" + WEIGHT + ") , " + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + REPS + ">=5"
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == DAOFonte.MAX1_FCT) {
            selectQuery = "SELECT MAX(" + WEIGHT + ") , " + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + REPS + ">=1"
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == DAOFonte.NBSERIE_FCT) {
            selectQuery = "SELECT count(" + KEY + ") , " + LOCAL_DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                + " GROUP BY " + LOCAL_DATE
                + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == DAOFonte.ONEREPMAX_FCT) {
            //https://en.wikipedia.org/wiki/One-repetition_maximum#Brzycki
            selectQuery = "SELECT MAX(" + WEIGHT + " * (36.0 / (37.0 - " + REPS + "))) , " + LOCAL_DATE + " FROM "
                + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                + " AND " + REPS + "<=10"
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                + " GROUP BY " + LOCAL_DATE
                + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == DAOFonte.TOTAL_REP_FCT) {
            selectQuery = "SELECT SUM(" + SETS + "*" + REPS + "), " + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == DAOFonte.MAX_REP_FCT) {
            selectQuery = "SELECT MAX(" + REPS + ") , " + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
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
                Date date = DateConverter.DBDateStrToDate(mCursor.getString(1));

                GraphData value = new GraphData(DateConverter.nbDays(date), mCursor.getDouble(0));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }

        // return value list
        return valueList;
    }

    /**
     * @return the number of sets for this machine for this day
     */
    public int getSets(Date pDate, String pMachine, Profile pProfile) {

        if (pProfile == null) return 0;
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
        String selectQuery = "SELECT " + SETS + ", " + WEIGHT + ", " + REPS + " FROM " + TABLE_NAME
                + " WHERE " + LOCAL_DATE + "=\"" + lDate + "\" AND " + EXERCISE_KEY + "=" + machine_key
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();
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
    public float getTotalWeightSession(Date pDate, Profile pProfile) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        float lReturn = 0;

        String lDate = DateConverter.dateToDBDateStr(pDate);

        // Select All Machines
        String selectQuery = "SELECT " + SETS + ", " + WEIGHT + ", " + REPS + " FROM " + TABLE_NAME
                + " WHERE " + LOCAL_DATE + "=\"" + lDate + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "<" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();
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
        String selectQuery = "SELECT MAX(" + WEIGHT + "), " + WEIGHT_UNIT + " FROM " + TABLE_NAME
                + " WHERE " + PROFILE_KEY + "=" + p.getId() + " AND " + EXERCISE_KEY + "=" + m.getId()
                + " AND ( " + TEMPLATE_RECORD_STATUS + "<" + ProgramRecordStatus.SUCCESS.ordinal()
                + " OR "+ TEMPLATE_RECORD_STATUS + "=" + ProgramRecordStatus.NONE.ordinal() + ")"
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();
        mCursor = db.rawQuery(selectQuery, null);

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

        if (mCursor.moveToFirst()) {
           w = new Weight(mCursor.getFloat(0), WeightUnit.fromInteger(mCursor.getInt(1)));
        }
        close();

        // return value
        return w;
    }

    public void populate() {
        // DBORecord(long id, Date pDate, String pMachine, int pSerie, int
        // pRepetition, int pPoids)
        Date date = DateConverter.timeToDate(12, 34, 56);
        int poids = 10;

        for (int i = 1; i <= 5; i++) {
            String machine = "Biceps";
            date.setDate(date.getDay() + i * 10);
            addStrengthRecordToFreeWorkout(date, machine, i * 2, 10 + i, poids * i, WeightUnit.KG, "", mProfile.getId());
        }

        date = DateConverter.timeToDate(12, 34, 56);
        poids = 12;

        for (int i = 1; i <= 5; i++) {
            String machine = "Dev Couche";
            date.setDate(date.getDay() + i * 10);
            addStrengthRecordToFreeWorkout(date, machine, i * 2, 10 + i, poids * i, WeightUnit.KG, "", mProfile.getId());
        }
    }

}
