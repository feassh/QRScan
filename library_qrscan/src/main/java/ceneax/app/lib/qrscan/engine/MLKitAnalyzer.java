package ceneax.app.lib.qrscan.engine;

import android.annotation.SuppressLint;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ceneax.app.lib.qrscan.arch.QRAnalyzer;
import ceneax.app.lib.qrscan.bean.ParseResult;
import ceneax.app.lib.qrscan.util.CoderUtil;
import ceneax.app.lib.qrscan.util.ImageUtil;

public class MLKitAnalyzer extends QRAnalyzer {
    // 是否扫描完毕
    private boolean mScanFinished = false;

    // 解析器
    private final BarcodeScanner mScanner;

    public MLKitAnalyzer() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(CoderUtil.defaultMLKitCoder())
                .build();
        mScanner = BarcodeScanning.getClient(options);
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        long startTime = System.currentTimeMillis();

        if (mScanFinished) {
            image.close();
            return;
        }

        @SuppressLint("UnsafeOptInUsageError") Image mediaImage = image.getImage();
        if (mediaImage == null) {
            image.close();
            return;
        }

        InputImage inputImage = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
        // 解析二维码
        mScanner.process(inputImage)
                .addOnSuccessListener(barcodes -> {
                    List<ParseResult> results = transResults(barcodes, inputImage.getRotationDegrees(),
                            System.currentTimeMillis() - startTime);
                    if (results.isEmpty()) {
                        // 未识别到二维码，继续处理下一帧图像
                        return;
                    }

                    // 识别到结果，执行回调
                    onResult(ImageUtil.rotateBitmap(ImageUtil.yuvToBitmap(image), inputImage.getRotationDegrees()),
                            results.toArray(new ParseResult[0]));
                    // 停止接收图像帧数据
                    mScanFinished = true;
                })
                .addOnCompleteListener(task -> image.close());
    }

    @Override
    public void rescan(long delayMillis) {
        runOnUIDelay(delayMillis, () -> mScanFinished = false);
    }

    public static List<ParseResult> transResults(List<Barcode> barcodes, int rotationDegrees, long tookMs) {
        List<ParseResult> results = new ArrayList<>();
        for (int i = 0; i < barcodes.size(); i++) {
            if (barcodes.get(i).getRawValue() == null) {
                continue;
            }
            results.add(new ParseResult(
                    ImageUtil.rotateRect(barcodes.get(i).getBoundingBox(), rotationDegrees),
                    Objects.requireNonNull(barcodes.get(i).getRawValue()),
                    tookMs
            ));
        }
        return results;
    }
}
