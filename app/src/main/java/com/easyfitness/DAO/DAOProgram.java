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
    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
        + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PROGRAM_NAME
        + " TEXT, " + PROFIL_KEY + " INTEGER);";
    protected Context mContext;
    protected Cursor mCursor = null;

    public DAOProgram(Context context) {
        super(context);
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
        value.put(DAOProgram.PROFIL_KEY, 1);
        SQLiteDatabase db = open();
        new_id = db.insert(DAOProgram.TABLE_NAME, null, value);
        close();
        return new_id;
    }

    public boolean programExists(String programName) {
        Program lMach = getRecord(programName);
        return lMach != null;
    }

    public Program getRecord(String pName) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.query(TABLE_NAME, new String[]{KEY, PROGRAM_NAME, PROFIL_KEY}, PROGRAM_NAME + "=?",
            new String[]{pName}, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        if (mCursor.getCount() == 0)
            return null;

        Program value = new Program(
            mCursor.getString(1),
            mCursor.getLong(2));

        value.setId(mCursor.getLong(0));
        // return value
        mCursor.close();
        close();
        return value;
    }

    public Program getRecord(Long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.query(TABLE_NAME, new String[]{KEY, PROGRAM_NAME,PROFIL_KEY}, KEY + "=?",
            new String[]{String.valueOf(id)}, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        if (mCursor.getCount() == 0)
            return null;

        Program value = new Program(mCursor.getString(1), mCursor.getLong(2));

        value.setId(mCursor.getLong(0));
        // return value
        mCursor.close();
        close();
        return value;
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

    public Cursor getAllPrograms() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY "
            + PROGRAM_NAME + " ASC";
        return getProgramListCursor(selectQuery);
    }

    /**
     * @return List of Machine object ordered by Favorite and Name
     */
    public Cursor getFilteredPrograms(CharSequence filterString) {
        // Select All Query
        // like '%"+inputText+"%'";
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + PROGRAM_NAME + " LIKE " + "'%" + filterString + "%' " + " ORDER BY "
            + PROGRAM_NAME + " ASC";
        return getProgramListCursor(selectQuery);
    }

    private Cursor getProgramListCursor(String pRequest) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(pRequest,null);
    }

    public int updateRecord(Program m) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put(DAOProgram.PROGRAM_NAME, m.getProgramName());
        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }

    public void delete(Program m) {
        if (m != null) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, KEY + " = ?",
                new String[]{String.valueOf(m.getId())});
            db.close();
        }
    }
}
