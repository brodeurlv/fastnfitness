package com.easyfitness.graph;

public enum ZoomType {
    ZOOM_ALL,
    ZOOM_YEAR,
    ZOOM_MONTH,
    ZOOM_WEEK;

    public static ZoomType fromInteger(int x) {
        switch (x) {
            case 1:
                return ZOOM_YEAR;
            case 2:
                return ZOOM_MONTH;
            case 3:
                return ZOOM_WEEK;
            case 0:
            default:
                return ZOOM_ALL;
        }
    }
}
