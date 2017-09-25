package com.easyfitness.utils;

import android.content.Context;

import java.text.DateFormat;
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

	static final int MILLISECONDINDAY = 60 * 60 * 24 * 1000;

	static public double nbDays(double millisecondes){
		return (int)(millisecondes / MILLISECONDINDAY);
	}

	static public double nbMilliseconds(double days){
		return days * MILLISECONDINDAY;
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

	static public Date localDateStrToDate(String dateStr, Context pContext) {
		Date date;
		try {
			DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(pContext.getApplicationContext());
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			date = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			date = new Date();
		}
		return date;
	}

	static public String dateToLocalDateStr(Date date, Context pContext) {
		DateFormat dateFormat3 = android.text.format.DateFormat.getDateFormat(pContext.getApplicationContext());
		dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat3.format(date);
	}
	
	static public String dateToDBDateStr(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);
	}
	
	static public Date DBDateStrToDate(String dateStr) {
		Date date;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			date = dateFormat.parse(dateStr);
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
