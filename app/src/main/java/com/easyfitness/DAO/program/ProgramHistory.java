package com.easyfitness.DAO.program;

import com.easyfitness.enums.ProgramStatus;

/* DataBase Object */
public class ProgramHistory {
    private long mId;
    private long mProgramId;
    private long mProfileId;
    private ProgramStatus mStatus;

    private String mStartDate;
    private String mStartTime;
    private String mEndDate;
    private String mEndTime;

    public ProgramHistory(long id, long programId, long profileId, ProgramStatus status, String startDate, String startTime, String endDate, String endTime) {
        mId = id;
        mProgramId = programId;
        mProfileId = profileId;
        mStatus = status;
        mStartDate = startDate;
        mStartTime = startTime;
        mEndDate = endDate;
        mEndTime = endTime;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getProgramId() {
        return mProgramId;
    }

    public void setProgramId(long programId) {
        mProgramId = programId;
    }

    public long getProfileId() {
        return mProfileId;
    }

    public void setProfileId(long profileId) {
        mProfileId = profileId;
    }

    public ProgramStatus getStatus() {
        return mStatus;
    }

    public void setStatus(ProgramStatus status) {
        mStatus = status;
    }

    public String getStartDate() {
        return mStartDate;
    }

    public void setStartDate(String startDate) {
        mStartDate = startDate;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String startTime) {
        mStartTime = startTime;
    }

    public String getEndDate() {
        return mEndDate;
    }

    public void setEndDate(String endDate) {
        mEndDate = endDate;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public void setEndTime(String endTime) {
        mEndTime = endTime;
    }

}
