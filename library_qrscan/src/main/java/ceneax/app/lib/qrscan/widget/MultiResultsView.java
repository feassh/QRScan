package ceneax.app.lib.qrscan.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ceneax.app.lib.qrscan.bean.ParseResult;

@SuppressLint("ViewConstructor")
public class MultiResultsView extends View {
    @NonNull
    private final ParseResult[] mResults;
    @NonNull
    private Bitmap mBitmap;

    private final Paint mPaint;
    private final Matrix mMatrix = new Matrix();
    private final RectF mRectF = new RectF();

    private OnSelectedListener mOnSelectedListener;

    public MultiResultsView(Context context, @NonNull Bitmap bitmap, @NonNull ParseResult... results) {
        super(context);

        mBitmap = bitmap;
        mResults = results;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5f);
        mPaint.setColor(Color.GREEN);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float scale = Math.min(1f * w / mBitmap.getWidth(), 1f * h / mBitmap.getHeight());
        Matrix matrix = new Matrix();
        // 对图像进行缩放
        matrix.postScale(scale, scale);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
        // 对识别结果的坐标进行缩放
        for (ParseResult result : mResults) {
            if (result.getBounds() == null) {
                continue;
            }
            RectF rectF = new RectF(result.getBounds());
            matrix.mapRect(rectF);
            result.setBounds(new Rect((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        }

        Point center = new Point(w / 2, h / 2);
        Point bmCenter = new Point(mBitmap.getWidth() / 2, mBitmap.getHeight() / 2);
        // 对图像进行位移
        mMatrix.postTranslate(center.x - bmCenter.x, center.y - bmCenter.y);
        // 对识别结果的坐标进行位移
        for (ParseResult result : mResults) {
            if (result.getBounds() == null) {
                continue;
            }
            RectF rectF = new RectF(result.getBounds());
            mMatrix.mapRect(rectF);
            result.setBounds(new Rect((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, mMatrix, mPaint);

        for (ParseResult result : mResults) {
            if (result.getBounds() != null) {
                mRectF.set(result.getBounds());
                canvas.drawRoundRect(mRectF, 10f, 10f, mPaint);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnSelectedListener == null) {
            return super.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            for (ParseResult result : mResults) {
                if (result.getBounds() == null) {
                    continue;
                }
                if (result.getBounds().contains((int) event.getX(), (int) event.getY())) {
                    mOnSelectedListener.onSelected(result);
                    return true;
                }
            }
        }
        return true;
    }

    public void setOnSelectedListener(OnSelectedListener listener) {
        mOnSelectedListener = listener;
    }

    public interface OnSelectedListener {
        void onSelected(ParseResult result);
    }
}
