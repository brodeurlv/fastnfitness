package com.easyfitness.DAO;

import java.util.Date;

/* DataBase Object */
public class Cardio extends ARecord {
    // Notez que l'identifiant est un long
    private float mDistance;
    private long mDuration;
    private int mDistanceUnit;

    public Cardio(Date pDate, String pExercice, float pDistance, long pDuration, Profile pProfile, String pTime, int pDistanceUnit) {
        this.mDate = pDate;
        this.mExercise = pExercice;
        this.mDistance = pDistance;
        this.mDuration = pDuration;
        this.mProfile = pProfile;
        this.mTime = pTime;
        mDistanceUnit = pDistanceUnit;
        this.mType = DAOMachine.TYPE_CARDIO;
    }

    public float getDistance() {
        return mDistance;
    }

    public long getDuration() {
        return mDuration;
    }

    public int getDistanceUnit() {
        return mDistanceUnit;
    }

}
