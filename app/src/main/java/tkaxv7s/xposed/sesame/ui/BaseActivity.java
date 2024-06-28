package tkaxv7s.xposed.sesame.ui;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ViewAppInfo;

public class BaseActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ViewAppInfo.init(getApplicationContext());
        toolbar = findViewById(R.id.x_toolbar);
        toolbar.setTitle(getBaseTitle());
        toolbar.setSubtitle(getBaseSubtitle());
        setSupportActionBar(toolbar);
    }

    public String getBaseTitle() {
        return ViewAppInfo.getAppTitle();
    }

    public String getBaseSubtitle() {
        return null;
    }

    public void setBaseTitle(String title) {
        toolbar.setTitle(title);
    }

    public void setBaseSubtitle(String subTitle) {
        toolbar.setSubtitle(subTitle);
    }

    public void setBaseTitleTextColor(@ColorInt int color) {
        toolbar.setTitleTextColor(color);
    }

    public void setBaseSubtitleTextColor(@ColorInt int color) {
        toolbar.setSubtitleTextColor(color);
    }

}
