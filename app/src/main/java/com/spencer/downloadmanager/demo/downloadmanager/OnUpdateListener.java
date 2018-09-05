package com.spencer.downloadmanager.demo.downloadmanager;

/**
 * @description:
 * @author: huanghuojun
 * @date: 2018/9/4 16:58
 */
public interface OnUpdateListener {
    void onProgress(long readLength, long countLength);
    void onComplete();
}
