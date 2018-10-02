package com.easyfitness.DAO.cardio;

import com.easyfitness.DAO.Profile;

import java.util.Date;

/* DataBase Object */
public class OldCardio {
    // Notez que l'identifiant est un long
    private long id;
    private Date mDate;
    private String mExercice;
    private float mDistance;
    private long mDuration;
    private Profile mProfile;

    public OldCardio(Date pDate, String pExercice, float pDistance, long pDuration, Profile pProfile) {
        super();
        this.mDate = pDate;
        this.mExercice = pExercice;
        this.mDistance = pDistance;
        this.mDuration = pDuration;
        this.mProfile = pProfile;
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

    public String getExercice() {
        return mExercice;
    }

    public float getDistance() {
        return mDistance;
    }

    public long getDuration() {
        return mDuration;
    }

    public Profile getProfil() {
        return mProfile;
    }
}