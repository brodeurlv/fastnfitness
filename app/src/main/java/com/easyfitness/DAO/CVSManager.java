package com.easyfitness.DAO;

import android.content.Context;
import android.os.Environment;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.easyfitness.DAO.cardio.Cardio;
import com.easyfitness.DAO.cardio.DAOCardio;

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
	static private String ID_HEAD =  "id";
	static private String PROFIL_HEAD = "profil";
	
	
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
		}
		else {
			//We use the Download directory for saving our .csv file.
			File exportDir = Environment.getExternalStoragePublicDirectory("/FastnFitness/export");
			if (!exportDir.exists()) 
			{
				exportDir.mkdirs();
			}

			File file;
			PrintWriter printWriter = null;
			try 
			{
				// FONTE
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_H_m_s");
				Date date = new Date();
				
				CsvWriter csvOutputFonte = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_Weight_" +dateFormat.format(date) + ".csv", ',', Charset.forName("UTF_8"));
				
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
				
				for (int i = 0; i<records.size();i++) {
					csvOutputFonte.write(DAOFonte.TABLE_NAME);
					csvOutputFonte.write(Long.toString(records.get(i).getId()));
					
					Date dateRecord = records.get(i).getDate();

					SimpleDateFormat dateFormatcsv = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
				
					csvOutputFonte.write(dateFormatcsv.format(dateRecord));
					csvOutputFonte.write(records.get(i).getMachine());
					csvOutputFonte.write(Float.toString(records.get(i).getPoids()));
					csvOutputFonte.write(Integer.toString(records.get(i).getRepetition()));
					csvOutputFonte.write(Integer.toString(records.get(i).getSerie()));
					if ( records.get(i).getProfil()  != null) csvOutputFonte.write(Long.toString(records.get(i).getProfil().getId()));
					else csvOutputFonte.write("-1"); 
					csvOutputFonte.write(Integer.toString(records.get(i).getUnit()));
					if ( records.get(i).getNote() == null ) csvOutputFonte.write("");
					else csvOutputFonte.write(records.get(i).getNote());
					csvOutputFonte.endRecord();
				}
				csvOutputFonte.close();
				dbcFonte.closeAll();	
				

				CsvWriter csvOutputCardio = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_Cardio_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF_8"));
				
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
				
				for (int i = 0; i<cardioRecords.size();i++) {
					csvOutputCardio.write(DAOCardio.TABLE_NAME);
					csvOutputCardio.write(Long.toString(cardioRecords.get(i).getId()));
					
					Date dateRecord = cardioRecords.get(i).getDate();

					SimpleDateFormat dateFormatcsv = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
				
					csvOutputCardio.write(dateFormatcsv.format(dateRecord));
					csvOutputCardio.write(cardioRecords.get(i).getExercice());
					csvOutputCardio.write(Long.toString(cardioRecords.get(i).getDuration()));
					csvOutputCardio.write(Float.toString(cardioRecords.get(i).getDistance()));
					if ( cardioRecords.get(i).getProfil()  != null) csvOutputCardio.write(Long.toString(cardioRecords.get(i).getProfil().getId()));
					else csvOutputCardio.write("-1"); 
					//write the record in the .csv file
					csvOutputCardio.endRecord();
				}
				csvOutputCardio.close();
				dbcCardio.close();	
				
				// Profile weight
				// use FileWriter constructor that specifies open for appending
				CsvWriter csvOutputWeight = new CsvWriter(exportDir.getPath() + "/" + "EF_" + pProfile.getName() + "_Profil_" + dateFormat.format(date) + ".csv", ',', Charset.forName("UTF_8"));
				DAOWeight dbcWeight = new DAOWeight(mContext);
				dbcWeight.open();

                List<ProfileWeight> weightRecords;
				weightRecords = dbcWeight.getWeightList(pProfile);
				
				csvOutputWeight.write(TABLE_HEAD);
				csvOutputWeight.write(ID_HEAD);
				csvOutputWeight.write(DAOWeight.POIDS);
				csvOutputWeight.write(DAOWeight.DATE);
				csvOutputWeight.endRecord();
				
				for (int i = 0; i<weightRecords.size();i++) {
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
			}

			catch(Exception e) {
				//if there are any exceptions, return false
				e.printStackTrace();
				return false;
			}
			finally {
				if(printWriter != null) printWriter.close();
			}

			//If there are no errors, return true.
			return true;
		}
	}
	
	/*
	 * TODO : Renforcer cette fonction. 
	 */
	public boolean importDatabase(String file, Profile pProfile) {
		
		boolean ret = true;
		
		try {			
			CsvReader csvRecords = new CsvReader(file, ',', Charset.forName("UTF_8"));
		
			csvRecords.readHeaders();
			
			while (csvRecords.readRecord())
			{
				if (csvRecords.get(TABLE_HEAD).equals(DAOFonte.TABLE_NAME)) {
					DAOFonte dbcFonte = new DAOFonte(mContext);
					dbcFonte.open();
					Date date;
					try {
						date = new SimpleDateFormat(DAOUtils.DATE_FORMAT)
								.parse( csvRecords.get(DAOFonte.DATE));
						
						String machine = csvRecords.get(DAOFonte.MACHINE);
						float poids = Float.valueOf(csvRecords.get(DAOFonte.POIDS));
						int repetition = Integer.valueOf(csvRecords.get(DAOFonte.REPETITION));
						int serie = Integer.valueOf(csvRecords.get(DAOFonte.SERIE));
						int unit = 0;
						if (!csvRecords.get(DAOFonte.UNIT).isEmpty()) { unit = Integer.valueOf(csvRecords.get(DAOFonte.UNIT)); }
						String notes = csvRecords.get(DAOFonte.NOTES);
						String time = csvRecords.get(DAOFonte.TIME);
						dbcFonte.addRecord(date, machine, serie, repetition, poids, pProfile, unit, notes, time);
						dbcFonte.close();
					} catch (ParseException e) {
						e.printStackTrace();
						ret = false;
					}
				} else if (csvRecords.get(TABLE_HEAD).equals(DAOCardio.TABLE_NAME)) {
					DAOCardio dbcCardio = new DAOCardio(mContext);
					dbcCardio.open();
					Date date;
					try {
						date = new SimpleDateFormat(DAOUtils.DATE_FORMAT)
								.parse( csvRecords.get(DAOCardio.DATE));
												
						String exercice = csvRecords.get(DAOCardio.EXERCICE);
						float distance = Float.valueOf(csvRecords.get(DAOCardio.DISTANCE));
						long duration = Integer.valueOf(csvRecords.get(DAOCardio.DURATION));
						Cardio ft = new Cardio(date, exercice, distance, duration, pProfile);
						dbcCardio.addRecord(ft);
						dbcCardio.close();
					} catch (ParseException e) {
						e.printStackTrace();
						ret = false;
					}
				} else if (csvRecords.get(TABLE_HEAD).equals(DAOWeight.TABLE_NAME)) {
					DAOWeight dbcWeight = new DAOWeight(mContext);
					dbcWeight.open(); 
					Date date;
					try {
						date = new SimpleDateFormat(DAOUtils.DATE_FORMAT)
								.parse( csvRecords.get(DAOFonte.DATE));
						
						float poids = Float.valueOf(csvRecords.get(DAOFonte.POIDS));
						dbcWeight.addWeight(date, poids, pProfile);
					} catch (ParseException e) {
						e.printStackTrace();
						ret = false;
					}
				} else if (csvRecords.get(TABLE_HEAD).equals(DAOProfil.TABLE_NAME)) {
					// TODO : import des profils
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
