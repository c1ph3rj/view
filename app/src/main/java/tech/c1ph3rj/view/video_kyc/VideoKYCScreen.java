package tech.c1ph3rj.view.video_kyc;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

import tech.c1ph3rj.view.PermissionManager;
import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.Services;

public class VideoKYCScreen extends AppCompatActivity implements PermissionManager.PermissionResultListener, RecordingTimer.TimerUpdateListener {
    private final Handler marqueeHandler = new Handler();
    CardView recordBtn;
    CardView flashBtn;
    ImageView flashIcon;
    ImageView recordIcon;
    CardView switchCameraBtn;
    ImageView switchCamIcon;
    PreviewView cameraPreview;
    TextView instructionView;
    CardView timerView;
    TextView timerTextView;
    Services services;
    PermissionManager permissionManager;
    int cameraPosition = CameraSelector.LENS_FACING_FRONT;
    boolean isFlashLightOn = false;
    boolean isPaused = false;
    Recording recording = null;
    CameraInfo currentCameraInfo;
    CameraControl currentCameraControl;
    ImageAnalysis imageAnalysis;
    RecordingTimer recordingTimer;
    TextView marqueeView;
    Executor executor;
    ObjectAnimator animator;
    private final int scrollX = 0;
    private final int deviceWidth = 0;
    private final int textWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_kycscreen);

        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Video KYC");
                actionBar.setHomeAsUpIndicator(R.drawable.back_ic);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        services = new Services(this, null);
        permissionManager = new PermissionManager(this);
        permissionManager.setPermissionResultListener(this);
        if (permissionManager.hasPermissions(permissionManager.cameraAndStoragePermissionArray())) {
            init();
        } else {
            permissionManager.requestPermissions(permissionManager.cameraAndStoragePermissionArray());
        }
    }

    void setIcon(ImageView imageView, int drawableId) {
        try {
            imageView.setImageDrawable(AppCompatResources.getDrawable(this, drawableId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void init() {
        try {
            flashBtn = findViewById(R.id.flashBtn);
            flashIcon = findViewById(R.id.flashIcon);
            recordBtn = findViewById(R.id.recordBtn);
            recordIcon = findViewById(R.id.recordIcon);
            timerView = findViewById(R.id.timerTextView);
            timerTextView = findViewById(R.id.timerText);
            switchCameraBtn = findViewById(R.id.switchCameraBtn);
            instructionView = findViewById(R.id.instructionView);
            cameraPreview = findViewById(R.id.cameraPreview);
            switchCamIcon = findViewById(R.id.switchCameraIcon);
            marqueeView = findViewById(R.id.marqueeView);

            executor = ContextCompat.getMainExecutor(this);
            recordingTimer = new RecordingTimer();
            recordingTimer.setTimerUpdateListener(this);

            switchCameraBtn.setOnClickListener(onClickSwitchCam -> {
                try {
                    if (recording != null) {
                        handlePause();
                    } else {
                        handleSwitchCamera();
                    }
                } catch (Exception e) {
                    services.handleException(e);
                }
            });

            startCamera(cameraPosition);
            marqueeView.setText("Video Kyc Process");
            startMarqueeAnimation();
        } catch (Exception e) {
            services.handleException(e);
        }
    }

    private void startMarqueeAnimation() {
        marqueeView.post(() -> {
            int textWidth = marqueeView.getWidth();
            int parentWidth = findViewById(R.id.marqueeLayout).getWidth();

            animator = ObjectAnimator.ofFloat(marqueeView, "translationX", -textWidth, parentWidth);
            animator.setDuration(10000); // Set duration for animation (adjust as needed)
            animator.setRepeatCount(ObjectAnimator.INFINITE); // Repeat the animation infinitely
            animator.setInterpolator(new LinearInterpolator());
            animator.start();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animator != null) {
            animator.cancel(); // Cancel the animation when the activity is destroyed
        }
    }

    private void handlePause() {
        try {
            isPaused = !isPaused;
            if (isPaused) {
                recording.pause();
                pauseTimer();
                setIcon(switchCamIcon, R.drawable.resume_ic);
            } else {
                recording.resume();
                resumeTimer();
                setIcon(switchCamIcon, R.drawable.pause_ic);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSwitchCamera() {
        try {
            if (cameraPosition == CameraSelector.LENS_FACING_BACK) {
                cameraPosition = CameraSelector.LENS_FACING_FRONT;
                flashBtn.setVisibility(View.GONE);
            } else {
                cameraPosition = CameraSelector.LENS_FACING_BACK;
                flashBtn.setVisibility(View.VISIBLE);
            }
            startCamera(cameraPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int getAspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    private void resumeTimer() {
        try {
            recordingTimer.resumeTimer();
            timerView.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.red));
            timerTextView.setTextColor(getColor(R.color.white));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pauseTimer() {
        try {
            recordingTimer.pauseTimer();
            timerView.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.greyLight));
            timerTextView.setTextColor(getColor(R.color.black));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    float getFreeSpaceInGB() {
        try {
            File externalStorage = Environment.getExternalStorageDirectory();
            long freeSpaceInBytes = externalStorage.getFreeSpace();
            float freeSpaceInGB = (float) freeSpaceInBytes / (1024 * 1024 * 1024);
            return (float) (Math.round(freeSpaceInGB * 10.0) / 10.0); // Round to one decimal point
        } catch (Exception e) {
            e.printStackTrace();
            return -1.0f;
        }
    }

    private void handleTapToFocus(MotionEvent event) {
        try {
            MeteringPointFactory factory = cameraPreview.getMeteringPointFactory();
            MeteringPoint point = factory.createPoint(event.getX(), event.getY());
            FocusMeteringAction action = new FocusMeteringAction.Builder(point).build();
            currentCameraControl.startFocusAndMetering(action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCamera(int cameraPosition) {
        try {
            int aspectRatio = getAspectRatio(cameraPreview.getWidth(), cameraPreview.getHeight());
            ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);

            listenableFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = listenableFuture.get();
                    Preview preview = new Preview.Builder()
                            .setResolutionSelector(
                                    new ResolutionSelector.Builder()
                                            .setAspectRatioStrategy(new AspectRatioStrategy(aspectRatio, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                                            .build()
                            )
                            .build();

                    ImageCapture imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                            .build();

                    Recorder recorder = new Recorder.Builder()
                            .setExecutor(executor)
                            .setQualitySelector(QualitySelector.from(Quality.FHD, FallbackStrategy.lowerQualityThan(Quality.FHD)))
                            .build();

                    VideoCapture<Recorder> videoCapture = VideoCapture.withOutput(recorder);

                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(cameraPosition)
                            .build();

                    cameraProvider.unbindAll();

                    imageAnalysis = new ImageAnalysis.Builder()
                            .setResolutionSelector(
                                    new ResolutionSelector.Builder()
                                            .setAspectRatioStrategy(new AspectRatioStrategy(aspectRatio
                                                    , AspectRatioStrategy.FALLBACK_RULE_AUTO))
                                            .build()
                            )
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

                    try {
                        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
                            @Override
                            public void onOrientationChanged(int orientation) {
                                int rotation;

                                // Monitors orientation values to determine the target rotation value
                                if (orientation >= 45 && orientation < 135) {
                                    rotation = Surface.ROTATION_270;
                                } else if (orientation >= 135 && orientation < 225) {
                                    rotation = Surface.ROTATION_180;
                                } else if (orientation >= 225 && orientation < 315) {
                                    rotation = Surface.ROTATION_90;
                                } else {
                                    rotation = Surface.ROTATION_0;
                                }

                                imageCapture.setTargetRotation(rotation);
                                videoCapture.setTargetRotation(rotation);
                                imageAnalysis.setTargetRotation(rotation);
                            }
                        };

                        orientationEventListener.enable();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Camera camera = cameraProvider.bindToLifecycle(VideoKYCScreen.this, cameraSelector, imageAnalysis, preview, imageCapture, videoCapture);

                    recordBtn.setOnClickListener(onClickRecord -> {
                        if (recording == null) {
                            if (getFreeSpaceInGB() < 1) {
                                Toast.makeText(this, "Not Enough Storage Space to Capture Video!", Toast.LENGTH_SHORT).show();
                            }
                            recordVideo(videoCapture);
                        } else {
                            stopRecording();
                        }
                    });


                    preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                    initFlashLight(camera);

                    currentCameraControl = camera.getCameraControl();
                    currentCameraInfo = camera.getCameraInfo();

                    initCameraFeatures();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, executor);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initCameraFeatures() {
        try {
            try {
                if (currentCameraInfo != null && currentCameraControl != null) {
                    ScaleGestureDetector.OnScaleGestureListener listener = new ScaleGestureDetector.OnScaleGestureListener() {
                        private float initialZoom = 1.0f;

                        @Override
                        public boolean onScale(@NonNull ScaleGestureDetector scaleGestureDetector) {
                            float scaleFactor = scaleGestureDetector.getScaleFactor();

                            if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                                return false;
                            }

                            initialZoom *= scaleFactor;

                            // Assuming you have a max and min zoom ratio, adjust as needed
                            float minZoom = 1.0f;
                            float maxZoom = (currentCameraInfo.getZoomState().getValue() != null) ? currentCameraInfo.getZoomState().getValue().getMaxZoomRatio() : 1;

                            // Clamp the zoom level within the specified range
                            initialZoom = Math.max(minZoom, Math.min(maxZoom, initialZoom));

                            Log.d("Zoom", String.valueOf(initialZoom));
                            currentCameraControl.setZoomRatio(initialZoom);

                            return true;
                        }

                        @Override
                        public boolean onScaleBegin(@NonNull ScaleGestureDetector scaleGestureDetector) {
                            initialZoom = (currentCameraInfo.getZoomState().getValue() != null) ? currentCameraInfo.getZoomState().getValue().getZoomRatio() : 1;
                            return true;
                        }

                        @Override
                        public void onScaleEnd(@NonNull ScaleGestureDetector scaleGestureDetector) {
                        }
                    };

                    ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getApplicationContext(), listener);

                    cameraPreview.setOnTouchListener((view, motionEvent) -> {
                        if ((motionEvent.getAction() == MotionEvent.ACTION_DOWN) || (motionEvent.getAction() == MotionEvent.ACTION_UP)) {
                            handleTapToFocus(motionEvent);
                            return true;
                        } else {
                            return scaleGestureDetector.onTouchEvent(motionEvent);
                        }
                    });


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        try {
            Recording previousRecording = recording;
            previousRecording.stop();
            recording = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recordVideo(VideoCapture<Recorder> videoCapture) {
        try {
            isPaused = false;
            setIcon(switchCamIcon, R.drawable.pause_ic);

            Recording previousRecording = recording;
            if (previousRecording != null) {
                previousRecording.stop();
                recording = null;
                return;
            }

            String fileName = generateUniqueName(true);
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/CameraFeatures/");

            MediaStoreOutputOptions videoOptions = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    .setContentValues(contentValues)
                    .build();


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            recording = videoCapture.getOutput()
                    .prepareRecording(this, videoOptions)
                    .withAudioEnabled()
                    .start(executor, videoRecordEvent -> {
                        if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                            startTimer();
                            setIcon(recordIcon, R.drawable.stop_recording_ic);
                        } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                            if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                                stopTimer();
                                Toast.makeText(VideoKYCScreen.this, "Video Recorded Successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                if (recording != null) {
                                    recording.stop();
                                    recording.close();
                                    recording = null;
                                }
                                stopTimer();
                                Toast.makeText(VideoKYCScreen.this, "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError(), Toast.LENGTH_SHORT).show();
                            }
                            setIcon(switchCamIcon, R.drawable.ic_switch_cam);
                            setIcon(recordIcon, R.drawable.record_video_kyc_ic);
                        }
                        if (recording != null && getFreeSpaceInGB() < 1) {
                            Toast.makeText(this, "Storage Space less than 1gb Please clear some storage to continue!", Toast.LENGTH_SHORT).show();
                            recordBtn.performClick();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        try {
            timerView.setVisibility(View.VISIBLE);
            instructionView.setVisibility(View.GONE);
            timerTextView.setText(R.string.reset_timer);
            recordingTimer.startTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopTimer() {
        try {
            timerView.setVisibility(View.GONE);
            instructionView.setVisibility(View.VISIBLE);
            recordingTimer.stopTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String generateUniqueName(boolean isVideo) {
        long currentTimeMillis = System.currentTimeMillis();
        long nanoseconds = System.nanoTime() % 1_000_000;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String timestamp = dateFormat.format(new Date(currentTimeMillis));
        return String.format(Locale.getDefault(), "%s_%s_%d.%s", (isVideo) ? "video" : "image", timestamp, nanoseconds, (isVideo) ? "mp4" : "jpg");
    }


    private void initFlashLight(Camera camera) {
        try {
            boolean isFlashAvailable = getApplicationContext().getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

            if (isFlashAvailable) {
                flashBtn.setOnClickListener(onClickFlash -> {
                    try {
                        isFlashLightOn = !isFlashLightOn;
                        setIcon(flashIcon, (isFlashLightOn) ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
                        camera.getCameraControl().enableTorch(isFlashLightOn);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                flashBtn.setVisibility(View.GONE);
            }

            if (cameraPosition == CameraSelector.LENS_FACING_FRONT) {
                flashBtn.setVisibility(View.GONE);
            } else {
                flashBtn.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            flashBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPermissionGranted() {
        init();
    }

    @Override
    public void onPermissionDenied() {
        permissionManager.showPermissionExplanationDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionsResult(permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionManager.handleSettingsActivityResult(permissionManager.cameraAndStoragePermissionArray(), requestCode, resultCode);
    }

    @Override
    public void onTimerUpdate(long millisUntilFinished) {
        if (timerTextView != null) {
            timerTextView.setText(formatTime(millisUntilFinished));
        }
    }

    @Override
    public void onTimerDone() {
        if (timerTextView != null) {
            timerTextView.setText(R.string.reset_timer);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        return (millis == 0) ? "00:00:00" : String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}