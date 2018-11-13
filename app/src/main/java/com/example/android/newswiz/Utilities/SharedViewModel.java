package com.example.android.newswiz.Utilities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

/**
 This SharedViewModel class helps sharing data between the LHS and RHS child fragments.
As this app uses master detail fragments they need to somehow
communicate with each other and this ViewModel class helps assist with this.
*
* */

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Integer> selected = new MutableLiveData<>();

    public void select(int position){
        selected.setValue(position);
    }

    public LiveData<Integer> getSelected(){
        return selected;
    }
}
