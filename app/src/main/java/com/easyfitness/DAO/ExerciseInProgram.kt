package com.easyfitness.DAO

/* DataBase Object */
class ExerciseInProgram : ARecord {
    var exerciseName: String
    var serie: Int private set
    var repetition: Int private set
    var poids: Float private set
    var unit: Int private set
    private var note: String
    var exType: Int = 0
    var secRest: Int private set
    var distance = 0
    var duration: String? = null
    var seconds = 0
    var distanceUnit = 0
    var order: Long = 0

    //    public ExerciseInProgram(int secRest, String pMachine, int pSerie, int pRepetition, int pPoids,
    //                             Profile pProfile, int pUnit, String pNote, long pMachineKey, String pTime,
    //                             int type, int distance, String duration, int seconds, int distanceUnit,
    //                             long order) {
    //    }
    constructor(secRest: Int, exerciseName: String, pSerie: Int, pRepetition: Int, pPoids: Float,
                pProfile: Profile?, pUnit: Int, pNote: String, pMachineKey: Long, pTime: String?,
                type: Int, distance: Int, duration: String?, seconds: Int, distanceUnit: Int,
                order: Long) : super() {
        this.secRest = secRest
        this.exerciseName = exerciseName
        serie = pSerie
        repetition = pRepetition
        poids = pPoids
        unit = pUnit
        note = pNote
        mProfile = pProfile
        mExerciseId = pMachineKey
        mTime = pTime
        exType = type
        this.distance = distance
        this.duration = duration
        this.seconds = seconds
        this.distanceUnit = distanceUnit
        this.order = order
    }

    constructor(secRest: Int, exerciseName: String, pSerie: Int, pRepetition: Int, pPoids: Float,
                pProfile: Profile?, pUnit: Int, pNote: String, pMachineKey: Long, pTime: String?) : super() {
        this.secRest = secRest
        this.exerciseName = exerciseName
        serie = pSerie
        repetition = pRepetition
        poids = pPoids
        unit = pUnit
        note = pNote
        mProfile = pProfile
        mExerciseId = pMachineKey
        mTime = pTime
        mType = DAOMachine.TYPE_FONTE
    }
}
