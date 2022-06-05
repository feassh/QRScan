package ceneax.app.lib.qrscan.util;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class CoderUtil {
    public static Map<DecodeHintType, ?> defaultZXingCoder() {
        Map<DecodeHintType, Object> hints = new HashMap<>(3);

        Vector<BarcodeFormat> PRODUCT_FORMATS = new Vector<>(5);
        PRODUCT_FORMATS.add(BarcodeFormat.UPC_A);
        PRODUCT_FORMATS.add(BarcodeFormat.UPC_E);
        PRODUCT_FORMATS.add(BarcodeFormat.EAN_13);
        PRODUCT_FORMATS.add(BarcodeFormat.EAN_8);
        // PRODUCT_FORMATS.add(BarcodeFormat.RSS14);
        Vector<BarcodeFormat> ONE_D_FORMATS = new Vector<>(PRODUCT_FORMATS.size() + 4);
        ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
        ONE_D_FORMATS.add(BarcodeFormat.CODE_39);
        ONE_D_FORMATS.add(BarcodeFormat.CODE_93);
        ONE_D_FORMATS.add(BarcodeFormat.CODE_128);
        ONE_D_FORMATS.add(BarcodeFormat.ITF);
        Vector<BarcodeFormat> QR_CODE_FORMATS = new Vector<>(1);
        QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);
        Vector<BarcodeFormat> DATA_MATRIX_FORMATS = new Vector<>(1);
        DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX);

        Vector<BarcodeFormat> decodeFormats = new Vector<>();
        decodeFormats.addAll(ONE_D_FORMATS);
        decodeFormats.addAll(QR_CODE_FORMATS);
        decodeFormats.addAll(DATA_MATRIX_FORMATS);

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hints.put(DecodeHintType.CHARACTER_SET, null);
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, (ResultPointCallback) point -> {});

        return hints;
    }

    public static int defaultMLKitCoder() {
        return Barcode.FORMAT_ALL_FORMATS;
    }
}