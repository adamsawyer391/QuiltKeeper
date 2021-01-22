package com.cosmicdesigns.quiltkeeper.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.cosmicdesigns.quiltkeeper.interfaces.InvestmentLoaderListener;
import com.cosmicdesigns.quiltkeeper.model.InvestmentModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class InvestmentRepository {

    private static final String TAG = "InvestmentRepository";

    static InvestmentRepository investmentRepositoryInstance;

    ArrayList<InvestmentModel> investmentModels = new ArrayList<>();

    static Context mContext;
    static InvestmentLoaderListener investmentLoaderListener;

    public static InvestmentRepository getInstance(Context context){
        mContext = context;
        if (investmentRepositoryInstance == null){
            investmentRepositoryInstance = new InvestmentRepository();
        }
        investmentLoaderListener = (InvestmentLoaderListener) mContext;
        return investmentRepositoryInstance;
    }

    public MutableLiveData<ArrayList<InvestmentModel>> getInvestments(){
        loadInvestments();
        MutableLiveData<ArrayList<InvestmentModel>> investmentLiveData = new MutableLiveData<>();
        investmentLiveData.setValue(investmentModels);
        return investmentLiveData;
    }

    private void loadInvestments(){
        Log.d(TAG, "loadInvestments: method is called");
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserID = mAuth.getCurrentUser().getUid();
        Query query = mReference.child("investments").child(currentUserID).orderByChild("timestamp");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "loadInvestments: onDataChange: snapshot : " + snapshot);
                investmentModels.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    investmentModels.add(dataSnapshot.getValue(InvestmentModel.class));
                    Log.d(TAG, "onDataChange: investmentmodels : " + investmentModels);
                }
                investmentLoaderListener.onInvestmentsLoaded();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
