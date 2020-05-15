package com.easyfitness.DAO

import android.content.ContentProvider
import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext

/* DataBase Object */
class ExerciseInProgram : ARecord {
    var exerciseName: String
    var serie: Int private set
    var repetition: Int private set
    var poids: Float private set
    var unit: Int private set
    var note: String
    var secRest: Int private set
    var distance = 0
    var duration: Long = 0
    var seconds = 0
    var distanceUnit = 0
    var order: Long = 0

    constructor(secRest: Int, exerciseName: String, pSerie: Int, pRepetition: Int, pPoids: Float,
                pProfile: Profile?, pUnit: Int, pNote: String, pMachineKey: Long, pTime: String?,
                exerciseType: Int, distance: Int, duration: Long, seconds: Int, distanceUnit: Int,
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
        this.mType = exerciseType
        this.distance = distance
        this.duration = duration
        this.seconds = seconds
        this.distanceUnit = distanceUnit
        this.order = order
    }

    constructor(secRest: Int, exerciseName: String, pSerie: Int, pRepetition: Int, pPoids: Float,
                pProfile: Long, pUnit: Int, pNote: String, pMachineKey: Long, pTime: String?, exerciseType: Int, ctx: Context) : super() {
        this.secRest = secRest
        this.exerciseName = exerciseName
        serie = pSerie
        repetition = pRepetition
        poids = pPoids
        unit = pUnit
        note = pNote
        val lDAOProfil = DAOProfil(ctx)
        mProfile = lDAOProfil.getProfil(pProfile)
        mExerciseId = pMachineKey
        mTime = pTime
        this.mType = exerciseType
    }
}
