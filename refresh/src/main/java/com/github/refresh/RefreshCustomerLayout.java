package com.github.refresh;


import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.github.refresh.interfaces.IRefreshDataView;
import com.github.refresh.interfaces.IRefreshStateView;
import com.github.refresh.util.CustomLoadMoreView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;


public class RefreshCustomerLayout extends FrameLayout implements IRefreshDataView {
    public final static int DEFAULT_SIZE = 10;
    public final static int Refresh = 0;
    public final static int LoadMore = 1;
    public final static int Refresh_LoadMore = 2;
    protected BaseQuickAdapter mAdapter;
    private RefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private IRefreshListener mIRefreshListener;
    private Context mContext;
    private boolean isLoadMore;//是否允许加载更多
    private int loadSize = DEFAULT_SIZE;//每次加载条目
    private int totalPage = -1;//加载总页数
    private int pageStartOffset = 0;//起始页
    private int currentPage = pageStartOffset;//当前加载页
    private IRefreshStateView mIRefreshStateView;//与外部对接View的切换
    private int mRefreshType = Refresh;

    public RefreshCustomerLayout(Context context) {
        this(context, null);
    }

    public RefreshCustomerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View content = View.inflate(context, R.layout.view_refresh_layout, null);
        addView(content);

        mRefreshLayout = (RefreshLayout) content.findViewById(R.id.mRefreshLayout);
        mRecyclerView = (RecyclerView) content.findViewById(R.id.mRecycleView);

        mContext = context;

        initAttr(context, attrs);
    }

    private void initLogic() {
        switch (mRefreshType) {
            case Refresh:
                requireRefresh();
                break;
            case LoadMore:
                requireLoadMore();
                break;
            case Refresh_LoadMore:
                requireRefresh();
                requireLoadMore();
                break;
        }
    }

    private void requireLoadMore() {
        if (mAdapter != null) {
            isLoadMore = true;
            mAdapter.getLoadMoreModule().setLoadMoreView(new CustomLoadMoreView());
            mAdapter.getLoadMoreModule().setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    if (mIRefreshListener != null
                            && mRefreshLayout.getCurrentRefreshStatus() == RefreshLayout.RefreshStatus.IDLE) {
                        if (isLoadMore) {
                            mIRefreshListener.onLoadMore(RefreshCustomerLayout.this, currentPage + 1);
                        } else {
                            mAdapter.getLoadMoreModule().loadMoreEnd();
                        }
                    } else {
                        mAdapter.getLoadMoreModule().loadMoreComplete();
                    }
                }
            });
