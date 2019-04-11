package com.example.myapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.example.myapp.entity.Banner;
import com.example.myapp.entity.PictuerInfor;
import com.example.myapp.entity.VersionEntity;
import com.example.myapp.service.ApiService;
import com.example.myapp.ui.BaseActivity;
import com.example.myapp.ui.ComponentsActivity;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jsc.kit.component.archeaderview.LGradientArcHeaderView;
import jsc.kit.component.bannerview.BannerPagerAdapter;
import jsc.kit.component.bannerview.JSCBannerView;
import jsc.kit.component.bannerview.OnCreateIndicatorViewListener;
import jsc.kit.component.bannerview.OnPageAdapterItemClickListener;
import jsc.kit.component.bannerview.PageAdapterItemLifeCycle;
import jsc.kit.component.bannerview.pageTransformer.ScaleTransformer;
import jsc.kit.component.baseui.download.DownloadEntity;
import jsc.kit.component.baseui.permission.PermissionChecker;
import jsc.kit.component.utils.CompatResourceUtils;
import jsc.kit.component.utils.CustomToast;
import jsc.kit.retrofit2.LoadingDialogObserver;
import jsc.kit.retrofit2.retrofit.CustomHttpClient;
import jsc.kit.retrofit2.retrofit.CustomRetrofit;
import okhttp3.OkHttpClient;

public class MainActivity extends BaseActivity  implements View.OnClickListener  {

    JSCBannerView jscBannerView;
    private Button display;
    private Button histories;
    private Button update;
    private Button information;
    private Button cleardatas;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//Android7以后，读取文件的路径的权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        setTitleBarTitle(getClass().getSimpleName().replace("MainActivity", "作物缺素检测系统"));

        jscBannerView = findViewById(R.id.banner_view);
        display=findViewById(R.id.takephoto);
        histories=findViewById(R.id.histories);
        update=findViewById(R.id.update);
        information=findViewById(R.id.information);
        cleardatas=findViewById(R.id.cleardatas);

        display.setOnClickListener(this);
        histories.setOnClickListener(this);
//        update.setOnClickListener(this);
        information.setOnClickListener(this);
        cleardatas.setOnClickListener(this);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadVersionInfo();
            }
        });

        LGradientArcHeaderView backgroundView = new LGradientArcHeaderView(this);//弧形控件
       // backgroundView.setArcHeight(100);//设置高度
      //  backgroundView.setColors(0xFF00BA86, 0x2200BA86);////设置渐变颜色
       jscBannerView.setBackgroundView(backgroundView,
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));

        Connector.getDatabase();//创建数据库
        wangxiong();
    }

    //点击拍照按钮，使用ShowDialog中的私有方法展示底下弹出菜单的功能

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.takephoto:
                Intent intent=new Intent(MainActivity.this, ComponentsActivity.class);
                startActivity(intent);
                break;
            case R.id.histories:
                Intent intent1=new Intent(MainActivity.this, History.class);
                startActivity(intent1);
                break;
