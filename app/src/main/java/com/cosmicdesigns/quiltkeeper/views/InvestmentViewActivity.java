package com.cosmicdesigns.quiltkeeper.views;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.cosmicdesigns.quiltkeeper.R;
import com.cosmicdesigns.quiltkeeper.interfaces.InvestmentLoaderListener;
import com.cosmicdesigns.quiltkeeper.model.InvestmentModel;
import com.cosmicdesigns.quiltkeeper.viewmodel.InvestmentViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import es.dmoral.toasty.Toasty;

public class InvestmentViewActivity extends AppCompatActivity implements InvestmentLoaderListener {

    private static final String TAG = "InvestmentViewActivity";

    private Context context;
    private int investmentPosition;
    private ImageView backArrow;
    private TextView description, cost, date;
    private InvestmentViewModel investmentViewModel;
    private InvestmentModel investmentModel = new InvestmentModel();
    private Button button;
    private String key;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment_view);
        context = InvestmentViewActivity.this;

        getIntentExtra();
        updateUI();
        observeLiveData();
    }


    /**
     * ----------------------------UI METHODS-------------------------------------------------------
     */

    private void getIntentExtra(){
        Intent intent = getIntent();
        investmentPosition = intent.getIntExtra("InvestmentKey", 0);
        Log.d(TAG, "getIntentExtra: investment position : " + investmentPosition);
    }

    private void updateUI(){
        backArrow = findViewById(R.id.backArrow);
        description = findViewById(R.id.description);
        cost = findViewById(R.id.cost);
        date = findViewById(R.id.date);
        button = findViewById(R.id.button);

        backArrow.setOnClickListener(v -> enableBackNavigation());
        description.setOnClickListener(v -> {
            inflateAlertDialog("description");
        });
        cost.setOnClickListener(v -> {
            inflateAlertDialog("cost");
        });
        date.setOnClickListener(v -> {
            datePicker();
        });
        button.setOnClickListener(v -> {
            updateInvestmentItem();
        });
    }









    /**
     * ----------------------------ALERT DIALOG AND SAVE SUPPORT METHODS----------------------------------
     */

    private void inflateAlertDialog(String input){
        String title = getADTitle(input);
        String message = getADMessage(input);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        LayoutInflater layoutInflater = getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.layout_edit_dialog, null);
        builder.setView(dialogView);
        EditText editFields = dialogView.findViewById(R.id.edit_field);
        editFields.setVisibility(View.VISIBLE);
        if (input.equals("description")){
            editFields.setInputType(InputType.TYPE_CLASS_TEXT);
            editFields.setText(description.getText().toString());
        }else if (input.equals("cost")){
            editFields.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
//            String[] costSplit = editFields.getText().toString().split("\\$");
//            Log.d(TAG, "updateInvestmentItem: cost split : " + costSplit[0]);
//            Log.d(TAG, "updateInvestmentItem: cost split : " + costSplit[1]);
//            String updateCost = costSplit[1];
//            Log.d(TAG, "updateInvestmentItem: update cost : " + updateCost);
            editFields.setText(editFields.getText().toString());
        }
        builder.setCancelable(true);
        builder.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.equals("description")){
                    description.setText(editFields.getText().toString());
                }else if (input.equals("cost")){
                    cost.setText(editFields.getText().toString());
                }
                updateInvestmentItem();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private String getADTitle(String input){
        switch (input){
            case "description":
                input = "Investment Description";
                return input;
            case "cost":
                input = "Investment Cost";
                return input;
            case "date":
                input = "Investment Date";
                return input;
        }
        return "";
    }

    private String getADMessage(String message){
        switch (message){
            case "description":
                message = "Change the description of this investment item";
                return message;
            case "cost":
                message = "Change the cost of this investment item";
                return message;
        }
        return "";

    }

    private void updateInvestmentItem(){
        String descriptionText = description.getText().toString();
        String costText = cost.getText().toString();
        String dateText = date.getText().toString();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("investment_description", descriptionText);
        hashMap.put("investment_cost", costText);
        hashMap.put("date_selected", dateText);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("investments").child(firebaseAuth.getCurrentUser().getUid()).child(key);
        databaseReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toasty.success(getApplicationContext(), "Investment Updated!", Toasty.LENGTH_SHORT).show();
            }
        });
    }

    private void datePicker(){
        AtomicLong timestamp = new AtomicLong();
        Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy", Locale.US);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            String mSelectedDate = dateFormatter.format(newDate.getTime());
            date.setText(mSelectedDate);
            try {
                timestamp.set(getTimeStampFromDate(mSelectedDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String updateStamp = String.valueOf(timestamp);
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("date_selected", mSelectedDate);
            hashMap.put("timestamp", updateStamp);
            DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            String currentUserID = firebaseAuth.getCurrentUser().getUid();
            mReference.child("investments").child(currentUserID).child(investmentModel.getInvestmentKey()).updateChildren(hashMap)
                    .addOnCompleteListener(task -> Toasty.success(getApplicationContext(), "Investment updated!", Toasty.LENGTH_SHORT).show());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private long getTimeStampFromDate(String finishDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        Log.d(TAG, "getTimeStampFromDate: simple date format : " + simpleDateFormat);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = simpleDateFormat.parse(finishDate);
        return date.getTime();
    }









    /**
     * ------------------------------POPULATING UI--------------------------------------------------
     */

    private void observeLiveData(){
        investmentViewModel = new ViewModelProvider(this).get(InvestmentViewModel.class);
        investmentViewModel.init(context);
        investmentModel = Objects.requireNonNull(investmentViewModel.getInvestments().getValue()).get(investmentPosition);
        description.setText(investmentModel.getInvestment_description());
        cost.setText(investmentModel.getInvestment_cost());
        date.setText(investmentModel.getDate_selected());
        key = investmentViewModel.getInvestments().getValue().get(investmentPosition).getInvestmentKey();
        Log.d(TAG, "observeLiveData: position key of investment item : " + key);
    }








    /**
     * ---------------------------------VIEW MODEL OBSERVE LISTENER-----------------------------------------------
     */


    @Override
    public void onInvestmentsLoaded() {
        investmentViewModel.getInvestments().observe(this, new Observer<ArrayList<InvestmentModel>>() {
            @Override
            public void onChanged(ArrayList<InvestmentModel> investmentModels) {

            }
        });
    }










    /**
     * ------------------------------LIFECYCLE METHODS-------------------------------------------
     */

    private void enableBackNavigation(){
        Intent intent = new Intent(context, InvestmentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        enableBackNavigation();
    }


}
