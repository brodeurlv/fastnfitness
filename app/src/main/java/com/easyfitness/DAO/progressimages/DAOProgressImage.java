package com.easyfitness.DAO.progressimages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyfitness.DAO.DAOBase;
import com.easyfitness.DAO.ProgressImage;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Value;

import java.io.File;
import java.util.Date;

public class DAOProgressImage extends DAOBase {

    public static final String TABLE_NAME = "EFprogressImage";
    public static final String KEY = "_id";
    public static final String DATE = "date";
    public static final String IMAGE_FILE = "imageFile";
    public static final String PROFIL_KEY = "profil_id";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " DATE, " + IMAGE_FILE + " TEXT , " + PROFIL_KEY + " INTEGER);";


    public DAOProgressImage(Context context) {
        super(context);
    }

    public void addProgressImage(Date pDate, File image, long pProfileId) {
        SQLiteDatabase db = getWritableDatabase();
        addProgressImage(db, pDate, image, pProfileId);
    }

    public void addProgressImage(SQLiteDatabase db, Date pDate, File image, long pProfileId) {
        ContentValues value = new ContentValues();

        String dateString = DateConverter.dateToDBDateStr(pDate);
        value.put(DATE, dateString);
        value.put(IMAGE_FILE, image.getAbsolutePath());
        value.put(PROFIL_KEY, pProfileId);

        db.insert(TABLE_NAME, null, value);
    }

    public ProgressImage getImage(long profileId, int offset) {
        SQLiteDatabase db = getReadableDatabase();
        return getImage(db, profileId, offset);
    }

    public ProgressImage getImage(SQLiteDatabase db, long profileId, int offset) {
        String query = "SELECT " + KEY + ", " + IMAGE_FILE + ", " + DATE
                + " FROM " + TABLE_NAME + " WHERE "
                + PROFIL_KEY + " = " + profileId
                + " ORDER BY " + DATE + " DESC, " + KEY + " DESC"
                + " LIMIT 1 OFFSET ?";
        Cursor cursor = db.rawQuery(query, new String[]{Integer.toString(offset)});
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        ProgressImage re = new ProgressImage(
                cursor.getLong(0),
                cursor.getString(1),
                DateConverter.DBDateStrToDate(cursor.getString(2))
        );
        cursor.close();
        return re;
    }

    public int count(long profileId) {
        return count(getReadableDatabase(), profileId);
    }

    public int count(SQLiteDatabase db, long profileId) {
        Cursor cursor = db.rawQuery("SELECT COUNT(" + KEY + ") FROM " + TABLE_NAME +
                " WHERE " + PROFIL_KEY + " = ?", new String[]{Long.toString(profileId)});
        if (!cursor.moveToFirst()) {
            return 0;
        }
        return cursor.getInt(0);
    }

    public void deleteImage(SQLiteDatabase db, long imageId) {
        db.delete(TABLE_NAME, KEY + " = ?",
                new String[]{Long.toString(imageId)}
        );
    }

    public void deleteImage(long imageId) {
        SQLiteDatabase db = getWritableDatabase();
        deleteImage(db, imageId);
    }

    public void updateImageDate(ProgressImage image) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues value = new ContentValues();

        String dateString = DateConverter.dateToDBDateStr(image.getCreated());
        value.put(DATE, dateString);

        db.update(TABLE_NAME, value, KEY + " = ?", new String[]{Long.toString(image.getId())});
    }
}
