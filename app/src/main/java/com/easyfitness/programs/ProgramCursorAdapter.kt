package com.easyfitness.programs

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.Filterable
import android.widget.TextView
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.R

class ProgramCursorAdapter(context: Context, c: Cursor?, flags: Int, pDbMachine: DAOProgram?) : CursorAdapter(context, c, flags), Filterable {
    var mDbMachine: DAOProgram? = null
    private val mInflater: LayoutInflater
    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val t0 = view.findViewById<TextView>(R.id.LIST_Program_ID)
        t0.text = cursor.getString(cursor.getColumnIndex(DAOProgram.KEY))
        val t1 = view.findViewById<TextView>(R.id.LIST_Program_name)
        t1.text = cursor.getString(cursor.getColumnIndex(DAOProgram.PROGRAM_NAME))
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return mInflater.inflate(R.layout.program_list_row, parent, false)
    }

    init {
        mDbMachine = pDbMachine
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}
