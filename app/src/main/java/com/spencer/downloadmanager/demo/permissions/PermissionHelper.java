package com.spencer.downloadmanager.demo.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description: 权限工具类
 * @author: huanghuojun
 * @date: 2018/9/5 10:12
 */
public class PermissionHelper {
    /**
     * 是否是Android 6.0 以上版本
     * @return
     */
    public static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 是否是Android 8.0 以上版本
     * @return
     */
    public static boolean isOverOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * 是否有安装权限
     * @param context
     * @return
     */
    public static boolean isHasInstallPermission(Context context) {
        if (isOverOreo()) {
            //必须设置目标SDK为26及以上才能正常检测安装权限
            if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.O) {
                throw new RuntimeException("The targetSdkVersion SDK must be 26 or more");
            }
            return context.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    /**
     * 是否有悬浮窗权限
     * @param context
     * @return
     */
    public static boolean isHasOverlaysPermission(Context context) {
        if (isOverMarshmallow()) {
            //必须设置目标SDK为23及以上才能正常检测安装权限
            if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.M) {
                throw new RuntimeException("The targetSdkVersion SDK must be 23 or more");
            }
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * 检测是否拥有指定的所有权限
     * @param permissions
     * @return
     */
    public static boolean checkPermissioAllGranted(Activity activity, String... permissions){
        for(String perminssion : permissions){
            if (ActivityCompat.checkSelfPermission(activity, perminssion) != PackageManager.PERMISSION_GRANTED){
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    /**
     * 申请权限
     * @param permissions
     * @param requestCode
     */
    public static void requestPermissioAllGranted(Activity activity, String[] permissions, int requestCode){
        //申请权限
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null){
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    /**
     * 获取AndroidMainest中注册的权限
     * @param context
     * @return
     */
    public static List<String> getManifestPermissions(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return Arrays.asList(pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取没有授权的权限
     * @param context
     * @param permissions
     * @return
     */
    public static ArrayList<String> getFailPermissions(Context context, List<String> permissions) {
        //必须设置目标SDK为23及以上才能正常检测安装权限
//        if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.M) {
//            throw new RuntimeException("The targetSdkVersion SDK must be 23 or more");
//        }
//        由于checkSelfPermission和requestPermissions从API 23才加入，低于23版本，需要在运行时判断 或者使用Support Library v4中提供的方法
//        ContextCompat.checkSelfPermission
//        ActivityCompat.requestPermissions
//        ActivityCompat.shouldShowRequestPermissionRationale

        //Android6.0以下版本就返回null
        if(!isOverMarshmallow()){
            return null;
        }
        ArrayList<String> failPermissionList = new ArrayList<>();
        for (String permission : permissions){

            //检测安装权限
            if (permission.equals(Manifest.permission.REQUEST_INSTALL_PACKAGES)) {
                if (!isHasInstallPermission(context)) {
                    failPermissionList.add(permission);
                }
                continue;
            }

            //检测悬浮窗权限
            if (permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                if (!isHasOverlaysPermission(context)) {
                    failPermissionList.add(permission);
                }
                continue;
            }

            //把没有授予过的权限加入到集合中
            if (!checkPermissioAllGranted((Activity) context)) {
                failPermissionList.add(permission);
            }
        }
        return failPermissionList;
    }

    /**
     * 获取已经授权的权限
     * @param permissions
     * @param grantResults
     * @return
     */
    public static List<String> getSucceedPermissions(String[] permissions, int[] grantResults) {
        List<String> succeedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            //把授予过的权限加入到集合中，-1表示没有授予，0表示已经授予
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                succeedPermissions.add(permissions[i]);
            }
        }
        return succeedPermissions;
    }
}
