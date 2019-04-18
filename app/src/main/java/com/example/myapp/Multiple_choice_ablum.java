package com.example.myapp;

import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapp.adapter.MyAdapter;
import com.example.myapp.entity.PictuerInfor;
import com.example.myapp.ternsorflow.Classifier;
import com.example.myapp.ternsorflow.TensorFlowImageClassifier;
import com.example.myapp.ui.BaseActivity;
import com.scrat.app.selectorlibrary.ImageSelector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import jsc.kit.component.graph.ColumnarItem;
import jsc.kit.component.graph.VerticalColumnarGraphView;
import jsc.kit.component.vscrollscreen.VScrollScreenLayout;

public class Multiple_choice_ablum extends BaseActivity {
    private static final int REQUEST_CODE_SELECT_IMG = 1;
    private static final int MAX_SELECT_COUNT = 9;

    private static final int INPUT_SIZE = 100;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;

    private static final String INPUT_NAME = "x";
    private static final String OUTPUT_NAME = "logits_eval";
    private static final String MODEL_FILE = "file:///android_asset/model/wx.pb";
    private static final String LABEL_FILE = "file:///android_asset/model/imagenet_comp_graph_label_strings.txt";

    private GridView gridView;
    private TextView textView1_0;
    private TextView textView2_1;
    private TextView textView2_2;
    private Executor executor;
    private Classifier classifier;
    VerticalColumnarGraphView verticalColumnarGraphView;
    VScrollScreenLayout vScrollScreenLayout;

    float[] ratios={0,0,0} ;
    String[] values={"0张","0张","0张"};


    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_choice_ablum);
        setTitleBarTitle(getClass().getSimpleName().replace("Multiple_choice_ablum", "相册多张选择结果"));
        initView();

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
            selectImg();
            return false;
        }
    };



    private void initView() {
        gridView=(GridView)findViewById(R.id.lv_grid);
        textView1_0=(TextView)findViewById(R.id.textview1_0);
        textView2_1=(TextView)findViewById(R.id.textview2_1);
        textView2_2=(TextView)findViewById(R.id.textview2_2);
        vScrollScreenLayout=(VScrollScreenLayout)findViewById(R.id.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SELECT_IMG) {
            if(resultCode==RESULT_OK){
                try {
                    showContent(data);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }else{
                finish();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showContent(Intent data) throws IOException {

        List<String> paths = ImageSelector.getImagePaths(data);

        vScrollScreenLayout.setVisibility(View.VISIBLE);
        List<Bitmap>bitmaps=new ArrayList<>();
        String s = null;
        String s1 = null;
        float K = 0,N=0,P=0;
        String []yuansu={"K","N","P"};
        int array_result[]={-1,-1,-1,-1,-1,-1,-1,-1,-1};
        Bitmap croppedBitmap = null;
        int i=0;
        while (i<paths.size()){
            Bitmap bitmap_temp = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(new File(paths.get(i))));
            croppedBitmap = getScaleBitmap(bitmap_temp, 100);
            array_result[i]=startImageClassifier(croppedBitmap);

            Calendar calendar=Calendar.getInstance();  //获取当前时间，作为标识当时多张图片识别。
            save_pic_infor(yuansu[array_result[i]],calendar.getTime().getTime(),"cucumber",paths.get(i));

            bitmaps.add(croppedBitmap);
            i++;
        }

        if (paths.isEmpty()) {
            Toast.makeText(this,"你没有选择照片哦！！！",Toast.LENGTH_SHORT).show();
            return;
        }

        MyAdapter adapter = new MyAdapter(bitmaps, Multiple_choice_ablum.this);
        gridView.setAdapter(adapter);

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

        verticalColumnarGraphView=findViewById(R.id.vertical_columnar_graph);
        verticalColumnarGraphView.setVisibility(View.VISIBLE);
        handlerProvider.sendUIEmptyMessageDelay(0, 350);//显示柱形

        textView2_1.setText("详细结果如下");
        textView2_1.setTextSize(25);

        textView2_2.setText(s1);
        textView2_2.setTextSize(20);
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


    public void selectImg() {
        ImageSelector.show(this, REQUEST_CODE_SELECT_IMG, MAX_SELECT_COUNT);
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



}
