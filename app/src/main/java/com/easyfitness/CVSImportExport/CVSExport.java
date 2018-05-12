package com.easyfitness.CVSImportExport;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.csvreader.CsvWriter;
import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.DAOUtils;
import com.easyfitness.DAO.DAOWeight;
import com.easyfitness.DAO.Fonte;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.ProfileWeight;
import com.easyfitness.DAO.cardio.Cardio;
import com.easyfitness.DAO.cardio.DAOCardio;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// thank you to https://github.com/androidmads/SQLite2XL/blob/master/library/src/main/java/com/ajts/androidmads/library/SQLiteToExcel.java

public class CVSExport {

    static private String TABLE_HEAD = "table";
    static private String ID_HEAD = "id";
    static private String PROFIL_HEAD = "profil";


    private Context mContext;
    private SQLiteDatabase database;
    private String mDbName;
    private String mExportPath;

    public CVSExport(Context context, String dbName) {
        this(context, dbName, Environment.getExternalStorageDirectory().toString() + File.separator);
    }

    public CVSExport(Context context, String dbName, String exportPath) {
        mContext = context;
        mDbName = dbName;
        mExportPath = exportPath;
        try {
            database = SQLiteDatabase.openOrCreateDatabase(mContext.getDatabasePath(mDbName).getAbsolutePath(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getAllTables() {
        ArrayList<String> tables = new ArrayList<>();
        Cursor cursor = database.rawQuery("select name from sqlite_master where type='table' order by name", null);
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0));
        }
        cursor.close();
        return tables;
    }

    private ArrayList<String> getColumns(String table) {
        ArrayList<String> columns = new ArrayList<>();
        Cursor cursor = database.rawQuery("PRAGMA table_info(" + table + ")", null);
        while (cursor.moveToNext()) {
            columns.add(cursor.getString(1));
        }
        cursor.close();
        return columns;
    }

    private void exportTables(List<String> tables, final String fileName) {

        for (int i = 0; i < tables.size(); i++) {
            if (!tables.get(i).equals("android_metadata")) {
                // Create cvs file
                // export headers
                // export all rows
            }
        }
        File file = new File(mExportPath, fileName);
        database.close();
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
            // FONTE
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_H_m_s");
            Date date = new Date();

            //We use the FastNFitness directory for saving our .csv file.
            File exportDir = Environment.getExternalStoragePublicDirectory("/FastnFitness/export/" + dateFormat.format(date));
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File file;
            PrintWriter printWriter = null;
            try {
                CsvWriter csvOutputFonte = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_Weight_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));

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
                records = dbcFonte.getAllRecordsByProfilArray(pProfile);

                //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
                csvOutputFonte.write(TABLE_HEAD);
                csvOutputFonte.write(ID_HEAD);
                csvOutputFonte.write(DAOFonte.DATE);
                csvOutputFonte.write(DAOFonte.MACHINE);
                csvOutputFonte.write(DAOFonte.POIDS);
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
                    csvOutputFonte.write(records.get(i).getMachine());
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
                cardioRecords = dbcCardio.getAllRecordsByProfil(pProfile);

                //Write the name of the table and the name of the columns (comma separated values) in the .csv file.
                csvOutputCardio.write(TABLE_HEAD);
                csvOutputCardio.write(ID_HEAD);
                csvOutputCardio.write(DAOCardio.DATE);
                csvOutputCardio.write(DAOCardio.EXERCICE);
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
                    csvOutputCardio.write(cardioRecords.get(i).getExercice());
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

                // Profile weight
                // use FileWriter constructor that specifies open for appending
                CsvWriter csvOutputWeight = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_Profil_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF-8"));
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
            } finally {
                if (printWriter != null) printWriter.close();
            }

            //If there are no errors, return true.
            return true;
        }
    }
}
