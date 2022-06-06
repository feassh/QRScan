package ceneax.app.lib.qrscan.arch;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;

import ceneax.app.lib.qrscan.bean.ParseResult;

public abstract class QRAnalyzer implements ImageAnalysis.Analyzer {
    // 主线程Handler
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    // 扫描结果回调
    private IScanCallback mScanCallback;

    protected final void runOnUI(Runnable runnable) {
        mMainHandler.post(runnable);
    }

    protected final void runOnUIDelay(long delayMillis, Runnable runnable) {
        mMainHandler.postDelayed(runnable, delayMillis);
    }

    protected final void onResult(@NonNull Bitmap bitmap, @NonNull ParseResult[] results) {
        if (mScanCallback == null) {
            return;
        }
        runOnUI(() -> mScanCallback.onResult(bitmap, results));
    }

    /**
     * 设置识别结果回调
     * @param scanCallback 识别结果回调
     */
    public final void setScanCallback(IScanCallback scanCallback) {
        mScanCallback = scanCallback;
    }

    /**
     * 获取识别结果回调
     * @return 识别结果回调
     */
    public final IScanCallback getScanCallback() {
        return mScanCallback;
    }

    /**
     * 连续扫描
     */
    public void rescan(long delayMillis) {}
}