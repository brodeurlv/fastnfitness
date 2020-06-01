package com.easyfitness.DAO.record;

import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.RecordType;

import java.util.Date;

/* DataBase Object */
public class Record {
    private long mId;

    private Date mDate;
    private String mTime; // Time in HH:MM:SS
    private String mExercise;
    private long mExerciseId;
    private long mProfileId;

    private int mSets;
    private int mReps;
    private float mWeight;
    private int mWeightUnit;

    private int mSecond;

    private float mDistance;
    private int mDistanceUnit;
    private long mDuration;

    private String mNote;

    private ExerciseType mExerciseType;
    private RecordType mRecordType;



    // Template part
    private int mTemplateSets;
    private int mTemplateReps;
    private float mTemplateWeight;
    private int mTemplateWeightUnit;
    private int mTemplateSeconds;
    private float mTemplateDistance;
    private int mTemplateDistanceUnit;
    private long mTemplateDuration;

    private long mTemplateId;
    private long mTemplateSessionId;

    private int mTemplateOrder;
    private ProgramRecordStatus mProgramRecordStatus;
    private String mTemplateName;

    public Record(Date date, String time, String exercise, long exerciseId, long profileId, int sets, int reps, float weight, int weightUnit, int second, float distance, int distanceUnit, long duration, String note, ExerciseType exerciseType) {
        this(date, time, exercise,  exerciseId, profileId,  sets, reps, weight, weightUnit, second, distance, distanceUnit, duration, note, exerciseType, -1, "" , -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, ProgramRecordStatus.SUCCESS, RecordType.FREE_RECORD_TYPE);
    }

    public Record(Date date, String time, String exercise, long exerciseId, long profileId, int sets, int reps, float weight, int weightUnit, int second, float distance, int distanceUnit, long duration, String note, ExerciseType exerciseType, long templateId, String templateName, long templateSessionId, int templateSets, int templateReps, float templateWeight, int templateWeightUnit, int templateSeconds, float templateDistance, int templateDistanceUnit, long templateDuration, int templateOrder, ProgramRecordStatus programRecordStatus, RecordType recordType) {
        mDate = date;
        mTime = time;
        mExercise = exercise;
        mExerciseId = exerciseId;
        mProfileId = profileId;
        mSets = sets;
        mReps = reps;
        mWeight = weight;
        mWeightUnit = weightUnit;
        mSecond = second;
        mDistance = distance;
        mDistanceUnit = distanceUnit;
        mDuration = duration;
        mNote = note;
        mExerciseType = exerciseType;
        mRecordType = recordType;
        mTemplateSets = templateSets;
        mTemplateReps = templateReps;
        mTemplateWeight = templateWeight;
        mTemplateWeightUnit = templateWeightUnit;
        mTemplateSeconds = templateSeconds;
        mTemplateDistance = templateDistance;
        mTemplateDistanceUnit = templateDistanceUnit;
        mTemplateDuration = templateDuration;
        mTemplateId = templateId;
        mTemplateName = templateName;
        mTemplateSessionId = templateSessionId;
        mTemplateOrder = templateOrder;
        mProgramRecordStatus = programRecordStatus;
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

    public void setDate(Date date) {
        mDate = date;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getExercise() {
        return mExercise;
    }

    public void setExercise(String exercise) {
        mExercise = exercise;
    }

    public long getExerciseId() {
        return mExerciseId;
    }

    public void setExerciseId(long exerciseId) {
        mExerciseId = exerciseId;
    }

    public long getProfileId() {
        return mProfileId;
    }

    public void setProfileId(long profileId) {
        mProfileId = profileId;
    }

    public int getSets() {
        return mSets;
    }

    public void setSets(int sets) {
        mSets = sets;
    }

    public int getReps() {
        return mReps;
    }

    public void setReps(int reps) {
        mReps = reps;
    }

    public float getWeight() {
        return mWeight;
    }

    public void setWeight(float weight) {
        mWeight = weight;
    }

    public int getWeightUnit() {
        return mWeightUnit;
    }

    public void setWeightUnit(int weightUnit) {
        mWeightUnit = weightUnit;
    }

    public int getSecond() {
        return mSecond;
    }

    public void setSecond(int second) {
        mSecond = second;
    }

    public float getDistance() {
        return mDistance;
    }

    public void setDistance(float distance) {
        mDistance = distance;
    }

    public int getDistanceUnit() {
        return mDistanceUnit;
    }

    public void setDistanceUnit(int distanceUnit) {
        mDistanceUnit = distanceUnit;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        mNote = note;
    }

    public ExerciseType getExerciseType() {
        return mExerciseType;
    }

    public void setExerciseType(ExerciseType exerciseType) {
        mExerciseType = exerciseType;
    }

    public RecordType getRecordType() {
        return mRecordType;
    }

    public void setRecordType(RecordType recordType) {
        mRecordType = recordType;
    }

    public int getTemplateOrder() {
        return mTemplateOrder;
    }

    public void setTemplateOrder(int templateOrder) {
        mTemplateOrder = templateOrder;
    }

    public String getTemplateName() {
        return mTemplateName;
    }

    public void setTemplateName(String templateName) {
        mTemplateName = templateName;
    }

    public long getTemplateId() {
        return mTemplateId;
    }

    public void setTemplateId(long templateId) {
        mTemplateId = templateId;
    }

    public int getTemplateSets() {
        return mTemplateSets;
    }

    public void setTemplateSets(int templateSets) {
        mTemplateSets = templateSets;
    }

    public int getTemplateReps() {
        return mTemplateReps;
    }

    public void setTemplateReps(int templateReps) {
        mTemplateReps = templateReps;
    }

    public float getTemplateWeight() {
        return mTemplateWeight;
    }

    public void setTemplateWeight(float templateWeight) {
        mTemplateWeight = templateWeight;
    }

    public int getTemplateWeightUnit() {
        return mTemplateWeightUnit;
    }

    public void setTemplateWeightUnit(int templateWeightUnit) {
        mTemplateWeightUnit = templateWeightUnit;
    }

    public int getTemplateSeconds() {
        return mTemplateSeconds;
    }

    public void setTemplateSeconds(int templateSeconds) {
        mTemplateSeconds = templateSeconds;
    }

    public float getTemplateDistance() {
        return mTemplateDistance;
    }

    public void setTemplateDistance(float templateDistance) {
        mTemplateDistance = templateDistance;
    }

    public int getTemplateDistanceUnit() {
        return mTemplateDistanceUnit;
    }

    public void setTemplateDistanceUnit(int templateDistanceUnit) {
        mTemplateDistanceUnit = templateDistanceUnit;
    }

    public long getTemplateDuration() {
        return mTemplateDuration;
    }

    public void setTemplateDuration(long templateDuration) {
        mTemplateDuration = templateDuration;
    }

    public long getTemplateSessionId() {
        return mTemplateSessionId;
    }

    public void setTemplateSessionId(long templateSessionId) {
        mTemplateSessionId = templateSessionId;
    }

    public ProgramRecordStatus getProgramRecordStatus() {
        return mProgramRecordStatus;
    }

    public void setProgramRecordStatus(ProgramRecordStatus programRecordStatus) {
        mProgramRecordStatus = programRecordStatus;
    }
}
