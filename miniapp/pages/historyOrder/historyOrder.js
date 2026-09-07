var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

Page({
  data: {
    tabs: ['全部','待付款','待接单','退款中','已取消'],
    tabIndex: 0,
    orderList: [],
    page: 1, pageSize: 10, hasMore: true,
    loading: false
  },

  onLoad: function() { this.loadOrders(); },

  onShow: function() {
    if (this.data.orderList.length > 0) {
      this.setData({ page: 1, orderList: [], hasMore: true });
      this.loadOrders();
    }
  },

  loadOrders: function(isLoadMore) {
    if (this.data.loading) return;
    var that = this;
    var statusMap = [null, 1, 2, 6, 7]; // 全部=null, 待付款=1, 待接单=2, 退款中=6, 已取消=7
    var page = isLoadMore ? this.data.page : 1;
    var s = statusMap[this.data.tabIndex];
    var params = { page: page, pageSize: this.data.pageSize };
    if (s != null) params.status = s;

    this.setData({ loading: true });
    request.get('/user/order/page', params).then(function(res) {
      var newOrders = (res.data && res.data.length !== undefined) ? res.data : [];
      var orderList = isLoadMore ? that.data.orderList.concat(newOrders) : newOrders;
      that.setData({
        orderList: orderList,
        page: page + 1,
        hasMore: newOrders.length >= that.data.pageSize,
        loading: false
      });
    }).catch(function() {
      that.setData({ loading: false });
    });
  },

  loadMore: function() {
    if (!this.data.hasMore) return;
    this.loadOrders(true);
  },

  changeTab: function(e) {
    var idx = e.currentTarget.dataset.index;
    this.setData({ tabIndex: idx, page: 1, orderList: [], hasMore: true });
    this.loadOrders();
  },

  goDetail: function(e) {
    wx.navigateTo({ url: '/pages/details/index?id=' + e.currentTarget.dataset.id });
  },

  goPay: function(e) {
    wx.navigateTo({ url: '/pages/pay/index?orderId=' + e.currentTarget.dataset.id });
  },

  cancelOrder: function(e) {
    var id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '取消订单',
      content: '确定要取消该订单吗？',
      success: function(res) {
        if (res.confirm) {
          wx.navigateTo({ url: '/pages/cancel-reason/index?orderId=' + id + '&from=list' });
        }
      }
    });
  },

  goBack: function() { wx.navigateBack(); },

  onPullDownRefresh: function() {
    this.setData({ page: 1, orderList: [], hasMore: true });
    this.loadOrders();
    wx.stopPullDownRefresh();
  }
});
