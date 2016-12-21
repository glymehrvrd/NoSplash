package com.glyme.nosplash.ui;

import android.graphics.drawable.Drawable;

/**
 * Created by Glyme on 2016/11/18.
 */
public class AppEntry {
    public String packageName;
    public String label;
    public Drawable icon;
    public String launcher;


    @Override
    public String toString() {
        return this.label;
    }
}
