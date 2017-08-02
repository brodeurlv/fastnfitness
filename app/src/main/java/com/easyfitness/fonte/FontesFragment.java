package com.easyfitness.fonte;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.easyfitness.CountdownDialogbox;
import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.Fonte;
import com.easyfitness.DAO.Profil;
import com.easyfitness.DatePickerDialogFragment;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.UnitConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@SuppressLint("ValidFragment")
public class FontesFragment extends Fragment {
	MainActivity mActivity = null;
	Profil mProfil = null;
	EditText dateEdit = null;
	AutoCompleteTextView machineEdit = null;
	ArrayAdapter<String> machineEditAdapter = null;
	EditText serieEdit = null;
	EditText repetitionEdit = null;
	EditText poidsEdit = null;
	LinearLayout detailsLayout = null;
	Button addButton = null;
	ListView recordList = null;
	String[] machineListArray = null;
	ImageButton machineListButton = null;
	Spinner unitSpinner = null;
	ImageButton detailsExpandArrow = null;
	EditText restTimeEdit = null;
	CheckBox restTimeCheck = null;
	DatePickerDialogFragment mDateFrag = null;
	int lTableColor = 1;
	private String name;
	private int id;
	private DAOFonte mDb = null;
	private OnClickListener clickAddButton = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			/* Reagir au clic pour les boutons 1 et 2*/

			// Verifie que les infos sont completes
			if (dateEdit.getText().toString().isEmpty() ||
					machineEdit.getText().toString().isEmpty() ||
					serieEdit.getText().toString().isEmpty() ||
					repetitionEdit.getText().toString().isEmpty() ||
					poidsEdit.getText().toString().isEmpty() )	{
				return;
			}

