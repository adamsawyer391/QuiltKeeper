package com.cosmicdesigns.quiltkeeper.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.cosmicdesigns.quiltkeeper.R;
import com.cosmicdesigns.quiltkeeper.SettingsActivity;
import com.cosmicdesigns.quiltkeeper.adapter.SpinnerDropdownAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import es.dmoral.toasty.Toasty;

public class SpinnerModificationActivity extends AppCompatActivity {

    private static final String TAG = "SpinnerModificationActi";

    private Context mContext;
    private Spinner spinnerInput;
    private ImageView addSpinnerItem;
    private RecyclerView recyclerView;
    private SpinnerDropdownAdapter spinnerDropdownAdapter;
    private FirebaseAuth firebaseAuth;
    private ArrayList<String> dropdownCategories = new ArrayList<>();
    private String item;
    private ImageView undoDelete;
    private ArrayList<String> undoList = new ArrayList<>();
    private int size = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spinner_modification);
        mContext = SpinnerModificationActivity.this;
        firebaseAuth = FirebaseAuth.getInstance();

        if (savedInstanceState != null){
            if (savedInstanceState.containsKey("Size")){
                size = savedInstanceState.getInt("Size");
                Log.d(TAG, "onCreate: size : " + size);
            }
            undoList = savedInstanceState.getStringArrayList("UndoList");
        }

        spinnerInput = findViewById(R.id.spinner_input);
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v-> enableBackNavigation());

        undoDelete = findViewById(R.id.undoDelete);
        undoDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undo();
            }
        });

        setupRecyclerView();
        itemTouchHelper();
        setupSpinner();
    }







    /**
     * -----------------------------RECYCLER VIEW METHODS-------------------------------------------
     */
    private void setupRecyclerView(){
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void itemTouchHelper(){
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                String key = spinnerDropdownAdapter.getKey(viewHolder.getAdapterPosition());
                Log.d(TAG, "onSwiped: key : " + key);
                addToUndoList(key);
                String adjustedItem = getPath(item);
                //checkIfDefault(key, adjustedItem);
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dropdowns").child(firebaseAuth.getCurrentUser().getUid())
                        .child(adjustedItem).child(key);
                databaseReference.removeValue();
            }
        }).attachToRecyclerView(recyclerView);

        addSpinnerItem = findViewById(R.id.addSpinnerItem);
        addSpinnerItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToSelectedItem();
            }
        });
    }










    /**
     * ------------------------------SUPPORT METHODS----------------------------------------------
     */

