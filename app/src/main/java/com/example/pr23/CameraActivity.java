package com.example.pr23;

import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.util.Size;
import android.view.OrientationEventListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private TextView textView;
    private ImageCapture imageCapture;
    /*
    Inicialitzem les variables d'instància i vinculem un proveïdor de càmara
    per a que puguem vincular-li el cas de l'anàlisi d'imatges
    */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        textView = findViewById(R.id.orientation);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindImageAnalysis(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        Button captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(v -> captureImage());
    }
    private void captureImage() {
        // Configurem opcions per a la captura (p. ex., format, qualitat, etc.)
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(getOutputFile()).build();
        // Capturem la imatge
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                // Obtenim la ruta de l'arxiu de la imatge
                String imagePath = getOutputFile().getAbsolutePath();

                // Notifiquem a l'usuari que la imatge s'ha desat amb èxit
                Toast.makeText(CameraActivity.this, "La imtage s'ha desat amb èxit!", Toast.LENGTH_SHORT).show();

                // Enviem la ruta de l'arxiu a la MainActivity
                Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                intent.putExtra("IMAGE_PATH", imagePath);
                startActivity(intent);
            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // Notifiquem a l'usuari que hi ha hagut un error en capturar la imatge
                Toast.makeText(CameraActivity.this, "Error en capturar la imatge", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Creem el fitxer on es desarà la imatge
    private File getOutputFile() {
        File directory = new ContextWrapper(getApplicationContext()).getFilesDir();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        return new File(directory, fileName);
    }
    /*
    Vinculem l'ImageAnalyzer al proveïdor de la càmara creada en el mètode onCreate
    i està atent a possibles canvis en la rotació de la càmara
    */
    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        ImageCapture.Builder builder = new ImageCapture.Builder();
        imageCapture = builder.build();

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), ImageProxy::close);

        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onOrientationChanged(int orientation) {
                textView.setText(Integer.toString(orientation));
            }
        };
        orientationEventListener.enable();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
    }
}
