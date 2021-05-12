package com.easyfitness.utils;

import android.content.Context;

import com.easyfitness.DAO.DAOUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;

public class DateConverter {

    static final int MILLISECONDINDAY = 60 * 60 * 24 * 1000;

    public DateConverter() {
    }

    static public double nbDays(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return (calendar.getTimeInMillis() + calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / MILLISECONDINDAY;
    }

    static public double nbMinutes(double millisecondes) {
        return (double) (millisecondes / (60 * 1000));
    }

    static public double nbMilliseconds(double days) {
        return days * MILLISECONDINDAY;
    }


    static public Date getNewDate() {
        return new Date();
    }

    static public Date localDateTimeStrToDateTime(String dateText, String timeText, Context pContext) {
        Date date;
        try {
            if (timeText.isEmpty()) {
                date = localDateStrToDate(dateText, pContext);
            } else {
                String dateFormat = ((SimpleDateFormat) getDateFormat(pContext.getApplicationContext())).toLocalizedPattern();
                String timeFormat = ((SimpleDateFormat) getTimeFormat(pContext.getApplicationContext())).toLocalizedPattern();
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat(dateFormat + "'T'" + timeFormat);
                date = dateTimeFormat.parse(dateText + "T" + timeText);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }

        return date;
    }

    static public Date localDateStrToDate(String dateStr, Context pContext) {
        Date date;
        try {
            DateFormat dateFormat = getDateFormat(pContext.getApplicationContext());
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        return date;
    }

    static public Date localTimeStrToDate(String timeStr, Context pContext) {
        Date date;
        try {
            DateFormat timeFormat = getTimeFormat(pContext.getApplicationContext());
            date = timeFormat.parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        return date;
    }

    static public String dateToLocalDateStr(Date date, Context pContext) {
        DateFormat dateFormat3 = getDateFormat(pContext.getApplicationContext());
        return dateFormat3.format(date);
    }

    static public String dateTimeToDBDateStr(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }

    static public String dateToDBDateStr(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
        // No time is given, use local time
        return dateFormat.format(date);
    }

    static public Date DBDateStrToDate(String dateStr) {
        Date date;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT);
            // No time is given, use local time
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        return date;
    }

    static public Date DBDateTimeStrToDate(String dateStr, String timeStr) {
        Date date;
        try {
            if (timeStr.isEmpty()) { // For old record where there was no Time
                date = DBDateStrToDate(dateStr);
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.DATE_FORMAT + "'T'" + DAOUtils.TIME_FORMAT);
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                date = dateFormat.parse(dateStr + "T" + timeStr);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        return date;
    }

    static public String dateToLocalTimeStr(Date date, Context pContext) {
        DateFormat dateFormat3 = getTimeFormat(pContext.getApplicationContext());
        return dateFormat3.format(date);
    }

    static public String dateTimeToDBTimeStr(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DAOUtils.TIME_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }

    static public String currentTime(Context pContext) {
        //Rajoute le moment du dernier ajout dans le bouton Add
        Date date = new Date();
        return dateToLocalTimeStr(date, pContext);
    }

    static public String currentDate(Context pContext) {
        //Rajoute le moment du dernier ajout dans le bouton Add
        Date date = new Date();
        return dateToLocalDateStr(date, pContext);
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

    static public Date dateToDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);

        return calendar.getTime();
    }

    static public Date timeToDate(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

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

    /**
     * @param longVal in milliseconds
     * @return duration in format "HH:MM:SS"
     */
    public static String durationToHoursMinutesSecondsStr(long longVal) {
        longVal = longVal / 1000;
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }
}
