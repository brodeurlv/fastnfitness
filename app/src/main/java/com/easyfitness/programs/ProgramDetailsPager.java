package com.easyfitness.programs;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.DAOProgram;
import com.easyfitness.DAO.DAORecord;
import com.easyfitness.DAO.IRecord;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.Program;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.fonte.FonteHistoryFragment;
import com.easyfitness.machines.MachineDetailsFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import java.util.List;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

public class ProgramDetailsPager extends Fragment {
    private long machineIdArg = 0;
    private long machineProfilIdArg = 0;
    private FragmentPagerItemAdapter pagerAdapter = null;
    private ImageButton machineSave = null;
    private Program program = null;
    private boolean toBeSaved = false;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ProgramDetailsPager newInstance(long machineId, long machineProfile) {
        ProgramDetailsPager f = new ProgramDetailsPager();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putLong("programID", machineId);
        args.putLong("programProfile", machineProfile);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.exercise_pager, container, false);

        // Locate the viewpager in activity_main.xml
        ViewPager mViewPager = view.findViewById(R.id.pager);

        if (mViewPager.getAdapter() == null) {

            Bundle args = this.getArguments();
            machineIdArg = args.getLong("programID");
            machineProfilIdArg = args.getLong("programProfile");

            pagerAdapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
                .add("Exercise", MachineDetailsFragment.class, args)
                .add("History", FonteHistoryFragment.class, args)
                .create());

            mViewPager.setAdapter(pagerAdapter);

            SmartTabLayout viewPagerTab = view.findViewById(R.id.viewpagertab);
            viewPagerTab.setViewPager(mViewPager);

            viewPagerTab.setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    Fragment frag1 = pagerAdapter.getPage(position);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false); // Refresh data
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        DAOProgram mDbProgram = new DAOProgram(getContext());

        ((MainActivity) getActivity()).getActivityToolbar().setVisibility(View.GONE);
        Toolbar top_toolbar = view.findViewById(R.id.actionToolbarMachine);
        top_toolbar.setNavigationIcon(R.drawable.ic_back);
//        top_toolbar.setNavigationOnClickListener(onClickToolbarItem);

        ImageButton machineDelete = view.findViewById(R.id.action_machine_delete);
        machineSave = view.findViewById(R.id.action_machine_save);
        program = mDbProgram.getRecord(machineIdArg);
        machineSave.setVisibility(View.GONE); // Hide Save button by default
//        machineDelete.setOnClickListener(onClickToolbarItem);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void requestForSave() {
        toBeSaved = true; // setting state
        machineSave.setVisibility(View.VISIBLE);
    }

    private void deleteRecordsAssociatedToProgram() {
        DAORecord mDbRecord = new DAORecord(getContext());
        DAOProfil mDbProfil = new DAOProfil(getContext());

        Profile lProfile = mDbProfil.getProfil(this.machineProfilIdArg);

        List<IRecord> listRecords = mDbRecord.getAllRecordByMachinesArray(lProfile, program.getProgramName());
        for (IRecord record : listRecords) {
            mDbRecord.deleteRecord(record.getId());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.machine_details_menu, menu);

        MenuItem item = menu.findItem(R.id.action_machine_save);
        item.setVisible(toBeSaved);

        super.onCreateOptionsMenu(menu, inflater);
    }

    public FragmentPagerItemAdapter getViewPagerAdapter() {
        return (FragmentPagerItemAdapter) ((ViewPager) (getView().findViewById(R.id.pager))).getAdapter();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            if (getViewPagerAdapter() != null) {
                Fragment frag1;
                for (int i = 0; i < 3; i++) {
                    frag1 = getViewPagerAdapter().getPage(i);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false); // Refresh data
                }
            }
        }
    }
}
