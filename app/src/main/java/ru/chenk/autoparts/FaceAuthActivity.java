package ru.chenk.autoparts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.Manifest;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Byte3;
import android.util.Rational;
import android.util.Size;
import android.graphics.Matrix;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class FaceAuthActivity extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 10;

    private TextureView viewFinder;

    private FirebaseVisionFaceDetectorOptions realTimeOpts;
    private int c = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_auth);

        viewFinder = findViewById(R.id.FAA_textureView);

        realTimeOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();

        if(allPermissionsGranted()){
            viewFinder.post(new Runnable() {
                @Override
                public void run() {
                    startCamera();
                }
            });
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
        }

        viewFinder.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                updateTransform();
            }
        });
    }

    private void startCamera(){
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.FRONT)
                .setTargetAspectRatio(new Rational(1,1))
                .setTargetResolution(new Size(640, 640))
                .build();

        Preview preview = new Preview(previewConfig);

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent = (ViewGroup)viewFinder.getParent();
                parent.removeView(viewFinder);
                parent.addView(viewFinder, 0);

                viewFinder.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });

        ImageAnalysisConfig config = new ImageAnalysisConfig.Builder()
                .setTargetAspectRatio(new Rational(1, 1))
                .setTargetResolution(new Size(viewFinder.getWidth(), viewFinder.getHeight()))
                .setLensFacing(CameraX.LensFacing.FRONT)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(config);

        imageAnalysis.setAnalyzer(new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(ImageProxy image, int rotationDegrees) {

                FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(viewFinder.getBitmap());

                FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                        .getVisionFaceDetector(realTimeOpts);

                detector.detectInImage(firebaseVisionImage)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                for (FirebaseVisionFace face : faces){
                                    Rect bounds = face.getBoundingBox();

                                    Paint p = new Paint();
                                    p.setColor(Color.RED);
                                    p.setStrokeWidth(10);

                                    Canvas boxCanvas = new Canvas();
                                    boxCanvas.drawRect(bounds, p);

                                    viewFinder.draw(boxCanvas);
                                }
                            }
                        });

            }
        });

        CameraX.bindToLifecycle((LifecycleOwner)this, imageAnalysis, preview);
    }

    private void updateTransform(){
        Matrix matrix = new Matrix();

        float centerX = viewFinder.getWidth() / 2f;
        float centerY = viewFinder.getHeight() / 2f;

        int rotationDegrees = 0;

        switch (viewFinder.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotationDegrees = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270;
                break;
        }
        matrix.postRotate(((float) - rotationDegrees), centerX, centerY);

        viewFinder.setTransform(matrix);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                viewFinder.post(new Runnable() {
                    @Override
                    public void run() {
                        startCamera();
                    }
                });
            }else{
                Toast.makeText(this, "I need camera permission to use Face Auth.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

}
