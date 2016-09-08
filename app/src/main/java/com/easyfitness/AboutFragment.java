package com.easyfitness;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.LicensesDialogFragment;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.GnuGeneralPublicLicense20;
import de.psdev.licensesdialog.licenses.GnuLesserGeneralPublicLicense21;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

import com.easyfitness.R;
import com.easyfitness.DAO.DatabaseHelper;

public class AboutFragment extends Fragment {
	private String name; 
	private int id;
	private MainActivity mActivity=null;
	
	private TextView mpDBVersionTextView = null;
	private TextView mpMPAndroidChartTextView = null;
	private TextView mpjavaCVSTextView = null;
	private TextView mpLicenseDialogTextView = null;

	
    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static AboutFragment newInstance(String name, int id) {
    	AboutFragment f = new AboutFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		this.mActivity = (MainActivity) activity;
	}
	
	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) { 
		
		View view =  inflater.inflate(R.layout.tab_about, container, false); 
				
		mpDBVersionTextView = (TextView) view.findViewById(R.id.database_version);
		mpDBVersionTextView.setText(Integer.toString(DatabaseHelper.DATABASE_VERSION));

		mpMPAndroidChartTextView = (TextView) view.findViewById(R.id.MPAndroidChart);
		mpjavaCVSTextView = (TextView) view.findViewById(R.id.javaCSV);
		mpLicenseDialogTextView = (TextView) view.findViewById(R.id.LicensesDialog);

		mpMPAndroidChartTextView.setOnClickListener(clickLicense);
		mpjavaCVSTextView.setOnClickListener(clickLicense);
		mpLicenseDialogTextView.setOnClickListener(clickLicense);

		// Inflate the layout for this fragment 
		return view;
	}

	public MainActivity getMainActivity() {
		return this.mActivity;
	}

	private View.OnClickListener clickLicense = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			String name = null;
			String url = null;
			String copyright = null;
			License license = null;

			switch(v.getId()) {
				case R.id.MPAndroidChart:
					name = "MPAndroidChart";
					url = "https://github.com/PhilJay/MPAndroidChart";
					copyright = "Copyright 2016 Philipp Jahoda";
					license = new ApacheSoftwareLicense20();
					break;
				case R.id.javaCSV:
					name = "javaCSV";
					url = "https://sourceforge.net/projects/javacsv/";
					copyright = "";
					license = new GnuLesserGeneralPublicLicense21();
					break;
				case R.id.LicensesDialog:
					name = "LicensesDialog";
					url = "http://psdev.de";
					copyright = "Copyright 2013 Philip Schiffer <admin@psdev.de>";
					license = new ApacheSoftwareLicense20();
					break;
			}


			final Notice notice = new Notice(name, url, copyright, license);
			new LicensesDialog.Builder(getMainActivity())
					.setNotices(notice)
					.build()
					.show();
		}
	};

}

