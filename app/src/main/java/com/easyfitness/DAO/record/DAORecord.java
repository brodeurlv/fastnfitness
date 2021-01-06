package com.easyfitness.DAO.record;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DAOBase;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.R;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.RecordType;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.DateConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAORecord extends DAOBase {

    // Contacts table name
    public static final String TABLE_NAME = "EFfontes";

    public static final String KEY = "_id";
    public static final String DATE = "date";
    public static final String LOCAL_DATE = "DATE(date || 'T' || time, 'localtime')";
    public static final String TIME = "time";
    public static final String DATE_TIME = "DATETIME(date || 'T' || time)";
    public static final String EXERCISE = "machine";
    public static final String PROFILE_KEY = "profil_id";
    public static final String EXERCISE_KEY = "machine_id";
    public static final String NOTES = "notes";
    public static final String EXERCISE_TYPE = "type";

    // Specific to BodyBuilding
    public static final String SETS = "serie";
    public static final String REPS = "repetition";
    public static final String WEIGHT = "poids";
    public static final String WEIGHT_UNIT = "unit"; // 0:kg 1:lbs

    // Specific to Cardio
    public static final String DISTANCE = "distance";
    public static final String DURATION = "duration";
    public static final String DISTANCE_UNIT = "distance_unit"; // 0:km 1:mi

    // Specific to STATIC
    public static final String SECONDS = "seconds";

    // For Workout Templates
    public static final String RECORD_TYPE = "RECORD_TYPE";
    public static final String TEMPLATE_KEY = "TEMPLATE_KEY";
    public static final String TEMPLATE_RECORD_KEY = "TEMPLATE_RECORD_KEY";
    public static final String TEMPLATE_SESSION_KEY = "TEMPLATE_SESSION_KEY";
    public static final String TEMPLATE_ORDER = "TEMPLATE_ORDER";
    public static final String TEMPLATE_REST_TIME = "TEMPLATE_SECONDS";

    public static final int FREE_RECORD_TYPE = 0;
    public static final int PROGRAM_RECORD_TYPE = 1;
    public static final int TEMPLATE_TYPE = 2;

    public static final String TEMPLATE_RECORD_STATUS = "TEMPLATE_RECORD_STATUS";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
            + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PROFILE_KEY + " INTEGER, "
            + EXERCISE_KEY + " INTEGER,"
            + DATE + " DATE, "
            + TIME + " TEXT,"
            + EXERCISE + " TEXT, "
            + SETS + " INTEGER, "
            + REPS + " INTEGER, "
            + WEIGHT + " REAL, "
            + WEIGHT_UNIT + " INTEGER, "
            + NOTES + " TEXT, "
            + DISTANCE + " REAL, "
            + DURATION + " TEXT, "
            + EXERCISE_TYPE + " INTEGER, "
            + SECONDS + " INTEGER, "
            + DISTANCE_UNIT + " INTEGER,"
            + TEMPLATE_KEY + " INTEGER,"
            + TEMPLATE_RECORD_KEY + " INTEGER,"
            + TEMPLATE_SESSION_KEY + " INTEGER,"
            + TEMPLATE_ORDER + " INTEGER,"
            + TEMPLATE_REST_TIME + " INTEGER,"
            + TEMPLATE_RECORD_STATUS + " INTEGER,"
            + RECORD_TYPE + " INTEGER"
            + " );";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS "
            + TABLE_NAME + ";";


    protected Profile mProfile = null;
    protected Cursor mCursor = null;
    protected Context mContext;

    public DAORecord(Context context) {
        super(context);
        mContext = context;
    }

    public void setProfile(Profile pProfile) {
        mProfile = pProfile;
    }

    /*public Cursor getCursor() {
        return mCursor;
    }*/

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

    public long addRecord(Record record) {
        return addRecord(record.getDate(),
                record.getExercise(), record.getExerciseType(),
                record.getSets(), record.getReps(), record.getWeight(), record.getWeightUnit(),
                record.getNote(),
                record.getDistance(), record.getDistanceUnit(), record.getDuration(),
                record.getSeconds(),
                record.getProfileId(), record.getRecordType(),
                record.getTemplateRecordId(), record.getTemplateId(), record.getTemplateSessionId(), record.getRestTime(), record.getProgramRecordStatus());
    }

    /**
     * @param pDate             Date
     * @param pExercise         Machine name
     * @param pProfileId
     * @param pTemplateRecordId Id of the Template record that has been used to generate this Record
     * @return id of the added record, -1 if error
     */
    public long addRecord(Date pDate, String pExercise, ExerciseType pExerciseType, int pSets, int pReps, float pWeight,
                          WeightUnit pUnit, int pSeconds, float pDistance, DistanceUnit pDistanceUnit, long pDuration, String pNote, long pProfileId,
                          long pTemplateRecordId, RecordType pRecordType) {
        return addRecord(pDate, pExercise, pExerciseType, pSets, pReps, pWeight, pUnit, pNote, pDistance, pDistanceUnit, pDuration, pSeconds, pProfileId,
                pRecordType, pTemplateRecordId, -1, -1, 0, ProgramRecordStatus.SUCCESS);
    }

    /**
     * @param pDate              Date
     * @param pExercise          Machine name
     * @param pExerciseType      Weight, Cardio or Isometric
     * @param pWeightUnit        LBS or KG
     * @param pProfileId
     * @param pTemplateRecordId
     * @param pTemplateSessionId
     * @return id of the added record, -1 if error
     */
    public long addRecord(Date pDate, String pExercise, ExerciseType pExerciseType, int pSets, int pReps, float pWeight,
                          WeightUnit pWeightUnit, String pNote, float pDistance, DistanceUnit pDistanceUnit, long pDuration, int pSeconds, long pProfileId,
                          RecordType pRecordType, long pTemplateRecordId, long pTemplateId, long pTemplateSessionId,
                          int pRestTime, ProgramRecordStatus pProgramRecordStatus) {

        ContentValues value = new ContentValues();
        long machine_key = -1;

        //Test is Machine exists. If not create it.
        DAOMachine lDAOMachine = new DAOMachine(mContext);
        if (!lDAOMachine.machineExists(pExercise)) {
            machine_key = lDAOMachine.addMachine(pExercise, "", pExerciseType, "", false, "");
        } else {
            machine_key = lDAOMachine.getMachine(pExercise).getId();
        }

        int templateOrder = 0;
        if (pRecordType == RecordType.TEMPLATE_TYPE) {
            Cursor cursor = this.getProgramTemplateRecords(pTemplateId);
            List<Record> records = fromCursorToList(cursor);
            templateOrder = records.size();
        }

        value.put(DAORecord.DATE, DateConverter.dateTimeToDBDateStr(pDate));
        value.put(DAORecord.TIME, DateConverter.dateTimeToDBTimeStr(pDate));
        value.put(DAORecord.EXERCISE, pExercise);
        value.put(DAORecord.EXERCISE_KEY, machine_key);
        value.put(DAORecord.EXERCISE_TYPE, pExerciseType.ordinal());
        value.put(DAORecord.PROFILE_KEY, pProfileId);
        value.put(DAORecord.SETS, pSets);
        value.put(DAORecord.REPS, pReps);
        value.put(DAORecord.WEIGHT, pWeight);
        value.put(DAORecord.WEIGHT_UNIT, pWeightUnit.ordinal());
        value.put(DAORecord.DISTANCE, pDistance);
        value.put(DAORecord.DISTANCE_UNIT, pDistanceUnit.ordinal());
        value.put(DAORecord.DURATION, pDuration);
        value.put(DAORecord.SECONDS, pSeconds);
        value.put(DAORecord.NOTES, pNote);
        value.put(DAORecord.TEMPLATE_KEY, pTemplateId);
        value.put(DAORecord.TEMPLATE_RECORD_KEY, pTemplateRecordId);
        value.put(DAORecord.TEMPLATE_SESSION_KEY, pTemplateSessionId);
        value.put(DAORecord.TEMPLATE_REST_TIME, pRestTime);
        value.put(DAORecord.TEMPLATE_ORDER, templateOrder);
        value.put(DAORecord.RECORD_TYPE, pRecordType.ordinal());
        value.put(DAORecord.TEMPLATE_RECORD_STATUS, pProgramRecordStatus.ordinal());

        SQLiteDatabase db = open();
        long new_id = db.insert(DAORecord.TABLE_NAME, null, value);
        close();

        return new_id;
    }

    public void addList(List<Record> list) {
        for (Record record : list) {
            addRecord(record.getDate(),
                    record.getExercise(), record.getExerciseType(),
                    record.getSets(), record.getReps(), record.getWeight(), record.getWeightUnit(),
                    record.getSeconds(),
                    record.getDistance(), record.getDistanceUnit(), record.getDuration(),
                    record.getNote(),
                    record.getProfileId(), record.getTemplateRecordId(),
                    record.getRecordType());
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
    public Record getRecord(long id) {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + KEY + "=" + id;

        mCursor = getRecordsListCursor(selectQuery);
        if (mCursor.moveToFirst()) {
            //Get Date
            return fromCursor(mCursor);
        } else {
            return null;
        }
    }

    private Record fromCursor(Cursor cursor) {
        Date date = DateConverter.DBDateTimeStrToDate(
                cursor.getString(cursor.getColumnIndex(DAOFonte.DATE)),
                cursor.getString(cursor.getColumnIndex(DAOFonte.TIME))
        );

        long machine_key = -1;

        //Test is Machine exists. If not create it.
        DAOMachine lDAOMachine = new DAOMachine(mContext);
        if (cursor.getString(cursor.getColumnIndex(DAOFonte.EXERCISE_KEY)) == null) {
            machine_key = lDAOMachine.addMachine(cursor.getString(cursor.getColumnIndex(DAOFonte.EXERCISE)), "", ExerciseType.STRENGTH, "", false, "");
        } else {
            machine_key = cursor.getLong(cursor.getColumnIndex(DAOFonte.EXERCISE_KEY));
        }

        Record value = new Record(date,
                cursor.getString(cursor.getColumnIndex(DAORecord.EXERCISE)),
                machine_key,
                cursor.getLong(cursor.getColumnIndex(DAORecord.PROFILE_KEY)),
                cursor.getInt(cursor.getColumnIndex(DAORecord.SETS)),
                cursor.getInt(cursor.getColumnIndex(DAORecord.REPS)),
                cursor.getFloat(cursor.getColumnIndex(DAORecord.WEIGHT)),
                WeightUnit.fromInteger(cursor.getInt(cursor.getColumnIndex(DAORecord.WEIGHT_UNIT))),
                cursor.getInt(cursor.getColumnIndex(DAORecord.SECONDS)),
                cursor.getFloat(cursor.getColumnIndex(DAORecord.DISTANCE)),
                DistanceUnit.fromInteger(cursor.getInt(cursor.getColumnIndex(DAORecord.DISTANCE_UNIT))),
                cursor.getLong(cursor.getColumnIndex(DAORecord.DURATION)),
                cursor.getString(cursor.getColumnIndex(DAORecord.NOTES)),
                ExerciseType.fromInteger(cursor.getInt(cursor.getColumnIndex(DAORecord.EXERCISE_TYPE))),
                cursor.getLong(cursor.getColumnIndex(DAORecord.TEMPLATE_KEY)),
                cursor.getLong(cursor.getColumnIndex(DAORecord.TEMPLATE_RECORD_KEY)),
                cursor.getLong(cursor.getColumnIndex(DAORecord.TEMPLATE_SESSION_KEY)),
                cursor.getInt(cursor.getColumnIndex(DAORecord.TEMPLATE_REST_TIME)),
                cursor.getInt(cursor.getColumnIndex(DAORecord.TEMPLATE_ORDER)),
                ProgramRecordStatus.fromInteger(cursor.getInt(cursor.getColumnIndex(DAORecord.TEMPLATE_RECORD_STATUS))),
                RecordType.fromInteger(cursor.getInt(cursor.getColumnIndex(DAORecord.RECORD_TYPE))));

        value.setId(cursor.getLong(cursor.getColumnIndex(DAORecord.KEY)));
        return value;
    }

    public List<Record> fromCursorToList(Cursor cursor) {
        List<Record> valueList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Record record = fromCursor(cursor);
                if (record != null) valueList.add(record);
            } while (cursor.moveToNext());
        }
        return valueList;
    }

    // Getting All Records
    public List<Record> getAllRecords() {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " ORDER BY " + KEY + " DESC";

        // return value list
        return getRecordsList(selectQuery);
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
    public List<Record> getAllRecordsByProfileList(Profile pProfile) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
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
    private Cursor getRecordsListCursor(String pRequest) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(pRequest, null);
    }

    // Getting All Machines
    public List<String> getAllMachinesStrList() {
        return getAllMachinesStrList(null);
    }

    // Getting All Machines
    public List<String> getAllMachinesStrList(Profile pProfile) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        String selectQuery = "";
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
            int i = 0;
            do {
                valueList.add(mCursor.getString(0));
                i++;
            } while (mCursor.moveToNext());
        }
        close();
        // return value list
        return valueList;
    }

    // Getting All Machines
    public String[] getAllMachines(Profile pProfile) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;

        // Select All Machines
        String selectQuery = "SELECT DISTINCT " + EXERCISE + " FROM "
                + TABLE_NAME + "  WHERE " + PROFILE_KEY + "=" + pProfile.getId() + " ORDER BY " + EXERCISE + " ASC";
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
        selectQuery += " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal();
        selectQuery += " ORDER BY " + DATE_TIME + " DESC";

        mCursor = db.rawQuery(selectQuery, null);
        int size = mCursor.getCount();

        List<String> valueList = new ArrayList<>(size);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                int i = 0;

                Date date = DateConverter.DBDateStrToDate(mCursor.getString(0));
                valueList.add(DateConverter.dateToLocalDateStr(date, mContext));
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
                + " WHERE " + PROFILE_KEY + "=" + pProfile.getId()
                + " AND " + LOCAL_DATE + " IN (SELECT DISTINCT " + LOCAL_DATE + " FROM " + TABLE_NAME + " WHERE " + PROFILE_KEY + "=" + pProfile.getId() + " AND " + TEMPLATE_KEY + "=-1" + " ORDER BY " + LOCAL_DATE + " DESC LIMIT 3)"
                + " AND " + TEMPLATE_KEY + "=-1"
                + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";

        return getRecordsListCursor(selectQuery);
    }

    public Cursor getProgramTemplateRecords(long mTemplateId) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + TEMPLATE_KEY + "=" + mTemplateId
                + " AND " + RECORD_TYPE + "=" + RecordType.TEMPLATE_TYPE.ordinal()
                + " ORDER BY " + TEMPLATE_ORDER + " ASC";

        return getRecordsListCursor(selectQuery);
    }

    public Cursor getProgramWorkoutRecords(long mProgramSessionId) {
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + TEMPLATE_SESSION_KEY + "=" + mProgramSessionId
                + " ORDER BY " + TEMPLATE_ORDER + " ASC";

        return getRecordsListCursor(selectQuery);
    }

    // Getting Filtered records
    public Cursor getFilteredRecords(Profile pProfile, String pMachine, String pDate) {

        boolean lfilterMachine = true;
        boolean lfilterDate = true;
        String selectQuery = null;

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
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";
        } else if (!lfilterMachine && lfilterDate) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + LOCAL_DATE + "=\"" + pDate + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";
        } else if (lfilterMachine) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + EXERCISE + "=\"" + pMachine + "\""
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";
        } else {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + PROFILE_KEY + "=" + pProfile.getId()
                    + " AND " + TEMPLATE_RECORD_STATUS + "!=" + ProgramRecordStatus.PENDING.ordinal()
                    + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";
        }

        // return value list
        return getRecordsListCursor(selectQuery);
    }

    /**
     * @return the last record for a profile p
     */
    public Record getLastRecord(Profile pProfile) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        Record lReturn = null;

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
    public Record getLastExerciseRecord(long machineID, Profile p) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        Record lReturn = null;

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
    public List<Record> getAllRecordByMachineStrArray(Profile pProfile, String pMachines) {
        return getAllRecordByMachineStrArray(pProfile, pMachines, -1);
    }

    public List<Record> getAllRecordByMachineStrArray(Profile pProfile, String pMachines, int pNbRecords) {
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

    public List<Record> getAllRecordByMachineIdArray(Profile pProfile, long pMachineId, int pNbRecords) {
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
    public List<Record> getAllRecordByMachineIdArray(Profile pProfile, long pMachineId) {
        return getAllRecordByMachineIdArray(pProfile, pMachineId, -1);
    }


    public List<Record> getAllTemplateRecordByProgramArray(long pTemplateId) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + TEMPLATE_KEY + "=" + pTemplateId
                + " AND " + RECORD_TYPE + "=" + DAORecord.TEMPLATE_TYPE;

        // return value list
        return getRecordsList(selectQuery);
    }


    // Getting All Records
    private List<Record> getRecordsList(String pRequest) {
        List<Record> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Select All Query

        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst() && mCursor.getCount() > 0) {
            do {
                Record value = fromCursor(mCursor);
                value.setId(mCursor.getLong(mCursor.getColumnIndex(DAORecord.KEY)));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        // return value list
        return valueList;
    }

    // Updating single value
    public int updateRecord(Record record) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DAORecord.KEY, record.getId());
        value.put(DAORecord.DATE, DateConverter.dateTimeToDBDateStr(record.getDate()));
        value.put(DAORecord.TIME, DateConverter.dateTimeToDBTimeStr(record.getDate()));
        value.put(DAORecord.EXERCISE, record.getExercise());
        value.put(DAORecord.EXERCISE_KEY, record.getExerciseId());
        value.put(DAORecord.EXERCISE_TYPE, record.getExerciseType().ordinal());
        value.put(DAORecord.PROFILE_KEY, record.getProfileId());
        value.put(DAORecord.SETS, record.getSets());
        value.put(DAORecord.REPS, record.getReps());
        value.put(DAORecord.WEIGHT, record.getWeight());
        value.put(DAORecord.WEIGHT_UNIT, record.getWeightUnit().ordinal());
        value.put(DAORecord.DISTANCE, record.getDistance());
        value.put(DAORecord.DISTANCE_UNIT, record.getDistanceUnit().ordinal());
        value.put(DAORecord.DURATION, record.getDuration());
        value.put(DAORecord.SECONDS, record.getSeconds());
        value.put(DAORecord.NOTES, record.getNote());
        value.put(DAORecord.TEMPLATE_KEY, record.getTemplateId());
        value.put(DAORecord.TEMPLATE_RECORD_KEY, record.getTemplateRecordId());
        value.put(DAORecord.TEMPLATE_SESSION_KEY, record.getTemplateSessionId());
        value.put(DAORecord.TEMPLATE_REST_TIME, record.getRestTime());
        value.put(DAORecord.TEMPLATE_ORDER, record.getTemplateOrder());
        value.put(DAORecord.TEMPLATE_RECORD_STATUS, record.getProgramRecordStatus().ordinal());
        value.put(DAORecord.RECORD_TYPE, record.getRecordType().ordinal());

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
