package com.easyfitness.DAO.export;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOProfile;
import com.easyfitness.DAO.DAOProfileWeight;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
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
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.RecordType;
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
public class CVSManager {

    static private final String TABLE_HEAD = "TABLE";
    static private final String PROGRAM_LABEL = "PROGRAM_NAME";
    static private final String BODYPART_LABEL = "BODYPART_NAME";
    static private final String MEASURE = "MEASURE";

    static private final String DATE = "DATE";
    static private final String TIME = "TIME";
    static private final String EXERCISE = "EXERCISE";
    static private final String EXERCISE_TYPE = "EXERCISE_TYPE";
    static private final String NOTES = "NOTES";
    static private final String SETS = "SETS";
    static private  final String REPS = "REPS";
    static private final String WEIGHT = "WEIGHT";
    static private  final String WEIGHT_UNIT = "WEIGHT_UNIT";
    static private final String DISTANCE = "DISTANCE";
    static private final String DURATION = "DURATION";
    static private final String DISTANCE_UNIT = "DISTANCE_UNIT"; // 0:km 1:mi
    static private final String SECONDS = "SECONDS";
    static private final String RECORD_TYPE = "RECORD_TYPE";
    static private final String TEMPLATE_ORDER = "TEMPLATE_ORDER"; // order of the exercise in the program
    static private final String TEMPLATE_SECONDS = "TEMPLATE_SECONDS";
    static private final String TEMPLATE_REST_TIME = "TEMPLATE_REST_TIME";
    static private final String TEMPLATE_RECORD_STATUS = "TEMPLATE_RECORD_STATUS"; // SUCCESS, FAILED or PENDING
    static private final String TEMPLATE_SETS = "TEMPLATE_SETS";
    static private final String TEMPLATE_REPS = "TEMPLATE_REPS";
    static private final String TEMPLATE_WEIGHT = "TEMPLATE_WEIGHT";
    static private final String TEMPLATE_WEIGHT_UNIT = "TEMPLATE_WEIGHT_UNIT"; // 0:kg 1:lbs
    static private final String TEMPLATE_DISTANCE = "TEMPLATE_DISTANCE";
    static private final String TEMPLATE_DURATION = "TEMPLATE_DURATION";
    static private final String TEMPLATE_DISTANCE_UNIT = "TEMPLATE_DISTANCE_UNIT"; // 0:km 1:mi

    static private final String UNIT = "UNIT";

    static private final String CUSTOM_PICTURE = "CUSTOM_PICTURE";
    static private final String CUSTOM_NAME = "CUSTOM_NAME";

    static private final String NAME = "NAME";
    static private final String DESCRIPTION = "DESCRIPTION";
    static private final String TYPE = "TYPE";
    static private final String BODYPARTS = "BODYPARTS";
    static private final String FAVORITE = "FAVORITE";

    static private final String TABLE_RECORD = "RECORD";
    static private final String TABLE_BODYMEASURE= "BODYMEASURE";
    static private final String TABLE_BODYPART = "BODYPART";
    static private final String TABLE_PROGRAM = "PROGRAM";
    static private final String TABLE_PROGRAM_TEMPLATE = "TEMPLATE";
    static private final String TABLE_EXERCISE = "EXERCISE";

    static private final String TABLE_PROGRESS_IMAGE = "PROGRESS_IMAGE";

    static private final String IMAGE_PATH = "IMAGE_PATH";

    static private final String PROGRESS_IMAGE_FOLDER_NAME = "progressImages";
    static private final String PROGRESS_IMAGE_FILE_NAME = "ProgressImage";


    private Context mContext;

    public CVSManager(Context pContext) {
        mContext = pContext;
    }

