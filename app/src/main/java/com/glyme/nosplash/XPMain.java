package com.glyme.nosplash;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * XPOSED module
 * Created by Glyme on 2016/11/12.
 */
public class XPMain implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    public static final String MY_PACKAGE_NAME = XPMain.class.getPackage().getName();
    private static XSharedPreferences pref;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        if (null == pref) {
            pref = new XSharedPreferences(MY_PACKAGE_NAME);
            XposedBridge.log("[NoSplash] loading preference, pref path: " + pref.getFile().getAbsolutePath());
        }
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        // must be reload to see if change happened
        pref.reload();

        if (pref.contains(loadPackageParam.packageName)) {
            Log.d("NoSplash", "Placing hook on package: " + loadPackageParam.packageName);

            XposedHelpers.findAndHookMethod("android.app.Activity",
                    loadPackageParam.classLoader,
                    "onCreate",
                    Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            Activity activity = (Activity) methodHookParam.thisObject;
                            PackageManager pm = activity.getPackageManager();
                            Intent launcherIntent = pm.getLaunchIntentForPackage(activity.getPackageName());

                            Intent intent = activity.getIntent();

                            Log.d("NoSplash", "Current intent: " + intent.getComponent().flattenToString());

                            // if intent is launcher intent
                            if (intent.getComponent().flattenToString().equals(
                                    launcherIntent.getComponent().flattenToString())) {

                                // if default launcher activity is not modified
                                if (intent.getComponent().getClassName().equals(
                                        pref.getString(loadPackageParam.packageName, "not set")
                                )) {
                                    return;
                                }

                                // if launcher activity is not empty
                                if (!pref.getString(loadPackageParam.packageName, "not set").isEmpty()) {
                                    Intent intent2 = new Intent();
                                    intent2.setClassName(activity, pref.getString(loadPackageParam.packageName, "not set"));
//                                    intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    activity.startActivity(intent2);

                                    Log.d("NoSplash", "Replacing current intent with: " + intent2.getComponent().flattenToString());
                                }

//                                activity.setResult(Activity.RESULT_CANCELED);
                                activity.finish();

//                                android.os.Process.killProcess(android.os.Process.myPid());

//                                methodHookParam.setResult(new Object());
                            }
                        }
                    }
            );
        }
    }
}
