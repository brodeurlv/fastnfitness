package com.easyfitness.DAO.program;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DAOBase;

import java.util.ArrayList;
import java.util.List;

public class DAOProgram extends DAOBase {

    // Contacts table name
    public static final String TABLE_NAME = "EFworkout";

    public static final String KEY = "_id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " TEXT, " + DESCRIPTION + " TEXT);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    private Cursor mCursor = null;

    public DAOProgram(Context context) {
        super(context);
    }

    /**
     * @param m Program to add to the database
     */
    public long add(Program m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DAOProgram.NAME, m.getName());
        value.put(DAOProgram.DESCRIPTION, m.getDescription());

        long new_id = db.insert(DAOProgram.TABLE_NAME, null, value);

        close();
        return new_id;
    }

    /**
     * @param id long id of the Program
     */
    public Program get(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (mCursor != null) mCursor.close();
        mCursor = null;
        mCursor = db.query(TABLE_NAME,
                new String[]{KEY, NAME, DESCRIPTION},
                KEY + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();

            Program value = new Program(mCursor.getLong(mCursor.getColumnIndex(DAOProgram.KEY)),
                    mCursor.getString(mCursor.getColumnIndex(DAOProgram.NAME)),
                    mCursor.getString(mCursor.getColumnIndex(DAOProgram.DESCRIPTION))
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

    // Getting All Programs
    public List<Program> getList(String pRequest) {
        List<Program> valueList = new ArrayList<>();
        // Select All Query

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                Program value = new Program(mCursor.getLong(mCursor.getColumnIndex(DAOProgram.KEY)),
                        mCursor.getString(mCursor.getColumnIndex(DAOProgram.NAME)),
                        mCursor.getString(mCursor.getColumnIndex(DAOProgram.DESCRIPTION))
                );

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }

        close();
        // return value list
        return valueList;
    }

    // Getting All
    public List<Program> getAll() {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + NAME + " ASC";

        // return value list
        return getList(selectQuery);
    }

    // Updating single program
    public int update(Program m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(DAOProgram.NAME, m.getName());
        value.put(DAOProgram.DESCRIPTION, m.getDescription());

        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
                new String[]{String.valueOf(m.getId())});
    }

    /**
     * @param m Program to delete
     */
    public void delete(Program m) {
        delete(m.getId());
    }

    /**
     * @param id id of Program to delete
     */
    public void delete(long id) {
        open();

        // Should delete the Workout template

        // Delete the Workout
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?",
                new String[]{String.valueOf(id)});

        close();
    }


    // Getting Programs Count
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
        Program m = new Program(0, "Template 1", "Description 1");
        this.add(m);
        m = new Program(0, "Template 2", "Description 2");
        this.add(m);
    }

    /**
     * Delete all empty workouts
     */
    public void deleteAllEmptyWorkout() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, NAME + "=?",
                new String[]{""});
        db.close();
    }
}
