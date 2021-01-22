package com.cosmicdesigns.quiltkeeper.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cosmicdesigns.quiltkeeper.repositories.TotalsRepository;

import java.util.ArrayList;

public class TotalViewModel extends ViewModel {

    MutableLiveData<ArrayList> investments;
    MutableLiveData<ArrayList> costs;

    public void init(Context context){
        if (investments != null){
            return;
        }
        investments = TotalsRepository.getInstance(context).getInvestments();
        costs = TotalsRepository.getInstance(context).getCosts();
    }

    public LiveData<ArrayList> getInvestments(){
        return investments;
    }

    public LiveData<ArrayList> getCosts(){
        return costs;
    }
}
