package com.cosmicdesigns.quiltkeeper.views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.cosmicdesigns.quiltkeeper.MainActivity;
import com.cosmicdesigns.quiltkeeper.R;
import com.cosmicdesigns.quiltkeeper.model.Quilt;
import com.cosmicdesigns.quiltkeeper.utils.SharedPreferencesKeys;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import es.dmoral.toasty.Toasty;

public class NewQuiltActivity extends AppCompatActivity {

    private static final String TAG = "NewQuiltActivity";

    public static final int GALLERY_REQUEST = 1;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    private final DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    private SharedPreferences sharedPreferences;
    boolean shouldCheck = true;

    private Context mContext;
    private ImageView quiltPhoto;
    private ImageView addPhoto;
    private ImageView changePhoto;
    private TextInputLayout tilQuiltName;
    private TextView tvAddPhoto, tvChangePhoto;
    private Bitmap bmRotated;
    private EditText quiltName, finishDate, cost, quiltOwner, length, width, additionalNotes, stictches;
    String mQuiltName, mFinishDate, mWidth, mLength, mCost, mQuiltOwner, mStiches, mHours, mMinutes, mSeconds, mMadeBy,
        mMachine, mFrame, mDesign, mBatting, mTopThread, mTopThreadColor, mBobbin, mBobbinColor, mNeedle, mSPI, mTowa, mTopTension, mNotes, mTimeStamp;
    String[] stringArray;
    private EditText hours, minutes, seconds;
    Spinner madeBySpinner, machineSpinner, frameSpinner, designSpinner, battingSpinner, topThreadSpinner, topThreadColorSpinner,
    bobbinSpinner, bobbinColorSpinner, needleSpinner, spiSpinner, towaSpinner, topTensionSpinner;
    ProgressBar progressBar;
    TextView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_quilt);
        mContext = NewQuiltActivity.this;
        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        stringArray = getResources().getStringArray(R.array.string_values);
        progressBar = findViewById(R.id.progressBar);
        progress = findViewById(R.id.progress);

        deployWidgets();
        populateSpinners();
    }

    /**
     * -----------------------------------WIDGET DEPLOYMENT------------------------------------
     */
    private void deployWidgets(){
        buttons();
        spinnerObjects();
        textInputLayouts();
        editTextValues();
    }

    private void buttons(){
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> enableBackNavigation());
        quiltPhoto = findViewById(R.id.quiltPhoto);
        addPhoto = findViewById(R.id.ivAddPhoto);
        changePhoto = findViewById(R.id.ivChangePhoto);
        tvAddPhoto = findViewById(R.id.tvAddPhoto);
        tvChangePhoto = findViewById(R.id.changePhoto);
        ImageView calculate = findViewById(R.id.lengthCalculate);
        calculate.setOnClickListener(v -> lengthCalculate());
        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(view -> {
            try {
                getValues();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        addPhoto.setOnClickListener(view -> openGallery());
        changePhoto.setOnClickListener(view -> openGallery());
        ImageView finishDatePick = findViewById(R.id.finishDatePick);
        finishDatePick.setOnClickListener(v -> datePicker());
    }

    private void spinnerObjects(){
        frameSpinner = findViewById(R.id.frameSpinner);
        machineSpinner = findViewById(R.id.machineUsedSpinner);
        madeBySpinner = findViewById(R.id.madeBySpiner);
        designSpinner = findViewById(R.id.design_spinner);
        battingSpinner = findViewById(R.id.batting_spinner);
        topThreadSpinner = findViewById(R.id.top_thread_spinner);
        topThreadColorSpinner = findViewById(R.id.top_thread_color_spinner);
        bobbinSpinner = findViewById(R.id.bobbin_spinner);
        bobbinColorSpinner = findViewById(R.id.bobbin_color_spinner);
        needleSpinner = findViewById(R.id.needle_spinner);
        spiSpinner = findViewById(R.id.spi_spinner);
        towaSpinner = findViewById(R.id.towa_spinner);
        topTensionSpinner = findViewById(R.id.top_tension_spinner);
    }


    /**
     * -----------------------------------EDIT TEXT INSTANCE AND SUPPORT METHODS---------------------------------------
     */

    private void textInputLayouts(){
        tilQuiltName = findViewById(R.id.inLayoutQuiltName);
    }

    private void editTextValues(){
        hours = findViewById(R.id.hours);
        minutes = findViewById(R.id.minutes);
        seconds = findViewById(R.id.seconds);
        finishDate = findViewById(R.id.finishDate);
        quiltName = findViewById(R.id.quiltName);
        cost = findViewById(R.id.cost);
        quiltOwner = findViewById(R.id.quiltOwner);
        length = findViewById(R.id.length);
        width = findViewById(R.id.width);
        stictches = findViewById(R.id.stiches);
        additionalNotes = findViewById(R.id.additionalNotes);

        watchWidthAndLengthInputs();
        watchStitchValueInputs();
    }

    private void watchStitchValueInputs() {
        stictches.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (!input.isEmpty()){
                    input = input.replace(",", "");
                    DecimalFormat decimalFormat = new DecimalFormat("#,###,###");
                    String s1 = decimalFormat.format(Double.parseDouble(input));
                    stictches.removeTextChangedListener(this);
                    stictches.setText(s1);
                    stictches.setSelection(s1.length());
                    stictches.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void watchWidthAndLengthInputs() {
        width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (!input.equals("")){
                    calculateCost(input, width);
                    width.removeTextChangedListener(this);
                    width.addTextChangedListener(this);
                }else{
                    calculateCost(String.valueOf(0), width);
                    width.removeTextChangedListener(this);
                    width.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        length.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (!input.equals("")){
                    calculateCost(input, length);
                    length.removeTextChangedListener(this);
                    length.addTextChangedListener(this);
                }else{
                    calculateCost(String.valueOf(0), length);
                    length.removeTextChangedListener(this);
                    length.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void calculateCost(String input, EditText editText) {
        String mLength, mWidth;
        String tag = editText.getTag().toString();
        Log.d(TAG, "calculateCost: tag : " + tag);
        if (tag.equals("length")){
            mLength = input;
            mWidth = width.getText().toString();
        }else{
            mLength = length.getText().toString();
            mWidth = input;
        }
        if (!mLength.equals("") && !mWidth.equals("")){
            double mCost = (Integer.valueOf(mLength) * Integer.valueOf(mWidth)) * .03;
            String s = String.format(Locale.US, "%.2f", mCost);
            cost.setText(s);
        }else{
            cost.setText(String.valueOf(0));
        }
    }

    /**
     * -------------------------------DATE PICKER------------------------------------------------------------------
     */

    private void datePicker(){
        Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy", Locale.US);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            String mSelectedDate = dateFormatter.format(newDate.getTime());
            finishDate.setText(mSelectedDate);
//                HashMap<String, Object> hashMap = new HashMap<>();
//                hashMap.put("mFinishDate", mSelectedDate);
//                mReference.child("user_quilts").child(currentUserID).child(quilt.getQuiltKey()).updateChildren(hashMap)
//                        .addOnCompleteListener(task -> Toast.makeText(mContext, "Quilt updated!", Toast.LENGTH_SHORT).show());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }




    /**
     * --------------------------------SPINNER INITIALIZERS-----------------------------------------
     */

    private void populateSpinners(){
        String[] list = getResources().getStringArray(R.array.master_list);
        for (String path : list) {
            String adjustedPath = getNodePath(path);
            if (adjustedPath.equals("made_by")) {
                addData(adjustedPath, madeBySpinner);
            }
            if (adjustedPath.equals("machine")) {
                addData(adjustedPath, machineSpinner);
            }
            if (adjustedPath.equals("frame")){
                addData(adjustedPath, frameSpinner);
            }
            if (adjustedPath.equals("design")){
                addData(adjustedPath, designSpinner);
            }
            if (adjustedPath.equals("batting")){
                addData(adjustedPath, battingSpinner);
            }
            if (adjustedPath.equals("top_thread")){
                addData(adjustedPath, topThreadSpinner);
            }
            if (adjustedPath.equals("top_thread_color")){
                addData(adjustedPath, topThreadColorSpinner);
            }
            if (adjustedPath.equals("bobbin")){
                addData(adjustedPath, bobbinSpinner);
            }
            if (adjustedPath.equals("bobbin_color")){
                addData(adjustedPath, bobbinColorSpinner);
            }
            if (adjustedPath.equals("needle")){
                addData(adjustedPath, needleSpinner);
            }
            if (adjustedPath.equals("spi")){
                addData(adjustedPath, spiSpinner);
            }
            if (adjustedPath.equals("towa")){
                addData(adjustedPath, towaSpinner);
            }
            if (adjustedPath.equals("top_tension")){
                addData(adjustedPath, topTensionSpinner);
            }
        }
    }

    private void addData(String path, Spinner spinner){
        Log.d(TAG, "addData: path : " + path);
        Log.d(TAG, "addData: spinner : " + spinner);
        ArrayList<String> spinnerList = new ArrayList<>();
        mReference.child("dropdowns").child(currentUserID).child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String defaultIndex = "";
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String key = dataSnapshot.getKey();
                    assert key != null;
                    String value = Objects.requireNonNull(snapshot.child(key).child("value").getValue()).toString();
                    spinnerList.add(value);
                    if (snapshot.child(key).hasChild("default")){
                        defaultIndex = Objects.requireNonNull(snapshot.child(key).child("value").getValue()).toString();
                    }
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.layout_spinner, spinnerList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(arrayAdapter);
                String madeByName;
                int madeByPosition;
                int defaultMadeByPosition = 0;
                for (int i = 0; i < spinnerList.size(); i++){
                    madeByName = spinnerList.get(i);
                    madeByPosition = i;
                    if(madeByName.equals(defaultIndex)){
                        defaultMadeByPosition = madeByPosition;
                    }
                }
                spinner.setSelection(defaultMadeByPosition);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String message = error.toString();
                Toasty.error(getApplicationContext(), message, Toasty.LENGTH_SHORT).show();
            }
        });
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






    /**
     * -------------------------------LENGTH & WIDTH CALCULATION---------------------------------------------------
     */



    private void lengthCalculate(){
        String mLength = length.getText().toString();
        String mWidth = width.getText().toString();
        if(mLength.equals("") || mWidth.equals("")){
            String message = "Please fill out length and width";
            int duration = Toasty.LENGTH_SHORT;
            Toasty.info(getApplicationContext(), message, duration).show();
        }else{
            double mCost = (Integer.valueOf(mLength) * Integer.valueOf(mWidth)) * .03;
            String s = String.format(Locale.US, "%.2f", mCost);
            cost.setText(s);
        }
    }






    /**
     * ---------------------------------ADDING A PICTURE--------------------------------------------
     */

    private void openGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
        quiltPhoto.setVisibility(View.VISIBLE);
        changePhoto.setVisibility(View.VISIBLE);
        tvChangePhoto.setVisibility(View.VISIBLE);
        addPhoto.setVisibility(View.GONE);
        tvAddPhoto.setVisibility(View.GONE);

        //move other views below image once added
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.changePhoto);
        params.setMargins(0, 10, 0, 0);
        tilQuiltName.setLayoutParams(params);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    int orientation = 0;
                    InputStream inputStream = mContext.getContentResolver().openInputStream(selectedImage);
                    if (inputStream != null) {
                        ExifInterface exif = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            exif = new ExifInterface(inputStream);
                        }
                        if (exif != null) {
                            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        }
                        inputStream.close();
                    }
                    bmRotated = rotateBitmap(bitmap, orientation);
                    quiltPhoto.setImageBitmap(bmRotated);
                    //uploadImageToFirebaseStorage(bmRotated);
                } catch (IOException e) {
                    String message = e.getMessage();
                    int duration = Toasty.LENGTH_SHORT;
                    assert message != null;
                    Toasty.error(getApplicationContext(), message, duration).show();
                }
            }
        }
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation){
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }

    }




    /**
     * ------------------------------SETTING GLOBAL STRING VALUES-----------------------------------
     */

    private void getValues() throws ParseException {
        mQuiltName = quiltName.getText().toString();
        mFinishDate = finishDate.getText().toString();
        mCost = cost.getText().toString();
        mQuiltOwner = quiltOwner.getText().toString();
        mLength = length.getText().toString();
        mWidth = width.getText().toString();
        mStiches = stictches.getText().toString();
        mHours = hours.getText().toString();
        mMinutes = minutes.getText().toString();
        mSeconds = seconds.getText().toString();
        mMadeBy = madeBySpinner.getSelectedItem().toString();
        mMachine = machineSpinner.getSelectedItem().toString();
        mFrame = frameSpinner.getSelectedItem().toString();
        mDesign = designSpinner.getSelectedItem().toString();
        mBatting = battingSpinner.getSelectedItem().toString();
        mTopThread = topThreadSpinner.getSelectedItem().toString();
        mTopThreadColor = topThreadColorSpinner.getSelectedItem().toString();
        mBobbin = bobbinSpinner.getSelectedItem().toString();
        mBobbinColor = bobbinColorSpinner.getSelectedItem().toString();
        mNeedle = needleSpinner.getSelectedItem().toString();
        mSPI = spiSpinner.getSelectedItem().toString();
        mTowa = towaSpinner.getSelectedItem().toString();
        mTopTension = topTensionSpinner.getSelectedItem().toString();
        mNotes = additionalNotes.getText().toString();
        long timestamp = getTimeStampFromDate(mFinishDate);
        String mCopied = "original";
        mTimeStamp = String.valueOf(timestamp);

        Quilt quilt = new Quilt(mQuiltName, mFinishDate, mCost, mQuiltOwner, mLength, mWidth, mStiches, mHours, mMinutes,
                mSeconds, mMadeBy, mMachine, mFrame, mDesign, mBatting, mTopThread, mTopThreadColor,
                mBobbin, mBobbinColor, mNeedle, mSPI, mTowa, mTopTension, mNotes, null, null, null, mTimeStamp, mCopied);

        checkForNulls(quilt);
    }

    private long getTimeStampFromDate(String finishDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        Log.d(TAG, "getTimeStampFromDate: simple date format : " + simpleDateFormat);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = simpleDateFormat.parse(finishDate);
        return date.getTime();
    }

    private void displayToast(String object){
        String message = "You did not fill out the ";
        String end = " field";
        int duration = Toasty.LENGTH_SHORT;
        Toasty.error(getApplicationContext(), message + object + end, duration).show();
    }

    private void checkForNulls(Quilt quilt){
        Log.d(TAG, "checkForNulls: started....");
        Log.d(TAG, "checkForNulls: quilt values :  " + quilt);
        int count = 0;
        String[] values = {mQuiltName, mFinishDate, mWidth, mLength, mCost, mQuiltOwner, mStiches, mHours, mMinutes, mSeconds};
        String[] warning = {"QUILT NAME", "DATE", "WIDTH", "LENGTH", "COST", "QUILT OWNER", "STITCHES", "HOURS", "MINUTES", "SECONDS"};
        for (int i = 0; i < values.length; i++){
            if (values[i].equals("")){
                count += 1;
                displayToast(warning[i]);
                break;
            }
        }
        if (count <= 0){
            boolean shouldCheck = sharedPreferences.getBoolean(SharedPreferencesKeys.shouldCheckPhoto, false);
            if (quiltPhoto.getVisibility() == View.GONE){
                if (shouldCheck){
                    LayoutInflater layoutInflater = getLayoutInflater();
                    View dialogLayout = layoutInflater.inflate(R.layout.layout_dialog, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setView(dialogLayout);
                    CheckBox checkBox = findViewById(R.id.rejectAskAgain);
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            enterValuesToDatabase(quilt);
                            if (checkBox.isChecked()){
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(SharedPreferencesKeys.shouldCheckPhoto, false);
                                editor.apply();
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
                }else{
                    enterValuesToDatabase(quilt);
                }
            }else{
                enterValuesToDatabase(quilt);
            }
        }
    }




    /**
     * --------------------------------FIREBASE STUFF-----------------------------------------------
     */

    private void uploadPhotoToFirebaseStorage(String key){
        if(bmRotated != null){
            final StorageReference storageReference = firebaseStorage.getReference().child("users").child(currentUserID);
            final String filename = UUID.randomUUID().toString();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bmRotated.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            UploadTask uploadTask = storageReference.child(filename).putBytes(data);
            uploadTask.addOnFailureListener(Throwable::printStackTrace)
                    .addOnSuccessListener(taskSnapshot -> storageReference.child(filename).getDownloadUrl().addOnSuccessListener(uri -> {
                        String url = uri.toString();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("url", url);
                        hashMap.put("filename", filename);
                        hashMap.put("quiltKey", key);
                        mReference.child("user_quilts").child(currentUserID).child(key).updateChildren(hashMap)
                                .addOnCompleteListener(task -> {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    progress.setVisibility(View.INVISIBLE);
                                    String message = "Quilt Saved!";
                                    int duration = Toasty.LENGTH_SHORT;
                                    Toasty.success(getApplicationContext(), message, duration).show();
                                    navigateToMainActivity();
                                });
                    }));
            uploadTask.addOnProgressListener(snapshot -> {
                int total = (int) snapshot.getTotalByteCount();
                long transferred = snapshot.getBytesTransferred();
                int progressValue = (int) (transferred / total);
                progress.setText(new StringBuilder().append(progressValue).append("%").append(" completed..."));
            });
        }else{
            String url = "";
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("url", url);
            hashMap.put("filename", "");
            hashMap.put("quiltKey", key);
            mReference.child("user_quilts").child(currentUserID).child(key).updateChildren(hashMap)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.INVISIBLE);
                        progress.setVisibility(View.INVISIBLE);
                        String message = "Quilt Saved!";
                        int duration = Toasty.LENGTH_SHORT;
                        Toasty.success(getApplicationContext(), message, duration).show();
                        navigateToMainActivity();
                    });
        }
    }

    private void enterValuesToDatabase(Quilt quilt){
        String key = mReference.push().getKey();
        assert key != null;
        mReference.child("user_quilts").child(currentUserID).child(key).setValue(quilt).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressBar.setVisibility(View.VISIBLE);
                progressBar.bringToFront();
                progress.setVisibility(View.VISIBLE);
                progress.bringToFront();
                uploadPhotoToFirebaseStorage(key);
            }
        });
    }





    /**
     * ----------------------------------------LIFE CYCLE SUPPORT METHODS----------------------------------------------------
     */


    private void navigateToMainActivity(){
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void enableBackNavigation(){
        startActivity(new Intent(mContext, MainActivity.class));
        Animatoo.animateSlideLeft(mContext);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        enableBackNavigation();
    }
}