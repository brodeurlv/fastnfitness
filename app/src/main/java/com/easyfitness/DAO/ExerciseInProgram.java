package com.easyfitness.DAO;

import java.util.Date;

/* DataBase Object */
public class ExerciseInProgram extends ARecord {
    // Notez que l'identifiant est un long
    private int mSerie;
    private int mRepetition;
    private float mPoids;
    private int mUnit;
    private String mNote;
    private int secRest;

    /*
     * Program(Date pDate, String pMachine, int pSerie, int pRepetition, int pPoids, Profile pProfile)
     */
//    public Program(Date pDate, String pMachine, int pSerie, int pRepetition, float pPoids, Profile pProfile, int pUnit, String pNote, long pMachineKey, String pTime) {
//        super();
//        this.secRest=60;
//        this.mDate = pDate;
//        this.mExercise = pMachine;
//        this.mSerie = pSerie;
//        this.mRepetition = pRepetition;
//        this.mPoids = pPoids;
//        this.mUnit = pUnit;
//        this.mNote = pNote;
//        this.mProfile = pProfile;
//        this.mExerciseId = pMachineKey;
//        this.mTime = pTime;
//        this.mType = DAOMachine.TYPE_FONTE;
//    }

    public ExerciseInProgram(int secRest, String pMachine, int pSerie, int pRepetition, float pPoids, Profile pProfile, int pUnit, String pNote, long pMachineKey, String pTime) {
        super();
        this.secRest=secRest;
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

    public int getSecRest(){ return secRest; }

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
