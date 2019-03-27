package jsc.kit.component.swiperecyclerview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * refresh component with {@link SwipeRefreshLayout} and {@link RecyclerView}.
 * <br>features:
 * <ul>
 *     <li>1、pull down to refresh</li>
 *     <li>2、pull up to load more</li>
 *     <li>3、custom empty view</li>
 *     <li>4、custom load more view</li>
 * </ul>
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * <br><a href="https://github.com/JustinRoom/JSCKit" target="_blank">https://github.com/JustinRoom/JSCKit</a>
 *
 * @author jiangshicheng
 */
public class SwipeRefreshRecyclerView extends FrameLayout {
    private final String TAG = getClass().getSimpleName();
    private SwipeRefreshLayout swipeRefreshLayout;
    private FrameLayout contentContainer;
    private RecyclerView recyclerView;
    private View emptyView;
    private View loadMoreView;

    private boolean isLoading = false;
    private boolean isHaveMore = false;
    private boolean refreshEnable = true;
    private boolean loadMoreEnable = true;
    private OnCustomRefreshListener customRefreshListener;
    private OnCustomLoadMoreAnimationListener customLoadMoreAnimationListener;

    public SwipeRefreshRecyclerView(Context context) {
        this(context, null);
    }

    public SwipeRefreshRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeRefreshRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        //>>>>>>>>>>>refresh
        swipeRefreshLayout = new SwipeRefreshLayout(context);
        swipeRefreshLayout.setColorSchemeColors(0xFF3F51B5, 0xFF303F9F);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isLoading = true;
                if (customRefreshListener != null) {
                    customRefreshListener.onRefresh();
                }
            }
        });
        addView(swipeRefreshLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        //>>>>>>>>>>data
        contentContainer = new FrameLayout(context);
        recyclerView = new RecyclerView(context);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                if (layoutManager == null || adapter == null) {
                    swipeRefreshLayout.setEnabled(false);
                    return;
                }

                boolean isFirstItemVisible;
                boolean isLastItemVisible;
                int itemCount = adapter.getItemCount();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                    if (linearLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL){
                        swipeRefreshLayout.setEnabled(false);
                        return;
                    }
                    isFirstItemVisible = (itemCount == 0 || linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
                    isLastItemVisible = linearLayoutManager.findLastCompletelyVisibleItemPosition() + 1 == itemCount;
                } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                    if (staggeredGridLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL){
                        swipeRefreshLayout.setEnabled(false);
                        return;
                    }
                    int[] firstItemPositions = staggeredGridLayoutManager.findFirstCompletelyVisibleItemPositions(null);
                    isFirstItemVisible = itemCount == 0 || (firstItemPositions.length > 0 && firstItemPositions[0] == 0);
                    int[] lastItemPositions = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(null);
                    isLastItemVisible = lastItemPositions.length > 0 && (lastItemPositions[lastItemPositions.length - 1] + 1 == itemCount);
                } else {
                    return;
                }
                swipeRefreshLayout.setEnabled(isRefreshEnable() && isFirstItemVisible);

                if (isLoading || !isLoadMoreEnable() || !isHaveMore || !isLastItemVisible)
                    return;

                if (customRefreshListener != null) {
                    isLoading = true;
                    //show your custom loading more animation.
                    if (customLoadMoreAnimationListener != null)
                        customLoadMoreAnimationListener.onLoadMoreStartAnim(SwipeRefreshRecyclerView.this, customRefreshListener);
                    else
                        showScrollUpAnim();
                }
            }
        });
        contentContainer.addView(recyclerView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        swipeRefreshLayout.addView(contentContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //>>>>>>>>>>empty view
        setEmptyView(getDefaultEmptyView(context));
        //>>>>>>>>>>>>>>>load more view
        setLoadMoreView(getDefaultLoadMoreView(context));
    }

    /**
     * reset init state.
     */
    public void reset() {
        setHaveMore(false);
        refreshComplete();
        loadMoreComplete();
    }

    private void showScrollUpAnim() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(swipeRefreshLayout, View.TRANSLATION_Y, 0, -loadMoreView.getMeasuredHeight())
                .setDuration(300);
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                loadMoreView.setVisibility(VISIBLE);
                customRefreshListener.onLoadMore();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private void showScrollDownAnim() {
        loadMoreView.setVisibility(INVISIBLE);
        float translationY = swipeRefreshLayout.getTranslationY();
        if (translationY == 0)
            return;

        ObjectAnimator animator = ObjectAnimator.ofFloat(swipeRefreshLayout, View.TRANSLATION_Y, -translationY, 0)
                .setDuration(300);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        recyclerView.setLayoutManager(layoutManager);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    /**
     * Set refreshing listener.
     *
     * @param customRefreshListener customized refresh listener
     */
    public void setCustomRefreshListener(OnCustomRefreshListener customRefreshListener) {
        this.customRefreshListener = customRefreshListener;
    }

    public void setCustomLoadMoreAnimationListener(OnCustomLoadMoreAnimationListener customLoadMoreAnimationListener) {
        this.customLoadMoreAnimationListener = customLoadMoreAnimationListener;
    }

    public void refreshComplete() {
        isLoading = false;
        swipeRefreshLayout.setRefreshing(false);
    }

    public void loadMoreComplete() {
        isLoading = false;
        if (loadMoreView == null)
            return;

        if (customLoadMoreAnimationListener != null)
            customLoadMoreAnimationListener.onLoadMoreCompleteAnim(this, customRefreshListener);
        else {
            showScrollDownAnim();
        }
    }

    public void setRefreshEnable(boolean refreshEnable) {
        this.refreshEnable = refreshEnable;
        swipeRefreshLayout.setEnabled(refreshEnable);
    }

    public void refresh() {
        refreshDelay(0);
    }

    /**
     * to start refresh.
     *
     * @param delay the delay to refresh
     */
    public void refreshDelay(long delay) {
        swipeRefreshLayout.setRefreshing(true);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                isLoading = true;
                if (customRefreshListener != null) {
                    customRefreshListener.onRefresh();
                }
            }
        }, delay);
    }

    /**
     * Whether it is refreshing or loading more.
     *
     * @return {@code true} loading, else not.
     */
    public boolean isLoading() {
        return isLoading;
    }

    public boolean isRefreshEnable() {
        return refreshEnable;
    }

    /**
     * Open or close loading more feature.
     *
     * @param loadMoreEnable enable load more or not.
     */
    public void setLoadMoreEnable(boolean loadMoreEnable) {
        this.loadMoreEnable = loadMoreEnable;
    }

    /**
     * @return {@code true} enable load more, else disable.
     * @see #setLoadMoreEnable(boolean)
     */
    public boolean isLoadMoreEnable() {
        return loadMoreEnable;
    }

    /**
     * @return {@code true} present that more data can be load.
     * @see #setHaveMore(boolean)
     */
    public boolean isHaveMore() {
        return isHaveMore;
    }

    /**
     * If set true, it presenter that your can scroll up to load next page.
     *
     * @param haveMore set whether have more data.
     */
    public void setHaveMore(boolean haveMore) {
        isHaveMore = haveMore;
    }

    public interface OnCustomRefreshListener {
        /**
         * Do refreshing.
         */
        void onRefresh();

        /**
         * Do loading more.
         */
        void onLoadMore();
    }

    public View getEmptyView() {
        return emptyView;
    }

    /**
     * emptyView hides if {@link RecyclerView#getAdapter()} is null or {@link RecyclerView.Adapter#getItemCount()} is 0, else shows.
     */
    public void showEmptyViewIfNecessary() {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        emptyView.setVisibility((adapter == null || adapter.getItemCount() == 0) ? VISIBLE : GONE);
    }

    public interface OnCustomLoadMoreAnimationListener {
        /**
         * show your custom loading more action.
         *
         * @param swipeRefreshRecyclerView swipeRefreshRecyclerView
         * @param refreshListener refresh listener
         */
        void onLoadMoreStartAnim(@NonNull final SwipeRefreshRecyclerView swipeRefreshRecyclerView, @Nullable final OnCustomRefreshListener refreshListener);

        /**
         * show your custom loading more completed action.
         *
         * @param swipeRefreshRecyclerView swipeRefreshRecyclerView
         * @param refreshListener refresh listener
         */
        void onLoadMoreCompleteAnim(@NonNull final SwipeRefreshRecyclerView swipeRefreshRecyclerView, @Nullable final OnCustomRefreshListener refreshListener);
    }

    /**
     * @param layout_empty layout resource id
     * @see #setEmptyView(View)
     */
    public void setEmptyView(@LayoutRes int layout_empty) {
        setEmptyView(LayoutInflater.from(getContext()).inflate(layout_empty, null));
    }

    /**
     * @param layout_empty layout resource id
     * @param params layout params
     * @see #setEmptyView(View)
     */
    public void setEmptyView(@LayoutRes int layout_empty, LayoutParams params) {
        setEmptyView(LayoutInflater.from(getContext()).inflate(layout_empty, null), params);
    }

    /**
     * Set your customized empty view.
     *
     * @param emptyView empty view
     */
    public void setEmptyView(View emptyView) {
        setEmptyView(emptyView, null);
    }

    /**
     * Set your customized empty view.
     *
     * @param emptyView empty view
     * @param params layout params
     */
    public void setEmptyView(View emptyView, LayoutParams params) {
        if (emptyView == null)
            return;

        if (this.emptyView != null)
            contentContainer.removeView(this.emptyView);
        this.emptyView = emptyView;
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
        }
        contentContainer.addView(emptyView, params);
        emptyView.setVisibility(GONE);
    }

    public View getLoadMoreView() {
        return loadMoreView;
    }

    /**
     * @param layout_load_more layout resource id
     * @see #setEmptyView(View)
     */
    public void setLoadMoreView(@LayoutRes int layout_load_more) {
        setLoadMoreView(LayoutInflater.from(getContext()).inflate(layout_load_more, null));
    }

    /**
     * Set your customized load more view.
     *
     * @param loadMoreView load more view
     */
    public void setLoadMoreView(View loadMoreView) {
        if (loadMoreView == null)
            return;
        if (this.loadMoreView != null)
            removeView(this.loadMoreView);
        this.loadMoreView = loadMoreView;
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        addView(loadMoreView, params);
        loadMoreView.setVisibility(INVISIBLE);
        loadMoreView.measure(0, 0);
    }

    /**
     * the default empty view.
     *
     * @param context context
     * @return the default empty view
     */
    private View getDefaultEmptyView(Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setText("No data.");
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        return textView;
    }

    /**
     * the default load more view.
     *
     * @param context context
     * @return the default load more view.
     */
    private View getDefaultLoadMoreView(Context context) {
        int dp8 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
        TextView tvLoadMore = new TextView(context);
        tvLoadMore.setGravity(Gravity.CENTER);
        tvLoadMore.setBackgroundColor(Color.WHITE);
        tvLoadMore.setTextColor(Color.GREEN);
        tvLoadMore.setText("Loading more...");
        tvLoadMore.setPadding(0, dp8, 0, dp8);
        return tvLoadMore;
    }
}
