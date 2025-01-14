package com.easyfitness.DAO.macros;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DAOBase;
import com.easyfitness.DAO.Profile;
import com.easyfitness.R;
import com.easyfitness.enums.RecordType;
import com.easyfitness.utils.DateConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAOFoodRecord extends DAOBase {

    // Contacts table name
    public static final String TABLE_NAME = "EFnourriture";

    public static final String KEY = "_id";
    public static final String DATE = "date";
    public static final String LOCAL_DATE = "DATE(date || 'T' || time, 'localtime')";
    public static final String TIME = "time";
    public static final String DATE_TIME = "DATETIME(date || 'T' || time)";
    public static final String FOOD = "food";
    public static final String PROFILE_KEY = "profil_id";
    public static final String FOOD_KEY = "food_id";
    public static final String NOTES = "notes";
    public static final String PROTEIN = "protein";
    public static final String FATS = "fats";
    public static final String CARBS = "carbs";
    public static final String CALORIES = "calories";
    public static final String QUANTITY = "quantity";
    public static final String QUANTITY_UNIT = "unit";


    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
            + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PROFILE_KEY + " INTEGER, "
            + FOOD_KEY + " INTEGER,"
            + DATE + " DATE, "
            + TIME + " TEXT,"
            + FOOD + " TEXT, "
            + CALORIES + " REAL, "
            + CARBS + " REAL, "
            + PROTEIN + " REAL, "
            + FATS + " REAL, "
            + QUANTITY + " REAL, "
            + QUANTITY_UNIT + " TEXT, "
            + NOTES + " TEXT, "
            + " );";

    protected Profile mProfile = null;
    protected Cursor mCursor = null;
    protected Context mContext;

    public DAOFoodRecord(Context context) {
        super(context);
        mContext = context;
    }

    public void setProfile(Profile pProfile) {
        mProfile = pProfile;
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

    public long addRecord(FoodRecord record) {
        return addRecord(record.getDate(),
                record.getExercise(), record.getExerciseType(),
                record.getSets(),
                record.getReps(),
                record.getWeightInKg(),
                record.getWeightUnit(),
                record.getNote(),
                record.getDistanceInKm(),
                record.getDistanceUnit(),
                record.getDuration(),
                record.getSeconds(),
                record.getProfileId(), record.getRecordType(),
                record.getTemplateRecordId(), record.getProgramId(), record.getTemplateSessionId(), record.getTemplateRestTime(), record.getProgramRecordStatus(),
                record.getTemplateSets(),
                record.getTemplateReps(),
                record.getTemplateWeight(),
                record.getTemplateWeightUnit(),
                record.getTemplateDistance(),
                record.getTemplateDistanceUnit(),
                record.getTemplateDuration(),
                record.getTemplateSeconds(),
                record.getTemplateOrder());
    }

    /**
     * @param pDate              Date
     * @param pExercise          Machine name
     * @param pExerciseType      Weight, Cardio or Isometric
     * @param pWeightUnit        LBS or KG
     * @param pProfileId         profile who created the record
     * @param pTemplateRecordId  record of type PROGRAM_TEMPLATE
     * @param pProgramSessionId Id of the Program session
     * @param templateOrder
     * @return id of the added record, -1 if error
     */
    public long addRecord(Date pDate, String pExercise, ExerciseType pExerciseType, int pSets, int pReps, float pWeight,
                          WeightUnit pWeightUnit, String pNote, float pDistance, DistanceUnit pDistanceUnit, long pDuration, int pSeconds, long pProfileId,
                          RecordType pRecordType, long pTemplateRecordId, long pProgramId, long pProgramSessionId,
                          int pRestTime, ProgramRecordStatus pProgramRecordStatus, int templateSets, int templateReps, float templateWeight,
                          WeightUnit templateWeightUnit, float templateDistance, DistanceUnit templateDistanceUnit, long templateDuration, int templateSeconds, int templateOrder) {

        ContentValues value = new ContentValues();
        long machine_key;

        //Test if Machine exists. If not create it.
        DAOMachine lDAOMachine = new DAOMachine(mContext);
        if (!lDAOMachine.machineExists(pExercise)) {
            machine_key = lDAOMachine.addMachine(pExercise, "", pExerciseType, "", false, "");
        } else {
            machine_key = lDAOMachine.getMachine(pExercise).getId();
        }

        /// TODO: Finish implementing the below block
        value.put(DAOFoodRecord.DATE, DateConverter.dateTimeToDBDateStr(pDate));
        value.put(DAOFoodRecord.TIME, DateConverter.dateTimeToDBTimeStr(pDate));
        value.put(DAOFoodRecord.PROFILE_KEY, pProfileId);
        value.put(DAOFoodRecord.QUANTITY, pExercise);
        value.put(DAOFoodRecord.QUANTITY_UNIT, machine_key);
        value.put(DAOFoodRecord.CALORIES, pExerciseType.ordinal());
        value.put(DAOFoodRecord.PROTEIN, pSets);
        value.put(DAOFoodRecord.CARBS, pReps);
        value.put(DAOFoodRecord.FATS, pWeight);
        value.put(DAOFoodRecord.NOTES, pNote);

        SQLiteDatabase db = open();
        long new_id = db.insert(DAOFoodRecord.TABLE_NAME, null, value);
        close();

        return new_id;
    }

    public void addList(List<FoodRecord> list) {
        for (FoodRecord record : list) {
            addRecord(record);
        }
    }

    // Deleting single Record
    public int deleteRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int ret = db.delete(TABLE_NAME, KEY + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return ret;
    }

    // Getting single value
    public FoodRecord getRecord(SQLiteDatabase db, long id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY + "=" + id;

        mCursor = getRecordsListCursor(db, selectQuery);
        if (mCursor.moveToFirst()) {
            //Get Date
            return fromCursor(mCursor);
        } else {
            return null;
        }
    }

    public FoodRecord getRecord(long id) {
       return getRecord(this.getReadableDatabase(), id);
    }

    private FoodRecord fromCursor(Cursor cursor) {
        Date date = DateConverter.DBDateTimeStrToDate(
                cursor.getString(cursor.getColumnIndexOrThrow(DAOFonte.DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DAOFonte.TIME))
        );

        long machine_key;

        //Test if Machine exists. If not create it.
        DAOMachine lDAOMachine = new DAOMachine(mContext);
        if (cursor.getString(cursor.getColumnIndexOrThrow(DAOFonte.EXERCISE_KEY)) == null) {
            machine_key = lDAOMachine.addMachine(cursor.getString(cursor.getColumnIndexOrThrow(DAOFonte.EXERCISE)), "", ExerciseType.STRENGTH, "", false, "");
        } else {
            machine_key = cursor.getLong(cursor.getColumnIndexOrThrow(DAOFonte.EXERCISE_KEY));
        }

        FoodRecord value = new FoodRecord(date,
                cursor.getString(cursor.getColumnIndexOrThrow(DAOFoodRecord.EXERCISE)),
                machine_key,
                cursor.getLong(cursor.getColumnIndexOrThrow(DAOFoodRecord.PROFILE_KEY)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.SETS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.REPS)),
                cursor.getFloat(cursor.getColumnIndexOrThrow(DAOFoodRecord.WEIGHT)),
                WeightUnit.fromInteger(cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.WEIGHT_UNIT))),
                cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.SECONDS)),
                cursor.getFloat(cursor.getColumnIndexOrThrow(DAOFoodRecord.DISTANCE)),
                DistanceUnit.fromInteger(cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.DISTANCE_UNIT))),
                cursor.getLong(cursor.getColumnIndexOrThrow(DAOFoodRecord.DURATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(DAOFoodRecord.NOTES)),
                ExerciseType.fromInteger(cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.EXERCISE_TYPE))),
                cursor.getLong(cursor.getColumnIndexOrThrow(DAOFoodRecord.PROGRAM_KEY)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DAOFoodRecord.TEMPLATE_RECORD_KEY)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DAOFoodRecord.PROGRAM_SESSION_KEY)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.TEMPLATE_REST_TIME)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.TEMPLATE_ORDER)),
                ProgramRecordStatus.fromInteger(cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.TEMPLATE_RECORD_STATUS))),
                RecordType.fromInteger(cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.RECORD_TYPE))),
                cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.TEMPLATE_SETS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.TEMPLATE_REPS)),
                cursor.getFloat(cursor.getColumnIndexOrThrow(DAOFoodRecord.TEMPLATE_WEIGHT)),
                WeightUnit.fromInteger(cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.TEMPLATE_WEIGHT_UNIT))),
                cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.TEMPLATE_SECONDS)),
                cursor.getFloat(cursor.getColumnIndexOrThrow(DAOFoodRecord.TEMPLATE_DISTANCE)),
                DistanceUnit.fromInteger(cursor.getInt(cursor.getColumnIndexOrThrow(DAOFoodRecord.TEMPLATE_DISTANCE_UNIT))),
                cursor.getLong(cursor.getColumnIndexOrThrow(DAOFoodRecord.TEMPLATE_DURATION)));

        value.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DAOFoodRecord.KEY)));
        return value;
    }

    public List<FoodRecord> fromCursorToList(Cursor cursor) {
        List<FoodRecord> valueList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                FoodRecord record = fromCursor(cursor);
                if (record != null) valueList.add(record);
            } while (cursor.moveToNext());
        }
        return valueList;
    }

    public List<FoodRecord> getAllRecords() {
        return getAllRecords(this.getReadableDatabase());
    }

    // Getting All Records
    public List<FoodRecord> getAllRecords(SQLiteDatabase sqLiteDatabase) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " ORDER BY " + KEY + " DESC";

        // return value list
        return getRecordsList(selectQuery, sqLiteDatabase);
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
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC" + mTop;

        // return value list
        return getRecordsListCursor(selectQuery);
    }

    // Getting All Records
    public Cursor getAllRecordsByProfile(Profile pProfile) {
        return getAllRecordsByProfile(pProfile, -1);
    }

    // Getting All Records
    public List<FoodRecord> getAllRecordsByProfileList(Profile pProfile) {
        Cursor cursor = getAllRecordsByProfile(pProfile, -1);
        return fromCursorToList(cursor);
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
                " WHERE " + PROFILE_KEY + "=" + pProfile.getId() +
                " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC" + mTop;

        // Return value list
        return getRecordsListCursor(selectQuery);
    }

    // Getting All Records
    private Cursor getRecordsListCursor(SQLiteDatabase db, String pRequest) {
        return db.rawQuery(pRequest, null);
    }

    private Cursor getRecordsListCursor(String pRequest) {
        return getRecordsListCursor(this.getReadableDatabase(), pRequest);
    }

    // Getting All Machines
    public List<String> getAllMachinesStrList() {
        return getAllMachinesStrList(null);
    }

    // Getting All Machines
    public List<String> getAllMachinesStrList(Profile pProfile) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        String selectQuery;
        if (pProfile == null) {
            selectQuery = "SELECT DISTINCT " + EXERCISE + " FROM "
                    + TABLE_NAME + " ORDER BY " + EXERCISE + " ASC";
        } else {
            selectQuery = "SELECT DISTINCT " + EXERCISE + " FROM "
                    + TABLE_NAME + "  WHERE " + PROFILE_KEY + "=" + pProfile.getId() + " ORDER BY " + EXERCISE + " ASC";
        }
        mCursor = db.rawQuery(selectQuery, null);

        int size = mCursor.getCount();

        List<String> valueList = new ArrayList<>(size);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                valueList.add(mCursor.getString(0));
            } while (mCursor.moveToNext());
        }
        close();
        // return value list
        return valueList;
    }

    // Getting All Dates
    public List<String> getAllDatesList(Profile pProfile, Machine pMachine) {

        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;

        // Select All Machines
        String selectQuery = "SELECT DISTINCT " + LOCAL_DATE + " FROM " + TABLE_NAME;
        if (pMachine != null) {
            selectQuery += " WHERE " + EXERCISE_KEY + "=" + pMachine.getId();
            if (pProfile != null)
                selectQuery += " AND " + PROFILE_KEY + "=" + pProfile.getId(); // pProfile should never be null but depending on how the activity is resuming it happen. to be fixed
        } else {
            if (pProfile != null)
                selectQuery += " WHERE " + PROFILE_KEY + "=" + pProfile.getId(); // pProfile should never be null but depending on how the activity is resuming it happen. to be fixed
        }
        selectQuery += " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                     + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                     + " ORDER BY " + DATE_TIME + " DESC";

        mCursor = db.rawQuery(selectQuery, null);
        int size = mCursor.getCount();

        List<String> valueList = new ArrayList<>(size);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                Date date = DateConverter.DBDateStrToDate(mCursor.getString(0));
                valueList.add(DateConverter.dateToLocalDateStr(date, mContext));
            } while (mCursor.moveToNext());
        }

        close();

        // return value list
        return valueList;
    }

    public Cursor getTop3DatesFreeWorkoutRecords(Profile pProfile) {

        if (pProfile == null)
            return null;

        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + LOCAL_DATE + " IN (SELECT DISTINCT " + LOCAL_DATE + " FROM " + TABLE_NAME + " WHERE " + PROFILE_KEY + "=" + pProfile.getId() + " AND " + PROGRAM_KEY + "=-1" + " ORDER BY " + LOCAL_DATE + " DESC LIMIT 3)"
                + " AND " + PROGRAM_KEY + "=-1"
                + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";

        return getRecordsListCursor(selectQuery);
    }

    // Getting Filtered records
    public Cursor getFilteredRecords(Profile pProfile, String pMachine, String pDate) {

        boolean lfilterMachine = true;
        boolean lfilterDate = true;
        String selectQuery;

        if (pMachine == null || pMachine.isEmpty() || pMachine.equals(mContext.getResources().getText(R.string.all).toString())) {
            lfilterMachine = false;
        }

        if (pDate == null || pDate.isEmpty() || pDate.equals(mContext.getResources().getText(R.string.all).toString())) {
            lfilterDate = false;
        }

        if (lfilterMachine && lfilterDate) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine
                    + "\" AND " + LOCAL_DATE + "=\"" + pDate + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";
        } else if (!lfilterMachine && lfilterDate) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + LOCAL_DATE + "=\"" + pDate + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";
        } else if (lfilterMachine) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";
        } else {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + RECORD_TYPE + "!=" + RecordType.PROGRAM_TEMPLATE.ordinal()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";
        }

        // return value list
        return getRecordsListCursor(selectQuery);
    }

    /**
     * @return the last record for a profile p
     */
    public FoodRecord getLastRecord(Profile pProfile) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        FoodRecord lReturn = null;

        // Select last record
        String selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                + " WHERE " + PROFILE_KEY + "=" + pProfile.getId();
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
    public FoodRecord getLastExerciseRecord(long machineID, Profile p) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        FoodRecord lReturn = null;

        String selectQuery;
        if (p == null) {
            selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE_KEY + "=" + machineID;
        } else {
            selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE_KEY + "=" + machineID +
                    " AND " + PROFILE_KEY + "=" + p.getId();
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
    public List<FoodRecord> getAllRecordByMachineStrArray(Profile pProfile, String pMachines) {
        return getAllRecordByMachineStrArray(pProfile, pMachines, -1);
    }

    public List<FoodRecord> getAllRecordByMachineStrArray(Profile pProfile, String pMachines, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + EXERCISE + "=\"" + pMachines + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC" + mTop;

        // return value list
        return getRecordsList(selectQuery);
    }

    public List<FoodRecord> getAllRecordByMachineIdArray(Profile pProfile, long pMachineId, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + EXERCISE_KEY + "=\"" + pMachineId + "\""
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC" + mTop;

        // return value list
        return getRecordsList(selectQuery);
    }

    // Get all record for one Machine
    public List<FoodRecord> getAllRecordByMachineIdArray(Profile pProfile, long pMachineId) {
        return getAllRecordByMachineIdArray(pProfile, pMachineId, -1);
    }


    public List<FoodRecord> getAllTemplateRecordByProgramArray(long pTemplateId) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + PROGRAM_KEY + "=" + pTemplateId
                + " AND " + RECORD_TYPE + "=" + RecordType.PROGRAM_TEMPLATE.ordinal();

        // return value list
        return getRecordsList(selectQuery);
    }

    private List<FoodRecord> getRecordsList(String pRequest) {
        return getRecordsList(pRequest, this.getReadableDatabase());
    }

    // Getting All Records
    private List<FoodRecord> getRecordsList(String pRequest, SQLiteDatabase db) {
        List<FoodRecord> valueList = new ArrayList<>();
        // Select All Query

        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst() && mCursor.getCount() > 0) {
            do {
                FoodRecord value = fromCursor(mCursor);
                value.setId(mCursor.getLong(mCursor.getColumnIndexOrThrow(DAOFoodRecord.KEY)));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        // return value list
        return valueList;
    }

    // Updating single value
    public int updateRecord(FoodRecord record) {
        return updateRecord(this.getWritableDatabase(), record);
    }

    public int updateRecord(SQLiteDatabase db, FoodRecord record) {
        ContentValues value = new ContentValues();

        value.put(DAOFoodRecord.KEY, record.getId());
        value.put(DAOFoodRecord.DATE, DateConverter.dateTimeToDBDateStr(record.getDate()));
        value.put(DAOFoodRecord.TIME, DateConverter.dateTimeToDBTimeStr(record.getDate()));
        value.put(DAOFoodRecord.EXERCISE, record.getExercise());
        value.put(DAOFoodRecord.EXERCISE_KEY, record.getExerciseId());
        value.put(DAOFoodRecord.EXERCISE_TYPE, record.getExerciseType().ordinal());
        value.put(DAOFoodRecord.PROFILE_KEY, record.getProfileId());
        value.put(DAOFoodRecord.SETS, record.getSets());
        value.put(DAOFoodRecord.REPS, record.getReps());
        value.put(DAOFoodRecord.WEIGHT, record.getWeightInKg());
        value.put(DAOFoodRecord.WEIGHT_UNIT, record.getWeightUnit().ordinal());
        value.put(DAOFoodRecord.DISTANCE, record.getDistanceInKm());
        value.put(DAOFoodRecord.DISTANCE_UNIT, record.getDistanceUnit().ordinal());
        value.put(DAOFoodRecord.DURATION, record.getDuration());
        value.put(DAOFoodRecord.SECONDS, record.getSeconds());
        value.put(DAOFoodRecord.NOTES, record.getNote());
        value.put(DAOFoodRecord.PROGRAM_KEY, record.getProgramId());
        value.put(DAOFoodRecord.TEMPLATE_RECORD_KEY, record.getTemplateRecordId());
        value.put(DAOFoodRecord.PROGRAM_SESSION_KEY, record.getTemplateSessionId());
        value.put(DAOFoodRecord.TEMPLATE_REST_TIME, record.getTemplateRestTime());
        value.put(DAOFoodRecord.TEMPLATE_ORDER, record.getTemplateOrder());
        value.put(DAOFoodRecord.TEMPLATE_RECORD_STATUS, record.getProgramRecordStatus().ordinal());
        value.put(DAOFoodRecord.RECORD_TYPE, record.getRecordType().ordinal());

        value.put(DAOFoodRecord.TEMPLATE_SETS, record.getTemplateSets());
        value.put(DAOFoodRecord.TEMPLATE_REPS, record.getTemplateReps());
        value.put(DAOFoodRecord.TEMPLATE_WEIGHT, record.getTemplateWeight());
        value.put(DAOFoodRecord.TEMPLATE_WEIGHT_UNIT,record.getTemplateWeightUnit().ordinal());
        value.put(DAOFoodRecord.TEMPLATE_DISTANCE, record.getTemplateDistance());
        value.put(DAOFoodRecord.TEMPLATE_DISTANCE_UNIT, record.getTemplateDistanceUnit().ordinal());
        value.put(DAOFoodRecord.TEMPLATE_DURATION, record.getTemplateDuration());
        value.put(DAOFoodRecord.TEMPLATE_SECONDS, record.getTemplateSeconds());


        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
                new String[]{String.valueOf(record.getId())});
    }

    public void closeCursor() {
        if (mCursor != null) mCursor.close();
    }

    public void closeAll() {
        if (mCursor != null) mCursor.close();
        close();
    }


}
