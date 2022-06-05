package ceneax.app.lib.qrscan;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import ceneax.app.lib.qrscan.arch.IScanCallback;
import ceneax.app.lib.qrscan.bean.ParseResult;
import ceneax.app.lib.qrscan.engine.MLKitAnalyzer;
import ceneax.app.lib.qrscan.util.QRProcessor;
import ceneax.app.lib.qrscan.widget.MultiResultsView;

public class QRScanActivity extends AppCompatActivity implements IScanCallback, ActivityResultCallback<Uri> {
    private QRScan mQRScan;

    private ConstraintLayout mRoot;
    private ImageView mIvTorch;
    private ImageView mIvGallery;

    private final ActivityResultLauncher<String[]> mDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 沉浸式状态栏
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        // 设置暗色状态栏
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(false);
        }

        setContentView(R.layout.ceneax_app_lib_qrscan_activity_qr_scan);

        // 权限检查并申请
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                init();
            } else {
                finish();
            }
        }).launch(Manifest.permission.CAMERA);
    }

    private void init() {
        initView();
        bindEvent();
    }

    private void initView() {
        mRoot = findViewById(R.id.root);
        mIvTorch = findViewById(R.id.ivTorch);
        mIvGallery = findViewById(R.id.ivGallery);

        mQRScan = new QRScan.Builder(this)
                .setQRPreviewView(findViewById(R.id.qrPreviewView))
                .setScanCallback(this)
                .build();
    }

    private void bindEvent() {
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        // 闪光灯
        mIvTorch.setOnClickListener(v -> {
            if (mQRScan.getQRCamera().isTorchEnabled()) {
                mQRScan.getQRCamera().enableTorch(false);
                mIvTorch.setImageResource(R.drawable.ceneax_app_lib_qrscan_icon_torch_disable);
            } else {
                mQRScan.getQRCamera().enableTorch(true);
                mIvTorch.setImageResource(R.drawable.ceneax_app_lib_qrscan_icon_torch_enable);
            }
        });

        // 相册
        mIvGallery.setOnClickListener(v -> {
            mDocumentLauncher.launch(new String[]{"image/*"});
        });
    }

    @Override
    public void onActivityResult(Uri result) {
        if (result == null) {
            return;
        }

        try {
            InputImage inputImage = InputImage.fromFilePath(this, result);
            QRProcessor.decoder().process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        List<ParseResult> results = MLKitAnalyzer.transResults(barcodes,
                                inputImage.getRotationDegrees(), 0);
                        if (results.isEmpty()) {
                            toast("未识别到结果");
                            return;
                        }

                        onResult(Objects.requireNonNull(inputImage.getBitmapInternal()),
                                results.toArray(new ParseResult[0]));
                    })
                    .addOnFailureListener(e -> {
                        toast("扫描失败: " + e.getMessage());
                    });
        } catch (IOException e) {
            toast("扫描失败: " + e.getMessage());
        }
    }

    @Override
    public void onResult(@NonNull Bitmap bitmap, @NonNull ParseResult... results) {
        // 只扫描到一个结果
        if (results.length == 1) {
            bitmap.recycle();
            finishWithResult(results[0]);
            return;
        }

        // 扫描到多个结果
        MultiResultsView drawView = new MultiResultsView(this, bitmap, results);
        drawView.setBackgroundColor(Color.BLACK);
        drawView.setTag("multi_results_container");
        drawView.setOnSelectedListener(this::finishWithResult);
        mRoot.addView(drawView);

        // 暂停相机
        mQRScan.getQRCamera().unBindAll();
    }

    @Override
    public void onBackPressed() {
        View container = mRoot.findViewWithTag("multi_results_container");
        if (container == null) {
            super.onBackPressed();
        } else {
            mRoot.removeView(container);
            // 重新启动相机
            mQRScan.getQRCamera().bindAll();
            mQRScan.rescan();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQRScan.release();
    }

    private void finishWithResult(ParseResult result) {
        Bundle bundle = new Bundle();
        bundle.putString("data", result.getContent());
        setResult(RESULT_OK, getIntent().putExtras(bundle));
        finish();
    }

    private void toast(String content) {
        if (content == null) {
            return;
        }
        Toast.makeText(getApplicationContext(), content, Toast.LENGTH_LONG).show();
    }
}