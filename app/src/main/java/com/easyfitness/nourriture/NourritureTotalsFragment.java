package com.easyfitness.nourriture;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.easyfitness.AppViMo;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.bodymeasures.DAOBodyPart;
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

    private FoodRecord selectedDateTotals;
    private SharedPreferences sp;
    private DAOBodyMeasure bodyMeasureDAO = null;

    private DAOFoodRecord foodDAO = null;
    private DAOBodyPart bodyPartDAO = null;
    private BodyPart weightBodyPart = null;
    private BodyPart heightBodyPart = null;
    MainActivity mActivity = null;

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
    private TextView goalsInstructions;

    private ImageView caloriesEditButton;
    private ImageView carbsEditButton;
    private ImageView proteinEditButton;
    private ImageView fatsEditButton;

    private EditText caloriesGoalEdit;
    private EditText carbsGoalEdit;
    private EditText proteinGoalEdit;
    private EditText fatsGoalEdit;

    private Date selectedDate;
    private AppViMo appViMo;
    private float caloriesGoal;
    private float carbsGoal;
    private float proteinGoal;
    private float fatsGoal;

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

    private interface GoalGetter {
        public float getGoalValue();
    }
    private interface GoalSetter {
        public void setGoalValue(float f);
    }

    private void setupGoalEditListeners(Context context, ImageView editButton, EditText goalEdit, String title, GoalGetter getGoal, GoalSetter setGoal) {

        Dialog d = new Dialog(context);
        d.setTitle(title);
        d.setContentView(goalEdit);
        d.setCancelable(true);
        d.setOnShowListener(dialog -> {
            goalEdit.setText(String.format(Locale.getDefault(), "%.1f", getGoal.getGoalValue()));
        });
        d.setOnDismissListener(dialog -> {

            String s = goalEdit.getText().toString();
            if(!s.isEmpty()) {
                try {
                    // User typed in a valid value, parse it and refresh the UI
                    float value = Float.parseFloat(s);
                    setGoal.setGoalValue(value);
                    refreshView();
                } catch (NumberFormatException ignored) {
                }
            }
        });
        editButton.setOnClickListener(v -> {
            d.show();
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_macros_totals, container, false);
        Context c = view.getContext();

        caloriePieChart = view.findViewById(R.id.caloriesGraph);
        carbsPieChart = view.findViewById(R.id.carbsGraph);
        proteinPieChart = view.findViewById(R.id.proteinGraph);
        fatsPieChart = view.findViewById(R.id.fatsGraph);

        totalCalories = view.findViewById(R.id.caloriesAmount);
        totalCarbs = view.findViewById(R.id.carbsAmount);
        totalProtein = view.findViewById(R.id.proteinAmount);
        totalFats = view.findViewById(R.id.fatsAmount);

        caloriesEditButton = view.findViewById(R.id.caloriesEditButton);
        carbsEditButton = view.findViewById(R.id.carbsEditButton);
        proteinEditButton = view.findViewById(R.id.proteinEditButton);
        fatsEditButton = view.findViewById(R.id.fatsEditButton);

        goalsInstructions = view.findViewById(R.id.goals_instructions_message);

        caloriesGoalEdit = new EditText(c);
        carbsGoalEdit = new EditText(c);
        proteinGoalEdit = new EditText(c);
        fatsGoalEdit = new EditText(c);

        sp = PreferenceManager.getDefaultSharedPreferences(c);

        editDate = view.findViewById(R.id.editDate);
        selectedDate = new Date();
        editDate.setText(DateConverter.dateToLocalDateStr(selectedDate, c));

        editDate.setOnClickListener(v -> {
            if (mDateFrag == null) {
                mDateFrag = DatePickerDialogFragment.newInstance(dateSet);
                mDateFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog");
            } else if (!mDateFrag.isVisible()) {
                mDateFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog");
            }
        });

        setupGoalEditListeners(c,
            caloriesEditButton,
            caloriesGoalEdit,
            "Daily Calories Goal",
            () -> caloriesGoal,
            v -> {
                caloriesGoal = v;
                sp.edit().putFloat("daily_caloric_intake_goal", v).apply();
            }
        );
        setupGoalEditListeners(c,
                carbsEditButton,
                carbsGoalEdit,
                "Daily Carbs Goal",
                () -> carbsGoal,
                v -> {
                    carbsGoal = v;
                    sp.edit().putFloat("daily_carbs_intake_goal", v).apply();
                }
        );
        setupGoalEditListeners(c,
                proteinEditButton,
                proteinGoalEdit,
                "Daily Protein Goal",
                () -> proteinGoal,
                v -> {
                    proteinGoal = v;
                    sp.edit().putFloat("daily_protein_intake_goal", v).apply();
                }
        );
        setupGoalEditListeners(c,
                fatsEditButton,
                fatsGoalEdit,
                "Daily Fats Goal",
                () -> fatsGoal,
                v -> {
                    fatsGoal = v;
                    sp.edit().putFloat("daily_fats_intake_goal", v).apply();
                }
        );

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

    private void populateProgressCardView(PieChart chart, TextView text, float goal, float consumed) {

        if (goal > 0.0f) {
            float caloriesProgress = consumed / goal;
            text.setText(String.format(Locale.getDefault(), "%.1f / %.1f", consumed, goal));
            setPieChartProgress(chart, caloriesProgress);
        }
        else {
            text.setText(String.format(Locale.getDefault(), "%.1f / ???", consumed));
            setPieChartProgress(chart, 0.0f);
        }
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView == null || getProfile() == null) {
            return;
        }

        Context c = fragmentView.getContext();

        if (bodyMeasureDAO == null) {
            bodyMeasureDAO = new DAOBodyMeasure(c);
        }

        if (bodyPartDAO == null) {
            bodyPartDAO = new DAOBodyPart(c);
        }

        if (foodDAO == null) {
            foodDAO = new DAOFoodRecord(c);
        }

        // Get the total macros for the currently selected day
        selectedDateTotals = foodDAO.getMacroTotalsForDate(selectedDate,getProfile());

        caloriesGoal = sp.getFloat("daily_caloric_intake_goal", 0.0f);

        carbsGoal = sp.getFloat("daily_carbs_intake_goal", 0.0f);
        proteinGoal = sp.getFloat("daily_protein_intake_goal", 0.0f);
        fatsGoal = sp.getFloat("daily_fats_intake_goal", 0.0f);

        refreshView();
    }

    private void refreshView() {
        if (selectedDateTotals == null) {

            setPieChartProgress(caloriePieChart, 0.0f);
            setPieChartProgress(carbsPieChart, 0.0f);
            setPieChartProgress(proteinPieChart, 0.0f);
            setPieChartProgress(fatsPieChart, 0.0f);

            totalCalories.setText("No Data");
            totalCarbs.setText("No Data");
            totalProtein.setText("No Data");
            totalFats.setText("No Data");
            return;
        }

        String instructionsMessage = "";
        int missingCounter = 0;
        if (caloriesGoal <= 0.0f) {
            instructionsMessage += "No daily consumption goal set for Calories! ";
            missingCounter++;
        }
        if (proteinGoal <= 0.0f) {
            instructionsMessage += "No daily consumption goal set for Protein! ";
            missingCounter++;
        }
        if (carbsGoal <= 0.0f) {
            instructionsMessage += "No daily consumption goal set for Carbs! ";
            missingCounter++;
        }
        if (fatsGoal <= 0.0f) {
            instructionsMessage += "No daily consumption goal set for Fats! ";
            missingCounter++;
        }

        if (missingCounter == 4) {
            instructionsMessage = "No daily intake goals are set! ";
        }

        if (!instructionsMessage.isEmpty()) {
            instructionsMessage += "Tap the edit icon next to each goal to set a daily consumption goal.";
            goalsInstructions.setText(instructionsMessage);
            goalsInstructions.setVisibility(View.VISIBLE);
        }
        else {
            goalsInstructions.setVisibility(View.GONE);
        }

        populateProgressCardView(caloriePieChart,totalCalories, caloriesGoal, selectedDateTotals.getCalories());
        populateProgressCardView(carbsPieChart,totalCarbs, carbsGoal, selectedDateTotals.getCarbs());
        populateProgressCardView(proteinPieChart,totalProtein, proteinGoal, selectedDateTotals.getProtein());
        populateProgressCardView(fatsPieChart,totalFats, fatsGoal, selectedDateTotals.getFats());

    }


    private Profile getProfile() {
        return appViMo.getProfile().getValue();
    }

    public Fragment getFragment() {
        return this;
    }
}
