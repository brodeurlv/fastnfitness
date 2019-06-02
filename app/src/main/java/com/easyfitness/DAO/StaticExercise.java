package com.easyfitness.DAO;

import java.util.Date;

/* DataBase Object */
public class StaticExercise extends ARecord {
    // Notez que l'identifiant est un long
    private int mSerie;
    private int mSecond;
    private float mPoids;
    private int mUnit;
    private String mNote;

    /*
     * Fonte(Date pDate, String pMachine, int pSerie, int pSecond, int pPoids, Profile pProfile)
     */
    public StaticExercise(Date pDate, String pMachine, int pSerie, int pSecond, float pPoids, Profile pProfile, int pUnit, long pMachineKey, String pTime) {
        super();
        this.mDate = pDate;
        this.mExercise = pMachine;
        this.mSerie = pSerie;
        this.mSecond = pSecond;
        this.mPoids = pPoids;
        this.mUnit = pUnit;
        this.mProfile = pProfile;
        this.mExerciseId = pMachineKey;
        this.mTime = pTime;
        this.mType = DAOMachine.TYPE_STATIC;
    }

    public int getSerie() {
        return mSerie;
    }

    public int getSecond() {
        return mSecond;
    }

    public float getPoids() {
        return mPoids;
    }

    public int getUnit() {
        return mUnit;
    }

    public String getNote() {
        return mNote;
    }
}
