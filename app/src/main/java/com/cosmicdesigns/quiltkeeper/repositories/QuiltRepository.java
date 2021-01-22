package com.cosmicdesigns.quiltkeeper.repositories;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cosmicdesigns.quiltkeeper.interfaces.QuiltLoaderListener;
import com.cosmicdesigns.quiltkeeper.model.Quilt;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuiltRepository {

    private static final String TAG = "QuiltRepository";

    //firebase
    DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

    static QuiltRepository quiltRepositoryInstance;

    ArrayList<Quilt> quilts = new ArrayList<>();
    ArrayList<Quilt> dates = new ArrayList<>();

    static Context mContext;
    static QuiltLoaderListener quiltLoaderListener;

    public static QuiltRepository getInstance(Context context){
        mContext = context;
        if (quiltRepositoryInstance == null){
            quiltRepositoryInstance = new QuiltRepository();
        }
        quiltLoaderListener = (QuiltLoaderListener) mContext;
        return quiltRepositoryInstance;
    }

    public MutableLiveData<ArrayList<Quilt>> getQuilts(String input, String type){
        Log.d(TAG, "getQuilts: mathod called");
        loadQuilts(input, type);
        MutableLiveData<ArrayList<Quilt>> quiltLiveData = new MutableLiveData<>();
        quiltLiveData.setValue(quilts);
        return quiltLiveData;
    }

    private void loadQuilts(String input, String type){
        Log.d(TAG, "loadQuilts: method called");
        Log.d(TAG, "loadQuilts: input : " + input);
        Log.d(TAG, "loadQuilts: type : " + type);
        if (input.equals("")){
            Query query = mReference.child("user_quilts").child(currentUserID).orderByChild("mTimeStamp");
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    quilts.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        quilts.add(dataSnapshot.getValue(Quilt.class));
                    }
                    quiltLoaderListener.onQuiltsLoaded();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            Log.d(TAG, "loadQuilts: input : " + input);
            if (type.equals("date")){
                Log.d(TAG, "loadQuilts: date selected");
                mReference.child("user_quilts").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        quilts.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            String key = dataSnapshot.getKey();
                            String mDate = snapshot.child(key).child("mFinishDate").getValue().toString();
                            Log.d(TAG, "onDataChange: m date per item quilt : " + mDate);
                            if (input.equals(mDate)){
                                Log.d(TAG, "onDataChange: key : " + key);
                                quilts.add(dataSnapshot.getValue(Quilt.class));
                                Log.d(TAG, "onDataChange: quilts : " + quilts);
                            }
                        }
                        quiltLoaderListener.onQuiltsLoaded();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }else if (type.equals("keyword")){
                Log.d(TAG, "loadQuilts: key word selected");
                mReference.child("user_quilts").child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        quilts.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            String key = dataSnapshot.getKey();
                            String notes = snapshot.child(key).child("mNotes").getValue().toString();
                            String title = snapshot.child(key).child("mQuiltName").getValue().toString();
                            Log.d(TAG, "onDataChange: notes : " + notes);
                            if (notes.toLowerCase().contains(input.toLowerCase())){
                                quilts.add(dataSnapshot.getValue(Quilt.class));
                            }else if (title.toLowerCase().contains(input.toLowerCase())){
                                quilts.add(dataSnapshot.getValue(Quilt.class));
                            }
                        }
                        quiltLoaderListener.onQuiltsLoaded();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

        }

    }

}
