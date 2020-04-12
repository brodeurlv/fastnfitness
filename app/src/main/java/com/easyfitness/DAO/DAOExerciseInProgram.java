package com.easyfitness.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Date;

public class DAOExerciseInProgram extends DAOBase {

    // Contacts table name
    public static final String TABLE_NAME = "EFExerciseInProgram";
    public static final String KEY = "_id";
    public static final String DATE = "date";
    private static final String TIME = "time";

    public static final String EXERCISE = "machine";
    public static final String PROFIL_KEY = "profil_id";
    public static final String MACHINE_KEY = "machine_id";
    public static final String NOTES = "notes";
    public static final String TYPE = "type";

    // Specific to Strength
    public static final String SERIE = "serie";
    public static final String REPETITION = "repetition";
    public static final String WEIGHT = "poids";
    public static final String UNIT = "unit"; // 0:kg 1:lbs

    // Specific to Cardio
    public static final String DISTANCE = "distance";
    public static final String DURATION = "duration";
    public static final String DISTANCE_UNIT = "distance_unit"; // 0:km 1:mi

    // Specific to STATIC
    public static final String SECONDS = "seconds";
    //rest between exercises
    private static final String REST_SECONDS = "rest_seconds";
    private static final String PROGRAM_ID = "program_id";
    private static final String ORDER_EXECUTION = "order_in_program";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
        + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + EXERCISE + " TEXT, "
        + REST_SECONDS + " INTEGER, " + SERIE + " INTEGER, "
        + REPETITION + " INTEGER, " + WEIGHT + " REAL, " + PROFIL_KEY
        + " INTEGER, " + UNIT + " INTEGER, " + NOTES + " TEXT, " + MACHINE_KEY
        + " INTEGER," + TIME + " TEXT," + DISTANCE + " REAL, " + DURATION + " TEXT, "
        + TYPE + " INTEGER, " + SECONDS + " INTEGER, " + DISTANCE_UNIT + " INTEGER, "
        + PROGRAM_ID + " INTEGER, " + ORDER_EXECUTION + " INTEGER);";

//    private String allFieldsExceptProgramId = REST_SECONDS + "," + EXERCISE + "," + SERIE + ","
//        + REPETITION + "," + WEIGHT + "," + WEIGHT + "," + PROFIL_KEY + ","
//        + UNIT + "," + NOTES + "," + MACHINE_KEY + "," + TIME + "," + DISTANCE + "," +
//        DURATION + "," + TYPE + "," + SECONDS + "," + DISTANCE_UNIT+ ","+ORDER_EXECUTION;
//    public static final String TABLE_DROP = "DROP TABLE IF EXISTS "
//        + TABLE_NAME + ";";

    protected Profile mProfile = null;
    protected Cursor mCursor = null;
    protected Context mContext;

    public DAOExerciseInProgram(Context context) {
        super(context);
        mContext = context;
    }

