package com.example.blindclient;

import static com.example.tools.Constants.serverURL;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.example.users.User;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackgroundCameraService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "CameraServiceChannel";

    private Handler handler;
    private ScheduledExecutorService executor;
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;

    class ServiceLifecycleOwner implements LifecycleOwner {
        private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

        ServiceLifecycleOwner() {
            lifecycleRegistry.setCurrentState(Lifecycle.State.STARTED);
        }

        @NonNull
        @Override
        public Lifecycle getLifecycle() {
            return lifecycleRegistry;
        }
    }
    private final ServiceLifecycleOwner lifecycleOwner = new ServiceLifecycleOwner();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());

        handler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startCamera();
        return START_STICKY;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // 配置ImageCapture
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // 绑定用例到生命周期
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, imageCapture);

                // 开始定时拍照
                startPeriodicCapture();

            } catch (Exception e) {
                Log.e("CameraX", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startPeriodicCapture() {
        Log.d("PeriodicCapture", "Scheduling started"); // 先确认方法被调用
        executor.scheduleWithFixedDelay(() -> {
            Log.d("PeriodicCapture", "Task executing"); // 确认任务执行
            handler.post(this::captureAndUpload);
        }, 0, 500, TimeUnit.MILLISECONDS); // 每500ms执行一次
    }

    public File compressToTargetSize(File originalFile, long targetSize) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());
        File compressedFile = new File(originalFile.getParent(), "compressed_" + originalFile.getName());

        int quality = 90;
        do {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] bytes = baos.toByteArray();

            if (bytes.length <= targetSize || quality <= 10) {
                FileOutputStream fos = new FileOutputStream(compressedFile);
                fos.write(bytes);
                fos.close();
                break;
            }

            quality -= 5;
        } while (quality > 0);

        bitmap.recycle();
        return compressedFile;
    }

    private void captureAndUpload() {
        if (imageCapture == null) return;

        // 创建临时文件保存图片
        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "temp_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, executor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        File compressedPhoto = null;
                        // 压缩图片
                        try {
                            compressedPhoto = compressToTargetSize(photoFile, 1024000);
                        } catch (IOException e) {
                            Log.e("ImageCompression", Objects.requireNonNull(e.getMessage()));
                            return;
                        }

                        // 读取文件内容并上传
//                        byte[] imageData = readFileToByteArray(photoFile);
                        Integer id = User.getUserId();
                        String ret = NetworkHandler.UploadImage(NetworkHandler.GetOkHttpClient(5,60,60), serverURL + "/route/upload", compressedPhoto, id.toString());
                        // 注意：返回值为true表明有危险，false表明网络连接失败
                        if (ret.equals("Danger")) {
                            sendUploadBroadcast(true);
                        } else if (ret.equals("Safe")) {
                        } else {
                            sendUploadBroadcast(false);
                        }
                        compressedPhoto.delete();
                        photoFile.delete(); // 上传后删除临时文件
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Photo capture failed: " + exception.getMessage(), exception);
                    }
                });
    }

    private byte[] readFileToByteArray(File file) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Files.readAllBytes(file.toPath());
            }
        } catch (IOException e) {
            Log.e("CameraX", "Error reading file", e);
            return null;
        }
        return null;
    }

    // 创建通知相关方法...
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("后台拍照服务")
                .setContentText("正在运行中...")
//                .setSmallIcon(R.drawable.ic_notification)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Camera Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void sendUploadBroadcast(boolean success) {
        Intent intent = new Intent("UPLOAD_COMPLETED");
        intent.putExtra("success", success);
        // 显式设置接收者的包名
        intent.setPackage(getPackageName());
        // 添加 FLAG_RECEIVER_INCLUDE_BACKGROUND 确保后台接收
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
//        }
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
