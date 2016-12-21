package com.glyme.nosplash.ui;

import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Glyme on 2016/11/18.
 */
public class PrefListLoader extends AsyncTaskLoader<List<AppEntry>> {
    final SharedPreferences mPref;
    final PackageManager mPm;

    List<AppEntry> mApps;

    public PrefListLoader(Context context) {
        super(context);

        // Retrieve the package manager for later use; note we don't
        // use 'context' directly but instead the save global application
        // context returned by getContext().
        mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPm = context.getPackageManager();
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public List<AppEntry> loadInBackground() {
        List<AppEntry> entries = new ArrayList<>();
        for (String key : mPref.getAll().keySet()) {
            AppEntry tmp = new AppEntry();
            try {
                ApplicationInfo appInfo = mPm.getApplicationInfo(key, 0);
                tmp.packageName = key;
                tmp.label = mPm.getApplicationLabel(appInfo).toString();
                tmp.icon = mPm.getApplicationIcon(appInfo);
                tmp.launcher = mPref.getString(key, "");
                entries.add(tmp);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

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
    }
}
