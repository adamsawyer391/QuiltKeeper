package com.cosmicdesigns.quiltkeeper.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.cosmicdesigns.quiltkeeper.interfaces.TotalsLoadedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class TotalsRepository {

    private static final String TAG = "TotalsRepository";

    static TotalsRepository totalsRepositoryInstance;

    ArrayList costs = new ArrayList();
    ArrayList investments = new ArrayList();

    static Context mContext;
    static TotalsLoadedListener totalsLoadedListener;

    final private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final private String currentUserID = mAuth.getCurrentUser().getUid();
    final private DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();

    public static TotalsRepository getInstance(Context context){
        mContext = context;
        if (totalsRepositoryInstance == null){
            totalsRepositoryInstance = new TotalsRepository();
        }
        totalsLoadedListener = (TotalsLoadedListener) mContext;
        return totalsRepositoryInstance;
    }

    public MutableLiveData<ArrayList> getCosts(){
        loadCosts();
        MutableLiveData<ArrayList> costsLiveData = new MutableLiveData<>();
        costsLiveData.setValue(costs);
        return costsLiveData;
    }

    public MutableLiveData<ArrayList> getInvestments(){
        loadInvestments();
        MutableLiveData<ArrayList> investmentsLiveData = new MutableLiveData<>();
        investmentsLiveData.setValue(investments);
        return investmentsLiveData;
    }

    private void loadInvestments(){
        mReference.child("investments").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                investments.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String key = dataSnapshot.getKey();
                    Log.d(TAG, "obtainTotalInvestment: onDataChange: snapshot : " + snapshot);
                    assert key != null;
                    float mCost = Float.parseFloat(Objects.requireNonNull(snapshot.child(key).child("investment_cost").getValue()).toString());
                    Log.d(TAG, "obtainTotalInvestment: onDataChange: investment array list : " + mCost);
                    investments.add(mCost);
                    Log.d(TAG, "obtainTotalInvestment: onDataChange: array list values : " + investments);
                }
                //totalsLoadedListener.onCostsTotalsLoaded();
                totalsLoadedListener.onInvestmentsTotalsLoaded();
                //produceSum(investments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCosts(){
        mReference.child("user_quilts").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                costs.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String key = dataSnapshot.getKey();
                    assert key != null;
                    float mCost = Float.parseFloat(Objects.requireNonNull(snapshot.child(key).child("mCost").getValue()).toString());
                    Log.d(TAG, "obtainTotalCost: onDataChange: cost array list : " + mCost);
                    costs.add(mCost);
                    Log.d(TAG, "obtainTotalCost: onDataChange: array list values : " + costs);
                }
                totalsLoadedListener.onCostsTotalsLoaded();
                //produceCostSum(costList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
