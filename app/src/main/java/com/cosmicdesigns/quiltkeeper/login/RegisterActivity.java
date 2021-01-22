package com.cosmicdesigns.quiltkeeper.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.cosmicdesigns.quiltkeeper.MainActivity;
import com.cosmicdesigns.quiltkeeper.R;
import com.cosmicdesigns.quiltkeeper.utils.SharedPreferencesKeys;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Context mContext;
    private EditText etEmail, etPassword, etPasswordConfirm, etFirstName, etLastName, etSecurityKey;
    ImageView eye_one, eye_two, eye_three;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentUserID;
    private final DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();

    boolean isPasswordVisible = false;
    boolean isPasswordVisibleTwo = false;
    boolean isPasswordVisibleThree = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext = RegisterActivity.this;

        deployWidgets();
    }

    private void deployWidgets(){
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        etSecurityKey = findViewById(R.id.etSecurityKey);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(view -> checkValues());

        eye_one = findViewById(R.id.eye_one);
        eye_two = findViewById(R.id.eye_two);
        eye_three = findViewById(R.id.eye_three);

        eye_one.setOnClickListener(v -> {
            Drawable visible = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_visibility_on, mContext.getTheme());
            Drawable invisible = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_visibility_off, mContext.getTheme());
            if (isPasswordVisible){
                eye_one.setImageDrawable(invisible);
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isPasswordVisible = false;
                Log.d(TAG, "deployWidgets: isPasswordVisibile : " + isPasswordVisible);
            }else{
                eye_one.setImageDrawable(visible);
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isPasswordVisible = true;
                Log.d(TAG, "deployWidgets: isPasswordVisibile : " + isPasswordVisible);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        eye_two.setOnClickListener(v -> {
            Drawable visible = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_visibility_on, mContext.getTheme());
            Drawable invisible = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_visibility_off, mContext.getTheme());
            if (isPasswordVisibleTwo){
                eye_two.setImageDrawable(invisible);
                etPasswordConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isPasswordVisibleTwo = false;
                Log.d(TAG, "deployWidgets: isPasswordVisibile : " + isPasswordVisibleTwo);
            }else{
                eye_two.setImageDrawable(visible);
                etPasswordConfirm.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isPasswordVisibleTwo = true;
                Log.d(TAG, "deployWidgets: isPasswordVisibile : " + isPasswordVisibleTwo);
            }
            etPasswordConfirm.setSelection(etPasswordConfirm.getText().length());
        });

        eye_three.setOnClickListener(v -> {
            Drawable visible = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_visibility_on, mContext.getTheme());
            Drawable invisible = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_visibility_off, mContext.getTheme());
            if (isPasswordVisibleThree){
                eye_three.setImageDrawable(invisible);
                etSecurityKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isPasswordVisibleThree = false;
                Log.d(TAG, "deployWidgets: isPasswordVisibile : " + isPasswordVisibleThree);
            }else{
                eye_three.setImageDrawable(visible);
                etSecurityKey.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                isPasswordVisibleThree = true;
                Log.d(TAG, "deployWidgets: isPasswordVisibile : " + isPasswordVisibleThree);
            }
        });
    }

    private void checkValues(){
        String mEmail = etEmail.getText().toString();
        String mPassword = etPassword.getText().toString();
        String mPasswordConfirm = etPasswordConfirm.getText().toString();
        String mSecurityKey = etSecurityKey.getText().toString();
        String mFirstName = etFirstName.getText().toString();
        String mLastName = etLastName.getText().toString();
        if (mEmail.equals("") || mPassword.equals("") || mPasswordConfirm.equals("") || mSecurityKey.equals("") || mFirstName.equals("") || mLastName.equals("")){
            Toast.makeText(mContext, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        }else if (!mPasswordConfirm.equals(mPassword)){
            Toast.makeText(mContext, "Your passwords must match", Toast.LENGTH_SHORT).show();
        }else{
            checkIfEmailIsInUse(mEmail, mPassword, mFirstName, mLastName);
        }
    }

    private void checkIfEmailIsInUse(String email, String password, String firstName, String lastName){
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if(Objects.requireNonNull(task.getResult().getSignInMethods()).size() == 1){
                Toast.makeText(mContext, "That email is already in use.", Toast.LENGTH_SHORT).show();
            }else{
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task1 -> {
                    if (task.isSuccessful()){
                        initializeSharedPreferences();
                        registerUser(firstName, lastName);
                    }else{
                        String errorMessage = task.getResult().toString();
                        int duration = Toast.LENGTH_SHORT;
                        Toast.makeText(mContext, errorMessage, duration).show();
                    }
                });
            }
        });
    }

    private void initializeSharedPreferences(){
        SharedPreferences mPreferences = getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE);
        SharedPreferencesKeys sharedPreferencesKeys = new SharedPreferencesKeys();
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean("check_delete_quilt", true);
        editor.putString("show_splash", "true");
        editor.putBoolean(SharedPreferencesKeys.shouldCheckDeleteInvestment, true);
        editor.apply();
    }

    private void registerUser(String firstName, String lastName){
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        String tokenKey = mReference.push().getKey();
        assert tokenKey != null;
        mReference.child("tokens").child(currentUserID).child(tokenKey);
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(s -> {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("token", s);
            mReference.child("tokens").child(currentUserID).child(tokenKey).setValue(hashMap);
        });
        FirebaseMessaging.getInstance().subscribeToTopic("updates");
        createDropDownResources();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("user_id", currentUserID);
        hashMap.put("first_name", firstName);
        hashMap.put("last_name", lastName);
        mReference.child("users").child(currentUserID).setValue(hashMap).addOnCompleteListener(task -> {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void createDropDownResources(){
        String[] dropdown = getResources().getStringArray(R.array.master_list);
        String item;
        for (String s : dropdown) {
            item = s;
            insertValuesToFirebase(item);
        }
    }

    /**
     * Determine a way to set default values for each node. They can be hard coded since this is only an
     * initilization task and I can find a way to modify them later, probably in Spinner ModificationActivity
     */
    private void insertValuesToFirebase(String item){
        String adjustedFirebaseNodePath = getNodePath(item);
        ArrayList<String> values = getArrayListMembers(item);
        Log.d(TAG, "insertValuesToFirebase: adjusted firebase node path : " + adjustedFirebaseNodePath);
        Log.d(TAG, "insertValuesToFirebase: array list values : " + values);
        assert values != null;
        if (values.size() > 0){
            for (int i = 0; i < values.size(); i ++){
                String path = mReference.push().getKey();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("value", values.get(i));
                assert path != null;
                mReference.child("dropdowns").child(currentUserID).child(adjustedFirebaseNodePath).child(path).setValue(hashMap);
            }
        }
    }

    private String getNodePath(String item){
        if (item.equals("Made By")){
            return "made_by";
        }
        if (item.equals("Machine")){
            return "machine";
        }
        if (item.equals("Frame")){
            return "frame";
        }
        if (item.equals("Design")){
            return "design";
        }
        if (item.equals("Batting")){
            return "batting";
        }
        if (item.equals("Top Thread")){
            return "top_thread";
        }
        if (item.equals("Top Thread Color")){
            return "top_thread_color";
        }
        if (item.equals("Bobbin")){
            return "bobbin";
        }
        if (item.equals("Bobbin Color")){
            return "bobbin_color";
        }
        if (item.equals("Needle")){
            return "needle";
        }
        if (item.equals("SPI")){
            return "spi";
        }
        if (item.equals("Towa")){
            return "towa";
        }
        if (item.equals("Top Tension")){
            return "top_tension";
        }
        return item;
    }

    private ArrayList<String> getArrayListMembers(String item){
        ArrayList<String> arrayList = new ArrayList<>();
        String[] arrayMembers;
        if (item.equals("Made By")){
            arrayMembers = getResources().getStringArray(R.array.made_by_spinner);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        if (item.equals("Machine")){
            arrayMembers = getResources().getStringArray(R.array.machine_spinner);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        if (item.equals("Frame")){
            arrayMembers = getResources().getStringArray(R.array.frame_spinner);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        if (item.equals("Design")){
            arrayMembers = getResources().getStringArray(R.array.design_spinner);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        if (item.equals("Batting")){
            arrayMembers = getResources().getStringArray(R.array.batting_array);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        if (item.equals("Top Thread")){
            arrayMembers = getResources().getStringArray(R.array.top_thread_array);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        if (item.equals("Top Thread Color")){
            arrayMembers = getResources().getStringArray(R.array.top_thread_color_array);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        if (item.equals("Bobbin")){
            arrayMembers = getResources().getStringArray(R.array.bobbin_array);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        if (item.equals("Bobbin Color")){
            arrayMembers = getResources().getStringArray(R.array.bobbin_color_array);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        if (item.equals("Needle")){
            arrayMembers = getResources().getStringArray(R.array.needle_array);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        if (item.equals("SPI")){
            arrayMembers = getResources().getStringArray(R.array.spi_array);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        if (item.equals("Towa")){
            arrayMembers = getResources().getStringArray(R.array.towa_array);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        if (item.equals("Top Tension")){
            arrayMembers = getResources().getStringArray(R.array.top_tension_array);
            arrayList.addAll(Arrays.asList(arrayMembers));
            return arrayList;
        }
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Glide.with(getApplicationContext()).pauseRequests();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(mContext, LoginActivity.class);
        startActivity(intent);
    }
}