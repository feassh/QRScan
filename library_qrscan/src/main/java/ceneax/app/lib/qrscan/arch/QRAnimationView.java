package ceneax.app.lib.qrscan.arch;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public abstract class QRAnimationView extends View {
    public QRAnimationView(Context context) {
        super(context);
    }

    public QRAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public QRAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public QRAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public abstract void release();
}