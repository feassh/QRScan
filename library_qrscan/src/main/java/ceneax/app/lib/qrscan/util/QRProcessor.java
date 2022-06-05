package ceneax.app.lib.qrscan.util;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.pdf417.encoder.BarcodeMatrix;

import java.util.Map;

public class QRProcessor {
    public static BarcodeScanner decoder(@Barcode.BarcodeFormat int... coder) {
        return decodeByMLKit(coder);
    }

    public static BarcodeScanner decodeByMLKit(@Barcode.BarcodeFormat int... coder) {
        int needCoder = Barcode.FORMAT_UNKNOWN;
        if (coder.length <= 0) {
            needCoder = CoderUtil.defaultMLKitCoder();
        }
        return BarcodeScanning.getClient(new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(needCoder, coder)
                        .build());
    }

    public static MultiFormatReader decodeByZXing(@Nullable Map<DecodeHintType, ?> hints) {
        if (hints == null) {
            hints = CoderUtil.defaultZXingCoder();
        }
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        return multiFormatReader;
    }

    public static void encoder() {
    }
}
