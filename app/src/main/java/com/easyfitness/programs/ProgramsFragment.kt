package com.easyfitness.programs

import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.DAO.Profile
import com.easyfitness.MainActivity
import com.easyfitness.R

class ProgramsFragment : Fragment() {
    private var programsList: ListView? = null
    private lateinit var searchField: AutoCompleteTextView
    private var mTableAdapter: ProgramCursorAdapter? = null
    private var programNewName: EditText? = null
    private var daoProgram: DAOProgram? = null
    private val onTextChangeListener: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            if (charSequence.isEmpty()) {
                mTableAdapter!!.notifyDataSetChanged()
                if(programsList!=null && programsList!!.adapter!=null) {
                    mTableAdapter = programsList!!.adapter as ProgramCursorAdapter
                    refreshData()
                }
            } else {
                if (mTableAdapter != null) {
                    mTableAdapter!!.filter.filter(charSequence)
                    mTableAdapter!!.notifyDataSetChanged()
                }
            }
        }

        override fun afterTextChanged(editable: Editable) {}
    }

    private val clickAddButton = View.OnClickListener { _: View? ->
        val programName = programNewName!!.text.toString()
        if (programName.isEmpty()) {
            Toast.makeText(context, "Enter not empty program name", Toast.LENGTH_LONG).show()
        } else {
            val lDAOProgram = DAOProgram(context)
            lDAOProgram.addRecord(programName)
            programNewName!!.setText("")
            mTableAdapter!!.notifyDataSetChanged()
            refreshData()
            Toast.makeText(context, "Added to program list", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // activates onCreateOptionsMenu in this fragment
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.tab_programs, container, false)
        programNewName = view.findViewById(R.id.new_program_name)
        val addButton = view.findViewById<Button>(R.id.addExercise)
        addButton.setOnClickListener(clickAddButton)
        searchField = view.findViewById(R.id.searchField)
        searchField.addTextChangedListener(onTextChangeListener)
        programsList = view.findViewById(R.id.listProgram)

        daoProgram = DAOProgram(view.context)
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items

        //for add Programs menu
        val addId = 555
        if (item.itemId == addId) {
            clickAddButton.onClick(view)
        }
        return super.onOptionsItemSelected(item)
    }

    val name: String?
        get() = requireArguments().getString("name")

    val `this`: ProgramsFragment
        get() = this

    private fun refreshData() {
        val c: Cursor?
        val oldCursor: Cursor?
        val fragmentView = view
        if (fragmentView != null) {
            if (profil != null) {
                c = daoProgram!!.allPrograms
                if (c == null || c.count <= 0) {
                    //Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();
                    programsList!!.adapter = null
                } else {
                    if (programsList!!.adapter == null) {
                        mTableAdapter = ProgramCursorAdapter(requireContext(), c, 0, daoProgram)
                        programsList!!.adapter = mTableAdapter
                    } else {
                        mTableAdapter = programsList!!.adapter as ProgramCursorAdapter
                        oldCursor = mTableAdapter!!.swapCursor(c)
                        oldCursor?.close()
                    }
                    mTableAdapter!!.filterQueryProvider = FilterQueryProvider { constraint: CharSequence -> daoProgram!!.getFilteredPrograms(constraint) }
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) refreshData()
    }

    private val profil: Profile?
        get() = (requireActivity() as MainActivity).currentProfile

    companion object {
        fun newInstance(name: String?, id: Long?): ProgramsFragment {
            val f = ProgramsFragment()

            // Supply index input as an argument.
            val args = Bundle()
            args.putString("name", name)
            args.putLong("profilId", id!!)
            f.arguments = args
            return f
        }
    }
}
