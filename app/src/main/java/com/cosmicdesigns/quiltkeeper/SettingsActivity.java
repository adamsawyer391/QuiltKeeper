package com.cosmicdesigns.quiltkeeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.cosmicdesigns.quiltkeeper.splash.ChangeSplashPhoto;
import com.cosmicdesigns.quiltkeeper.utils.SharedPreferencesKeys;
import com.cosmicdesigns.quiltkeeper.views.SpinnerModificationActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private Context mContext;
    private SwitchCompat switchCompatDeleteQuilt, switchShowSplashPage, switchShowDeleteinvestment;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mContext = SettingsActivity.this;

        sharedPreferences = getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE);

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> enableBackNavigation());

        TextView changeSplashPhoto = findViewById(R.id.changeSplashPhoto);
        changeSplashPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ChangeSplashPhoto.class);
            startActivity(intent);
            Animatoo.animateSlideUp(this);
        });

        updateUI();
    }

    private void updateUI(){
        TextView addRemoveSpinnerItems = findViewById(R.id.addRemoveSpinnerItems);
        addRemoveSpinnerItems.setOnClickListener(v -> openSpinnerModificationActivity());

        switchCompatDeleteQuilt = findViewById(R.id.switch_delete_quilt_check);
        int color = R.color.purple_200;
        switchCompatDeleteQuilt.setThumbTintList(ColorStateList.valueOf(getResources().getColor(color)));

        switchShowSplashPage = findViewById(R.id.switch_show_splash_page);
        switchShowSplashPage.setThumbTintList(ColorStateList.valueOf(getResources().getColor(color)));

        switchShowDeleteinvestment = findViewById(R.id.switch_delete_investment_check);
        switchShowDeleteinvestment.setThumbTintList(ColorStateList.valueOf(getResources().getColor(color)));

        checkDeleteSwitch();
        checkSplashSwitch();
        checkDeleteInvestmentSwitch();
    }

    private void checkDeleteSwitch(){
        TextView textView = findViewById(R.id.checkDeleteView);
        boolean shouldCheck = sharedPreferences.getBoolean("check_delete_quilt", false);
        Log.d(TAG, "checkDeleteSwitch: should check shared preferences value : " + shouldCheck);
        switchCompatDeleteQuilt.setTextOn("Check Before Deleting");
        switchCompatDeleteQuilt.setTextOff("Don't Check");
        switchCompatDeleteQuilt.setChecked(shouldCheck);
        String on = switchCompatDeleteQuilt.getTextOn().toString();
        String off = switchCompatDeleteQuilt.getTextOff().toString();
        if (shouldCheck){
            textView.setText(on);
        }else{
            textView.setText(off);
        }
        switchCompatDeleteQuilt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "onCheckedChanged: is checked : " + isChecked);
            if (isChecked){
                updateDeleteQuiltSharedPreferences(true);
                textView.setText(on);
            }else{
                updateDeleteQuiltSharedPreferences(false);
                textView.setText(off);
            }
        });
    }

    private void checkDeleteInvestmentSwitch(){
        TextView textView = findViewById(R.id.checkDeleteInvestment);
        boolean shouldCheck = sharedPreferences.getBoolean(SharedPreferencesKeys.shouldCheckDeleteInvestment, false);
        Log.d(TAG, "checkDeleteInvestmentSwitch: should check value : " + shouldCheck);
        switchShowDeleteinvestment.setTextOn("Check Before Deleting");
        switchShowDeleteinvestment.setTextOff("Don't Check");
        switchShowDeleteinvestment.setChecked(shouldCheck);
        String on = switchShowDeleteinvestment.getTextOn().toString();
        String off = switchShowDeleteinvestment.getTextOff().toString();
        if (shouldCheck){
            textView.setText(on);
        }else{
            textView.setText(off);
        }
        switchShowDeleteinvestment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: is checked : " + isChecked);
                if (isChecked){
                    updateSharedPreferences(true);
                    textView.setText(on);;
                }else{
                    updateSharedPreferences(false);
                    textView.setText(off);
                }
            }
        });
    }

    private void checkSplashSwitch(){
        TextView textView = findViewById(R.id.checkSplashView);
        String shouldCheck = sharedPreferences.getString("show_splash", "");
        switchShowSplashPage.setTextOn("Show Splash on Startup");
        switchShowSplashPage.setTextOff("Don't show splash page");
        switchShowSplashPage.setChecked(shouldCheck.equals("true"));
        String on = switchShowSplashPage.getTextOn().toString();
        String off = switchShowSplashPage.getTextOff().toString();
        if (shouldCheck.equals("true")){
            textView.setText(on);
        }else{
            textView.setText(off);
        }
        switchShowSplashPage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                updateSplashSharedPreferences("true");
                textView.setText(on);
            }else{
                updateSplashSharedPreferences("false");
                textView.setText(off);
            }
        });
    }

    private void updateSplashSharedPreferences(String isChecked){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("show_splash", isChecked);
        editor.apply();
        String status = sharedPreferences.getString("show_splash", "");
        Log.d(TAG, "updateSplashSharedPreferences: status : " + status);
    }

    private void updateDeleteQuiltSharedPreferences(boolean isChecked){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("check_delete_quilt", isChecked);
        editor.apply();
        boolean status = sharedPreferences.getBoolean("check_delete_quilt", false);
        Log.d(TAG, "updateDeleteQuiltSharedPreferences: status : " + status);
    }

    private void updateSharedPreferences(boolean isChecked){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferencesKeys.shouldCheckDeleteInvestment, isChecked);
        editor.apply();
        boolean status = sharedPreferences.getBoolean(SharedPreferencesKeys.shouldCheckDeleteInvestment, false);
        Log.d(TAG, "updateSharedPreferences: status : " + status);
    }

    private void openSpinnerModificationActivity() {
        Intent intent = new Intent(mContext, SpinnerModificationActivity.class);
        startActivity(intent);
    }

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