package com.easyfitness.DAO;

import android.content.Context;
import android.os.Environment;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.cardio.DAOOldCardio;
import com.easyfitness.utils.DateConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


// Uses http://javacsv.sourceforge.net/com/csvreader/CsvReader.html //
public class CVSManager {

    static private String TABLE_HEAD = "table";
    static private String ID_HEAD = "id";

    private Context mContext = null;

    public CVSManager(Context pContext) {
        mContext = pContext;
    }

    public boolean exportDatabase(Profile pProfile) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        /**First of all we check if the external storage of the device is available for writing.
         * Remember that the external storage is not necessarily the sd card. Very often it is
         * the device storage.
         */
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s_");
            Date date = new Date();

            //We use the FastNFitness directory for saving our .csv file.
            File exportDir = Environment.getExternalStoragePublicDirectory("/FastnFitness/export/" + dateFormat.format(date) + pProfile.getName());
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            PrintWriter printWriter = null;
            try {
                exportFontes(exportDir, pProfile);
                exportCardio(exportDir, pProfile);
                exportProfileWeight(exportDir, pProfile);
                exportBodyMeasures(exportDir, pProfile);
                exportExercise(exportDir, pProfile);
            } catch (Exception e) {
                //if there are any exceptions, return false
                e.printStackTrace();
                return false;
            } finally {
                if (printWriter != null) printWriter.close();
            }

