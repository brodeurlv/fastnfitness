package com.easyfitness.nourriture;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.easyfitness.AppViMo;
import com.easyfitness.DAO.DAOProfile;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.macros.DAOFoodRecord;
import com.easyfitness.DAO.macros.FoodRecord;
import com.easyfitness.DatePickerDialogFragment;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Keyboard;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


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
    private TextView totalCalories;
    private TextView totalCarbs;
    private TextView totalProtein;
    private TextView totalFats;
    private Date selectedDate;
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
        chart.setTouchEnabled(false);
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
        selectedDate = DateConverter.dateToDate(year, month, day);
        Keyboard.hide(getContext(), editDate);
        refreshData();
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

        totalCalories = view.findViewById(R.id.caloriesAmount);
        totalCarbs = view.findViewById(R.id.carbsAmount);
        totalProtein = view.findViewById(R.id.proteinAmount);
        totalFats = view.findViewById(R.id.fatsAmount);

        editDate = view.findViewById(R.id.editDate);
        selectedDate = new Date();
        editDate.setText(DateConverter.dateToLocalDateStr(selectedDate, getContext()));

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

        // Get the total macros for the currently selected day
        DAOFoodRecord foodDAO = new DAOFoodRecord(getContext());
        FoodRecord r = foodDAO.getMacroTotalsForDate(selectedDate,getProfile());

        if (r == null) {
            // No data for this day
            setPieChartProgress(caloriePieChart, 0.0f);
            setPieChartProgress(carbsPieChart, 0.0f);
            setPieChartProgress(proteinPieChart, 0.0f);
            setPieChartProgress(fatsPieChart, 0.0f);

            totalCalories.setText("No Data");
            totalCarbs.setText("No Data");
            totalProtein.setText("No Data");
            totalFats.setText("No Data");
        }
        else {
            totalCalories.setText(String.format(Locale.getDefault(), "%.1f", r.getCalories()));
            totalCarbs.setText(String.format(Locale.getDefault(), "%.1f", r.getCarbs()));
            totalProtein.setText(String.format(Locale.getDefault(), "%.1f", r.getProtein()));
            totalFats.setText(String.format(Locale.getDefault(), "%.1f", r.getFats()));

            /// TODO: Calculate progress percentage
            setPieChartProgress(caloriePieChart, 0.5f);
            setPieChartProgress(carbsPieChart, 0.5f);
            setPieChartProgress(proteinPieChart, 0.5f);
            setPieChartProgress(fatsPieChart, 0.5f);
        }
    }


    private Profile getProfile() {
        return appViMo.getProfile().getValue();
    }

    public Fragment getFragment() {
        return this;
    }
}
