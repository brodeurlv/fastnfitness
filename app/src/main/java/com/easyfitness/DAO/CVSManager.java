package com.easyfitness.DAO;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.bodymeasures.DAOBodyPart;
import com.easyfitness.DAO.cardio.DAOOldCardio;
import com.easyfitness.DAO.program.Program;
import com.easyfitness.DAO.record.DAOCardio;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.Unit;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.DateConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI;


// Uses http://javacsv.sourceforge.net/com/csvreader/CsvReader.html //
public class CVSManager {

    static private final String TABLE_HEAD = "table";
    static private final String ID_HEAD = "id";

    private Context mContext = null;

    public CVSManager(Context pContext) {
        mContext = pContext;
    }

    public boolean exportDatabase(Profile pProfile, String destFolder) {
         /**First of all we check if the external storage of the device is available for writing.
         * Remember that the external storage is not necessarily the sd card. Very often it is
         * the device storage.
         */

         boolean ret = true;

        PrintWriter printWriter = null;
            try {
                ret &= exportBodyMeasures(pProfile, destFolder);
                ret &= exportRecords(pProfile, destFolder);
                ret &= exportExercise(pProfile, destFolder);
                ret &= exportBodyParts(pProfile, destFolder);
            } catch (Exception e) {
                //if there are any exceptions, return false
                e.printStackTrace();
                return false;
            } finally {
                if (printWriter != null) printWriter.close();
            }

            //If there are no errors, return true.
            return ret;
    }