			Date date;
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
				dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				date = dateFormat.parse(dateEdit.getText().toString());
			} catch (ParseException e) {
				e.printStackTrace();
				date = new Date();
			}

			/* Convertion du poid */
			int tmpPoids=Integer.parseInt(poidsEdit.getText().toString());
			if ( unitSpinner.getSelectedItem().toString().equals(getView().getContext().getString(R.string.LbsUnitLabel)) ) {
				tmpPoids=Math.round(UnitConverter.LbstoKg((float)tmpPoids));
			}

			mDb.addRecord(date,
					machineEdit.getText().toString(),
					Integer.parseInt(serieEdit.getText().toString()),
					Integer.parseInt(repetitionEdit.getText().toString()),
					tmpPoids,
					getProfil(),
					0, // Store always in Kg
					"", //Notes
					DateConverter.currentTime()
					);

			getActivity().findViewById(R.id.drawer_layout).requestFocus();

			lTableColor = (lTableColor+1)%2; // Change la couleur a chaque ajout de donnees

			FillRecordTable(machineEdit.getText().toString());

			/* Reinitialisation des machines */
			// TODO Eviter de recreer a chaque fois l'adapter. On peut utiliser toujours le meme.
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getView().getContext(),
					android.R.layout.simple_dropdown_item_1line, mDb.getAllMachines(getProfil()));
			machineEdit.setAdapter(adapter);
			mDb.closeCursor();

			//Rajoute le moment du dernier ajout dans le bouton Add
			addButton.setText(getView().getContext().getString(R.string.AddLabel)+"\n("+DateConverter.currentTime()+")");

			//saveSharedParams(DateConverter.currentTime(), "LastRecordDate");

			// If setting prefShowRestTime is ON
			//SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity.getBaseContext());
			//boolean bLaunchRest = SP.getBoolean("prefShowRestTime", true);
			boolean bLaunchRest = restTimeCheck.isChecked();
			int restTime = 60;
			try {
				restTime = Integer.valueOf(restTimeEdit.getText().toString());
			} catch (NumberFormatException e) {
				restTime = 60;
				restTimeEdit.setText("60");
			}
			// Launch Countdown
			if (bLaunchRest) {
				CountdownDialogbox cdd = new CountdownDialogbox(mActivity, restTime);
				cdd.show();
			}

		}
	};
	private OnClickListener collapseDetailsClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			detailsLayout.setVisibility(detailsLayout.isShown() ? View.GONE : View.VISIBLE);
			detailsExpandArrow.setImageResource(detailsLayout.isShown() ? R.drawable.arrow_up : R.drawable.arrow_down);
			saveSharedParams();
		}
	};
	private OnClickListener onClickMachineList = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			machineListArray = mDb.getAllMachines(getProfil());
			mDb.closeCursor();

			AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
			builder.setTitle("Select a Machine")
			.setItems(machineListArray, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					machineEdit.setText(machineListArray[which]); // Met a jour le text
					FillRecordTable(machineListArray[which]); // Met a jour le tableau
					getMainActivity().findViewById(R.id.drawer_layout).requestFocus();

					//((ViewGroup)machineEdit.getParent()).requestFocus(); //Permet de reactiver le clavier lors du focus sur l'editText
				}
			});
			//builder.create();
			builder.show();
		}
	};
	private OnClickListener clickDateEdit = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.editDate:
				showDatePickerFragment();
				/*FragmentTransaction ft = getFragmentManager().beginTransaction();
				mDateFrag = new DatePickerDialogFragment() {
					@Override
					public void onDateSet(DatePicker view, int year, int month, int day) {
						dateEdit.setText(DateConverter.dateToString(year, month + 1, day));
					}
				};
				mDateFrag.show(ft, "dialog");	*/
				break;
			case R.id.editMachine:
				//InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				//imm.showSoftInput(machineEdit, InputMethodManager.SHOW_IMPLICIT);
				//machineEdit.setText("");
				//machineEdit.set.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				break;
			}
		}
	};
	private OnFocusChangeListener touchRazEdit = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus == true) {
				switch(v.getId()) {
				case R.id.editDate:
					/*getFragment().mDateFrag = new DatePickerDialogFragment() {
						@Override
						public void onDateSet(DatePicker view, int year, int month, int day) {
							dateEdit.setText(DateConverter.dateToString(year, month + 1, day));
						}
					};*/
					showDatePickerFragment();

					break;
				case R.id.editSerie:
					serieEdit.setText("");
					break;
				case R.id.editRepetition:
					repetitionEdit.setText("");
					break;
				case R.id.editPoids:
					poidsEdit.setText("");
					break;
				case R.id.editMachine:
					////InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					//imm.showSoftInput(machineEdit, InputMethodManager.SHOW_IMPLICIT);
					machineEdit.setText("");
					//machineEdit.set.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
					break;
				}
			}else if (hasFocus == false) {
				switch(v.getId()) {
				case R.id.editMachine:
					FillRecordTable(machineEdit.getText().toString());
					break;
				}
			}
		}
	};

	private OnFocusChangeListener restTimeEditChange = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				saveSharedParams();
			}
		}
	};


	private CompoundButton.OnCheckedChangeListener restTimeCheckChange = new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			saveSharedParams();
		}
	};
	private OnItemLongClickListener itemlongclickDeleteRecord = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> listView, View view,
				int position, long id) {

			// Get the cursor, positioned to the corresponding row in the result set
			//Cursor cursor = (Cursor) listView.getItemAtPosition(position);

			final long selectedID = id;

			String[] profilListArray = new String[3];
			profilListArray[0] = getActivity().getResources().getString(R.string.DeleteLabel);
			profilListArray[1] = getActivity().getResources().getString(R.string.EditLabel);
			profilListArray[2] = getActivity().getResources().getString(R.string.ShareLabel);


			AlertDialog.Builder itemActionbuilder = new AlertDialog.Builder(getView().getContext());
			itemActionbuilder.setTitle("").setItems(profilListArray, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ListView lv = ((AlertDialog)dialog).getListView();

					switch (which) {
					// Delete
						case 0:
							mDb.deleteRecord(selectedID);

						FillRecordTable(machineEdit.getText().toString());

						Toast.makeText(getActivity(), getResources().getText(R.string.removedid) + " " + selectedID, Toast.LENGTH_SHORT).show();

							break;
					// Edit
					case 1:
						Toast.makeText(getActivity(), "Edit soon available", Toast.LENGTH_SHORT).show();//TODO change static string
						break;
					// Share
					case 2:
						//Toast.makeText(getActivity(), "Share soon available", Toast.LENGTH_SHORT).show();
						Fonte fonte = mDb.getRecord(selectedID);
						// Build text
						String text = getView().getContext().getResources().getText(R.string.ShareTextDefault).toString();
						text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamName), fonte.getProfil().getName());
						text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamWeight), String.valueOf(fonte.getPoids()));
						text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamMachine), fonte.getMachine());
						shareRecord(text);
					break;
					default:
					}
				}
			});
			itemActionbuilder.show();

			return true;

			/*
			mDb.deleteRecord(id);

			//listView.removeViewInLayout(view);

			FillRecordTable(machineEdit.getText().toString());

			Toast.makeText(getActivity(), "Removed id" + id, Toast.LENGTH_SHORT).show();

			return true;*/
		}
	};
	private OnItemClickListener onItemClickFilterList = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
								long id) {
			FillRecordTable(machineEdit.getText().toString());
		}
	};

	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 */
	public static FontesFragment newInstance(String name, int id) {
		FontesFragment f = new FontesFragment();

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

		View view = inflater.inflate(R.layout.tab_fontes, container, false);

		dateEdit = (EditText) view.findViewById(R.id.editDate);
		machineEdit = (AutoCompleteTextView) view.findViewById(R.id.editMachine);
		serieEdit = (EditText) view.findViewById(R.id.editSerie);
		repetitionEdit = (EditText) view.findViewById(R.id.editRepetition);
		poidsEdit = (EditText) view.findViewById(R.id.editPoids);
		recordList = (ListView) view.findViewById(R.id.listRecord);
		machineListButton = (ImageButton) view.findViewById(R.id.buttonListMachine);
		addButton = (Button) view.findViewById(R.id.addperff);
		unitSpinner = (Spinner) view.findViewById(R.id.spinnerUnit);
		detailsLayout = (LinearLayout) view.findViewById(R.id.notesLayout);
		detailsExpandArrow = (ImageButton) view.findViewById(R.id.buttonExpandArrow);
		restTimeEdit = (EditText) view.findViewById(R.id.editRestTime);
		restTimeCheck = (CheckBox) view.findViewById(R.id.restTimecheckBox);

		/* Initialisation des boutons */
		addButton.setOnClickListener(clickAddButton);
		machineListButton.setOnClickListener(onClickMachineList);

		dateEdit.setOnClickListener(clickDateEdit);
		dateEdit.setOnFocusChangeListener(touchRazEdit);
		serieEdit.setOnFocusChangeListener(touchRazEdit);
		repetitionEdit.setOnFocusChangeListener(touchRazEdit);
		poidsEdit.setOnFocusChangeListener(touchRazEdit);
		machineEdit.setOnFocusChangeListener(touchRazEdit);
		machineEdit.setOnItemClickListener(onItemClickFilterList);
		recordList.setOnItemLongClickListener(itemlongclickDeleteRecord);
		detailsExpandArrow.setOnClickListener(collapseDetailsClick);
		restTimeEdit.setOnFocusChangeListener(restTimeEditChange);
		restTimeCheck.setOnCheckedChangeListener(restTimeCheckChange); //.setOnFocusChangeListener(restTimeEditChange);

		restoreSharedParams();

		// Initialisation de la base de donnee
		mDb = new DAOFonte(view.getContext());

		// Inflate the layout for this fragment
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		this.mActivity = (MainActivity) this.getActivity();
		refreshData();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	// invoked when the activity may be temporarily destroyed, save the instance state here
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// call superclass to save any view hierarchy
		super.onSaveInstanceState(outState);
	}

	public String getName() {
		return getArguments().getString("name");
	}

	public int getFragmentId() {
		return getArguments().getInt("id", 0);
	}

	public MainActivity getMainActivity() {
		return this.mActivity;
	}

	private void showDatePickerFragment() {
		if (mDateFrag == null) {
			mDateFrag = new DatePickerDialogFragment() {
				@Override
				public void onDateSet(DatePicker view, int year, int month, int day) {
					dateEdit.setText(DateConverter.dateToString(year, month + 1, day));
				}
			};
		}

		FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
		mDateFrag.show(ft, "dialog");
	}

	// Share your performances with friends
	public boolean shareRecord(String text) {
		AlertDialog.Builder newProfilBuilder = new AlertDialog.Builder(getView().getContext());

		newProfilBuilder.setTitle(getView().getContext().getResources().getText(R.string.ShareTitle));
		newProfilBuilder.setMessage(getView().getContext().getResources().getText(R.string.ShareInstruction));

		// Set an EditText view to get user input
		final EditText input = new EditText(getView().getContext());
		input.setText(text);
		newProfilBuilder.setView(input);

		newProfilBuilder.setPositiveButton(getView().getContext().getResources().getText(R.string.ShareText), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();

				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, value);
				sendIntent.setType("text/plain");
				startActivity(sendIntent);
			}
		});

		newProfilBuilder.setNegativeButton(getView().getContext().getResources().getText(R.string.global_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		newProfilBuilder.show();

		return true;
	}

	public DAOFonte getDB(){
		return mDb;
	}

	public FontesFragment getFragment() {
		return this;
	}
	
	private Profil getProfil()
	{
		return mActivity.getCurrentProfil();
	}
	
	public String getMachine()
	{
		//if (machineEdit == null) machineEdit = (AutoCompleteTextView) this.getView().findViewById(R.id.editMachine);
		return machineEdit.getText().toString();
	}
	

	/*  */
	private void FillRecordTable (String pMachines) {

		List<Fonte> records = null;
		Cursor newCursor = null, oldCursor = null;

		// Informe l'activité de la machine courante
		this.getMainActivity().setCurrentMachine(pMachines);

		// Recupere les valeurs
		if (pMachines==null || pMachines.isEmpty()) {
			records = mDb.getAllRecordsByProfil(getProfil(), 10); // TODO Pourrait etre un parametre   	
		} else {
			records = mDb.getAllRecordByMachines(getProfil(), pMachines, 10); // TODO Pourrait etre un parametre   
		}
		newCursor = mDb.GetCursor();

		if(records.isEmpty()) {
			//Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();    
			recordList.setAdapter(null);
		} else {
			// ...
			if ( recordList.getAdapter() == null ) {
				FonteCursorAdapter mTableAdapter = new FonteCursorAdapter (this.getView().getContext(), mDb.GetCursor(), 0);
				mTableAdapter.setFirstColorOdd(lTableColor);
				recordList.setAdapter(mTableAdapter);
			} else {				
				FonteCursorAdapter mTableAdapter = ((FonteCursorAdapter)recordList.getAdapter());
				mTableAdapter.setFirstColorOdd(lTableColor);
				oldCursor = mTableAdapter.swapCursor(mDb.GetCursor());
				if (oldCursor!=null) oldCursor.close();
				//mTableAdapter.notifyDataSetChanged();
			}
		}
	}

	/*public void onDateSet(DatePicker view, int year,
			int month, int day) {
		// Do something with the date chosen by the user
		dateEdit.setText(DateConverter.dateToString(year, month+1, day));
	}*/


	private void refreshData(){
		View fragmentView = getView();
		if(fragmentView != null) {
			if (getProfil() != null) {
			mDb.setProfil(getProfil());
			
			machineListArray = mDb.getAllMachines(getProfil());   
			mDb.closeCursor();
			
			/* Init machines list*/
			machineEditAdapter = new ArrayAdapter<String>(this.getView().getContext(),
					android.R.layout.simple_dropdown_item_1line, machineListArray);
			machineEdit.setAdapter(machineEditAdapter);	
			
			// Si on a change de profil
			//if (mProfil != getProfil()) {
				mProfil = getProfil();
				
				/* Initialisation serie */ 
				Fonte lLastRecord = mDb.getLastRecord(getProfil());
				if (lLastRecord != null ) {
					machineEdit.setText(lLastRecord.getMachine());
					serieEdit.setText(String.valueOf(lLastRecord.getSerie()));
					repetitionEdit.setText(String.valueOf(lLastRecord.getRepetition()));
					poidsEdit.setText(String.valueOf(lLastRecord.getPoids()));
				} else {
					// valeur par defaut
					machineEdit.setText(""); //@TODO recuperer une valeur par defaut. 
					serieEdit.setText("1");
					repetitionEdit.setText("10");
					poidsEdit.setText("50");
				}
			//}
			
			// Set Initial text
			dateEdit.setText(DateConverter.currentDate());
	
			// Set Table
			FillRecordTable(machineEdit.getText().toString());
			}
		}
	}

	public void saveSharedParams() {
		SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("restTime", restTimeEdit.getText().toString());
		editor.putBoolean("restCheck", restTimeCheck.isChecked());
		editor.putBoolean("showDetails", this.detailsLayout.isShown());
		editor.commit();
	}

	public void restoreSharedParams() {
		SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		restTimeEdit.setText(sharedPref.getString("restTime", ""));
		restTimeCheck.setChecked(sharedPref.getBoolean("restCheck", true));

		if (sharedPref.getBoolean("showDetails", false)) {
			detailsLayout.setVisibility(View.VISIBLE);
		} else {
			detailsLayout.setVisibility(View.GONE);
		}
		detailsExpandArrow.setImageResource(sharedPref.getBoolean("showDetails", false) ? R.drawable.arrow_up : R.drawable.arrow_down);
	}

	@Override
	public void onHiddenChanged (boolean hidden) {
		if (!hidden) refreshData();
	}


}