    public boolean exportDatabase(Profile pProfile, String destFolder) {
        boolean ret = true;

        try {
            ret &= exportBodyMeasures(pProfile, destFolder);
            ret &= exportRecords(pProfile, destFolder, false);
            ret &= exportExercise(pProfile, destFolder);
            ret &= exportBodyParts(pProfile, destFolder);
            ret &= exportPrograms(pProfile, destFolder);
            ret &= exportRecords(pProfile, destFolder, true);
            ret &= exportProgressImages(pProfile, destFolder);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        //If there are no errors, return true.
        return ret;
    }

    private ZipOutputStream createNewCSVZipFile(String name, String destFolder, Profile pProfile) {
        String fileName = "export_" + name;
        if (pProfile!=null) {
            fileName = fileName + "_" + pProfile.getName();
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = mContext.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/zip");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, destFolder);
                Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
                Uri file = resolver.insert(collection, contentValues);
                return new ZipOutputStream(resolver.openOutputStream(file));
            } else {
                File exportDir = Environment.getExternalStoragePublicDirectory(destFolder);
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                File exportFile = Environment.getExternalStoragePublicDirectory(destFolder + "/" + fileName + ".zip");
                return new ZipOutputStream(new FileOutputStream(exportFile));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    private OutputStream createNewCSVFile(String name, String destFolder, Profile pProfile) {
        String fileName = "export_" + name;
        if (pProfile!=null) {
            fileName = fileName + "_" + pProfile.getName();
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = mContext.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, destFolder);
                Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
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

    private boolean exportRecords(Profile pProfile, String destFolder, boolean templatesOnly) {
        try {
            OutputStream exportFile = createNewCSVFile(templatesOnly ? "ProgramTemplates" : "Records", destFolder, templatesOnly ? null : pProfile);

            CsvWriter csvOutputFonte = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);

            DAORecord dbc = new DAORecord(mContext);
            dbc.open();
            DAOProgram daoProgram = new DAOProgram(mContext);
            daoProgram.open();

            List<Record> records;

            if (templatesOnly) {
                records = dbc.getAllRecords();
            }else{
                Cursor cursor;
                cursor = dbc.getAllRecordsByProfile(pProfile);
                records = dbc.fromCursorToList(cursor);
            }

            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
            csvOutputFonte.write(TABLE_HEAD);
            csvOutputFonte.write(DATE);
            csvOutputFonte.write(TIME);
            csvOutputFonte.write(EXERCISE);
            csvOutputFonte.write(EXERCISE_TYPE);
            csvOutputFonte.write(SETS);
            csvOutputFonte.write(REPS);
            csvOutputFonte.write(WEIGHT);
            csvOutputFonte.write(WEIGHT_UNIT);
            csvOutputFonte.write(SECONDS);
            csvOutputFonte.write(DISTANCE);
            csvOutputFonte.write(DISTANCE_UNIT);
            csvOutputFonte.write(DURATION);
            csvOutputFonte.write(NOTES);
            csvOutputFonte.write(RECORD_TYPE);
            csvOutputFonte.write(PROGRAM_LABEL);
            csvOutputFonte.write(TEMPLATE_REST_TIME);
            csvOutputFonte.write(TEMPLATE_ORDER);
            if (!templatesOnly) {
                csvOutputFonte.write(TEMPLATE_SETS);
                csvOutputFonte.write(TEMPLATE_REPS);
                csvOutputFonte.write(TEMPLATE_WEIGHT);
                csvOutputFonte.write(TEMPLATE_WEIGHT_UNIT);
                csvOutputFonte.write(TEMPLATE_SECONDS);
                csvOutputFonte.write(TEMPLATE_DISTANCE);
                csvOutputFonte.write(TEMPLATE_DISTANCE_UNIT);
                csvOutputFonte.write(TEMPLATE_DURATION);
                csvOutputFonte.write(TEMPLATE_RECORD_STATUS);
            }

            csvOutputFonte.endRecord();

            for (int i = 0; i < records.size(); i++) {
                if ((templatesOnly && records.get(i).getRecordType() == RecordType.PROGRAM_TEMPLATE) ||
                        (!templatesOnly && records.get(i).getRecordType() != RecordType.PROGRAM_TEMPLATE && records.get(i).getProgramRecordStatus()!=ProgramRecordStatus.PENDING)) {

                    csvOutputFonte.write(templatesOnly ? TABLE_PROGRAM_TEMPLATE : TABLE_RECORD);

                    Date dateRecord = records.get(i).getDate();

                    csvOutputFonte.write(DateConverter.dateTimeToDBDateStr(dateRecord));
                    csvOutputFonte.write(DateConverter.dateTimeToDBTimeStr(dateRecord));
                    csvOutputFonte.write(records.get(i).getExercise());
                    csvOutputFonte.write(records.get(i).getExerciseType().toString());
                    csvOutputFonte.write(Integer.toString(records.get(i).getSets()));
                    csvOutputFonte.write(Integer.toString(records.get(i).getReps()));
                    Float weight = UnitConverter.weightConverter(records.get(i).getWeightInKg(), WeightUnit.KG, records.get(i).getWeightUnit());
                    csvOutputFonte.write(Float.toString(weight));
                    csvOutputFonte.write(records.get(i).getWeightUnit().toString());
                    csvOutputFonte.write(Integer.toString(records.get(i).getSeconds()));
                    Float distance = UnitConverter.distanceConverter(records.get(i).getDistanceInKm(), DistanceUnit.KM, records.get(i).getDistanceUnit());
                    csvOutputFonte.write(Float.toString(distance));
                    csvOutputFonte.write(records.get(i).getDistanceUnit().toString());
                    csvOutputFonte.write(Long.toString(records.get(i).getDuration()));
                    if (records.get(i).getNote() == null) csvOutputFonte.write("");
                    else csvOutputFonte.write(records.get(i).getNote());
                    csvOutputFonte.write(records.get(i).getRecordType().toString());

                    if (records.get(i).getRecordType() != RecordType.FREE_RECORD) {
                        Program program = daoProgram.get(records.get(i).getProgramId());
                        if (program != null) {
                            csvOutputFonte.write(program.getName());
                        } else {
                            csvOutputFonte.write("");
                        }

                        csvOutputFonte.write(Long.toString(records.get(i).getTemplateRestTime()));
                        csvOutputFonte.write(Long.toString(records.get(i).getTemplateOrder()));

                        if (!templatesOnly) {
                            csvOutputFonte.write(Integer.toString(records.get(i).getTemplateSets()));
                            csvOutputFonte.write(Integer.toString(records.get(i).getTemplateReps()));
                            Float template_weight = UnitConverter.weightConverter(records.get(i).getTemplateWeight(), WeightUnit.KG, records.get(i).getTemplateWeightUnit());
                            csvOutputFonte.write(Float.toString(template_weight));
                            csvOutputFonte.write(records.get(i).getTemplateWeightUnit().toString());
                            csvOutputFonte.write(Integer.toString(records.get(i).getTemplateSeconds()));
                            Float template_distance = UnitConverter.distanceConverter(records.get(i).getTemplateDistance(), DistanceUnit.KM, records.get(i).getTemplateDistanceUnit());
                            csvOutputFonte.write(Float.toString(template_distance));
                            csvOutputFonte.write(records.get(i).getTemplateDistanceUnit().toString());
                            csvOutputFonte.write(Long.toString(records.get(i).getTemplateDuration()));
                            csvOutputFonte.write(records.get(i).getProgramRecordStatus().toString());
                        }
                    }

                    csvOutputFonte.endRecord();
                }
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
            OutputStream exportFile = createNewCSVFile("BodyMeasures", destFolder, pProfile);

            // use FileWriter constructor that specifies open for appending
            CsvWriter cvsOutput = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);
            DAOBodyMeasure daoBodyMeasure = new DAOBodyMeasure(mContext);
            daoBodyMeasure.open();

            DAOBodyPart daoBodyPart = new DAOBodyPart(mContext);

            List<BodyMeasure> bodyMeasures = daoBodyMeasure.getBodyMeasuresList(pProfile);

            cvsOutput.write(TABLE_HEAD);
            cvsOutput.write(DATE);
            cvsOutput.write(BODYPART_LABEL);
            cvsOutput.write(MEASURE);
            cvsOutput.write(UNIT);
            cvsOutput.endRecord();

            for (int i = 0; i < bodyMeasures.size(); i++) {
                cvsOutput.write(TABLE_BODYMEASURE);
                Date dateRecord = bodyMeasures.get(i).getDate();
                cvsOutput.write(DateConverter.dateToDBDateStr(dateRecord));
                BodyPart bp = daoBodyPart.getBodyPart(bodyMeasures.get(i).getBodyPartID());
                cvsOutput.write(bp.getName(mContext)); // Write the full name of the BodyPart because the ID is not enough
                cvsOutput.write(Float.toString(bodyMeasures.get(i).getBodyMeasure().getValue()));
                cvsOutput.write(bodyMeasures.get(i).getBodyMeasure().getUnit().toString());

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

    private boolean exportProgressImages(Profile pProfile, String destFolder) {
        try {
            ZipOutputStream zipFile = createNewCSVZipFile(PROGRESS_IMAGE_FILE_NAME, destFolder, pProfile);

            String fileName = "export_" + PROGRESS_IMAGE_FILE_NAME;
            if (pProfile!=null) {
                fileName = fileName + "_" + pProfile.getName();
            }
            ZipEntry csvEntry = new ZipEntry(fileName + ".csv");
            zipFile.putNextEntry(csvEntry);

            // use FileWriter constructor that specifies open for appending
            CsvWriter cvsOutput = new CsvWriter(zipFile, ',', StandardCharsets.UTF_8);

            DAOProgressImage daoProgressImage = new DAOProgressImage(mContext);

            cvsOutput.write(TABLE_HEAD);
            cvsOutput.write(DATE);
            cvsOutput.write(IMAGE_PATH);
            cvsOutput.endRecord();

            final int imageCount = daoProgressImage.count(pProfile.getId());

            for (int i = 0; i < imageCount; i++) {
                ProgressImage record = daoProgressImage.getImage(pProfile.getId(), i);

                cvsOutput.write(TABLE_PROGRESS_IMAGE);
                Date dateRecord = record.getCreated();
                cvsOutput.write(DateConverter.dateToDBDateStr(dateRecord));
                File internalFile = new File(record.getFile());
                String exportedFileName = internalFile.getName();
                cvsOutput.write(PROGRESS_IMAGE_FOLDER_NAME + File.separator + exportedFileName);

                cvsOutput.endRecord();
            }
            cvsOutput.flush();
            zipFile.closeEntry();

            for (int i = 0; i < imageCount; i++) {
                ProgressImage record = daoProgressImage.getImage(pProfile.getId(), i);

                File internalFile = new File(record.getFile());
                String exportedFileName = internalFile.getName();
                String exportRelativePath = PROGRESS_IMAGE_FOLDER_NAME + File.separator + exportedFileName;

                ZipEntry exportImage = new ZipEntry(exportRelativePath);
                zipFile.putNextEntry(exportImage);
                ImageUtil.copyFileToStream(internalFile, zipFile);
                zipFile.closeEntry();
            }
            zipFile.close();
            cvsOutput.close();
            daoProgressImage.close();
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
            OutputStream exportFile = createNewCSVFile("BodyParts", destFolder, null);
            // use FileWriter constructor that specifies open for appending
            CsvWriter cvsOutput = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);
            DAOBodyPart daoBodyPart = new DAOBodyPart(mContext);
            daoBodyPart.open();


            List<BodyPart> bodyParts = daoBodyPart.getList();

            cvsOutput.write(TABLE_HEAD);
            cvsOutput.write(CUSTOM_NAME);
            cvsOutput.write(CUSTOM_PICTURE);
            cvsOutput.endRecord();

            for (BodyPart bp : bodyParts) {
                if (bp.getBodyPartResKey() == -1) { // Only custom BodyPart are exported
                    cvsOutput.write(TABLE_BODYPART);
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
            OutputStream exportFile = createNewCSVFile("Exercises", destFolder, null);
            CsvWriter csvOutput = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);

            DAOMachine dbcMachine = new DAOMachine(mContext);
            dbcMachine.open();

            List<Machine> records = dbcMachine.getAllMachinesArray();

            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
            csvOutput.write(TABLE_HEAD);
            csvOutput.write(NAME);
            csvOutput.write(DESCRIPTION);
            csvOutput.write(TYPE);
            csvOutput.write(BODYPARTS);
            csvOutput.write(FAVORITE);
            //csvOutput.write(DAOMachine.PICTURE_RES);
            csvOutput.endRecord();

            for (int i = 0; i < records.size(); i++) {
                csvOutput.write(TABLE_EXERCISE);
                csvOutput.write(records.get(i).getName());
                csvOutput.write(records.get(i).getDescription());
                csvOutput.write(records.get(i).getType().toString());
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

    private boolean exportPrograms(Profile pProfile, String destFolder) {
        try {
        OutputStream exportFile = createNewCSVFile("Programs", destFolder, null);
        CsvWriter csvOutput = new CsvWriter(exportFile, ',', StandardCharsets.UTF_8);

        DAOProgram dbcProgram = new DAOProgram(mContext);
        dbcProgram.open();

        List<Program> records = dbcProgram.getAll();

        //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
        csvOutput.write(TABLE_HEAD);
        csvOutput.write(NAME);
        csvOutput.write(DESCRIPTION);
        csvOutput.endRecord();

        for (int i = 0; i < records.size(); i++) {
            csvOutput.write(TABLE_PROGRAM);
            csvOutput.write(records.get(i).getName());
            csvOutput.write(records.get(i).getDescription());
            csvOutput.endRecord();
        }
        csvOutput.close();
        dbcProgram.close();
    } catch (Exception e) {
        //if there are any exceptions, return false
        e.printStackTrace();
        return false;
    }
    //If there are no errors, return true.
        return true;
}

    public boolean importDatabase(ZipFile file, Profile pProfile) {
        Enumeration<? extends ZipEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            final String extension = FileNameUtil.getExtension(entry.getName());
            if (FileNameUtil.FILE_ENDING_CSV.equalsIgnoreCase(extension)) {
                try (InputStream inputStream = file.getInputStream(entry)) {
                    if (!importDatabase(inputStream, pProfile, file)) {
                        return false;
                    }
                } catch (IOException e) {
                    Log.e(getClass().getName(), "Failed to read zip file", e);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean importDatabase(InputStream file, Profile pProfile) {
        return importDatabase(file, pProfile, null);
    }

    private boolean importDatabase(InputStream file, Profile pProfile, ZipFile parentZip) {

        boolean ret = true;
        int importedRow = 0;
        int failedImportRow = 0;

        try {
            CsvReader csvRecords = new CsvReader(file, ',', StandardCharsets.UTF_8);

            csvRecords.readHeaders();

            ArrayList<Record> recordsList = new ArrayList<>();

            DAOMachine dbcMachine = new DAOMachine(mContext);
            DAOProgressImage daoProgressImage = new DAOProgressImage(mContext);

            while (csvRecords.readRecord()) {
                switch (csvRecords.get(TABLE_HEAD)) {
                    case TABLE_PROGRESS_IMAGE:
                        Date imageDate = DateConverter.DBDateStrToDate(csvRecords.get(DATE));
                        String relativeImagePath = csvRecords.get(IMAGE_PATH);

                        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        ZipEntry imageZipEntry = parentZip.getEntry(relativeImagePath);
                        try (InputStream imageStream = parentZip.getInputStream(imageZipEntry)) {
                            File newFile = ImageUtil.copyFileFromStream(
                                    imageStream,
                                    storageDir,
                                    new File(relativeImagePath).getName()
                            );

                            daoProgressImage.addProgressImage(
                                    imageDate,
                                    newFile,
                                    pProfile.getId()
                            );
                        }
                        break;
                    case TABLE_PROGRAM_TEMPLATE:
                    case TABLE_RECORD:
                    case DAORecord.TABLE_NAME: {
                        try {
                            Date date = DateConverter.DBDateTimeStrToDate(csvRecords.get(DATE), csvRecords.get(TIME));
                            String exerciseName = csvRecords.get(EXERCISE);
                            ExerciseType exerciseType = ExerciseType.fromString(csvRecords.get(EXERCISE_TYPE));

                            Machine machine = FindOrCreateMachine(exerciseName, exerciseType);

                            WeightUnit unit = WeightUnit.fromString(TryGetString(csvRecords, WEIGHT_UNIT, WeightUnit.KG.toString()));
                            float weight = TryGetFloat(csvRecords, WEIGHT, 0);
                            weight = UnitConverter.weightConverter(weight, unit, WeightUnit.KG);
                            int repetitions = TryGetInteger(csvRecords, REPS, 0);
                            int sets = TryGetInteger(csvRecords, SETS, 0);
                            int second = TryGetInteger(csvRecords, SECONDS, 0);

                            int duration = TryGetInteger(csvRecords, DURATION, 0);
                            DistanceUnit distance_unit = DistanceUnit.fromString(TryGetString(csvRecords, DISTANCE_UNIT, DistanceUnit.KM.toString()));
                            float distance = TryGetFloat(csvRecords, DISTANCE, 0);
                            distance = UnitConverter.distanceConverter(distance, distance_unit, DistanceUnit.KM);

                            String notes = TryGetString(csvRecords, NOTES, "");

                            RecordType record_type = RecordType.fromString(csvRecords.get(RECORD_TYPE));

                            if (record_type==RecordType.PROGRAM_RECORD) {
                                long programId;
                                String programName = TryGetString(csvRecords, PROGRAM_LABEL, "");
                                Program program = FindOrCreateProgram(programName);
                                if (program != null) {
                                    programId = program.getId();
                                } else {
                                    programId = -1;
                                }

                                int template_order = TryGetInteger(csvRecords, TEMPLATE_ORDER, 0);

                                WeightUnit template_unit = WeightUnit.fromString(TryGetString(csvRecords, TEMPLATE_WEIGHT_UNIT, WeightUnit.KG.toString()));
                                float template_weight = TryGetFloat(csvRecords, TEMPLATE_WEIGHT, 0);
                                template_weight = UnitConverter.weightConverter(template_weight, template_unit, WeightUnit.KG);

                                int template_repetitions = TryGetInteger(csvRecords, TEMPLATE_REPS, 0);
                                int template_sets = TryGetInteger(csvRecords, TEMPLATE_SETS, 0);
                                int template_second = TryGetInteger(csvRecords, TEMPLATE_SECONDS, 0);

                                int template_duration = TryGetInteger(csvRecords, TEMPLATE_DURATION, 0);
                                DistanceUnit template_distance_unit = DistanceUnit.fromString(TryGetString(csvRecords, TEMPLATE_DISTANCE_UNIT, DistanceUnit.KM.toString()));
                                float template_distance = TryGetFloat(csvRecords, TEMPLATE_DISTANCE, 0);
                                template_distance = UnitConverter.distanceConverter(template_distance, template_distance_unit, DistanceUnit.KM);
                                int template_rest_time = TryGetInteger(csvRecords, TEMPLATE_REST_TIME, 0);

                                ProgramRecordStatus template_record_status = ProgramRecordStatus.fromString(TryGetString(csvRecords, TEMPLATE_RECORD_STATUS, ProgramRecordStatus.NONE.toString()));
                                Record record = new Record(date, machine.getName(), machine.getId(), pProfile.getId(), sets, repetitions, weight, unit, second, distance, distance_unit, duration, notes, exerciseType,
                                        programId, -1, -1,
                                        template_rest_time, template_order, template_record_status, record_type, template_sets, template_repetitions, template_weight, template_unit, template_second, template_distance, template_distance_unit, template_duration);
                                recordsList.add(record);
                            } else if (record_type==RecordType.FREE_RECORD){
                                Record record = new Record(date, machine.getName(), machine.getId(), pProfile.getId(), sets, repetitions, weight, unit, second, distance, distance_unit, duration, notes, exerciseType);
                                recordsList.add(record);
                            } else {
                                long programId;
                                String programName = TryGetString(csvRecords, PROGRAM_LABEL, "");
                                Program program = FindOrCreateProgram(programName);
                                if (program != null) {
                                    programId = program.getId();
                                } else {
                                    programId = -1;
                                }

                                int template_order = TryGetInteger(csvRecords, TEMPLATE_ORDER, 0);


                                int template_rest_time = TryGetInteger(csvRecords, TEMPLATE_REST_TIME, 0);

                                Record record = new Record(date, machine.getName(), machine.getId(), pProfile.getId(), sets, repetitions, weight, unit, second, distance, distance_unit, duration, notes, exerciseType,
                                        programId, template_rest_time, template_order);
                                recordsList.add(record);
                            }

                            importedRow++;
                        } catch (Exception e) {
                            e.printStackTrace();
                            failedImportRow++;
                            //TODO build a log file to list all errors
                        }

                        break;
                    }
                    case DAOOldCardio.TABLE_NAME: {
                        // This is the deprecated weight table
                        DAOCardio dbcCardio = new DAOCardio(mContext);
                        dbcCardio.open();

                        Date date = DateConverter.DBDateStrToDate(csvRecords.get(DAOCardio.DATE));

                        String exercice = csvRecords.get(DAOOldCardio.EXERCICE);
                        float distance = Float.parseFloat(csvRecords.get(DAOOldCardio.DISTANCE));
                        int duration = Integer.parseInt(csvRecords.get(DAOOldCardio.DURATION));
                        dbcCardio.addCardioRecordToFreeWorkout(date, exercice, distance, duration, pProfile.getId(), DistanceUnit.KM);
                        dbcCardio.close();

                        break;
                    }
                    case DAOProfileWeight.TABLE_NAME: {
                        // This is the deprecated weight table
                        DAOBodyMeasure dbcWeight = new DAOBodyMeasure(mContext);
                        dbcWeight.open();
                        Date date = DateConverter.DBDateStrToDate(csvRecords.get(DAOProfileWeight.DATE));

                        float weight = Float.parseFloat(csvRecords.get(DAOProfileWeight.POIDS));
                        dbcWeight.addBodyMeasure(date, BodyPartExtensions.WEIGHT, new Value(weight, Unit.KG), pProfile.getId());

                        break;
                    }
                    case TABLE_BODYMEASURE:
                    case DAOBodyMeasure.TABLE_NAME: {
                        try {
                            DAOBodyMeasure dbcBodyMeasure = new DAOBodyMeasure(mContext);
                            dbcBodyMeasure.open();
                            Date date = DateConverter.DBDateStrToDate(csvRecords.get(DATE));
                            Unit unit = Unit.fromString(csvRecords.get(UNIT));
                            String bodyPartName = csvRecords.get(BODYPART_LABEL);
                            BodyPart bodyPart = FindOrCreateBodyPart(bodyPartName);
                            float measure = Float.parseFloat(csvRecords.get(MEASURE));
                            dbcBodyMeasure.addBodyMeasure(date, bodyPart.getId(), new Value(measure, unit), pProfile.getId());
                            importedRow++;
                        } catch (Exception e) {
                            e.printStackTrace();
                            failedImportRow++;
                        }
                        break;
                    }
                    case TABLE_BODYPART:
                    case DAOBodyPart.TABLE_NAME:
                        try {
                            DAOBodyPart dbcBodyPart = new DAOBodyPart(mContext);
                            dbcBodyPart.open();
                            String customName = csvRecords.get(CUSTOM_NAME);
                            String customPicture = TryGetString(csvRecords, CUSTOM_PICTURE, "");
                            BodyPart bodyPart = FindOrCreateBodyPart(customName);
                            bodyPart.setCustomPicture(customPicture);
                            dbcBodyPart.update(bodyPart);
                            importedRow++;
                        } catch (Exception e) {
                            e.printStackTrace();
                            failedImportRow++;
                        }
                        break;
                    case DAOProfile.TABLE_NAME:
                        // TODO : import profiles
                        break;
                    case TABLE_EXERCISE:
                    case DAOMachine.TABLE_NAME:
                        try {
                            DAOMachine dbc = new DAOMachine(mContext);
                            String name = csvRecords.get(NAME);
                            String description = TryGetString(csvRecords, DESCRIPTION, "");
                            ExerciseType type = ExerciseType.fromString(csvRecords.get(TYPE));
                            boolean favorite = TryGetBoolean(csvRecords, FAVORITE, false);
                            String bodyParts = TryGetString(csvRecords, BODYPARTS, "");

                            Machine m = FindOrCreateMachine(name, type);
                            m.setDescription(description);
                            m.setFavorite(favorite);
                            m.setBodyParts(bodyParts);
                            dbc.updateMachine(m);
                            importedRow++;
                        } catch (Exception e) {
                            e.printStackTrace();
                            failedImportRow++;
                        }
                        break;
                    case TABLE_PROGRAM:
                    case DAOProgram.TABLE_NAME:
                        try {
                            DAOProgram daoProgram = new DAOProgram(mContext);
                            daoProgram.open();
                            String programName = csvRecords.get(NAME);
                            String programDescription = TryGetString(csvRecords, DESCRIPTION, "");

                            Program program = FindOrCreateProgram(programName);
                            program.setDescription(programDescription);
                            daoProgram.update(program);
                            importedRow++;

                        } catch (Exception e) {
                            e.printStackTrace();
                            failedImportRow++;
                        }
                        break;
                }
            }

            csvRecords.close();

            // In case of success
            if (!recordsList.isEmpty()) {
                DAORecord daoRecord = new DAORecord(mContext);
                daoRecord.addList(recordsList);
            }

        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }

        return ret;
    }

    private int TryGetInteger(CsvReader csvReader, String value, int defaultValue) {
        try {
            return Integer.parseInt(csvReader.get(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private float TryGetFloat(CsvReader csvReader, String value, float defaultValue) {
        try {
            return Float.parseFloat(csvReader.get(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean TryGetBoolean(CsvReader csvReader, String value, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(csvReader.get(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String TryGetString(CsvReader csvReader, String value, String defaultValue) {
        try {
            return csvReader.get(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private BodyPart FindOrCreateBodyPart(String bodyPartName) {
        BodyPart bodyPart = null;
        DAOBodyPart daoBodyPart = new DAOBodyPart(mContext);
        daoBodyPart.open();
        List<BodyPart> bodyParts = daoBodyPart.getList();
        for (BodyPart bp : bodyParts) {
            if (bp.getName(mContext).equals(bodyPartName)) {
                bodyPart = bp;
                break;
            }
        }
        if (bodyPart==null) {
            long newItemId = daoBodyPart.add(-1, bodyPartName, "", daoBodyPart.getCount(), BodyPartExtensions.TYPE_MUSCLE);
            bodyPart = daoBodyPart.getBodyPart(newItemId);
        }
        return bodyPart;
    }

    private Program FindOrCreateProgram(String programName) {
        Program program = null;
        DAOProgram daoProgram= new DAOProgram(mContext);
        daoProgram.open();
        List<Program> programs = daoProgram.getAll();
        for (Program prg : programs) {
            if (prg.getName().equals(programName)) {
                program = prg;
                break;
            }
        }
        if (program==null && !programName.isEmpty()) {
            program = new Program(-1, programName, "");
            long newItemId = daoProgram.add(program);
            program.setId(newItemId);
        }
        return program;
    }

    private Machine FindOrCreateMachine(String machineName, ExerciseType exerciseType) {
        Machine machine = null;
        DAOMachine daoMachine = new DAOMachine(mContext);
        daoMachine.open();
        List<Machine> machines = daoMachine.getAll();
        for (Machine lMachine : machines) {
            if (lMachine.getName().equals(machineName) && lMachine.getType()==exerciseType) {
                machine = lMachine;
                break;
            }
        }
        if (machine==null) {
            long newItemId = daoMachine.addMachine(machineName,"", exerciseType, "", false, "");
            machine = daoMachine.getMachine(newItemId);
        }
        return machine;
    }
}
