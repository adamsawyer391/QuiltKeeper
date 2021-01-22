package com.cosmicdesigns.quiltkeeper.splash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.cosmicdesigns.quiltkeeper.MainActivity;
import com.cosmicdesigns.quiltkeeper.R;
import com.cosmicdesigns.quiltkeeper.login.LoginActivity;
import com.cosmicdesigns.quiltkeeper.utils.SharedPreferencesKeys;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.CubeGrid;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import es.dmoral.toasty.Toasty;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private Context mContext;
    ProgressBar progressBar;

    Timer timer = new Timer();
    Handler handler = new Handler();
    TimerTask timerTask;
    int count = 0;

    final private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentUserID;
    final private DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
    private TextView textView, quiltNumber;
    private ImageView main_image;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = SplashActivity.this;
        quiltNumber = findViewById(R.id.quiltNumber);
        sharedPreferences = getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);

        progressBar = findViewById(R.id.spin_kit);
        Sprite cubeGrid = new CubeGrid();
        progressBar.setIndeterminateDrawable(cubeGrid);

        main_image = findViewById(R.id.main_image);

        savePhoto();
        //checkForMainLogo();
        checkAuthenticationStatus();
        updateSharedPreferences();
    }

    private void savePhoto(){
        if (sharedPreferences.contains("splash_logo")){
            Log.d(TAG, "savePhoto: user did have a splash logo. Retrieving it....");
            String logo = sharedPreferences.getString("splash_logo", "");
            Glide.with(getApplicationContext()).load(logo).into(main_image);
        }else{
            Log.d(TAG, "savePhoto: user did not have a splash logo saved. Creating one....");
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss", Locale.US);
            String filename = dateFormat.format(date);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "savePhoto: permission was granted");
                try{
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    Log.d(TAG, "savePhoto: path value : " + path);
                    File file = new File(path);
                    File directory = new File(file + "/" + "QuiltKeeper");
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.main_logo);
                    File imageFile = new File(directory + "/" + filename + ".jpg");
                    Log.d(TAG, "savePhoto: image file path + " + imageFile);
                    FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                    int quality = 100;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();

                    //save file to shared preferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("splash_logo", imageFile.toString());
                    editor.apply();

                    scanFile(mContext, Uri.fromFile(imageFile));
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else{
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }

    }

    private void scanFile(Context context, Uri fromFile){
        Log.d(TAG, "scanFile: uri + " + fromFile);
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(fromFile);
        context.sendBroadcast(scanIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0){
            savePhoto();
        }
    }

    private void checkForMainLogo(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild("splash_url")){
                    uploadMainLogo();
                }else{
                    displayMainLogo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayMainLogo(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String splash_url = Objects.requireNonNull(snapshot.child("splash_url").getValue()).toString();
                Glide.with(getApplicationContext()).load(splash_url).into(main_image);
                savePhoto();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadMainLogo(){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.main_logo);
        final StorageReference storageReference = firebaseStorage.getReference().child("users").child(currentUserID);
        final String filename = UUID.randomUUID().toString();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        UploadTask uploadTask = storageReference.child(filename).putBytes(data);
        uploadTask.addOnFailureListener(e -> {
            String message = e.getMessage();
            int duration = Toasty.LENGTH_SHORT;
            assert message != null;
            Toasty.error(getApplicationContext(), message, duration).show();
        });
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Task<Uri> downloadUrl = storageReference.child(filename).getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("splash_url", url);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child("users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).updateChildren(hashMap);
            });
        });
    }

    private void checkAuthenticationStatus(){
        Log.d(TAG, "checkAuthenticationStatus: checking auth status ");
        if (mAuth != null){
            Log.d(TAG, "checkAuthenticationStatus: mauth : " + mAuth);
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null){
                currentUserID = firebaseUser.getUid();
                addTimeStamps();
                addTimeStampsAndKeys();
                SharedPreferences mPreferences = getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE);
                String showSplash = mPreferences.getString("show_splash", "");
                Log.d(TAG, "checkAuthenticationStatus: show splash status : " + showSplash);
                if (showSplash.equals("true")){
                    loadUser();
                    loadQuiltNumber();
                }else{
                    switchToLogin();
                }
            }else{
                switchToLogin();
            }
        }else{
            switchToLogin();
        }

    }





    /**
     * --------------------------ADDING TIMESTAMPS IF NONE EXIST FOR CURRENT OBJECTS---------------------------------------
     */
    private void addTimeStamps(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user_quilts").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String key = dataSnapshot.getKey();
                    assert key != null;
                    Log.d(TAG, "onDataChange: key value : " + key);
                    String finishDate = Objects.requireNonNull(snapshot.child(key).child("mFinishDate").getValue()).toString();
                    Log.d(TAG, "onDataChange: finish date : " + finishDate);
                    try {
                        long timestamp = getTimeStampFromDate(finishDate);
                        Log.d(TAG, "onDataChange: time stamp : " + timestamp);
                        databaseReference.child(key).child("mTimeStamp").setValue(String.valueOf(timestamp));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addTimeStampsAndKeys(){
        Log.d(TAG, "addTimeStampsAndKeys: method called....");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("investments").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "addTimeStampsAndKeys: onDataChange: snapshot: " + snapshot);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String key = dataSnapshot.getKey();
                    Log.d(TAG, "onDataChange: key : " + key);
                    assert key != null;
                    String finishDate = Objects.requireNonNull(snapshot.child(key).child("date_selected").getValue()).toString();
                    try{
                        long timestamp = getTimeStampFromDate(finishDate);
                        databaseReference.child(key).child("timestamp").setValue(String.valueOf(timestamp));
                        databaseReference.child(key).child("investmentKey").setValue(key);
                    }catch (ParseException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private long getTimeStampFromDate(String finishDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        Log.d(TAG, "getTimeStampFromDate: simple date format : " + simpleDateFormat);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = simpleDateFormat.parse(finishDate);
        assert date != null;
        return date.getTime();
    }





    /**
     * --------------------------LOAD USER DATA---------------------------------------
     */

    private void loadUser(){
        mReference.child("users").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = Objects.requireNonNull(snapshot.child("first_name").getValue()).toString();
                textView = findViewById(R.id.user_name);
                StringBuilder stringBuilder = new StringBuilder().append("Welcome Back, ").append(firstName).append("!");
                textView.setText(stringBuilder);
                playTextViewAnimation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadQuiltNumber(){
        mReference.child("user_quilts").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                quiltNumber.setText(String.valueOf(count));
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    quiltNumber.setVisibility(View.VISIBLE);
                    playNumberAnimation();
                }, 2000);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }






    private void updateSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferencesKeys.shouldCheckDeleteInvestment, true);
        editor.apply();
    }



    /**
     * --------------------------ANIMATION AND TIMER---------------------------------------
     */

    private void playTextViewAnimation(){
        YoYo.with(Techniques.SlideInLeft)
                .duration(500)
                .playOn(textView);
    }

    private void playNumberAnimation(){
        YoYo.with(Techniques.BounceInDown)
                .duration(500)
                .playOn(quiltNumber);
    }

    private void startTimer(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    count++;
                    Log.d(TAG, "run: count : " + count);
                    if (count >= 8){
                        count = 0;
                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        };
        timer.schedule(timerTask, 1, 1000);
    }






    /**
     * --------------------------NAVIGATION AND LIFECYCLE SUPPORT METHODS---------------------------------------
     */

    private void switchToLogin(){
        startActivity(new Intent(mContext, LoginActivity.class));
        Animatoo.animateDiagonal(mContext);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        Glide.with(getApplicationContext()).pauseRequests();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        Glide.with(getApplicationContext()).pauseRequests();
    }
}