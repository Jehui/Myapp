package com.example.myapp.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.myapp.Multiple_TakePhoto;
import com.example.myapp.R;
import com.example.myapp.TakephotoOrAblum;
import com.example.myapp.adapter.ComponentItemAdapter;
import com.example.myapp.adapter.ComponentItemDecoration;
import com.example.myapp.entity.ComponentItem;

import jsc.kit.component.baseui.transition.TransitionProvider;
import jsc.kit.component.reboundlayout.ReboundRecyclerView;
import jsc.kit.component.swiperecyclerview.BaseRecyclerViewAdapter;
import jsc.kit.component.utils.dynamicdrawable.DynamicDrawableFactory;

public class ComponentsActivity extends BaseActivity implements View.OnClickListener {

    @Override
    public Transition createExitTransition() {
        return TransitionProvider.createFade(300L);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Transition createReturnTransition() {
        Slide slide = TransitionProvider.createSlide(300L);
        slide.setSlideEdge(Gravity.START);
        return slide;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    //Transition可以设置动画
    public Transition createReenterTransition() {
        Slide slide = TransitionProvider.createSlide(300L);
        slide.setSlideEdge(Gravity.BOTTOM);
        return slide;
    }

    @Override
    public void initComponent() {
        super.initComponent();
        //可以设置背景颜色，
        getWindow().setBackgroundDrawable(DynamicDrawableFactory.cornerRectangleDrawable(0xFF303F9F, 0));
    }


    ComponentItemAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //ReboundRecyclerView实现可以往下拉动，拖拽反弹效果，https://www.jianshu.com/p/c3f2c9f852ef
        ReboundRecyclerView reboundRecyclerView = new ReboundRecyclerView(this);
        RecyclerView recyclerView = reboundRecyclerView.getRecyclerView();
        recyclerView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));//设置布局管理器，3列
        recyclerView.addItemDecoration(new ComponentItemDecoration(this));// //设置分隔线

        setContentView(reboundRecyclerView);
        //把target中的activity改成此活动的名字，也就是java文件的名字，默认的名字就会消失，会被后面的replacement代替
        setTitleBarTitle(getClass().getSimpleName().replace("ComponentsActivity", "作物类型"));

        adapter = new ComponentItemAdapter();
        recyclerView.setAdapter(adapter);

//给Item添加点击事件
        adapter.setOnItemClickListener(new BaseRecyclerViewAdapter.OnItemClickListener<ComponentItem>() {
            @Override
            public void onItemClick(View itemView, int position, ComponentItem item, int viewType) {
                toNewActivity(item);
            }
        });

        List<ComponentItem> items = getComponentItems();
        adapter.setItems(items);

        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // 拖拽的标记，这里允许上下左右四个方向
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT |
                        ItemTouchHelper.RIGHT;
                // 滑动的标记，这里允许左右滑动
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // 移动时更改列表中对应的位置并返回true
                Collections.swap(adapter.getItems(), viewHolder.getAdapterPosition(), target
                        .getAdapterPosition());
                return true;
            }

            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
                // 移动完成后刷新列表
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target
                        .getAdapterPosition());
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // 将数据集中的数据移除
//                adapter.getItems().remove(viewHolder.getAdapterPosition());
                // 刷新列表
//                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        Toast.makeText(this,"请选择检测的作物类型",Toast.LENGTH_SHORT).show();
    }

    private void toNewActivity(ComponentItem item){
        switch (item.getFragmentClassName()){
            case "1":
                showBottomDialog();
                break;
            case "2":
                Toast.makeText(this,"功能待开发，尽请期待",Toast.LENGTH_SHORT).show();
                break;
            case "3":
                Toast.makeText(this,"功能待开发，尽请期待",Toast.LENGTH_SHORT).show();
                break;
            case "4":
                Toast.makeText(this,"功能待开发，尽请期待",Toast.LENGTH_SHORT).show();
                break;
            case "5":
                Toast.makeText(this,"功能待开发，尽请期待",Toast.LENGTH_SHORT).show();
                break;
            case "6":
                Toast.makeText(this,"功能待开发，尽请期待",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private List<ComponentItem> getComponentItems() {
        List<ComponentItem> classItems = new ArrayList<>();
        //true表示红点，更新的意思
        //下面第一个的意思：标签名字，跳转的活动，是否更新（显示红点），跳转活动的顶端的名字
       classItems.add(new ComponentItem("黄瓜\nCucumber", null, false, "1"));
        classItems.add(new ComponentItem("番茄\nTomato",  null, false,"2"));
        classItems.add(new ComponentItem("玉米\nCorn", null, false, "3"));
        classItems.add(new ComponentItem("土豆\nPotato", null,false,"4"));
        classItems.add(new ComponentItem("小麦\nWheat",null,false,"5"));
        classItems.add(new ComponentItem("水稻\nrice", null,false,"6"));

        return classItems;
    }

    private void showBottomDialog(){
        //1、使用Dialog、设置style
        final Dialog dialog = new Dialog(this, R.style.DialogTheme);
        //2、设置布局
        View view = View.inflate(this,R.layout.activity_show_dialog,null);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
        //设置弹出动画
        window.setWindowAnimations(R.style.main_menu_animStyle);
        //设置对话框大小
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        dialog.findViewById(R.id.tv_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //注意下面的context中的this要指明是谁的this，因为这个文件中共有两个，一个是主体的一个是Dialog的。
                //Toast.makeText(ComponentsActivity.this,"启动拍照功能",Toast.LENGTH_SHORT).show();
                String flag="1";
                Intent intent=new Intent(ComponentsActivity.this, TakephotoOrAblum.class);
                intent.putExtra("flag",flag);//extra_data是data的名字，用于后面取值
                //Toast.makeText(ComponentsActivity.this,flag,Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                startActivity(intent);

            }
        });

        dialog.findViewById(R.id.multiple_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //注意下面的context中的this要指明是谁的this，因为这个文件中共有两个，一个是主体的一个是Dialog的。
                //Toast.makeText(ComponentsActivity.this,"启动拍照功能",Toast.LENGTH_SHORT).show();
                String flag="2";
                Intent intent=new Intent(ComponentsActivity.this, TakephotoOrAblum.class);
                intent.putExtra("flag",flag);//extra_data是data的名字，用于后面取值
                //Toast.makeText(ComponentsActivity.this,flag,Toast.LENGTH_SHORT).show();
                Toast.makeText(ComponentsActivity.this, "最多拍摄9张", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                startActivity(intent);

            }
        });

        dialog.findViewById(R.id.tv_take_pic).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                String flag="3";
                Intent intent=new Intent(ComponentsActivity.this, TakephotoOrAblum.class);
                intent.putExtra("flag",flag);//extra_data是data的名字，用于后面取值
                //Toast.makeText(ComponentsActivity.this,flag,Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                startActivity(intent);
            }
        });

        dialog.findViewById(R.id.tv_take_pic_times).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                String flag="4";
                Intent intent=new Intent(ComponentsActivity.this, TakephotoOrAblum.class);
                intent.putExtra("flag",flag);//extra_data是data的名字，用于后面取值
                //Toast.makeText(ComponentsActivity.this,flag,Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                startActivity(intent);
            }
        });

        dialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


    }

    @Override
    public void onClick(View v) {
        //
    }
}
