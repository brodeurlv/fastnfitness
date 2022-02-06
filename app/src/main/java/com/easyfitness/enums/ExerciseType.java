package com.easyfitness.enums;

public enum ExerciseType {
    STRENGTH("strength"),
    CARDIO("cardio"),
    ISOMETRIC("isometric");

    private String mDisplayName = "";

    ExerciseType(String displayName) {
        this.mDisplayName = displayName;
    }

    public String toString() {
        return mDisplayName;
    }

    public static ExerciseType fromInteger(int x) {
        switch (x) {
            case 0:
                return STRENGTH;
            case 1:
                return CARDIO;
            case 2:
                return ISOMETRIC;
        }
        return null;
    }

    public static ExerciseType fromString(String x) throws Exception{
        if (x.equals(STRENGTH.mDisplayName)) return STRENGTH;
        else if (x.equals(CARDIO.mDisplayName)) return CARDIO;
        else if (x.equals(ISOMETRIC.mDisplayName)) return ISOMETRIC;

        throw new Exception("Illegal record type string");
    }
}
