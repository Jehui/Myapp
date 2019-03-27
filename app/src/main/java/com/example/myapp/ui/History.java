package com.example.myapp.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.HistoryToPictuerinformation;
import com.example.myapp.MainActivity;
import com.example.myapp.PictuerInfor;
import org.litepal.crud.DataSupport;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import jsc.kit.component.stepview.VerticalStepLinearLayout;
import jsc.kit.component.utils.CompatResourceUtils;
import jsc.kit.component.utils.dynamicdrawable.DynamicDrawableFactory;
import static com.example.myapp.R.*;

public class History extends BaseActivity {
    String[][]data;
    VerticalStepLinearLayout stepLinearLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleBarTitle(getClass().getSimpleName().replace("History", "我的记录"));
        int space = CompatResourceUtils.getDimensionPixelSize(this, dimen.space_16);
        //stepLinearLayout是左侧索引的控件，lineSpaceDrawable是中间分割线，lScrollView是滑动面板
        ScrollView lScrollView = new ScrollView(this);
        stepLinearLayout = new VerticalStepLinearLayout(this);
        stepLinearLayout.setPadding(space * 2, 0, space * 2, 0);
        stepLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);//分割线
        GradientDrawable lineSpaceDrawable = DynamicDrawableFactory.cornerRectangleDrawable(Color.TRANSPARENT, 0);//Color.TRANSPARENT背景色设置为透明,这是分割线
        lineSpaceDrawable.setSize(-1, CompatResourceUtils.getDimensionPixelSize(this, dimen.space_8));
        stepLinearLayout.setDividerDrawable(lineSpaceDrawable);
        lScrollView.addView(stepLinearLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        lScrollView.setBackgroundColor(Color.WHITE);
        setContentView(lScrollView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            lScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    stepLinearLayout.updateScroll(v.getScrollY());
                }
            });
        }


        List <PictuerInfor> pictuerInforslist= DataSupport.findAll(PictuerInfor.class);
        if(!pictuerInforslist.isEmpty()){//判断数据库是不是空的
            /*
             * 以下for循环是将从数据库中读取的信息放到data数组中
             * */
            data=new String[pictuerInforslist.size()][3];
            int j=0;
            int x=0;
            int y=0;
            int pictuer_bumber=0;//因为二维数组之前就已经定了了行数，也就是二维数组的长度，但是图片被删除，则数据依次往前填充，后面数组数据为空，之后在for循环中会报错，因为之后读取的是空数据
            for(PictuerInfor pictuerInfor:pictuerInforslist){
                try {
                    BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(pictuerInfor.getImageUri())));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.d(MainActivity.class.getSimpleName(),"监控");
                    pictuer_bumber++;
                    continue;
                }
                data[j++][0]="该黄瓜植株缺少"+pictuerInfor.getFlag()+"元素"+"      "+pictuerInfor.getTime();
                data[x++][1]=pictuerInfor.getImageUri();
                data[y++][2]=pictuerInfor.getFlag();

            }
            /*
             * 以下循环是将data数据放到布局上,倒序输出
             * */
            for ( int i = data.length-pictuer_bumber-1; i >=0; i--) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.topMargin = space;
                ImageView imageView=new ImageView(this);
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(data[i][1])));//讲Uri图像转化成bitmap
                    bitmap=getScaleBitmap(bitmap,1000,700);
                    imageView.setImageBitmap(bitmap);
                    final String flag=data[i][2];
                    final String address=data[i][1];
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(History.this, HistoryToPictuerinformation.class);
                            intent.putExtra("flag",flag);
                            intent.putExtra("address", address);
                            startActivity(intent);
                        }
                    });

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stepLinearLayout.addView(imageView, params);
                TextView textView = new TextView(this);
                textView.setText(data[i][0]);
                textView.setTextSize(18);
                textView.setTextColor(Color.BLACK);
                stepLinearLayout.addView(textView, params);
            }
        }else{
            Toast.makeText(getBaseContext(), "数据库为空！！！", Toast.LENGTH_SHORT).show();
            finish();
        }


    }


    /**
     * 对图片进行缩放
     * @param bitmap
     * @param
     * @return
     * @throws IOException
     */
    private static Bitmap getScaleBitmap(Bitmap bitmap, int Towidth,int Toheight) throws IOException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) Towidth) / width;
        float scaleHeight = ((float) Toheight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

}
