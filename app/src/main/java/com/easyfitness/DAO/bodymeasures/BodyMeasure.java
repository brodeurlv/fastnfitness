package com.easyfitness.DAO.bodymeasures;

import java.util.Date;

/* DataBase Object */
public class BodyMeasure {
    // Notez que l'identifiant est un long
    private long id;
    private Date mDate;
    private int mBodypart_id;
    private float mMeasure;
    private long mProfil_id;

    public BodyMeasure(long id, Date pDate, int pBodypart_id, float pMeasure, long pProfil_id) {
        super();
        this.id = id;
        this.mDate = pDate;
        this.mBodypart_id = pBodypart_id;
        this.mMeasure = pMeasure;
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

    /**
     * @return long Body Part ID
     */
    public int getBodyPartID() {
        return mBodypart_id;
    }

    public float getBodyMeasure() {
        return mMeasure;
    }

    public long getProfileID() {
        return mProfil_id;
    }

}
