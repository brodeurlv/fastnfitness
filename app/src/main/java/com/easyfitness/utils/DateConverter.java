package com.easyfitness.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.easyfitness.DAO.DAOUtils;

public class DateConverter {

	public DateConverter() {
	}
	
	static public Date getNewDate() {
		return new Date();
	}
	
	static public Date editToDate(String editText) {
		Date date;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			date = dateFormat.parse(editText);
		} catch (ParseException e) {
			e.printStackTrace();
			date = new Date(0);
		}
		
		return date;
	}
	
	static public String dateToDatabase(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
		return dateFormat.format(date);		
	}
	
	static public Date databaseToDate(String dateFromDatabase) {
		Date date;
		try {
			date = new SimpleDateFormat(DAOUtils.DATE_FORMAT)
					.parse(dateFromDatabase);
		} catch (ParseException e) {
			e.printStackTrace();
			date = new Date();
		}
		return date;
	}
	
	static public String currentTime() {
		//Rajoute le moment du dernier ajout dans le bouton Add
		Calendar calendar = Calendar.getInstance();
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);
		
		DecimalFormat df = new DecimalFormat("00");		
	    return df.format(hours)+":"+df.format(minutes)+":"+df.format(seconds);
	}
	
	static public String currentDate() {
		//Rajoute le moment du dernier ajout dans le bouton Add
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		
		DecimalFormat df = new DecimalFormat("00");		
	    return df.format(day)+"/"+df.format(month+1)+"/"+df.format(year);
	}
	
	static public String dateToString(int year, int month, int day) {
		// Do something with the date chosen by the user
		DecimalFormat df = new DecimalFormat("00");
		String date = df.format(day) + "/" + df.format(month) + "/" + df.format(year);
		return date;
	}

}
