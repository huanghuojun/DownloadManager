package com.spencer.downloadmanager.demo.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @description:
 * @author: huanghuojun
 * @date: 2018/9/5 15:08
 */
public class PermissionFragment extends Fragment {

    private static final String EXTRA_KEY_PERMISSIONS = "permissions";
    private static final String EXTRA_KEY_REQUESTCODE = "requestcode";

    private final static SparseArray<OnPermission> sparseArray = new SparseArray<>();

    public static PermissionFragment newInstance(ArrayList<String> permissions){
        PermissionFragment fragment = new PermissionFragment();
        Bundle bundle = new Bundle();
        int requestCode = new Random().nextInt(255);
        bundle.putInt(EXTRA_KEY_REQUESTCODE, requestCode);
        bundle.putStringArrayList(EXTRA_KEY_PERMISSIONS, permissions);

        fragment.setArguments(bundle);
        return fragment;
    }
    public static PermissionFragment newInstance(String permission){
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(permission);
        return newInstance(permissions);
    }

    public void prepareRequest(Activity activity, OnPermission listener) {
        //将当前的请求码和对象添加到集合中
        sparseArray.put(getArguments().getInt(EXTRA_KEY_REQUESTCODE), listener);
        activity.getFragmentManager().beginTransaction().add(this, activity.getClass().getName()).commit();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<String> permissions = getArguments().getStringArrayList(EXTRA_KEY_PERMISSIONS);
        if (permissions == null) return;
        if (permissions.contains(Manifest.permission.REQUEST_INSTALL_PACKAGES) && !PermissionHelper.isHasInstallPermission(getActivity())) {
            //跳转到允许安装未知来源设置页面
            startInstallPermissionSettingActivity();
        }else if (permissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW) && !PermissionHelper.isHasOverlaysPermission(getActivity())) {
            //跳转到悬浮窗设置页面
            startOverlayPermissionSettingActivity();
        }else{
            requestPermission();
        }
    }
    private void startInstallPermissionSettingActivity() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(intent, getArguments().getInt(EXTRA_KEY_REQUESTCODE));
    }

    private void startOverlayPermissionSettingActivity() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(intent, getArguments().getInt(EXTRA_KEY_REQUESTCODE));
    }

    /**
     * 请求权限
     */
    public void requestPermission() {
        if (PermissionHelper.isOverMarshmallow()) {
            ArrayList<String> permissions = getArguments().getStringArrayList(EXTRA_KEY_PERMISSIONS);
            requestPermissions(permissions.toArray(new String[permissions.size() - 1]), getArguments().getInt(EXTRA_KEY_REQUESTCODE));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        OnPermission listener = sparseArray.get(requestCode);
        if (listener == null) return;
        //获取授予权限
        List<String> succeedPermissions = PermissionHelper.getSucceedPermissions(permissions, grantResults);
        if (succeedPermissions.size() == permissions.length) {
            listener.permissiOnSuccess();
        }else {
            listener.permissiOnFail();
        }
        //权限回调结束后要删除集合中的对象，避免重复请求
        sparseArray.remove(requestCode);
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    //是否已经回调了，避免安装权限和悬浮窗同时请求导致的重复回调
    private boolean isBackCall;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!isBackCall && requestCode == getArguments().getInt(EXTRA_KEY_REQUESTCODE)) {
            isBackCall = true;
            //需要延迟执行，不然有些华为机型授权了但是获取不到权限
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestPermission();
                }
            }, 500);
        }
    }
}
