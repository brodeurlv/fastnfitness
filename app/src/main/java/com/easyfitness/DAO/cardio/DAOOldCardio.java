package com.easyfitness.DAO.cardio;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.easyfitness.DAO.DAOBase;
import com.easyfitness.DAO.DAOProfile;
import com.easyfitness.DAO.DAOUtils;
import com.easyfitness.DAO.Profile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAOOldCardio extends DAOBase {

    // Contacts table name
    public static final String TABLE_NAME = "EFcardio";

    public static final String KEY = "_id";
    public static final String DATE = "date";
    public static final String EXERCICE = "exercice";
    public static final String DISTANCE = "distance";
    public static final String DURATION = "duration";
    public static final String PROFIL_KEY = "profil_id";
    public static final String NOTES = "notes";
    public static final String DISTANCE_UNIT = "distance_unit";
    public static final String VITESSE = "vitesse";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
            + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DATE + " DATE, "
            + EXERCICE + " TEXT, "
            + DISTANCE + " FLOAT, "
            + DURATION + " INTEGER, "
            + PROFIL_KEY + " INTEGER, "
            + NOTES + " TEXT, "
            + DISTANCE_UNIT + " TEXT, "
            + VITESSE + " FLOAT);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

    private Cursor mCursor = null;
    private Context mContext = null;

    public DAOOldCardio(Context context) {
        super(context);
        mContext = context;
    }

    // Getting All Records
    private List<OldCardio> getRecordsList(String pRequest) {
        List<OldCardio> valueList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Select All Query

        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                //Get Date
                Date date;
                try {
                    date = new SimpleDateFormat(DAOUtils.DATE_FORMAT).parse(mCursor.getString(mCursor.getColumnIndex(DAOOldCardio.DATE)));
                } catch (ParseException e) {
                    e.printStackTrace();
                    date = new Date();
                }

                // Get Profile
                DAOProfile lDAOProfile = new DAOProfile(mContext);
                Profile lProfile = lDAOProfile.getProfile(mCursor.getLong(mCursor.getColumnIndex(DAOOldCardio.PROFIL_KEY)));

                OldCardio value = new OldCardio(date,
                        mCursor.getString(mCursor.getColumnIndex(DAOOldCardio.EXERCICE)),
                        mCursor.getFloat(mCursor.getColumnIndex(DAOOldCardio.DISTANCE)),
                        mCursor.getLong(mCursor.getColumnIndex(DAOOldCardio.DURATION)),
                        lProfile);

                value.setId(Long.parseLong(mCursor.getString(mCursor.getColumnIndex(DAOOldCardio.KEY))));

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }
        // return value list
        return valueList;
    }

    // Getting All Records
    public List<OldCardio> getAllRecords() {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY "
                + KEY + " DESC";

        // return value list
        return getRecordsList(selectQuery);
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

    public boolean tableExists() {
        boolean isExist = true;
        Cursor res;

        SQLiteDatabase db = this.getReadableDatabase();
        try {
            res = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            res.close();
        } catch (SQLiteException e) {
            isExist = false;
        }
        return isExist;
    }

    public boolean dropTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(TABLE_DROP);
        return true;
    }
}
