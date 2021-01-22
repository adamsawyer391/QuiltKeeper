package com.cosmicdesigns.quiltkeeper.views;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.cosmicdesigns.quiltkeeper.R;
import com.cosmicdesigns.quiltkeeper.model.InvestmentModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class AddInvestmentActivity extends AppCompatActivity {

    private static final String TAG = "AddInvestmentActivity";

    private Context mContext;
    private TextView date_selected;
    private EditText investment_description, investment_cost;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    private final DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_investment);
        mContext = AddInvestmentActivity.this;

        deployUI();
    }

    private void deployUI(){
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> enableBackNavigation());
        ImageView date_picker_actions = findViewById(R.id.date_picker_actions);
        date_picker_actions.setOnClickListener(v -> selectDate());
        date_selected = findViewById(R.id.date_selected);
        investment_description = findViewById(R.id.investment_description);
        investment_cost = findViewById(R.id.investment_cost);
        Button save_button = findViewById(R.id.save_button);
        save_button.setOnClickListener(v -> {
            try {
                saveInvestment();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    private void selectDate(){
        Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy", Locale.US);
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddInvestmentActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            String mSelectedDate = dateFormatter.format(newDate.getTime());
            date_selected.setText(mSelectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveInvestment()throws ParseException{
        String mDate, mDescription, mCost, mTimestamp;
        mDate = date_selected.getText().toString();
        mDescription = investment_description.getText().toString();
        mCost = investment_cost.getText().toString();
        long timestamp = getTimeStampFromDate(mDate);
        mTimestamp = String.valueOf(timestamp);
        if (TextUtils.isEmpty(mDate) || TextUtils.isEmpty(mDescription) || TextUtils.isEmpty(mCost)){
            Toast.makeText(mContext, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        }else{
            String key = mReference.push().getKey();
            InvestmentModel investmentModel = new InvestmentModel(mDescription, mCost, mDate, key, mTimestamp);
            assert key != null;
            mReference.child("investments").child(currentUserID).child(key).setValue(investmentModel).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Intent intent = new Intent(mContext, InvestmentActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private long getTimeStampFromDate(String finishDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        Log.d(TAG, "getTimeStampFromDate: simple date format : " + simpleDateFormat);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = simpleDateFormat.parse(finishDate);
        assert date != null;
        return date.getTime();
    }

    private void enableBackNavigation() {
        Intent intent = new Intent(mContext, InvestmentActivity.class);
        startActivity(intent);
    }
}