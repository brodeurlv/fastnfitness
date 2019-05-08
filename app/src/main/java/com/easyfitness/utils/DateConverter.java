package com.easyfitness.utils;

import android.content.Context;

import com.easyfitness.DAO.DAOUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.text.format.DateFormat.getDateFormat;

public class DateConverter {

    static final int MILLISECONDINDAY = 60 * 60 * 24 * 1000;

    public DateConverter() {
    }

    static public double nbDays(double millisecondes) {
        return (int) (millisecondes / MILLISECONDINDAY);
    }

    static public double nbMinutes(double millisecondes) {
        return (int) (millisecondes / (60 * 1000));
    }

    static public double nbMilliseconds(double days) {
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
            DateFormat dateFormat = getDateFormat(pContext.getApplicationContext());
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        return date;
    }

    static public String dateToLocalDateStr(Date date, Context pContext) {
        DateFormat dateFormat3 = getDateFormat(pContext.getApplicationContext());
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
            date = new Date(0);
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
        return df.format(hours) + ":" + df.format(minutes) + ":" + df.format(seconds);
    }

    static public String currentDate() {
        //Rajoute le moment du dernier ajout dans le bouton Add
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DecimalFormat df = new DecimalFormat("00");
        return df.format(day) + "/" + df.format(month + 1) + "/" + df.format(year);
    }

    static public String dateToString(int year, int month, int day) {
        // Do something with the date chosen by the user
        DecimalFormat df = new DecimalFormat("00");
        return df.format(day) + "/" + df.format(month) + "/" + df.format(year);
    }

    /**
     * @param year
     * @param month    0-based
     * @param day
     * @param pContext
     * @return date for local format
     */
    static public String dateToLocalDateStr(int year, int month, int day, Context pContext) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        Date date = calendar.getTime();

        return dateToLocalDateStr(date, pContext);
    }

    static public Date dateToDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    /**
     * @param longVal in milliseconds
     * @return duration in format "HH:MM"
     */
    public static String durationToHoursMinutesStr(long longVal) {
        longVal = longVal / 1000;
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        //remainder = remainder - mins * 60;
        //int secs = remainder;

        return String.format("%02d:%02d", hours, mins);
    }
}
