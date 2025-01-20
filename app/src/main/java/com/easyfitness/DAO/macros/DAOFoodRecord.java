package com.easyfitness.DAO.macros;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.easyfitness.DAO.DAOBase;
import com.easyfitness.DAO.Profile;
import com.easyfitness.R;
import com.easyfitness.enums.FoodQuantityUnit;
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
    public static final String FOOD_NAME = "food_name";
    public static final String PROFILE_KEY = "profile_id";
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
            + DATE + " DATE, "
            + TIME + " TEXT,"
            + FOOD_NAME + " TEXT, "
            + CALORIES + " REAL, "
            + CARBS + " REAL, "
            + PROTEIN + " REAL, "
            + FATS + " REAL, "
            + QUANTITY + " REAL, "
            + QUANTITY_UNIT + " TEXT, "
            + NOTES + " TEXT"
            + ");";

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

    public long addRecord(@NonNull FoodRecord record) {
        return addRecord(record.getDate(),record.getFoodName(),record.getProfileId(),record.getQuantity(),record.getQuantityUnit(),
                record.getCalories(), record.getCarbs(), record.getProtein(), record.getFats(), record.getNote());
    }

    /**
     * @return id of the added record, -1 if error
     */
    public long addRecord(Date date, String foodName, long profileId, float quantity, FoodQuantityUnit quantityUnit,
                          float calories, float carbs, float protein, float fats, String notes) {

        ContentValues value = new ContentValues();

        value.put(DAOFoodRecord.FOOD_NAME, foodName);
        value.put(DAOFoodRecord.DATE, DateConverter.dateTimeToDBDateStr(date));
        value.put(DAOFoodRecord.TIME, DateConverter.dateTimeToDBTimeStr(date));
        value.put(DAOFoodRecord.PROFILE_KEY, profileId);
        value.put(DAOFoodRecord.QUANTITY, quantity);
        value.put(DAOFoodRecord.QUANTITY_UNIT, quantityUnit.toString());
        value.put(DAOFoodRecord.CALORIES, calories);
        value.put(DAOFoodRecord.PROTEIN, protein);
        value.put(DAOFoodRecord.CARBS, carbs);
        value.put(DAOFoodRecord.FATS, fats);
        value.put(DAOFoodRecord.NOTES, notes);

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

    // Get macro totals for a given day
    public @Nullable FoodRecord getMacroTotalsForDate(Date d, Profile p) {
        String selectQuery = "SELECT ";
        selectQuery += "SUM(" + CALORIES + "), ";
        selectQuery += "SUM(" + CARBS + "), ";
        selectQuery += "SUM(" + PROTEIN + "), ";
        selectQuery += "SUM(" + FATS + ") ";

        selectQuery += "FROM " + TABLE_NAME + " WHERE " + DATE + "='" + DateConverter.dateToDBDateStr(d) + "' ";
        selectQuery += "AND " + PROFILE_KEY + "=" + p.getId();

        Cursor c = getRecordsListCursor(selectQuery);
        if (c.moveToFirst()) {
            FoodRecord r = new FoodRecord(
                d,
       "Totals for " + DateConverter.dateToLocalDateStr(d, mContext),p.getId(),
        0,
                FoodQuantityUnit.GRAMS,
        0.0f,
          0.0f,
         0.0f,
        0.0f);
            r.setCalories(c.getFloat(0));
            r.setCarbs(c.getFloat(1));
            r.setProtein(c.getFloat(2));
            r.setFats(c.getFloat(3));
            c.close();
            return r;
        } else {
            c.close();
            return null;
        }
    }

    public FoodRecord getRecord(long id) {
       return getRecord(this.getReadableDatabase(), id);
    }

    private FoodRecord fromCursor(Cursor cursor) {
        Date date = DateConverter.DBDateTimeStrToDate(
                cursor.getString(cursor.getColumnIndexOrThrow(DAOFoodRecord.DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DAOFoodRecord.TIME))
        );
//        (Date date, String foodName, long foodId, long profileId, float quantity, FoodQuantityUnit quantityUnit,
//        float calories, float carbs, float protein, float fats)
        FoodRecord value = new FoodRecord(date,
            cursor.getString(cursor.getColumnIndexOrThrow(DAOFoodRecord.FOOD_NAME)),
            cursor.getLong(cursor.getColumnIndexOrThrow(DAOFoodRecord.PROFILE_KEY)),
            cursor.getFloat(cursor.getColumnIndexOrThrow(DAOFoodRecord.QUANTITY)),
            FoodQuantityUnit.fromString(cursor.getString(cursor.getColumnIndexOrThrow(DAOFoodRecord.QUANTITY_UNIT))),
            cursor.getFloat(cursor.getColumnIndexOrThrow(DAOFoodRecord.CALORIES)),
            cursor.getFloat(cursor.getColumnIndexOrThrow(DAOFoodRecord.CARBS)),
            cursor.getFloat(cursor.getColumnIndexOrThrow(DAOFoodRecord.PROTEIN)),
            cursor.getFloat(cursor.getColumnIndexOrThrow(DAOFoodRecord.FATS))
        );

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
    public Cursor getAllRecordByFoodName(Profile pProfile, String foodName) {
        return getAllRecordByFoodName(pProfile, foodName, -1);
    }

    public Cursor getAllRecordByFoodName(Profile pProfile, String foodName, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + FOOD_NAME + "='" + foodName + "'"
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
        List<FoodRecord> l = fromCursorToList(cursor);
        cursor.close();
        return l;
    }

    /**
     * @param pProfile   record associated to one profile
     * @param pNbRecords max number of records requested
     * @return pNbRecords number of records for a specified pProfile
     */
    public Cursor getAllRecordsByProfile(Profile pProfile, @NonNull int pNbRecords) {
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
    public List<String> getAllFoodsStrList() {
        return getAllFoodsStrList(null);
    }

    // Getting All Machines
    public List<String> getAllFoodsStrList(Profile pProfile) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        String selectQuery;
        if (pProfile == null) {
            selectQuery = "SELECT DISTINCT " + FOOD_NAME + " FROM "
                    + TABLE_NAME + " ORDER BY " + FOOD_NAME + " ASC";
        } else {
            selectQuery = "SELECT DISTINCT " + FOOD_NAME + " FROM "
                    + TABLE_NAME + "  WHERE " + PROFILE_KEY + "=" + pProfile.getId() + " ORDER BY " + FOOD_NAME + " ASC";
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
    public List<String> getAllDatesList(Profile pProfile, @Nullable String foodName) {

        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;

        // Select All dates of food entries
        String selectQuery = "SELECT DISTINCT " + LOCAL_DATE + " FROM " + TABLE_NAME;
        if (foodName != null) {
            selectQuery += " WHERE " + FOOD_NAME + "='" + foodName + "'";
            if (pProfile != null)
                selectQuery += " AND " + PROFILE_KEY + "=" + pProfile.getId(); // pProfile should never be null but depending on how the activity is resuming it happen. to be fixed
        } else {
            if (pProfile != null)
                selectQuery += " WHERE " + PROFILE_KEY + "=" + pProfile.getId(); // pProfile should never be null but depending on how the activity is resuming it happen. to be fixed
        }
        selectQuery += " ORDER BY " + DATE_TIME + " DESC";

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


    // Getting Filtered records
    public Cursor getFilteredRecords(Profile pProfile, @Nullable String foodName, @Nullable String pDate) {

        boolean lfilterFoodName = true;
        boolean lfilterDate = true;
        String selectQuery;

        if (foodName == null || foodName.isEmpty() || foodName.equals(mContext.getResources().getText(R.string.all).toString())) {
            lfilterFoodName = false;
        }

        if (pDate == null || pDate.isEmpty() || pDate.equals(mContext.getResources().getText(R.string.all).toString())) {
            lfilterDate = false;
        }

        if (lfilterFoodName && lfilterDate) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + FOOD_NAME + "='" + foodName + "'"
                    + " AND " + LOCAL_DATE + "='" + pDate + "'"
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";
        } else if (!lfilterFoodName && lfilterDate) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + LOCAL_DATE + "='" + pDate + "'"
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";
        } else if (lfilterFoodName) {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + FOOD_NAME + "='" + foodName + "'"
                    + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                    + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";
        } else {
            selectQuery = "SELECT * FROM " + TABLE_NAME
                    + " WHERE " + PROFILE_KEY + "=" + pProfile.getId()
                    + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC";
        }

        // return value list
        return getRecordsListCursor(selectQuery);
    }

    public FoodRecord getMostRecentFoodRecord(Profile pProfile, String foodName) {

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        FoodRecord lReturn = null;

        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + FOOD_NAME + "='" + foodName + "'";
        if (pProfile != null) {
            selectQuery += " AND " + PROFILE_KEY + "=" + pProfile.getId();
        }
        selectQuery += " ORDER BY " + DATE_TIME + " DESC LIMIT 1";
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

        mCursor.close();
        close();

        // return value list
        return lReturn;
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
    public FoodRecord getLastFoodRecord(long foodID, Profile p) {

        SQLiteDatabase db = this.getReadableDatabase();
        FoodRecord lReturn = null;

        String selectQuery;
        if (p == null) {
            selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                    + " WHERE " + KEY + "=" + foodID;
        } else {
            selectQuery = "SELECT MAX(" + KEY + ") FROM " + TABLE_NAME
                    + " WHERE " + KEY + "=" + foodID +
                    " AND " + PROFILE_KEY + "=" + p.getId();
        }
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through only the first rows.
        if (c.moveToFirst()) {
            try {
                long value = c.getLong(0);
                lReturn = this.getRecord(value);
            } catch (NumberFormatException e) {
                lReturn = null; // Return une valeur
            }
        }

        c.close();

        // return value list
        return lReturn;
    }

    // Get all record for one Machine
    public List<FoodRecord> getAllRecordByFoodNameStrArray(Profile pProfile, String pMachines) {
        return getAllRecordByFoodNameStrArray(pProfile, pMachines, -1);
    }

    public List<FoodRecord> getAllRecordByFoodNameStrArray(Profile pProfile, String foodName, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + FOOD_NAME + "='" + foodName + "'"
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC" + mTop;

        // return value list
        return getRecordsList(selectQuery);
    }

    public List<FoodRecord> getAllRecordByFoodIdArray(Profile pProfile, long foodId, int pNbRecords) {
        String mTop;
        if (pNbRecords == -1) mTop = "";
        else mTop = " LIMIT " + pNbRecords;

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME
                + " WHERE " + KEY + "='" + foodId + "'"
                + " AND " + PROFILE_KEY + "=" + pProfile.getId()
                + " ORDER BY " + DATE_TIME + " DESC," + KEY + " DESC" + mTop;

        // return value list
        return getRecordsList(selectQuery);
    }

    // Get all record for one Machine
    public List<FoodRecord> getAllRecordByFoodIdArray(Profile pProfile, long foodId) {
        return getAllRecordByFoodIdArray(pProfile, foodId, -1);
    }

    private List<FoodRecord> getRecordsList(String pRequest) {
        return getRecordsList(pRequest, this.getReadableDatabase());
    }

    // Getting All Records
    private List<FoodRecord> getRecordsList(String pRequest, SQLiteDatabase db) {
        List<FoodRecord> valueList = new ArrayList<>();
        // Select All Query


        Cursor c = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (c.moveToFirst() && c.getCount() > 0) {
            do {
                FoodRecord value = fromCursor(c);
                value.setId(c.getLong(c.getColumnIndexOrThrow(DAOFoodRecord.KEY)));

                // Adding value to list
                valueList.add(value);
            } while (c.moveToNext());
        }
        c.close();
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
        value.put(DAOFoodRecord.KEY, record.getId());
        value.put(DAOFoodRecord.FOOD_NAME, record.getFoodName());
        value.put(DAOFoodRecord.DATE, DateConverter.dateTimeToDBDateStr(record.getDate()));
        value.put(DAOFoodRecord.TIME, DateConverter.dateTimeToDBTimeStr(record.getDate()));
        value.put(DAOFoodRecord.PROFILE_KEY, record.getId());
        value.put(DAOFoodRecord.QUANTITY, record.getQuantity());
        value.put(DAOFoodRecord.QUANTITY_UNIT, record.getQuantityUnit().toString());
        value.put(DAOFoodRecord.CALORIES, record.getCalories());
        value.put(DAOFoodRecord.PROTEIN, record.getCalories());
        value.put(DAOFoodRecord.CARBS, record.getProtein());
        value.put(DAOFoodRecord.FATS, record.getFats());
        value.put(DAOFoodRecord.NOTES, record.getNote());

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
