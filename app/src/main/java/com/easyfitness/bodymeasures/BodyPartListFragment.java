package com.easyfitness.bodymeasures;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.bodymeasures.DAOBodyPart;
import com.easyfitness.MainActivity;
import com.easyfitness.R;

import java.util.ArrayList;

public class BodyPartListFragment extends Fragment {
    ArrayList<BodyPart> dataModels;
    ListView measureList = null;

    private OnItemClickListener onClickListItem = (parent, view, position, id) -> {

        TextView textView = view.findViewById(R.id.LIST_BODYPART_ID);
        int bodyPartID = Integer.parseInt(textView.getText().toString());

        BodyPartDetailsFragment fragment = BodyPartDetailsFragment.newInstance(bodyPartID, true);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, fragment, MainActivity.BODYTRACKINGDETAILS);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    };

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
        DAOBodyPart mdbBodyPart = new DAOBodyPart(this.getContext());

        measureList = view.findViewById(R.id.listBodyMeasures);

        dataModels = new ArrayList<>();

        /*
        dataModels.add(new BodyPart(BodyPartExtensions.LEFTBICEPS, mdbMeasure.getLastBodyMeasures(BodyPartExtensions.LEFTBICEPS, ((MainActivity) getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPartExtensions.RIGHTBICEPS, mdbMeasure.getLastBodyMeasures(BodyPartExtensions.RIGHTBICEPS, ((MainActivity) getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPartExtensions.PECTORAUX, mdbMeasure.getLastBodyMeasures(BodyPartExtensions.PECTORAUX, ((MainActivity) getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPartExtensions.WAIST, mdbMeasure.getLastBodyMeasures(BodyPartExtensions.WAIST, ((MainActivity) getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPartExtensions.BEHIND, mdbMeasure.getLastBodyMeasures(BodyPartExtensions.BEHIND, ((MainActivity) getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPartExtensions.LEFTTHIGH, mdbMeasure.getLastBodyMeasures(BodyPartExtensions.LEFTTHIGH, ((MainActivity) getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPartExtensions.RIGHTTHIGH, mdbMeasure.getLastBodyMeasures(BodyPartExtensions.RIGHTTHIGH, ((MainActivity) getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPartExtensions.LEFTCALVES, mdbMeasure.getLastBodyMeasures(BodyPartExtensions.LEFTCALVES, ((MainActivity) getActivity()).getCurrentProfil())));
        dataModels.add(new BodyPart(BodyPartExtensions.RIGHTCALVES, mdbMeasure.getLastBodyMeasures(BodyPartExtensions.RIGHTCALVES, ((MainActivity) getActivity()).getCurrentProfil())));
        */

        dataModels.addAll(mdbBodyPart.getBodyPartList());

        BodyPartListAdapter adapter = new BodyPartListAdapter(dataModels, getContext());

        measureList.setAdapter(adapter);
        measureList.setOnItemClickListener(onClickListItem);

        return view;
    }

    public BodyPartListFragment getThis() {
        return this;
    }

}
