package com.ricky.refreshlayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.refresh.RefreshCustomerLayout;
import com.github.refresh.interfaces.IRefreshStateView;
import com.github.refresh.util.CommonItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RefreshCustomerLayout rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rl = (RefreshCustomerLayout) findViewById(R.id.mRefreshLayout);

        rl
                .setLayoutManager(new LinearLayoutManager(this))
                .setViewType(RefreshCustomerLayout.Refresh_LoadMore)
                .setPageStartOffset(0)
                .setLoadSize(5)
                .setRefreshListener(new RefreshCustomerLayout.IRefreshListener() {
                    @Override
                    public void onRefresh(RefreshCustomerLayout rcl) {
                        List<String> list = getList();
                        rcl.setData(list, false);
                    }

                    @Override
                    public void onLoadMore(RefreshCustomerLayout rcl, int targetPage) {
                        List<String> loadMoreList = getLoadMoreList();
                        rcl.setData(loadMoreList, true);
                        Log.d("page:", targetPage + "");
                    }
                })
                .setViewStateListener(new IRefreshStateView() {
                    @Override
                    public void showMessageFromNet(Object error, String content) {
                        System.out.println();
                    }

                    @Override
                    public void showMessage(String content) {
                        System.out.println();
                    }

                    @Override
                    public void showEmpty() {
                        System.out.println();
                    }

                    @Override
                    public void showContent() {
                        System.out.println();
                    }

                    @Override
                    public void showLoading() {
                        System.out.println();
                    }
                })
                .setAdapter(new BaseQuickAdapter<String, BaseViewHolder>(R.layout.item,getList()) {
                    @Override
                    protected void convert(BaseViewHolder helper, String item) {
                        helper.setText(R.id.tv, item + ":" + helper.getAdapterPosition());
                    }

                });
        rl.startRequest();
        StatusBarUtil.darkMode(this);
        StatusBarUtil.setPaddingSmart(this,findViewById(R.id.mRefreshLayout));

    }

    public int dp2px(float dpValue) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public List<String> getList() {
        List<String> strings = new ArrayList<>();
        Collections.addAll(strings, "one", "two", "three", "four", "five", "six");
        return strings;
    }

    public List<String> getLoadMoreList() {
        List<String> strings = new ArrayList<>();
        Collections.addAll(strings, "one", "two", "three", "four", "five");
        return strings;
    }
}