    public void setProfile(Profile pProfile) {
        mProfile = pProfile;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public int getCount() {
        String countQuery = "SELECT " + KEY + " FROM " + TABLE_NAME;
        open();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int value = cursor.getCount();
        cursor.close();
        close();
        return value;
    }

    /**
     * @param pMachine Machine name
     * @return id of the added record, -1 if error
     */
    public long addRecord(long order, long programId, int restSeconds, String pMachine, int pType, int pSerie, int pRepetition, float pPoids, Profile pProfile, int pUnit, String pNote,
                          String pTime, float pDistance, long pDuration, int pSeconds, int distance_unit) {
        ContentValues value = new ContentValues();
        long new_id = -1;
        long machine_key = -1;

        DAOExerciseInProgram daoExerciseInProgram = new DAOExerciseInProgram(mContext);
        if (daoExerciseInProgram.exerciseExists(pMachine)) {
            return new_id;
        }

        value.put(DAOExerciseInProgram.PROGRAM_ID, programId);
        value.put(DAOExerciseInProgram.REST_SECONDS, restSeconds);
        value.put(DAOExerciseInProgram.EXERCISE, pMachine);
        value.put(DAOExerciseInProgram.SERIE, pSerie);
        value.put(DAOExerciseInProgram.REPETITION, pRepetition);
        value.put(DAOExerciseInProgram.WEIGHT, pPoids);
        value.put(DAOExerciseInProgram.PROFIL_KEY, pProfile.getId());
        value.put(DAOExerciseInProgram.UNIT, pUnit);
        value.put(DAOExerciseInProgram.NOTES, pNote);
        value.put(DAOExerciseInProgram.MACHINE_KEY, machine_key);
        value.put(DAOExerciseInProgram.TIME, pTime);
        value.put(DAOExerciseInProgram.DISTANCE, pDistance);
        value.put(DAOExerciseInProgram.DURATION, pDuration);
        value.put(DAOExerciseInProgram.TYPE, pType);
        value.put(DAOExerciseInProgram.SECONDS, pSeconds);
        value.put(DAOExerciseInProgram.DISTANCE_UNIT, distance_unit);
        value.put(DAOExerciseInProgram.ORDER_EXECUTION, order);
        SQLiteDatabase db = open();
        new_id = db.insert(DAOExerciseInProgram.TABLE_NAME, null, value);
        close();
        return new_id;
    }

    private boolean exerciseExists(String name) {
        ExerciseInProgram lMach = getRecord(name);
        return lMach != null;
    }

    private ExerciseInProgram getRecord(String pName) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.query(TABLE_NAME, new String[]{EXERCISE}, EXERCISE + "=?",
            new String[]{pName}, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        if (mCursor.getCount() == 0)
            return null;
        DAOProfil lDAOProfil = new DAOProfil(mContext);
        Profile lProfile = lDAOProfil.getProfil(mCursor.getLong(mCursor.getColumnIndex(DAOFonte.PROFIL_KEY)));
        ExerciseInProgram value = new ExerciseInProgram(
            mCursor.getInt(mCursor.getColumnIndex(REST_SECONDS)),
            mCursor.getString(mCursor.getColumnIndex(EXERCISE)),
            mCursor.getInt(mCursor.getColumnIndex(SERIE)),
            mCursor.getInt(mCursor.getColumnIndex(REPETITION)),
            mCursor.getFloat(mCursor.getColumnIndex(WEIGHT)),
            lProfile,
            mCursor.getInt(mCursor.getColumnIndex(UNIT)),
            mCursor.getString(mCursor.getColumnIndex(NOTES)),
            mCursor.getInt(mCursor.getColumnIndex(MACHINE_KEY)),
            mCursor.getString(mCursor.getColumnIndex(TIME))
        );

        value.setId(mCursor.getLong(0));
        // return value
        mCursor.close();
        close();
        return value;
    }

    public void deleteRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public IRecord getRecord(long id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY + "=" + id;

        mCursor = getRecordsListCursor(selectQuery);
        if (mCursor.moveToFirst()) {
            //Get Profile
            DAOProfil lDAOProfil = new DAOProfil(mContext);
            Profile lProfile = lDAOProfil.getProfil(mCursor.getLong(mCursor.getColumnIndex(DAOExerciseInProgram.PROFIL_KEY)));

            long machine_key = -1;

            //Test is Machine exists. If not create it.
            DAOMachine lDAOMachine = new DAOMachine(mContext);
            if (mCursor.getString(mCursor.getColumnIndex(DAOExerciseInProgram.MACHINE_KEY)) == null) {
                machine_key = lDAOMachine.addMachine(mCursor.getString(mCursor.getColumnIndex(DAOExerciseInProgram.EXERCISE)), "", DAOMachine.TYPE_FONTE, "", false, "");
            } else {
                machine_key = mCursor.getLong(mCursor.getColumnIndex(DAOExerciseInProgram.MACHINE_KEY));
            }

            IRecord value = null;

            if (mCursor.getInt(mCursor.getColumnIndex(DAOExerciseInProgram.TYPE)) == DAOMachine.TYPE_FONTE) {
                value = new ExerciseInProgram(mCursor.getColumnIndex(DAOExerciseInProgram.SECONDS),
                    mCursor.getString(mCursor.getColumnIndex(DAOExerciseInProgram.EXERCISE)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOExerciseInProgram.SERIE)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOExerciseInProgram.REPETITION)),
                    mCursor.getFloat(mCursor.getColumnIndex(DAOExerciseInProgram.WEIGHT)),
                    lProfile,
                    mCursor.getInt(mCursor.getColumnIndex(DAOExerciseInProgram.UNIT)),
                    mCursor.getString(mCursor.getColumnIndex(DAOExerciseInProgram.NOTES)),
                    machine_key,
                    mCursor.getString(mCursor.getColumnIndex(DAOExerciseInProgram.TIME)));
            } else if (mCursor.getInt(mCursor.getColumnIndex(DAOExerciseInProgram.TYPE)) == DAOMachine.TYPE_STATIC) {
                value = new StaticExercise(new Date(),
                    mCursor.getString(mCursor.getColumnIndex(DAOExerciseInProgram.EXERCISE)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOExerciseInProgram.SERIE)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOExerciseInProgram.SECONDS)),
                    mCursor.getFloat(mCursor.getColumnIndex(DAOExerciseInProgram.WEIGHT)),
                    lProfile,
                    mCursor.getInt(mCursor.getColumnIndex(DAOExerciseInProgram.UNIT)),
                    machine_key,
                    mCursor.getString(mCursor.getColumnIndex(DAOExerciseInProgram.TIME)));
            } else {
                value = new Cardio(new Date(),
                    mCursor.getString(mCursor.getColumnIndex(DAOExerciseInProgram.EXERCISE)),
                    mCursor.getFloat(mCursor.getColumnIndex(DAOExerciseInProgram.DISTANCE)),
                    mCursor.getLong(mCursor.getColumnIndex(DAOExerciseInProgram.DURATION)),
                    lProfile,
                    mCursor.getString(mCursor.getColumnIndex(DAOExerciseInProgram.TIME)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOExerciseInProgram.DISTANCE_UNIT)));
            }

            value.setId(mCursor.getLong(mCursor.getColumnIndex(DAOExerciseInProgram.KEY)));
            return value;
        } else {
            return null;
        }
    }

    // Getting All Records
    private Cursor getRecordsListCursor(String pRequest) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(pRequest, null);
    }

    // Getting All Machines
    public String[] getAllMachines(Profile pProfile) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;

        // Select All Machines
        String selectQuery = "SELECT DISTINCT " + EXERCISE + " FROM "
            + TABLE_NAME + "  WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY " + KEY + " ASC";
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

    public Cursor getTop3DatesRecords(Profile pProfile) {
        if (pProfile == null)
            return null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME
            + " WHERE " + PROFIL_KEY + "=" + pProfile.getId()
            + " AND " + KEY + " IN (SELECT DISTINCT " + KEY + " FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY " + KEY + " ASC LIMIT 10)"
            + " ORDER BY " + KEY + " ASC";
        return getRecordsListCursor(selectQuery);
    }

    /**
     * @return the last record for a profile p
     */
    public IRecord getLastRecord(Profile pProfile) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        IRecord lReturn = null;
        String selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
            + " WHERE " + PROFIL_KEY + "=" + pProfile.getId();
        mCursor = db.rawQuery(selectQuery, null);
        if (mCursor.moveToFirst()) {
            try {
                long value = mCursor.getLong(0);
                lReturn = getRecord(value);
            } catch (NumberFormatException e) {
                lReturn = null; // Return une valeur
            }
        }
        close();
        return lReturn;
    }

    public void closeCursor() {
        if (mCursor != null) mCursor.close();
    }

    public ArrayList<ARecord> getAllExerciseInProgramArray() {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + ";";//+ " ORDER BY "
        // + KEY + " ASC;";
        return getExerciseList(selectQuery);
    }

    public ArrayList<ARecord> getAllExerciseInProgram(Long programId) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + PROGRAM_ID + " = " + programId
            + " ORDER BY " + KEY + " DESC;";
        return getExerciseList(selectQuery);
    }

    private ArrayList<ARecord> getExerciseList(String pRequest) {
        ArrayList<ARecord> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        try {
            mCursor = db.rawQuery(pRequest, null);
        } catch (Exception ex) {
            Log.e("Err MU", "EX " + ex);
            Toast.makeText(mContext, "Ex" + ex, Toast.LENGTH_LONG).show();
        }
        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                ExerciseInProgram value = new ExerciseInProgram(
                    mCursor.getInt(mCursor.getColumnIndex(REST_SECONDS)),
                    mCursor.getString(mCursor.getColumnIndex(EXERCISE)),
                    mCursor.getInt(mCursor.getColumnIndex(SERIE)),
                    mCursor.getInt(mCursor.getColumnIndex(REPETITION)),
                    mCursor.getInt(mCursor.getColumnIndex(WEIGHT)),
                    mProfile,
                    mCursor.getInt(mCursor.getColumnIndex(UNIT)),
                    mCursor.getString(mCursor.getColumnIndex(NOTES)),
                    mCursor.getInt(mCursor.getColumnIndex(MACHINE_KEY)),
                    mCursor.getString(mCursor.getColumnIndex(TIME))
                );

                value.setId(mCursor.getLong(mCursor.getColumnIndex(KEY)));
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        return valueList;
    }
}