//    private void checkIfDefault(String key, String adjustedItem){
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dropdowns").child(firebaseAuth.getCurrentUser().getUid())
//                .child(adjustedItem);
//        databaseReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.hasChild("default")){
//                    transferDefaultStatus(adjustedItem);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//
//    private void transferDefaultStatus(String adjustedItem) {
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dropdowns").child(firebaseAuth.getCurrentUser().getUid())
//                .child(adjustedItem);
//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                    long count = dataSnapshot.getChildrenCount();
//                    Random random = new Random();
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    private void undo(){
        Log.d(TAG, "undo: undo list : " + undoList);
        size = undoList.size();
        Log.d(TAG, "undo: size : " + size);
        if (size >= 1){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dropdowns").child(firebaseAuth.getCurrentUser().getUid());
            String key = databaseReference.push().getKey();
            String adjustedItem = getPath(item);
            String newItem = undoList.get(size - 1);
            databaseReference.child(adjustedItem).child(key).child("value").setValue(newItem);
            undoList.remove(size - 1);
            size = size - 1;
            Log.d(TAG, "undo: size : " + size);
        }else{
            Toasty.info(mContext, "Cannot undo", Toasty.LENGTH_SHORT).show();
        }
        Log.d(TAG, "undo: undo list : " + undoList);
        Log.d(TAG, "undo: size : " + size);
    }

    private void addToUndoList(String key){
        String adjustedItem = getPath(item);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dropdowns").child(firebaseAuth.getCurrentUser().getUid()).child(adjustedItem)
                .child(key).child("value");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "addToUndoList: onDataChange: snapshot : " + snapshot);
                String value = snapshot.getValue().toString();
                undoList.add(value);
                Log.d(TAG, "onDataChange: undo list : " + undoList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setupSpinner(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dropdowns").child(firebaseAuth.getCurrentUser().getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dropdownCategories.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String key = dataSnapshot.getKey();
                    dropdownCategories.add(key);
                    Log.d(TAG, "onDataChange: dropdown categories : " + dropdownCategories);
                }

                //adjust dropdown categories
                String[] adjustedList = {"Batting", "Bobbin", "Bobbin Color", "Design", "Frame", "Machine", "Made By", "Needle", "SPI", "Top Tension", "Top Thread",
                            "Top Thread Color", "Towa"};

//                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext, R.layout.layout_spinner, dropdownCategories);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext, R.layout.layout_spinner, adjustedList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerInput.setAdapter(arrayAdapter);
                spinnerInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        item = parent.getSelectedItem().toString();
                        String adjustedItem = getPath(item);
                        Log.d(TAG, "onItemSelected: item : " + item);
                        updateRecyclerView(adjustedItem);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getPath(String item){
        String value = "";
        switch (item){
            case "Made By":
                value = "made_by";
                return value;
            case "Batting":
                value = "batting";
                return value;
            case "Bobbin Thread":
                value = "bobbin";
                return value;
            case "Bobbin Thread Color":
                value = "bobbin_color";
                return value;
            case "Design":
                value = "design";
                return value;
            case "Frame":
                value = "frame";
                return value;
            case "Machine":
                value = "machine";
                return value;
            case "Needle":
                value = "needle";
                return value;
            case "SPI":
                value = "spi";
                return value;
            case "Top Tension":
                value = "top_tension";
                return value;
            case "Top Thread":
                value = "top_thread";
                return value;
            case "Top Thread Color":
                value = "top_thread_color";
                return value;
            case "Towa":
                value = "towa";
                return value;
        }
        return item;
    }

    private void addItemToSelectedItem(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Item");
        builder.setMessage("Add another item to the current dropdown menu.");
        LayoutInflater layoutInflater = getLayoutInflater();
        View dialogLayout = layoutInflater.inflate(R.layout.layout_edit, null);
        builder.setView(dialogLayout);
        EditText editFields = dialogLayout.findViewById(R.id.edit_field);
        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String field = editFields.getText().toString();
                if (field.equals("")){
                    Toast.makeText(mContext, "Please fill out the field", Toast.LENGTH_SHORT).show();
                }else{
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dropdowns").child(firebaseAuth.getCurrentUser().getUid());
                    String key = databaseReference.push().getKey();
                    String adjustedItem = getPath(item);
                    databaseReference.child(adjustedItem).child(key).child("value").setValue(field);
                }

            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(true);
        builder.create().show();
    }

    private void updateRecyclerView(String item){
        Log.d(TAG, "updateRecyclerView:  item : " + item);
        ArrayList<String> values = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dropdowns").child(firebaseAuth.getCurrentUser().getUid()).child(item);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                values.clear();
                String key = "";
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    key = dataSnapshot.getKey();
                    values.add(key);
                }
                spinnerDropdownAdapter = new SpinnerDropdownAdapter(values, mContext, item);
                recyclerView.setAdapter(spinnerDropdownAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }






    /**
     * --------------------------LIFE CYCLE METHODS---------------------------------------------------
     */

    private void enableBackNavigation() {
        Intent intent = new Intent(mContext, SettingsActivity.class);
        startActivity(intent);
        undoList.clear();
//        if (size == 0){
//            Toasty.info(mContext, "You must have at least one dropdown item", Toasty.LENGTH_SHORT).show();
//        }else{
//            Intent intent = new Intent(mContext, SettingsActivity.class);
//            startActivity(intent);
//            undoList.clear();
//        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        enableBackNavigation();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("Size", size);
        outState.putStringArrayList("UndoList", undoList);
    }
}