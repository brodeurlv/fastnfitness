package com.easyfitness.nourriture;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.easyfitness.AppViMo;
import com.easyfitness.DAO.DAOProfile;
import com.easyfitness.DAO.DAOProfileWeight;
import com.easyfitness.DAO.DAOUtils;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.bodymeasures.DAOBodyPart;
import com.easyfitness.DAO.export.OpenScaleSync;
import com.easyfitness.DAO.macros.DAOFoodRecord;
import com.easyfitness.DatePickerDialogFragment;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.SettingsFragment;
import com.easyfitness.ValuesEditorDialogbox;
import com.easyfitness.bodymeasures.BodyPartDetailsFragment;
import com.easyfitness.enums.Gender;
import com.easyfitness.enums.Unit;
import com.easyfitness.enums.UnitType;
import com.easyfitness.graph.MiniDateGraph;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Keyboard;
import com.easyfitness.utils.UnitConverter;
import com.easyfitness.utils.Value;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class NourritureTotalsFragment extends Fragment {
    private final DAOProfile mDb = null;
    MainActivity mActivity = null;

    private SwipeRefreshLayout pullToRefresh = null;
    private DatePickerDialogFragment mDateFrag = null;

    private PieChart caloriePieChart;
    private PieChart carbsPieChart;
    private PieChart proteinPieChart;
    private PieChart fatsPieChart;
    private TextView editDate;
    private AppViMo appViMo;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static NourritureTotalsFragment newInstance(String name, int id) {
        NourritureTotalsFragment f = new NourritureTotalsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    private void initializePieChartStyle(PieChart chart) {
        chart.setDrawCenterText(false);
        chart.setDrawEntryLabels(false);
        chart.setDrawMarkers(false);
        chart.setDrawHoleEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setClickable(false);
    }

    private void setPieChartProgress(@NonNull PieChart chart, @NonNull float progress) {

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(max(0,progress), "Finished"));
        entries.add(new PieEntry(abs(1-progress), "Remaining"));
        PieDataSet set = new PieDataSet(entries, "");
        set.setDrawValues(false);
        set.setDrawIcons(false);
        set.setColors(new int[] {R.color.progress_chart_filled,R.color.progress_chart_remaining}, getContext());

        PieData data = new PieData(set);

        chart.setData(data);

        chart.invalidate(); // refresh
    }

    private final DatePickerDialog.OnDateSetListener dateSet = (view, year, month, day) -> {
        editDate.setText(DateConverter.dateToLocalDateStr(year, month, day, getContext()));
        Keyboard.hide(getContext(), editDate);
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_macros_totals, container, false);

        // Disable pullToRefresh on default
        pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setEnabled(false);

        caloriePieChart = view.findViewById(R.id.caloriesGraph);
        carbsPieChart = view.findViewById(R.id.carbsGraph);
        proteinPieChart = view.findViewById(R.id.proteinGraph);
        fatsPieChart = view.findViewById(R.id.fatsGraph);

        editDate = view.findViewById(R.id.editDate);
        editDate.setText(DateConverter.currentDate(getContext()));

        editDate.setOnClickListener(v -> {
            if (mDateFrag == null) {
                mDateFrag = DatePickerDialogFragment.newInstance(dateSet);
                mDateFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog");
            } else if (!mDateFrag.isVisible()) {
                mDateFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog");
            }
        });

        initializePieChartStyle(caloriePieChart);
        initializePieChartStyle(carbsPieChart);
        initializePieChartStyle(proteinPieChart);
        initializePieChartStyle(fatsPieChart);

        setPieChartProgress(caloriePieChart, 0.5f);
        setPieChartProgress(carbsPieChart, 0.5f);
        setPieChartProgress(proteinPieChart, 0.5f);
        setPieChartProgress(fatsPieChart, 0.5f);

        appViMo = new ViewModelProvider(requireActivity()).get(AppViMo.class);
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        appViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            // Update the UI, in this case, a TextView.
            refreshData();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshData();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mActivity = (MainActivity) context;
    }

    public String getName() {
        return getArguments().getString("name");
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView == null || getProfile() == null) {
            return;
        }
        // TODO: Get totals for the current day, display

        DAOFoodRecord dailyTotal = new DAOFoodRecord(getContext());



        setPieChartProgress(caloriePieChart, 0.5f);
        setPieChartProgress(carbsPieChart, 0.5f);
        setPieChartProgress(proteinPieChart, 0.5f);
        setPieChartProgress(fatsPieChart, 0.5f);
    }


    private Profile getProfile() {
        return appViMo.getProfile().getValue();
    }

    public Fragment getFragment() {
        return this;
    }
}
