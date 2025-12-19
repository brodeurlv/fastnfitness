package com.easyfitness.DAO;


public class DAOUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";

    public static String sanitizeStringForSqlValue(String string) {
        return string.replace("'", "''");
    }

    //TODO make this escape more characters if needed
    public static String sanitizeStringForSqlLike(String string) {
        string = string.replace("'", "''");
        return string;
    }
}
