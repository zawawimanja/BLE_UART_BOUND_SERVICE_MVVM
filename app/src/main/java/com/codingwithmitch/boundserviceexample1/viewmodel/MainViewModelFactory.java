package com.codingwithmitch.boundserviceexample1.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.codingwithmitch.boundserviceexample1.service.MyService;


/**
 * Created by Ali Esa Assadi on 19/12/2018.
 */
public class MainViewModelFactory implements ViewModelProvider.Factory {

    private final MyService myService;

    public MainViewModelFactory(MyService myService) {
        this.myService = myService;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainActivityViewModel.class)) {
            return (T) new MainActivityViewModel(myService);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }

}