//            mAdapter.setRecyclerView(mRecyclerView);
        }

    }

    private void requireRefresh() {
        mRefreshLayout.setRefreshViewHolder(new NormalRefreshViewHolder(mContext, false));
        mRefreshLayout.setDelegate(new RefreshLayout.RefreshLayoutDelegate() {
            @Override
            public void onRefreshLayoutBeginRefreshing(RefreshLayout refreshLayout) {
                if (mIRefreshListener != null) {
                    mIRefreshListener.onRefresh(RefreshCustomerLayout.this);
                }
            }

            @Override
            public boolean onRefreshLayoutBeginLoadingMore(RefreshLayout refreshLayout) {
                return false;
            }
        });
    }


    private void initAttr(Context context, @Nullable AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.refreshLayout);
        //type

        mRefreshType = ta.getInt(R.styleable.refreshLayout_rl_refresh_type, Refresh);
        //...
        ta.recycle();
    }

    private void setLoadMore(boolean loadMore) {
        isLoadMore = loadMore;
    }

    /**
     * --------------------------------------Setter----------------------------------------------------
     */

    /**
     * 刷新View的类型
     */
    public RefreshCustomerLayout setViewType(@Type int type) {
        this.mRefreshType = type;
        return this;
    }

    /**
     * 设置recyclerView的layoutManager
     */
    public RefreshCustomerLayout setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        this.mRecyclerView.setLayoutManager(layoutManager);
        return this;
    }

    /**
     * 设置recyclerView的ItemDecoration
     */
    public RefreshCustomerLayout setItemDecoration(RecyclerView.ItemDecoration decoration) {
        this.mRecyclerView.addItemDecoration(decoration);
        return this;
    }

    /**
     * 设置回调
     */
    public RefreshCustomerLayout setRefreshListener(IRefreshListener mIRefreshListener) {
        this.mIRefreshListener = mIRefreshListener;
        return this;
    }

    /**
     * 通过内部来处理一些视图状态，以便结束刷新或者加载状态
     */
    public RefreshCustomerLayout setViewStateListener(IRefreshStateView IRefreshStateView) {
        this.mIRefreshStateView = IRefreshStateView;
        return this;
    }

    /**
     * 设置适配器（放于链试的最后调用）
     */
    public void setAdapter(BaseQuickAdapter mAdapter) {
        this.mAdapter = mAdapter;
        initLogic();
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 加载总页数，通常由接口获取，没有可不传，则加载更多由下一次加载的loadSize来判断
     */
    @Override
    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    /**
     * 设置数据源（请求发送后）
     */
    @SuppressWarnings("all")
    public synchronized void setData(List beanList, boolean loadMore) {

        if (beanList == null || beanList.size() == 0) {
            //refresh trigger
            if (!loadMore) {
                mAdapter.setNewData(beanList);
                if (isEmpty() && mIRefreshStateView != null) {
                    mIRefreshStateView.showEmpty();
                }
                mRefreshLayout.endRefreshing();
            } else {
                mAdapter.getLoadMoreModule().loadMoreEnd();
                setLoadMore(false);
            }
            return;
        }
        //refresh trigger
        if (!loadMore) {
            mAdapter.setNewData(beanList);
            currentPage = pageStartOffset;
            mRefreshLayout.endRefreshing();
            setLoadMore(true);
            if (mIRefreshStateView != null) {
                mIRefreshStateView.showContent();
            }
        } else {
            //没有传递totalPage，（验证发生在下次加载时）
            if (totalPage == -1 || totalPage == 0) {
                boolean valid = beanList.size() >= loadSize;
                mAdapter.addData(beanList);
                currentPage++;
                setLoadMore(valid);
                if (valid) {
                    mAdapter.getLoadMoreModule().loadMoreComplete();
                } else {
                    mAdapter.getLoadMoreModule().loadMoreEnd();
                }
                return;
            }
            //有传递totalPage，（验证发生在这次加载后）
            if (currentPage < (totalPage + pageStartOffset) - 1) {
                mAdapter.addData(beanList);
                mAdapter.getLoadMoreModule().loadMoreComplete();
                currentPage++;
                setLoadMore(currentPage < (totalPage + pageStartOffset) - 1);
            } else {
                setLoadMore(false);
                mAdapter.getLoadMoreModule().loadMoreEnd();
            }
        }
    }

    @Override
    public void setMessage(Object error, String content) {
        if (mIRefreshStateView != null) {
            if (isEmpty()) {
                mIRefreshStateView.showMessageFromNet(error, content);
                mRefreshLayout.endRefreshing();
            } else {
                //错误是由 刷新 还是 加载更多 引发的
                if (mRefreshLayout.getCurrentRefreshStatus() != RefreshLayout.RefreshStatus.IDLE) {
                    mRefreshLayout.endRefreshing();
                    mIRefreshStateView.showMessage(content);
                } else {
                    mAdapter.getLoadMoreModule().loadMoreFail();
                }
            }
        }
    }

    /**
     * ---------------------------------------------getter---------------------------------------------
     */

    public void startRequest() {
        if (isEmpty() && mIRefreshStateView != null) {
            mIRefreshStateView.showLoading();
        }
        mRefreshLayout.beginRefreshing();
    }

    public boolean isEmpty() {
        return mAdapter.getData().isEmpty();
    }

    public int getPageStartOffset() {
        return pageStartOffset;
    }

    /**
     * 起始加载偏移页
     */
    public RefreshCustomerLayout setPageStartOffset(int start) {
        this.pageStartOffset = start;
        return this;
    }

    public int getLoadSize() {
        return loadSize;
    }

    /**
     * 每次加载条目数，与保持接口一致!用于在未传totalPage之后判断是否加载更多
     */
    public RefreshCustomerLayout setLoadSize(int size) {
        this.loadSize = size;
        return this;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }


    public RefreshLayout getRefreshLayout() {
        return mRefreshLayout;
    }


    /**
     * ---------------------------------------------interface---------------------------------------------
     */

    public static interface IRefreshListener {
        void onRefresh(RefreshCustomerLayout rcl);

        void onLoadMore(RefreshCustomerLayout rcl, int targetPage);
    }

    @IntDef(value = {Refresh, LoadMore, Refresh_LoadMore})
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
    }

}
