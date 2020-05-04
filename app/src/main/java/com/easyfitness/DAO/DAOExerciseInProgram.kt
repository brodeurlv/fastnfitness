package com.easyfitness.DAO

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.widget.Toast
import timber.log.Timber
import java.util.*

class DAOExerciseInProgram(var mContext: Context) : DAOBase(mContext) {
    //    private String allFieldsExceptProgramId = REST_SECONDS + "," + EXERCISE + "," + SERIE + ","
    //        + REPETITION + "," + WEIGHT + "," + WEIGHT + "," + PROFIL_KEY + ","
    //        + UNIT + "," + NOTES + "," + MACHINE_KEY + "," + TIME + "," + DISTANCE + "," +
    //        DURATION + "," + TYPE + "," + SECONDS + "," + DISTANCE_UNIT+ ","+ORDER_EXECUTION;
    var mProfile: Profile? = null
    var cursor: Cursor? = null

    fun setProfile(pProfile: Profile?) {
        mProfile = pProfile
    }

    val count: Int
        get() {
            val countQuery = "SELECT $KEY FROM $TABLE_NAME"
            open()
            val db = this.readableDatabase
            val cursor = db.rawQuery(countQuery, null)
            val value = cursor.count
            cursor.close()
            close()
            return value
        }

    /**
     * @param pMachine Machine name
     * @return id of the added record, -1 if error
     */
    fun addRecord(order: Long, programId: Long, restSeconds: Int, pMachine: String, pType: Int, pSerie: Int, pRepetition: Int, pPoids: Float, pProfile: Profile, pUnit: Int, pNote: String?,
                  pTime: String?, pDistance: Float, pDuration: Long, pSeconds: Int, distance_unit: Int): Long {
        val value = ContentValues()
        var newId: Long = -1
        val machineKey: Long = -1
        val daoExerciseInProgram = DAOExerciseInProgram(mContext)
        if (daoExerciseInProgram.exerciseExists(pMachine)) {
            return newId
        }
        value.put(PROGRAM_ID, programId)
        value.put(REST_SECONDS, restSeconds)
        value.put(EXERCISE, pMachine)
        value.put(SERIE, pSerie)
        value.put(REPETITION, pRepetition)
        value.put(WEIGHT, pPoids)
        value.put(PROFIL_KEY, pProfile.id)
        value.put(UNIT, pUnit)
        value.put(NOTES, pNote)
        value.put(MACHINE_KEY, machineKey)
        value.put(TIME, pTime)
        value.put(DISTANCE, pDistance)
        value.put(DURATION, pDuration)
        value.put(TYPE, pType)
        value.put(SECONDS, pSeconds)
        value.put(DISTANCE_UNIT, distance_unit)
        value.put(ORDER_EXECUTION, order)
        val db = open()
        newId = db.insert(TABLE_NAME, null, value)
        close()
        return newId
    }

    private fun exerciseExists(name: String): Boolean {
        val lMach = getRecord(name)
        return lMach != null
    }

    private fun getRecord(pName: String): ExerciseInProgram? {
        val db = this.readableDatabase
        cursor = null
        cursor = db.query(TABLE_NAME, arrayOf(EXERCISE), "$EXERCISE=?", arrayOf(pName), null, null, null, null)
        if (cursor != null) cursor!!.moveToFirst()
        if (0 == cursor!!.count) { close()
            cursor!!.close()
            return null}
        val lDAOProfil = DAOProfil(mContext)
        val lProfile = lDAOProfil.getProfil(cursor!!.getLong(cursor!!.getColumnIndex(DAOFonte.PROFIL_KEY)))
        val value = ExerciseInProgram(
            cursor!!.getInt(cursor!!.getColumnIndex(REST_SECONDS)),
            cursor!!.getString(cursor!!.getColumnIndex(EXERCISE)),
            cursor!!.getInt(cursor!!.getColumnIndex(SERIE)),
            cursor!!.getInt(cursor!!.getColumnIndex(REPETITION)),
            cursor!!.getFloat(cursor!!.getColumnIndex(WEIGHT)),
            lProfile,
            cursor!!.getInt(cursor!!.getColumnIndex(UNIT)),
            cursor!!.getString(cursor!!.getColumnIndex(NOTES)),
            cursor!!.getInt(cursor!!.getColumnIndex(MACHINE_KEY)).toLong(),
            cursor!!.getString(cursor!!.getColumnIndex(TIME)),
            cursor!!.getInt(cursor!!.getColumnIndex(TYPE))
        )
        value.setId(cursor!!.getLong(0))

        close()
        cursor!!.close()
        return value
    }

