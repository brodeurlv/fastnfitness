package com.easyfitness.enums;

public enum SizeUnit {
    CM("cm"),
    INCH("in");

    private String mDisplayName = "";

    SizeUnit(String displayName) {
        this.mDisplayName = displayName;
    }

    public static SizeUnit fromInteger(int x) {
        switch (x) {
            case 0:
                return CM;
            case 1:
                return INCH;
        }
        return null;
    }

    public static SizeUnit fromString(String x) {
        if (x.equals(CM.mDisplayName)) return CM;
        else if (x.equals(INCH.mDisplayName)) return INCH;
        return null;
    }

    public static float CmToInch(double cm) {
        return (float) (cm * 0.393700787);
    }

    public static float InchToCm(double in) {
        return (float) (in / 0.393700787);
    }

    public String toString() {
        return mDisplayName;
    }


}
