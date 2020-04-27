package com.easyfitness.programs

import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.easyfitness.DAO.DAOProfil
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.DAO.DAORecord
import com.easyfitness.DAO.Program
import com.easyfitness.MainActivity
import com.easyfitness.R
import com.easyfitness.fonte.FonteHistoryFragment
import com.easyfitness.machines.MachineDetailsFragment
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems

class ProgramDetailsPager : Fragment() {
    private var machineIdArg: Long = 0
    private var machineProfilIdArg: Long = 0
    private var pagerAdapter: FragmentPagerItemAdapter? = null
    private lateinit var machineSave: ImageButton
    private var program: Program? = null
    private var toBeSaved = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.exercise_pager, container, false)

        // Locate the viewpager in activity_main.xml
        val mViewPager: ViewPager = view.findViewById(R.id.pager)
        if (mViewPager.adapter == null) {
            val args = this.arguments
            machineIdArg = args!!.getLong("programID")
            machineProfilIdArg = args.getLong("programProfile")
            pagerAdapter = FragmentPagerItemAdapter(
                childFragmentManager, FragmentPagerItems.with(this.context)
                .add("Exercise", MachineDetailsFragment::class.java, args)
                .add("History", FonteHistoryFragment::class.java, args)
                .create())
            mViewPager.adapter = pagerAdapter
            val viewPagerTab: SmartTabLayout = view.findViewById(R.id.viewpagertab)
            viewPagerTab.setViewPager(mViewPager)
            viewPagerTab.setOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {
                    val frag1 = pagerAdapter!!.getPage(position)
                    frag1?.onHiddenChanged(false) // Refresh data
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
        val mDbProgram = DAOProgram(context)
        (activity as MainActivity?)!!.activityToolbar.visibility = View.GONE
        val top_toolbar: Toolbar = view.findViewById(R.id.actionToolbarMachine)
        top_toolbar.setNavigationIcon(R.drawable.ic_back)
        //        top_toolbar.setNavigationOnClickListener(onClickToolbarItem);
        val machineDelete = view.findViewById<ImageButton>(R.id.action_machine_delete)
        machineSave = view.findViewById(R.id.action_machine_save)
        program = mDbProgram.getRecord(machineIdArg)
        machineSave.setVisibility(View.GONE) // Hide Save button by default
        //        machineDelete.setOnClickListener(onClickToolbarItem);
        // Inflate the layout for this fragment
        return view
    }

    fun requestForSave() {
        toBeSaved = true // setting state
        machineSave!!.visibility = View.VISIBLE
    }

    private fun deleteRecordsAssociatedToProgram() {
        val mDbRecord = DAORecord(context)
        val mDbProfil = DAOProfil(context)
        val lProfile = mDbProfil.getProfil(machineProfilIdArg)
        val listRecords = mDbRecord.getAllRecordByMachinesArray(lProfile, program!!.programName)
        for (record in listRecords) {
            mDbRecord.deleteRecord(record.id)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()

        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.machine_details_menu, menu)
        val item = menu.findItem(R.id.action_machine_save)
        item.isVisible = toBeSaved
        super.onCreateOptionsMenu(menu, inflater)
    }

    private val viewPagerAdapter: FragmentPagerItemAdapter?
        get() = (requireView().findViewById<View>(R.id.pager) as ViewPager).adapter as FragmentPagerItemAdapter?

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            if (viewPagerAdapter != null) {
                var frag1: Fragment?
                for (i in 0..2) {
                    frag1 = viewPagerAdapter!!.getPage(i)
                    frag1?.onHiddenChanged(false) // Refresh data
                }
            }
        }
    }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(machineId: Long, machineProfile: Long): ProgramDetailsPager {
            val f = ProgramDetailsPager()

            // Supply index input as an argument.
            val args = Bundle()
            args.putLong("programID", machineId)
            args.putLong("programProfile", machineProfile)
            f.arguments = args
            return f
        }
    }
}
