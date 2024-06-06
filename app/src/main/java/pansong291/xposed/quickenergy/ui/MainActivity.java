package pansong291.xposed.quickenergy.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.entity.FriendWatch;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.FileUtils;
import pansong291.xposed.quickenergy.util.PermissionUtil;
import pansong291.xposed.quickenergy.util.Statistics;

public class MainActivity extends Activity {

    public static String version = "";

    private final Handler handler = new Handler();

    private boolean hasPermissions = false;

    private boolean isBackground = false;

    private TextView tvStatistics;

    private static boolean isExpModuleActive(Context context) {
        boolean isExp = false;
        if (context == null)
            throw new IllegalArgumentException("context must not be null!!");

        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Uri.parse("content://me.weishu.exposed.CP/");
            Bundle result = null;
            try {
                result = contentResolver.call(uri, "active", null, null);
            } catch (RuntimeException e) {
                // TaiChi is killed, try invoke
                try {
                    Intent intent = new Intent("me.weishu.exp.ACTION_ACTIVE");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Throwable e1) {
                    return false;
                }
            }
            if (result == null)
                result = contentResolver.call(uri, "active", null, null);

            if (result == null)
                return false;
            isExp = result.getBoolean("active", false);
        } catch (Throwable ignored) {
        }
        return isExp;
    }

    /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatistics = findViewById(R.id.tv_statistics);
//        Button btnGithub = findViewById(R.id.btn_github);
//        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
//        int height = metrics.heightPixels;

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = " v" + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        this.setTitle(this.getTitle() + version);

        setModuleActive(isExpModuleActive(this));
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("本APP是为了学习研究开发，免费提供，不得进行任何形式的转发、发布、传播。请于24小时内卸载本APP。如果您是购买的可能已经被骗，请联系卖家退款。")
                .setNegativeButton("我知道了", null)
                .create().show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasPermissions) {
            if (!hasFocus) {
                isBackground = true;
                return;
            }
            isBackground = false;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (isBackground) {
                        return;
                    }
                    hasPermissions = PermissionUtil.checkOrRequestFilePermissions(MainActivity.this);
                    if (hasPermissions) {
                        Config.load();
                        Statistics.load();
                        onResume();
                        return;
                    }
                    Toast.makeText(MainActivity.this, "未获取文件读写权限", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(this, 2000);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasPermissions) {
            tvStatistics.setText(Statistics.getText());
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        if (v.getId() == R.id.btn_test) {
            if (isApkInDebug(this)) {
                Toast toast = Toast.makeText(this, "测试", Toast.LENGTH_SHORT);
                toast.setGravity(toast.getGravity(), toast.getXOffset(), Config.INSTANCE.getToastOffsetY());
                toast.show();
                sendBroadcast(new Intent("com.eg.android.AlipayGphone.xqe.test"));
            }
            return;
        }

        String data = "file://";
        switch (v.getId()) {
            case R.id.btn_forest_log:
                data += FileUtils.getForestLogFile().getAbsolutePath();
                break;

            case R.id.btn_farm_log:
                data += FileUtils.getFarmLogFile().getAbsolutePath();
                break;

            case R.id.btn_all_log:
                data += FileUtils.getRecordLogFile().getAbsolutePath();
                break;

            case R.id.btn_github:
                data = "https://github.com/TKaxv-7S/XQuickEnergy";
                break;

            case R.id.btn_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return;
            case R.id.btn_friend_watch:
                ListDialog.show(this, getString(R.string.friend_watch), FriendWatch.getList(), new ArrayList<>(), null, ListDialog.ListType.SHOW);
                return;
        }
        Intent it = new Intent(this, HtmlViewerActivity.class);
        it.setData(Uri.parse(data));
        startActivity(it);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int state = getPackageManager()
                .getComponentEnabledSetting(new ComponentName(this, getClass().getCanonicalName() + "Alias"));
        menu.add(0, 1, 1, R.string.hide_the_application_icon)
                .setCheckable(true)
                .setChecked(state > PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
        menu.add(0, 2, 2, R.string.export_the_log_file);
        menu.add(0, 3, 3, R.string.export_the_statistic_file);
        menu.add(0, 4, 4, R.string.import_the_statistic_file);
        menu.add(0, 6, 6, R.string.settings);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Config.INSTANCE.isDebugMode()) {
            MenuItem item = menu.findItem(5);
            if (item == null) {
                menu.add(0, 5, 5, R.string.view_debug);
            }
        } else {
            menu.removeItem(5);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                int state = item.isChecked() ? PackageManager.COMPONENT_ENABLED_STATE_DEFAULT : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                getPackageManager()
                        .setComponentEnabledSetting(new ComponentName(this, getClass().getCanonicalName() + "Alias"), state, PackageManager.DONT_KILL_APP);
                item.setChecked(!item.isChecked());
                break;

            case 2:
                File logFile = FileUtils.exportFile(FileUtils.getRuntimeLogFile());
                if (logFile != null) {
                    Toast.makeText(this, "文件已导出到: " + logFile.getPath(), Toast.LENGTH_SHORT).show();
                }
                break;

            case 3:
                File statisticsFile = FileUtils.exportFile(FileUtils.getStatisticsFile());
                if (statisticsFile != null) {
                    Toast.makeText(this, "文件已导出到: " + statisticsFile.getPath(), Toast.LENGTH_SHORT).show();
                }
                break;

            case 4:
                if (FileUtils.copyTo(FileUtils.getExportedStatisticsFile(), FileUtils.getStatisticsFile())) {
                    tvStatistics.setText(Statistics.getText());
                    Toast.makeText(this, "导入成功！", Toast.LENGTH_SHORT).show();
                }
                break;

            case 5:
                String data = "file://";
                data += FileUtils.getDebugLogFile().getAbsolutePath();
                Intent it = new Intent(this, HtmlViewerActivity.class);
                it.setData(Uri.parse(data));
                startActivity(it);
                break;

            case 6:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setModuleActive(boolean b) {
//        ImageView ivUnactivated = findViewById(R.id.iv_unactivated);
//        ivUnactivated.setVisibility(b ? View.GONE : View.VISIBLE);

        this.setTitle(this.getTitle() + (b ? "【已激活】" : "【未激活】"));
    }

}
