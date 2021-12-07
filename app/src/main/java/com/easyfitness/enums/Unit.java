package com.easyfitness.enums;

import java.util.Locale;

public enum Unit {
    KG("kg", UnitType.WEIGHT),
    LBS("lb", UnitType.WEIGHT),
    STONES("st", UnitType.WEIGHT),
    KM("km", UnitType.DISTANCE),
    MILES("miles", UnitType.DISTANCE),
    CM("cm", UnitType.SIZE),
    INCH("in", UnitType.SIZE),
    PERCENTAGE("%", UnitType.PERCENTAGE),
    UNITLESS("", UnitType.NONE);

    private final String mDisplayName;
    private UnitType mUnitType;

    //Constructeur
    Unit(String displayName, UnitType unitType) {
        mDisplayName = displayName;
        mUnitType = unitType;
    }

    public static Unit fromInteger(int x) {
        switch (x) {
            case 0:
                return KG;
            case 1:
                return LBS;
            case 2:
                return STONES;
            case 3:
                return KM;
            case 4:
                return MILES;
            case 5:
                return CM;
            case 6:
                return INCH;
            case 7:
                return PERCENTAGE;
            case 8:
            default:
                return UNITLESS;
        }
    }

    public static Unit fromString(String x) {
        if (x.toLowerCase(Locale.ROOT).equals(KG.mDisplayName)) return KG;
        if (x.toLowerCase(Locale.ROOT).equals(LBS.mDisplayName)) return LBS;
        if (x.toLowerCase(Locale.ROOT).equals(STONES.mDisplayName)) return STONES;

        if (x.toLowerCase(Locale.ROOT).equals(KM.mDisplayName)) return KM;
        if (x.toLowerCase(Locale.ROOT).equals(MILES.mDisplayName)) return MILES;

        if (x.toLowerCase(Locale.ROOT).equals(CM.mDisplayName)) return CM;
        if (x.toLowerCase(Locale.ROOT).equals(INCH.mDisplayName)) return INCH;
        if (x.toLowerCase(Locale.ROOT).equals(PERCENTAGE.mDisplayName)) return PERCENTAGE;
        if (x.toLowerCase(Locale.ROOT).equals(UNITLESS.mDisplayName)) return UNITLESS;
        return null;
    }

    public String toString() {
        return mDisplayName;
    }

    public UnitType getUnitType() {
        return mUnitType;
    }
    public void setUnitType(UnitType unitType) {
        mUnitType = unitType;
    }
}
