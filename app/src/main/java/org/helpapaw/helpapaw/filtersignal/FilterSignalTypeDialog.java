package org.helpapaw.helpapaw.filtersignal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import org.helpapaw.helpapaw.R;

import java.util.Arrays;

public class FilterSignalTypeDialog extends DialogFragment {

    public static final int REQUEST_UPDATE_SIGNAL_TYPE_SELECTION = 7;
    public static final String EXTRA_SIGNAL_TYPE_SELECTION = "EXTRA_SIGNAL_TYPE_SELECTION";

    private static boolean[] signalTypeSelection;
    private SignalTypeCustomAdapter customAdapter;

    public FilterSignalTypeDialog() {
    }

    public static FilterSignalTypeDialog newInstance() {
        return new FilterSignalTypeDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.txt_filter_signal_types_description);

        View view = getActivity().getLayoutInflater().inflate(R.layout.view_select_signal_type, null);

        ListView signalTypeListView = view.findViewById(R.id.signal_type_list_view);
        String[] signalType = getResources().getStringArray(R.array.signal_types_items);
        if (signalTypeSelection == null) {
            signalTypeSelection = new boolean[signalType.length];
            signalTypeSelection = setSelection(true);
        }
        customAdapter = new SignalTypeCustomAdapter(signalTypeListView.getContext(), signalType, signalTypeSelection);
        signalTypeListView.setAdapter(customAdapter);

        dialog.setView(view);

        dialog.setPositiveButton(R.string.txt_filter_signal_types, (dialog1, which) -> {
            customAdapter.setSignalTypeSelectionToCurrent();
            signalTypeSelection = customAdapter.getSignalTypeSelection();

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_SIGNAL_TYPE_SELECTION, signalTypeSelection);
            getParentFragment().onActivityResult(REQUEST_UPDATE_SIGNAL_TYPE_SELECTION, Activity.RESULT_OK, resultIntent);

            //TODO: Delete this
//            SignalsMapFragment mParentFragment = (SignalsMapFragment) getParentFragment();
//            mParentFragment.getOnSignalFilterClickListener(signalTypeSelection);

            dismiss();
        });

        return dialog.create();
    }

    public static boolean[] getSignalTypeSelection() {
        return signalTypeSelection;
    }

    private boolean[] setSelection(boolean isSelect) {
        boolean[] selection = new boolean[signalTypeSelection.length];

        Arrays.fill(selection, isSelect);

        return selection;
    }
}


