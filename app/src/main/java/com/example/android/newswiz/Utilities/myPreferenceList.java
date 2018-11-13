package com.example.android.newswiz.Utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * This custom myPreferenceList class is to create the "OK" button in the dialogue box when the
 * user clicks onto the preferences. Currently the default dialogue box only has a "Cancel" button
 * and also when user selects option it immediately disappears.
* */

public class myPreferenceList extends ListPreference implements DialogInterface.OnClickListener {

    private int mClickedDialogEntryIndex;

    public myPreferenceList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public myPreferenceList(Context context) {
        this(context, null);
    }


    private int getValueIndex() {
        return findIndexOfValue(this.getValue() +"");
    }


    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        mClickedDialogEntryIndex = getValueIndex();
        builder.setSingleChoiceItems(this.getEntries(), mClickedDialogEntryIndex, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mClickedDialogEntryIndex = which;

            }
        });

        System.out.println(getEntry() + " " + this.getEntries()[0]);
        builder.setPositiveButton("OK", this);
    }

    public  void onClick (DialogInterface dialog, int which)
    {
        this.setValue(this.getEntryValues()[mClickedDialogEntryIndex]+"");
    }
}