//            case R.id.update:
//                new Update().loadVersionInfo();
//                break;
            case R.id.cleardatas:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle("提示");
                builder.setMessage("\n确定要清空数据库吗？");
                builder.setCancelable(true);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                         *  在这里实现你自己的业务逻辑
                         */
                        DataSupport.deleteAll(PictuerInfor.class);
                        Toast.makeText(getBaseContext(), "已清空数据库！", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                         *  在这里实现你自己的逻辑
                         */
//                        Toast.makeText(getBaseContext(), "未执行此操作", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.create().show();

                break;
            case R.id.information:
                Intent intent2=new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent2);
                break;
        }
    }

    private void wangxiong() {
        jscBannerView.getChildAt(0).setVisibility(View.INVISIBLE);
        jscBannerView.setClipChildren(false);
        /*
        * android:clipChildren是否允许子View超出父View的返回，有两个值true 、false ，默认true。
          使用的时候给子View的父View设置此属性为false，那么这个子View就不会限制在父View当中。
        * */
        jscBannerView.getViewPager().setClipChildren(false);
//        jscBannerView.getViewPager().setPageMargin(20);//设置ViewPager页间距
        jscBannerView.getViewPager().setOffscreenPageLimit(3);//设置预加载的页数
        jscBannerView.setPageTransformer(true, new ScaleTransformer());// 然后把这个实现对象设置到viewpager里

        List<Banner> banners = new ArrayList<>();
        banners.add(new Banner("img/2.jpg"));
        banners.add(new Banner("img/1.jpg"));
        banners.add(new Banner("img/3.jpg"));
        banners.add(new Banner("img/4.jpg"));
        banners.add(new Banner("img/5.jpg"));
        banners.add(new Banner("img/6.jpg"));

        BannerPagerAdapter<Banner> adapter = new BannerPagerAdapter<>(true);
        adapter.setOnPageAdapterItemClickListener(new OnPageAdapterItemClickListener<Banner>() {
            @Override
            public void onPageAdapterItemClick(View view, Banner item) {
                Toast.makeText(view.getContext(), item.getUrl(), Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setPageAdapterItemLifeCycle(new PageAdapterItemLifeCycle<Banner>() {
            @NonNull
            @Override
            public View onInstantiateItem(ViewGroup container, Banner item) {
                FrameLayout layout = new FrameLayout(container.getContext());
                ImageView imageView = new ImageView(container.getContext());
                //ImageView的Scaletype决定了图片在View上显示时的样子，如进行何种比例的缩放，及显示图片的整体还是部分，等等。
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                layout.addView(imageView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                Glide.with(container.getContext())//使用Application上下文，Glide请求将不受Activity/Fragment生命周期控制。
                        .load("file:///android_asset/" + item.getUrl())
                        .into(imageView);
                return layout;
            }
        /*
        * //主要功能实现图片加载功能至少需要三个参数：
        //with(Context context)
        //Context对于很多Android API的调用都是必须的，这里就不多说了
        //load(String imageUrl)：被加载图像的Url地址。
        //大多情况下，一个字符串代表一个网络图片的URL。
        //into(ImageView targetImageView)：图片最终要展示的地方。
        * */

            @Override
            public boolean onDestroyItem(ViewGroup container, Object object) {
                return false;
            }
        });
        adapter.setOnCreateIndicatorViewListener(new OnCreateIndicatorViewListener<Banner>() {
            @Override
            public View onCreateIndicatorView(Context context, int index, Banner item) {
                return null;
            }
        });
        jscBannerView.setAdapter(adapter);
        adapter.setBannerItems(banners);
    }


/**
 * 以下的方法都是为了更新APP
 */
    //网络读取文件output.json的内容：
    public void loadVersionInfo() {
        OkHttpClient client = new CustomHttpClient()
                .setConnectTimeout(5_0000)
                .setShowLog(true)
                .createOkHttpClient();
        new CustomRetrofit()
                //我在app的build.gradle文件的defaultConfig标签里定义了BASE_URL
                .setBaseUrl("https://raw.githubusercontent.com/")
                .setOkHttpClient(client)
                .createRetrofit()
                .create(ApiService.class)
                .getVersionInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LoadingDialogObserver<String>(createLoadingDialog()) {
                    @Override
                    public void onStart(Disposable disposable) {

                    }

                    @RequiresApi(api = Build.VERSION_CODES.P)
                    @Override
                    public void onResult(String s) {
                        s = s.substring(1, s.length() - 1);
                        VersionEntity entity = VersionEntity.fromJson(s);
                        showUpdateTipsDialog(entity);
                    }

                    @Override
                    public void onException(Throwable e) {

                    }

                    @Override
                    public void onCompleteOrCancel(Disposable disposable) {

                    }
                });
    }


    //比较最新版本与本地版本：如果版本最新的versionCode大于本地版本的versionCode，弹窗提示。
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void showUpdateTipsDialog(final VersionEntity entity) {
        if (entity == null) {
            showCustomToast("Failed to access server.");
            Toast.makeText(getBaseContext(), "Failed to access server！", Toast.LENGTH_SHORT).show();
            return;
        }

        long curVersionCode = 0;
        String curVersionName = "";

        if (Build.VERSION.SDK_INT >= 26){
            try {
                PackageManager manager = getPackageManager();
                PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
                curVersionCode = info.getLongVersionCode();
                curVersionName = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                showCustomToast("Failed to access server.");
                Toast.makeText(getBaseContext(), "Failed to access server！", Toast.LENGTH_SHORT).show();
            }
            catch (NoSuchMethodError e){
                Toast.makeText(getBaseContext(), "NoSuchMethodError！", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }else{
            try {
                PackageManager manager = getPackageManager();
                PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
                curVersionCode = info.versionCode;
                curVersionName = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                showCustomToast("Failed to access server.");
                Toast.makeText(getBaseContext(), "Failed to access server！", Toast.LENGTH_SHORT).show();
            }
        }


        if (curVersionCode > 0 && entity.getApkInfo().getVersionCode() > curVersionCode)
            new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle("更新提示")
                    .setMessage("1、当前版本：" + curVersionName + "\n2、最新版本：" + entity.getApkInfo().getVersionName())
                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkPermissionBeforeDownloadApk(entity.getApkInfo().getVersionName());
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        else
//            showCustomToast("This is latest version.");
//        Log.d(MainActivity.class.getSimpleName(),curVersionCode+"   github版本："+entity.getApkInfo().getVersionCode());
            Toast.makeText(getBaseContext(), "当前是最新版本！", Toast.LENGTH_SHORT).show();
    }


    //有新版本，我们下载新版本：这里主要用系统自带的DownloadManager下载文件，我的库中已经封装好了
    private void checkPermissionBeforeDownloadApk(final String versionName) {
        permissionChecker.checkPermissions(this, 0, new PermissionChecker.OnPermissionCheckListener() {
            @Override
            public void onResult(int requestCode, boolean isAllGranted, @NonNull List<String> grantedPermissions, @Nullable List<String> deniedPermissions, @Nullable List<String> shouldShowPermissions) {
                if (isAllGranted) {
                    downloadApk(versionName);
                    return;
                }

                if (shouldShowPermissions != null && shouldShowPermissions.size() > 0) {
                    String message = "当前应用需要以下权限:\n\n" + PermissionChecker.getAllPermissionDes(getBaseContext(), shouldShowPermissions);
                    showPermissionRationaleDialog("温馨提示", message, "设置", "知道了");
                }
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void downloadApk(String versionName) {
        DownloadEntity entity = new DownloadEntity();
        entity.setUrl("https://raw.githubusercontent.com/wangbuer1/Myapp/master/app/release/app-release.apk");
//        entity.setUrl("https://dldir1.qq.com/weixin/Windows/WeChatSetup.exe");
        entity.setDestinationDirectory(new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS));
        entity.setSubPath("王雄/植物缺素检测系统" + versionName + ".apk");
        entity.setTitle("作物缺素检测系统" + versionName + ".apk");
        entity.setDesc("WangXiong Library");
        entity.setMimeType("application/vnd.android.package-archive");
        fileDownloader.registerDownloadCompleteReceiver();
        fileDownloader.downloadFile(entity);


    }

    //安装下载好的apk：
    @Override
    public void onDownloadCompleted(Uri uri) {
        fileDownloader.unRegisterDownloadCompleteReceiver();
        if (uri == null)
            return;
        //8.0有未知应用安装请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //先获取是否有安装未知来源应用的权限
            if (getPackageManager().canRequestPackageInstalls()) {

                installApk(uri);

            } else {
                requestInstallPackages(uri);
            }
        } else {

            installApk(uri);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestInstallPackages(final Uri uri) {
        permissionChecker.checkPermissions(this, 0, new PermissionChecker.OnPermissionCheckListener() {
            @Override
            public void onResult(int requestCode, boolean isAllGranted, @NonNull List<String> grantedPermissions, @Nullable List<String> deniedPermissions, @Nullable List<String> shouldShowPermissions) {
                if (isAllGranted) {
                    installApk(uri);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
                    }
                }
            }
        }, Manifest.permission.REQUEST_INSTALL_PACKAGES);
    }

    private final void showCustomToast(CharSequence txt) {
        new CustomToast.Builder(this)
                .setText(txt)
                .setBackgroundColor(CompatResourceUtils.getColor(this, R.color.colorAccent))
                .setTextColor(Color.WHITE)
                .show();
    }

}