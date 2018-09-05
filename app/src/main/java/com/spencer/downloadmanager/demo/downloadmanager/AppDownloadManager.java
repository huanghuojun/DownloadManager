package com.spencer.downloadmanager.demo.downloadmanager;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * @description: APK升级，适配android6.0，7.0,8.0
 * @author: huanghuojun
 * @date: 2018/9/4 16:41
 */
public class AppDownloadManager {
    private static final String TAG = "AppDownloadManager";
    private Activity activity;
    private WeakReference<Activity> weakReference;
    public static long mDownloadID;
    private DownloadManager mDownloadManager;

    private DownloadReceiver mDownloadReceiver;
    private final DownloadChangeObserver mDownLoadChangeObserver;
    private OnUpdateListener mListener;

    public AppDownloadManager(Activity activity){
        this.activity = activity;
        weakReference = new WeakReference<Activity>(activity);
        mDownloadManager = (DownloadManager) weakReference.get().getSystemService(Context.DOWNLOAD_SERVICE);
        mDownLoadChangeObserver = new DownloadChangeObserver(new OnChangeListener() {
            @Override
            public void onChange() {
                updateView();
            }
        },new Handler());
        mDownloadReceiver = new DownloadReceiver();
    }

    public void downloadApk(String apkUrl, String title, String desc) {
        // 下载之前检查文件是否存在，如果存在，则删除
        File apkFile = new File(weakReference.get().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), title+".apk");
        if (apkFile != null && apkFile.exists()) {
            apkFile.delete();
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        //设置title
        request.setTitle(title);
        // 设置描述
        request.setDescription(desc);
        // 完成后显示通知栏
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //设置下载文件路径到存储的外部路径，context.getExternalFilesDir（dirType）获取到文件路径具体路径：/storage/emulated/0/Android/data/(应用包名)
        request.setDestinationInExternalFilesDir(weakReference.get(), Environment.DIRECTORY_DOWNLOADS, title+".apk");
        //在手机SD卡上创建一个download文件夹
        // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir() ;
        //指定下载到SD卡的/download/my/目录下
        // request.setDestinationInExternalPublicDir("/codoon/","codoon_health.apk");

        request.setMimeType("application/vnd.android.package-archive");
        //保存
        mDownloadID = mDownloadManager.enqueue(request);
    }

    private void updateView() {
        int[] bytesAndStatus = new int[]{0, 0, 0};
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(mDownloadID);
        Cursor cursor = null;
        try {
            cursor = mDownloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                //已经下载的字节数
                bytesAndStatus[0] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                //总需下载的字节数
                bytesAndStatus[1] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                //状态所在的列索引
                bytesAndStatus[2] = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (mListener != null) {
            mListener.onProgress(bytesAndStatus[0], bytesAndStatus[1]);
        }
    }

    /**
     * 对应Activity的onResume
     */
    public void onResume() {
        //设置监听Uri.parse("content://downloads/my_downloads")
        weakReference.get().getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, mDownLoadChangeObserver);
        //注册广播，监听APK是否下载完成
        weakReference.get().registerReceiver(mDownloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    /**
     * 对应Activity的onPause
     */
    public void onPause() {
        weakReference.get().getContentResolver().unregisterContentObserver(mDownLoadChangeObserver);
        weakReference.get().unregisterReceiver(mDownloadReceiver);
    }

    /**
     * 取消下载
     */
    public void cancel(){
        mDownloadManager.remove(mDownloadID);
    }


    public interface OnChangeListener{
        void onChange();
    }

    public static boolean isDownloadManagerAvailable(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD
                || context.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads") ==
                context.getPackageManager().COMPONENT_ENABLED_STATE_DISABLED_USER
                || context.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads") ==
                context.getPackageManager().COMPONENT_ENABLED_STATE_DISABLED
                || context.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads") ==
                context.getPackageManager().COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED){
            return false;
        }
        return true;
    }
}
