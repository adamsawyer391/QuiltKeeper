package com.cosmicdesigns.quiltkeeper.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.cosmicdesigns.quiltkeeper.MainActivity;
import com.cosmicdesigns.quiltkeeper.R;
import com.cosmicdesigns.quiltkeeper.adapter.InvestmentAdapter;
import com.cosmicdesigns.quiltkeeper.interfaces.InvestmentLoaderListener;
import com.cosmicdesigns.quiltkeeper.interfaces.TotalsLoadedListener;
import com.cosmicdesigns.quiltkeeper.model.InvestmentModel;
import com.cosmicdesigns.quiltkeeper.recycleviewhelper.RecycleItemClickListener;
import com.cosmicdesigns.quiltkeeper.utils.SharedPreferencesKeys;
import com.cosmicdesigns.quiltkeeper.viewmodel.InvestmentViewModel;
import com.cosmicdesigns.quiltkeeper.viewmodel.TotalViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

public class InvestmentActivity extends AppCompatActivity implements InvestmentLoaderListener, TotalsLoadedListener {

    private static final String TAG = "InvestmentActivity";

    private Context mContext;
    private TextView investmentNumber, costNumber, payoffNumber;
    private RecyclerView recyclerView;
    InvestmentViewModel investmentViewModel;
    InvestmentAdapter mAdapter;
    TotalViewModel totalViewModel;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment);
        mContext = InvestmentActivity.this;

        mPreferences = getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE);

        totalViewModel = new ViewModelProvider(this).get(TotalViewModel.class);
        totalViewModel.init(InvestmentActivity.this);

        deployUI();
        prepareRecyclerView();
    }

    private void deployUI(){
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> enableBackNavigation());
        ImageView addInvestment = findViewById(R.id.addInvestment);
        addInvestment.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, AddInvestmentActivity.class);
            startActivity(intent);
        });
        investmentNumber = findViewById(R.id.investmentNumber);
        costNumber = findViewById(R.id.costNumber);
        payoffNumber = findViewById(R.id.payoffNumber);
        recyclerView = findViewById(R.id.recycler_view);
    }







    /**
     * -----------------------------RECYCLER VIEW SUPPORT METHODS--------------------------------------
     */

    private void prepareRecyclerView(){
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        investmentViewModel = new ViewModelProvider(this).get(InvestmentViewModel.class);
        investmentViewModel.init(InvestmentActivity.this);
        mAdapter = new InvestmentAdapter(mContext, investmentViewModel.getInvestments().getValue());
        recyclerView.setAdapter(mAdapter);

        recyclerTouchListener();
        recyclerTouchHelper();
    }

    private void recyclerTouchListener(){
        recyclerView.addOnItemTouchListener(new RecycleItemClickListener(this, new RecycleItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, "onItemClick: item position : " + position);
                Intent intent = new Intent(mContext, InvestmentViewActivity.class);
                intent.putExtra("InvestmentKey", position);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                Animatoo.animateSlideRight(mContext);
            }
        }));
    }

    private void recyclerTouchHelper(){
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                String key = investmentViewModel.getInvestments().getValue().get(viewHolder.getAdapterPosition()).getInvestmentKey();
                Log.d(TAG, "onSwiped: key : " + key);
                int position = viewHolder.getAdapterPosition();
                boolean shouldCheck = mPreferences.getBoolean(SharedPreferencesKeys.shouldCheckDeleteInvestment, false);
                Log.d(TAG, "onSwiped: should check : " + shouldCheck);
                if (shouldCheck){
                    confirmDeleteDialog(key, position);
                }else{
                    deleteInvestmentEntry(key, position);
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void deleteInvestmentEntry(String key, int position){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("investments").child(firebaseAuth.getCurrentUser().getUid());
        databaseReference.child(key).removeValue();
        mAdapter.notifyItemRemoved(position);
        onCostsTotalsLoaded();
        onInvestmentsTotalsLoaded();
    }

    private void confirmDeleteDialog(String key, int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        View dialogLayout = layoutInflater.inflate(R.layout.layout_delete_dialog, null);
        CheckBox checkBox = dialogLayout.findViewById(R.id.checkbox);
        builder.setTitle("Delete Investment");
        builder.setMessage("This action will permanently delete this investment item. Are you sure you wish to proceed?");
        builder.setView(dialogLayout);
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checkBox.isChecked()){
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putBoolean(SharedPreferencesKeys.shouldCheckDeleteInvestment, false);
                    editor.apply();
                }
                deleteInvestmentEntry(key, position);
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






    /**
     * ------------------------------CALCULATION METHODS--------------------------------------------
     */

    private void produceInvestmentSum(ArrayList<Float> arrayList){
        double sum = 0;
        for (int i = 0; i < arrayList.size(); i++){
            sum += arrayList.get(i);
        }
        String total = String.format(Locale.US, "%.2f", sum);
        StringBuilder stringBuilder = new StringBuilder().append("$").append(total);
        investmentNumber.setText(stringBuilder);
    }

    private void produceCostSum(ArrayList<Float> arrayList){
        double sum = 0;
        for (int i = 0; i < arrayList.size(); i++){
            sum += arrayList.get(i);
        }
        String total = String.format(Locale.US, "%.2f", sum);
        StringBuilder stringBuilder = new StringBuilder().append("$").append(total);
        costNumber.setText(stringBuilder);
        Handler handler = new Handler();
        handler.postDelayed(this::determinePayoff, 500);

    }

    private void determinePayoff(){
        String investment = investmentNumber.getText().toString();
        String cost = costNumber.getText().toString();
        String[] splitInvestment = investment.split("\\$");
        String adjustedInvestment = splitInvestment[1];
        String[] splitCost = cost.split("\\$");
        String adjustedCost = splitCost[1];
        float investmentInt = Float.parseFloat(adjustedInvestment);
        float costInt = Float.parseFloat(adjustedCost);
        float payoff = costInt - investmentInt;
        String total = String.format(Locale.US, "%.2f", payoff);
        StringBuilder stringBuilder = new StringBuilder().append("$").append(total);
        if (payoff <= 0){
            payoffNumber.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        }else{
            payoffNumber.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        }
        payoffNumber.setText(stringBuilder);
    }






    /**
     * ----------------------------interface listeners-----------------------------------------------
     */

    @Override
    public void onInvestmentsLoaded() {
        investmentViewModel.getInvestments().observe(this, new Observer<ArrayList<InvestmentModel>>() {
            @Override
            public void onChanged(ArrayList<InvestmentModel> investmentModels) {
                mAdapter.notifyDataSetChanged();
                int lastPosition = investmentModels.size();
            }
        });
    }

    @Override
    public void onCostsTotalsLoaded() {
        totalViewModel.getCosts().observe(this, arrayList -> {
            Log.d(TAG, "onCostsTotalsLoaded: onChanged: array list : " + arrayList);
            produceCostSum(arrayList);
        });
    }

    @Override
    public void onInvestmentsTotalsLoaded() {
        totalViewModel.getInvestments().observe(this, arrayList -> {
            Log.d(TAG, "onInvestmentsTotalsLoaded: onChanged: array list : " + arrayList);
            produceInvestmentSum(arrayList);
        });
    }






    /**
     * -------------------------------LIFE CYCLE METHODS-------------------------------------------------
     */

    private void enableBackNavigation(){
        Intent intent = new Intent(mContext, MainActivity.class);
        startActivity(intent);
        Animatoo.animateSlideRight(mContext);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        enableBackNavigation();
    }
}