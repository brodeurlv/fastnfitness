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
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.DAO.Profile
import com.easyfitness.MainActivity
import com.easyfitness.R
import com.easyfitness.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.tab_programs.*

class ProgramsFragment : Fragment(R.layout.tab_programs) {
    private var programsList: ListView = listProgram
//    private lateinit var searchField: AutoCompleteTextView
    private var mTableAdapter: ProgramCursorAdapter? = null
    private var newProgramName: EditText = new_program_name
    val addButton=addExercise

//    private var daoProgram: DAOProgram? = null
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
        val programName = newProgramName!!.text.toString()
        if (programName.isEmpty()) {
            Toast.makeText(context, "Enter not empty program name", Toast.LENGTH_LONG).show()
        } else {
            val lDAOProgram = DAOProgram(context)
            lDAOProgram.addRecord(programName)
            newProgramName!!.setText("")
            mTableAdapter!!.notifyDataSetChanged()
            refreshData()
            Toast.makeText(context, "Added to program list", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        addButton.setOnClickListener(clickAddButton)
        searchField.addTextChangedListener(onTextChangeListener)
        return layoutInflater.inflate(R.layout.tab_programs, container, false)
    }
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        setContentView<ActivityMainBinding>(this.requireActivity(), R.layout.tab_programs)
////        var binding : ActivityMainBinding =
////            DataBindingUtil.setContentView(this.requireActivity(), R.layout.tab_programs)
////    }
//
////    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
////                              savedInstanceState: Bundle?): View? {
////
//////        val view: ViewDataBinding =
////            setContentView(this.requireActivity(), R.layout.tab_programs)
////        // activates onCreateOptionsMenu in this fragment
////        setHasOptionsMenu(true)
////
////        // Inflate the layout for this fragment
//////        val view = inflater.inflate(R.layout.tab_programs, container, false)
//////        newProgramName = view.findViewById(R.id.new_program_name)//view.findViewById(R.id.new_program_name)
////
////        val addButton = view.findViewById<Button>(R.id.addExercise)
//        val addButton=addExercise
//        addButton.setOnClickListener(clickAddButton)
//        searchField.addTextChangedListener(onTextChangeListener)
//
////            view.findViewById(R.id.searchField)
////        programsList = view.findViewById(R.id.listProgram)
////
//////        daoProgram = DAOProgram(view.context)
////        return view
//    }

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
                val daoProgram = DAOProgram(context)
                c = daoProgram.allPrograms
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
