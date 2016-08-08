package com.easyfitness;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easyfitness.R;
import com.easyfitness.DAO.DatabaseHelper;

public class AboutFragment extends Fragment {
	private String name; 
	private int id;    
	
	private TextView mpDBVersionTextView = null;
	
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) { 
		
		View view =  inflater.inflate(R.layout.tab_about, container, false); 
				
		mpDBVersionTextView = (TextView) view.findViewById(R.id.database_version);
		mpDBVersionTextView.setText(Integer.toString(DatabaseHelper.DATABASE_VERSION));

		// Inflate the layout for this fragment 
		return view;
	}
}

