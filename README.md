RefreshLayout 是[BGARefreshLayout](https://github.com/bingoogolapple/BGARefreshLayout-Android "BGARefreshLayout") 和[BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper "BaseRecyclerViewAdapterHelper") 的封装

- 解决了BGARefreshLayout在recyclerView为StaggeredGridLayoutManager时，item高度过高时对刷新判断失效
- 延长刷新动画的时间
- 封装常用控件RefreshCustomerLayout，loadingView在recycleView内部更美观
	- 提供更简易的刷新与加载更多回调，无需判断页数
	- 提供两种加载更多逻辑：总页数、预设每次加载条数
- 兼容原有两个项目用法

> compile 'com.ricky:refreshLayout:0.4'

	allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
   