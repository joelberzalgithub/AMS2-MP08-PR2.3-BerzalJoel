package com.example.pr23;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String[] CAMERA_PERMISSION = new String[] { android.Manifest.permission.CAMERA };
    private static final int CAMERA_REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.imageView);
        // Verifiquem si es rep una ruta d'imatge de la CameraActivity
        if (getIntent().hasExtra("IMAGE_PATH")) {
            String imagePath = getIntent().getStringExtra("IMAGE_PATH");
            Log.i("INFO", "Ruta de la imatge capturada: " + imagePath);
            // Carreguem la imatge en l'ImageView emprant una biblioteca d'imatges
            assert imagePath != null;
            Glide.with(this).load(new File(imagePath)).into(imageView);
        }

        Button enableCamera = findViewById(R.id.enableCamera);
        enableCamera.setOnClickListener(v -> {
            if (hasCameraPermission()) {
                enableCamera();
            } else {
                requestPermission();
            }
        });
    }
    // Retornem un booleà depenent si l'usuari ha donat permissos de càmara o no
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    // Demanem permís per accedir a la càmara del dispositiu i poder fer l'anàlisi de la imatge
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
    }
    // Creem un nou Intent per iniciar la classe CameraActivity
    private void enableCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
}
