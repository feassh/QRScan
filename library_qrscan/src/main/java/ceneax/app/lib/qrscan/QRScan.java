package ceneax.app.lib.qrscan;

import androidx.core.app.ComponentActivity;

import ceneax.app.lib.qrscan.arch.QRAnalyzer;
import ceneax.app.lib.qrscan.arch.IScanCallback;
import ceneax.app.lib.qrscan.arch.QRAnimationView;
import ceneax.app.lib.qrscan.engine.MLKitAnalyzer;
import ceneax.app.lib.qrscan.widget.DefaultQRAnimationView;
import ceneax.app.lib.qrscan.widget.QRPreviewView;

public final class QRScan {
    private final Builder mBuilder;

    // 相机
    private final QRCamera mCamera;

    private QRScan(Builder builder) {
        mBuilder = builder;

        // 创建相机
        mCamera = new QRCamera.Builder(mBuilder.cameraBuilder).build();
        // 添加扫描动画View
        if (mBuilder.animationView != null) {
            mBuilder.qrPreviewView.addView(mBuilder.animationView);
        }
    }

    /**
     * 继续处理扫描数据，用以支持连续扫描
     */
    public void rescan() {
        rescan(0);
    }

    /**
     * 继续处理扫描数据，用以支持连续扫描
     */
    public void rescan(long delayMillis) {
        mBuilder.qrAnalyzer.rescan(delayMillis);
    }

    /**
     * 获取相机
     * @return 相机
     */
    public QRCamera getQRCamera() {
        return mCamera;
    }

    /**
     * 获取当前解析引擎
     * @return Engine
     */
    public Engine getEngine() {
        return mBuilder.engine;
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mBuilder.animationView != null) {
            mBuilder.animationView.release();
        }
        if (mCamera != null) {
            mCamera.release();
        }
    }

    public static class Builder {
        private final QRCamera.Builder cameraBuilder;
        // 预览图像View
        private QRPreviewView qrPreviewView;
        // 解析识别引擎
        private Engine engine = Engine.MLKIT;
        // 识别分析处理类
        private QRAnalyzer qrAnalyzer = new MLKitAnalyzer();
        // 扫描动画View
        private QRAnimationView animationView;

        public Builder(ComponentActivity activity) {
            cameraBuilder = new QRCamera.Builder(activity);
            // 赋值一个默认的Analyzer
            cameraBuilder.setAnalyzer(qrAnalyzer);
            // 赋值一个默认的扫描动画View
            animationView = new DefaultQRAnimationView(activity);
        }

        public Builder setQRPreviewView(QRPreviewView qrPreviewView) {
            this.qrPreviewView = qrPreviewView;
            cameraBuilder.setPreviewView(this.qrPreviewView.getPreviewView());
            return this;
        }

        public Builder setEngine(Engine engine) {
            this.engine = engine;
            setQRAnalyzer(EngineFactory.create(engine));
            return this;
        }

        public Builder setCameraId(int cameraId) {
            cameraBuilder.setCameraId(cameraId);
            return this;
        }

        public Builder setScanCallback(IScanCallback scanCallback) {
            qrAnalyzer.setScanCallback(scanCallback);
            return this;
        }

        public Builder setAnimationView(QRAnimationView animationView) {
            this.animationView = animationView;
            return this;
        }

        private void setQRAnalyzer(QRAnalyzer qrAnalyzer) {
            qrAnalyzer.setScanCallback(this.qrAnalyzer.getScanCallback());
            this.qrAnalyzer = qrAnalyzer;
            cameraBuilder.setAnalyzer(this.qrAnalyzer);
        }

        public QRScan build() {
            return new QRScan(this);
        }
    }

    public enum Engine {
        // Google MLKit
        MLKIT,
        // Google ZXing
        ZXING
    }
}