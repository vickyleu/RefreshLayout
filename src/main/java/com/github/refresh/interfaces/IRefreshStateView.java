package com.github.refresh.interfaces;


/**
 * 用于传递处理之后的对象，在执行setMessage前提下
 */
public interface IRefreshStateView {

    //发生错误，通常由网络请求引起
    void showMessageFromNet(Object error, String content);

    //列表已经有数据时发生错误
    void showMessage(String content);

    //内容为空时
    void showEmpty();

    //显示内容视图
    void showContent();

    //显示加载框视图
    void showLoading();
}