            //If there are no errors, return true.
            return true;
        }
    }

    public boolean exportFontes(File exportDir, Profile pProfile) {
        try {
            // FONTE
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s");
            Date date = new Date();

            CsvWriter csvOutputFonte = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_BodyBuilding_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));

            /**This is our database connector class that reads the data from the database.
             * The code of this class is omitted for brevity.
             */
            DAOFonte dbcFonte = new DAOFonte(mContext);
            dbcFonte.open();

            /**Let's read the first table of the database.
             * getFirstTable() is a method in our DBCOurDatabaseConnector class which retrieves a Cursor
             * containing all records of the table (all fields).
             * The code of this class is omitted for brevity.
             */
            List<Fonte> records = null;
            records = dbcFonte.getAllBodyBuildingRecordsByProfileArray(pProfile);

            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
            csvOutputFonte.write(TABLE_HEAD);
            csvOutputFonte.write(ID_HEAD);
            csvOutputFonte.write(DAOFonte.DATE);
            csvOutputFonte.write(DAOFonte.EXERCISE);
            csvOutputFonte.write(DAOFonte.WEIGHT);
            csvOutputFonte.write(DAOFonte.REPETITION);
            csvOutputFonte.write(DAOFonte.SERIE);
            csvOutputFonte.write(DAOFonte.PROFIL_KEY);
            csvOutputFonte.write(DAOFonte.UNIT);
            csvOutputFonte.write(DAOFonte.NOTES);
            csvOutputFonte.endRecord();

            for (int i = 0; i < records.size(); i++) {
                csvOutputFonte.write(DAOFonte.TABLE_NAME);
                csvOutputFonte.write(Long.toString(records.get(i).getId()));

                Date dateRecord = records.get(i).getDate();

                SimpleDateFormat dateFormatcsv = new SimpleDateFormat(DAOUtils.DATE_FORMAT);

                csvOutputFonte.write(dateFormatcsv.format(dateRecord));
                csvOutputFonte.write(records.get(i).getExercise());
                csvOutputFonte.write(Float.toString(records.get(i).getPoids()));
                csvOutputFonte.write(Integer.toString(records.get(i).getRepetition()));
                csvOutputFonte.write(Integer.toString(records.get(i).getSerie()));
                if (records.get(i).getProfil() != null)
                    csvOutputFonte.write(Long.toString(records.get(i).getProfil().getId()));
                else csvOutputFonte.write("-1");
                csvOutputFonte.write(Integer.toString(records.get(i).getUnit()));
                if (records.get(i).getNote() == null) csvOutputFonte.write("");
                else csvOutputFonte.write(records.get(i).getNote());
                csvOutputFonte.endRecord();
            }
            csvOutputFonte.close();
            dbcFonte.closeAll();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    public boolean exportProfileWeight(File exportDir, Profile pProfile) {
        try {
            // FONTE
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s");
            Date date = new Date();

            // use FileWriter constructor that specifies open for appending
            CsvWriter csvOutputWeight = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_ProfilWeight_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));
            DAOWeight dbcWeight = new DAOWeight(mContext);
            dbcWeight.open();

            List<ProfileWeight> weightRecords;
            weightRecords = dbcWeight.getWeightList(pProfile);

            csvOutputWeight.write(TABLE_HEAD);
            csvOutputWeight.write(ID_HEAD);
            csvOutputWeight.write(DAOWeight.POIDS);
            csvOutputWeight.write(DAOWeight.DATE);
            csvOutputWeight.endRecord();

            for (int i = 0; i < weightRecords.size(); i++) {
                csvOutputWeight.write(DAOWeight.TABLE_NAME);
                csvOutputWeight.write(Long.toString(weightRecords.get(i).getId()));
                csvOutputWeight.write(Float.toString(weightRecords.get(i).getWeight()));

                Date dateRecord = weightRecords.get(i).getDate();
                SimpleDateFormat dateFormatcsv = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
                csvOutputWeight.write(dateFormatcsv.format(dateRecord));

                csvOutputWeight.endRecord();
            }
            csvOutputWeight.close();
            dbcWeight.close();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    public boolean exportBodyMeasures(File exportDir, Profile pProfile) {
        try {
            // FONTE
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s");
            Date date = new Date();

            // use FileWriter constructor that specifies open for appending
            CsvWriter cvsOutput = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_BodyMeasures_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));
            DAOBodyMeasure daoBodyMeasure = new DAOBodyMeasure(mContext);
            daoBodyMeasure.open();

            List<BodyMeasure> bodyMeasures;
            bodyMeasures = daoBodyMeasure.getBodyMeasuresList(pProfile);

            cvsOutput.write(TABLE_HEAD);
            cvsOutput.write(ID_HEAD);
            cvsOutput.write(DAOBodyMeasure.DATE);
            cvsOutput.write(DAOBodyMeasure.BODYPART_KEY);
            cvsOutput.write("bodypart_label");
            cvsOutput.write(DAOBodyMeasure.MEASURE);
            cvsOutput.write(DAOBodyMeasure.PROFIL_KEY);
            cvsOutput.endRecord();

            for (int i = 0; i < bodyMeasures.size(); i++) {
                cvsOutput.write(DAOBodyMeasure.TABLE_NAME);
                cvsOutput.write(Long.toString(bodyMeasures.get(i).getId()));
                Date dateRecord = bodyMeasures.get(i).getDate();
                cvsOutput.write(DateConverter.dateToDBDateStr(dateRecord));
                cvsOutput.write(Long.toString(bodyMeasures.get(i).getBodyPartID()));
                BodyPart bp = new BodyPart(bodyMeasures.get(i).getBodyPartID());
                cvsOutput.write(this.mContext.getString(bp.getResourceNameID())); // Write the full name of the BodyPart
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

    public boolean exportCardio(File exportDir, Profile pProfile) {
        try {
            // FONTE
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s");
            Date date = new Date();

            CsvWriter csvOutputCardio = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_Cardio_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));

            /**This is our database connector class that reads the data from the database.
             * The code of this class is omitted for brevity.
             */
            DAOCardio dbcCardio = new DAOCardio(mContext);
            dbcCardio.open();

            /**Let's read the first table of the database.
             * getFirstTable() is a method in our DBCOurDatabaseConnector class which retrieves a Cursor
             * containing all records of the table (all fields).
             * The code of this class is omitted for brevity.
             */
            List<Cardio> cardioRecords = null;
            cardioRecords = dbcCardio.getAllCardioRecordsByProfile(pProfile);

            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
            csvOutputCardio.write(TABLE_HEAD);
            csvOutputCardio.write(ID_HEAD);
            csvOutputCardio.write(DAOCardio.DATE);
            csvOutputCardio.write(DAOCardio.TIME);
            csvOutputCardio.write(DAOCardio.EXERCISE);
            csvOutputCardio.write(DAOCardio.DURATION);
            csvOutputCardio.write(DAOCardio.DISTANCE);
            csvOutputCardio.write(DAOCardio.PROFIL_KEY);
            csvOutputCardio.endRecord();

            for (int i = 0; i < cardioRecords.size(); i++) {
                csvOutputCardio.write(DAOCardio.TABLE_NAME);
                csvOutputCardio.write(Long.toString(cardioRecords.get(i).getId()));

                Date dateRecord = cardioRecords.get(i).getDate();

                SimpleDateFormat dateFormatcsv = new SimpleDateFormat(DAOUtils.DATE_FORMAT);

                csvOutputCardio.write(dateFormatcsv.format(dateRecord));
                csvOutputCardio.write(cardioRecords.get(i).getTime());
                csvOutputCardio.write(cardioRecords.get(i).getExercise());
                csvOutputCardio.write(Long.toString(cardioRecords.get(i).getDuration()));
                csvOutputCardio.write(Float.toString(cardioRecords.get(i).getDistance()));
                if (cardioRecords.get(i).getProfil() != null)
                    csvOutputCardio.write(Long.toString(cardioRecords.get(i).getProfil().getId()));
                else csvOutputCardio.write("-1");
                //write the record in the .csv file
                csvOutputCardio.endRecord();
            }
            csvOutputCardio.close();
            dbcCardio.close();
        } catch (Exception e) {
            //if there are any exceptions, return false
            e.printStackTrace();
            return false;
        }
        //If there are no errors, return true.
        return true;
    }

    public boolean exportExercise(File exportDir, Profile pProfile) {
        try {
            // FONTE
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s");
            Date date = new Date();

            CsvWriter csvOutput = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_Exercises_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));

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
            List<Machine> records = null;
            records = dbcMachine.getAllMachinesArray();

            //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
            csvOutput.write(TABLE_HEAD);
            csvOutput.write(ID_HEAD);
            csvOutput.write(DAOMachine.NAME);
            csvOutput.write(DAOMachine.DESCRIPTION);
            csvOutput.write(DAOMachine.TYPE);
            csvOutput.write(DAOMachine.BODYPARTS);
            csvOutput.write(DAOMachine.FAVORITES);
            //csvOutput.write(DAOMachine.PICTURE);
            csvOutput.endRecord();

            for (int i = 0; i < records.size(); i++) {
                csvOutput.write(DAOMachine.TABLE_NAME);
                csvOutput.write(Long.toString(records.get(i).getId()));
                csvOutput.write(records.get(i).getName());
                csvOutput.write(records.get(i).getDescription());
                csvOutput.write(Integer.toString(records.get(i).getType()));
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

    /*
     * TODO : Renforcer cette fonction.
     */
    public boolean importDatabase(String file, Profile pProfile) {

        boolean ret = true;

        try {
            CsvReader csvRecords = new CsvReader(file, ',', Charset.forName("UTF-8"));

            csvRecords.readHeaders();

            while (csvRecords.readRecord()) {
                switch (csvRecords.get(TABLE_HEAD)) {
                    case DAOFonte.TABLE_NAME: {
                        DAOFonte dbcFonte = new DAOFonte(mContext);
                        DAOCardio dbcCardio = new DAOCardio(mContext);
                        DAOMachine dbcMachine = new DAOMachine(mContext);
                        dbcFonte.open();
                        Date date;
                        try {
                            date = new SimpleDateFormat(DAOUtils.DATE_FORMAT)
                                .parse(csvRecords.get(DAOFonte.DATE));

                            String machine = csvRecords.get(DAOFonte.EXERCISE);
                            if (dbcMachine.getMachine(machine).getType() == DAOMachine.TYPE_FONTE) {
                                float poids = Float.valueOf(csvRecords.get(DAOFonte.WEIGHT));
                                int repetition = Integer.valueOf(csvRecords.get(DAOFonte.REPETITION));
                                int serie = Integer.valueOf(csvRecords.get(DAOFonte.SERIE));
                                int unit = 0;
                                if (!csvRecords.get(DAOFonte.UNIT).isEmpty()) {
                                    unit = Integer.valueOf(csvRecords.get(DAOFonte.UNIT));
                                }
                                String notes = csvRecords.get(DAOFonte.NOTES);
                                String time = csvRecords.get(DAOFonte.TIME);
                                dbcFonte.addBodyBuildingRecord(date, machine, serie, repetition, poids, pProfile, unit, notes, time);
                                dbcFonte.close();
                            } else {
                                String time = csvRecords.get(DAOCardio.TIME);
                                String exercise = csvRecords.get(DAOCardio.EXERCISE);
                                float distance = Float.valueOf(csvRecords.get(DAOCardio.DISTANCE));
                                int duration = Integer.valueOf(csvRecords.get(DAOCardio.DURATION));
                                dbcCardio.addCardioRecord(date, time, exercise, distance, duration, pProfile);
                                dbcCardio.close();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            ret = false;
                        }
                        break;
                    }
                    case DAOOldCardio.TABLE_NAME: {
                        DAOCardio dbcCardio = new DAOCardio(mContext);
                        dbcCardio.open();
                        Date date;
                        try {
                            date = new SimpleDateFormat(DAOUtils.DATE_FORMAT)
                                .parse(csvRecords.get(DAOCardio.DATE));

                            String exercice = csvRecords.get(DAOOldCardio.EXERCICE);
                            float distance = Float.valueOf(csvRecords.get(DAOOldCardio.DISTANCE));
                            int duration = Integer.valueOf(csvRecords.get(DAOOldCardio.DURATION));
                            dbcCardio.addCardioRecord(date, "", exercice, distance, duration, pProfile);
                            dbcCardio.close();
                        } catch (ParseException e) {
                            e.printStackTrace();
                            ret = false;
                        }
                        break;
                    }
                    case DAOWeight.TABLE_NAME: {
                        DAOWeight dbcWeight = new DAOWeight(mContext);
                        dbcWeight.open();
                        Date date;
                        try {
                            date = new SimpleDateFormat(DAOUtils.DATE_FORMAT)
                                .parse(csvRecords.get(DAOWeight.DATE));

                            float poids = Float.valueOf(csvRecords.get(DAOWeight.POIDS));
                            dbcWeight.addWeight(date, poids, pProfile);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            ret = false;
                        }
                        break;
                    }
                    case DAOProfil.TABLE_NAME:
                        // TODO : import profiles
                        break;
                    case DAOMachine.TABLE_NAME:
                        DAOMachine dbc = new DAOMachine(mContext);
                        String name = csvRecords.get(DAOMachine.NAME);
                        String description = csvRecords.get(DAOMachine.DESCRIPTION);
                        int type = Integer.valueOf(csvRecords.get(DAOMachine.TYPE));
                        boolean favorite = Boolean.valueOf(csvRecords.get(DAOMachine.FAVORITES));
                        // Check if this machine doesn't exist
                        if (dbc.getMachine(name) == null) {
                            dbc.addMachine(name, description, type, "", favorite);
                        }
                        break;
                }
            }

            csvRecords.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ret = false;
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }

        return ret;
    }
}
