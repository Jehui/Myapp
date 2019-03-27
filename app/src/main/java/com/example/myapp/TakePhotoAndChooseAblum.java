package com.example.myapp;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import jsc.kit.component.archeaderview.PictureArcHeaderView;
import com.example.myapp.ui.BaseActivity;
import com.example.myapp.ternsorflow.Classifier;
import com.example.myapp.ternsorflow.TensorFlowImageClassifier;

import org.litepal.crud.DataSupport;

public class TakePhotoAndChooseAblum  extends BaseActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private PictureArcHeaderView picture;
    private TextView textview;
    private TextView textview1;
    private TextView textview_potry;
    private Uri pictuer_uri;
    private byte[]images;

    private static final int INPUT_SIZE = 100;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;

    private static final String INPUT_NAME = "x";
    private static final String OUTPUT_NAME = "logits_eval";
    //    private static final String MODEL_FILE = "file:///android_asset/model/tensorflow_inception_graph.pb";
    private static final String MODEL_FILE = "file:///android_asset/model/wx.pb";
    private static final String LABEL_FILE = "file:///android_asset/model/imagenet_comp_graph_label_strings.txt";

    private Classifier classifier;
    private Executor executor;


    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v_scroll_screen_layout);
        //下面的方法是是给页面设置标题，是baseActivity中的一个方法
        setTitleBarTitle(getClass().getSimpleName().replace("TakePhotoAndChooseAblum", "详情"));

        /*
         * 7.0之后你的app就算有权限，给出一个URI之后手机也认为你没有权限，调用摄像头后，直接崩溃。
         * 不用修改原有代码，在Application的oncreate方法中添加以下三行代码。
         * */
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        picture = (PictureArcHeaderView) findViewById(R.id.image_pic);
//        picture1=(PictureArcHeaderView) findViewById(R.id.image_pic1);
        textview=(TextView)findViewById(R.id.textview) ;
        textview1=(TextView)findViewById(R.id.textview1) ;
        textview_potry=(TextView)findViewById(R.id.textview_potry);
        Intent intent=getIntent();
//活动与活动之间是不能传送Uri的，只能将其转化成字符串
        pictuer_uri = Uri.parse(intent.getStringExtra("address"));

//避免耗时任务占用 CPU 时间片造成UI绘制卡顿，提升启动页面加载速度
        Looper.myQueue().addIdleHandler(idleHandler);
    }



    /**
     *  主线程消息队列空闲时（视图第一帧绘制完成时）处理耗时事件
     */
    MessageQueue.IdleHandler idleHandler = new MessageQueue.IdleHandler() {
        @Override
        public boolean queueIdle() {

            if (classifier == null) {
                // 创建 Classifier
                classifier = TensorFlowImageClassifier.create(getResources().getAssets(),
                        MODEL_FILE, LABEL_FILE, INPUT_SIZE, IMAGE_MEAN, IMAGE_STD, INPUT_NAME, OUTPUT_NAME);
            }

            // 初始化线程池
            executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName("ThreadPool-ImageClassifier");
                    return thread;
                }
            });

            try {
                handleInputPhoto(pictuer_uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }
    };


    /**
     * 处理图片
     * @param imageUri
     */
    private void handleInputPhoto(Uri imageUri) throws IOException {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));//讲Uri图像转化成bitmap
            startImageClassifier(bitmap);
            picture.setBitmap(bitmap);//显示图片的第一种方法
            //picture1.setBitmap(bitmap);
    }


    /**
     * 开始图片识别匹配
     * @param bitmap
     */
    private void startImageClassifier(final Bitmap bitmap) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, Thread.currentThread().getName() + " startImageClassifier");
                    final Bitmap croppedBitmap = getScaleBitmap(bitmap, INPUT_SIZE);

                    final List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);
                    Log.i(TAG, "startImageClassifier results: " + results);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (results.get(0).getId()){
                                case "0":
                                    textview.setText(String.format("该黄瓜缺少元素: %s ", results.get(0).getTitle()));
                                    textview_potry.setText(R.string.K);
                                    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                                    textview1.setText(R.string.cucumer);

                                    images=img(croppedBitmap);
                                    save_pic_infor(results.get(0).getTitle(),images,"cucumber");

                                    break;
                                case "1":
                                    textview.setText(String.format("该黄瓜缺少元素: %s  ", results.get(0).getTitle()));
                                    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                                    textview_potry.setText(R.string.N);
                                    textview1.setText(R.string.cucumer);

                                    images=img(croppedBitmap);
                                    save_pic_infor(results.get(0).getTitle(),images,"cucumber");

                                    break;
                                case "2":
                                    textview.setText(String.format("该黄瓜缺少元素: %s  ", results.get(0).getTitle()));
                                    textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                                    textview_potry.setText(R.string.P);
                                    textview1.setText(R.string.cucumer);

                                    images=img(croppedBitmap);
                                    save_pic_infor(results.get(0).getTitle(),images,"cucumber");

                                    break;

                        }
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "startImageClassifier getScaleBitmap " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 对图片进行缩放
     * @param bitmap
     * @param size
     * @return
     * @throws IOException
     */
    private static Bitmap getScaleBitmap(Bitmap bitmap, int size) throws IOException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) size) / width;
        float scaleHeight = ((float) size) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }
//将图片转化成二进制数组，存储在数据库中
    private byte[]img(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();

    }


    private void save_pic_infor(String flag,byte[]images,String name){

        Calendar calendar=Calendar.getInstance();  //获取当前时间，作为图标的名字
        String year=calendar.get(Calendar.YEAR)+".";
        String month=calendar.get(Calendar.MONTH)+1+".";
        String day=calendar.get(Calendar.DAY_OF_MONTH)+"";
        String time=year+month+day;

        PictuerInfor pictuerinfor=new PictuerInfor();
        pictuerinfor.setImages(images);//图片转化成二进制保存
        pictuerinfor.setName(name);//名字
        pictuerinfor.setFlag(flag);//缺少元素名字
        pictuerinfor.setImageUri(pictuer_uri.toString());//Uri路径
        pictuerinfor.setTime(time);//拍照时间
        pictuerinfor.save();


    }

}
