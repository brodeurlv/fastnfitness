package com.easyfitness.programs;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

//import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOProgram;
//import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Program;
import com.easyfitness.R;
import com.easyfitness.utils.ImageUtil;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

public class ProgramCursorAdapter extends CursorAdapter implements Filterable {

    DAOProgram mDbMachine = null;
//    MaterialFavoriteButton iFav = null;
    private LayoutInflater mInflater;

    public ProgramCursorAdapter(Context context, Cursor c, int flags, DAOProgram pDbMachine) {
        super(context, c, flags);
        mDbMachine = pDbMachine;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView t0 = view.findViewById(R.id.LIST_Program_ID);
        t0.setText(cursor.getString(cursor.getColumnIndex(DAOProgram.KEY)));

        TextView t1 = view.findViewById(R.id.LIST_Program_name);
        t1.setText(cursor.getString(cursor.getColumnIndex(DAOProgram.PROGRAM_NAME)));

//
//        iFav = view.findViewById(R.id.LIST_MACHINE_FAVORITE);
//        boolean bFav = cursor.getInt(6) == 1;
//        iFav.setFavorite(bFav);
//        iFav.setRotationDuration(500);
//        iFav.setAnimateFavorite(true);
//        iFav.setTag(cursor.getLong(0));
//
//        iFav.setOnClickListener(v -> {
//            MaterialFavoriteButton mFav = (MaterialFavoriteButton) v;
//            boolean t = mFav.isFavorite();
//            mFav.setFavoriteAnimated(!t);
//            if (mDbMachine != null) {
//                Program m = mDbMachine.getMachine((long) mFav.getTag());
////                m.setFavorite(!t);
//                mDbMachine.updateRecord(m);
//            }
//        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.program_list_row, parent, false);

    }

}
