package com.cosmicdesigns.quiltkeeper.views;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.cosmicdesigns.quiltkeeper.MainActivity;
import com.cosmicdesigns.quiltkeeper.R;
import com.cosmicdesigns.quiltkeeper.async.ImageLoadAsyncTask;
import com.cosmicdesigns.quiltkeeper.interfaces.QuiltLoaderListener;
import com.cosmicdesigns.quiltkeeper.model.Quilt;
import com.cosmicdesigns.quiltkeeper.viewmodel.QuiltViewModel;
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
import java.util.concurrent.atomic.AtomicLong;

import es.dmoral.toasty.Toasty;

public class QuiltViewActivity extends AppCompatActivity implements QuiltLoaderListener {

    private static final String TAG = "QuiltViewActivity";

    public static final int GALLERY_REQUEST = 1;

    private Context mContext;
    private int quiltPosition;
    Quilt quilt = new Quilt();
    QuiltViewModel quiltViewModel;

    final private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final private String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    final private DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();

    private ImageView quiltPhoto;
    private TextView quiltName, dateFinished, width, length, cost, quiltOwner, stitches, time,
            tvMadeBy, tvMachineUsed, tvDesign, tvFrame, tvBatting, tvTopThread, tvTopThreadColor,
            tvBobbinThread, tvBobbinThreadColor, tvNeedle, tvSPI, tvTowa, tvTopTension, additionalNotes;
    private Spinner spinnerInput;
    private ProgressBar progressBar;
    private TextView progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quilt_view);
        mContext = QuiltViewActivity.this;

