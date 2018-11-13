package com.example.android.newswiz.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.example.android.newswiz.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * The user has the option to delete their account that was created in the Firebase DB. A dialog
 * message will display to get the user to confirm account deletion.
* */

public class DeleteAccountPreference extends DialogPreference {

    private deleteAccountListener listener;

    public interface deleteAccountListener{
        void deleteAccount();
    }

    @Override
    protected void onAttachedToActivity() {
        super.onAttachedToActivity();
        listener = (deleteAccountListener) getContext();
    }

    public DeleteAccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources res = getContext().getResources();
        setDialogTitle(res.getString(R.string.remove_account_title));
        setDialogMessage(res.getString(R.string.remove_account_msg));
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {

        DatabaseReference sourcesDB = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // When the user selects "OK" then delete account in Firebase DB
        if(positiveResult){
            sourcesDB.child(user.getUid()).removeValue();
            listener.deleteAccount();
        }
    }
}
