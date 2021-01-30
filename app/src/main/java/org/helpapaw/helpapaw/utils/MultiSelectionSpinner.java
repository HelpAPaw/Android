package org.helpapaw.helpapaw.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiSelectionSpinner extends androidx.appcompat.widget.AppCompatSpinner implements
        DialogInterface.OnMultiChoiceClickListener {

    private static final String TAP_TO_FILTER = "Tap to filter";

    private List<String> items = null;
    private boolean[] selection = null;
    private ArrayAdapter adapter;
    private boolean selectionChanged = false;

    public MultiSelectionSpinner(Context context) {
        super(context);

        adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item);
        super.setAdapter(adapter);
    }

    public MultiSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item);
        super.setAdapter(adapter);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (selection != null && which < selection.length) {
            selection[which] = isChecked;

            adapter.clear();
            adapter.add(buildSelectedItemString());
        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds.");
        }
    }

    @Override
    public boolean performClick() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        String[] itemNames = new String[items.size()];

        for (int i = 0; i < items.size(); i++) {
            itemNames[i] = items.get(i);
        }

        builder.setMultiChoiceItems(itemNames, selection, this);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                selectionChanged = true;
                // TODO: find a way to refresh view when selection is changed
            }
        });
        builder.show();
        return true;
    }

    public void setItems(List<String> items) {
        this.items = items;
        selection = new boolean[this.items.size()];
        adapter.clear();
        adapter.add(TAP_TO_FILTER);
        Arrays.fill(selection, true);
    }

    public boolean[] getSelection() {
        return selection;
    }

    public boolean isSelectionChanged() {
        return selectionChanged;
    }

    public List<String> getSelectedItems() {
        List<String> selectedItems = new ArrayList<>();

        for (int i = 0; i < items.size(); ++i) {
            if (selection[i]) {
                selectedItems.add(items.get(i));
            }
        }

        return selectedItems;
    }

    private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        int numberOfSelectedItems = 0;
        boolean foundOne = false;

        for (int i = 0; i < items.size(); ++i) {
            if (selection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }

                numberOfSelectedItems++;
                foundOne = true;

                sb.append(items.get(i));
            }
        }

        if (numberOfSelectedItems == items.size()) {
            return TAP_TO_FILTER;
        } else {
            return sb.toString();
        }
    }
}