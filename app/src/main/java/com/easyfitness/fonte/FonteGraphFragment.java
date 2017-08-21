package com.easyfitness.fonte;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.easyfitness.DAO.Profile;
import com.easyfitness.DateGraphData;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.graph.CustomMarkerView;
import com.easyfitness.graph.Graph;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.IMarker;
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
		
		functionList = (Spinner) view.findViewById(R.id.filterGraphFunction);
		machineList = (Spinner) view.findViewById(R.id.filterGraphMachine);
		allButton = (Button) view.findViewById(R.id.allbutton);
		lastyearButton = (Button) view.findViewById(R.id.lastyearbutton);
		lastmonthButton = (Button) view.findViewById(R.id.lastmonthbutton);
		lastweekButton = (Button) view.findViewById(R.id.lastweekbutton);
		
		/* Initialisation des evenements */
		machineList.setOnItemSelectedListener(onItemSelectedList);
		functionList.setOnItemSelectedListener(onItemSelectedList);

        allButton.setOnClickListener(onZoomClick);
		lastyearButton.setOnClickListener(onZoomClick);
		lastmonthButton.setOnClickListener(onZoomClick);
		lastweekButton.setOnClickListener(onZoomClick);
		
		/* Initialise le graph */ 
		mChart = (LineChart) view.findViewById(R.id.graphChart);
		//mChart.setDescription(""); @TODO: fix this
		if ( mGraph == null ) mGraph = new Graph(mChart, getResources().getText(R.string.weightLabel).toString());

		//Define Marker
		IMarker marker = new CustomMarkerView(getActivity(), R.layout.graph_markerview);
		mGraph.getLineChart().setMarker(marker);

		/* Initialisation de l'historique */
		if (mDb ==null) mDb = new DAOFonte(view.getContext());
		
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
        if (this.getUserVisibleHint() == true)
            refreshData();
    }
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		this.mActivity = (MainActivity) activity;
	}

    @Override
    public void onStop()
    {
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

		String pMachine = null;
		String pFunction = null;
		int pDAOFunction = 0;

		if (machineList.getSelectedItem() == null) { mChart.clear(); return; }// Evite les problemes au cas ou il n'y aurait aucune machine d'enregistree
		if (functionList.getSelectedItem() == null) { mChart.clear(); return; }
		
		pMachine = machineList.getSelectedItem().toString();
		pFunction = functionList.getSelectedItem().toString();
		
		if (pFunction.equals(mActivity.getResources().getString(R.string.maxRep1))) {
			pDAOFunction = DAOFonte.MAX1_FCT;
		} else if (pFunction.equals(mActivity.getResources().getString(R.string.maxRep5))) {
			pDAOFunction = DAOFonte.MAX5_FCT;
		} else if (pFunction.equals(mActivity.getResources().getString(R.string.sum))) {
			pDAOFunction = DAOFonte.SUM_FCT;
		}

		// Recupere les enregistrements
		List<DateGraphData> valueList = mDb.getFunctionRecords(getProfil(), pMachine, pDAOFunction);

		//ArrayList<String> xVals = new ArrayList<String>();
		ArrayList<Entry> yVals = new ArrayList<Entry>();

		// Recherche le min et max des dates
		long maxDate = -1;
		long minDate = -1;

		/*for (int i = 0; i<valueList.size();i++) {
			long tmpDate = (long)(valueList.get(i).getX());
			if (maxDate == -1)  maxDate = tmpDate;
			if (minDate == -1)  minDate = tmpDate;

			if (tmpDate > maxDate) maxDate = tmpDate;
			if (tmpDate < minDate) minDate = tmpDate;
		}*/

		// Reformat l'X axis. Transforme le date de
		/*for (long i = minDate; i<=maxDate;i=i+86400000) {
			SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yy");
			xVals.add(dt1.format(i));
		}*/

		for (int i = 0; i<valueList.size();i++) {
			Entry value = new Entry((float) (valueList.get(i).getX()), (float) valueList.get(i).getY());//-minDate)/86400000));
			yVals.add(value);		
		}

		Description desc = new Description();
		desc.setText(pMachine + "/" + pFunction);

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
		View fragmentView = getView();
		if(fragmentView != null) {
			if (getProfil() != null) {
				/* Initialisation des machines */
				String[] lMachinesArray = mDb.getAllMachines(getProfil());
				
				ArrayAdapter<String> adapterFunction = new ArrayAdapter<String>(
						getView().getContext(), android.R.layout.simple_spinner_item,
						mActivity.getResources().getStringArray(R.array.graph_functions));
				adapterFunction.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				machineList.setAdapter(adapterFunction);
				
				machineList.setOnItemSelectedListener(null);
				// lMachinesArray = prepend(lMachinesArray, "All");
				ArrayAdapter<String> adapterMachine = new ArrayAdapter<String>(
						getView().getContext(), android.R.layout.simple_spinner_item,
						lMachinesArray);
				adapterMachine.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				machineList.setAdapter(adapterMachine);
				mDb.closeCursor();
				machineList.setOnItemSelectedListener(onItemSelectedList);
				
				if ( adapterMachine.getPosition(this.getFontesMachine()) != -1 ) {
					machineList.setSelection(adapterMachine.getPosition(this.getFontesMachine()));
					// Le setSelection lance automatiquement le DrawGraph dans le listener.
				} else {				
					DrawGraph();
				}
			}
		}
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
