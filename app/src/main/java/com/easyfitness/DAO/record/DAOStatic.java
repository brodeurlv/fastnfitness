package com.easyfitness.DAO.record;

import android.content.Context;
import android.database.Cursor;
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

import org.jetbrains.annotations.Nullable;

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
    @Nullable
    public List<GraphData> getStaticFunctionRecords(Profile pProfile, String pMachine,
                                                    int pFunction) {

        String selectQuery = null;
        String[] selectionArgs = null;

        // TODO attention aux units de poids. Elles ne sont pas encore prise en compte ici.
        if (pFunction == DAOStatic.MAX_FCT) {
            selectQuery = "SELECT MAX(" + WEIGHT + ") , " + SECONDS + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "= ?"
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + SECONDS
                    + " ORDER BY " + SECONDS + " ASC";
            selectionArgs = new String[]{pMachine};
        } else if (pFunction == DAOStatic.MAX_LENGTH) {
            selectQuery = "SELECT MAX(" + SECONDS + ") , " + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "= ?"
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
            selectionArgs = new String[]{pMachine};
        } else if (pFunction == DAOStatic.NBSERIE_FCT) {
            selectQuery = "SELECT count(" + KEY + ") , " + LOCAL_DATE + " FROM "
                    + TABLE_NAME
                    + " WHERE " + EXERCISE + "= ?"
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " GROUP BY " + LOCAL_DATE
                    + " ORDER BY " + DATE_TIME + " ASC";
            selectionArgs = new String[]{pMachine};
        } else {
            return null;
        }

        // Formation de tableau de valeur
        List<GraphData> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(selectQuery, selectionArgs)) {
            if (cursor != null) {
                if (pFunction == DAOStatic.NBSERIE_FCT || pFunction == DAOStatic.MAX_LENGTH) {
                    if (cursor.moveToFirst()) {
                        do {
                            Date date = DateConverter.DBDateStrToDate(cursor.getString(1));
                            GraphData value = new GraphData(DateConverter.nbDays(date), cursor.getDouble(0));
                            valueList.add(value);
                        } while (cursor.moveToNext());
                    }
                } else if (pFunction == DAOStatic.MAX_FCT) {
                    if (cursor.moveToFirst()) {
                        do {
                            GraphData value = new GraphData(cursor.getDouble(1), cursor.getDouble(0));
                            valueList.add(value);
                        } while (cursor.moveToNext());
                    }
                }
            }
        } finally {
            close();
        }

        // return value list
        return valueList;
    }

    /**
     * @return the number of series for this machine for this day
     */
    public int getSets(Date pDate, String pMachine, Profile pProfile) {
        if (pProfile == null) return 0;
        int lReturn = 0;

        //Test is Machine exists. If not create it.
        DAOMachine lDAOMachine = new DAOMachine(mContext);
        long machine_key = lDAOMachine.getMachine(pMachine).getId();

        String lDate = DateConverter.dateToDBDateStr(pDate);

        SQLiteDatabase db = this.getReadableDatabase();

        // Select All Machines
        String selectQuery = "SELECT SUM(" + SETS + ") FROM " + TABLE_NAME
                + " WHERE " + LOCAL_DATE + " = ?"
                + " AND " + EXERCISE_KEY + " = " + machine_key
                + " AND " + PROFILE_KEY + " = " + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();

        try (Cursor cursor = db.rawQuery(selectQuery, new String[]{lDate})) {
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    lReturn = cursor.getInt(0);
                } catch (NumberFormatException e) {
                    lReturn = 0;
                }
            }
        } finally {
            close();
        }

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

        // Select All Machines
        String selectQuery = "SELECT " + SETS + ", " + WEIGHT + " FROM " + TABLE_NAME
                + " WHERE " + LOCAL_DATE + "= ?"
                + " AND " + EXERCISE_KEY + "=" + machine_key
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();

        try (Cursor cursor = db.rawQuery(selectQuery, new String[]{lDate})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    float value = cursor.getInt(0) * cursor.getFloat(1);
                    lReturn += value;
                } while (cursor.moveToNext());
            }
        } finally {
            close();
        }

        // return value
        return lReturn;
    }


    /**
     * @return the total weight for this day
     */
    public float getTotalWeightSession(Date pDate, Profile pProfile) {
        if (pProfile == null) return 0;
        float lReturn = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        String lDate = DateConverter.dateToDBDateStr(pDate);

        // Select All Machines
        String selectQuery = "SELECT " + SETS + ", " + WEIGHT + " FROM " + TABLE_NAME
                + " WHERE " + LOCAL_DATE + "= ?"
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();

        try (Cursor cursor = db.rawQuery(selectQuery, new String[]{lDate})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    float value = cursor.getInt(0) * cursor.getFloat(1);
                    lReturn += value;
                } while (cursor.moveToNext());
            }
        } finally {
            close();
        }

        // return value
        return lReturn;
    }

    /**
     * @return Max weight for a profile p and a machine m
     */
    public Weight getMax(Profile p, Machine m) {
        if (p == null || m == null) return null;

        SQLiteDatabase db = this.getReadableDatabase();
        Weight w = null;

        // Select All Machines
        String selectQuery = "SELECT MAX(" + WEIGHT + "), " + WEIGHT_UNIT + " FROM " + TABLE_NAME
                + " WHERE " + PROFILE_KEY + "=" + p.getId()
                + " AND " + EXERCISE_KEY + "=" + m.getId()
                + " AND ( " + TEMPLATE_RECORD_STATUS + "<" + ProgramRecordStatus.SUCCESS.ordinal()
                + " OR "+ TEMPLATE_RECORD_STATUS + "=" + ProgramRecordStatus.NONE.ordinal() + ")"
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();

        try (Cursor cursor = db.rawQuery(selectQuery, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                w = new Weight(cursor.getFloat(0), WeightUnit.fromInteger(cursor.getInt(1)));
            }
        } finally {
            close();
        }

        // return value
        return w;
    }

    /**
     * @return Min weight for a profile p and a machine m
     */
    public Weight getMin(Profile p, Machine m) {
        if (p == null || m == null) return null;

        SQLiteDatabase db = this.getReadableDatabase();
        Weight w = null;

        // Select All Machines
        String selectQuery = "SELECT MIN(" + WEIGHT + "), " + WEIGHT_UNIT + " FROM " + TABLE_NAME
                + " WHERE " + PROFILE_KEY + "=" + p.getId()
                + " AND " + EXERCISE_KEY + "=" + m.getId()
                + " AND ( " + TEMPLATE_RECORD_STATUS + "<" + ProgramRecordStatus.SUCCESS.ordinal()
                + " OR "+ TEMPLATE_RECORD_STATUS + "=" + ProgramRecordStatus.NONE.ordinal() + ")"
                + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal();

        try (Cursor cursor = db.rawQuery(selectQuery, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                w = new Weight(cursor.getFloat(0), WeightUnit.fromInteger(cursor.getInt(1)));
            }
        } finally {
            close();
        }

        // return value
        return w;
    }
}
