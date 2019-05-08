package com.easyfitness.DAO;

import java.util.Date;

/* DataBase Object */
public class Fonte extends ARecord {
    // Notez que l'identifiant est un long
    private int mSerie;
    private int mRepetition;
    private float mPoids;
    private int mUnit;
    private String mNote;

    /*
     * Fonte(Date pDate, String pMachine, int pSerie, int pRepetition, int pPoids, Profile pProfile)
     */
    public Fonte(Date pDate, String pMachine, int pSerie, int pRepetition, float pPoids, Profile pProfile, int pUnit, String pNote, long pMachineKey, String pTime) {
        super();
        this.mDate = pDate;
        this.mExercise = pMachine;
        this.mSerie = pSerie;
        this.mRepetition = pRepetition;
        this.mPoids = pPoids;
        this.mUnit = pUnit;
        this.mNote = pNote;
        this.mProfile = pProfile;
        this.mExerciseId = pMachineKey;
        this.mTime = pTime;
        this.mType = DAOMachine.TYPE_FONTE;
    }

    public int getSerie() {
        return mSerie;
    }

    public int getRepetition() {
        return mRepetition;
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
