package com.glyme.nosplash.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.glyme.nosplash.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends Activity implements AppChooseDialogFragment.AppChooseDialogListener,
        LoaderManager.LoaderCallbacks {

    private static final int FILE_SELECT = 1;
    private static final String CONF_PATH = Environment.getExternalStorageDirectory() + "/nosplash_conf.json";

    private AppListAdapter mAdapter;
    private ListView lv_changed_app;
    private SharedPreferences mPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setLogo(R.mipmap.ic_launcher);
        getActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_main);

        this.mPref = getSharedPreferences(getPackageName() + "_preferences", MODE_WORLD_READABLE);

        this.mAdapter = new AppListAdapter(this, 0);
        this.lv_changed_app = (ListView) findViewById(R.id.listview_changed_apps);
        this.lv_changed_app.setAdapter(this.mAdapter);
        this.lv_changed_app.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AppEntry app = MainActivity.this.mAdapter.getItem(position);

                final EditText input = new EditText(MainActivity.this);
                input.setText(app.launcher);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.dialog_change_launcher_title)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.this.mPref.edit().putString(app.packageName, input.getText().toString()).apply();
                                MainActivity.this.getLoaderManager().getLoader(0).forceLoad();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .setView(input)
                        .show();
            }
        });
        this.lv_changed_app.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.dialog_delete_rule)
                        .setMessage(R.string.confirm_delete_rule)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.this.mPref.edit().remove(MainActivity.this.mAdapter.getItem(position).packageName).apply();
                                MainActivity.this.getLoaderManager().getLoader(0).forceLoad();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            }
        });

        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_donate: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.dialog_donate_title)
                        .setItems(new String[]{getString(R.string.donate_alipay)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        Donate.openAlipayPayPage(MainActivity.this);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).show();
                return true;
            }
            case R.id.action_add: {
                DialogFragment dialog = new AppChooseDialogFragment();
                dialog.show(getFragmentManager(), getString(R.string.dialog_choose_app_title));
                return true;
            }
            case R.id.action_export: {
                Map<String, String> actMap = (Map<String, String>) mPref.getAll();
                JSONObject jsonAct = new JSONObject(actMap);
                JSONObject root = new JSONObject();
                try {
                    root.put("activity_map", jsonAct);
                    try {
                        FileOutputStream fos = new FileOutputStream(CONF_PATH, false);
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                        bw.write(root.toString());
                        bw.close();
                        Toast.makeText(MainActivity.this, R.string.export_succ, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            case R.id.action_import: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select"), FILE_SELECT);
                return true;
            }
            case R.id.action_help: {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, HelpActivity.class);
                startActivity(intent);
                return true;
            }

            case R.id.action_about: {
                StringBuilder sb = new StringBuilder();
                sb.append("Android: ");
                sb.append(Locale.getDefault());
                sb.append("-");
                sb.append(Build.VERSION.RELEASE);
                sb.append("\n");

                sb.append("Author: Glyme\n");
                sb.append("Mail: glymehrvrd@gmail.com\n");
                sb.append("\n");
                sb.append("我为编写和维护这款软件花费了大量的时间，如果你觉得本软件对你有帮助，可以点击红心使用支付宝为我捐款，我将会十分感谢，你的捐款将帮助我继续完善和开发更多软件:)");

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.about_title)
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage(sb.toString())
                        .show();
                return true;
            }

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT && resultCode == RESULT_OK
                && null != data) {
            Uri selectedFile = data.getData();
            String path = selectedFile.getPath();
            BufferedInputStream bis = null;
            try {
                bis = new BufferedInputStream(new FileInputStream(path));
                BufferedReader br = new BufferedReader(new InputStreamReader(bis));
                StringBuilder sb = new StringBuilder();
                String tmp;
                while ((tmp = br.readLine()) != null) {
                    sb.append(tmp);
                }
                br.close();

                String json = sb.toString();
                JSONObject root = new JSONObject(json);
                JSONObject jsonAct = root.getJSONObject("activity_map");
                mPref.edit().clear().apply();

                Iterator<String> itr = jsonAct.keys();
                while (itr.hasNext()) {
                    String key = itr.next();
                    mPref.edit().putString(key, jsonAct.getString(key)).apply();
                }

                getLoaderManager().getLoader(0).forceLoad();
                Toast.makeText(MainActivity.this, R.string.succeed, Toast.LENGTH_SHORT).show();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                getLoaderManager().getLoader(0).forceLoad();
                Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onDialogClick(DialogFragment dialog, String key) {
//        Toast.makeText(this, key, Toast.LENGTH_LONG).show();
        PackageManager pm = getPackageManager();
        Intent launcherIntent = pm.getLaunchIntentForPackage(key);

        if (!mPref.contains(key)) {
            mPref.edit().putString(key, launcherIntent.getComponent().getClassName()).apply();
            getLoaderManager().getLoader(0).forceLoad();
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new PrefListLoader(this);
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
