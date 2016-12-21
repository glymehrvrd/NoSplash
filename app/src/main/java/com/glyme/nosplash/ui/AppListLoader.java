package com.glyme.nosplash.ui;

import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.MODE_WORLD_READABLE;

/**
 * Created by Glyme on 2016/11/18.
 */
public class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
    final PackageManager mPm;

    List<AppEntry> mApps;
    PackageIntentReceiver mPackageObserver;
    SharedPreferences mPref;

    public AppListLoader(Context context) {
        super(context);

        // Retrieve the package manager for later use; note we don't
        // use 'context' directly but instead the save global application
        // context returned by getContext().
        mPm = getContext().getPackageManager();
        mPref = getContext().getSharedPreferences(
                getContext().getPackageName() + "_preferences", MODE_WORLD_READABLE);
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public List<AppEntry> loadInBackground() {
        // Retrieve all known applications.
        List<ApplicationInfo> apps = mPm.getInstalledApplications(0);
        if (apps == null) {
            apps = new ArrayList<>();
        }

        List<AppEntry> entries = new ArrayList<>();
        for (ApplicationInfo app : apps) {

            // find launcher intent
            Intent launcherIntent = mPm.getLaunchIntentForPackage(app.packageName);

            // only non-system application with launcher, and not added to list
            if (null != launcherIntent
                    && (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                    && !mPref.contains(app.packageName)) {

                AppEntry item = new AppEntry();
                item.packageName = app.packageName;
                item.label = mPm.getApplicationLabel(app).toString();
                item.icon = mPm.getApplicationIcon(app);
                item.launcher = launcherIntent.getComponent().getClassName();

                entries.add(item);
            }
        }

        Collections.sort(entries, new Comparator<AppEntry>() {
            @Override
            public int compare(AppEntry lhs, AppEntry rhs) {
                return lhs.label.compareToIgnoreCase(rhs.label);
            }
        });

        // Done!
        return entries;
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (mApps != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mApps);
        }

        // Start watching for changes in the app data.
        if (mPackageObserver == null) {
            mPackageObserver = new PackageIntentReceiver(this);
        }

        if (takeContentChanged() || mApps == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mApps != null) {
            mApps = null;
        }

        // Stop monitoring for changes.
        if (mPackageObserver != null) {
            getContext().unregisterReceiver(mPackageObserver);
            mPackageObserver = null;
        }
    }

    /**
     * Helper class to look for interesting changes to the installed apps
     * so that the loader can be updated.
     */
    public static class PackageIntentReceiver extends BroadcastReceiver {
        final AppListLoader mLoader;

        public PackageIntentReceiver(AppListLoader loader) {
            mLoader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            mLoader.getContext().registerReceiver(this, filter);
            // Register for events related to sdcard installation.
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            mLoader.getContext().registerReceiver(this, sdFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Tell the loader about the change.
            mLoader.onContentChanged();
        }
    }
}
