package com.spencer.downloadmanager.demo;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.spencer.downloadmanager.demo.downloadmanager.AndroidOPermissionListener;
import com.spencer.downloadmanager.demo.downloadmanager.AppDownloadManager;
import com.spencer.downloadmanager.demo.permissions.OnPermission;
import com.spencer.downloadmanager.demo.permissions.PermissionHelper;
import com.spencer.downloadmanager.demo.permissions.Permissions;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private AppDownloadManager mAppDownloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppDownloadManager = new AppDownloadManager(this);

        findViewById(R.id.main_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取服务器最新版本，确认需要下载更新了
                /**
                 * 其实这还缺少一个判断，判断用户是否下载过最新的app，就使用服务给的MD5值
                 * 1.本地下载文件是否存在，如果存在，获取文件的MD5值
                 * 2.拿本地文件MD5与服务器返回的最新版本文件的MD5值比对，如果一致就不用再下载了
                 * 3.不一致的话就重新下载
                 */
                startDownload();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAppDownloadManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAppDownloadManager.onPause();
    }

    private void doDownload(){
        //是否屏蔽DownloadManager
        if (AppDownloadManager.isDownloadManagerAvailable(this)){
            mAppDownloadManager.downloadApk("http://learnapp.cgnpc.com.cn/app/zxy.apk", "test","app");
        }
    }

    private void startDownload(){
        //Android 6.0 申请权限判断
        if (PermissionHelper.isOverMarshmallow()){
            String[] mPermissionList = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            Permissions.with(this)
                    .permission(mPermissionList)
                    .request(new OnPermission() {
                        @Override
                        public void permissiOnSuccess() {
                            doDownload();
                        }

                        @Override
                        public void permissiOnFail() {
                            //跳转到权限设置列表
                            Permissions.gotoPermissionSettings(MainActivity.this);
                        }
                    });
        }else{
            doDownload();
        }
    }
}
