package com.example.myapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakephotoOrAblum extends AppCompatActivity {
    private  String flag;
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int PHOTO_REQUEST_CAREMA = 1;// 返回拍照后的处理
    public static final int CROP_PHOTO = 2;//显示拍照后的裁剪图片
    public  static final  int CHOOSE_PHOTO=3;//从相册中选择
    public  static  final  int CROP_ABLUM=4;//显示从相册中裁剪的图片
    private Uri imageUri;//照相的uir路径，与choosefromablumpath对应，后者是字符串
    private  Uri userPickedUri;//从相册中选照片的uir路径
    private static final String CURRENT_TAKE_PHOTO_URI = "currentTakePhotoUri";
    public static File tempFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent infor=getIntent();
        flag=infor.getStringExtra("flag");
        MyClick(flag);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // 防止拍照后无法返回当前 activity 时数据丢失
        savedInstanceState.putParcelable(CURRENT_TAKE_PHOTO_URI, imageUri);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable(CURRENT_TAKE_PHOTO_URI);
        }
    }

    public void MyClick(String  flag) {
        switch (flag) {
            case "1":
                if(ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{
                            Manifest.permission.CAMERA},1);
                    /*以上是检测APP有没有得到相应的权限，没有的话调用ActivityCompat.requestPermissions函数，
                     动态申请权限，动态码是1，因为要判断多个权限的获得情况，此处是判断打开照相机权限
                     */
                }else{
                    openCamera(this);
                }

                break;
            case "2":

                break;
            case "3":

                if(ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},3);
                    /*
                     * 以上是判断有没有获得读取存储信息的权限，动态申请码是3。
                     * */
                }else{
                    openAlbum();
                }
                break;
            case "4":
                if(ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},4);
                    /*
                     * 以上是判断有没有获得读取存储信息的权限，动态申请码是4。
                     * */
                }else{
                    Intent intent=new Intent(TakephotoOrAblum.this, Multiple_choice_ablum.class);
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }




    private void openAlbum(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");//引号中的字符串是Action，相当于键值对，是系统内部自己已经定义好了，传入其后，系统自动寻找对应的操作
        intent.setType("image/*");//表示处理的是图片类型
        startActivityForResult(intent,CHOOSE_PHOTO);//打开相册，系统后台处理完后，会返回值到onActivityResult函数中，CHOOSE_PHOTO是对应码
    }

    //以下函数是动态申请权限，与上面的onClick点击事件对应
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera(this);
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case 4:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent=new Intent(TakephotoOrAblum.this, Multiple_choice_ablum.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }



    //只要是进行了startActivityForResult（Intent，xx），都会返回信息到onActivityResult中
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PHOTO_REQUEST_CAREMA:
                if (resultCode == RESULT_OK) {

                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image/*");//需要设置图片来源
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//指定拍照的输出地址
                    startActivityForResult(intent, CROP_PHOTO); // 启动裁剪程序
                }else{
                    finish();//加上finish（）的原因是，有可能打开相机没有拍照直接返回，那就直接销毁此活动，否则会显示当前没有布局的活动
                }
                break;
            case CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    Intent thakeintent=new Intent(TakephotoOrAblum.this,TakePhotoAndChooseAblum.class);
                    thakeintent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//指定拍照的输出地址
                    thakeintent.putExtra("address", imageUri.toString());
                    startActivity(thakeintent);
                    finish();
                }else{
                    finish();
                }
                break;

            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //判断手机系统的版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        //4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        handleImageBeforeKitKat(data);
                    }
                }else{
                    finish();
                }
                break;
            case CROP_ABLUM:
                //并没有用到此case，因为没有使用相册选取图片并裁剪的功能
                Intent thakeintent=new Intent(TakephotoOrAblum.this,TakePhotoAndChooseAblum.class);
                thakeintent.putExtra(MediaStore.EXTRA_OUTPUT, userPickedUri);//指定拍照的输出地址
                thakeintent.putExtra("address", userPickedUri.toString());
                startActivity(thakeintent);
                finish();
                break;
            default:
                break;
        }


    }


    public void openCamera(Activity activity) {
        //獲取系統版本
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        // 激活相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断存储卡是否可以用，可用进行存储
        if (hasSdcard()) {
            SimpleDateFormat timeStampFormat = new SimpleDateFormat(
                    "yyyy_MM_dd_HH_mm_ss");
            String filename = timeStampFormat.format(new Date());
            tempFile = new File(Environment.getExternalStorageDirectory(),
                    filename + ".jpg");
            if (currentapiVersion < 24) {
                // 从文件中创建uri
                imageUri = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            } else {

                //兼容android7.0 使用共享文件的形式
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, tempFile.getAbsolutePath());
                //检查是否有存储权限，以免崩溃
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    Toast.makeText(this, "请开启存储权限", Toast.LENGTH_SHORT).show();
                    return;
                }

                imageUri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }
        }


        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CAREMA
        activity.startActivityForResult(intent, PHOTO_REQUEST_CAREMA);
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitKat(Intent data){
        String imagePath =null;
        Uri uri=data.getData();
        if (DocumentsContract.isDocumentUri(this,uri)){
            //如果是document类型的uri，则通过document id处理
            String docId=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];
                String selection=MediaStore.Images.Media._ID+ "="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);

            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            //如果content类型的Uri，则使用普通的方式处理
            imagePath=getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            //如果是file类型的Uri，直接回去图片的路径即可
            imagePath=uri.getPath();
        }
        userPickedUri = Uri.fromFile(new File(imagePath));
//注释的代码是从相册中选取图片裁剪的功能，觉得没必要，所以取消
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.setDataAndType(userPickedUri, "image/*");//需要设置图片来源
//        intent.putExtra("scale", true);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, userPickedUri);//指定拍照的输出地址
//        startActivityForResult(intent, CROP_ABLUM); // 启动裁剪程序

        Intent thakeintent=new Intent(TakephotoOrAblum.this,TakePhotoAndChooseAblum.class);
        thakeintent.putExtra(MediaStore.EXTRA_OUTPUT, userPickedUri);//指定拍照的输出地址
        thakeintent.putExtra("address", userPickedUri.toString());
        startActivity(thakeintent);
        finish();

    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath=getImagePath(uri,null);
        Uri  userPickedUri = Uri.fromFile(new File(imagePath));
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(userPickedUri, "image/*");//需要设置图片来源
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, userPickedUri);//指定拍照的输出地址
        startActivityForResult(intent, CROP_ABLUM); // 启动裁剪程序

    }

    @TargetApi(Build.VERSION_CODES.O)
    private String getImagePath(Uri uri, String selection){
        String path=null;
        //通过Uri和selection来回去真是的图片路径
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /*
     * 判断sdcard是否被挂载
     */
    public static boolean hasSdcard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
}
