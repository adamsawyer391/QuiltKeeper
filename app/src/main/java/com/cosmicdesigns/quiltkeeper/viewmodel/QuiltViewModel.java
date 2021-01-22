package com.cosmicdesigns.quiltkeeper.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.cosmicdesigns.quiltkeeper.model.Quilt;
import com.cosmicdesigns.quiltkeeper.repositories.QuiltRepository;
import java.util.ArrayList;

public class QuiltViewModel extends ViewModel {

    private static final String TAG = "QuiltViewModel";

    MutableLiveData<ArrayList<Quilt>> quilts;

    public void init(Context context, String input, String type){
        Log.d(TAG, "init: method called : " + input);
//        if (quilts != null){
//            return;
//        }
        quilts = QuiltRepository.getInstance(context).getQuilts(input, type);
    }

    public LiveData<ArrayList<Quilt>> getQuilts(){
        return quilts;
    }

}
