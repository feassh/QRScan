package ceneax.app.lib.qrscan.arch;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import ceneax.app.lib.qrscan.bean.ParseResult;

public interface IScanCallback {
    void onResult(@NonNull Bitmap bitmap, @NonNull ParseResult... results);
}
