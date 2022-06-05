package ceneax.app.lib.qrscan.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;

import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageUtil {
    /**
     * 将 ByteBuffer 转为 byte[]
     */
    public static byte[] byteBufferToByteArray(ByteBuffer byteBuffer) {
        // Rewind the buffer to zero
        byteBuffer.rewind();
        byte[] data = new byte[byteBuffer.remaining()];
        byteBuffer.get(data);
        return data;
    }

    /**
     * 将 YUV 转 NV21
     */
    public static byte[] yuvToNV21(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();

        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        int size = image.getWidth() * image.getHeight();
        byte[] nv21 = new byte[size * 3 / 2];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);

        byte[] u = new byte[uSize];
        uBuffer.get(u);

        // 每隔开一位替换V，达到VU交替
        int pos = ySize + 1;
        for (int i = 0; i < uSize; i++) {
            if (i % 2 == 0) {
                nv21[pos] = u[i];
                pos += 2;
            }
        }

        return nv21;
    }

    /**
     * 将 NV21 图像数据转换为 Bitmap
     */
    public static Bitmap nv21ToBitmap(byte[] data, int width, int height) {
        BitmapFactory.Options newOptions = new BitmapFactory.Options();
        newOptions.inJustDecodeBounds = true;
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // JPG图片的质量[0-100], 100最高
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);
        byte[] rawImage = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException ignored) {}

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
    }

    /**
     * 将 YUV 转 Bitmap
     */
    public static Bitmap yuvToBitmap(ImageProxy image) {
        return nv21ToBitmap(yuvToNV21(image), image.getWidth(), image.getHeight());
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Rect rotateRect(Rect src, int angle) {
        if (src == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        RectF rectF = new RectF(src);
        matrix.setRotate(angle, src.centerX(), src.centerY());
        matrix.mapRect(rectF);
        src.left = (int) rectF.left;
        src.top = (int) rectF.top;
        src.right = (int) rectF.right;
        src.bottom = (int) rectF.bottom;
        return src;
    }

    /**
     * 转换坐标
     * 有一个常见任务是处理坐标（而不是缓冲区），例如预览时围绕检测到的人脸绘制一个框。
     * 在这种情况下，您需要将检测到的人脸的坐标从图片分析转换为预览。
     * 以下代码段会创建一个矩阵，将用于图片分析的坐标映射到 PreviewView 坐标。
     * 如需使用 Matrix 转换 (x, y) 坐标，请参阅 Matrix.mapPoints()
     */
    public static Matrix getCorrectionMatrix(ImageProxy imageProxy, PreviewView previewView) {
        Matrix matrix = new Matrix();
        Rect cropRect = imageProxy.getCropRect();
        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();

        // 源顶点的浮点数组（crop rect）按顺时针顺序排列
        float[] source = new float[] {
                cropRect.left,
                cropRect.top,
                cropRect.right,
                cropRect.top,
                cropRect.right,
                cropRect.bottom,
                cropRect.left,
                cropRect.bottom
        };

        // 目标顶点的浮点数组，按顺时针顺序
        float[] destination = new float[] {
                0,
                0,
                previewView.getWidth(),
                0,
                previewView.getWidth(),
                previewView.getHeight(),
                0,
                previewView.getHeight()
        };

        // 需要根据旋转角度来移动目标顶点。旋转度表示校正图像所需的顺时针旋转

        // 每个顶点由顶点数组中的2个浮点数表示
        int vertexSize = 2;
        // 每旋转90°，目标位置需要移动1个顶点
        int shiftOffset = rotationDegrees / 90 * vertexSize;
        float[] tempArray = destination.clone();
        for (int i = 0; i < destination.length; i ++) {
            int fromIndex = (i + shiftOffset) % source.length;
            destination[i] = tempArray[fromIndex];
        }
        matrix.setPolyToPoly(source, 0, destination, 0, 4);

        return matrix;
    }

    /**
     * Returns a transformation matrix from one reference frame into another. Handles cropping (if
     * maintaining aspect ratio is desired) and rotation.
     *
     * @param srcWidth Width of source frame.
     * @param srcHeight Height of source frame.
     * @param dstWidth Width of destination frame.
     * @param dstHeight Height of destination frame.
     * @param applyRotation Amount of rotation to apply from one frame to another. Must be a multiple
     *     of 90.
     * @param maintainAspectRatio If true, will ensure that scaling in x and y remains constant,
     *     cropping the image if necessary.
     * @return The transformation fulfilling the desired requirements.
     */
    public static Matrix getTransformationMatrix(
            final int srcWidth,
            final int srcHeight,
            final int dstWidth,
            final int dstHeight,
            final int applyRotation,
            final boolean maintainAspectRatio) {
        final Matrix matrix = new Matrix();

        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
//                LOGGER.w("Rotation of %d % 90 != 0", applyRotation);
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

        // Account for the already applied rotation, if any, and then determine how
        // much scaling is needed for each axis.
        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;

        final int inWidth = transpose ? srcHeight : srcWidth;
        final int inHeight = transpose ? srcWidth : srcHeight;

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            final float scaleFactorX = dstWidth / (float) inWidth;
            final float scaleFactorY = dstHeight / (float) inHeight;

            if (maintainAspectRatio) {
                // Scale by minimum factor so that dst is filled completely while
                // maintaining the aspect ratio. Some image may fall off the edge.
                final float scaleFactor = Math.max(scaleFactorX, scaleFactorY);
                matrix.postScale(scaleFactor, scaleFactor);
            } else {
                // Scale exactly to fill dst from src.
                matrix.postScale(scaleFactorX, scaleFactorY);
            }
        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;
    }
}
