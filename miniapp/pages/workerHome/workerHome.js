Page({
  data: { workerInfo: {} },

  onShow: function() {
    // 未登录则回登录页
    if (!wx.getStorageSync('workerToken')) {
      wx.redirectTo({ url: '/pages/workerLogin/workerLogin' });
      return;
    }
    this.setData({ workerInfo: wx.getStorageSync('workerInfo') || {} });
  },

  goOrder: function() {
    wx.navigateTo({ url: '/pages/workerOrder/workerOrder' });
  },

  goProfile: function() {
    wx.navigateTo({ url: '/pages/workerProfile/workerProfile' });
  }
});
