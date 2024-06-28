package tkaxv7s.xposed.sesame.ui;

import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ConfigV2;
import tkaxv7s.xposed.sesame.data.RunType;
import tkaxv7s.xposed.sesame.data.ViewAppInfo;
import tkaxv7s.xposed.sesame.entity.FriendWatch;
import tkaxv7s.xposed.sesame.model.normal.base.BaseModel;
import tkaxv7s.xposed.sesame.util.FileUtil;
import tkaxv7s.xposed.sesame.util.Log;
import tkaxv7s.xposed.sesame.util.PermissionUtil;
import tkaxv7s.xposed.sesame.util.Statistics;

import java.io.File;
import java.util.LinkedHashMap;

public class MainActivity extends BaseActivity {

    private final Handler handler = new Handler();

    private boolean hasPermissions = false;

    private boolean isBackground = false;

    private boolean isClick = false;

    private TextView tvStatistics;

    private Handler viewHandler;

    private Runnable titleRunner;

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStatistics = findViewById(R.id.tv_statistics);
        ViewAppInfo.checkRunType();
        updateSubTitle(ViewAppInfo.getRunType());
        viewHandler = new Handler();
        titleRunner = () -> updateSubTitle(RunType.DISABLE);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i("view broadcast action:" + action + " intent:" + intent);
                if (action != null) {
                    switch (action) {
                        case "tkaxv7s.xposed.sesame.status":
                            if (RunType.DISABLE == ViewAppInfo.getRunType()) {
                                updateSubTitle(RunType.PACKAGE);
                            }
                            viewHandler.removeCallbacks(titleRunner);
                            if (isClick) {
                                Toast toast = Toast.makeText(context, "芝麻粒加载状态正常", Toast.LENGTH_SHORT);
                                toast.setGravity(toast.getGravity(), toast.getXOffset(), BaseModel.getToastOffsetY().getValue());
                                toast.show();
                                isClick = false;
                            }
                            break;
                        case "tkaxv7s.xposed.sesame.update":
                            Statistics.load();
                            tvStatistics.setText(Statistics.getText());
                            break;
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tkaxv7s.xposed.sesame.status");
        intentFilter.addAction("tkaxv7s.xposed.sesame.update");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(broadcastReceiver, intentFilter);
        }
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
            if (RunType.DISABLE == ViewAppInfo.getRunType()) {
                viewHandler.postDelayed(titleRunner, 3000);
                try {
                    sendBroadcast(new Intent("com.eg.android.AlipayGphone.sesame.status"));
                } catch (Throwable th) {
                    Log.i("view sendBroadcast status err:");
                    Log.printStackTrace(th);
                }
            }
            try {
                Statistics.load();
                tvStatistics.setText(Statistics.getText());
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        if (v.getId() == R.id.btn_test) {
            try {
                sendBroadcast(new Intent("com.eg.android.AlipayGphone.sesame.status"));
                isClick = true;
            } catch (Throwable th) {
                Log.i("view sendBroadcast status err:");
                Log.printStackTrace(th);
            }
            return;
        }

        String data = "file://";
        switch (v.getId()) {
            case R.id.btn_forest_log:
                data += FileUtil.getForestLogFile().getAbsolutePath();
                break;

            case R.id.btn_farm_log:
                data += FileUtil.getFarmLogFile().getAbsolutePath();
                break;

            case R.id.btn_all_log:
                data += FileUtil.getRecordLogFile().getAbsolutePath();
                break;

            case R.id.btn_github:
                data = "https://github.com/TKaxv-7S/Sesame-TK";
                break;

            case R.id.btn_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return;
            case R.id.btn_friend_watch:
                ListDialog.show(this, getString(R.string.friend_watch), FriendWatch.getList(), new LinkedHashMap<>(), false, ListDialog.ListType.SHOW);
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
        menu.add(0, 2, 2, R.string.view_error_log_file);
        menu.add(0, 3, 3, R.string.export_error_log_file);
        menu.add(0, 4, 4, R.string.export_runtime_log_file);
        menu.add(0, 5, 5, R.string.export_the_statistic_file);
        menu.add(0, 6, 6, R.string.import_the_statistic_file);
        menu.add(0, 8, 8, R.string.settings);
        if("TEST".equals(ViewAppInfo.getAppVersion())) {
            menu.add(0, 9, 9, R.string.sync_the_config_file);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (hasPermissions) {
            if (!ConfigV2.INSTANCE.isInit()) {
                ConfigV2.load();
            }
            if (BaseModel.getDebugMode().getValue()) {
                MenuItem item = menu.findItem(7);
                if (item == null) {
                    menu.add(0, 7, 7, R.string.view_debug);
                }
            } else {
                menu.removeItem(7);
            }
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
                String errorData = "file://";
                errorData += FileUtil.getErrorLogFile().getAbsolutePath();
                Intent errorIt = new Intent(this, HtmlViewerActivity.class);
                errorIt.setData(Uri.parse(errorData));
                startActivity(errorIt);
                break;

            case 3:
                File errorLogFile = FileUtil.exportFile(FileUtil.getErrorLogFile());
                if (errorLogFile != null) {
                    Toast.makeText(this, "文件已导出到: " + errorLogFile.getPath(), Toast.LENGTH_SHORT).show();
                }
                break;

            case 4:
                File allLogFile = FileUtil.exportFile(FileUtil.getRuntimeLogFile());
                if (allLogFile != null) {
                    Toast.makeText(this, "文件已导出到: " + allLogFile.getPath(), Toast.LENGTH_SHORT).show();
                }
                break;

            case 5:
                File statisticsFile = FileUtil.exportFile(FileUtil.getStatisticsFile());
                if (statisticsFile != null) {
                    Toast.makeText(this, "文件已导出到: " + statisticsFile.getPath(), Toast.LENGTH_SHORT).show();
                }
                break;

            case 6:
                if (FileUtil.copyTo(FileUtil.getExportedStatisticsFile(), FileUtil.getStatisticsFile())) {
                    tvStatistics.setText(Statistics.getText());
                    Toast.makeText(this, "导入成功！", Toast.LENGTH_SHORT).show();
                }
                break;

            case 7:
                String debugData = "file://";
                debugData += FileUtil.getDebugLogFile().getAbsolutePath();
                Intent debugIt = new Intent(this, HtmlViewerActivity.class);
                debugIt.setData(Uri.parse(debugData));
                debugIt.putExtra("canClear", true);
                startActivity(debugIt);
                break;

            case 8:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case 9:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSubTitle(RunType runType) {
        setBaseSubtitle("【" + runType.getName() + "】");
        switch (runType) {
            case DISABLE:
                setBaseSubtitleTextColor(Color.parseColor("#333333"));
                break;
            case MODEL:
                setBaseSubtitleTextColor(getResources().getColor(R.color.textColorPrimary));
                break;
            case PACKAGE:
                setBaseSubtitleTextColor(getResources().getColor(R.color.textColorPrimary));
                break;
        }
    }

}
