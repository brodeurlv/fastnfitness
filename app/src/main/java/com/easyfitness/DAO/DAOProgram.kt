package com.easyfitness.DAO

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import java.util.*

class DAOProgram(context: Context?) : DAOBase(context) {
    private var mCursor: Cursor? = null
    fun addRecord(programName: String?): Long {
        val value = ContentValues()
        val newId: Long
        if (programExists(programName)) {
            return -1
        }
        value.put(PROGRAM_NAME, programName)
        value.put(PROFIL_KEY, 1)
        val db = open()
        newId = db.insert(TABLE_NAME, null, value)
        close()
        return newId
    }

    private fun programExists(programName: String?): Boolean {
        val lMach = getRecord(programName)
        return lMach != null
    }

    fun getRecord(pName: String?): Program? {
        val db = this.readableDatabase
        mCursor = null
        mCursor = db.query(TABLE_NAME, arrayOf(KEY, PROGRAM_NAME, PROFIL_KEY), "$PROGRAM_NAME=?", arrayOf(pName), null, null, null, null)
        if (mCursor != null) mCursor!!.moveToFirst()
        if (mCursor!!.count == 0) return null
        val value = Program(
            mCursor!!.getString(1),
            mCursor!!.getLong(2))
        value.setId(mCursor!!.getLong(0))
        mCursor!!.close()
        close()
        return value
    }

    fun getRecord(id: Long): Program? {
        val db = this.readableDatabase
        mCursor = null
        mCursor = db.query(TABLE_NAME, arrayOf(KEY, PROGRAM_NAME, PROFIL_KEY), "$KEY=?", arrayOf(id.toString()), null, null, null, null)
        if (mCursor != null) mCursor!!.moveToFirst()
        if (mCursor!!.count == 0) return null
        val value = Program(mCursor!!.getString(1), mCursor!!.getLong(2))
        value.setId(mCursor!!.getLong(0))
        mCursor!!.close()
        close()
        return value
    }

    val allProgramsNames: MutableList<String>?
        get() {
            val programs: MutableList<String> = ArrayList()
            val db = this.readableDatabase
            mCursor = null
            mCursor = db.query(TABLE_NAME, arrayOf(PROGRAM_NAME), null, null, null, null, PROGRAM_NAME)
            if (mCursor != null) mCursor!!.moveToFirst()
            if (mCursor!!.count == 0) return null
            if (mCursor!!.moveToFirst()) {
                do {
                    programs.add(mCursor!!.getString(mCursor!!.getColumnIndex(PROGRAM_NAME)))
                } while (mCursor!!.moveToNext())
            }
            mCursor!!.close()
            close()
            return programs
        }

    val allPrograms: Cursor?
        get() {
            val selectQuery = ("SELECT  * FROM " + TABLE_NAME + " ORDER BY "
                + PROGRAM_NAME + " ASC")
            return getProgramListCursor(selectQuery)
        }

    /**
     * @return List of Machine object ordered by Favorite and Name
     */
    fun getFilteredPrograms(filterString: CharSequence): Cursor {
        // Select All Query
        // like '%"+inputText+"%'";
        val selectQuery = ("SELECT  * FROM " + TABLE_NAME + " WHERE " + PROGRAM_NAME + " LIKE " + "'%" + filterString + "%' " + " ORDER BY "
            + PROGRAM_NAME + " ASC")
        return getProgramListCursor(selectQuery)
    }

    private fun getProgramListCursor(pRequest: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(pRequest, null)
    }

//    fun updateRecord(m: Program): Int {
//        val db = this.writableDatabase
//        val value = ContentValues()
//        value.put(PROGRAM_NAME, m.programName)
//        return db.update(TABLE_NAME, value, "$KEY = ?", arrayOf(m.getId().toString()))
//    }

    fun delete(m: Program?) {
        if (m != null) {
            val db = this.writableDatabase
            db.delete(TABLE_NAME, "$KEY = ?", arrayOf(m.getId().toString()))
            db.close()
        }
    }

    companion object {
        const val TABLE_NAME = "EFProgram"
        const val KEY = "_id"
        const val PROGRAM_NAME = "name"
        const val PROFIL_KEY = "profil_key"
        const val TABLE_CREATE = ("CREATE TABLE " + TABLE_NAME
            + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PROGRAM_NAME
            + " TEXT, " + PROFIL_KEY + " INTEGER);")
    }
}
