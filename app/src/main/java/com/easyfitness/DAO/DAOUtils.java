package com.easyfitness.DAO;


import java.text.SimpleDateFormat;
import java.util.Locale;

public class DAOUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat(DATE_FORMAT, Locale.US);
    }
    private static final String TIME_FORMAT = "HH:mm:ss";

    public static SimpleDateFormat getTimeFormat() {
        return new SimpleDateFormat(TIME_FORMAT, Locale.US);
    }

    public static SimpleDateFormat getDateTimeFormat() {
        return new SimpleDateFormat(DATE_FORMAT + "'T'" + TIME_FORMAT, Locale.US);
    }
}
