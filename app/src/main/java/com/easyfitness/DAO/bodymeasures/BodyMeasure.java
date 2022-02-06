package com.easyfitness.DAO.bodymeasures;

import com.easyfitness.utils.Value;

import java.util.Date;

/* DataBase Object */
public class BodyMeasure {
    private final Date mDate;
    private final int mBodypart_id;
    private final long mProfile_id;
    private long mId;
    private Value mMeasure;

    public BodyMeasure(long id, Date pDate, int pBodypart_id,  Value pMeasure, long pProfile_id) {
        super();
        mId = id;
        mDate = pDate;
        mBodypart_id = pBodypart_id;
        mMeasure = pMeasure;
        mProfile_id = pProfile_id;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public Date getDate() {
        return mDate;
    }

    public int getBodyPartID() {
        return mBodypart_id;
    }

    public Value getBodyMeasure() {
        return mMeasure;
    }

    public void setBodyMeasure(Value bodyMeasure) {
        mMeasure = bodyMeasure;
    }

    public long getProfileID() {
        return mProfile_id;
    }

}
