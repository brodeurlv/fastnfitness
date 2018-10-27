package com.easyfitness.fonte;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DateGraphData;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.graph.Graph;
import com.easyfitness.utils.UnitConverter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class FonteGraphFragment extends Fragment {
	private String name;
	private int id;
	//Profile mProfile = null;

	private Spinner functionList = null;
	private Spinner machineList = null;
	private Button lastyearButton = null;
	private Button lastmonthButton = null;
	private Button lastweekButton = null;
	private Button allButton = null;
    private Graph.zoomType currentZoom = Graph.zoomType.ZOOM_ALL;
	
	private LineChart mChart = null;
	private Graph mGraph = null;

	MainActivity mActivity = null;

	private DAOFonte mDb = null;

	private View mFragmentView = null;

	ArrayAdapter<String> mAdapterMachine = null;
	List<String> mMachinesArray = null;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static FonteGraphFragment newInstance(String name, int id) {
    	FonteGraphFragment f = new FonteGraphFragment();

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
		View view = inflater.inflate(R.layout.tab_graph, container, false);
		mFragmentView = view;
        functionList = view.findViewById(R.id.filterGraphFunction);
        machineList = view.findViewById(R.id.filterGraphMachine);
        allButton = view.findViewById(R.id.allbutton);
        lastyearButton = view.findViewById(R.id.lastyearbutton);
        lastmonthButton = view.findViewById(R.id.lastmonthbutton);
        lastweekButton = view.findViewById(R.id.lastweekbutton);
		
		/* Initialisation des evenements */
		machineList.setOnItemSelectedListener(onItemSelectedList);
		functionList.setOnItemSelectedListener(onItemSelectedList);

        allButton.setOnClickListener(onZoomClick);
		lastyearButton.setOnClickListener(onZoomClick);
		lastmonthButton.setOnClickListener(onZoomClick);
		lastweekButton.setOnClickListener(onZoomClick);
		
		/* Initialise le graph */ 
		mChart = view.findViewById(R.id.graphChart);
		mGraph = new Graph(getContext(), mChart, getResources().getText(R.string.weightLabel).toString());

		/* Initialisation de l'historique */
        if (mDb == null) mDb = new DAOFonte(getContext());

		ArrayAdapter<String> adapterFunction = new ArrayAdapter<String>(
				getContext(), android.R.layout.simple_spinner_item,
				mActivity.getResources().getStringArray(R.array.graph_functions));
		adapterFunction.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		functionList.setAdapter(adapterFunction);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getProfil() != null) {
            mMachinesArray = new ArrayList<String>(0); //Data are refreshed on show //mDb.getAllMachinesStrList(getProfil());
            // lMachinesArray = prepend(lMachinesArray, "All");
            mAdapterMachine = new ArrayAdapter<String>(
                    getContext(), android.R.layout.simple_spinner_item,
                    mMachinesArray);
            mAdapterMachine.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            machineList.setAdapter(mAdapterMachine);
            mDb.closeCursor();
        }

        if (this.getUserVisibleHint())
            refreshData();
    }
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mActivity = (MainActivity) activity;
	}

    @Override
    public void onStop() {
        super.onStop();
        // Save Shared Preferences
    }

    public MainActivity getMainActivity() {
        return this.mActivity;
    }

	private OnItemSelectedListener onItemSelectedList = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			DrawGraph();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	};
	
	private OnClickListener onZoomClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
            case R.id.allbutton:
                currentZoom=Graph.zoomType.ZOOM_ALL;
                break;
			case R.id.lastweekbutton:
                currentZoom=Graph.zoomType.ZOOM_WEEK;
				break;
			case R.id.lastmonthbutton:
                currentZoom=Graph.zoomType.ZOOM_MONTH;
                break;
			case R.id.lastyearbutton:
                currentZoom=Graph.zoomType.ZOOM_YEAR;
                break;
			}
            mGraph.setZoom(currentZoom);
		}		
	};

	

	private void DrawGraph() {

		if (getProfil()==null) return;

        String lMachine = null;
        String lFunction = null;
        int lDAOFunction = 0;

        mChart.clear();
		if (machineList.getSelectedItem() == null) {
            return;
        }// Evite les problemes au cas ou il n'y aurait aucune machine d'enregistree
		if (functionList.getSelectedItem() == null) {
            return;
        }

        lMachine = machineList.getSelectedItem().toString();
        lFunction = functionList.getSelectedItem().toString();

        if (lFunction.equals(mActivity.getResources().getString(R.string.maxRep1))) {
            lDAOFunction = DAOFonte.MAX1_FCT;
        } else if (lFunction.equals(mActivity.getResources().getString(R.string.maxRep5d))) {
            lDAOFunction = DAOFonte.MAX5_FCT;
        } else if (lFunction.equals(mActivity.getResources().getString(R.string.sum))) {
            lDAOFunction = DAOFonte.SUM_FCT;
        }

        DAOMachine mDbExercise = new DAOMachine(mActivity);
        Machine m = mDbExercise.getMachine(lMachine);

        if (m.getType() != DAOMachine.TYPE_FONTE) return; // TODO Manage also Cardio records

		// Recupere les enregistrements
        List<DateGraphData> valueList = mDb.getBodyBuildingFunctionRecords(getProfil(), lMachine, lDAOFunction);

		if (valueList.size()<=0) {
		    mChart.clear();
		    return;
        }

		ArrayList<Entry> yVals = new ArrayList<Entry>();

		// Recherche le min et max des dates
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getActivity());
		int defaultUnit = 0;
		try {
			defaultUnit = Integer.valueOf(SP.getString("defaultUnit", "0"));
		}catch (NumberFormatException e) {
			defaultUnit = 0;
		}

		for (int i = 0; i<valueList.size();i++) {
			Entry value = null;
			if (defaultUnit == UnitConverter.UNIT_LBS) {
				value = new Entry((float)valueList.get(i).getX(), UnitConverter.KgtoLbs((float) valueList.get(i).getY()));//-minDate)/86400000));
			} else {
				value = new Entry((float)valueList.get(i).getX(), (float) valueList.get(i).getY());//-minDate)/86400000));
			}
			yVals.add(value);
		}

		Description desc = new Description();
        desc.setText(lMachine + "/" + lFunction);

		mGraph.getLineChart().setDescription(desc);
		mGraph.draw(yVals);
	}

	public String getName() { 
		return getArguments().getString("name");
	}
	
	public int getFragmentId() { 
		return getArguments().getInt("id", 0);
	}

	public DAOFonte getDB() {
		return mDb;
	}
	
	private void refreshData(){
		//View fragmentView = getView();

		if(mFragmentView != null) {
			if (getProfil() != null) {
				//functionList.setOnItemSelectedListener(onItemSelectedList);
                if (mAdapterMachine == null) {
                    mMachinesArray = mDb.getAllMachinesStrList(getProfil());
                    //Data are refreshed on show //mDb.getAllMachinesStrList(getProfil());
                    // lMachinesArray = prepend(lMachinesArray, "All");
                    mAdapterMachine = new ArrayAdapter<String>(
                            getContext(), android.R.layout.simple_spinner_item,
                            mMachinesArray);
                    mAdapterMachine.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    machineList.setAdapter(mAdapterMachine);
                } else {
                    /* Initialisation des machines */
                    if (mMachinesArray == null)
                        mMachinesArray = mDb.getAllMachinesStrList(getProfil());
                    else {
                        mMachinesArray.clear();
                        mMachinesArray.addAll(mDb.getAllMachinesStrList(getProfil()));
                        mAdapterMachine.notifyDataSetChanged();
                        mDb.closeCursor();
                    }
                }

                int position = mAdapterMachine.getPosition(this.getFontesMachine());
                if (position != -1) {
                    if (machineList.getSelectedItemPosition() != position)
                        machineList.setSelection(position);
					DrawGraph();
				} else {
                    mChart.clear();
				}
			}
		}
	}

    private ArrayAdapter<String> getAdapterMachine() {
        ArrayAdapter<String> a;
        mMachinesArray = new ArrayList<String>(0); //Data are refreshed on show //mDb.getAllMachinesStrList(getProfil());
        // lMachinesArray = prepend(lMachinesArray, "All");
        mAdapterMachine = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item,
                mMachinesArray);
        mAdapterMachine.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        machineList.setAdapter(mAdapterMachine);
        return mAdapterMachine;
    }
  
	private Profile getProfil()
	{
		return mActivity.getCurrentProfil();
	}  

	private String getFontesMachine()	{
		return getMainActivity().getCurrentMachine();
	}

  @Override
  public void onHiddenChanged (boolean hidden) {
	  //machineList
	  if (!hidden) refreshData();
  }

    public void saveSharedParams(String toSave, String paramName) {
        SharedPreferences sharedPref = this.mActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(toSave, paramName);
        editor.commit();
    }

    public String getSharedParams(String paramName) {
        SharedPreferences sharedPref = this.mActivity.getPreferences(Context.MODE_PRIVATE);
        String ret = sharedPref.getString(paramName, "");
        return ret;
    }
	
}
