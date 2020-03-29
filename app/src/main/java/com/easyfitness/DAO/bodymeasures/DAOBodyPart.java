package com.easyfitness.DAO.bodymeasures;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DAOBase;

import java.util.ArrayList;
import java.util.List;

public class DAOBodyPart extends DAOBase {

    // Contacts table name
    public static final String TABLE_NAME = "EFbodyparts";

    public static final String KEY = "_id";
    public static final String NAME_RES = "resource_name";
    public static final String CUSTOM_NAME = "custom_name";
    public static final String PICTURE_RES = "resource_picture";
    public static final String CUSTOM_PICTURE = "custom_picture";
    public static final String DISPLAY_ORDER = "display_order";
    public static final String TYPE = "type"; // Muscles or Body weight

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME_RES + " INTEGER, " + CUSTOM_NAME + " STRING, " + PICTURE_RES + " INTEGER , " + CUSTOM_PICTURE + " STRING, "+ DISPLAY_ORDER + " INTEGER, " + TYPE + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    private Cursor mCursor = null;

    public DAOBodyPart(Context context) {
        super(context);
    }

    public void addBodyPart(long pNameResource, String pCustomName, long pPictureResource, String pCustomPicture, int pDisplay, int pType) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DAOBodyPart.NAME_RES, pNameResource);
        value.put(DAOBodyPart.PICTURE_RES, pPictureResource);
        value.put(DAOBodyPart.CUSTOM_NAME, pCustomName);
        value.put(DAOBodyPart.CUSTOM_PICTURE, pCustomPicture);
        value.put(DAOBodyPart.DISPLAY_ORDER, pDisplay);
        value.put(DAOBodyPart.TYPE, pType);

        db.insert(DAOBodyPart.TABLE_NAME, null, value);
        db.close(); // Closing database connection
    }

    // Getting single value
    public BodyPart getBodyPart(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.query(TABLE_NAME,
            new String[]{KEY, NAME_RES, PICTURE_RES, CUSTOM_NAME, CUSTOM_PICTURE, DISPLAY_ORDER, TYPE},
            KEY + "=?",
            new String[]{String.valueOf(id)},
            null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        BodyPart value = new BodyPart(mCursor.getLong(mCursor.getColumnIndex(this.KEY)),
            mCursor.getLong(mCursor.getColumnIndex(this.NAME_RES)),
            mCursor.getLong(mCursor.getColumnIndex(this.PICTURE_RES)),
            mCursor.getString(mCursor.getColumnIndex(this.CUSTOM_NAME)),
            mCursor.getString(mCursor.getColumnIndex(this.CUSTOM_PICTURE)),
            mCursor.getInt(mCursor.getColumnIndex(this.DISPLAY_ORDER)),
            mCursor.getInt(mCursor.getColumnIndex(this.TYPE))
        );

        db.close();

        // return value
        return value;
    }

    // Getting All Measures
    public List<BodyPart> getBodyPartList() {
        return getBodyPartList("SELECT * FROM " + TABLE_NAME  + " ORDER BY " + DISPLAY_ORDER + " ASC");
    }

    // Getting All Measures
    private List<BodyPart> getBodyPartList(String pRequest) {
        List<BodyPart> valueList = new ArrayList<>();
        // Select All Query

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                BodyPart value = new BodyPart(mCursor.getLong(mCursor.getColumnIndex(this.KEY)),
                    mCursor.getLong(mCursor.getColumnIndex(this.NAME_RES)),
                    mCursor.getLong(mCursor.getColumnIndex(this.PICTURE_RES)),
                    mCursor.getString(mCursor.getColumnIndex(this.CUSTOM_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(this.CUSTOM_PICTURE)),
                    mCursor.getInt(mCursor.getColumnIndex(this.DISPLAY_ORDER)),
                    mCursor.getInt(mCursor.getColumnIndex(this.TYPE))
                );

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }

        // return value list
        return valueList;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    // Updating single value
    public int updateBodyPart(BodyPart m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(DAOBodyPart.NAME_RES, m.getNameRes());
        value.put(DAOBodyPart.PICTURE_RES, m.getPictureRes());
        value.put(DAOBodyPart.CUSTOM_NAME, m.getCustomName());
        value.put(DAOBodyPart.CUSTOM_PICTURE, m.getCustomPicture());
        value.put(DAOBodyPart.DISPLAY_ORDER, m.getDisplayOrder());
        value.put(DAOBodyPart.TYPE, m.getType());

        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }

    // Deleting single Measure
    public void deleteBodyPart(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?",
            new String[]{String.valueOf(id)});
    }

    // Getting Profils Count
    public int getCount() {
        String countQuery = "SELECT * FROM " + TABLE_NAME;
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


