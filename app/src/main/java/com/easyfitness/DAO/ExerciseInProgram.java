package com.easyfitness.DAO;

/* DataBase Object */
public class ExerciseInProgram extends ARecord {
    private String exerciseName;
    private int mSerie;
    private int mRepetition;
    private float mPoids;
    private int mUnit;
    private String mNote;
    private int mType;
    private int secRest;
    private int distance;
    private String duration;
    private int seconds;
    private int distanceUnit;
    private long orderInProgram;

//    public ExerciseInProgram(int secRest, String pMachine, int pSerie, int pRepetition, int pPoids,
//                             Profile pProfile, int pUnit, String pNote, long pMachineKey, String pTime,
//                             int type, int distance, String duration, int seconds, int distanceUnit,
//                             long order) {
//    }
    public ExerciseInProgram(int secRest, String exerciseName, int pSerie, int pRepetition, float pPoids,
                             Profile pProfile, int pUnit, String pNote, long pMachineKey, String pTime,
                             int type, int distance, String duration, int seconds, int distanceUnit,
                             long order) {
        super();
        this.secRest=secRest;
        this.exerciseName = exerciseName;
        this.mSerie = pSerie;
        this.mRepetition = pRepetition;
        this.mPoids = pPoids;
        this.mUnit = pUnit;
        this.mNote = pNote;
        this.mProfile = pProfile;
        this.mExerciseId = pMachineKey;
        this.mTime = pTime;
        this.mType = type;
        this.distance = distance;
        this.duration = duration;
        this.seconds=seconds;
        this.distanceUnit=distanceUnit;
        this.orderInProgram=order;
    }

    public ExerciseInProgram(int secRest, String exerciseName, int pSerie, int pRepetition, float pPoids,
                             Profile pProfile, int pUnit, String pNote, long pMachineKey, String pTime) {
        super();
        this.secRest=secRest;
        this.exerciseName = exerciseName;
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

    public long getOrder() {
        return orderInProgram;
    }

    public void setOrder(long order) {
        this.orderInProgram = order;
    }

    public int getDistanceUnit() {
        return distanceUnit;
    }

    public void setDistanceUnit(int distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }
}
