package com.easyfitness.DAO.record;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.Profile;
import com.easyfitness.R;
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

public class DAOCardio extends DAORecord {

    public static final int DISTANCE_FCT = 0;
    public static final int DURATION_FCT = 1;
    public static final int SPEED_FCT = 2;
    public static final int MAXDURATION_FCT = 3;
    public static final int MAXDISTANCE_FCT = 4;
    public static final int NBSERIE_FCT = 5;

    private static final String OLD_TABLE_NAME = "EFcardio";

    private static final String TABLE_ARCHI = KEY + "," + DATE + "," + EXERCISE + "," + DISTANCE + "," + DURATION + "," + PROFILE_KEY + "," + TIME + "," + DISTANCE_UNIT;

    public DAOCardio(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * @param pDate
     * @param pMachine
     * @param pDistance
     * @param pDuration
     * @param pProfileId
     * @return
     */
    public long addCardioRecordToFreeWorkout(Date pDate, String pMachine, float pDistance, long pDuration, long pProfileId, DistanceUnit pDistanceUnit) {
        return addRecordToFreeWorkout(pDate, pMachine, ExerciseType.CARDIO, 0, 0, 0, WeightUnit.KG, 0, pDistance, pDistanceUnit, pDuration, "", pProfileId);
    }

    public long addCardioTemplateToProgram(long pProgramId, Date pDate, String pExerciseName, float pDistance, DistanceUnit pDistanceUnit, long pDuration, int restTime, int templateOrder) {
        return addTemplateToProgram(pDate, pExerciseName, ExerciseType.CARDIO, 0, 0, 0,
                WeightUnit.KG, 0, pDistance, pDistanceUnit, pDuration, "",
                pProgramId, restTime, templateOrder);
    }

    // Getting Function records
    public List<GraphData> getFunctionRecords(Profile pProfile, String pMachine,
                                              int pFunction) {

        boolean lfilterMachine = true;
        boolean lfilterFunction = true;
        String selectQuery = null;

        if (pMachine == null || pMachine.isEmpty() || pMachine.equals(mContext.getResources().getText(R.string.all).toString())) {
            lfilterMachine = false;
        }

        if (pFunction == DAOCardio.DISTANCE_FCT) {
            selectQuery = "SELECT SUM(" + DISTANCE + "), " + LOCAL_DATE + " FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == DAOCardio.DURATION_FCT) {
            selectQuery = "SELECT SUM(" + DURATION + ") , " + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == DAOCardio.SPEED_FCT) {
            selectQuery = "SELECT SUM(" + DISTANCE + ") / SUM(" + DURATION + ")," + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        } else if (pFunction == DAOCardio.MAXDISTANCE_FCT) {
            selectQuery = "SELECT MAX(" + DISTANCE + ") , " + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
        }
        // case "MEAN" : selectQuery = "SELECT SUM("+ SERIE + "*" + REPETITION +
        // "*" + WEIGHT +") FROM " + TABLE_NAME + " WHERE " + EXERCISE + "=\"" +
        // pMachine + "\" AND " + DATE + "=\"" + pDate + "\" ORDER BY " + KEY +
        // " DESC";
        // break;

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

                GraphData value = new GraphData(DateConverter.nbDays(date),
                        mCursor.getDouble(0));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }

        // return value list
        return valueList;
    }
}
