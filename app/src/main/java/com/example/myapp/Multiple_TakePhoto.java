package com.example.myapp;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.adapter.MyAdapter;
import com.example.myapp.entity.MyBitmap;
import com.example.myapp.entity.PictuerInfor;
import com.example.myapp.ternsorflow.Classifier;
import com.example.myapp.ternsorflow.TensorFlowImageClassifier;
import com.example.myapp.ui.BaseActivity;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import jsc.kit.component.graph.ColumnarItem;
import jsc.kit.component.graph.VerticalColumnarGraphView;
import jsc.kit.component.vscrollscreen.VScrollScreenLayout;
import rx.functions.Action1;

public class Multiple_TakePhoto extends BaseActivity {

    private static final int INPUT_SIZE = 100;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;

    private static final String INPUT_NAME = "x";
    private static final String OUTPUT_NAME = "logits_eval";
    private static final String MODEL_FILE = "file:///android_asset/model/wx.pb";
    private static final String LABEL_FILE = "file:///android_asset/model/imagenet_comp_graph_label_strings.txt";

    private Button bt;
    private List<Map<String, Bitmap>> list;
    private RxPermissions mRxPermissions;
    private int mYear;
    private int mMonth;
    private int mDay;
    private int REQUEST_SMALL = 111;
    private Calendar calendar;
    private long systemTime1;
    private long systemTime2;
    private GridView gridView;
    private View pb;
    private VScrollScreenLayout vScrollScreenLayout;
    private VerticalColumnarGraphView verticalColumnarGraphView;
    private TextView textView1_0;
    private TextView textView2_1;
    private TextView textView2_2;
    private Executor executor;
    private Classifier classifier;
    float[] ratios={0,0,0} ;
    String[] values={"0张","0张","0张"};

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple__take_photo);
        setTitleBarTitle(getClass().getSimpleName().replace("Multiple_TakePhoto", "多张拍摄检测结果"));
        mRxPermissions = new RxPermissions(this);
        calendar = Calendar.getInstance();
        pb = findViewById(R.id.pb);
        gridView = ((GridView) findViewById(R.id.lv_grid));
        textView1_0=(TextView)findViewById(R.id.textview1_0);
        textView2_1=(TextView)findViewById(R.id.textview2_1);
        textView2_2=(TextView)findViewById(R.id.textview2_2);
        vScrollScreenLayout=(VScrollScreenLayout)findViewById(R.id.activity_main);
        verticalColumnarGraphView=findViewById(R.id.vertical_columnar_graph);

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
            takeOnCamera();
            return false;
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("data", "onActivityResult: " + data);
        //关闭相机之后获得时间；2；
        pb.setVisibility(View.VISIBLE);
        systemTime2 = getSystemTime();
        Log.i(Multiple_TakePhoto.class.getName(),"wodeceshi"+resultCode);
        if (requestCode == REQUEST_SMALL) {
            getContactList();
//       resultCode的值不拍照成功与否，一直等于0，原因在于Android系统版本问题，在4.4以后的版本，在拍照时候需要利用Intent控件拍照完毕保存指定的URI
//        等操作，详情可以看TakephotoOrAblum.java文件中拍照功能，或者网址https://blog.csdn.net/lepaitianshi/article/details/82837748
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void getContactList() {
        mRxPermissions.requestEach(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Action1<Permission>() {
                    @Override
                    public void call(Permission permission) {
                        if (permission.granted) {
                            final List<Bitmap>bitmaps=new ArrayList<>();
                            final List<String> paths=new ArrayList<>();
                            //  读取照片然后选择合适的照片保存再list里面
                            final String[] projection = {MediaStore.Images.Media._ID,
                                    MediaStore.Images.Media.DISPLAY_NAME,
                                    MediaStore.Images.Media.DATA};
                            //这个MediaStore.Images.Media.DATA就是要查找哪些字段的意思，所以他是个字符串数组，一个Uri里面包含好多信息，比如图片的大小，修改时间，文件名等等
                            final String orderBy = MediaStore.Images.Media.DISPLAY_NAME;
                            final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    List<MyBitmap> list2 = getContentProvider(uri, projection, orderBy);//到时候抽取接口
                                    if(list2.isEmpty())finish();
                                    Log.e("list", "call: " + list2.toString() + ".size" + list2.size());
                                    if (list2 != null) {
                                        if (list2.size() > 9) {//这里看要求最多几张照片
                                            list2 = list2.subList(list2.size() - 9, list2.size());
                                        }
                                        final List<MyBitmap> finalList = list2;
                                        int i=0;
                                        Log.e("测试", "call: " +finalList.size());
                                        while(i<finalList.size()){
                                            bitmaps.add(finalList.get(i).getBm());
                                            paths.add(finalList.get(i).getPath());
                                            i++;
                                        }
                                        gridView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                //TODO 拿到数据源然后拓展
                                                vScrollScreenLayout.setVisibility(View.VISIBLE);

                                                String s = null;
                                                String s1 = null;
                                                float K = 0,N=0,P=0;
                                                String []yuansu={"K","N","P"};
                                                int array_result[]={-1,-1,-1,-1,-1,-1,-1,-1,-1};
                                                int tempcount=0;
                                                while (tempcount<bitmaps.size()){
                                                    array_result[tempcount]=startImageClassifier(bitmaps.get(tempcount));
                                                    Calendar calendar=Calendar.getInstance();  //获取当前时间，作为标识当时多张图片识别。
                                                    save_pic_infor(yuansu[array_result[tempcount]],calendar.getTime().getTime(),"cucumber",paths.get(tempcount));
                                                    tempcount++;
                                                }
                                                MyAdapter adapter = new MyAdapter(bitmaps, Multiple_TakePhoto.this);
                                                gridView.setAdapter(adapter);
                                                pb.setVisibility(View.GONE);

                                                for(int j=0;j<9&&array_result[j]!=-1;j++) {
                                                    switch (array_result[j]) {
                                                        case 0:
                                                            K++;
                                                            break;
                                                        case 1:
                                                            N++;
                                                            break;
                                                        case 2:
                                                            P++;
                                                            break;
                                                    }
                                                }
                                                ratios[0]=K/(K+N+P);
                                                ratios[0]= (float) (Math.round( ratios[0]*100)/100.0);
                                                ratios[1]=N/(K+N+P);
                                                ratios[1]= (float) (Math.round( ratios[1]*100)/100.0);
                                                ratios[2]=P/(K+N+P);
                                                ratios[2]= (float) (Math.round( ratios[2]*100)/100.0);
                                                values[0]=(int)K+"张";
                                                values[1]=(int)N+"张";
                                                values[2]=(int)P+"张";

                                                for(int j=0;j<9&&array_result[j]!=-1;j++){
                                                    switch (array_result[j]){
                                                        case 0:
                                                            s="第"+(j+1)+"张图片缺乏元素：K"+"\n";
                                                            break;
                                                        case 1:
                                                            s="第"+(j+1)+"张图片缺乏元素：N"+"\n";
                                                            break;
                                                        case 2:
                                                            s="第"+(j+1)+"张图片缺乏元素：P"+"\n";
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                    if(s1==null){
                                                        s1=s;
                                                    }else{
                                                        s1=s1+s;
                                                    }
                                                }

                                                textView1_0.setText("检测结果");
                                                textView1_0.setTextSize(24);

                                                verticalColumnarGraphView.setVisibility(View.VISIBLE);
                                                handlerProvider.sendUIEmptyMessageDelay(0, 350);//显示柱形

                                                textView2_1.setText("详细结果如下");
                                                textView2_1.setTextSize(25);

                                                textView2_2.setText(s1);
                                                textView2_2.setTextSize(20);
                                            }
                                        });

                                    }
                                }
                            }).start();

                        } else if (permission.shouldShowRequestPermissionRationale) {
                            //拒绝
                            Toast.makeText(Multiple_TakePhoto.this, "您拒绝了读取照片的权限", Toast.LENGTH_SHORT).show();

                        } else {
                            // gotoSetting();
                            Toast.makeText(Multiple_TakePhoto.this, "您拒绝了读取照片的权限", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    /**
     * 获取ContentProvider
     *
     * @param projection
     * @param orderBy
     */
    public List<MyBitmap> getContentProvider(Uri uri, String[] projection, String orderBy) {
        // TODO Auto-generated method stub

        List<MyBitmap> lists = new ArrayList<MyBitmap>();
        HashSet<String> set = new HashSet<String>();
        Cursor cursor = getContentResolver().query(uri, projection, null,
                null, orderBy);
        if (null == cursor) {
            return null;
        }

        while (cursor.moveToNext()) {
            Log.e("lengthpro", "getContentProvider: " + projection.length);
            for (int i = 0; i < projection.length; i++) {
                String string = cursor.getString(i);////返回当前行指定列的值，也就是图片的存储路径
                //    string=/storage/emulated/0/DCIM/Camera/IMG_20170413_165659.jpg
//                ss=IMG_20170413_165659.jpg
                if (string != null) {
                    int length = string.length();
                    String ss = null;
                    if (length >= 30) {//根据实际路径得到的。大一点保险
                        ss = string.substring(length - 23, length);
                        String substring = ss.substring(0, 4);//大致判断一下是系统图片，后面严格塞选
                        String hen = ss.substring(12, 13);
                        if (substring.equals("IMG_") && hen.equals("_")) {
                            //以下一行代码意思是4-19之间的字符串中的“_”用“ ”代替，目的是为了忽的拍摄时间
                            String laststring = ss.substring(4, 19).replace("_", "");
                            Log.i(MainActivity.class.getName(),"测试"+laststring);
                            try {
//                                Long.valueOf(这里有参数)，是将参数转换成long的包装类——Long。
//                                longValue()是Long类的一个方法，用来得到Long类中的数值。
//                                前者是将基本数据类型转换成包装类
//                                后者是将包装类中的数据拆箱成基本数据类型
                                long time = Long.valueOf(laststring).longValue();
                                if (time > systemTime1 && time <= systemTime2) {
                                    set.add(string);
                                }
                            } catch (Exception e) {
                                Log.e("exception", "getContentProvider: " + e.toString());
                            }
                        }
                    }
                }
            }
        }

        for (String strings : set) {
            Log.e("setsize", "getContentProvider: " + strings);
            try {
                Bitmap bitmap = convertToBitmap(strings, 100, 100);

                MyBitmap myBitmap = new MyBitmap(strings, bitmap);
                lists.add(myBitmap);
            } catch (Exception e) {
                Log.e("exceptionee", "getSystemTime: " + e.toString());

            }

        }

        return lists;
    }

    public void takeOnCamera() {
        //打开相机之前，记录时间1
        systemTime1 = getSystemTime();
        Intent intent = new Intent();
        //此处之所以诸多try catch，是因为各大厂商手机不确定哪个方法
        try {
            intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            startActivityForResult(intent, REQUEST_SMALL);
        } catch (Exception e) {
            try {
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                startActivityForResult(intent, REQUEST_SMALL);

            } catch (Exception e1) {
                try {
                    intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
                    startActivityForResult(intent, REQUEST_SMALL);
                } catch (Exception ell) {
                    Toast.makeText(Multiple_TakePhoto.this, "请从相册选择", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    public long getSystemTime() {
//("yyyy年MM月dd日 HH时MM分ss秒"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        long times = System.currentTimeMillis();
        System.out.println(times);
        Date date = new Date(times);
        String time = sdf.format(date).toString();
        Log.e("timeintimet", "timeint: " + time.toString());
        long timeint = 0;
        try {
            ;
            timeint = Long.valueOf(time).longValue();

        } catch (Exception e) {
            Log.e("exception", "getSystemTime: " + e.toString());
        }


        return timeint;
    }

    /**
     * 根据路径，二次采样并且压缩
     * @param filePath 路径
     * @param destWidth 压缩到的宽度
     * @param destHeight 压缩到的高度
     * @return
     */
    public Bitmap convertToBitmap(String filePath, int destWidth, int destHeight) {
        //第一采样
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int sampleSize = 1;
        while ((outWidth / sampleSize > destWidth) || (outHeight / sampleSize > destHeight)) {

            sampleSize *= 2;
        }
        //第二次采样
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(filePath, options);
    }

    private void save_pic_infor(String flag,long setMultiple_time_flag,String name,String uri){
        Calendar calendar=Calendar.getInstance();  //获取当前时间，作为图标的名字
        String year=calendar.get(Calendar.YEAR)+".";
        String month=calendar.get(Calendar.MONTH)+1+".";
        String day=calendar.get(Calendar.DAY_OF_MONTH)+"";
        String time=year+month+day;

        PictuerInfor pictuerinfor=new PictuerInfor();
        pictuerinfor.setName(name);//名字
        pictuerinfor.setFlag(flag);//缺少元素名字
        pictuerinfor.setImageUri("file://"+uri);//Uri路径
        pictuerinfor.setTime(time);//拍照时间
        pictuerinfor.setTab(2);//多张还是单张识别
        pictuerinfor.setMultiple_time_flag(setMultiple_time_flag);
        pictuerinfor.save();


    }

    @Override
    public void handleUIMessage(Message msg) {
        super.handleUIMessage(msg);
        verticalColumnarGraphView.initCustomUI(
                new VerticalColumnarGraphView.Builder()
                        .setYAxisLabels(new String[]{"\u20000", "25", "50", "75", "100"})
                        .setXAxisLabels(new String[]{"\u20000", "钾", "氮", "磷"," "})
                        .setOffset(60, 0, 20, 60)
        );
        verticalColumnarGraphView.setItems(createTestData());
    }

    /**
     * 数据
     *
     * @return test data source
     */
    private List<ColumnarItem> createTestData() {
        List<ColumnarItem> data = new ArrayList<>();
        int[] colors = {0xFFFFCF5E, 0xFFB4EE4D, 0xFF27E67B};
        String[] labels = {"K", "N", "P"};
        for (int i = 0; i < 3; i++) {
            ColumnarItem item = new ColumnarItem();
            item.setColor(colors[i]);
            item.setRatio(ratios[i]);
            item.setLabel(labels[i]);
            item.setValue(values[i]);
            data.add(item);
        }
        return data;
    }

    /**
     * 开始图片识别匹配
     * @param bitmap
     */
    private int  startImageClassifier(final Bitmap bitmap) {
        List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//        List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
//            }
//        });
        return Integer.parseInt(results.get(0).getId());
    }

}
