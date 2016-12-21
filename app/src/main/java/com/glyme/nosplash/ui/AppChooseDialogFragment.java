package com.glyme.nosplash.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import com.glyme.nosplash.R;

import java.util.List;

/**
 * Dialog to Choose Application
 * Created by Glyme on 2016/11/17.
 */
public class AppChooseDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks {
    private AppListAdapter mAdapter;
    private AppChooseDialogListener mListener;

    public interface AppChooseDialogListener {
        void onDialogClick(DialogFragment dialog, String key);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // Instantiate the AppChooseDialogListener so we can send events to the host
            mListener = (AppChooseDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement AppChooseDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.mAdapter = new AppListAdapter(getActivity(), 0);

        AppEntry tmp = new AppEntry();
        tmp.label = "Loading";
        this.mAdapter.add(tmp);

        getLoaderManager().initLoader(0, null, this);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_choose_app_title)
                .setAdapter(this.mAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                AppEntry item = mAdapter.getItem(which);
                                mListener.onDialogClick(AppChooseDialogFragment.this, item.packageName);
                            }
                        });
        return builder.create();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new AppListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mAdapter.clear();
        mAdapter.addAll((List<AppEntry>) data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.clear();
    }

}
