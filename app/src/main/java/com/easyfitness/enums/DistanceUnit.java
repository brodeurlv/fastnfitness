package com.easyfitness.enums;

public enum DistanceUnit {
    KM("km"),
    MILES("miles");

    private String mDisplayName = "";

    //Constructeur
    DistanceUnit(String displayName) {
        this.mDisplayName = displayName;
    }

    public static DistanceUnit fromInteger(int x) {
        switch (x) {
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

    public String toString() {
        return mDisplayName;
    }

    public Unit toUnit() {
        switch (ordinal()) {
            case 0:
                return Unit.KM;
            case 1:
                return Unit.MILES;
        }
        return null;
    }
}
