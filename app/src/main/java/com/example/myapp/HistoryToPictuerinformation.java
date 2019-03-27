package com.example.myapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;

import com.example.myapp.ui.BaseActivity;

import java.io.FileNotFoundException;

import jsc.kit.component.archeaderview.PictureArcHeaderView;

public class HistoryToPictuerinformation extends BaseActivity {
    private PictureArcHeaderView picture;
    private TextView textview;
    private TextView textview1;
    private TextView textview_potry;
    private Uri pictuer_uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleBarTitle(getClass().getSimpleName().replace("HistoryToPictuerinformation", "详情"));
        setContentView(R.layout.activity_history_to_pictuerinformation);

        picture = (PictureArcHeaderView) findViewById(R.id.image_pic);
        textview=(TextView)findViewById(R.id.textview) ;
        textview1=(TextView)findViewById(R.id.textview1) ;
        textview_potry=(TextView)findViewById(R.id.textview_potry);

        Intent infor=getIntent();
        String flag=infor.getStringExtra("flag");
        pictuer_uri = Uri.parse(infor.getStringExtra("address"));


        Bitmap bitmap = null;//讲Uri图像转化成bitmap
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(pictuer_uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        picture.setBitmap(bitmap);

        switch (flag){
            case "K":
                textview.setText(String.format("该黄瓜缺少元素: %s ", flag));
                textview_potry.setText(R.string.K);
                textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                textview1.setText(R.string.cucumer);
                break;
            case "N":
                textview.setText(String.format("该黄瓜缺少元素: %s ", flag));
                textview_potry.setText(R.string.N);
                textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                textview1.setText(R.string.cucumer);
                break;
            case "P":
                textview.setText(String.format("该黄瓜缺少元素: %s ", flag));
                textview_potry.setText(R.string.P);
                textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                textview1.setText(R.string.cucumer);
                break;
        }





    }



}
