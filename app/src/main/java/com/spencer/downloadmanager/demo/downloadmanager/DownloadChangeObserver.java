package com.spencer.downloadmanager.demo.downloadmanager;

import android.database.ContentObserver;
import android.os.Handler;

/**
 * @description: 观察者，当下载进度变化的时候，就会通知观察者，从而更新进度
 * @author: huanghuojun
 * @date: 2018/9/4 16:46
 */
public class DownloadChangeObserver extends ContentObserver {

    private AppDownloadManager.OnChangeListener mOnChangeListener;
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public DownloadChangeObserver(AppDownloadManager.OnChangeListener listener, Handler handler) {
        super(handler);
        mOnChangeListener = listener;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (mOnChangeListener != null){
            mOnChangeListener.onChange();
        }
    }
}
