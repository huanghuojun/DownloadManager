package com.spencer.downloadmanager.demo.downloadmanager;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import com.spencer.downloadmanager.demo.permissions.OnPermission;
import com.spencer.downloadmanager.demo.permissions.PermissionFragment;
import com.spencer.downloadmanager.demo.permissions.PermissionHelper;

import java.io.File;

/**
 * @description:
 * @author: huanghuojun
 * @date: 2018/9/4 17:03
 */
public class DownloadReceiver extends BroadcastReceiver {

    DownloadManager mDownloadManager;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (PermissionHelper.isHasInstallPermission(context)){
            installAPK(context, intent);
        }else{
            //申请没有授予过的权限
            PermissionFragment.newInstance(Manifest.permission.REQUEST_INSTALL_PACKAGES).prepareRequest((Activity) context, new OnPermission() {
                @Override
                public void permissiOnSuccess() {
                    installAPK(context, intent);
                }

                @Override
                public void permissiOnFail() {
                    Toast.makeText(context, "授权失败，无法安装", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void installAPK(Context context, Intent intent){
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long completeDownLoadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        Uri uri;
        File apkFile = queryDownloadApk(downloadManager, completeDownLoadId);

        Intent intentInstall = new Intent();
        intentInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentInstall.setAction(Intent.ACTION_VIEW);
        if (completeDownLoadId == AppDownloadManager.mDownloadID) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // 6.0以下
                uri = mDownloadManager.getUriForDownloadedFile(completeDownLoadId);
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) { // 6.0 - 7.0
                uri = Uri.fromFile(apkFile);
            } else { // Android 7.0 以上
                uri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".provider", apkFile);
                intentInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            // 安装应用
            intentInstall.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(intentInstall);
        }

    }

    public File queryDownloadApk(DownloadManager downloadManager, long dwnID){
        File targetFile = null;
        if(dwnID != -1){
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(dwnID);
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
            Cursor cursor = null;
            try {
                cursor = downloadManager.query(query);
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        if (!TextUtils.isEmpty(uriString)){
                            targetFile = new File(Uri.parse(uriString).getPath());
                        }
                    }
                }
            } finally {
                if(cursor != null){
                    cursor.close();
                }
            }
        }
        return targetFile;
    }
}
