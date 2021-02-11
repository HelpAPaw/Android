package org.helpapaw.helpapaw.filtersignal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.signalsmap.SignalsMapFragment;

import java.util.Arrays;

public class FilterSignalTypeDialog extends DialogFragment {

    private static boolean[] signalTypeSelection;
    private SignalTypeCustomAdapter customAdapter;

    public FilterSignalTypeDialog() {
    }

    public static FilterSignalTypeDialog newInstance() {
        return new FilterSignalTypeDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.view_select_signal_type, container);
        init(view);
        return view;
    }

    private void init(View view) {

        ListView signalTypeListView = view.findViewById(R.id.signal_type_list_view);
        TextView selectAll = view.findViewById(R.id.txt_select_all_signal_types);
        TextView deselectAll = view.findViewById(R.id.txt_deselect_all_signal_types);
        TextView filter = view.findViewById(R.id.txt_filter_signal_types);
        TextView title = view.findViewById(R.id.txt_filter_signal_types_description);
        title.setText(R.string.txt_filter_signal_types_description);

        String[] signalType = getResources().getStringArray(R.array.signal_types_items);
        if (signalTypeSelection == null) {
            signalTypeSelection = new boolean[signalType.length];
            signalTypeSelection = setSelection(true);
        }

        customAdapter = new SignalTypeCustomAdapter(signalTypeListView.getContext(), signalType, signalTypeSelection);
        signalTypeListView.setAdapter(customAdapter);

        selectAll.setOnClickListener(v -> {
            customAdapter.refreshView(setSelection(true));
        });

        deselectAll.setOnClickListener(v -> {
            customAdapter.refreshView(setSelection(false));
        });

        filter.setOnClickListener(v -> {
            customAdapter.setSignalTypeSelectionToCurrent();
            signalTypeSelection = customAdapter.getSignalTypeSelection();

            SignalsMapFragment mParentFragment = (SignalsMapFragment) getParentFragment();
            mParentFragment.getOnSignalFilterClickListener(signalTypeSelection);

            dismiss();
        });
    }

    public static boolean[] getSignalTypeSelection() {
        return signalTypeSelection;
    }

    private boolean[] setSelection(boolean isSelect){
        boolean[] selection = new boolean[signalTypeSelection.length];

        Arrays.fill(selection, isSelect);

        return selection;
    }
}


