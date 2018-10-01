package com.easyfitness.fonte;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.easyfitness.BtnClickListener;
import com.easyfitness.CountdownDialogbox;
import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.Fonte;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.Weight;
import com.easyfitness.DatePickerDialogFragment;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.machines.MachineArrayFullAdapter;
import com.easyfitness.machines.MachineCursorAdapter;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.ExpandedListView;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.UnitConverter;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FontesFragment extends Fragment {
	MainActivity mActivity = null;
	Profile mProfile = null;
	EditText dateEdit = null;
	AutoCompleteTextView machineEdit = null;
    MachineArrayFullAdapter machineEditAdapter = null;
	EditText serieEdit = null;
	EditText repetitionEdit = null;
	EditText poidsEdit = null;
	LinearLayout detailsLayout = null;
	Button addButton = null;
	ExpandedListView recordList = null;
	String[] machineListArray = null;
	ImageButton machineListButton = null;
	Spinner unitSpinner = null;
	ImageButton detailsExpandArrow = null;
	EditText restTimeEdit = null;
	CheckBox restTimeCheck = null;
	DatePickerDialogFragment mDateFrag = null;
	CircularImageView machineImage = null;
    TextView minText = null;
    TextView maxText = null;
	int lTableColor = 1;
	private DAOFonte mDb = null;
	private DAOMachine mDbMachine = null;
	AlertDialog machineListDialog;

	private OnClickListener clickAddButton = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// Verifie que les infos sont completes
			if (
					machineEdit.getText().toString().isEmpty() ||
					serieEdit.getText().toString().isEmpty() ||
					repetitionEdit.getText().toString().isEmpty() ||
					poidsEdit.getText().toString().isEmpty() )	{
                //Toast.makeText(getActivity(), R.string.missinginfo, Toast.LENGTH_SHORT).show();
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
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
			float tmpPoids = Float.parseFloat(poidsEdit.getText().toString().replaceAll(",", "."));
			int unitPoids= UnitConverter.UNIT_KG; // Kg
			if ( unitSpinner.getSelectedItem().toString().equals(getView().getContext().getString(R.string.LbsUnitLabel)) ) {
                tmpPoids = UnitConverter.LbstoKg(tmpPoids); // Always convert to KG
				unitPoids = UnitConverter.UNIT_LBS; // LBS
			}

			mDb.addRecord(date,
					machineEdit.getText().toString(),
					Integer.parseInt(serieEdit.getText().toString()),
					Integer.parseInt(repetitionEdit.getText().toString()),
					tmpPoids, // Always save in KG
					getProfil(),
					unitPoids, // Store Unit for future display
					"", //Notes
					DateConverter.currentTime()
					);

			getActivity().findViewById(R.id.drawer_layout).requestFocus();
			hideKeyboard(v);

			lTableColor = (lTableColor+1)%2; // Change la couleur a chaque ajout de donnees

			FillRecordTable(machineEdit.getText().toString());

			/* Reinitialisation des machines */
			// TODO Eviter de recreer a chaque fois l'adapter. On peut utiliser toujours le meme.
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getView().getContext(),
					android.R.layout.simple_dropdown_item_1line, mDb.getAllMachines(getProfil()));
			machineEdit.setAdapter(adapter);

			//Rajoute le moment du dernier ajout dans le bouton Add
			addButton.setText(getView().getContext().getString(R.string.AddLabel)+"\n("+DateConverter.currentTime()+")");

			//--Launch Rest Dialog
			boolean bLaunchRest = restTimeCheck.isChecked();
			int restTime = 60;
			try {
				restTime = Integer.valueOf(restTimeEdit.getText().toString());
			} catch (NumberFormatException e) {
				restTime = 60;
				restTimeEdit.setText("60");
			}

			float iTotalWeightSession = mDb.getTotalWeightSession(date);
			float iTotalWeight = mDb.getTotalWeightMachine(date, machineEdit.getText().toString() );
			int iNbSeries = mDb.getNbSeries(date, machineEdit.getText().toString() );

			// Launch Countdown
			if (bLaunchRest && DateConverter.dateToLocalDateStr(date, getContext()).equals(DateConverter.dateToLocalDateStr(new Date(), getContext())) ) { // Only launch Countdown if date is today.
				CountdownDialogbox cdd = new CountdownDialogbox(mActivity, restTime);
				cdd.setNbSeries(iNbSeries);
				cdd.setTotalWeightMachine(iTotalWeight);
				cdd.setTotalWeightSession(iTotalWeightSession);
				cdd.show();
			}

			mDb.closeCursor();
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
	private OnClickListener onClickMachineListWithIcons = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
            Cursor c;
            Cursor oldCursor;

			ListView machineList = new ListView(v.getContext());

			// Version avec table Machine
            c = mDbMachine.getAllMachines();

            if (c == null || c.getCount() == 0) {
                //Toast.makeText(getActivity(), R.string.createExerciseFirst, Toast.LENGTH_SHORT).show();
                KToast.warningToast(getActivity(), getResources().getText(R.string.createExerciseFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                machineList.setAdapter(null);
			} else {
				if ( machineList.getAdapter() == null ) {
                    MachineCursorAdapter mTableAdapter = new MachineCursorAdapter(v.getContext(), c, 0);
                    //MachineArrayFullAdapter lAdapter = new MachineArrayFullAdapter(v.getContext(),records);
					machineList.setAdapter(mTableAdapter);
				} else {
					MachineCursorAdapter mTableAdapter = ((MachineCursorAdapter)machineList.getAdapter());
                    oldCursor = mTableAdapter.swapCursor(c);
					if (oldCursor!=null) oldCursor.close();
				}

				machineList.setOnItemClickListener(new OnItemClickListener(){
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView textView = view.findViewById(R.id.LIST_MACHINE_ID);
						long machineID = Long.parseLong(textView.getText().toString());

						DAOMachine lMachineDb = new DAOMachine(getContext());
						Machine lMachine = lMachineDb.getMachine(machineID);
						machineImage.setImageResource(R.drawable.ic_machine); // Default image
                        ImageUtil imgUtil = new ImageUtil();
                        imgUtil.setThumb(machineImage, imgUtil.getThumbPath(lMachine.getPicture())); // Overwrite image is there is one

                        machineEdit.setText(lMachine.getName()); // Met a jour le text
						updateLastRecord(machineID);
						FillRecordTable(lMachine.getName()); // Met a jour le tableau
						getMainActivity().findViewById(R.id.drawer_layout).requestFocus();

						hideKeyboard(getMainActivity().findViewById(R.id.drawer_layout));

						if(machineListDialog.isShowing())
						{
							machineListDialog.dismiss();
						}
					}
				});

				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
				builder.setTitle(R.string.selectMachineDialogLabel);
				builder.setView(machineList);
				machineListDialog = builder.create();
				machineListDialog.show();
			}

		}
	};
	private void updateLastRecord(long machineId) {
		Fonte lLastRecord = mDb.getLastMachineRecord(machineId);
		if (lLastRecord != null) {
			serieEdit.setText(String.valueOf(lLastRecord.getSerie()));
			repetitionEdit.setText(String.valueOf(lLastRecord.getRepetition()));
			unitSpinner.setSelection(lLastRecord.getUnit());
			DecimalFormat numberFormat = new DecimalFormat("#.##");
			if (lLastRecord.getUnit() == UnitConverter.UNIT_LBS)
				poidsEdit.setText(numberFormat.format(UnitConverter.KgtoLbs(lLastRecord.getPoids())));
			else
				poidsEdit.setText(numberFormat.format(lLastRecord.getPoids()));
		}
	}

	private OnClickListener clickDateEdit = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.editDate:
				showDatePickerFragment();
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
            if (hasFocus) {
				switch(v.getId()) {
				case R.id.editDate:
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
					machineEdit.setText("");
					machineImage.setImageResource(R.drawable.ic_machine);
					break;
				}
            } else if (!hasFocus) {
				switch(v.getId()) {
				case R.id.editMachine:
					Machine lMachine = mDbMachine.getMachine(machineEdit.getText().toString());
                    if (lMachine != null) {
                        ImageUtil imgUtil = new ImageUtil();
                        imgUtil.setThumb(machineImage, imgUtil.getThumbPath(lMachine.getPicture())); // Overwrite image is there is one
                    }
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

			showRecordListMenu(id);

			return true;
		}
	};

	private BtnClickListener itemClickDeleteRecord = new BtnClickListener() {
		@Override
		public void onBtnClick(long id) {
			showDeleteDialog(id);
		}
	};

	private OnItemClickListener onItemClickFilterList = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
								long id) {
			FillRecordTable(machineEdit.getText().toString());
		}
	};

	private void showRecordListMenu(final long id) {
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
						showDeleteDialog(id);
						break;
					// Edit
					case 1:
						Toast.makeText(getActivity(), R.string.edit_soon_available, Toast.LENGTH_SHORT).show();
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
	}

	private void showDeleteDialog(final long idToDelete){

        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.DeleteRecordDialog))
                .setContentText(getResources().getText(R.string.areyousure).toString())
                .setCancelText(getResources().getText(R.string.global_no).toString())
                .setConfirmText(getResources().getText(R.string.global_yes).toString())
                .showCancelButton(true)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        mDb.deleteRecord(idToDelete);

                        FillRecordTable(machineEdit.getText().toString());

                        //Toast.makeText(getContext(), getResources().getText(R.string.removedid) + " " + idToDelete, Toast.LENGTH_SHORT).show();
                        // Info
                        KToast.infoToast(getActivity(), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();

    }

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

		dateEdit = view.findViewById(R.id.editDate);
		machineEdit = view.findViewById(R.id.editMachine);
		serieEdit = view.findViewById(R.id.editSerie);
		repetitionEdit = view.findViewById(R.id.editRepetition);
		poidsEdit = view.findViewById(R.id.editPoids);
		recordList = view.findViewById(R.id.listRecord);
		machineListButton = view.findViewById(R.id.buttonListMachine);
		addButton = view.findViewById(R.id.addperff);
		unitSpinner = view.findViewById(R.id.spinnerUnit);
		detailsLayout = view.findViewById(R.id.notesLayout);
		detailsExpandArrow = view.findViewById(R.id.buttonExpandArrow);
		restTimeEdit = view.findViewById(R.id.editRestTime);
		restTimeCheck = view.findViewById(R.id.restTimecheckBox);
		machineImage = view.findViewById(R.id.imageMachine);
        minText = view.findViewById(R.id.minText);
        maxText = view.findViewById(R.id.maxText);

		/* Initialisation des boutons */
		addButton.setOnClickListener(clickAddButton);
		machineListButton.setOnClickListener(onClickMachineListWithIcons); //onClickMachineList

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

		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getActivity());
		int defaultUnit = 0;
		try {
			defaultUnit = Integer.valueOf(SP.getString("defaultUnit", "0"));
		}catch (NumberFormatException e) {
			defaultUnit = 0;
		}
		unitSpinner.setSelection(defaultUnit);

		// Initialisation de la base de donnee
		mDb = new DAOFonte(getContext());
		mDbMachine = new DAOMachine (getContext());
		dateEdit.setText(DateConverter.currentDate());

		// Inflate the layout for this fragment
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		this.mActivity = (MainActivity) this.getActivity();
		refreshData();
	}

    /*@Override
    public void onResume() {
        super.onResume();
        this.mActivity = (MainActivity) this.getActivity();
        refreshData();
    }*/

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
			mDateFrag = DatePickerDialogFragment.newInstance(dateSet);
		}

		FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
		mDateFrag.show(ft, "dialog");
	}

	private DatePickerDialog.OnDateSetListener dateSet = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			dateEdit.setText(DateConverter.dateToString(year, month + 1, day));
            hideKeyboard(dateEdit);
		}
	};

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
	
	private Profile getProfil()
	{
		return mActivity.getCurrentProfil();
	}

    public String getMachine() {
		//if (machineEdit == null) machineEdit = (AutoCompleteTextView) this.getView().findViewById(R.id.editMachine);
		return machineEdit.getText().toString();
	}

	/*  */
    private void FillRecordTable(String pMachine) {

		List<Fonte> records = null;
        Cursor c = null;
		Cursor oldCursor = null;

		// Informe l'activité de la machine courante
        Machine m = mDbMachine.getMachine(pMachine);
        this.getMainActivity().setCurrentMachine(pMachine);

		// Recupere les valeurs
        if (pMachine == null || pMachine.isEmpty()) {
            c = mDb.getAllRecordsByProfil(getProfil(), 10);
		} else {
            c = mDb.getAllRecordByMachines(getProfil(), pMachine, 10);
		}

        if (c == null || c.getCount() == 0) {
			//Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();    
			recordList.setAdapter(null);
		} else {
			// ...
			if ( recordList.getAdapter() == null ) {
                FonteCursorAdapter mTableAdapter = new FonteCursorAdapter(getContext(), c, 0, itemClickDeleteRecord);
				mTableAdapter.setFirstColorOdd(lTableColor);
				recordList.setAdapter(mTableAdapter);
			} else {				
				FonteCursorAdapter mTableAdapter = ((FonteCursorAdapter)recordList.getAdapter());
				mTableAdapter.setFirstColorOdd(lTableColor);
                oldCursor = mTableAdapter.swapCursor(c);
				if (oldCursor!=null) oldCursor.close();
			}
		}

        String unitStr = "";
        float weight = 0;
        if (getProfil() != null && m != null) {
			updateLastRecord(m.getId());
            Weight minValue = mDb.getMin(getProfil(), m);
            getView().findViewById(R.id.minmaxLayout).setVisibility(View.VISIBLE);
            if (minValue != null) {
                if (minValue.getStoredUnit() == UnitConverter.UNIT_LBS) {
                    weight = UnitConverter.KgtoLbs(minValue.getStoredWeight());
                    unitStr = getContext().getString(R.string.LbsUnitLabel);
                } else {
                    weight = minValue.getStoredWeight();
                    unitStr = getContext().getString(R.string.KgUnitLabel);
                }
                DecimalFormat numberFormat = new DecimalFormat("#.##");
                minText.setText(numberFormat.format(weight) + " " + unitStr);

                Weight maxValue = mDb.getMax(getProfil(), m);
                if (maxValue.getStoredUnit() == UnitConverter.UNIT_LBS) {
                    weight = UnitConverter.KgtoLbs(maxValue.getStoredWeight());
                    unitStr = getContext().getString(R.string.LbsUnitLabel);
                } else {
                    weight = maxValue.getStoredWeight();
                    unitStr = getContext().getString(R.string.KgUnitLabel);
                }
                maxText.setText(numberFormat.format(weight) + " " + unitStr);
            } else {
                minText.setText("-");
                maxText.setText("-");
                getView().findViewById(R.id.minmaxLayout).setVisibility(View.GONE);
            }
        } else {
            minText.setText("-");
            maxText.setText("-");
            getView().findViewById(R.id.minmaxLayout).setVisibility(View.GONE);
        }
	}

	private void refreshData(){
		View fragmentView = getView();
		if(fragmentView != null) {
			if (getProfil() != null) {
                mDb.setProfil(getProfil());

                ArrayList<Machine> machineListArray;
                // Version avec table Machine
                machineListArray = mDbMachine.getAllMachinesArray();

                /* Init machines list*/
                machineEditAdapter = new MachineArrayFullAdapter(getContext(), machineListArray);
                machineEdit.setAdapter(machineEditAdapter);

                // Si on a change de profil
                mProfile = getProfil();

                /* Initialisation serie */
                if (machineEdit.getText().toString().isEmpty()) {
                    Fonte lLastRecord = mDb.getLastRecord(getProfil());
                    if (lLastRecord != null) {
                        machineEdit.setText(lLastRecord.getMachine());
                        Machine lMachine = mDbMachine.getMachine(lLastRecord.getMachine());
                        machineImage.setImageResource(R.drawable.ic_machine); // Default image
                        ImageUtil imgUtil = new ImageUtil();
                        imgUtil.setThumb(machineImage, imgUtil.getThumbPath(lMachine.getPicture())); // Overwrite image is there is one
                        serieEdit.setText(String.valueOf(lLastRecord.getSerie()));
                        repetitionEdit.setText(String.valueOf(lLastRecord.getRepetition()));
                        unitSpinner.setSelection(lLastRecord.getUnit());
                        DecimalFormat numberFormat = new DecimalFormat("#.##");
                        if (lLastRecord.getUnit() == UnitConverter.UNIT_LBS)
                            poidsEdit.setText(numberFormat.format(UnitConverter.KgtoLbs(lLastRecord.getPoids())));
                        else
                            poidsEdit.setText(numberFormat.format(lLastRecord.getPoids()));
                    } else {
                        // valeur par defaut
                        machineEdit.setText("");
                        serieEdit.setText("1");
                        repetitionEdit.setText("10");
                        poidsEdit.setText("50");
                    }
                } else { // Restore picture on fragment restore.
                    Machine lMachine = mDbMachine.getMachine(machineEdit.getText().toString());
                    machineImage.setImageResource(R.drawable.ic_machine); // Default image
                    ImageUtil imgUtil = new ImageUtil();
                    if (lMachine != null)
                        imgUtil.setThumb(machineImage, imgUtil.getThumbPath(lMachine.getPicture())); // Overwrite image is there is one
                }

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
        if (!hidden)
            refreshData();
	}

	public void hideKeyboard(View view) {
		InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

}

