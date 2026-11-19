var request = require('../../utils/request.js');

Page({
  data: { workerInfo: {}, pendingCount: 0 },

  onShow: function() {
    // 未登录则回登录页
    if (!wx.getStorageSync('workerToken')) {
      wx.redirectTo({ url: '/pages/workerLogin/workerLogin' });
      return;
    }
    this.setData({ workerInfo: wx.getStorageSync('workerInfo') || {} });
    this.loadPendingCount();
  },

  // 待接单角标：小程序没有实时推送，进页面时主动拉一次
  loadPendingCount: function() {
    var that = this;
    request.get('/worker/order/pending-count').then(function(res) {
      that.setData({ pendingCount: res.data || 0 });
    }).catch(function() { /* 角标拉取失败不影响页面 */ });
  },

  goOrder: function() {
    wx.navigateTo({ url: '/pages/workerOrder/workerOrder' });
  },

  goProfile: function() {
    wx.navigateTo({ url: '/pages/workerProfile/workerProfile' });
  },

  doLogout: function() {
    var that = this;
    wx.showModal({
      title: '退出登录',
      content: '确定要退出家政员账号吗？退出后需重新输入账号密码登录',
      success: function(res) {
        if (res.confirm) {
          wx.removeStorageSync('workerToken');
          wx.removeStorageSync('workerInfo');
          wx.redirectTo({ url: '/pages/workerLogin/workerLogin' });
        }
      }
    });
  }
});