    fun deleteRecord(id: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$KEY = ?", arrayOf(id.toString()))
        db.close()
    }

    // Getting All Records
    private fun getRecordsListCursor(pRequest: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(pRequest, null)
    }

    fun getAllExerciseInProgramToRecord(programId: Long): Cursor? {
        val selectQuery = ("SELECT * FROM " + TABLE_NAME
            + " WHERE " + PROGRAM_ID + "=" + programId
            + " AND " + KEY + " IN (SELECT DISTINCT " + KEY + " FROM " + TABLE_NAME + " WHERE " + PROGRAM_ID + "=" + programId + " ORDER BY " + ORDER_EXECUTION + " ASC)"
            + " ORDER BY " + KEY + " ASC")
        return getRecordsListCursor(selectQuery)
    }

    fun getAllExerciseInProgramAsList(programId: Long): ArrayList<ARecord> {
        val selectQuery = ("SELECT * FROM " + TABLE_NAME + " WHERE " + PROGRAM_ID + " = " + programId
            + " ORDER BY " + ORDER_EXECUTION + " ASC;")
        return getExerciseListToList(selectQuery)
    }

    fun getAllExerciseInProgram(programId: Long): ArrayList<ExerciseInProgram> {
        val selectQuery = ("SELECT * FROM " + TABLE_NAME + " WHERE " + PROGRAM_ID + " = " + programId
            + " ORDER BY " + ORDER_EXECUTION + " ASC;")
        return getExerciseList(selectQuery)
    }

    private fun getExerciseList(pRequest: String): ArrayList<ExerciseInProgram> {
        val valueList = ArrayList<ExerciseInProgram>()
        val db = this.readableDatabase
        cursor = null
        try {
            cursor = db.rawQuery(pRequest, null)
        } catch (ex: Exception) {
            Timber.e("Err in getExList $ex")
            Toast.makeText(mContext, "Ex$ex", Toast.LENGTH_LONG).show()
        }
        // looping through all rows and adding to list
        if (cursor!!.moveToFirst()) {
            do {
                val value = ExerciseInProgram( //int secRest, String pMachine, int pSerie, int pRepetition, float pPoids,
                    //                             Profile pProfile, int pUnit, String pNote, long pMachineKey, String pTime,
                    //                             int type, int distance, String duration, int seconds, int distanceUnit,
                    //                             long order
                    cursor!!.getInt(cursor!!.getColumnIndex(REST_SECONDS)),
                    cursor!!.getString(cursor!!.getColumnIndex(EXERCISE)),
                    cursor!!.getInt(cursor!!.getColumnIndex(SERIE)),
                    cursor!!.getInt(cursor!!.getColumnIndex(REPETITION)),
                    cursor!!.getInt(cursor!!.getColumnIndex(WEIGHT)).toFloat(),
                    mProfile,
                    cursor!!.getInt(cursor!!.getColumnIndex(UNIT)),
                    cursor!!.getString(cursor!!.getColumnIndex(NOTES)),
                    cursor!!.getInt(cursor!!.getColumnIndex(MACHINE_KEY)).toLong(),
                    cursor!!.getString(cursor!!.getColumnIndex(TIME)),
                    cursor!!.getInt(cursor!!.getColumnIndex(TYPE)),
                    cursor!!.getInt(cursor!!.getColumnIndex(DISTANCE)),
                    cursor!!.getLong(cursor!!.getColumnIndex(DURATION)),
                    cursor!!.getInt(cursor!!.getColumnIndex(SECONDS)),
                    cursor!!.getInt(cursor!!.getColumnIndex(DISTANCE_UNIT)),
                    cursor!!.getLong(cursor!!.getColumnIndex(ORDER_EXECUTION))
                )
                value.setId(cursor!!.getLong(cursor!!.getColumnIndex(KEY)))
                valueList.add(value)
            } while (cursor!!.moveToNext())
        }
        cursor!!.close()
        close()
        return valueList
    }

