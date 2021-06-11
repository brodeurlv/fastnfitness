package com.easyfitness.DAO.program;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DAOBase;
import com.easyfitness.DAO.Profile;
import com.easyfitness.enums.ProgramStatus;

import java.util.ArrayList;
import java.util.List;

public class DAOProgramHistory extends DAOBase {

    // Contacts table name
    public static final String TABLE_NAME = "EFworkoutHistory";

    public static final String KEY = "_id";
    public static final String PROGRAM_KEY = "program_key";
    public static final String PROFILE_KEY = "profile_key";
    public static final String STATUS = "description";
    public static final String START_DATE = "start_date";
    public static final String START_TIME = "start_time";
    public static final String END_DATE = "end_date";
    public static final String END_TIME = "end_time";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PROGRAM_KEY + " INTEGER, " +
            PROFILE_KEY + " INTEGER, " +
            STATUS + " INTEGER, " +
            START_DATE + " TEXT, " +
            START_TIME + " TEXT, " +
            END_DATE + " TEXT, " +
            END_TIME + " TEXT);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    private Cursor mCursor = null;

    public DAOProgramHistory(Context context) {
        super(context);
    }

    /**
     * @param m DBOProfil Profile a ajouter a la base
     */
    public long add(ProgramHistory m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DAOProgramHistory.PROGRAM_KEY, m.getProgramId());
        value.put(DAOProgramHistory.PROFILE_KEY, m.getProfileId());
        value.put(DAOProgramHistory.START_DATE, m.getStartDate());
        value.put(DAOProgramHistory.START_TIME, m.getStartTime());
        value.put(DAOProgramHistory.END_DATE, m.getEndDate());
        value.put(DAOProgramHistory.END_TIME, m.getEndTime());
        value.put(DAOProgramHistory.STATUS, m.getStatus().ordinal());

        long new_id = db.insert(DAOProgramHistory.TABLE_NAME, null, value);

        close();
        return new_id;
    }

    /**
     * @param id long id of the Profile
     */
    public ProgramHistory get(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (mCursor != null) mCursor.close();
        mCursor = null;
        mCursor = db.query(TABLE_NAME,
                new String[]{KEY, PROGRAM_KEY, PROFILE_KEY, STATUS, START_DATE, START_TIME, END_DATE, END_TIME},
                KEY + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();

            ProgramHistory value = new ProgramHistory(mCursor.getLong(mCursor.getColumnIndex(DAOProgramHistory.KEY)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOProgramHistory.PROGRAM_KEY)),
                    mCursor.getInt(mCursor.getColumnIndex(DAOProgramHistory.PROFILE_KEY)),
                    ProgramStatus.fromInteger(mCursor.getInt(mCursor.getColumnIndex(DAOProgramHistory.STATUS))),
                    mCursor.getString(mCursor.getColumnIndex(DAOProgramHistory.START_DATE)),
                    mCursor.getString(mCursor.getColumnIndex(DAOProgramHistory.START_TIME)),
                    mCursor.getString(mCursor.getColumnIndex(DAOProgramHistory.END_DATE)),
                    mCursor.getString(mCursor.getColumnIndex(DAOProgramHistory.END_TIME))
            );
            mCursor.close();
            close();

            // return value
            return value;
        } else {
            mCursor.close();
            close();
            return null;
        }
    }

    // Getting All Profils
    public List<ProgramHistory> getList(String pRequest) {
        List<ProgramHistory> valueList = new ArrayList<>();
        // Select All Query

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                ProgramHistory value = new ProgramHistory(mCursor.getLong(mCursor.getColumnIndex(DAOProgramHistory.KEY)),
                        mCursor.getInt(mCursor.getColumnIndex(DAOProgramHistory.PROGRAM_KEY)),
                        mCursor.getInt(mCursor.getColumnIndex(DAOProgramHistory.PROFILE_KEY)),
                        ProgramStatus.fromInteger(mCursor.getInt(mCursor.getColumnIndex(DAOProgramHistory.STATUS))),
                        mCursor.getString(mCursor.getColumnIndex(DAOProgramHistory.START_DATE)),
                        mCursor.getString(mCursor.getColumnIndex(DAOProgramHistory.START_TIME)),
                        mCursor.getString(mCursor.getColumnIndex(DAOProgramHistory.END_DATE)),
                        mCursor.getString(mCursor.getColumnIndex(DAOProgramHistory.END_TIME))
                );

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }

        close();
        // return value list
        return valueList;
    }

    public Cursor getCursor(String pRequest) {
        // Select All Query
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(pRequest, null);
    }

    // Getting All
    public List<ProgramHistory> getAll() {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + KEY + " DESC";

        // return value list
        return getList(selectQuery);
    }

    // Getting All filtered
    public Cursor getFilteredHistoryCursor(long p_programKey, long p_profileKey) {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + PROGRAM_KEY + "=" + p_programKey + " AND " + PROFILE_KEY + "=" + p_profileKey + " ORDER BY " + KEY + " DESC";

        // return value list
        return getCursor(selectQuery);
    }

    // Getting All
    public ProgramHistory getRunningProgram(Profile profile) {
        String selectQuery = "";
        // Select All Query
        if (profile == null)
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + STATUS + "=" + ProgramStatus.RUNNING.ordinal() + " ORDER BY " + KEY + " DESC";
        else
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + STATUS + "=" + ProgramStatus.RUNNING.ordinal() + " AND " + PROFILE_KEY + "=" + profile.getId() + " ORDER BY " + KEY + " DESC";

        List<ProgramHistory> list = getList(selectQuery);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    // Updating single value
    public int update(ProgramHistory m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(DAOProgramHistory.PROGRAM_KEY, m.getProgramId());
        value.put(DAOProgramHistory.PROFILE_KEY, m.getProfileId());
        value.put(DAOProgramHistory.START_DATE, m.getStartDate());
        value.put(DAOProgramHistory.START_TIME, m.getStartTime());
        value.put(DAOProgramHistory.END_DATE, m.getEndDate());
        value.put(DAOProgramHistory.END_TIME, m.getEndTime());
        value.put(DAOProgramHistory.STATUS, m.getStatus().ordinal());

        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
                new String[]{String.valueOf(m.getId())});
    }

    // Deleting single Profile
    public void delete(ProgramHistory m) {
        delete(m.getId());
    }

    // Deleting single Profile
    public void delete(long id) {
        open();

        // Should delete the Workout template

        // Delete the Workout
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?",
                new String[]{String.valueOf(id)});

        close();
    }


    // Getting Profils Count
    public int getCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        open();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int value = cursor.getCount();
        cursor.close();
        close();

        // return count
        return value;
    }

    /* DEBUG ONLY */
    public void populate() {
        /*WorkoutHistory m = new WorkoutHistory(0,"Template 1", "Description 1");
        this.add(m);
        m = new WorkoutHistory(0,"Template 2", "Description 2");
        this.add(m);*/
    }
}
