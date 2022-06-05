package ceneax.app.lib.qrscan.engine;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;

import java.util.Map;

import ceneax.app.lib.qrscan.arch.QRAnalyzer;
import ceneax.app.lib.qrscan.bean.ParseResult;
import ceneax.app.lib.qrscan.util.CoderUtil;
import ceneax.app.lib.qrscan.util.ImageUtil;

public final class ZXingAnalyzer extends QRAnalyzer {
    // 是否扫描完毕
    private boolean mScanFinished = false;

    // 支持的图像格式
    private final StringBuilder mImageFormat = new StringBuilder();
    // 解析器
    private final MultiFormatReader mMultiFormatReader;

    public ZXingAnalyzer() {
        this(null);
    }

    public ZXingAnalyzer(Map<DecodeHintType, ?> hints) {
        mImageFormat.append(ImageFormat.YUV_420_888);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mImageFormat.append(ImageFormat.YUV_422_888)
                    .append(ImageFormat.YUV_444_888);
        }

        mMultiFormatReader = new MultiFormatReader();
        if (hints == null) {
            mMultiFormatReader.setHints(CoderUtil.defaultZXingCoder());
        } else {
            mMultiFormatReader.setHints(hints);
        }
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        long startTime = System.currentTimeMillis();

        if (mScanFinished) {
            imageProxy.close();
            return;
        }
        if (mImageFormat.indexOf(String.valueOf(imageProxy.getFormat())) == -1) {
            imageProxy.close();
            return;
        }
        if (imageProxy.getPlanes().length <= 0) {
            imageProxy.close();
            return;
        }

        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new PlanarYUVLuminanceSource(
                ImageUtil.byteBufferToByteArray(imageProxy.getPlanes()[0].getBuffer()),
                imageProxy.getWidth(), imageProxy.getHeight(),
                0, 0,
                imageProxy.getWidth(), imageProxy.getHeight(),
                false
        )));

        try {
            // 解析二维码
            Result result = mMultiFormatReader.decodeWithState(bitmap);
            if (result == null || result.getText() == null) {
                // 识别失败，继续处理下一帧图像
                return;
            }

            // 识别到结果，执行回调
            onResult(ImageUtil.rotateBitmap(ImageUtil.yuvToBitmap(imageProxy),
                    imageProxy.getImageInfo().getRotationDegrees()),
                    new ParseResult(
                        transResultPoints(result.getResultPoints()),
                        result.getText(),
                        System.currentTimeMillis() - startTime
                    ));
            // 停止接收图像帧数据
            mScanFinished = true;
        } catch (Exception ignored) {
            // 未识别到二维码，继续处理下一帧图像
        } finally {
            imageProxy.close();
        }
    }

    @Override
    public void rescan(long delayMillis) {
        runOnUIDelay(delayMillis, () -> mScanFinished = false);
    }

    public static Rect transResultPoints(ResultPoint[] points) {
        if (points == null || points.length < 4) {
            return null;
        }
        return new Rect((int) points[0].getX(), (int) points[0].getY(),
                (int) points[1].getX(), (int) points[2].getY());
    }
}