    private fun getExerciseListToList(pRequest: String): ArrayList<ARecord> {
        val valueList = ArrayList<ARecord>()
        val db = this.readableDatabase
        cursor = null
        try {
            cursor = db.rawQuery(pRequest, null)
        } catch (ex: Exception) {
            Timber.e("Err in getExToList $ex")
            Toast.makeText(mContext, "Ex$ex", Toast.LENGTH_LONG).show()
        }
        // looping through all rows and adding to list
        if (cursor!!.moveToFirst()) {
            do {
                val value = ExerciseInProgram(
                    cursor!!.getInt(cursor!!.getColumnIndex(REST_SECONDS)),
                    cursor!!.getString(cursor!!.getColumnIndex(EXERCISE)),
                    cursor!!.getInt(cursor!!.getColumnIndex(SERIE)),
                    cursor!!.getInt(cursor!!.getColumnIndex(REPETITION)),
                    cursor!!.getInt(cursor!!.getColumnIndex(WEIGHT)).toFloat(),
                    mProfile,
                    cursor!!.getInt(cursor!!.getColumnIndex(UNIT)),
                    cursor!!.getString(cursor!!.getColumnIndex(NOTES)),
                    cursor!!.getInt(cursor!!.getColumnIndex(MACHINE_KEY)).toLong(),
                    cursor!!.getString(cursor!!.getColumnIndex(TIME)),
                    cursor!!.getInt(cursor!!.getColumnIndex(TYPE))
                    )
                value.setId(cursor!!.getLong(cursor!!.getColumnIndex(KEY)))
                valueList.add(value)
            } while (cursor!!.moveToNext())
        }
        close()
        cursor!!.close()
        return valueList
    }

    fun updateString(exerciseInProgram: ExerciseInProgram , field: String, newValue: String):Int {
            val db = this.writableDatabase
            val value = ContentValues()
            value.put(field, newValue)
            return db.update(TABLE_NAME, value, "$KEY = ?", arrayOf(exerciseInProgram.id.toString()))
    }

    companion object {
        // Contacts table name
        const val TABLE_NAME = "EFExerciseInProgram"
        const val KEY = "_id"
        const val DATE = "date"
        private const val TIME = "time"
        const val EXERCISE = "machine"
        const val PROFIL_KEY = "profil_id"
        private const val MACHINE_KEY = "machine_id"
        const val NOTES = "notes"
        const val TYPE = "type"

        // Specific to Strength
        const val SERIE = "serie"
        const val REPETITION = "repetition"
        const val WEIGHT = "poids"
        const val UNIT = "unit" // 0:kg 1:lbs

        // Specific to Cardio
        const val DISTANCE = "distance"
        const val DURATION = "duration"
        const val DISTANCE_UNIT = "distance_unit" // 0:km 1:mi

        // Specific to STATIC
        const val SECONDS = "seconds"

        //rest between exercises
        private const val REST_SECONDS = "rest_seconds"
        private const val PROGRAM_ID = "program_id"
        const val ORDER_EXECUTION = "order_in_program"
        const val TABLE_CREATE = ("CREATE TABLE " + TABLE_NAME
            + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + EXERCISE + " TEXT, "
            + REST_SECONDS + " INTEGER, " + SERIE + " INTEGER, "
            + REPETITION + " INTEGER, " + WEIGHT + " REAL, " + PROFIL_KEY
            + " INTEGER, " + UNIT + " INTEGER, " + NOTES + " TEXT, " + MACHINE_KEY
            + " INTEGER," + TIME + " TEXT," + DISTANCE + " REAL, " + DURATION + " TEXT, "
            + TYPE + " INTEGER, " + SECONDS + " INTEGER, " + DISTANCE_UNIT + " INTEGER, "
            + PROGRAM_ID + " INTEGER, " + ORDER_EXECUTION + " INTEGER);")
    }

}
