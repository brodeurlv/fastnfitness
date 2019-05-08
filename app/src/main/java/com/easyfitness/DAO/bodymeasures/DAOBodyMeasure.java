package com.easyfitness.DAO.bodymeasures;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DAOBase;
import com.easyfitness.DAO.DAOUtils;
import com.easyfitness.DAO.Profile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DAOBodyMeasure extends DAOBase {

    // Contacts table name
    public static final String TABLE_NAME = "EFbodymeasures";

    public static final String KEY = "_id";
    public static final String BODYPART_KEY = "bodypart_id";
    public static final String MEASURE = "mesure";
    public static final String DATE = "date";
    public static final String UNIT = "unit";
    public static final String PROFIL_KEY = "profil_id";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " DATE, " + BODYPART_KEY + " INTEGER, " + MEASURE + " REAL , " + PROFIL_KEY + " INTEGER, " + UNIT + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    private Profile mProfile = null;
    private Cursor mCursor = null;

    public DAOBodyMeasure(Context context) {
        super(context);
    }

    public void setProfil(Profile pProfile) {
        mProfile = pProfile;
    }

    /**
     * @param pDate           date of the weight measure
     * @param pBodymeasure_id id of the body part
     * @param pMeasure        body measure
     * @param pProfileID      profil associated with the measure
     */
    public void addBodyMeasure(Date pDate, int pBodymeasure_id, float pMeasure, long pProfileID) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        value.put(DAOBodyMeasure.DATE, dateFormat.format(pDate));
        value.put(DAOBodyMeasure.BODYPART_KEY, pBodymeasure_id);
        value.put(DAOBodyMeasure.MEASURE, pMeasure);
        value.put(DAOBodyMeasure.PROFIL_KEY, pProfileID);

        db.insert(DAOBodyMeasure.TABLE_NAME, null, value);
        db.close(); // Closing database connection
    }

    // Getting single value
    private BodyMeasure getMeasure(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.query(TABLE_NAME,
            new String[]{KEY, DATE, BODYPART_KEY, MEASURE, PROFIL_KEY},
            KEY + "=?",
            new String[]{String.valueOf(id)},
            null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        Date date;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = dateFormat.parse(mCursor.getString(1));
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }

        BodyMeasure value = new BodyMeasure(mCursor.getLong(0),
            date,
            mCursor.getInt(2),
            mCursor.getFloat(3),
            mCursor.getLong(4)
        );

        db.close();

        // return value
        return value;
    }

    // Getting All Measures
    private List<BodyMeasure> getMeasuresList(String pRequest) {
        List<BodyMeasure> valueList = new ArrayList<>();
        // Select All Query

        SQLiteDatabase db = this.getReadableDatabase();
        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                Date date;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    date = dateFormat.parse(mCursor.getString(1));
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = new Date();
                }

                BodyMeasure value = new BodyMeasure(mCursor.getLong(0),
                    date,
                    mCursor.getInt(2),
                    mCursor.getFloat(3),
                    mCursor.getLong(4)
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

    /**
     * Getting All Measures associated to a Body part for a specific Profile
     *
     * @param pBodyPartID
     * @param pProfile
     * @return List<BodyMeasure>
     */
    public List<BodyMeasure> getBodyPartMeasuresList(long pBodyPartID, Profile pProfile) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_KEY + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.getId() + " GROUP BY " + DATE + " ORDER BY date(" + DATE + ") DESC";

        // return value list
        return getMeasuresList(selectQuery);
    }

    /**
     * Getting All Measures for a specific Profile
     *
     * @param pProfile
     * @return List<BodyMeasure>
     */
    public List<BodyMeasure> getBodyMeasuresList(Profile pProfile) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC";

        // return value list
        return getMeasuresList(selectQuery);
    }

    /**
     * Getting All Measures associated to a Body part for a specific Profile
     *
     * @param pBodyPartID
     * @param pProfile
     * @return List<BodyMeasure>
     */
    public BodyMeasure getLastBodyMeasures(long pBodyPartID, Profile pProfile) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_KEY + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.getId() + " GROUP BY " + DATE + " ORDER BY date(" + DATE + ") DESC";

        List<BodyMeasure> array = getMeasuresList(selectQuery);
        if (array.size() <= 0) {
            return null;
        }

        // return value list
        return getMeasuresList(selectQuery).get(0);
    }

    // Updating single value
    public int updateMeasure(BodyMeasure m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateString = dateFormat.format(m.getDate());
        value.put(DAOBodyMeasure.DATE, dateString);
        value.put(DAOBodyMeasure.BODYPART_KEY, m.getBodyPartID());
        value.put(DAOBodyMeasure.MEASURE, m.getBodyMeasure());
        value.put(DAOBodyMeasure.PROFIL_KEY, m.getProfileID());

        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }

    // Deleting single Measure
    public void deleteMeasure(long id) {
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

    public void populate() {
        Date date = new Date();
        int poids = 10;

        for (int i = 1; i <= 5; i++) {
            date.setTime(date.getTime() + i * 1000 * 60 * 60 * 24 * 2);
            //addBodyMeasure(date, (float) i, mProfile);
        }
    }
}


