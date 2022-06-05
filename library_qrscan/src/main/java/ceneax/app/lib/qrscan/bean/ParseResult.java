package ceneax.app.lib.qrscan.bean;

import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ParseResult {
    @Nullable
    private Rect bounds;
    @NonNull
    private String content;
    private long tookMs;

    public ParseResult(@Nullable Rect bounds, @NonNull String content, long tookMs) {
        this.bounds = bounds;
        this.content = content;
        this.tookMs = tookMs;
    }

    @Nullable
    public Rect getBounds() {
        return bounds;
    }

    public void setBounds(@Nullable Rect bounds) {
        this.bounds = bounds;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    public long getTookMs() {
        return tookMs;
    }

    public void setTookMs(long tookMs) {
        this.tookMs = tookMs;
    }
}