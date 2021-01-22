package com.cosmicdesigns.quiltkeeper.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.cosmicdesigns.quiltkeeper.R;
import com.cosmicdesigns.quiltkeeper.SettingsActivity;
import com.cosmicdesigns.quiltkeeper.utils.SharedPreferencesKeys;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class ChangeSplashPhoto extends AppCompatActivity {

    private static final String TAG = "ChangeSplashPhoto";

    private Context mContext;
    private ImageView imageView;
    private SharedPreferences sharedPreferences;
    private TextView savePhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_splash_photo);
        mContext = ChangeSplashPhoto.this;
        imageView = findViewById(R.id.splash_logo);
        TextView textView = findViewById(R.id.changePhoto);
        savePhoto = findViewById(R.id.savePhoto);
        sharedPreferences = getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferencesKeys sharedPreferencesKey = new SharedPreferencesKeys();
        textView.setOnClickListener(v -> {
            openGallery();
        });
        savePhoto.setOnClickListener(v -> {
            saveNewImage();
        });

        displayPhoto();
    }

    private void displayPhoto(){
        String logo = sharedPreferences.getString(SharedPreferencesKeys.splashLogo, SharedPreferencesKeys.emptyDefaultString);
        Glide.with(mContext).load(logo).into(imageView);
    }

    private void openGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0) {
                if (data != null) {
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        Glide.with(mContext).load(bitmap).into(imageView);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void saveNewImage(){
        try{
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss", Locale.US);
            String filename = dateFormat.format(date);
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.d(TAG, "savePhoto: path value : " + path);
            File file = new File(path);
            File directory = new File(file + "/" + "QuiltKeeper");
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.main_logo);
            imageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = null;
            bitmap = imageView.getDrawingCache();
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
    }

    private void scanFile(Context context, Uri fromFile){
        Log.d(TAG, "scanFile: uri + " + fromFile);
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(fromFile);
        context.sendBroadcast(scanIntent);
        Toasty.success(context, "Splash logo updated", Toasty.LENGTH_SHORT).show();
    }





    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(mContext, SettingsActivity.class);
        startActivity(intent);
        Animatoo.animateSlideDown(mContext);
    }
}
