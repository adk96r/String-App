package adk.string;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends Activity {


    Switch mSaveHistorySwitch;
    TextView mClearHistoryTextView;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Toolbar back.
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Toolbar title.
        ((TextView) findViewById(R.id.title_text_view)).setText(R.string.settings);


        mSaveHistorySwitch = findViewById(R.id.save_history_switch);
        mClearHistoryTextView = findViewById(R.id.clear_history_text_view);
        context = SettingsActivity.this;

        // Set the switch's initial position based on user's preference to save words in history.
        mSaveHistorySwitch.setChecked(getSharedPreferences("Preferences", MODE_PRIVATE)
                .getBoolean("SAVE_HISTORY", true));

        // Update the user's preference.
        mSaveHistorySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences("Preferences", MODE_PRIVATE)
                        .edit()
                        .putBoolean("SAVE_HISTORY", isChecked)
                        .apply();
            }
        });

        // Clears the history.
        mClearHistoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                new AlertDialog.Builder(context)
                        .setTitle("Are you sure you want to clear the history ?")
                        .setPositiveButton("Yes, clear it.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Delete the shared preferences.
                                getSharedPreferences("Preferences", MODE_PRIVATE)
                                        .edit()
                                        .putString("HISTORY", null)
                                        .apply();

                                // Show a snack bar.
                                Snackbar.make(v, "Deleted successfully.", Snackbar.LENGTH_LONG)
                                        .setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                                        .setAction("DISMISS", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // Do nothing.
                                            }
                                        })
                                        .show();

                            }
                        })
                        .setNegativeButton("No, go back.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing.
                            }
                        }).show();


            }
        });

        // Worknik logo button.
        findViewById(R.id.wordnik_logo_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.wordnik.com/")));
            }
        });

        // Fluidic logo button.
        findViewById(R.id.fluidic_logo_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/adk96r/fluidic")));
            }
        });


        // Google logo button.
        findViewById(R.id.google_logo_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://material.io/icons/")));
            }
        });

    }
}
