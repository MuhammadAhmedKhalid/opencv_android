package com.example.opencv_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import opencv.CVScanner;
import opencv.util.Util;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_PHOTO = 123;
    private static final int REQ_CROP_IMAGE = 122;
    Uri currentPhotoUri = null;
    Button buttonFromDevice;
    ImageView imageView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonFromDevice = findViewById(R.id.buttonFromDevice);
        buttonFromDevice.setOnClickListener(v -> startImagePickerIntent());

        imageView = findViewById(R.id.imageView);
    }

    void startImagePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Pick an image"), REQUEST_PICK_PHOTO);
    }

    void startImageCropIntent() {
        CVScanner.startManualCropper(this, currentPhotoUri, REQ_CROP_IMAGE);}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_CROP_IMAGE:
                    if (data != null && data.getExtras() != null) {
                        String path = data.getStringExtra(CVScanner.RESULT_IMAGE_PATH);
                        File file = new File(Objects.requireNonNull(path));
                        Uri imageUri = Util.getUriForFile(this, file);
                        if (imageUri != null) {
                            try {
                                Bitmap croppedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                                imageView.setImageBitmap(croppedBitmap);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    break;
                case REQUEST_PICK_PHOTO:
                    if (data.getData() != null) {
                        currentPhotoUri = data.getData();
                        startImageCropIntent();
                    }
                    break;
            }

        }
    }

}
