package com.cosmicdesigns.quiltkeeper.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.cosmicdesigns.quiltkeeper.MainActivity;
import com.cosmicdesigns.quiltkeeper.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends AppCompatActivity {

    private Context mContext;
    private EditText etEmail, etPassword;
    private String mEmail, mPassword;
    String logoSource = "https://upload.wikimedia.org/wikipedia/commons/e/e6/Quilt_with_triangle_pattern.jpg";
    private ImageView eye;
    boolean isPasswordVisible = false;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = LoginActivity.this;

        logoSetup();

        eye = findViewById(R.id.eye_one);
        eye.setOnClickListener(v -> changePasswordVisibility(eye));
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(v -> {
            mEmail = etEmail.getText().toString();
            if (!mEmail.equals("")){
                mAuth.fetchSignInMethodsForEmail(mEmail).addOnCompleteListener(task -> {
                    if (Objects.requireNonNull(task.getResult().getSignInMethods()).size() == 1){
                        mAuth.sendPasswordResetEmail(mEmail).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()){
                                Toast.makeText(mContext, "Check your email for a password reset email", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(mContext, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        Toast.makeText(mContext, "That email does not exist", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                Toast.makeText(mContext, "Please enter your email into the email field and click 'Forgot Password' again.", Toast.LENGTH_LONG).show();
            }
        });
        TextView createAccount = findViewById(R.id.createAccount);
        createAccount.setOnClickListener(view -> registerUser());
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(view -> checkForNullValues());
        checkLoginStatus();
    }

    private void changePasswordVisibility(ImageView eye_one){
        Drawable visible = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_visibility_on, mContext.getTheme());
        Drawable invisible = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_visibility_off, mContext.getTheme());
        if (isPasswordVisible){
            eye_one.setImageDrawable(invisible);
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            isPasswordVisible = false;
        }else{
            eye_one.setImageDrawable(visible);
            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void logoSetup(){
        CircleImageView circleImageView = findViewById(R.id.logo);
        Glide.with(getApplicationContext()).resumeRequests();
        Glide.with(getApplicationContext()).load(logoSource).into(circleImageView);
    }

    private void checkForNullValues(){
        mEmail = etEmail.getText().toString();
        mPassword = etPassword.getText().toString();
        if(mPassword.equals("") || mEmail.equals("")){
            Toast.makeText(mContext, "Please enter your email or password", Toast.LENGTH_SHORT).show();
        }else{
            signInUser();
        }
    }

    private void signInUser(){
        mAuth.signInWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser(){
        Intent intent = new Intent(mContext, RegisterActivity.class);
        startActivity(intent);
    }

    private void checkLoginStatus(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null){
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Glide.with(getApplicationContext()).pauseRequests();
    }
}