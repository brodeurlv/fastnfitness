package com.easyfitness.utils;

public enum WeightUnit {
    KG("Kg"),
    LBS("Lbs");

    private String mDisplayName = "";

    //Constructeur
    WeightUnit(String displayName){
        this.mDisplayName = displayName;
    }

    public String toString(){
        return mDisplayName;
    }

    public static WeightUnit fromInteger(int x) {
        switch(x) {
            case 0:
                return KG;
            case 1:
                return LBS;
        }
        return null;
    }

    public static WeightUnit fromString(String x) {
        if (x.equals(KG.mDisplayName)) return KG;
        else if (x.equals(LBS.mDisplayName)) return LBS;
        return null;
    }
}
