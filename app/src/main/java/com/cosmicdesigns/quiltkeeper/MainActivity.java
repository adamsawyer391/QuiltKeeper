package com.cosmicdesigns.quiltkeeper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.cosmicdesigns.quiltkeeper.adapter.MainAdapter;
import com.cosmicdesigns.quiltkeeper.interfaces.QuiltLoaderListener;
import com.cosmicdesigns.quiltkeeper.login.LoginActivity;
import com.cosmicdesigns.quiltkeeper.recycleviewhelper.RecycleItemClickListener;
import com.cosmicdesigns.quiltkeeper.viewmodel.QuiltViewModel;
import com.cosmicdesigns.quiltkeeper.views.InvestmentActivity;
import com.cosmicdesigns.quiltkeeper.views.NewQuiltActivity;
import com.cosmicdesigns.quiltkeeper.views.QuiltViewActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements QuiltLoaderListener{

    private static final String TAG = "MainActivity";

    private Context mContext;
    private MainAdapter mAdapter;
    final private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DrawerLayout drawerLayout;
    ImageView menuIcon;
    RecyclerView recyclerView;
    private EditText searchInput;
    private TextView search_word, search_date;
    QuiltViewModel quiltViewModel;
    private int lastPosition = 0;
    SharedPreferences mPreferences;
    SharedPreferences.Editor editor;
    CircleImageView profile_photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;

        mPreferences = getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE);

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(view ->{
            startActivity(new Intent(mContext, NewQuiltActivity.class));
            Animatoo.animateSlideRight(mContext);
        });

        menuIcon = findViewById(R.id.menu);
        drawerLayout = findViewById(R.id.drawerLayout);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        setupNavigationMenu();
        updateSearchUI();
        setupRecyclerView();
    }


    /**
     * ---------------------------RECYCLER VIEW SUPPORT METHODS-------------------------------------------------
     */

    private void setupRecyclerView(){
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //VIEW MODEL
        quiltViewModel = new ViewModelProvider(this).get(QuiltViewModel.class);
        quiltViewModel.init(MainActivity.this, "", "");
        mAdapter = new MainAdapter(quiltViewModel.getQuilts().getValue(), mContext);
        recyclerView.setAdapter(mAdapter);

        recyclerTouchListener();
        recyclerTouchHelper();
    }

    private void recyclerTouchListener(){
        recyclerView.addOnItemTouchListener(new RecycleItemClickListener(this, (view, position) -> {
            Log.d(TAG, "onItemClick:  position : " + position);
            Intent intent = new Intent(mContext, QuiltViewActivity.class);
            intent.putExtra("QuiltKey", position);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            Animatoo.animateSlideRight(mContext);
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
                String key = Objects.requireNonNull(quiltViewModel.getQuilts().getValue()).get(viewHolder.getAdapterPosition()).getQuiltKey();
                String storageFile = quiltViewModel.getQuilts().getValue().get(viewHolder.getAdapterPosition()).getFilename();
                Log.d(TAG, "onSwiped: storage file : " + storageFile);
                int position = viewHolder.getAdapterPosition();
                boolean shouldCheck = mPreferences.getBoolean("check_delete_quilt", false);
                Log.d(TAG, "onSwiped: should check : " + shouldCheck);
                if (shouldCheck){
                    Log.d(TAG, "onSwiped: checking.....");
                    confirmDeleteDialog(key, position, storageFile);
                }else{
                    Log.d(TAG, "onSwiped: NOT checking....");
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                    if (!storageFile.equals("")){
                        storageReference.child("users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child(storageFile).delete();
                    }
                    DatabaseReference mReference = FirebaseDatabase.getInstance().getReference().child("user_quilts").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                    mReference.child(key).removeValue();
                    mAdapter.notifyItemRemoved(position);
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void confirmDeleteDialog(String key, int position, String storageFile){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater layoutInflater = getLayoutInflater();
        View dialogLayout = layoutInflater.inflate(R.layout.layout_delete_dialog, null);
        CheckBox checkBox = dialogLayout.findViewById(R.id.checkbox);
        builder.setTitle("Delete Quilt?");
        builder.setMessage("This will delete the quilt permanently and the action cannot be undone. Do you wish to continue?");
        builder.setView(dialogLayout);
        builder.setPositiveButton("DELETE", (dialog, which) -> {
            if (checkBox.isChecked()){
                Log.d(TAG, "confirmDeleteDialog: check box is checked");
                editor = mPreferences.edit();
                editor.putBoolean("check_delete_quilt", false);
                editor.apply();
            }
            DatabaseReference mReference = FirebaseDatabase.getInstance().getReference().child("user_quilts").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            if (!storageFile.equals("")){
                storageReference.child("users").child(mAuth.getCurrentUser().getUid()).child(storageFile).delete();
            }
            mReference.child(key).removeValue();
            mAdapter.notifyItemRemoved(position);
            resetQuiltList("", "");
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.dismiss();
            resetQuiltList("", "");
        });
        builder.setCancelable(false);
        builder.create().show();
        Log.d(TAG, "onSwiped: position : " + position);
        Log.d(TAG, "onSwiped: quilt key : " + key);
    }





    /**
     * --------------------------------------SEARCH METHODS--------------------------------------------------------------
     */

    private void updateSearchUI(){
        searchInput = findViewById(R.id.searchInput);
        search_date = findViewById(R.id.searchDate);
        search_word = findViewById(R.id.searchWord);
        ImageView searchButton = findViewById(R.id.search_button);
        ImageView refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(v -> resetQuiltList("", ""));

        searchListeners();

        searchButton.setOnClickListener(v -> {
            String mInput = searchInput.getText().toString();
            String type;
            if (search_word.getVisibility() == View.VISIBLE){
                type = "keyword";
                Log.d(TAG, "updateSearchUI: search input : " + mInput);
                resetQuiltList(mInput, type);
                searchInput.clearFocus();
                searchInput.setText("");
            }else if (search_date.getVisibility() == View.VISIBLE){
                type = "date";
                Log.d(TAG, "updateSearchUI: search input : " + mInput);
                resetQuiltList(mInput, type);
                searchInput.clearFocus();
                searchInput.setText("");
            }
        });

    }

    private void searchListeners(){
        search_word.setOnClickListener(v -> {
            search_word.setVisibility(View.INVISIBLE);
            search_date.setVisibility(View.VISIBLE);
            searchInput.setText("");
            searchInput.setHint("Please select a date");
            searchInput.clearFocus();
        });
        search_date.setOnClickListener(v -> {
            search_date.setVisibility(View.INVISIBLE);
            search_word.setVisibility(View.VISIBLE);
            searchInput.setText("");
            searchInput.setHint("Enter a word or phrase");
            searchInput.clearFocus();
        });

        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                String hint = searchInput.getHint().toString();
                Log.d(TAG, "onFocusChange: hint value : " + hint);
                if (hint.equals("Please select a date")){
                    selectDate();
                }
            }
        });
    }

    private void selectDate(){
        Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy", Locale.US);
        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            String mSelectedDate = dateFormatter.format(newDate.getTime());
            searchInput.setText(mSelectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void resetQuiltList(String input, String type){
        Log.d(TAG, "resetQuiltList: type : " + type);
        Log.d(TAG, "resetQuiltList: method called");
        quiltViewModel.init(MainActivity.this, input, type);
        mAdapter = new MainAdapter(quiltViewModel.getQuilts().getValue(), mContext);
        recyclerView.setAdapter(mAdapter);
    }



    /**
     * -------------------------------------------NAVIGATION MENU-------------------------------------------------------
     */

    private void setupNavigationMenu(){
        NavigationView navigationView = findViewById(R.id.navigation);
        final View navView = navigationView.inflateHeaderView(R.layout.layout_navigation);
        profile_photo = navView.findViewById(R.id.profile_photo);
        TextView username = navView.findViewById(R.id.username);
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstname = Objects.requireNonNull(snapshot.child("first_name").getValue()).toString();
                String lastname = Objects.requireNonNull(snapshot.child("last_name").getValue()).toString();
                username.setText(new StringBuilder().append(firstname).append(" ").append(lastname));
                if (snapshot.hasChild("profile_photo")){
                    String profilePhoto = Objects.requireNonNull(snapshot.child("profile_photo").getValue()).toString();
                    Glide.with(getApplicationContext()).load(profilePhoto).placeholder(R.mipmap.ic_launcher_round).into(profile_photo);
                }else{
                    Glide.with(getApplicationContext()).load(R.mipmap.ic_launcher_round).into(profile_photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        navigationView.setNavigationItemSelectedListener(item -> {
            UserMenuSelector(item);
            return false;
        });
        profile_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 1);
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()){
            case R.id.home:
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.investment:
                Intent investmentIntent = new Intent(mContext, InvestmentActivity.class);
                startActivity(investmentIntent);
                drawerLayout.closeDrawer(GravityCompat.START);
                Animatoo.animateSlideLeft(mContext);
                break;
            case R.id.settings:
                Intent intent1 = new Intent(mContext, SettingsActivity.class);
                startActivity(intent1);
                drawerLayout.closeDrawer(GravityCompat.START);
                Animatoo.animateSlideLeft(mContext);
                break;
            case R.id.sign_out:
                signOutUser();
                drawerLayout.closeDrawer(GravityCompat.START);
                Animatoo.animateSlideLeft(mContext);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            if (resultCode == RESULT_OK){
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    Glide.with(getApplicationContext()).load(bitmap).into(profile_photo);

                    //uplodad photo to firebase
                    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                    final StorageReference storageReference = firebaseStorage.getReference().child("users").child(mAuth.getCurrentUser().getUid());
                    final String filename = UUID.randomUUID().toString();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                    byte[] datas = byteArrayOutputStream.toByteArray();
                    UploadTask uploadTask = storageReference.child(filename).putBytes(datas);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.child(filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("profile_photo");
                                    databaseReference.setValue(url);
                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ------------------------------------------------SUPPORT METHODS----------------------------------------------------
     */

    private void signOutUser(){
        mAuth.signOut();
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onQuiltsLoaded() {
        quiltViewModel.getQuilts().observe(this, quilts -> {
            mAdapter.notifyDataSetChanged();
            lastPosition = quilts.size();
            recyclerView.smoothScrollToPosition(lastPosition);
        });
    }

}