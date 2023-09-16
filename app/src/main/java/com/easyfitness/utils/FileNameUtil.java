package com.easyfitness.utils;

import androidx.annotation.NonNull;

public class FileNameUtil {

    public static final String MINE_TYPE_CSV = "text/comma-separated-values";
    public static final String MINE_TYPE_ZIP = "application/zip";
    public static final String FILE_ENDING_CSV = "csv";

    @NonNull
    public static String getExtension(String path) {
        return path.substring(path.lastIndexOf(".") + 1);
    }
}
