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
    public static final String BODYPART_RESID = "bodypart_id";
    public static final String CUSTOM_NAME = "custom_name";
    public static final String CUSTOM_PICTURE = "custom_picture";
    public static final String DISPLAY_ORDER = "display_order";
    public static final String TYPE = "type"; // Muscles or Body weight

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + BODYPART_RESID + " INTEGER, " + CUSTOM_NAME + " TEXT, " + CUSTOM_PICTURE + " TEXT, " + DISPLAY_ORDER + " INTEGER, " + TYPE + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    private Cursor mCursor = null;

    public DAOBodyPart(Context context) {
        super(context);
    }

    public long add(int pBodyPartId, String pCustomName, String pCustomPicture, int pDisplay, int pType) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        value.put(DAOBodyPart.BODYPART_RESID, pBodyPartId);
        value.put(DAOBodyPart.CUSTOM_NAME, pCustomName);
        value.put(DAOBodyPart.CUSTOM_PICTURE, pCustomPicture);
        value.put(DAOBodyPart.DISPLAY_ORDER, pDisplay);
        value.put(DAOBodyPart.TYPE, pType);

        long new_id = db.insert(DAOBodyPart.TABLE_NAME, null, value);
        db.close(); // Closing database connection
        return new_id;
    }

    // Getting single value
    public BodyPart getBodyPart(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        mCursor = null;
        mCursor = db.query(TABLE_NAME,
                new String[]{KEY, BODYPART_RESID, CUSTOM_NAME, CUSTOM_PICTURE, DISPLAY_ORDER, TYPE},
                KEY + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        BodyPart value = null;
        if (mCursor != null && mCursor.getCount() != 0) {
            mCursor.moveToFirst();

            value = new BodyPart(mCursor.getLong(mCursor.getColumnIndex(KEY)),
                    mCursor.getInt(mCursor.getColumnIndex(BODYPART_RESID)),
                    mCursor.getString(mCursor.getColumnIndex(CUSTOM_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(CUSTOM_PICTURE)),
                    mCursor.getInt(mCursor.getColumnIndex(DISPLAY_ORDER)),
                    mCursor.getInt(mCursor.getColumnIndex(TYPE))
            );
        }

        db.close();

        // return value
        return value;
    }

    public BodyPart getBodyPartfromBodyPartKey(long bodyPartKey) {
        SQLiteDatabase db = this.getWritableDatabase();

        mCursor = null;
        mCursor = db.query(TABLE_NAME,
                new String[]{KEY, BODYPART_RESID, CUSTOM_NAME, CUSTOM_PICTURE, DISPLAY_ORDER, TYPE},
                BODYPART_RESID + "=?",
                new String[]{String.valueOf(bodyPartKey)},
                null, null, null, null);
        BodyPart value = null;
        if (mCursor != null && mCursor.getCount() != 0) {
            mCursor.moveToFirst();

            value = new BodyPart(mCursor.getLong(mCursor.getColumnIndex(KEY)),
                    mCursor.getInt(mCursor.getColumnIndex(BODYPART_RESID)),
                    mCursor.getString(mCursor.getColumnIndex(CUSTOM_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(CUSTOM_PICTURE)),
                    mCursor.getInt(mCursor.getColumnIndex(DISPLAY_ORDER)),
                    mCursor.getInt(mCursor.getColumnIndex(TYPE))
            );
        }

        db.close();

        // return value
        return value;
    }

    // Getting All Measures
    public List<BodyPart> getList() {
        return getList("SELECT * FROM " + TABLE_NAME + " ORDER BY " + DISPLAY_ORDER + " ASC");
    }

    // Getting All Measures
    public List<BodyPart> getMusclesList() {
        return getList("SELECT * FROM " + TABLE_NAME + " WHERE " + TYPE + "=" + BodyPartExtensions.TYPE_MUSCLE + " ORDER BY " + DISPLAY_ORDER + " ASC");
    }

    // Getting All Measures
    private List<BodyPart> getList(String pRequest) {
        List<BodyPart> valueList = new ArrayList<>();
        // Select All Query

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                BodyPart value = new BodyPart(mCursor.getLong(mCursor.getColumnIndex(KEY)),
                        mCursor.getInt(mCursor.getColumnIndex(BODYPART_RESID)),
                        mCursor.getString(mCursor.getColumnIndex(CUSTOM_NAME)),
                        mCursor.getString(mCursor.getColumnIndex(CUSTOM_PICTURE)),
                        mCursor.getInt(mCursor.getColumnIndex(DISPLAY_ORDER)),
                        mCursor.getInt(mCursor.getColumnIndex(TYPE))
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
    public int update(BodyPart m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(DAOBodyPart.BODYPART_RESID, m.getBodyPartResKey());
        value.put(DAOBodyPart.CUSTOM_NAME, m.getCustomName());
        value.put(DAOBodyPart.CUSTOM_PICTURE, m.getCustomPicture());
        value.put(DAOBodyPart.DISPLAY_ORDER, m.getDisplayOrder());
        value.put(DAOBodyPart.TYPE, m.getType());

        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
                new String[]{String.valueOf(m.getId())});
    }

    // Deleting single Measure
    public void delete(long id) {
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

    /**
     * @return List of Machine object ordered by Favorite and Name
     */
    public void deleteAllEmptyBodyPart() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, BODYPART_RESID + "=? " + " AND " + CUSTOM_NAME + "=?",
                new String[]{"-1", ""});
        db.close();
    }

}


