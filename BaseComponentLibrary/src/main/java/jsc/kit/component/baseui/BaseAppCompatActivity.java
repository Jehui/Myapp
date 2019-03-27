package jsc.kit.component.baseui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import jsc.kit.component.baseui.download.DownloadListener;
import jsc.kit.component.baseui.download.FileDownloader;
import jsc.kit.component.baseui.handler.HandlerDelegate;
import jsc.kit.component.baseui.handler.HandlerProvider;
import jsc.kit.component.baseui.permission.PermissionChecker;
import jsc.kit.component.baseui.transition.TransitionDelegate;
import jsc.kit.component.baseui.transition.TransitionProvider;
import jsc.kit.component.utils.FileProviderCompat;
import jsc.kit.component.widget.dialog.LoadingDialog;
public abstract class BaseAppCompatActivity extends AppCompatActivity implements HandlerDelegate, TransitionDelegate, DownloadListener {

    public HandlerProvider handlerProvider;
    public TransitionProvider transitionProvider;
    public PermissionChecker permissionChecker;
    public FileDownloader fileDownloader;

    /**
     * Show full screen or not.
     *
     * @return {@code true}, show full screen, else not.
     */
    protected boolean fullScreen() {
        return false;
    }

    protected void initActionBar(ActionBar actionBar) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initComponent();
        if (fullScreen()) {
            //without ActionBar
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (getSupportActionBar() != null)
                getSupportActionBar().hide();
            //show full screen
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            initActionBar(getSupportActionBar());
        }
    }

    /**
     * Initialize components here.
     */
    @CallSuper
    public void initComponent() {
        handlerProvider = new HandlerProvider(getClass().getSimpleName(), this);
        transitionProvider = new TransitionProvider(this);
        permissionChecker = new PermissionChecker();
        fileDownloader = new FileDownloader(this);
        fileDownloader.setDownloadListener(this);
        transitionProvider.provide(getWindow());
    }

    /**
     * Destroy components here.
     */
    @CallSuper
    public void destroyComponent() {
        if (handlerProvider != null) {
            handlerProvider.destroy();
            handlerProvider = null;
        }
        transitionProvider = null;
        permissionChecker = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionChecker != null)
            permissionChecker.onPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        destroyComponent();
        super.onDestroy();
    }

    protected Dialog createLoadingDialog() {
        return createLoadingDialog("Loading…");
    }

    protected Dialog createLoadingDialog(CharSequence txt) {
        LoadingDialog dialog = new LoadingDialog(this);
        dialog.setMessage(txt);
        dialog.showMessageView(!TextUtils.isEmpty(txt));
        return dialog;
    }

    /**
     * Install application.
     *
     * @param uri apk uri
     */
    public final void installApk(Uri uri) {
        Intent intentInstall = new Intent();
        intentInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentInstall.setAction(Intent.ACTION_VIEW);
        FileProviderCompat.setDataAndType(intentInstall, uri, "application/vnd.android.package-archive", true);
        startActivity(intentInstall);
    }

    public void showPermissionRationaleDialog(String title, String message, String positiveButton, String negativeButton) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        //跳转到当前应用的设置界面
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(negativeButton, null)
                .show();
    }
}