    private OutputStream CreateNewFile(String name, String destFolder, Profile pProfile) {
        String fileName = "export_" + name + "_" + pProfile.getName();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = mContext.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, destFolder);
                Uri collection = null;
                collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
                Uri file = resolver.insert(collection, contentValues);
                return resolver.openOutputStream(file);
            } else {
                File exportDir = Environment.getExternalStoragePublicDirectory(destFolder);
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                File exportFile = Environment.getExternalStoragePublicDirectory(destFolder + "/" + fileName + ".csv");
                return new FileOutputStream(exportFile);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean exportRecords(Profile pProfile, String destFolder) {
        try {
            OutputStream exportFile = CreateNewFile("Records", destFolder, pProfile);

            CsvWriter csvOutputFonte = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);

            /**This is our database connector class that reads the data from the database.
             * The code of this class is omitted for brevity.
             */
            DAORecord dbc = new DAORecord(mContext);
            dbc.open();

            /**Let's read the first table of the database.
             * getFirstTable() is a method in our DBCOurDatabaseConnector class which retrieves a Cursor
             * containing all records of the table (all fields).
             * The code of this class is omitted for brevity.
             */
            Cursor cursor = dbc.getAllRecordsByProfile(pProfile);
            List<Record> records = dbc.fromCursorToList(cursor);

            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
            csvOutputFonte.write(TABLE_HEAD);
            csvOutputFonte.write(ID_HEAD);
            csvOutputFonte.write(DAORecord.DATE);
            csvOutputFonte.write(DAORecord.TIME);
            csvOutputFonte.write(DAORecord.EXERCISE);
            csvOutputFonte.write(DAORecord.EXERCISE_TYPE);
            csvOutputFonte.write(DAORecord.PROFILE_KEY);
            csvOutputFonte.write(DAORecord.SETS);
            csvOutputFonte.write(DAORecord.REPS);
            csvOutputFonte.write(DAORecord.WEIGHT);
            csvOutputFonte.write(DAORecord.WEIGHT_UNIT);
            csvOutputFonte.write(DAORecord.SECONDS);
            csvOutputFonte.write(DAORecord.DISTANCE);
            csvOutputFonte.write(DAORecord.DISTANCE_UNIT);
            csvOutputFonte.write(DAORecord.DURATION);
            csvOutputFonte.write(DAORecord.NOTES);
            csvOutputFonte.write(DAORecord.RECORD_TYPE);
            csvOutputFonte.endRecord();

            for (int i = 0; i < records.size(); i++) {
                csvOutputFonte.write(DAORecord.TABLE_NAME);
                csvOutputFonte.write(Long.toString(records.get(i).getId()));

                Date dateRecord = records.get(i).getDate();

                csvOutputFonte.write(DateConverter.dateTimeToDBDateStr(dateRecord));
                csvOutputFonte.write(DateConverter.dateTimeToDBTimeStr(dateRecord));
                csvOutputFonte.write(records.get(i).getExercise());
                csvOutputFonte.write(Integer.toString(ExerciseType.STRENGTH.ordinal()));
                csvOutputFonte.write(Long.toString(records.get(i).getProfileId()));
                csvOutputFonte.write(Integer.toString(records.get(i).getSets()));
                csvOutputFonte.write(Integer.toString(records.get(i).getReps()));
                csvOutputFonte.write(Float.toString(records.get(i).getWeight()));
                csvOutputFonte.write(Integer.toString(records.get(i).getWeightUnit().ordinal()));
                csvOutputFonte.write(Integer.toString(records.get(i).getSeconds()));
                csvOutputFonte.write(Float.toString(records.get(i).getDistance()));
                csvOutputFonte.write(Integer.toString(records.get(i).getDistanceUnit().ordinal()));
                csvOutputFonte.write(Long.toString(records.get(i).getDuration()));
                if (records.get(i).getNote() == null) csvOutputFonte.write("");
                else csvOutputFonte.write(records.get(i).getNote());
                csvOutputFonte.write(Integer.toString(records.get(i).getRecordType().ordinal()));
                csvOutputFonte.endRecord();
            }
            csvOutputFonte.close();
            dbc.closeAll();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    private boolean exportBodyMeasures(Profile pProfile, String destFolder) {
        try {
            OutputStream exportFile = CreateNewFile("BodyMeasures", destFolder, pProfile);

            // use FileWriter constructor that specifies open for appending
            CsvWriter cvsOutput = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);
            DAOBodyMeasure daoBodyMeasure = new DAOBodyMeasure(mContext);
            daoBodyMeasure.open();

            DAOBodyPart daoBodyPart = new DAOBodyPart(mContext);

            List<BodyMeasure> bodyMeasures = daoBodyMeasure.getBodyMeasuresList(pProfile);

            cvsOutput.write(TABLE_HEAD);
            cvsOutput.write(ID_HEAD);
            cvsOutput.write(DAOBodyMeasure.DATE);
            cvsOutput.write("bodypart_label");
            cvsOutput.write(DAOBodyMeasure.MEASURE);
            cvsOutput.write(DAOBodyMeasure.PROFIL_KEY);
            cvsOutput.endRecord();

            for (int i = 0; i < bodyMeasures.size(); i++) {
                cvsOutput.write(DAOBodyMeasure.TABLE_NAME);
                cvsOutput.write(Long.toString(bodyMeasures.get(i).getId()));
                Date dateRecord = bodyMeasures.get(i).getDate();
                cvsOutput.write(DateConverter.dateToDBDateStr(dateRecord));
                BodyPart bp = daoBodyPart.getBodyPart(bodyMeasures.get(i).getBodyPartID());
                cvsOutput.write(bp.getName(mContext)); // Write the full name of the BodyPart
                cvsOutput.write(Float.toString(bodyMeasures.get(i).getBodyMeasure()));
                cvsOutput.write(Long.toString(bodyMeasures.get(i).getProfileID()));

                cvsOutput.endRecord();
            }
            cvsOutput.close();
            daoBodyMeasure.close();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    private boolean exportBodyParts(Profile pProfile, String destFolder) {
        try {
            OutputStream exportFile = CreateNewFile("BodyParts", destFolder, pProfile);
            // use FileWriter constructor that specifies open for appending
            CsvWriter cvsOutput = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);
            DAOBodyPart daoBodyPart = new DAOBodyPart(mContext);
            daoBodyPart.open();


            List<BodyPart> bodyParts = daoBodyPart.getList();

            cvsOutput.write(TABLE_HEAD);
            cvsOutput.write(DAOBodyPart.KEY);
            cvsOutput.write(DAOBodyPart.CUSTOM_NAME);
            cvsOutput.write(DAOBodyPart.CUSTOM_PICTURE);
            cvsOutput.endRecord();

            for (BodyPart bp : bodyParts) {
                if (bp.getBodyPartResKey() == -1) { // Only custom BodyPart are exported
                    cvsOutput.write(DAOBodyMeasure.TABLE_NAME);
                    cvsOutput.write(Long.toString(bp.getId()));
                    cvsOutput.write(bp.getName(mContext));
                    cvsOutput.write(bp.getCustomPicture());
                    cvsOutput.endRecord();
                }
            }
            cvsOutput.close();
            daoBodyPart.close();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    private boolean exportExercise(Profile pProfile, String destFolder) {
        try {
            // FONTE
            OutputStream exportFile = CreateNewFile("Exercises", destFolder, pProfile);
            CsvWriter csvOutput = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);

            /**This is our database connector class that reads the data from the database.
             * The code of this class is omitted for brevity.
             */
            DAOMachine dbcMachine = new DAOMachine(mContext);
            dbcMachine.open();

            /**Let's read the first table of the database.
             * getFirstTable() is a method in our DBCOurDatabaseConnector class which retrieves a Cursor
             * containing all records of the table (all fields).
             * The code of this class is omitted for brevity.
             */
            List<Machine> records = dbcMachine.getAllMachinesArray();

            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
            csvOutput.write(TABLE_HEAD);
            csvOutput.write(ID_HEAD);
            csvOutput.write(DAOMachine.NAME);
            csvOutput.write(DAOMachine.DESCRIPTION);
            csvOutput.write(DAOMachine.TYPE);
            csvOutput.write(DAOMachine.BODYPARTS);
            csvOutput.write(DAOMachine.FAVORITES);
            //csvOutput.write(DAOMachine.PICTURE_RES);
            csvOutput.endRecord();

            for (int i = 0; i < records.size(); i++) {
                csvOutput.write(DAOMachine.TABLE_NAME);
                csvOutput.write(Long.toString(records.get(i).getId()));
                csvOutput.write(records.get(i).getName());
                csvOutput.write(records.get(i).getDescription());
                csvOutput.write(Integer.toString(records.get(i).getType().ordinal()));
                csvOutput.write(records.get(i).getBodyParts());
                csvOutput.write(Boolean.toString(records.get(i).getFavorite()));
                //write the record in the .csv file
                csvOutput.endRecord();
            }
            csvOutput.close();
            dbcMachine.close();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    public boolean importDatabase(InputStream file, Profile pProfile) {

        boolean ret = true;

        try {
            CsvReader csvRecords = new CsvReader(file, ',', StandardCharsets.UTF_8);

            csvRecords.readHeaders();

            ArrayList<Record> recordsList = new ArrayList<>();

            DAOMachine dbcMachine = new DAOMachine(mContext);

            while (csvRecords.readRecord()) {
                switch (csvRecords.get(TABLE_HEAD)) {
                    case DAORecord.TABLE_NAME: {
                        Date date = DateConverter.DBDateTimeStrToDate(csvRecords.get(DAORecord.DATE), csvRecords.get(DAORecord.TIME));
                        String exercise = csvRecords.get(DAORecord.EXERCISE);
                        if (dbcMachine.getMachine(exercise) != null) {
                            long exerciseId = dbcMachine.getMachine(exercise).getId();
                            ExerciseType exerciseType = dbcMachine.getMachine(exercise).getType();

                            float poids = TryGetFloat(csvRecords.get(DAORecord.WEIGHT), 0);
                            int repetition = TryGetInteger(csvRecords.get(DAORecord.REPS), 0);
                            int serie = TryGetInteger(csvRecords.get(DAORecord.SETS), 0);
                            WeightUnit unit = WeightUnit.KG;
                            if (!csvRecords.get(DAORecord.WEIGHT_UNIT).isEmpty()) {
                                unit = WeightUnit.fromInteger(TryGetInteger(csvRecords.get(DAORecord.WEIGHT_UNIT), WeightUnit.KG.ordinal()));
                            }
                            int second = TryGetInteger(csvRecords.get(DAORecord.SECONDS), 0);
                            float distance = TryGetFloat(csvRecords.get(DAORecord.DISTANCE), 0);
                            int duration = TryGetInteger(csvRecords.get(DAORecord.DURATION), 0);
                            DistanceUnit distance_unit = DistanceUnit.KM;
                            if (!csvRecords.get(DAORecord.DISTANCE_UNIT).isEmpty()) {
                                distance_unit = DistanceUnit.fromInteger(TryGetInteger(csvRecords.get(DAORecord.DISTANCE_UNIT), DistanceUnit.KM.ordinal()));
                            }
                            String notes = csvRecords.get(DAORecord.NOTES);

                            Record record = new Record(date, exercise, exerciseId, pProfile.getId(), serie, repetition, poids, unit, second, distance, distance_unit, duration, notes, exerciseType, -1);
                            recordsList.add(record);
                        } else {
                            return false;
                        }

                        break;
                    }
                    case DAOOldCardio.TABLE_NAME: {
                        DAOCardio dbcCardio = new DAOCardio(mContext);
                        dbcCardio.open();

                        Date date = DateConverter.DBDateStrToDate(csvRecords.get(DAOCardio.DATE));

                        String exercice = csvRecords.get(DAOOldCardio.EXERCICE);
                        float distance = Float.parseFloat(csvRecords.get(DAOOldCardio.DISTANCE));
                        int duration = Integer.parseInt(csvRecords.get(DAOOldCardio.DURATION));
                        dbcCardio.addCardioRecord(date, exercice, distance, duration, pProfile.getId(), DistanceUnit.KM, -1);
                        dbcCardio.close();

                        break;
                    }
                    case DAOProfileWeight.TABLE_NAME: {
                        DAOBodyMeasure dbcWeight = new DAOBodyMeasure(mContext);
                        dbcWeight.open();
                        Date date = DateConverter.DBDateStrToDate(csvRecords.get(DAOProfileWeight.DATE));

                        float poids = Float.parseFloat(csvRecords.get(DAOProfileWeight.POIDS));
                        dbcWeight.addBodyMeasure(date, BodyPartExtensions.WEIGHT, poids, pProfile.getId(), Unit.KG);

                        break;
                    }
                    case DAOBodyMeasure.TABLE_NAME: {
                        DAOBodyMeasure dbcBodyMeasure = new DAOBodyMeasure(mContext);
                        dbcBodyMeasure.open();
                        Date date = DateConverter.DBDateStrToDate(csvRecords.get(DAOBodyMeasure.DATE));
                        Unit unit = Unit.fromInteger(Integer.parseInt(csvRecords.get(DAOBodyMeasure.UNIT))); // Mandatory. Cannot not know the Unit.
                        String bodyPartName = csvRecords.get("bodypart_label");
                        DAOBodyPart dbcBodyPart = new DAOBodyPart(mContext);
                        dbcBodyPart.open();
                        List<BodyPart> bodyParts = dbcBodyPart.getList();
                        for (BodyPart bp : bodyParts) {
                            if (bp.getName(mContext).equals(bodyPartName)) {
                                float measure = Float.parseFloat(csvRecords.get(DAOBodyMeasure.MEASURE));
                                dbcBodyMeasure.addBodyMeasure(date, bp.getId(), measure, pProfile.getId(), unit);
                                dbcBodyPart.close();
                                break;
                            }
                        }
                        break;
                    }
                    case DAOBodyPart.TABLE_NAME:
                        DAOBodyPart dbcBodyPart = new DAOBodyPart(mContext);
                        dbcBodyPart.open();
                        int bodyPartId = -1;
                        String customName = csvRecords.get(DAOBodyPart.CUSTOM_NAME);
                        String customPicture = csvRecords.get(DAOBodyPart.CUSTOM_PICTURE);
                        dbcBodyPart.add(bodyPartId, customName, customPicture, 0, BodyPartExtensions.TYPE_MUSCLE);
                        break;
                    case DAOProfile.TABLE_NAME:
                        // TODO : import profiles
                        break;
                    case DAOMachine.TABLE_NAME:
                        DAOMachine dbc = new DAOMachine(mContext);
                        String name = csvRecords.get(DAOMachine.NAME);
                        String description = csvRecords.get(DAOMachine.DESCRIPTION);
                        ExerciseType type = ExerciseType.fromInteger(Integer.parseInt(csvRecords.get(DAOMachine.TYPE)));
                        boolean favorite = TryGetBoolean(csvRecords.get(DAOMachine.FAVORITES), false);
                        String bodyParts = csvRecords.get(DAOMachine.BODYPARTS);

                        // Check if this machine doesn't exist
                        if (dbc.getMachine(name) == null) {
                            dbc.addMachine(name, description, type, "", favorite, bodyParts);
                        } else {
                            Machine m = dbc.getMachine(name);
                            m.setDescription(description);
                            m.setFavorite(favorite);
                            m.setBodyParts(bodyParts);
                            dbc.updateMachine(m);
                        }
                        break;
                }
            }

            csvRecords.close();

            // In case of success
            DAORecord daoRecord = new DAORecord(mContext);
            daoRecord.addList(recordsList);

        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }

        return ret;
    }

    private int TryGetInteger(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private float TryGetFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean TryGetBoolean(String value, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Unit TryGetUnit(String value, Unit defaultValue) {
        Unit unit = Unit.fromString(value);
        if (unit != null) {
            return unit;
        }
        return defaultValue;
    }

}
