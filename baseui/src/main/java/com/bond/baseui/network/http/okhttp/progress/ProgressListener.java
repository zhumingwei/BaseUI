package com.bond.baseui.network.http.okhttp.progress;

public interface ProgressListener {
    /**
     * 进度为 --> progress*100 /total
     * @param progress
     * @param total
     * @param done
     */
    void onProgress(long progress, long total, boolean done);
}
