package com.easyfitness.bodymeasures;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.MainActivity;
import com.easyfitness.R;

import java.util.ArrayList;

public class BodyPartListFragment extends Fragment {
	private String name;
	private int id;

	Spinner typeList = null;
	Spinner musclesList = null;
	EditText description = null;
	ImageButton renameMachineButton = null;
	ArrayList<BodyPart> dataModels;
	ListView measureList = null;
	private static BodyPartListAdapter adapter;

	private DAOBodyMeasure mDbBodyMeasures = null;
	
	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 */
	public static BodyPartListFragment newInstance(String name, int id) {
		BodyPartListFragment f = new BodyPartListFragment();

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
		View view = inflater.inflate(R.layout.tab_bodytracking, container, false);

		DAOBodyMeasure mdbMeasure = new DAOBodyMeasure(this.getContext());

        measureList = (ListView) view.findViewById(R.id.listBodyMeasures);

		dataModels= new ArrayList<>();

		dataModels.add(new BodyPart(BodyPart.LEFTARM, mdbMeasure.getLastBodyMeasures(BodyPart.LEFTARM, ((MainActivity)getActivity()).getCurrentProfil())));
		dataModels.add(new BodyPart(BodyPart.RIGHTARM, mdbMeasure.getLastBodyMeasures(BodyPart.RIGHTARM, ((MainActivity)getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPart.PECTORAUX, mdbMeasure.getLastBodyMeasures(BodyPart.PECTORAUX, ((MainActivity)getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPart.WAIST, mdbMeasure.getLastBodyMeasures(BodyPart.WAIST, ((MainActivity)getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPart.LEFTTHIGH, mdbMeasure.getLastBodyMeasures(BodyPart.LEFTTHIGH, ((MainActivity)getActivity()).getCurrentProfil())));
		dataModels.add(new BodyPart(BodyPart.RIGHTTHIGH, mdbMeasure.getLastBodyMeasures(BodyPart.RIGHTTHIGH, ((MainActivity)getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPart.LEFTCALVES, mdbMeasure.getLastBodyMeasures(BodyPart.LEFTCALVES, ((MainActivity)getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPart.RIGHTCALVES, mdbMeasure.getLastBodyMeasures(BodyPart.RIGHTCALVES, ((MainActivity)getActivity()).getCurrentProfil())));

		adapter= new BodyPartListAdapter(dataModels,getContext());

		measureList.setAdapter(adapter);
		measureList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				BodyPart dataModel= dataModels.get(position);

				/*Snackbar.make(view, dataModel.getName()+"\n"+dataModel.getType()+" API: "+dataModel.getVersion_number(), Snackbar.LENGTH_LONG)
						.setAction("No action", null).show();*/
			}
		});

		measureList.setOnItemClickListener(onClickListItem);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private static String[] prepend(String[] a, String el) {
		String[] c = new String[a.length + 1];
		c[0] = el;
		System.arraycopy(a, 0, c, 1, a.length);
		return c;
	}

	public String getName() { 
		return getArguments().getString("name");
	}

	public int getFragmentId() { 
		return getArguments().getInt("id", 0);
	}
	
	private OnItemClickListener onClickListItem = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			TextView textView = (TextView) view.findViewById(R.id.LIST_BODYPART_ID);
			int bodyPartID = Integer.parseInt(textView.getText().toString());

			BodyPartDetailsFragment fragment = BodyPartDetailsFragment.newInstance(bodyPartID);
			FragmentTransaction transaction =getThis().getActivity().getSupportFragmentManager().beginTransaction();
			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack so the user can navigate back
			transaction.replace(R.id.fragment_container, fragment);
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
			
			/*// Get Machine Name selected
			TextView textView = (TextView) view.findViewById(R.id.LIST_MACHINE_NAME);
            String machineName = textView.getText().toString(); 
            

			TextView textViewID = (TextView) view.findViewById(R.id.LIST_MACHINE_ID);
            long machineId = Long.valueOf(textViewID.getText().toString()); 
			
			// Create detailled machine Activity			
            Intent intent = new Intent();
            intent.setClass(getActivity(), MachineDetailsActivity.class);
            intent.putExtra("machineName", machineName);
            intent.putExtra("machineId", machineId);
            intent.putExtra("machineProfilId", ((MainActivity)getActivity()).getCurrentProfil().getId());
            
            getActivity().invalidateOptionsMenu();
            
            startActivity(intent);		*/
		}
	};
		public BodyPartListFragment getThis() {
		return this;
	}

	private void refreshData(){

	}

	@Override
	public void onHiddenChanged (boolean hidden) {

	}

	private Profile getProfil()	{
		return ((MainActivity)getActivity()).getCurrentProfil();
	}

}
