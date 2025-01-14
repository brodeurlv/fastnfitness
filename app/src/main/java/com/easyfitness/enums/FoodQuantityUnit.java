package com.easyfitness.enums;

public enum FoodQuantityUnit {

    SERVINGS("servings"),
    PIECES("pieces"),
    GRAMS("grams"),
    OUNCES("ounces"),
    MILLILITERS("mL"),
    CUPS("Cups"),
    FLUID_OUNCES("fl. oz.");


    private String mDisplayName = "";

    //Constructeur
    FoodQuantityUnit(String displayName) {
        this.mDisplayName = displayName;
    }

    public static FoodQuantityUnit fromInteger(int x) {
        return switch (x) {
            case 0 -> SERVINGS;
            case 1 -> PIECES;
            case 2 -> GRAMS;
            case 3 -> OUNCES;
            case 4 -> MILLILITERS;
            case 5 -> CUPS;
            case 6 -> FLUID_OUNCES;
            default -> null;
        };
    }

    public static FoodQuantityUnit fromString(String x) {
        if (x.equals(SERVINGS.mDisplayName)) {
            return SERVINGS;
        }
        if (x.equals(PIECES.mDisplayName)) {
            return PIECES;
        }
        if (x.equals(GRAMS.mDisplayName)) {
            return GRAMS;
        }
        if (x.equals(OUNCES.mDisplayName)) {
            return OUNCES;
        }
        if (x.equals(MILLILITERS.mDisplayName)) {
            return MILLILITERS;
        }
        if (x.equals(CUPS.mDisplayName)) {
            return CUPS;
        }
        if (x.equals(FLUID_OUNCES.mDisplayName)) {
            return FLUID_OUNCES;
        }
        return null;
    }

    public String toString() {
        return mDisplayName;
    }
}
