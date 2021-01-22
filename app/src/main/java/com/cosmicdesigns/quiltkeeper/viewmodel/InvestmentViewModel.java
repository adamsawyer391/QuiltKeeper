package com.cosmicdesigns.quiltkeeper.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cosmicdesigns.quiltkeeper.model.InvestmentModel;
import com.cosmicdesigns.quiltkeeper.repositories.InvestmentRepository;

import java.util.ArrayList;

public class InvestmentViewModel extends ViewModel {

    MutableLiveData<ArrayList<InvestmentModel>> investments;

    public void init(Context context){
        if (investments != null){
            return;
        }
        investments = InvestmentRepository.getInstance(context).getInvestments();
    }

    public LiveData<ArrayList<InvestmentModel>> getInvestments(){
        return investments;
    }
}
