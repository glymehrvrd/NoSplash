package com.glyme.nosplash.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.glyme.nosplash.R;

public class AppListAdapter extends ArrayAdapter<AppEntry> {

    private LayoutInflater mInflater;

    public AppListAdapter(Context context, int resource) {
        super(context, resource);
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listview_item, null);
        }

        final TextView tv_label = (TextView) convertView.findViewById(R.id.label);
        final ImageView iv_icon = (ImageView) convertView.findViewById(R.id.icon);

        final String label = getItem(position).label;
        final Drawable icon = getItem(position).icon;

        tv_label.setText(label);
        iv_icon.setImageDrawable(icon);

        return convertView;
    }
}
