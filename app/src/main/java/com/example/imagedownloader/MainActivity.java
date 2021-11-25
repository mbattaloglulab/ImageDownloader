package com.example.imagedownloader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    EditText txtURL;
    Button download;
    ImageView imgView;

    //Permissions
    private static final int REQUEST_EXTERNAL_STORAGES = 1;

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtURL = findViewById(R.id.txtURL);
        download = findViewById(R.id.btnDownload);
        imgView = findViewById(R.id.imgView);



        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGES);
                }
                else{
                    DownloadTask task = new DownloadTask();
                    String[] urls = { txtURL.getText().toString() };
                    task.execute(urls);
                }
            }
        });
    }

    private Bitmap scaleBitmap(String imagePath) {
        Bitmap image = BitmapFactory.decodeFile(imagePath);
        float w = image.getWidth();
        float h = image.getHeight();
        int W = 400;
        int H = (int)((h*W)/w);
        Bitmap b =  Bitmap.createScaledBitmap(image,W,H,false);
        return b;
    }

    private void downloadFile(String strUrl, String imagePath){
        try {
            URL url = new URL(strUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            InputStream inputStream = new BufferedInputStream(url.openStream(),8192);
            OutputStream outputStream = new FileOutputStream(imagePath);
            byte[] data = new byte[1024];
            int count ;

            while ((count = inputStream.read(data)) != -1){
                outputStream.write(data, 0 ,count);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

        } catch (Exception e) {
            Log.e("downloadFile","Cannot Download:"+strUrl, e );
        }
    }

    class DownloadTask extends AsyncTask<String,Integer,Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            String filename = "temp.jpg";
            String imagePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).toString();
            downloadFile(urls[0], imagePath+"/"+filename);
            return scaleBitmap(imagePath+"/"+filename);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imgView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGES) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                String fileName = "temp.jpg";
                String imagePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).toString() + "/" + fileName;
                DownloadTask task = new DownloadTask();
                String[] urls = { txtURL.getText().toString() };
                task.execute(urls);
            } else {
                Toast.makeText(this, "External Storage permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
