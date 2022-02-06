package com.easyfitness.DAO.record;

import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.RecordType;
import com.easyfitness.enums.WeightUnit;

import java.util.Date;

/* DataBase Object */
public class Record {
    private long mId;

    private Date mDate;
    private String mExercise;
    private long mExerciseId;
    private long mProfileId;

    private int mSets;
    private int mReps;
    private float mWeight;
    private WeightUnit mWeightUnit;

    private int mSecond;

    private float mDistance;
    private DistanceUnit mDistanceUnit;
    private long mDuration;

    private String mNote;

    private ExerciseType mExerciseType;
    private RecordType mRecordType;

    private int mTemplateRestTime;

    private long mProgramId; // Id of the Program Template
    private long mTemplateSessionId; // Id of the Workout Session
    private long mTemplateRecordId; // Id of the Template Record of the Program

    private int mTemplateOrder;
    private ProgramRecordStatus mProgramRecordStatus;

    private int mTemplateSets;
    private int mTemplateReps;
    private float mTemplateWeight;
    private WeightUnit mTemplateWeightUnit;

    private int mTemplateSecond;

    private float mTemplateDistance;
    private DistanceUnit mTemplateDistanceUnit;
    private long mTemplateDuration;

    /**
     * Record from Free Workout
     */
    public Record(Date date, String exercise, long exerciseId, long profileId, int sets, int reps, float weight, WeightUnit weightUnit, int second, float distance, DistanceUnit distanceUnit, long duration, String note, ExerciseType exerciseType) {
        this(date, exercise, exerciseId, profileId, sets, reps, weight, weightUnit, second, distance, distanceUnit, duration, note, exerciseType, -1, -1, -1, 0, 0, ProgramRecordStatus.NONE, RecordType.FREE_RECORD, 0, 0, 0, WeightUnit.KG, 0, 0, DistanceUnit.KM, 0);
    }

    /**
     * Record from Program
     */
    public Record(Date date, String exercise, long exerciseId, long profileId,
                  int sets,
                  int reps,
                  float weight,
                  WeightUnit weightUnit,
                  int second,
                  float distance,
                  DistanceUnit distanceUnit,
                  long duration,
                  String note, ExerciseType exerciseType, long programId, long templateRecordId, long templateSessionId, int restTime, int templateOrder, ProgramRecordStatus programRecordStatus, RecordType recordType,
                  int templateSets,
                  int templateReps,
                  float templateWeight,
                  WeightUnit templateWeightUnit,
                  int templateSecond,
                  float templateDistance,
                  DistanceUnit templateDistanceUnit,
                  long templateDuration) {
        mDate = date;
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
        mTemplateRestTime = restTime;
        mProgramId = programId;
        mTemplateRecordId = templateRecordId;
        mTemplateSessionId = templateSessionId;
        mTemplateOrder = templateOrder;
        mProgramRecordStatus = programRecordStatus;

        mTemplateSets = templateSets;
        mTemplateReps = templateReps;
        mTemplateWeight = templateWeight;
        mTemplateWeightUnit = templateWeightUnit;

        mTemplateSecond = templateSecond;

        mTemplateDistance = templateDistance;
        mTemplateDistanceUnit = templateDistanceUnit;
        mTemplateDuration = templateDuration;
    }

    /**
     * Template for program
     */
    public Record(Date date, String exercise, long exerciseId, long profileId, int sets, int reps, float weight, WeightUnit weightUnit, int second, float distance, DistanceUnit distanceUnit, long duration, String note, ExerciseType exerciseType, long programId, int restTime, int templateOrder) {
        this(date, exercise, exerciseId, profileId, sets, reps, weight, weightUnit, second, distance, distanceUnit, duration, note, exerciseType, programId, -1, -1, restTime, templateOrder, ProgramRecordStatus.NONE, RecordType.PROGRAM_TEMPLATE, 0, 0, 0, WeightUnit.KG, 0, 0, DistanceUnit.KM, 0);
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

    public int getSets() { return mSets; }
    public void setSets(int sets) {
        mSets = sets;
    }

    public int getReps() {
        return mReps;
    }
    public void setReps(int reps) {
        mReps = reps;
    }

    public float getWeightInKg() {
        return mWeight;
    }
    public void setWeightInKg(float weight) {
        mWeight = weight;
    }

    public WeightUnit getWeightUnit() {
        return mWeightUnit;
    }
    public void setWeightUnit(WeightUnit weightUnit) {
        mWeightUnit = weightUnit;
    }

    public int getSeconds() {
        return mSecond;
    }
    public void setSeconds(int second) {
        mSecond = second;
    }

    public float getDistanceInKm() {
        return mDistance;
    }
    public void setDistanceInKm(float distance) {
        mDistance = distance;
    }

    public DistanceUnit getDistanceUnit() {
        return mDistanceUnit;
    }
    public void setDistanceUnit(DistanceUnit distanceUnit) {
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

    public long getProgramId() { return mProgramId;   }

    public void setProgramId(long programId) {
        mProgramId = programId;
    }

    public int getTemplateOrder() {
        return mTemplateOrder;
    }

    public void setTemplateOrder(int templateOrder) {
        mTemplateOrder = templateOrder;
    }

    public int getTemplateRestTime() {
        return mTemplateRestTime;
    }

    public void setRestTime(int restTime) {
        mTemplateRestTime = restTime;
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

    public long getTemplateRecordId() {
        return mTemplateRecordId;
    }

    public void setTemplateRecordId(long id) {
        mTemplateRecordId = id;
    }

    public int getTemplateSets() { return mTemplateSets; }
    public void setTemplateSets(int sets) {
        mTemplateSets = sets;
    }

    public int getTemplateReps() {
        return mTemplateReps;
    }
    public void setTemplateReps(int reps) {
        mTemplateReps = reps;
    }

    public float getTemplateWeight() {
        return mTemplateWeight;
    }
    public void setTemplateWeight(float weight) {
        mTemplateWeight = weight;
    }

    public WeightUnit getTemplateWeightUnit() {
        return mTemplateWeightUnit;
    }
    public void setTemplateWeightUnit(WeightUnit weightUnit) {
        mTemplateWeightUnit = weightUnit;
    }

    public int getTemplateSeconds() {
        return mTemplateSecond;
    }
    public void setTemplateSeconds(int second) {
        mTemplateSecond = second;
    }

    public float getTemplateDistance() {
        return mTemplateDistance;
    }
    public void setTemplateDistance(float distance) {
        mTemplateDistance = distance;
    }

    public DistanceUnit getTemplateDistanceUnit() {
        return mTemplateDistanceUnit;
    }
    public void setTemplateDistanceUnit(DistanceUnit distanceUnit) {
        mTemplateDistanceUnit = distanceUnit;
    }

    public long getTemplateDuration() {
        return mTemplateDuration;
    }
    public void setTemplateDuration(long duration) {
        mTemplateDuration = duration;
    }
}
