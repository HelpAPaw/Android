package org.helpapaw.helpapaw.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;


public class LanguageCustomAdapter extends BaseAdapter {

    private final Context context;
    private final String[] languageNames;
    private final ArrayList<String> languageCodes;

    private int currentLanguageSelectionIndex;

    public LanguageCustomAdapter(Context context, String[] languageNames, String[] languageCodes, String selectedLanguageCode) {
        this.context = context;
        this.languageNames = languageNames;
        this.languageCodes = new ArrayList<>(Arrays.asList(languageCodes));
        this.currentLanguageSelectionIndex = Utils.getLanguageIndexFromLanguageCode(
                selectedLanguageCode,
                this.languageCodes
        );
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return languageNames.length;
    }

    @Override
    public Object getItem(int position) {
        return languageNames[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.language_item, null, true);
            holder.radioButton = convertView.findViewById(R.id.language_radiobutton);
            convertView.setTag(holder);
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder) convertView.getTag();
        }
        holder.radioButton.setText(languageNames[position]);
        holder.radioButton.setChecked(position == currentLanguageSelectionIndex);
        holder.radioButton.setTag(position);

        holder.radioButton.setOnClickListener(v -> {
            currentLanguageSelectionIndex = (Integer) holder.radioButton.getTag();
            notifyDataSetChanged();
        });
        return convertView;
    }

    public String getCurrentLanguageSelectionCode() {
        return getLanguageCodeFromLanguageIndex(this.currentLanguageSelectionIndex);
    }

    private static class ViewHolder {
        protected RadioButton radioButton;
    }

    private String getLanguageCodeFromLanguageIndex(int languageIndex) {
        String code = "en";
        try {
            code = languageCodes.get(languageIndex);
        } catch (Exception e) {
            // Do nothing - we will return en
        }
        return code;
    }
}