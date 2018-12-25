package com.easyfitness.DAO;

import java.util.Date;

/* DataBase Object */
public abstract class ARecord implements IRecord {
    protected long id;
    protected Date mDate;
    protected String mExercise;
    protected long mExerciseId;
    protected Profile mProfile;
    protected String mTime; // Time in HH:MM:SS
    protected int mType; // Time in HH:MM:SS


    public ARecord() {
        super();
    }

    public ARecord(Date pDate, String pMachine, Profile pProfile, long pMachineKey, String pTime, int pType) {
        super();
        this.mDate = pDate;
        this.mExercise = pMachine;
        this.mProfile = pProfile;
        this.mExerciseId = pMachineKey;
        this.mTime = pTime;
        this.mType = pType;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Date getDate() {
        return mDate;
    }

    @Override
    public String getExercise() {
        return mExercise;
    }

    @Override
    public void setExercise(String exercise) {
        this.mExercise = exercise;
    }

    @Override
    public long getExerciseKey() {
        return mExerciseId;
    }

    @Override
    public void setExerciseKey(long id) {
        this.mExerciseId = id;
    }

    @Override
    public Profile getProfil() {
        return mProfile;
    }

    @Override
    public long getProfilKey() {
        return mProfile.getId();
    }

    @Override
    public String getTime() {
        return mTime;
    }

    @Override
    public int getType() {
        return mType;
    }
}