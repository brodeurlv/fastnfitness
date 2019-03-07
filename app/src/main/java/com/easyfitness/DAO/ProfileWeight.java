package com.easyfitness.DAO;

import java.util.Date;

/* DataBase Object */
public class ProfileWeight {
    // Notez que l'identifiant est un long
    private long id;
    private Date mDate;
    private float mWeight;
    private long mProfil_id;

    public ProfileWeight(long id, Date pDate, float pWeight, long pProfil_id) {
        super();
        this.id = id;
        this.mDate = pDate;
        this.mWeight = pWeight;
        this.mProfil_id = pProfil_id;
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

    public float getWeight() {
        return mWeight;
    }

    public long getProfilId() {
        return mProfil_id;
    }
}