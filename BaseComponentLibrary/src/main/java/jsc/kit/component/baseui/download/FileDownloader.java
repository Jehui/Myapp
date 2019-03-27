package jsc.kit.component.baseui.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import jsc.kit.component.utils.FileProviderCompat;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * <br><a href="https://github.com/JustinRoom/JSCKit" target="_blank">https://github.com/JustinRoom/JSCKit</a>
 *
 * @author jiangshicheng
 */
public final class FileDownloader {

    private Context context;
    private DownloadListener downloadListener;
    private BroadcastReceiver downloadReceiver;
    private boolean isRegisterDownloadReceiver;

    public FileDownloader(@NonNull Context context) {
        this.context = context;
    }

    public void setDownloadListener(DownloadListener DownloadListener) {
        this.downloadListener = DownloadListener;
    }

    /**
     * 注册下载完成监听
     */
    public void registerDownloadCompleteReceiver() {
        if (isRegisterDownloadReceiver)
            return;

        if (downloadReceiver == null)
            downloadReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                        findDownloadFileUri(downloadId);

                    } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())) {
                        context.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                    }
                }
            };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        context.registerReceiver(downloadReceiver, intentFilter);
        isRegisterDownloadReceiver = true;
    }

    /**
     * 注销下载完成监听
     */
    public void unRegisterDownloadCompleteReceiver() {
        Log.d(FileDownloader.class.getSimpleName(),"定位5");
        if (downloadReceiver != null && isRegisterDownloadReceiver) {
            context.unregisterReceiver(downloadReceiver);
            downloadReceiver = null;
            isRegisterDownloadReceiver = false;
        }
    }

    /**
     * Download file.
     * <br>If {@link DownloadEntity#destinationDirectory} is null, it will be downloaded into specific folder.
     * <br>The specific path is: {@code request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, subPath);}.
     * <br>see {@link DownloadManager.Request#setDestinationInExternalFilesDir(Context, String, String)}
     *
     * @param downloadEntity download config entity
     * @return download id
     */
    public long downloadFile(DownloadEntity downloadEntity) {
        Log.d(FileDownloader.class.getSimpleName(),"定位6");
        String url = downloadEntity.getUrl();
        if (TextUtils.isEmpty(url))
            return -1;

        Uri uri = Uri.parse(url);
        String subPath = downloadEntity.getSubPath();
        if (subPath == null || subPath.trim().length() == 0) {
            subPath = uri.getLastPathSegment();
        }

        File destinationDirectory = downloadEntity.getDestinationDirectory();
        if (destinationDirectory == null) {
            destinationDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        }

        File file = new File(destinationDirectory, subPath);
        File directory = file.getParentFile();
        if (!directory.exists()) {//创建文件保存目录
            directory.mkdirs();
        }

        if (file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        DownloadManager.Request request = new DownloadManager.Request(uri);
        //设置title
        request.setTitle(downloadEntity.getTitle());
        // 设置描述
        request.setDescription(downloadEntity.getDesc());
        // 完成后显示通知栏
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //
        Uri destinationUri = Uri.withAppendedPath(Uri.fromFile(destinationDirectory), subPath);
//        Uri destinationUri = FileProviderCompat.getUriForFile(this, file);
        request.setDestinationUri(destinationUri);
//        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, subPath);
        request.setMimeType(downloadEntity.getMimeType());
        request.setVisibleInDownloadsUi(true);

        DownloadManager mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        return mDownloadManager == null ? -1 : mDownloadManager.enqueue(request);
    }


    /**
     * Get uri by download id.
     *
     * @param completeDownLoadId download id
     */
    public void findDownloadFileUri(long completeDownLoadId) {
        Uri uri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // 6.0以下
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            assert downloadManager != null;
            uri = downloadManager.getUriForDownloadedFile(completeDownLoadId);
        } else {

            File file = queryDownloadedFile(completeDownLoadId);
            //android7以上获取路径的方法
            Log.d(FileDownloader.class.getSimpleName(), "我是大人"+String.valueOf(file));

            uri = FileProvider.getUriForFile(context, "com.example.myapp.fileProvider", file);
//            uri = FileProviderCompat.getUriForFile(context, file);
            Log.d(FileDownloader.class.getSimpleName(),"定位x");
        }

        if (downloadListener != null){
            downloadListener.onDownloadCompleted(uri);
        }
    }

    public File queryDownloadedFile(long downloadId) {

        File targetFile = null;
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadId != -1) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
            assert downloadManager != null;
            Cursor cur = downloadManager.query(query);
            if (cur != null) {
                Log.d(FileDownloader.class.getSimpleName(), "我是大人9");
                if (cur.moveToFirst()) {
                    Log.d(FileDownloader.class.getSimpleName(), "我是大人2");
                    String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    Log.d(FileDownloader.class.getSimpleName(), "我是大人1"+uriString);
                    if (!TextUtils.isEmpty(uriString)) {
                        targetFile = new File(Uri.parse(uriString).getPath());
                    }
                }
                cur.close();
            }
        }
        return targetFile;
    }



}
