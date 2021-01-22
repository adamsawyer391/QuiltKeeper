package com.cosmicdesigns.quiltkeeper.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.cosmicdesigns.quiltkeeper.async.ImageLoadAsyncTask;
import com.cosmicdesigns.quiltkeeper.model.Quilt;
import com.cosmicdesigns.quiltkeeper.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.QuiltViewHolder> {

    private static final String TAG = "MainAdapter";

    private final ArrayList<Quilt> quiltList;
    private final Context mContext;

    public MainAdapter(ArrayList<Quilt> quiltList, Context mContext) {
        this.quiltList = quiltList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public QuiltViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quilt_list_item, parent, false);
        return new QuiltViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuiltViewHolder holder, int position) {
        int count = quiltList.size();
        holder.itemView.setTag(quiltList.get(position));
        holder.title.setText(quiltList.get(position).getmQuiltName());
        holder.date.setText(quiltList.get(position).getmFinishDate());
        holder.cost.setText(new StringBuilder().append("$").append(quiltList.get(position).getmCost()));
        Glide.with(mContext).load(quiltList.get(position).getUrl()).placeholder(R.mipmap.ic_launcher).into(holder.circleImageView);
        holder.itemView.setOnLongClickListener(v -> {
            inflateAlertDialog(position);
            return true;
        });
    }

    private void inflateAlertDialog(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Copy Quilt");
        builder.setMessage("Do you wish to copy this quilt?");
        builder.setCancelable(true);
        builder.setPositiveButton("YES", (dialog, which) -> {
            try {
                createCopyOfQuilt(position);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void createCopyOfQuilt(int position) throws ParseException {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("user_quilts").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        String key = reference.push().getKey();
        String filename = quiltList.get(position).getFilename();
        String batting = quiltList.get(position).getmBatting();
        String bobbin = quiltList.get(position).getmBobbin();
        String bobbinColor = quiltList.get(position).getmBobbinColor();
        String cost = quiltList.get(position).getmCost();
        String design = quiltList.get(position).getmDesign();
        String finishDate = quiltList.get(position).getmFinishDate();
        String frame = quiltList.get(position).getmFrame();
        String hours = quiltList.get(position).getmHours();
        String length = quiltList.get(position).getmLength();
        String machine = quiltList.get(position).getmMachine();
        String madeBy = quiltList.get(position).getmMadeBy();
        String minutes = quiltList.get(position).getmMinutes();
        String needle = quiltList.get(position).getmNeedle();
        String notes = quiltList.get(position).getmNotes();
        String quiltName = quiltList.get(position).getmQuiltName();
        String owner = quiltList.get(position).getmQuiltOwner();
        String spi = quiltList.get(position).getmSPI();
        String seconds = quiltList.get(position).getmSeconds();
        String stitches = quiltList.get(position).getmStiches();
        String topTension = quiltList.get(position).getmTopTension();
        String topThread = quiltList.get(position).getmTopThread();
        String topThreadColor = quiltList.get(position).getmTopThreadColor();
        String towa = quiltList.get(position).getmTowa();
        String width = quiltList.get(position).getmWidth();
        String url = quiltList.get(position).getUrl();
        long timestamp = getTimeStampFromDate(finishDate);
        String unixTimeStamp = String.valueOf(timestamp);
        Quilt quilt = new Quilt(quiltName + " (copy)", finishDate, cost, owner, length, width, stitches, hours, minutes, seconds, madeBy, machine, frame, design,
                batting, topThread, topThreadColor, bobbin, bobbinColor, needle, spi, towa, topTension, notes, url, filename, key, unixTimeStamp, "copy");
        if (key != null) {
            reference.child(key).setValue(quilt).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "onComplete: file name : " + filename);
                    if (!filename.equals("")){
                        uploadNewPhotoToFirebase(filename, key);
                    }
                    //uploadNewPhotoToFirebase(filename, key);
                }
            });
        }
    }

    private void uploadNewPhotoToFirebase(String url, String key){
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).child(url);
        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG, "onSuccess: bytes : " + bytes);
                //Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                final String filename = UUID.randomUUID().toString();
                UploadTask uploadTask = storageReference.child(filename).putBytes(bytes);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.child(filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("url", url);
                                hashMap.put("filename", filename);
                                hashMap.put("quiltKey", key);
                                mReference.child("user_quilts").child(firebaseAuth.getCurrentUser().getUid()).child(key).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            String message = "Quilt Copied!";
                                            int duration = Toasty.LENGTH_SHORT;
                                            Toasty.success(mContext, message, duration).show();
                                            notifyDataSetChanged();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private long getTimeStampFromDate(String finishDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        Log.d(TAG, "getTimeStampFromDate: simple date format : " + simpleDateFormat);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = simpleDateFormat.parse(finishDate);
        if (date != null) {
            return date.getTime();
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return quiltList.size();
    }

    static class QuiltViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circleImageView;
        TextView title, date, cost;

        public QuiltViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.quiltPhoto);
            title = itemView.findViewById(R.id.quiltTitle);
            date = itemView.findViewById(R.id.quiltDate);
            cost = itemView.findViewById(R.id.quiltCost);
        }

    }

}
