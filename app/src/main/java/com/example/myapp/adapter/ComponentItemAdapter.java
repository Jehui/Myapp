package com.example.myapp.adapter;


import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.myapp.entity.ComponentItem;

import com.example.myapp.R;

import jsc.kit.component.swiperecyclerview.BaseRecyclerViewAdapter;
import jsc.kit.component.swiperecyclerview.BaseViewHolder;
import jsc.kit.component.utils.CompatResourceUtils;
import jsc.kit.component.utils.WindowUtils;
import jsc.kit.component.utils.dynamicdrawable.DynamicDrawableFactory;
import jsc.kit.component.widget.aspectlayout.AspectRatioFrameLayout;
import jsc.kit.component.widget.DotView;
import jsc.kit.component.widget.aspectlayout.AspectRatioHelper;

public class ComponentItemAdapter extends BaseRecyclerViewAdapter<ComponentItem, ComponentItemAdapter.ComponentItemViewHolder> {

    public ComponentItemAdapter() {
    }

    public ComponentItemAdapter(int layoutId) {
        super(layoutId);
    }

    public ComponentItemAdapter(int layoutId, boolean itemClickEnable, boolean itemLongClickEnable) {
        super(layoutId, itemClickEnable, itemLongClickEnable);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View createView(@NonNull ViewGroup parent, int viewType) {
        int radius = CompatResourceUtils.getDimensionPixelSize(parent, R.dimen.space_4);
        AspectRatioFrameLayout aspectRatioLinearLayout = new AspectRatioFrameLayout(parent.getContext());
        aspectRatioLinearLayout.setBaseWhat(AspectRatioHelper.BASE_HORIZONTAL);
        aspectRatioLinearLayout.setAspectX(1);
        aspectRatioLinearLayout.setAspectY(1);
        aspectRatioLinearLayout.setPadding(12, 12, 12, 12);
        View backgroundView = new View(parent.getContext());
        backgroundView.setBackground(WindowUtils.getSelectableItemBackgroundBorderless(parent.getContext()));
        aspectRatioLinearLayout.addView(backgroundView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        aspectRatioLinearLayout.setBackground(DynamicDrawableFactory.cornerRectangleDrawable(0xFF3F51B5, radius));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            aspectRatioLinearLayout.setElevation(radius);
        }
        aspectRatioLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        return aspectRatioLinearLayout;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @NonNull
    @Override
    public ComponentItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AspectRatioFrameLayout v = (AspectRatioFrameLayout) createView(parent, viewType);
        return new ComponentItemViewHolder(v);
    }

    @Override
    public void bindViewHolder(@NonNull ComponentItemViewHolder holder, int position, ComponentItem item, int viewType) {
        String label = item.getLabel();
        holder.tvShortName.setText(label.substring(0, 1).toUpperCase());
        holder.tvComponentName.setText(label);
        holder.dotView.setVisibility(item.isUpdated() ? View.VISIBLE : View.GONE);
    }

    public void setOnItemClickListener(OnItemClickListener<ComponentItem> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class ComponentItemViewHolder extends BaseViewHolder {

        TextView tvShortName;
        TextView tvComponentName;
        DotView dotView;

        ComponentItemViewHolder(AspectRatioFrameLayout itemView) {
            super(itemView);
            int size = CompatResourceUtils.getDimensionPixelSize(itemView, R.dimen.space_48);
            FrameLayout.LayoutParams p1 = new FrameLayout.LayoutParams(size, size);
            p1.gravity = Gravity.CENTER_HORIZONTAL;
            p1.topMargin = 8;
            tvShortName = new TextView(itemView.getContext());
            tvShortName.setGravity(Gravity.CENTER);
            tvShortName.setTextColor(0xFF333333);
            tvShortName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            tvShortName.getPaint().setFakeBoldText(true);
            initViewShapeProvider(tvShortName);
            tvShortName.setBackgroundColor(0xFFF2F2F2);
            itemView.addView(tvShortName, p1);
            //
            FrameLayout.LayoutParams p2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p2.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            p2.bottomMargin = 4;
            tvComponentName = new TextView(itemView.getContext());
            tvComponentName.setTextColor(Color.WHITE);
            tvComponentName.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
            tvComponentName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tvComponentName.setLines(2);
            itemView.addView(tvComponentName, p2);
            //
            int dotSize = CompatResourceUtils.getDimensionPixelSize(itemView, R.dimen.space_12);
            FrameLayout.LayoutParams p3 = new FrameLayout.LayoutParams(dotSize, dotSize);
            p3.gravity = Gravity.RIGHT;
            p3.topMargin = 8;
            p3.rightMargin = 8;
            dotView = new DotView(itemView.getContext());
            dotView.setShape(DotView.CIRCULAR);
            dotView.setBackgroundColor(Color.RED);
            itemView.addView(dotView, p3);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void initViewShapeProvider(View targetView) {
            targetView.setClipToOutline(true);
            targetView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
        }
    }
}
