package ceneax.app.lib.qrscan;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.core.impl.CameraControlInternal;
import androidx.camera.core.impl.CameraInternal;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ComponentActivity;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class QRCamera {
    private final Builder mBuilder;

    private ProcessCameraProvider mCameraProvider;
    private ExecutorService mCameraExecutor;

    // 预览功能 UseCases
    private Preview mPreview;
    // 图像分析功能 UseCases
    private ImageAnalysis mImageAnalyzer;

    private QRCamera(Builder builder) {
        mBuilder = builder;
        initCamera();
    }

    /**
     * 初始化相机
     */
    private void initCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(mBuilder.activity);
        cameraProviderFuture.addListener(() -> {
            try {
                // 将Camera的lifecycle与lifecycleOwner进行绑定
                mCameraProvider = cameraProviderFuture.get();

                // 预览功能 UseCases
                mPreview = new Preview.Builder().build();
                mPreview.setSurfaceProvider(mBuilder.previewView.getSurfaceProvider());

                // 图像分析功能 UseCases
                mImageAnalyzer = new ImageAnalysis.Builder().build();
                mCameraExecutor = Executors.newSingleThreadExecutor();
                if (mBuilder.analyzer != null) {
                    mImageAnalyzer.setAnalyzer(mCameraExecutor, mBuilder.analyzer);
                }

                bindAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(mBuilder.activity));
    }

    /**
     * 绑定
     */
    public void bindAll() {
        if (mCameraProvider == null) {
            return;
        }

        // 绑定之前需要取消绑定全部
        mCameraProvider.unbindAll();
        mCameraProvider.bindToLifecycle(
                mBuilder.activity,
                new CameraSelector.Builder().requireLensFacing(mBuilder.cameraId).build(),
                // UseCases
                mPreview, mImageAnalyzer
        );
    }

    public void unBindAll() {
        if (mCameraProvider == null) {
            return;
        }
        mCameraProvider.unbindAll();
    }

    /**
     * 获取相机对象
     */
    @SuppressLint("RestrictedApi")
    @Nullable
    public CameraInternal getCamera() {
        return mPreview.getCamera();
    }

    /**
     * 控制闪光灯
     * @param enable 是否启用
     */
    @SuppressLint("RestrictedApi")
    public void enableTorch(boolean enable) {
        if (getControllerInternal() == null) {
            return;
        }
        getControllerInternal().enableTorch(enable);
        getControllerInternal().setFlashMode(enable ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF);
    }

    /**
     * 获取相机闪光灯状态
     * @return boolean
     */
    @SuppressLint("RestrictedApi")
    public boolean isTorchEnabled() {
        if (getControllerInternal() == null) {
            return false;
        }
        return getControllerInternal().getFlashMode() == ImageCapture.FLASH_MODE_ON;
    }

    /**
     * 获取相机ID
     */
    public int getCameraId() {
        return mBuilder.cameraId;
    }

    /**
     * 设置相机ID
     */
    public void setCameraId(int cameraId) {
        mBuilder.setCameraId(cameraId);
        bindAll();
    }

    /**
     * 获取内部相机控制器
     * @return CameraControlInternal
     */
    @Nullable
    public CameraControlInternal getControllerInternal() {
        if (getCamera() == null) {
            return null;
        }
        return getCamera().getCameraControlInternal();
    }

    /**
     * 获取相机控制器
     * @return 相机控制器
     */
    @Nullable
    public CameraControl getController() {
        if (getCamera() == null) {
            return null;
        }
        return getCamera().getCameraControl();
    }

    /**
     * 获取预览组件
     * @return Preview
     */
    public Preview getPreview() {
        return mPreview;
    }

    /**
     * 获取预览控件
     * @return PreviewView
     */
    public PreviewView getPreviewView() {
        return mBuilder.previewView;
    }

    /**
     * 释放资源
     */
    public void release() {
        enableTorch(false);
        if (mCameraExecutor != null) {
            mCameraExecutor.shutdown();
        }
    }

    public static class Builder {
        private final ComponentActivity activity;
        // 相机预览View
        private PreviewView previewView;
        // 相机ID，默认后置摄像头
        private int cameraId = CameraSelector.LENS_FACING_BACK;
        // 图像帧分析处理类
        private ImageAnalysis.Analyzer analyzer;

        public Builder(Builder builder) {
            activity = builder.activity;
            previewView = builder.previewView;
            cameraId = builder.cameraId;
            analyzer = builder.analyzer;
        }

        public Builder(ComponentActivity activity) {
            this.activity = activity;
        }

        public Builder setPreviewView(PreviewView previewView) {
            this.previewView = previewView;
            return this;
        }

        public Builder setCameraId(int cameraId) {
            this.cameraId = cameraId;
            return this;
        }

        public Builder setAnalyzer(ImageAnalysis.Analyzer analyzer) {
            this.analyzer = analyzer;
            return this;
        }

        public QRCamera build() {
            if (previewView == null) {
                throw new IllegalArgumentException("必须设置一个 previewView");
            }
            return new QRCamera(this);
        }
    }
}
