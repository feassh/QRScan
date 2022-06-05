package ceneax.app.lib.qrscan;

import ceneax.app.lib.qrscan.arch.QRAnalyzer;
import ceneax.app.lib.qrscan.engine.MLKitAnalyzer;
import ceneax.app.lib.qrscan.engine.ZXingAnalyzer;

public class EngineFactory {
    public static QRAnalyzer create(QRScan.Engine engine) {
        switch (engine) {
            case ZXING:
                return new ZXingAnalyzer();
            default:
                return new MLKitAnalyzer();
        }
    }
}
