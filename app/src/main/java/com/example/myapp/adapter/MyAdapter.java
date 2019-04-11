package com.example.myapp.adapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.example.myapp.R;
import java.util.List;

public class MyAdapter extends BaseAdapter {

    private List<Bitmap> path;
    private Context context;

    public MyAdapter(List<Bitmap> path, Context context) {
        this.path = path;
        this.context = context;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return path.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return path.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        ViewHolder vh=null;

        if(convertView==null){

            vh=new ViewHolder();

            convertView= LayoutInflater.from(context).inflate(R.layout.grid_item, null);

            vh.img=(ImageView) convertView.findViewById(R.id.img);

            convertView.setTag(vh);
        }else{

            vh=(ViewHolder) convertView.getTag();
        }
        vh.img.setImageBitmap(path.get(position)); //这里要用BitMap解析图片
        return convertView;
    }

    class ViewHolder{
        ImageView img;
    }

}