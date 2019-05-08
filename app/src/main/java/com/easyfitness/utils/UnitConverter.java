package com.easyfitness.utils;

public class UnitConverter {

    public static final int UNIT_KG = 0;
    public static final int UNIT_LBS = 1;

    public UnitConverter() {
    }

    /*
     * convert Kg to Lbs
     */
    static public float weightConverter(float pWeight, int pUnitIn, int pUnitOut) {
        switch (pUnitIn) {
            case UNIT_KG:
                switch (pUnitOut) {
                    case UNIT_LBS:
                        return KgtoLbs(pWeight);
                    case UNIT_KG:
                    default:
                        return pWeight;
                }
            case UNIT_LBS:
                switch (pUnitOut) {
                    case UNIT_KG:
                        return LbstoKg(pWeight);
                    case UNIT_LBS:
                    default:
                        return pWeight;
                }
            default:
                return pWeight;
        }
    }

    /*
     * convert Kg to Lbs
     */
    static public float KgtoLbs(float pKg) {
        return pKg / (float) 0.45359237;
    }

    /*
     * convert Lbs to Kg
     */
    static public float LbstoKg(float pLbs) {
        return pLbs * (float) 0.45359237;
    }

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     *
     * @param currentDuration
     * @param totalDuration
     */
    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      -
     * @param totalDuration returns current duration in milliseconds
     */
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }
}
