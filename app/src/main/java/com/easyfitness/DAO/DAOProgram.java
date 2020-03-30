package com.easyfitness.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class DAOProgram extends DAOBase{
    public static final String TABLE_NAME = "EFProgram";
    public static final String KEY = "_id";
    public static final String PROGRAM_NAME = "name";
    public static final String PROFIL_KEY = "profil_key";
    protected Context mContext;
    protected Cursor mCursor = null;

    private static final String TABLE_ARCHI = KEY + "," + PROGRAM_NAME + "," + PROFIL_KEY;

    public DAOProgram(Context context) {
        super(context);
    }

    /**
     * @param programName program name
     */
    public long addProgramRecord(String programName) {
        return this.addRecord(programName);
    }

    public long addRecord(String programName ) {
        ContentValues value = new ContentValues();
        long new_id = -1;
        //Test is Program exists. If not create it.
        DAOProgram daoProgram = new DAOProgram(mContext);
        if (daoProgram.programExists(programName)) {
            return -1;
        }
        value.put(DAOProgram.PROGRAM_NAME, programName);
        SQLiteDatabase db = open();
        new_id = db.insert(DAOProgram.TABLE_NAME, null, value);
        close();
        return new_id;
    }

    // Getting single value
    public Program getProgramRecord(long id) {
        String selectQuery = "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME + " WHERE " + KEY + "=" + id;
        List<Program> valueList = getRecordsList(selectQuery);
        if (valueList.isEmpty())
            return null;
        else
            return valueList.get(0);
    }

    public Program getProgramRecord(String programName) {
        String selectQuery = "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME + " WHERE " + PROGRAM_NAME + "=" + programName;
        List<Program> valueList = getRecordsList(selectQuery);
        if (valueList.isEmpty())
            return null;
        else
            return valueList.get(0);
    }

    public boolean programExists(String programName) {
        String selectQuery = "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME + " WHERE " + PROGRAM_NAME + "=" + programName;
        List<Program> valueList = getRecordsList(selectQuery);
        if (valueList.isEmpty())
            return false;
        else
            return true;
    }

    // Getting All Records
    private List<Program> getRecordsList(String pRequest) {

        List<Program> valueList = new ArrayList<>();
            // Select All Query
            SQLiteDatabase db = this.getReadableDatabase();
            mCursor = null;
            mCursor = db.rawQuery(pRequest, null);
            if (mCursor.moveToFirst()) {
                do {
                    Program value = new Program(
                        mCursor.getString(mCursor.getColumnIndex(DAOProgram.PROGRAM_NAME)),
                        mCursor.getLong(mCursor.getColumnIndex(DAOProgram.PROFIL_KEY))
                    );
                    valueList.add(value);
                } while (mCursor.moveToNext());
            }
            close();
            return valueList;
        }

    // Getting All Records
    public List<Program> getAllProgramRecords() {
        String selectQuery = "SELECT  " + TABLE_ARCHI + " FROM " + TABLE_NAME
//            + " WHERE "
            + " ORDER BY " + PROGRAM_NAME + " DESC";
        return getRecordsList(selectQuery);
    }

    public Cursor getAllPrograms() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY "
            + PROGRAM_NAME + " DESC";
        return getProgramListCursor(selectQuery);
    }

    /**
     * @return List of Machine object ordered by Favorite and Name
     */
    public Cursor getFilteredPrograms(CharSequence filterString) {
        // Select All Query
        // like '%"+inputText+"%'";
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + PROGRAM_NAME + " LIKE " + "'%" + filterString + "%' " + " ORDER BY "
//            + FAVORITES + " DESC,"
            + PROGRAM_NAME + " ASC";
        // return value list
        return getProgramListCursor(selectQuery);
    }

    private Cursor getProgramListCursor(String pRequest) {
        ArrayList<Program> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(pRequest, null);
    }


    // Updating single value
    public int updateRecord(Program m) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(DAOProgram.PROGRAM_NAME, m.getProgramName());
        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }
}
