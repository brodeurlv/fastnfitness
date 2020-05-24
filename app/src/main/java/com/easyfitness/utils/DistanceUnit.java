package com.easyfitness.utils;

public enum DistanceUnit {
    KM("Km"),
    MILES("Miles");

    private String mDisplayName = "";

    //Constructeur
    DistanceUnit(String displayName){
        this.mDisplayName = displayName;
    }

    public String toString(){
        return mDisplayName;
    }

    public static DistanceUnit fromInteger(int x) {
        switch(x) {
            case 0:
                return KM;
            case 1:
                return MILES;
        }
        return null;
    }

    public static DistanceUnit fromString(String x) {
        if (x.equals(KM.mDisplayName)) return KM;
        else if (x.equals(MILES.mDisplayName)) return MILES;
        return null;
    }
}
