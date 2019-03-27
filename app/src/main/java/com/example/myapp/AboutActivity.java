package com.example.myapp;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;
import com.example.myapp.ui.BaseActivity;


public class AboutActivity extends BaseActivity {

    TextView tvVersion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitleBarTitle(getClass().getSimpleName().replace("AboutActivity", "版本信息"));
        tvVersion = findViewById(R.id.tv_version);
        showVersion();
    }

    private void showVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            tvVersion.setText("version:" + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
