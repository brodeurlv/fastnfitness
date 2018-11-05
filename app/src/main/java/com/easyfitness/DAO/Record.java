package com.easyfitness.DAO;

import java.util.Date;

/* DataBase Object */
public class Record {
    protected long id;
    protected Date mDate;
    protected String mExercise;
    protected long mExerciseId;
    protected Profile mProfile;
    protected String mTime; // Time in HH:MM:SS
    protected int mType; // Time in HH:MM:SS


    public Record() {
        super();
    }

    public Record(Date pDate, String pMachine, Profile pProfile, long pMachineKey, String pTime, int pType) {
        super();
        this.mDate = pDate;
        this.mExercise = pMachine;
        this.mProfile = pProfile;
        this.mExerciseId = pMachineKey;
        this.mTime = pTime;
        this.mType = pType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return mDate;
    }

    public String getExercise() {
        return mExercise;
    }

    public void setExercise(String exercise) {
        this.mExercise = exercise;
    }

    public long getExerciseKey() {
        return mExerciseId;
    }

    public void setExerciseKey(long id) {
        this.mExerciseId = id;
    }

    public Profile getProfil() {
        return mProfile;
    }

    public long getProfilKey() {
        return mProfile.getId();
    }

    public String getTime() {
        return mTime;
    }

    public int getType() {
        return mType;
    }

    public void setType(int pType) {
        this.mType = pType;
    }

}