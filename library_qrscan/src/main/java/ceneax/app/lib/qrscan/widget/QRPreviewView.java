package ceneax.app.lib.qrscan.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;

public class QRPreviewView extends FrameLayout {
    // CameraX的预览组件
    private PreviewView mPreviewView;

    public QRPreviewView(@NonNull Context context) {
        this(context, null);
    }

    public QRPreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QRPreviewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mPreviewView = new PreviewView(getContext());

        addView(mPreviewView);
    }

    public PreviewView getPreviewView() {
        return mPreviewView;
    }
}