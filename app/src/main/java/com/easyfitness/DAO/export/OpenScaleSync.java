package com.easyfitness.DAO.export;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.easyfitness.BuildConfig;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOProfile;
import com.easyfitness.DAO.DAOProfileWeight;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.ProfileWeight;
import com.easyfitness.DAO.ProgressImage;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.bodymeasures.DAOBodyPart;
import com.easyfitness.DAO.cardio.DAOOldCardio;
import com.easyfitness.DAO.program.DAOProgram;
import com.easyfitness.DAO.program.Program;
import com.easyfitness.DAO.progressimages.DAOProgressImage;
import com.easyfitness.DAO.record.DAOCardio;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.SettingsFragment;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.RecordType;
import com.easyfitness.enums.SizeUnit;
import com.easyfitness.enums.Unit;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.FileNameUtil;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.UnitConverter;
import com.easyfitness.utils.Value;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


// Uses http://javacsv.sourceforge.net/com/csvreader/CsvReader.html //
public class OpenScaleSync {
    private Context mContext;
    private Activity mActivity;
    final String APP_ID = "com.health.openscale";
    final String AUTHORITY = APP_ID + ".provider";
    final String REQUIRED_PERMISSION = APP_ID + ".READ_DATA";

    public OpenScaleSync(Context pContext, Activity pActivity) {
        mContext = pContext;
        mActivity = pActivity;
    }

    public boolean importDatabase() {
        boolean ret = true;

//        Uri metaUri = new Uri.Builder()
//                .scheme(ContentResolver.SCHEME_CONTENT)
//                .authority(AUTHORITY)
//                .path("meta")
//                .build();
        Uri usersUri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .path("users")
                .build();
        Uri measurementsUri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY)
                .path("measurements")
                .build();

        try {
//            Cursor cursor = mContext.getContentResolver().query(
//                    metaUri, null, null, null, null);

//            try {
//                while (cursor.moveToNext()) {
//                    Integer apiVersion = Integer.valueOf(cursor.getInt(cursor.getColumnIndex("apiVersion")));
//                    Integer versionCode = Integer.valueOf(cursor.getInt(cursor.getColumnIndex("versionCode")));
//                }
//            } finally {
//                cursor.close();
//            }

            Cursor cursor = mContext.getContentResolver().query(
                    usersUri, null, null, null, null);

            try {
                while (cursor.moveToNext()) {
                    Integer id = cursor.getInt(cursor.getColumnIndex("_ID"));
                    String username = cursor.getString(cursor.getColumnIndex("username"));
//                    Date birthday = DateConverter.DBDateStrToDate(cursor.getString(cursor.getColumnIndex("birthday")));
//                    Integer gender = cursor.getInt(cursor.getColumnIndex("gender"));
//                    Integer activityLevel = cursor.getInt(cursor.getColumnIndex("activityLevel"));
//                    Double bodyHeight = cursor.getDouble(cursor.getColumnIndex("bodyHeight"));
//                    Double measureUnit = cursor.getDouble(cursor.getColumnIndex("measureUnit"));
                    Unit defaultWeightUnit = SettingsFragment.getDefaultWeightUnit(mActivity).toUnit();
//                    Unit defaultDiestanceUnit = SettingsFragment.getDefaultDistanceUnit(mActivity).toUnit();
//                    Unit defaulSizeUnit = SettingsFragment.getDefaultSizeUnit(mActivity);


                    Cursor m = mContext.getContentResolver().query(
                            ContentUris.withAppendedId(measurementsUri, id),
                            null, null, null, null);

                    DAOProfile mDbProfiles = new DAOProfile(mContext);
                    Profile profile = mDbProfiles.getProfile(username);
                    long userId = profile.getId();


                    try {
                        while (m.moveToNext()) {
                            DAOBodyMeasure dbcWeight = new DAOBodyMeasure(mContext);
                            dbcWeight.open();

                            // Integer measurement_id = m.getInt(m.getColumnIndex("_ID"));
                            Date datetime = new Date(m.getLong(m.getColumnIndex("datetime")));
                            Float weight = m.getFloat(m.getColumnIndex("weight"));
                            Float fat = m.getFloat(m.getColumnIndex("fat"));
                            Float water = m.getFloat(m.getColumnIndex("water"));
                            Float muscle = m.getFloat(m.getColumnIndex("muscle"));

                            dbcWeight.addBodyMeasure(datetime, BodyPartExtensions.WEIGHT, new Value(weight, defaultWeightUnit), userId);
                            dbcWeight.addBodyMeasure(datetime, BodyPartExtensions.FAT, new Value(fat, Unit.PERCENTAGE),userId);
                            dbcWeight.addBodyMeasure(datetime, BodyPartExtensions.WATER, new Value(water, Unit.PERCENTAGE),userId);
                            dbcWeight.addBodyMeasure(datetime, BodyPartExtensions.MUSCLES, new Value(muscle, Unit.PERCENTAGE),userId);
                            dbcWeight.close();
                        }
                    } finally {
                        m.close();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        catch (Exception e) {
            ret = false;
        }
        return ret;
    }
}
