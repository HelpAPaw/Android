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


public class FilterSignalTypeDialog extends DialogFragment {

    public static final int REQUEST_UPDATE_SIGNAL_TYPE_SELECTION = 7;
    public static final String EXTRA_SIGNAL_TYPE_SELECTION = "EXTRA_SIGNAL_TYPE_SELECTION";

    private boolean[] signalTypeSelection;
    private SignalTypeCustomAdapter customAdapter;

    public FilterSignalTypeDialog() {
    }

    public static FilterSignalTypeDialog newInstance(boolean[] currentSignalTypeSelection) {
        FilterSignalTypeDialog filterSignalTypeDialog = new FilterSignalTypeDialog();
        filterSignalTypeDialog.signalTypeSelection = currentSignalTypeSelection;
        return filterSignalTypeDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.txt_filter_signal_types_description);

        View view = getActivity().getLayoutInflater().inflate(R.layout.view_select_signal_type, null);

        ListView signalTypeListView = view.findViewById(R.id.signal_type_list_view);
        String[] signalTypes = getResources().getStringArray(R.array.signal_types_items);
        customAdapter = new SignalTypeCustomAdapter(signalTypeListView.getContext(), signalTypes, signalTypeSelection);
        signalTypeListView.setAdapter(customAdapter);

        dialog.setView(view);

        dialog.setPositiveButton(R.string.txt_filter_signal_types, (dialog1, which) -> {
            customAdapter.setSignalTypeSelectionToCurrent();
            signalTypeSelection = customAdapter.getSignalTypeSelection();

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_SIGNAL_TYPE_SELECTION, signalTypeSelection);
            getParentFragment().onActivityResult(REQUEST_UPDATE_SIGNAL_TYPE_SELECTION, Activity.RESULT_OK, resultIntent);

            dismiss();
        });

        return dialog.create();
    }
}


