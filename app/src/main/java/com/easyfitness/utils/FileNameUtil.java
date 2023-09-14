package com.easyfitness.utils;

import androidx.annotation.NonNull;

public class FileNameUtil {

    public static final String FILE_ENDING_CSV = "csv";
    public static final String FILE_ENDING_ZIP = "zip";
    @NonNull
    public static String getExtension(String path) {
        return path.substring(path.lastIndexOf(".") + 1);
    }
}
