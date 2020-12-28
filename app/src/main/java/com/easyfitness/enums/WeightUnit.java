package com.easyfitness.enums;

public enum WeightUnit {
    KG("kg"),
    LBS("lb"),
    STONES("st");

    private String mDisplayName = "";

    //Constructeur
    WeightUnit(String displayName) {
        this.mDisplayName = displayName;
    }

    public static WeightUnit fromInteger(int x) {
        switch (x) {
            case 0:
                return KG;
            case 1:
                return LBS;
            case 2:
                return STONES;
        }
        return null;
    }

    public static WeightUnit fromString(String x) {
        if (x.equals(KG.mDisplayName)) return KG;
        else if (x.equals(LBS.mDisplayName)) return LBS;
        else if (x.equals(STONES.mDisplayName)) return STONES;
        return null;
    }

    public String toString() {
        return mDisplayName;
    }

    public Unit toUnit() {
        switch (ordinal()) {
            case 0:
                return Unit.KG;
            case 1:
                return Unit.LBS;
            case 2:
                return Unit.STONES;
        }
        return null;
    }
}
