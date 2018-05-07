package com.easyfitness.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DAOFavorites extends DAOBase {

    // Contacts table name
    public static final String TABLE_NAME = "EFfavorites";

    public static final String KEY = "_id";
    public static final String MACHINE_KEY = "machine_id";
    public static final String PROFIL_KEY = "profil_id";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + PROFIL_KEY + " INTEGER, " + MACHINE_KEY + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    private Profile mProfile = null;
    private Cursor mCursor = null;

    public DAOFavorites(Context context, Profile pProfile) {
        super(context);
        mProfile = pProfile;
    }

    public void setProfil(Profile pProfile) {
        mProfile = pProfile;
    }

    /**
     * @param m machine to be set favorite
     * @param p profile associated with the measure
     */
    public void addFavorite(Machine m, Profile p) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DAOFavorites.MACHINE_KEY, m.getId());
        value.put(DAOFavorites.PROFIL_KEY, p.getId());

        db.insert(DAOFavorites.TABLE_NAME, null, value);
        db.close(); // Closing database connection
    }


    // Deleting single favourite
    public void deleteFavorite(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, MACHINE_KEY + " = ? AND " + PROFIL_KEY + " = ?",
                new String[]{String.valueOf(id), String.valueOf(mProfile.getId())});
    }

    // Getting All Measures
    private List<Long> getFavoritesList(String pRequest) {
        List<Long> valueList = new ArrayList<Long>();
        // Select All Query
        String selectQuery = pRequest;

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                // Adding value to list
                valueList.add(mCursor.getLong(0));
            } while (mCursor.moveToNext());
        }

        // return value list
        return valueList;
    }

    public Cursor GetCursor() {
        return mCursor;
    }

    // Getting All Measures
    public List<Long> getFavoriteList(Profile pProfile) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.getId();

        // return value list
        return getFavoritesList(selectQuery);
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
}


