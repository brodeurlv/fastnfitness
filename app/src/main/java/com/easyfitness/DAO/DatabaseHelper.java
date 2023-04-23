package com.easyfitness.DAO;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.preference.PreferenceManager;

import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.bodymeasures.DAOBodyPart;
import com.easyfitness.DAO.program.DAOProgram;
import com.easyfitness.DAO.program.DAOProgramHistory;
import com.easyfitness.DAO.progressimages.DAOProgressImage;
import com.easyfitness.DAO.record.DAOFonte;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.Muscle;
import com.easyfitness.enums.RecordType;
import com.easyfitness.enums.Unit;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 26;
    public static final String OLD09_DATABASE_NAME = "easyfitness";
    public static final String DATABASE_NAME = "easyfitness.db";
    private static DatabaseHelper sInstance;
    private Context mContext = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public static void renameOldDatabase(Activity activity) {
        File oldDatabaseFile = activity.getDatabasePath(OLD09_DATABASE_NAME);
        if (oldDatabaseFile.exists()) {
            File newDatabaseFile = new File(oldDatabaseFile.getParentFile(), DATABASE_NAME);
            oldDatabaseFile.renameTo(newDatabaseFile);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DAORecord.TABLE_CREATE); // Covers Fonte and Cardio and Static
        db.execSQL(DAOProfile.TABLE_CREATE);
        db.execSQL(DAOProfileWeight.TABLE_CREATE);
        db.execSQL(DAOMachine.TABLE_CREATE);
        db.execSQL(DAOBodyMeasure.TABLE_CREATE);
        db.execSQL(DAOBodyPart.TABLE_CREATE);
        db.execSQL(DAOProgram.TABLE_CREATE);
        db.execSQL(DAOProgramHistory.TABLE_CREATE);
        db.execSQL(DAOProgressImage.TABLE_CREATE);
        initBodyPartTable(db);
    }

    @Override
    public void onUpgrade(
            final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 1:
                    //NOT SUPPORTED ANYMORE
                    //db.execSQL(DAOCardio.TABLE_CREATE);
                    break;
                case 2:
                    //NOT SUPPORTED ANYMOREdb.execSQL(DAOCardio.TABLE_CREATE);
                    break;
                case 3:
                    //NOT SUPPORTED ANYMOREdb.execSQL(DAOCardio.TABLE_DROP);
                    //NOT SUPPORTED ANYMOREdb.execSQL(DAOCardio.TABLE_CREATE);
                    break;
                case 4: // Easyfitness 0.7
                    db.execSQL("ALTER TABLE " + DAOFonte.TABLE_NAME + " ADD COLUMN " + DAOFonte.NOTES + " TEXT");
                    db.execSQL("ALTER TABLE " + DAOFonte.TABLE_NAME + " ADD COLUMN " + DAOFonte.WEIGHT_UNIT + " INTEGER DEFAULT 0");
                    break;
                case 5:
                    db.execSQL(DAOMachine.TABLE_CREATE_5);
                    db.execSQL("ALTER TABLE " + DAOFonte.TABLE_NAME + " ADD COLUMN " + DAOFonte.EXERCISE_KEY + " INTEGER");
                    break;
                case 6: // Easyfitness 0.8
                    if (!FieldExists(db, DAOMachine.TABLE_NAME, DAOMachine.BODYPARTS)) // Easyfitness 0.9 : Probleme d'upgrade
                        db.execSQL("ALTER TABLE " + DAOMachine.TABLE_NAME + " ADD COLUMN " + DAOMachine.BODYPARTS + " TEXT");
                    break;
                case 7: // Easyfitness 0.10
                    db.execSQL("ALTER TABLE " + DAOMachine.TABLE_NAME + " ADD COLUMN " + DAOMachine.PICTURE + " TEXT");
                    break;
                case 8: // Easyfitness 0.12
                    db.execSQL("ALTER TABLE " + DAOFonte.TABLE_NAME + " ADD COLUMN " + DAOFonte.TIME + " TEXT");
                    break;
                case 9: // Easyfitness 0.13
                    db.execSQL(DAOBodyMeasure.TABLE_CREATE);
                    break;
                case 10: // Easyfitness 0.13 BIS
                    db.execSQL("ALTER TABLE " + DAOMachine.TABLE_NAME + " ADD COLUMN " + DAOMachine.FAVORITES + " INTEGER");
                    break;
                case 11: // FastnFitness 0.13.3 - Changed Poids from Integer to Real
                    // Renomme la table FONTE en table temporaire
                    db.execSQL("ALTER TABLE " + DAOFonte.TABLE_NAME + " RENAME TO tmp_table_name");
                    // Cree la nouvelle table FONTE
                    db.execSQL(DAOFonte.TABLE_CREATE);
                    // Copie les infos de l'ancienne vers la nouvelle
                    db.execSQL("INSERT INTO " + DAOFonte.TABLE_NAME + " SELECT * FROM tmp_table_name");
                    // do not delete old table here in case of issue
                    break;
                case 12:
                    // Delete old table table
                    db.execSQL("DROP TABLE IF EXISTS tmp_table_name");
                    break;
                case 13:
                    // Update profile database
                    db.execSQL("ALTER TABLE " + DAOProfile.TABLE_NAME + " ADD COLUMN " + DAOProfile.SIZE + " INTEGER");
                    db.execSQL("ALTER TABLE " + DAOProfile.TABLE_NAME + " ADD COLUMN " + DAOProfile.BIRTHDAY + " DATE");
                    break;
                case 14:
                    db.execSQL("ALTER TABLE " + DAOProfile.TABLE_NAME + " ADD COLUMN " + DAOProfile.PHOTO + " TEXT");
                    break;
                case 15:
                    // Merge of Cardio DB and Fonte DB
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.DISTANCE + " REAL");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.DURATION + " INTEGER");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.EXERCISE_TYPE + " INTEGER DEFAULT " + ExerciseType.STRENGTH.ordinal());
                    break;
                case 16:
                    db.execSQL("ALTER TABLE " + DAOBodyMeasure.TABLE_NAME + " ADD COLUMN " + DAOBodyMeasure.UNIT + " INTEGER");
                    migrateWeightTable(db);
                    break;
                case 17:
                    db.execSQL("ALTER TABLE " + DAOProfile.TABLE_NAME + " ADD COLUMN " + DAOProfile.GENDER + " INTEGER");
                    break;
                case 18:
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.SECONDS + " INTEGER DEFAULT 0");
                    break;
                case 19:
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.DISTANCE_UNIT + " INTEGER DEFAULT 0");
                    break;
                case 20:
                    db.execSQL(DAOBodyPart.TABLE_CREATE);
                    initBodyPartTable(db);
                    break;
                case 21:
                    db.execSQL(DAOProgram.TABLE_CREATE);
                    db.execSQL(DAOProgramHistory.TABLE_CREATE);

                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.RECORD_TYPE + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.PROGRAM_KEY + " INTEGER DEFAULT -1");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.TEMPLATE_RECORD_KEY + " INTEGER DEFAULT -1");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.PROGRAM_SESSION_KEY + " INTEGER DEFAULT -1");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.TEMPLATE_REST_TIME + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.TEMPLATE_ORDER + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.TEMPLATE_RECORD_STATUS + " INTEGER DEFAULT 3");
                    break;
                case 22:
                    // update all unitless bodymeasures based on BODYPART_ID
                    upgradeBodyMeasureUnits(db);
                    break;
                case 23:
                    long sizeBodyPartId = addInitialBodyPart(db, BodyPartExtensions.SIZE, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
                    DAOProfile daoProfile = new DAOProfile(mContext);
                    DAOBodyMeasure daoBodyMeasure = new DAOBodyMeasure(mContext);

                    // Get Size unit preference
                    SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mContext);
                    String defaultSizeUnitString = SP.getString("defaultSizeUnit", String.valueOf(Unit.CM.ordinal()));
                    int defaultSizeUnitInteger;
                    try {
                        defaultSizeUnitInteger = Integer.parseInt(defaultSizeUnitString);
                    } catch (NumberFormatException e) {
                        defaultSizeUnitInteger = Unit.CM.ordinal();
                    }
                    Unit defaultSizeUnit = Unit.fromInteger(defaultSizeUnitInteger);

                    List<Profile> profileList = daoProfile.getAllProfiles(db);
                    for (Profile profile:profileList) {
                        daoBodyMeasure.addBodyMeasure(db, DateConverter.getNewDate(), sizeBodyPartId, new Value((float) profile.getSize(), defaultSizeUnit), profile.getId());
                    }
                    break;
                case 24:
                    updateMusclesToUseNewIds(db);
                    break;
                case 25:
                    // Add all new template fields
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.TEMPLATE_SETS + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.TEMPLATE_REPS + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.TEMPLATE_WEIGHT + " REAL DEFAULT 0");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.TEMPLATE_WEIGHT_UNIT + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.TEMPLATE_DISTANCE + " REAL DEFAULT 0");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.TEMPLATE_DISTANCE_UNIT + " INTEGER DEFAULT 0");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.TEMPLATE_DURATION + " TEXT");
                    db.execSQL("ALTER TABLE " + DAORecord.TABLE_NAME + " ADD COLUMN " + DAORecord.TEMPLATE_SECONDS + " INTEGER DEFAULT 0");
                    // Copy current value from current templates
                    copyTemplateValues(db);
                    break;
                case 26:
                    db.execSQL(DAOProgressImage.TABLE_CREATE);
                    break;
            }
            upgradeTo++;
        }
    }

    private void updateMusclesToUseNewIds(SQLiteDatabase db) {
        List<Machine> machines = new DAOMachine(mContext).getAllMachinesUsingDb(db);
        for (Machine machine : machines) {
            updateMachineToUseNewId(machine, db);
        }
    }

    private void updateMachineToUseNewId(Machine machine, SQLiteDatabase db) {
        Set<Muscle> usedMuscles = Muscle.setFromBodyParts(machine.getBodyParts(), mContext.getResources());
        machine.setBodyParts(Muscle.migratedBodyPartStringFor(usedMuscles));
        new DAOMachine(mContext).updateMachineUsingDb(machine, db);
    }

    @Override
    public void onDowngrade(
            final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
        int upgradeTo = oldVersion - 1;
        while (upgradeTo >= newVersion) {
            switch (upgradeTo) {
                case 2:
                    // Ne fonctionne pas pour ces versions
                    break;
                case 3:
                    // Ne fonctionne pas pour ces versions
                    //db.execSQL("ALTER TABLE "+ DAOFonte.TABLE_NAME + " DROP COLUMN " + DAOFonte.NOTES);
                    //db.execSQL("ALTER TABLE "+ DAOFonte.TABLE_NAME + " DROP COLUMN " + DAOFonte.UNIT);
                    break;
                case 4:
                    //db.execSQL(DAOMachine.TABLE_DROP);
                    //db.execSQL("ALTER TABLE "+ DAOFonte.TABLE_NAME + " DROP COLUMN " + DAOFonte.MACHINE_KEY );
                    break;
                case 5:
                    //db.execSQL("ALTER TABLE "+ DAOMachine.TABLE_NAME + " DROP COLUMN " + DAOMachine.BODYPARTS );
                    break;
                case 20:
                    // Delete WORKOUT TABLE
                    db.delete(DAOProgram.TABLE_NAME, null, null);
                    break;
                    // TODO delete image table
            }
            upgradeTo--;
        }
    }

    // This method will return true if a field exists in your table
    public boolean FieldExists(SQLiteDatabase db, String tableName, String fieldName) {
        boolean exists = true;
        Cursor res;

        try {
            res = db.rawQuery("SELECT " + fieldName + " FROM " + tableName, null);
            res.close();
        } catch (SQLiteException e) {
            exists = false;
        }

        return exists;
    }

    public boolean TableExists(SQLiteDatabase db, String tableName) {
        boolean isExist = true;
        Cursor res;

        try {
            res = db.rawQuery("SELECT * FROM " + tableName, null);
            res.close();
        } catch (SQLiteException e) {
            isExist = false;
        }
        return isExist;
    }

    private void migrateWeightTable(SQLiteDatabase db) {
        List<ProfileWeight> valueList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + DAOProfileWeight.TABLE_NAME;
        //SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                ContentValues value = new ContentValues();

                value.put(DAOBodyMeasure.DATE, mCursor.getString(mCursor.getColumnIndex(DAOProfileWeight.DATE)));
                value.put(DAOBodyMeasure.BODYPART_ID, BodyPartExtensions.WEIGHT);
                value.put(DAOBodyMeasure.MEASURE, mCursor.getFloat(mCursor.getColumnIndex(DAOProfileWeight.POIDS)));
                value.put(DAOBodyMeasure.UNIT, Unit.KG.ordinal());
                value.put(DAOBodyMeasure.PROFIL_KEY, mCursor.getLong(mCursor.getColumnIndex(DAOProfileWeight.PROFIL_KEY)));

                db.insert(DAOBodyMeasure.TABLE_NAME, null, value);
            } while (mCursor.moveToNext());
            mCursor.close();
            //db.close(); // Closing database connection
        }
    }

    private void upgradeBodyMeasureUnits(SQLiteDatabase db) {
        DAOBodyMeasure daoBodyMeasure = new DAOBodyMeasure(mContext);
        String selectQuery = "SELECT * FROM " + DAOBodyMeasure.TABLE_NAME + " ORDER BY date(" + DAOBodyMeasure.DATE + ") DESC";
        List<BodyMeasure> valueList = daoBodyMeasure.getMeasuresList(db, selectQuery);

        for (BodyMeasure bodyMeasure : valueList) {
            Value oldValue = bodyMeasure.getBodyMeasure();
            Value newValue = oldValue;
            switch (bodyMeasure.getBodyPartID()) {
                case BodyPartExtensions.LEFTBICEPS:
                case BodyPartExtensions.RIGHTBICEPS:
                case BodyPartExtensions.PECTORAUX:
                case BodyPartExtensions.WAIST:
                case BodyPartExtensions.BEHIND:
                case BodyPartExtensions.LEFTTHIGH:
                case BodyPartExtensions.RIGHTTHIGH:
                case BodyPartExtensions.LEFTCALVES:
                case BodyPartExtensions.RIGHTCALVES:
                    newValue = new Value(oldValue.getValue(), Unit.CM);
                    break;
                case BodyPartExtensions.WEIGHT:
                    newValue = new Value(oldValue.getValue(), Unit.KG);
                    break;
                case BodyPartExtensions.MUSCLES:
                case BodyPartExtensions.WATER:
                case BodyPartExtensions.FAT:
                    newValue = new Value(oldValue.getValue(), Unit.PERCENTAGE);
                    break;
            }
            bodyMeasure.setBodyMeasure(newValue);
            daoBodyMeasure.updateMeasure(db, bodyMeasure);
        }
    }

    public void initBodyPartTable(SQLiteDatabase db) {
        int display_order = 0;

        addInitialBodyPart(db, BodyPartExtensions.LEFTBICEPS, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.RIGHTBICEPS, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.PECTORAUX, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.WAIST, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.BEHIND, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.LEFTTHIGH, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.RIGHTTHIGH, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.LEFTCALVES, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.RIGHTCALVES, "", "", display_order++, BodyPartExtensions.TYPE_MUSCLE);
        addInitialBodyPart(db, BodyPartExtensions.WEIGHT, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
        addInitialBodyPart(db, BodyPartExtensions.MUSCLES, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
        addInitialBodyPart(db, BodyPartExtensions.WATER, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
        addInitialBodyPart(db, BodyPartExtensions.FAT, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
        addInitialBodyPart(db, BodyPartExtensions.SIZE, "", "", 0, BodyPartExtensions.TYPE_WEIGHT);
    }

    public long addInitialBodyPart(SQLiteDatabase db, long pKey, String pCustomName, String pCustomPicture, int pDisplay, int pType) {
        ContentValues value = new ContentValues();

        value.put(DAOBodyPart.KEY, pKey);
        value.put(DAOBodyPart.BODYPART_RESID, pKey);
        value.put(DAOBodyPart.CUSTOM_NAME, pCustomName);
        value.put(DAOBodyPart.CUSTOM_PICTURE, pCustomPicture);
        value.put(DAOBodyPart.DISPLAY_ORDER, pDisplay);
        value.put(DAOBodyPart.TYPE, pType);

        return db.insert(DAOBodyPart.TABLE_NAME, null, value);
    }

    /**
     * Copy template values into record values because now, template value are stored inside program records and not as independent record.
     */
    public void copyTemplateValues(SQLiteDatabase db) {
        // for all records if there is a template ID
        // then copy template values to template fields

        DAORecord daoRecord = new DAORecord(mContext);

        List<Record> recordList = daoRecord.getAllRecords(db);

        for (Record record:recordList) {
            if (record.getProgramId() != -1 && record.getRecordType()== RecordType.PROGRAM_RECORD) {
                Record templateRecord = daoRecord.getRecord(db, record.getProgramId());
                if (templateRecord!=null) {
                    record.setTemplateSets(templateRecord.getSets());
                    record.setTemplateReps(templateRecord.getReps());
                    record.setTemplateWeight(templateRecord.getWeightInKg());
                    record.setTemplateWeightUnit(templateRecord.getWeightUnit());
                    record.setTemplateSeconds(templateRecord.getSeconds());
                    record.setTemplateDistance(templateRecord.getDistanceInKm());
                    record.setTemplateDistanceUnit(templateRecord.getDistanceUnit());
                    record.setTemplateDuration(templateRecord.getDuration());
                    daoRecord.updateRecord(db, record);
                }
            }
        }
    }

}
