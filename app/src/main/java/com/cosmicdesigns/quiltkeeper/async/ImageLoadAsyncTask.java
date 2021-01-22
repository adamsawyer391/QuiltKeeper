package com.cosmicdesigns.quiltkeeper.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoadAsyncTask extends AsyncTask<Void, Void, Bitmap> {

    private String url;
    private ImageView imageView;

    public ImageLoadAsyncTask(String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            URL urlConnection = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);

            int currentBitmapWidth = myBitmap.getWidth();
            int currentBitmpaHeight = myBitmap.getHeight();

            int ivWith = imageView.getWidth();
            int ivHeight = imageView.getHeight();
            int newWidth = ivWith;
            int newHeight = (int) Math.floor((double) currentBitmpaHeight * ( (double)newWidth / (double) currentBitmapWidth));

            return Bitmap.createScaledBitmap(myBitmap, newWidth, newHeight, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        imageView.setImageBitmap(result);
    }
}
