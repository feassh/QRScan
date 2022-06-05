package ceneax.app.lib.qrscan.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import ceneax.app.lib.qrscan.arch.QRAnimationView;

public class DefaultQRAnimationView extends QRAnimationView implements ViewTreeObserver.OnGlobalLayoutListener {
    // 画笔
    private Paint mPaint;
    // 动画
    private ValueAnimator mValueAnimator;
    // 当前位置
    private float mCurrentPosition = 0;
    // 当前动画执行进度
    private float mCurrentFraction = 0;
    // 矩阵变换，用于Canvas平移
    private final Matrix mMatrix = new Matrix();

    public DefaultQRAnimationView(Context context) {
        this(context, null);
    }

    public DefaultQRAnimationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultQRAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);

        // 设置着色器，辐射渐变
        mPaint.setShader(new RadialGradient(getWidth() / 2f, 45,
                (getWidth() - (getWidth() / 10f) * 2) / 2,
                0xA0FFFFFF, Color.TRANSPARENT, Shader.TileMode.CLAMP));

        mValueAnimator = ValueAnimator.ofFloat(getHeight() / 5f, getHeight() - (getHeight() / 5f));
        mValueAnimator.setDuration(2500);
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mValueAnimator.addUpdateListener(animation -> {
            mCurrentPosition = (float) animation.getAnimatedValue();
            mCurrentFraction = animation.getAnimatedFraction();
            mMatrix.setTranslate(0, mCurrentPosition);
            invalidate();
        });
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCurrentFraction < 0.5) {
            mPaint.setAlpha((int) (255 * mCurrentFraction * 2));
        } else if (mCurrentFraction == 0.5) {
            mPaint.setAlpha(255);
        } else {
            mPaint.setAlpha((int) (255 * (1 - mCurrentFraction) * 2));
        }

        canvas.concat(mMatrix);
        canvas.drawArc(getWidth() / 10f, 0, getWidth() - (getWidth() / 10f),
                90, -180, 180, false, mPaint);
    }

    @Override
    public void release() {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
    }
}
