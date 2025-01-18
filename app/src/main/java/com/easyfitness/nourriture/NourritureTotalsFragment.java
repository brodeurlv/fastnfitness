package com.easyfitness.nourriture;

import android.app.Activity;
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
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.bodymeasures.DAOBodyPart;
import com.easyfitness.DAO.export.OpenScaleSync;
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

    private PieChart caloriePieChart;
    private PieChart carbsPieChart;
    private PieChart proteinPieChart;
    private PieChart fatsPieChart;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_macros_totals, container, false);

        // Disable pullToRefresh on default
        pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setEnabled(false);


        /* Views Initialisation */
//        weightEdit = view.findViewById(R.id.weightInput);
//        fatEdit = view.findViewById(R.id.fatInput);
//        musclesEdit = view.findViewById(R.id.musclesInput);
//        waterEdit = view.findViewById(R.id.waterInput);
//        sizeEdit = view.findViewById(R.id.sizeInput);

        caloriePieChart = view.findViewById(R.id.caloriesGraph);
        carbsPieChart = view.findViewById(R.id.carbsGraph);
        proteinPieChart = view.findViewById(R.id.proteinGraph);
        fatsPieChart = view.findViewById(R.id.fatsGraph);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(30.0f, "White"));
        entries.add(new PieEntry(70.0f, "Blue"));
        PieDataSet set = new PieDataSet(entries, "Total Calories");
        PieData data = new PieData(set);
        set.setDrawValues(false);
        set.setDrawIcons(false);
        caloriePieChart.setData(data);
        caloriePieChart.setDrawCenterText(false);
        caloriePieChart.setDrawEntryLabels(false);
        caloriePieChart.setDrawMarkers(false);
        caloriePieChart.invalidate(); // refresh

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
    }


    private Profile getProfile() {
        return appViMo.getProfile().getValue();
    }

    public Fragment getFragment() {
        return this;
    }
}
