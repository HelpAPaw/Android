package org.helpapaw.helpapaw.faq;

import androidx.databinding.DataBindingUtil;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.databinding.ActivityFaqsViewBinding;

public class FAQsView extends AppCompatActivity {
    ActivityFaqsViewBinding binding;
    TextView mTextFaqs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    private void initViews() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_faqs_view);
        setSupportActionBar(binding.toolbar);

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
            binding.toolbarTitle.setText(getString(R.string.txt_faqs_view_title));
        }
        binding.tvFaqsText.setText(Html.fromHtml(getString(R.string.string_faq)));
    }
//    "Q1" = "How does this app work?";
//    "A1" = "If you see a stray animal that needs help but for some reason cannot provide the help yourself you can submit a signal that marks the place and describes the situation. Other people that are nearby will receive a notification about it. Hopefully someone will react to the signal and help the animal.";
//    "Q2" = "Who are the people that will help those animals?";
//    "A2" = "Help A Paw connects a network of volunteers that care about animals - just like you!";
//    "Q3" = "How does the status of the signal change?";
//    "A3" = "When a signal is submitted it starts with status 'Help needed'. When somebody decides to answer the signal he/she changes the status to 'Somebody on the way' so that other people know. If for example the person arrives at the place but needs some assistance the status can be changed back to 'Help needed'. When the animal finally receives the needed help the signal is marked as 'Solved'. Signals are color-coded in red, orange and green according to their status.";
//    "Q4" = "Does this app track my location?";
//    "A4" = "No. Your location is obtained and used only locally on your device. It will not be recorded on a server or used with any other purpose beside notifying you of animals in need in your area.";

    private StringBuilder generateText() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\'Q1\'");
        stringBuilder.append("\t");
        stringBuilder.append("=");
        stringBuilder.append("\t");
        stringBuilder.append("\'How does this app work?\'");
        stringBuilder.append("\n\n");
        stringBuilder.append("\'A1\'");
        stringBuilder.append("\t");
        stringBuilder.append("=");
        stringBuilder.append("\t");
        stringBuilder.append("\'If you see a stray animal that needs help but for some reason cannot provide the help yourself you can submit a signal that marks the place and describes the situation. Other people that are nearby will receive a notification about it. Hopefully someone will react to the signal and help the animal.\'");
        stringBuilder.append("\n\n");
        stringBuilder.append("\'Q2\'");
        stringBuilder.append("\t");
        stringBuilder.append("=");
        stringBuilder.append("\t");
        stringBuilder.append("\'Who are the people that will help those animals?\'");
        stringBuilder.append("\n\n");
        stringBuilder.append("\'A2\'");
        stringBuilder.append("\t");
        stringBuilder.append("=");
        stringBuilder.append("\t");
        stringBuilder.append("\'Help A Paw connects a network of volunteers that care about animals - just like you!\'");
        stringBuilder.append("\n\n");
        stringBuilder.append("\'Q3\'");
        stringBuilder.append("\t");
        stringBuilder.append("=");
        stringBuilder.append("\t");
        stringBuilder.append("\'How does the status of the signal change?\'");
        stringBuilder.append("\n\n");
        stringBuilder.append("\'A3\'");
        stringBuilder.append("\t");
        stringBuilder.append("=");
        stringBuilder.append("\t");
        stringBuilder.append("\'When a signal is submitted it starts with status 'Help needed'. When somebody decides to answer the signal he/she changes the status to 'Somebody on the way' so that other people know. If for example the person arrives at the place but needs some assistance the status can be changed back to 'Help needed'. When the animal finally receives the needed help the signal is marked as 'Solved'. Signals are color-coded in red, orange and green according to their status.\'");
        stringBuilder.append("\n\n");
        stringBuilder.append("\'Q4\'");
        stringBuilder.append("\t");
        stringBuilder.append("=");
        stringBuilder.append("\t");
        stringBuilder.append("\"Does this app track my location?\'");
        stringBuilder.append("\n\n");
        stringBuilder.append("\'A4\'");
        stringBuilder.append("\t");
        stringBuilder.append("=");
        stringBuilder.append("\t");
        stringBuilder.append("\'No. Your location is obtained and used only locally on your device. It will not be recorded on a server or used with any other purpose beside notifying you of animals in need in your area.\'");

        return stringBuilder;
    }
}
