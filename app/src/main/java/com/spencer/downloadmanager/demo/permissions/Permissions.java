package com.spencer.downloadmanager.demo.permissions;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: huanghuojun
 * @date: 2018/9/5 14:22
 */
public class Permissions {

    private Activity mActivity;
    private List<String> mPermissions = new ArrayList<>();

    private Permissions(Activity activity){
        this.mActivity = activity;
    }
    /**
     * 设置请求的对象
     */
    public static Permissions with(Activity activity) {
        return new Permissions(activity);
    }

    /**
     * 设置权限组
     */
    public Permissions permission(String... permissions) {
        mPermissions.addAll(Arrays.asList(permissions));
        return this;
    }

    /**
     * 设置权限组
     */
    public Permissions permission(String[]... permissions) {
        for (String[] group : permissions) {
            mPermissions.addAll(Arrays.asList(group));
        }
        return this;
    }

    /**
     * 设置权限组
     */
    public Permissions permission(List<String> permissions) {
        mPermissions.addAll(permissions);
        return this;
    }


    /**
     * 请求权限
     */
    public void request(OnPermission listener) {
        //如果没有指定请求的权限，就使用清单注册的权限进行请求
        if (mPermissions == null || mPermissions.size() == 0) mPermissions = PermissionHelper.getManifestPermissions(mActivity);
        if (mPermissions == null || mPermissions.size() == 0) throw new IllegalArgumentException("The requested permission cannot be empty");
        //使用isFinishing方法Activity在熄屏状态下会导致崩溃
        //if (mActivity == null || mActivity.isFinishing()) throw new IllegalArgumentException("Illegal Activity was passed in");
        if (mActivity == null) throw new IllegalArgumentException("The activity is empty");
        if (listener == null) throw new IllegalArgumentException("The permission request callback interface must be implemented");

        ArrayList<String> failPermissions = PermissionHelper.getFailPermissions(mActivity, mPermissions);

        if (failPermissions == null || failPermissions.size() == 0) {
            //证明权限已经全部授予过
            listener.permissiOnSuccess();
        } else {
            //申请没有授予过的权限
            PermissionFragment.newInstance(failPermissions).prepareRequest(mActivity, listener);
        }
    }

    /**
     * 跳转到应用权限设置页面
     * @param context
     */
    public static void gotoPermissionSettings(Context context) {
        PermissionSettingPage.start(context, false);
    }

    /**
     * 跳转到应用权限设置页面
     * @param context
     * @param newTask   是否使用新的任务栈启动
     */
    public static void gotoPermissionSettings(Context context, boolean newTask) {
        PermissionSettingPage.start(context, newTask);
    }
}