//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);

        getStringExtraKey();
        deployUI();
        observeLiveData();
        updateImageViewUI();
    }

    private void getStringExtraKey(){
        Intent intentExtra = getIntent();
        quiltPosition = intentExtra.getIntExtra("QuiltKey", 0);
        Log.d(TAG, "getStringExtraKey: quilt key value : " + quiltPosition);
    }






    /**
     * ---------------------------UI METHODS-------------------------------------------------------
     */

    private void updateImageViewUI(){
        String textFieldType = "text";
        String spinnerFieldType = "drop down";

        ImageView edit_quilt_name = findViewById(R.id.edit_quilt_name);
        edit_quilt_name.setOnClickListener(v -> openAlertDialog("Quilt Name", textFieldType, quiltName));

        ImageView edit_finish_date = findViewById(R.id.edit_finish_date);
        edit_finish_date.setOnClickListener(v -> datePicker());

        ImageView edit_width = findViewById(R.id.edit_width);
        edit_width.setOnClickListener(v -> openAlertDialog("Width", textFieldType, width));

        ImageView edit_length = findViewById(R.id.edit_length);
        edit_length.setOnClickListener(v -> openAlertDialog("Length", textFieldType, length));

        ImageView edit_cost = findViewById(R.id.edit_cost);
        edit_cost.setOnClickListener(v-> openAlertDialog("Cost", textFieldType, cost));
        //edit_cost.setOnClickListener(v -> adjustCost());

        ImageView edit_quilt_owner = findViewById(R.id.edit_quilt_owner);
        edit_quilt_owner.setOnClickListener(v -> openAlertDialog("Quilt Owner", textFieldType, quiltOwner));

        ImageView edit_stitches = findViewById(R.id.edit_stitches);
        edit_stitches.setOnClickListener(v -> openAlertDialog("Stitches", textFieldType, stitches));

        ImageView edit_time = findViewById(R.id.edit_time);
        edit_time.setOnClickListener(v -> openTimeDialog());

        ImageView edit_made_by = findViewById(R.id.edit_made_by);
        edit_made_by.setOnClickListener(v -> openAlertDialog("Made By", spinnerFieldType, tvMadeBy));

        ImageView edit_machine = findViewById(R.id.edit_machine);
        edit_machine.setOnClickListener(v -> openAlertDialog("Machine", spinnerFieldType, tvMachineUsed));

        ImageView edit_frame = findViewById(R.id.edit_frame);
        edit_frame.setOnClickListener(v -> openAlertDialog("Frame", spinnerFieldType, tvFrame));

        ImageView edit_design = findViewById(R.id.edit_design);
        edit_design.setOnClickListener(v -> openAlertDialog("Design", spinnerFieldType, tvDesign));

        ImageView edit_batting = findViewById(R.id.edit_batting);
        edit_batting.setOnClickListener(v -> openAlertDialog("Batting", spinnerFieldType, tvBatting));

        ImageView edit_top_thread = findViewById(R.id.edit_top_thread);
        edit_top_thread.setOnClickListener(v -> openAlertDialog("Top Thread", spinnerFieldType, tvTopThread));

        ImageView edit_top_thread_color = findViewById(R.id.edit_top_thread_color);
        edit_top_thread_color.setOnClickListener(v -> openAlertDialog("Top Thread Color", spinnerFieldType, tvTopThreadColor));

        ImageView edit_bobbin_thread = findViewById(R.id.edit_bobbin_thread);
        edit_bobbin_thread.setOnClickListener(v -> openAlertDialog("Bobbin Thread", spinnerFieldType, tvBobbinThread));

        ImageView edit_bobbin_thread_color = findViewById(R.id.edit_bobbin_thread_color);
        edit_bobbin_thread_color.setOnClickListener(v -> openAlertDialog("Bobbin Thread Color", spinnerFieldType, tvBobbinThreadColor));

        ImageView edit_needle = findViewById(R.id.edit_needle);
        edit_needle.setOnClickListener(v -> openAlertDialog("Needle", spinnerFieldType, tvNeedle));

        ImageView edit_spi = findViewById(R.id.edit_spi);
        edit_spi.setOnClickListener(v -> openAlertDialog("SPI", spinnerFieldType, tvSPI));

        ImageView edit_towa = findViewById(R.id.edit_towa);
        edit_towa.setOnClickListener(v -> openAlertDialog("Towa", spinnerFieldType, tvTowa));

        ImageView edit_top_tension = findViewById(R.id.edit_top_tension);
        edit_top_tension.setOnClickListener(v -> openAlertDialog("Top Tension", spinnerFieldType, tvTopTension));

        ImageView edit_notes = findViewById(R.id.edit_notes);
        edit_notes.setOnClickListener(v -> openAlertDialog("Notes", textFieldType, additionalNotes));
    }

    private void deployUI(){
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(view -> enableBackNavigation());

        ImageView addPhoto = findViewById(R.id.ivAddPhoto);
        addPhoto.setOnClickListener(v -> openGallery());

        quiltPhoto = findViewById(R.id.quiltPhoto);
        quiltName = findViewById(R.id.quiltName);
        dateFinished = findViewById(R.id.finishDate);
        width = findViewById(R.id.width);
        length = findViewById(R.id.length);
        cost = findViewById(R.id.cost);
        quiltOwner = findViewById(R.id.quiltOwner);
        stitches = findViewById(R.id.stiches);
        time = findViewById(R.id.time);
        tvMadeBy = findViewById(R.id.tvMadeBy);
        tvMachineUsed = findViewById(R.id.tvMachineUsed);
        tvFrame = findViewById(R.id.tvFrame);
        tvDesign = findViewById(R.id.tvDesign);
        tvBatting = findViewById(R.id.tvBatting);
        tvTopThread = findViewById(R.id.tvTopThread);
        tvTopThreadColor = findViewById(R.id.tvTopThreadColor);
        tvBobbinThread = findViewById(R.id.tvBobbinThread);
        tvBobbinThreadColor = findViewById(R.id.tvBobbinThreadColor);
        tvNeedle = findViewById(R.id.tvNeedle);
        tvSPI = findViewById(R.id.tvSPI);
        tvTowa = findViewById(R.id.tvTowa);
        tvTopTension = findViewById(R.id.tvTopTension);
        additionalNotes = findViewById(R.id.additionalNotes);

        progress = findViewById(R.id.progress);
        progressBar = findViewById(R.id.progressBar);
    }







    /**
     * --------------------------LIVE DATA MVVM GET DATA/OBSERVE-------------------------------------
     */
    private void observeLiveData(){
        quiltViewModel = new ViewModelProvider(this).get(QuiltViewModel.class);
        quiltViewModel.init(mContext, "", "");
        quilt = Objects.requireNonNull(quiltViewModel.getQuilts().getValue()).get(quiltPosition);
        getQuiltDetails();
    }

    private void getQuiltDetails(){
        String url = Objects.requireNonNull(quiltViewModel.getQuilts().getValue()).get(quiltPosition).getUrl();
        Log.d(TAG, "getQuiltDetails: url : " + url);
        if (!url.equals("")){
            ImageLoadAsyncTask imageLoadAsyncTask = new ImageLoadAsyncTask(quilt.getUrl(), quiltPhoto);
            imageLoadAsyncTask.execute();
        }else{
            quiltPhoto.setVisibility(View.GONE);
        }
        quiltName.setText(quilt.getmQuiltName());
        dateFinished.setText(quilt.getmFinishDate());
        width.setText(quilt.getmWidth());
        length.setText(quilt.getmLength());
        cost.setText(quilt.getmCost());
        quiltOwner.setText(quilt.getmQuiltOwner());
        stitches.setText(quilt.getmStiches());
        time.setText(new StringBuilder().append(quilt.getmHours()).append(":").append(quilt.getmMinutes()).append(":")
                .append(quilt.getmSeconds()));
        tvMadeBy.setText(quilt.getmMadeBy());
        tvMachineUsed.setText(quilt.getmMachine());
        tvFrame.setText(quilt.getmFrame());
        tvDesign.setText(quilt.getmDesign());
        tvBatting.setText(quilt.getmBatting());
        tvTopThread.setText(quilt.getmTopThread());
        tvTopThreadColor.setText(quilt.getmTopThreadColor());
        tvBobbinThread.setText(quilt.getmBobbin());
        tvBobbinThreadColor.setText(quilt.getmBobbinColor());
        tvNeedle.setText(quilt.getmNeedle());
        tvSPI.setText(quilt.getmSPI());
        tvTowa.setText(quilt.getmTowa());
        tvTopTension.setText(quilt.getmTopTension());
        additionalNotes.setText(quilt.getmNotes());
    }








    /**
     * ----------------------------ALERT DIALOG METHODS--------------------------------------------
     */

    private void openAlertDialog(String input, String fieldType, TextView textView) {
        Log.d(TAG, "openAlertDialog: input : " + input);
        Log.d(TAG, "openAlertDialog: field type : " + fieldType);
        Log.d(TAG, "openAlertDialog: text view : " + textView);

        //inflate custom alert view with spinner or edit text
        LayoutInflater layoutInflater = getLayoutInflater();
        View dialogLayout = layoutInflater.inflate(R.layout.layout_edit_dialog, null);

        //alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Update Quilt")
                .setMessage("Edit your " + input + " values here.")
                .setView(dialogLayout);

        //instances id of edit text and spinner
        EditText editFields = dialogLayout.findViewById(R.id.edit_field);
        spinnerInput = dialogLayout.findViewById(R.id.spinner_input);

        //determine if spinner drop down menu or edit text should be visible in custom view
        if (fieldType.equals("drop down")){
            spinnerInput.setVisibility(View.VISIBLE);
            editFields.setVisibility(View.GONE);
            populateSpinner(input);
        }else if (fieldType.equals("text")){
            spinnerInput.setVisibility(View.GONE);
            editFields.setVisibility(View.VISIBLE);
            editFields.setText(textView.getText().toString());
        }

        //set stitches, width, and length to input type of numbers or numbers decimal
        if (input.equals("Width") || input.equals("Length") || input.equals("Stitches") || input.equals("Cost")){
            editFields.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }

        //add text watcher if it is stitches
        if (input.equals("Stitches")){
            editFields.addTextChangedListener(new TextWatcher() {
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
                        editFields.removeTextChangedListener(this);
                        editFields.setText(s1);
                        editFields.setSelection(s1.length());
                        editFields.addTextChangedListener(this);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        //alert dialog buttons
        builder.setPositiveButton("UPDATE", (dialog, which) -> {
            String updateValue = "";
            if (editFields.getVisibility() == View.VISIBLE){
                updateValue = editFields.getText().toString();
            }else if (spinnerInput.getVisibility() == View.VISIBLE){
                updateValue = spinnerInput.getSelectedItem().toString();
            }
            String node_path = getNodePath(input);
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(node_path, updateValue);
            mReference.child("user_quilts").child(currentUserID).child(quilt.getQuiltKey()).updateChildren(hashMap)
                    .addOnCompleteListener(task -> Toasty.success(getApplicationContext(), "Quilt updated!", Toasty.LENGTH_SHORT).show());
            String finalUpdateValue = updateValue;
            textView.setText(finalUpdateValue);
            if (input.equals("Width")){
                adjustCost();
            }
            if (input.equals("Length")){
                adjustCost();
            }
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());

        //final steps
        builder.setCancelable(true);
        builder.show();
    }

    private void openTimeDialog() {
        LayoutInflater layoutInflater = getLayoutInflater();
        View timeLayout = layoutInflater.inflate(R.layout.layout_time_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Quilt");
        builder.setMessage("Edit your time spent on the project here");
        builder.setView(timeLayout);
        EditText hours = timeLayout.findViewById(R.id.hours);
        EditText minutes = timeLayout.findViewById(R.id.minutes);
        EditText seconds = timeLayout.findViewById(R.id.seconds);
        builder.setCancelable(true);
        builder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String hoursText = hours.getText().toString();
                String minutesText = minutes.getText().toString();
                String secondsText = seconds.getText().toString();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("mHours", hoursText);
                hashMap.put("mMinutes", minutesText);
                hashMap.put("mSeconds", secondsText);
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user_quilts").child(currentUserID).child(quilt.getQuiltKey());
                databaseReference.updateChildren(hashMap).addOnCompleteListener(task -> Toasty.success(getApplicationContext(), "Quilt updated!", Toasty.LENGTH_SHORT).show());

                time.setText(new StringBuilder().append(hoursText).append(":").append(minutesText).append(":")
                        .append(secondsText));
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









    private void adjustCost() {
        String mLength = length.getText().toString();
        String mWidth = width.getText().toString();
        double mCost = (Integer.parseInt(mLength) * Integer.parseInt(mWidth)) * .03;
        String s = String.format(Locale.US, "%.2f", mCost);
        cost.setText(s);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("mCost", s);
        mReference.child("user_quilts").child(currentUserID).child(quilt.getQuiltKey()).updateChildren(hashMap)
                .addOnCompleteListener(task -> Toasty.success(getApplicationContext(), "Quilt updated!", Toasty.LENGTH_SHORT).show());
    }

    private void datePicker(){
        //AtomicLong timestamp = new AtomicLong();
        Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy", Locale.US);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            String mSelectedDate = dateFormatter.format(newDate.getTime());
            dateFinished.setText(mSelectedDate);
            try {
                //timestamp.set(getTimeStampFromDate(mSelectedDate));
                long timestamp = getTimeStampFromDate(mSelectedDate);
                String updateStamp = String.valueOf(timestamp);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("mFinishDate", mSelectedDate);
                hashMap.put("mTimeStamp", updateStamp);
                mReference.child("user_quilts").child(currentUserID).child(quilt.getQuiltKey()).updateChildren(hashMap)
                        .addOnCompleteListener(task -> Toasty.success(getApplicationContext(), "Quilt updated!", Toasty.LENGTH_SHORT).show());
            } catch (ParseException e) {
                e.printStackTrace();
            }
//            String updateStamp = String.valueOf(timestamp);
//            HashMap<String, Object> hashMap = new HashMap<>();
//            hashMap.put("mFinishDate", mSelectedDate);
//            hashMap.put("mTimeStamp", updateStamp);
//            mReference.child("user_quilts").child(currentUserID).child(quilt.getQuiltKey()).updateChildren(hashMap)
//                    .addOnCompleteListener(task -> Toasty.success(getApplicationContext(), "Quilt updated!", Toasty.LENGTH_SHORT).show());
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
     * -----------------------SPINNER SUPPORT METHODS-----------------------------------------------
     */

    private void populateSpinner(String input){
        Log.d(TAG, "populateSpinner: input value : " + input);
        String node_path = getDropdownNode(input);
        Log.d(TAG, "populateSpinner: node path value : " + node_path);
        addData(node_path);
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.layout_spinner, arrayList);
//        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerInput.setAdapter(arrayAdapter);
    }

    private String getDropdownNode(String inpput){
        String value = "";
        switch (inpput){
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
        return inpput;
    }

    private void addData(String path){
        Log.d(TAG, "addData: path : " + path);
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
                spinnerInput.setAdapter(arrayAdapter);
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
                spinnerInput.setSelection(defaultMadeByPosition);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String message = error.toString();
                Toasty.error(getApplicationContext(), message, Toasty.LENGTH_SHORT).show();
            }
        });
    }





    private String[] getArrayList(String input){
        String[] list;
        switch (input) {
            case "Made By":
                list = getResources().getStringArray(R.array.made_by_spinner);
                return list;
            case "Machine":
                list = getResources().getStringArray(R.array.machine_spinner);
                return list;
            case "Frame":
                list = getResources().getStringArray(R.array.frame_spinner);
                return list;
            case "Design":
                list = getResources().getStringArray(R.array.design_spinner);
                return list;
            case "Batting":
                list = getResources().getStringArray(R.array.batting_array);
                return list;
            case "Top Thread":
                list = getResources().getStringArray(R.array.top_thread_array);
                return list;
            case "Top Thread Color":
                list = getResources().getStringArray(R.array.top_thread_color_array);
                return list;
            case "Bobbin Thread":
                list = getResources().getStringArray(R.array.bobbin_array);
                return list;
            case "Bobbin Thread Color":
                list = getResources().getStringArray(R.array.bobbin_color_array);
                return list;
            case "Needle":
                list = getResources().getStringArray(R.array.needle_array);
                return list;
            case "SPI":
                list = getResources().getStringArray(R.array.spi_array);
                return list;
            case "Towa":
                list = getResources().getStringArray(R.array.towa_array);
                return list;
            case "Top Tension":
                list = getResources().getStringArray(R.array.top_tension_array);
                return list;
        }
        return null;
    }

    private String getNodePath(String input){
        String node_path = "";
        switch (input) {
            case "Quilt Name":
                node_path = "mQuiltName";
                return node_path;
            case "Batting":
                node_path = "mBatting";
                return node_path;
            case "Bobbin Thread":
                node_path = "mBobbin";
                return node_path;
            case "Bobbin Thread Color":
                node_path = "mBobbinColor";
                return node_path;
            case "Cost":
                node_path = "mCost";
                return node_path;
            case "Design":
                node_path = "mDesign";
                return node_path;
            case "Finish Date":
                node_path = "mFinishDate";
                return node_path;
            case "Frame":
                node_path = "mFrame";
                return node_path;
            case "Time":
                //what to do here, because there are three separate values
                break;
            case "Length":
                node_path = "mLength";
                return node_path;
            case "Machine":
                node_path = "mMachine";
                return node_path;
            case "Made By":
                node_path = "mMadeBy";
                return node_path;
            case "Needle":
                node_path = "mNeedle";
                return node_path;
            case "Notes":
                node_path = "mNotes";
                return node_path;
            case "Quilt Owner":
                node_path = "mQuiltOwner";
                return node_path;
            case "SPI":
                node_path = "mSPI";
                return node_path;
            case "Stitches":
                node_path = "mStiches";
                return node_path;
            case "Top Tension":
                node_path = "mTopTension";
                return node_path;
            case "Top Thread":
                node_path = "mTopThread";
                return node_path;
            case "Top Thread Color":
                node_path = "mTopThreadColor";
                return node_path;
            case "Towa":
                node_path = "mTowa";
                return node_path;
            case "Width":
                node_path = "mWidth";
                return node_path;
        }
        return node_path;
    }







    /**
     * ------------------------------GALLERY AND PHOTO SUPPORT METHODS--------------------------------
     */

    private void openGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
        quiltPhoto.setVisibility(View.VISIBLE);
//        addPhoto.setVisibility(View.GONE);
//        tvAddPhoto.setVisibility(View.GONE);

        //move other views below image once added
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
//                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        params.addRule(RelativeLayout.BELOW, R.id.changePhoto);
//        params.setMargins(0, 10, 0, 0);
//        tilQuiltName.setLayoutParams(params);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                Uri selectedImage = null;
                if (data != null) {
                    selectedImage = data.getData();
                }
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
                    Bitmap bmRotated = rotateBitmap(bitmap, orientation);
                    quiltPhoto.setImageBitmap(bmRotated);
                    uploadImageToFirebaseStorage(bmRotated);
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

    private void uploadImageToFirebaseStorage(Bitmap bmRotated){
        progressBar.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        progress.bringToFront();
        progressBar.bringToFront();
        if(bmRotated != null){
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            String key = Objects.requireNonNull(quiltViewModel.getQuilts().getValue()).get(quiltPosition).getQuiltKey();
            final StorageReference storageReference = firebaseStorage.getReference().child("users").child(currentUserID);

            //does the quilt have a photo that needs to be deleted and replaced?
            String exisitngURL = quiltViewModel.getQuilts().getValue().get(quiltPosition).getUrl();
            String existingFilename = quiltViewModel.getQuilts().getValue().get(quiltPosition).getFilename();
            Log.d(TAG, "uploadImageToFirebaseStorage: url : " + exisitngURL);
            Log.d(TAG, "uploadImageToFirebaseStorage: existing filename : " + existingFilename);

            if (!existingFilename.equals("")){
                Log.d(TAG, "uploadImageToFirebaseStorage: file does exist");
                storageReference.child(existingFilename).delete();
            }
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
                                    String message = "Quilt Updated!";
                                    int duration = Toasty.LENGTH_SHORT;
                                    Toasty.success(getApplicationContext(), message, duration).show();
                                });
                    }));
            uploadTask.addOnProgressListener(snapshot -> {
                int total = (int) snapshot.getTotalByteCount();
                long transferred = snapshot.getBytesTransferred();
                int progressValue = (int) (transferred / total);
                progress.setText(new StringBuilder().append(progressValue).append("%").append(" completed..."));
            });
        }
    }




    /**
     * ----------------------------LIFECYCLE SUPPORT AND NAVIGATION METHODS-------------------------------------------
     */


    private void enableBackNavigation(){
        startActivity(new Intent(mContext, MainActivity.class));
        Animatoo.animateSlideLeft(mContext);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        enableBackNavigation();
    }

    @Override
    public void onQuiltsLoaded() {
        quiltViewModel.getQuilts().observe(this, quilts -> {
            //int lastPosition = quilts.size();
        });
    }

}

