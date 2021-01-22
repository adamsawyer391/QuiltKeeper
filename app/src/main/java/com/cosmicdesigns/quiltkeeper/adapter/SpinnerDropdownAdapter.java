package com.cosmicdesigns.quiltkeeper.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cosmicdesigns.quiltkeeper.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SpinnerDropdownAdapter extends RecyclerView.Adapter<SpinnerDropdownAdapter.ViewHolder> {

    private static final String TAG = "SpinnerDropdownAdapter";

    private ArrayList<String> choices = new ArrayList<>();
    private Context context;
    private String item;

    public SpinnerDropdownAdapter(ArrayList<String> choices, Context context, String item) {
        this.choices = choices;
        this.context = context;
        this.item = item;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;
        final String postKey = choices.get(position);
        viewHolder.getData(postKey);
        viewHolder.checkBoxes(postKey);
    }

    public String getKey(int position){
        return choices.get(position).toString();
    }

    @Override
    public int getItemCount() {
        return choices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name, defaultStatus;
        private CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.list_item);
            defaultStatus = itemView.findViewById(R.id.defaultStatus);
            checkBox = itemView.findViewById(R.id.defaultCheckBox);
        }

        public void getData(String postKey){
            Log.d(TAG, "getData: post key : " + postKey);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dropdowns").child(firebaseAuth.getCurrentUser().getUid()).child(item);
            databaseReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String value = snapshot.child("value").getValue().toString();
                    name.setText(value);
                    if (snapshot.hasChild("default")){
                        defaultStatus.setText("Default Menu Item");
                        checkBox.setChecked(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        public void checkBoxes(String postKey){
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked){
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dropdowns").child(firebaseAuth.getCurrentUser().getUid()).child(item).child(postKey);
                    databaseReference.child("default").setValue("true");
                    defaultStatus.setText("Default Menu Item");
                    checkForMultiples();
                }else{
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dropdowns").child(firebaseAuth.getCurrentUser().getUid()).child(item).child(postKey);
                    databaseReference.child("default").removeValue();
                    defaultStatus.setText("");
                    checkForMultiples();
                }
            });
        }

        public void checkForMultiples(){
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dropdowns").child(firebaseAuth.getCurrentUser().getUid()).child(item);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<Boolean> arrayList = new ArrayList<>();
                    arrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String key = dataSnapshot.getKey();
                        Log.d(TAG, "onDataChange: key : " + key);
                        if (snapshot.child(key).hasChild("default")){
                            boolean check = true;
                            arrayList.add(check);
                        }
                    }
                    if (arrayList.size() > 1){
                        Toast.makeText(context, "Please uncheck another box before changing the default", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
