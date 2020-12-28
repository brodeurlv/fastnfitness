package com.easyfitness.enums;

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
    private final UnitType mUnitType;

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
        if (x.equals(KG.mDisplayName)) return KG;
        if (x.equals(LBS.mDisplayName)) return LBS;
        if (x.equals(STONES.mDisplayName)) return STONES;

        if (x.equals(KM.mDisplayName)) return KM;
        if (x.equals(MILES.mDisplayName)) return MILES;

        if (x.equals(CM.mDisplayName)) return CM;
        if (x.equals(INCH.mDisplayName)) return INCH;
        if (x.equals(PERCENTAGE.mDisplayName)) return PERCENTAGE;
        if (x.equals(UNITLESS.mDisplayName)) return UNITLESS;
        return null;
    }

    public String toString() {
        return mDisplayName;
    }

    public UnitType getUnitType() {
        return mUnitType;
    }
}
