var app = getApp();
var request = require('../../utils/request.js');
var util = require('../../utils/util.js');

Page({
  data: { userInfo: {}, recentOrders: [] },

  onLoad: function() {
    var userInfo = wx.getStorageSync('userInfo') || {};
    this.setData({ userInfo: userInfo });
  },

  onShow: function() {
    wx.hideTabBar();
    if (!this.checkLogin()) return;
    var userInfo = wx.getStorageSync('userInfo') || {};
    this.setData({ userInfo: userInfo });
    this.loadRecentOrders();
  },

  loadRecentOrders: function() {
    var that = this;
    request.get('/user/order/page', { page: 1, pageSize: 3 }).then(function(res) {
      var orders = (res.data && res.data.length !== undefined) ? res.data : [];
      that.setData({ recentOrders: orders });
    }).catch(function() {
      that.setData({ recentOrders: [] });
    });
  },

  goAddress: function() { wx.navigateTo({ url: '/pages/address/address' }); },
  goHistoryOrder: function() { wx.navigateTo({ url: '/pages/historyOrder/historyOrder' }); },

  // 家政员工作台入口
  goWorkerLogin: function() {
    wx.navigateTo({ url: '/pages/workerLogin/workerLogin' });
  },

  goDetail: function(e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/details/index?id=' + id });
  },

  goPay: function(e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/pay/index?orderId=' + id });
  },

  cancelOrder: function(e) {
    var id = e.currentTarget.dataset.id;
    var that = this;
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

  logout: function() {
    var that = this;
    wx.showModal({
      title: '退出登录',
      content: '确定要清除登录状态吗？',
      success: function(res) {
        if (res.confirm) {
          wx.removeStorageSync('userInfo');
          wx.removeStorageSync('code');
          wx.removeStorageSync('token');
          var app = getApp();
          app.globalData.userInfo = null;
          app.globalData.isLoggedIn = false;
          app.globalData.needReAuth = true;
          that.setData({ userInfo: {} });
          wx.switchTab({ url: '/pages/index/index' });
        }
      }
    });
  },

  checkLogin: function() {
    if (!app.globalData.isLoggedIn) {
      app.globalData.needReAuth = true;
      wx.switchTab({ url: '/pages/index/index' });
      return false;
    }
    return true;
  }
});
