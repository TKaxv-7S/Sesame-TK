package tkaxv7s.xposed.sesame.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ViewAppInfo;
import tkaxv7s.xposed.sesame.util.FileUtil;
import tkaxv7s.xposed.sesame.util.LanguageUtil;

import java.io.File;

public class HtmlViewerActivity extends Activity {
    MyWebView mWebView;
    ProgressBar pgb;
    Uri uri;
    Boolean canClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageUtil.setLocale(this);
        setContentView(R.layout.activity_html_viewer);

        mWebView = findViewById(R.id.mwv_webview);
        pgb = findViewById(R.id.pgb_webview);

        mWebView.setWebChromeClient(
                new WebChromeClient() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onProgressChanged(WebView view, int progress) {
                        pgb.setProgress(progress);
                        if (progress < 100) {
                            setTitle("Loading...");
                            pgb.setVisibility(View.VISIBLE);
                        } else {
                            setTitle(ViewAppInfo.getAppTitle() + " " + mWebView.getTitle());
                            pgb.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        uri = intent.getData();
        if (uri != null) {
            mWebView.loadUrl(uri.toString());
        }
        canClear = intent.getBooleanExtra("canClear", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, getString(R.string.export_file));
        if (canClear) {
            menu.add(0, 2, 2, getString(R.string.clear_file));
        }
        menu.add(0, 3, 3, getString(R.string.open_with_other_browser));
        menu.add(0, 4, 4, getString(R.string.copy_the_url));
        menu.add(0, 5, 5, getString(R.string.scroll_to_top));
        menu.add(0, 6, 6, getString(R.string.scroll_to_bottom));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                if (uri != null) {
                    String path = uri.getPath();
                    if (path != null) {
                        File exportFile = FileUtil.exportFile(new File(path));
                        if (exportFile != null) {
                            Toast.makeText(this, "文件已导出到: " + exportFile.getPath(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;

            case 2:
                if (uri != null) {
                    String path = uri.getPath();
                    if (path != null) {
                        File file = new File(path);
                        if (FileUtil.clearFile(file)) {
                            android.widget.Toast.makeText(this, "文件已清空", android.widget.Toast.LENGTH_SHORT).show();
                            mWebView.reload();
                        }
                    }
                }
                break;

            case 3:
                if (uri != null) {
                    String scheme = uri.getScheme();
                    if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } else if ("file".equalsIgnoreCase(scheme)) {
                        Toast.makeText(this, "该文件不支持用浏览器打开", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "不支持用浏览器打开", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case 4:
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText(null, mWebView.getUrl()));
                Toast.makeText(this, getString(R.string.copy_success), Toast.LENGTH_SHORT).show();
                break;

            case 5:
                mWebView.scrollTo(0, 0);
                break;

            case 6:
                mWebView.scrollToBottom();
                break;
        }
        return true;
    }
}
